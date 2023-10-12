package nl.infcomtec.pubsub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Just a big bucket of blobs.
 *
 * @author walter
 */
public class BlobPool {

    public final static long millieNanos = 1000000L;
    public final static long startNanos = System.currentTimeMillis() * millieNanos;
    public final static long nanoOffset = System.nanoTime();
    private final static Semaphore uniTimeLock = new Semaphore(1);
    public static final String UUID_OF = "UUIDof:";
    /**
     * Set this to change the maximum age a blob can be.
     *
     * Note that the pool is not automatically cleaned unless you hire a
     * cleaner.
     */
    public static final AtomicInteger expireSeconds = new AtomicInteger(600);

    /**
     * Time in nanoseconds since 1970 as a long. That gives range of AD 1679 to
     * AD 2261 shaving a year or so of both ends to make sure. Whatever happened
     * or happens before or after that is SEP (Someone Else's Problem).
     *
     * @return Time in nanoseconds since 1970 as a long.
     */
    public static long getRealTime() {
        uniTimeLock.acquireUninterruptibly();
        long ret = System.nanoTime() - nanoOffset + startNanos;
        uniTimeLock.release();
        return ret;
    }

    /**
     * Uses getRealTime() but checks a unique value will be returned (next
     * caller will get a guaranteed higher value).
     *
     * @return Time in nanoseconds since 1970 as a long.
     */
    public static long getUniqueTime() {
        long mark = getRealTime();
        long overhead = 0;
        while (mark == getRealTime()) {
            overhead++;
        }
        //System.out.println(overhead);
        return mark;
    }

    /**
     * Utility function.
     *
     * @param content Anything.
     * @return Bytes.
     */
    public static byte[] serialize(Serializable content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            boolean isString = (content instanceof CharSequence);
            if (isString) {
                baos.write(1);
            } else {
                baos.write(0);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                if (isString) {
                    oos.writeUTF(((CharSequence) content).toString());
                } else {
                    oos.writeObject(content);
                }
                oos.flush();
            }
            baos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(BlobPool.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Serializable deserialize(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            int enc = bais.read();
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                switch (enc) {
                    case 1:
                        return ois.readUTF();
                }
                return (Serializable) ois.readObject();
            }
        } catch (Exception ex) {
            Logger.getLogger(BlobPool.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    /**
     * Caller protection, lets keep things reasonable.
     */
    private final long MAX_WAIT_TIME = 10000L;
    /**
     * This is where the blobs live. Or not.
     */
    private final ConcurrentSkipListMap<String, List<Blob>> pool = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Register a new topic.
     *
     * @param topic A hopefully decent name.
     * @return false if the topic existed already.
     */
    public boolean createTopic(String topic) {
        synchronized (pool) {
            if (pool.containsKey(topic)) {
                return false;
            }
            LinkedList<Blob> l = new LinkedList<>();
            pool.put(topic, l);
            return true;
        }
    }

    /**
     * Register a new topic or fetch an existing one.
     *
     * @param topic A hopefully decent name.
     * @return this for chaining.
     */
    public BlobPool withTopic(String topic) {
        createTopic(topic);
        return this;
    }

    /**
     * Register a new topic or fetch an existing one.
     *
     * @param topics Some topic names.
     * @return this for chaining.
     */
    public BlobPool withTopics(String... topics) {
        if (null != topics) {
            for (String topic : topics) {
                createTopic(topic);
            }
        }
        return this;
    }

    /**
     * For the curious.
     *
     * @return Known topics.
     */
    public List<String> listTopics() {
        LinkedList<String> ret = new LinkedList<>(pool.keySet());
        return ret;
    }

    /**
     * Tell the world.
     *
     * @param blob The message / object.
     * @return An empty string (good) or an error (not good).
     */
    public String submit(Blob blob) {
        synchronized (pool) {
            List<Blob> list = pool.get(blob.topic);
            if (null == list) {
                return "No such topic";
            }
            list.add(0, blob);
            pool.notifyAll();
        }
        return "";
    }

    /**
     * Poll for messages.
     *
     * @param topic Topic to check.
     * @param nTime Time of last message, use 0 to start at the beginning.
     * @return A Result.
     */
    public Result poll(String topic, long nTime) {
        synchronized (pool) {
            List<Blob> msgs = pool.get(topic);
            if (null == msgs) {
                return new Result(Results.NoSuchTopic, null);
            }
            Blob fnd = null;
            for (Blob msg : msgs) {
                if (msg.nTime > nTime) {
                    fnd = msg;
                } else {
                    break;
                }
            }
            return new Result(null == fnd ? Results.NoMessage : Results.NewMessage, fnd);
        }
    }

    /**
     * Fetch newest (disregard any older).
     *
     * @param topic Topic to check.
     * @return A Result.
     */
    public Result fetchNewest(String topic) {
        synchronized (pool) {
            List<Blob> msgs = pool.get(topic);
            if (null == msgs) {
                return new Result(Results.NoSuchTopic, null);
            }
            Blob fnd = msgs.isEmpty() ? null : msgs.get(0);
            return new Result(null == fnd ? Results.NoMessage : Results.NewMessage, fnd);
        }
    }

    /**
     * Wait for a message to arrive.
     *
     * @param topic Topic to check.
     * @param nTime Time of last message, use 0 to start at the beginning.
     * @param milliesMaxWait Time to idle (capped).
     * @return A Result.
     */
    public Result waitForMessage(String topic, long nTime, int milliesMaxWait) {
        long until = System.currentTimeMillis() + Math.min(MAX_WAIT_TIME, milliesMaxWait);
        do {
            synchronized (pool) {
                List<Blob> msgs = pool.get(topic);
                if (null == msgs) {
                    return new Result(Results.NoSuchTopic, null);
                }
                Blob fnd = null;
                for (Blob msg : msgs) {
                    if (msg.nTime > nTime) {
                        fnd = msg;
                    } else {
                        break;
                    }
                }
                if (null == fnd) {
                    try {
                        pool.wait(until - System.currentTimeMillis());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BlobPool.class.getName()).log(Level.SEVERE, null, ex);
                        Thread.currentThread().interrupt();
                        return new Result(Results.NoMessage, null);
                    }
                } else {
                    return new Result(Results.NewMessage, fnd);
                }
            }
        } while (System.currentTimeMillis() < until);
        return new Result(Results.NoMessage, null);
    }

    /**
     * Wait for a message to arrive.<p>
     * <b>Note:</b>Do not use poolCleaner if this is used.</p>
     *
     * @param topic Topic to check.
     * @param nTime Time of last message, use 0 to start at the beginning.
     * @return A Result.
     */
    public Result waitForever(String topic, long nTime) {
        while (true) {
            synchronized (pool) {
                List<Blob> msgs = pool.get(topic);
                if (null == msgs) {
                    return new Result(Results.NoSuchTopic, null);
                }
                Blob fnd = null;
                for (Blob msg : msgs) {
                    if (msg.nTime > nTime) {
                        fnd = msg;
                    } else {
                        break;
                    }
                }
                if (null == fnd) {
                    try {
                        pool.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BlobPool.class.getName()).log(Level.SEVERE, null, ex);
                        Thread.currentThread().interrupt();
                        return new Result(Results.NoMessage, null);
                    }
                } else {
                    return new Result(Results.NewMessage, fnd);
                }
            }
        }
    }

    /**
     * Blocks the current thread and waits for a new message to arrive in any
     * topic that has a timestamp greater than the given {@code nTime}. Returns
     * as soon as such a message is found. Aka the neighbors and their
     * binoculars.
     *
     * @param nTime The timestamp to compare against. Set to 0 to start
     * monitoring from the beginning of available messages.
     * @return A {@link Result} object containing the status and the message if
     * found. The status is {@code Results.NewMessage} if a new message is found
     * and {@code Results.NoMessage} if the method is interrupted.
     */
    public Result monitor(long nTime) {
        while (true) {
            synchronized (pool) {
                for (List<Blob> msgs : pool.values()) {
                    Blob fnd = null;
                    for (Blob msg : msgs) {
                        if (msg.nTime > nTime) {
                            fnd = msg;
                        } else {
                            break;
                        }
                    }
                    if (null == fnd) {
                        try {
                            pool.wait();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return new Result(Results.NoMessage, null);
                        }
                    } else {
                        return new Result(Results.NewMessage, fnd);
                    }
                }
            }
        }
    }

    /**
     * Close a topic.
     * <p>
     * <b>Note:</b>This also removes all messages in the topic, read or not.</p>
     * <p>
     * <b>Note:</b>Do not use when waitForever is used.</p>
     *
     * @param topic To close.
     */
    public void poolCleaner(String topic) {
        pool.remove(topic);
    }

    /**
     * Hire a pool cleaner.
     *
     * @return In case someday you want to fire the cleaner.
     */
    public Thread hireCleaner() {
        Thread ret = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    long timeOfDeath = getRealTime() - expireSeconds.get() * 1000000000L;
                    synchronized (pool) {
                        for (List<Blob> blobs : pool.values()) {
                            for (Iterator<Blob> it = blobs.iterator(); it.hasNext();) {
                                Blob b = it.next();
                                if (b.nTime < timeOfDeath) {
                                    it.remove();
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(Math.max(1000, expireSeconds.get() * 100));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BlobPool.class.getName()).log(Level.FINE, "Cleaner fired");
                        return;
                    }
                }
            }
        });
        ret.start();
        return ret;
    }

    /**
     * Fire the cleaner.
     *
     * @param cleaner Thread as returned by hireCleaner.
     */
    public void fireCleaner(Thread cleaner) {
        cleaner.interrupt();
    }

    /**
     * Result of fetching a blob.
     */
    public static class Result {

        public final Results found;
        public final Blob blob;

        public Result(Results found, Blob blob) {
            this.found = found;
            this.blob = blob;
        }

        @Override
        public String toString() {
            return "Result{" + "found=" + found + ", blob=" + blob + '}';
        }
    }

    public Consumer getConsumer(String topic) {
        return new Consumer(this, topic);
    }

    public Producer getProducer(String topic) {
        return new Producer(this, topic);
    }

    /**
     * Returns a String from a topic
     */
    public static class Consumer<T extends Serializable> implements Callable<T> {

        private final BlobPool pool;
        private final String topic;
        private long nTime = 0;

        /**
         * Start reading from the beginning of time.
         *
         * @param pool
         * @param topic
         */
        public Consumer(BlobPool pool, String topic) {
            this.pool = pool;
            this.topic = topic;
        }

        /**
         * Start at the specified point in time.
         *
         * @param pool
         * @param topic
         * @param nTime Usually from a prior received Blob.
         */
        public Consumer(BlobPool pool, String topic, long nTime) {
            this.pool = pool;
            this.topic = topic;
            this.nTime = nTime;
        }

        @Override
        public T call() throws Exception {
            while (true) {
                Result mRes = pool.waitForMessage(topic, nTime, 1000);
                if (mRes.found == Results.NewMessage) {
                    nTime = mRes.blob.nTime;
                    return (T) mRes.blob.getData();
                }
                if (mRes.found == Results.NoSuchTopic) {
                    throw new Exception("No such topic");
                }
            }
        }
    }

    /**
     * Sends String objects to a topic.
     */
    public static class Producer<T extends Serializable> {

        private final BlobPool pool;
        private final String topic;

        public Producer(BlobPool pool, String topic) {
            this.pool = pool;
            this.topic = topic;
        }

        public void send(T obj) {
            pool.submit(new Blob(topic, obj));
        }
    }

    public enum Results {
        NoSuchTopic, NoMessage, NewMessage
    }
}
