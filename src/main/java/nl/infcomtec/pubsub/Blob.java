package nl.infcomtec.pubsub;

import java.io.Serializable;

/**
 *
 * @author walter
 */
public class Blob {

    public final String topic;
    public final long nTime;
    public final byte[] data;

    /**
     * Create a message to send.
     *
     * Note: check logging if data is null: most likely your object failed to
     * serialize.
     *
     * @param topic As returned from BlobPool createTopic().
     * @param content Object to send.
     */
    public Blob(String topic, Serializable content) {
        this.topic = topic;
        this.nTime = BlobPool.getUniqueTime();
        this.data = BlobPool.serialize(content);
    }

    @Override
    public String toString() {
        return "Blob{topic=" + topic + ", nTime=" + nTime + ", data=" + data.length + '}';
    }
    public Serializable getData(){
        return BlobPool.deserialize(data);
    }
}
