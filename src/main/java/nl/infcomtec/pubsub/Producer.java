package nl.infcomtec.pubsub;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Send messages to a topic.
 *
 * @author Walter Stroebel
 * @param <T> Type of the messages.
 */
public abstract class Producer<T> {

    private final Topic topic;

    /**
     * Constructor.
     *
     * @param topic Topic as gotten earlier.
     */
    public Producer(Topic topic) {
        this.topic = topic;
    }

    /**
     * Constructor.
     *
     * @param topicName Topic as gotten earlier.
     */
    public Producer(String topicName) {
        this.topic = Topic.openOrCreate(topicName);
    }
/**
 * Send a message to the topic.
 * @param message Raw message.
 * @return false if the topic was closed.
 */
    protected boolean produce(byte[] message) {
        if (!topic.publish(message)) {
            try {
                topic.close();
            } catch (Exception ex) {
                // artefact: cannot fail
            }
            return false;
        }
        return true;
    }

    /**
     * Publish a message.
     *
     * @param message Any object.
     * @throws Exception generic as an implementation might have its own
     * exceptions.
     */
    public abstract void sendMessage(T message) throws Exception;

    /** 
     * Sample producer for any kind of object.
     * @param topicName Name of the topic.
     * @return 
     */
    public static Producer<Object> anyObject(String topicName) {
        return new Producer<Object>(Topic.openOrCreate(topicName)) {
            @Override
            public void sendMessage(Object message) throws Exception {
                try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    try ( ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                        oos.writeObject(message);
                    }
                    baos.flush();
                    produce(baos.toByteArray());
                }
            }
        };
    }

    /** 
     * Sample producer for any kind of object.
     * @param topicName Name of the topic.
     * @return 
     */
    public static Producer<String> stringUTF(String topicName) {
        return new Producer<String>(Topic.openOrCreate(topicName)) {
            @Override
            public void sendMessage(String message) throws Exception {
                try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    try ( ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                        oos.writeUTF(message);
                    }
                    baos.flush();
                    produce(baos.toByteArray());
                }
            }
        };
    }
}
