/**
 *
 * @author walter
 */
package nl.infcomtec.pubsub;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.util.UUID;

public abstract class Consumer<T> {

    private final Topic topic;
    private final UUID consumerId;
    /**
     * Default time to wait before returning null from consume.
     */
    private final Duration defDur;
    private Thread t;

    /**
     * Constructor, waits for default 1 second.
     *
     * @param topic
     */
    public Consumer(Topic topic) {
        this.topic = topic;
        this.consumerId = topic.subscribe();
        this.defDur = Duration.ofSeconds(1);
    }

    /**
     * Constructor, waits for specified duration.
     *
     * @param topic
     * @param waitFor Default time to wait before returning null from consume.
     * May be null to wait forever.
     */
    public Consumer(Topic topic, Duration waitFor) {
        this.topic = topic;
        this.consumerId = topic.subscribe();
        this.defDur = waitFor;
    }

    protected byte[] consume() {
        return topic.consume(consumerId, defDur);
    }

    protected byte[] consume(Duration waitFor) {
        return topic.consume(consumerId, waitFor);
    }

    /**
     * Must be implemented to return a message of the proper type.
     *
     * @param waitFor Optional poll frequency specifier. See the sample
     * consumers for usage.
     * @return null if consume returned null, else the message.
     * @throws Exception generic as an implementation might have its own
     * exceptions.
     */
    public abstract T getMessageFromTopic(Duration... waitFor) throws Exception;

    public synchronized void setAutoTask(Runnable r) {
        if (null != t) {
            t.interrupt();
        }
        t = new Thread(r);
        t.start();
    }

    /**
     * Sample consumer for generic objects
     */
    public static Consumer<Object> anyObject(final String topicName) {
        return new Consumer<Object>(Topic.openOrCreate(topicName)) {
            @Override
            public Object getMessageFromTopic(Duration... waitFor) throws Exception {
                byte[] msg = consume(0 == waitFor.length ? null : waitFor[0]);
                if (null != msg) {
                    try ( ByteArrayInputStream bais = new ByteArrayInputStream(msg)) {
                        try ( ObjectInputStream ois = new ObjectInputStream(bais)) {
                            return ois.readObject();
                        }
                    }
                }
                return null;
            }
        };
    }

    /**
     * Sample consumer for Strings.
     */
    public static Consumer<String> stringUTF(final String topicName) {
        return new Consumer<String>(Topic.openOrCreate(topicName)) {
            @Override
            public String getMessageFromTopic(Duration... waitFor) throws Exception {
                byte[] msg = consume(0 == waitFor.length ? null : waitFor[0]);
                if (null != msg) {
                    try ( ByteArrayInputStream bais = new ByteArrayInputStream(msg)) {
                        try ( ObjectInputStream ois = new ObjectInputStream(bais)) {
                            return ois.readUTF();
                        }
                    }
                }
                return null;
            }
        };
    }
}
