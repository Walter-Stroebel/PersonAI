package nl.infcomtec.pubsub;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Pub/Sub Topic.
 *
 * @author Walter Stroebel
 */
public class Topic implements AutoCloseable {

    private final String name;
    private final ConcurrentLinkedDeque<byte[]> deque;
    private final ConcurrentHashMap<UUID, Integer> consumerOffsets;
    private static final ConcurrentSkipListMap<String, Topic> topics = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Semaphore newMessages = new Semaphore(0);
    /**
     * Seconds.
     */
    private static final long INFINITY = 1;

    /**
     * Internal constructor.
     */
    private Topic(String name) {
        this.name = name;
        this.deque = new ConcurrentLinkedDeque<>();
        this.consumerOffsets = new ConcurrentHashMap<>();
    }

    /**
     * Get an existing topic or create a new one.
     *
     * @param name The topic name.
     * @return A Topic.
     */
    public static Topic openOrCreate(String name) {
        synchronized (topics) {
            Topic topic = topics.get(name);
            if (null != topic) {
                return topic;
            }
            topics.put(name, topic = new Topic(name));
            return topic;
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Register a new consumer and return its UUID.
     *
     * @return New unique ID.
     */
    public UUID subscribe() {
        UUID consumerId = UUID.randomUUID();
        consumerOffsets.put(consumerId, 0);
        return consumerId;
    }

    /**
     * Unsubscribe.
     *
     * @param uuid Consumer Id.
     */
    public void unsubscribe(UUID uuid) {
        consumerOffsets.remove(uuid);
    }

    /**
     * Producers will use this method to publish messages to the Topic.
     *
     * @param message The message.
     * @return false if topic is closed.
     */
    public boolean publish(byte[] message) {
        if (topics.containsKey(this.name)) {
            deque.offer(message);
            newMessages.release();
            return true;
        }
        return false;
    }

    /**
     * Consumers will use this method to poll messages from the Topic.
     *
     * @param consumerId
     * @param waitFor Wait this long for new messages. If null, wait forever.
     * @return null if the consumer Id was not subscribed, waitFor expired or
     * topic was closed.
     */
    public byte[] consume(UUID consumerId, Duration waitFor) {
        if (!topics.containsKey(name)) {
            return null;
        }
        Integer offset = consumerOffsets.get(consumerId);
        if (offset == null) {
            return null;  // Consumer not registered
        }

        Iterator<byte[]> iterator = deque.iterator();
        int i = 0;
        byte[] message = null;

        while (iterator.hasNext()) {
            message = iterator.next();
            if (i >= offset) {
                break;
            }
            i++;
        }

        if (message != null) {
            // Update consumer's offset
            consumerOffsets.put(consumerId, offset + 1);

            // Cleanup: remove messages that have been read by all consumers
            cleanup();
        } else if (null != waitFor) {
            try {
                boolean gotOne = newMessages.tryAcquire(waitFor.toNanos(), TimeUnit.NANOSECONDS);
                if (gotOne) {
                    message = consume(consumerId, waitFor);
                    newMessages.release();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            }
        } else {
            try {
                boolean gotOne = newMessages.tryAcquire(INFINITY, TimeUnit.SECONDS);
                if (gotOne) {
                    message = consume(consumerId, waitFor);
                    newMessages.release();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return message;
    }

    /**
     * Remove messages that all consumers have read
     *
     */
    private void cleanup() {
        int minOffset = Integer.MAX_VALUE;
        for (Integer offset : consumerOffsets.values()) {
            minOffset = Math.min(minOffset, offset);
        }

        Iterator<byte[]> iterator = deque.iterator();
        int i = 0;
        while (iterator.hasNext() && i < minOffset) {
            iterator.next();
            iterator.remove();
            i++;
        }
    }

    @Override
    public void close() throws Exception {
        topics.remove(name);
        newMessages.release(50);
    }
}
