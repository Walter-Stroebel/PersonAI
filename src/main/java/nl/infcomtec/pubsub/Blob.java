package nl.infcomtec.pubsub;

import java.io.Serializable;

/**
 * A truly generic blob of data.
 *
 * @author walter
 */
public class Blob {

    public final String topic;
    public final long nTime;
    public final byte[] data;

    /**
     * Create a blob with some content.
     *
     * @param topic As returned from BlobPool createTopic().
     * @param ser Serialization methods.
     * @param content Object to encapsulate.
     */
    public Blob(String topic, Serialization ser, Serializable content) {
        this.topic = topic;
        this.nTime = BlobPool.getUniqueTime();
        this.data = ser.serialize(content);
    }

    @Override
    public String toString() {
        return "Blob{topic=" + topic + ", nTime=" + nTime + ", data=" + data.length + '}';
    }

    /**
     * Get the message from this blob.
     *
     * @param ser Serialization methods.
     * @return The original content.
     */
    public Serializable getData(Serialization ser) {
        return ser.deserialize(data);
    }
}
