package nl.infcomtec.pubsub;

/**
 * A truly generic blob of content.
 *
 * @author walter
 */
public class Blob {

    public final String topic;
    public final long nTime;
    /**
     * Why not just make this public? So we can one day use this with serialization over
     * the wire, for instance.
     */
    protected final Object content;

    /**
     * Create a blob with some content.
     *
     * @param topic As returned from BlobPool createTopic().
     * @param content Object to encapsulate.
     */
    public Blob(String topic, Object content) {
        this.topic = topic;
        this.nTime = BlobPool.getUniqueTime();
        this.content = content;
    }

    @Override
    public String toString() {
        return "Blob{topic=" + topic + ", nTime=" + nTime + ", data=" + (null == content ? "(null)" : content.getClass().getSimpleName()) + '}';
    }

    /**
     * Get the message from this blob.
     *
     * @return The original content.
     */
    public Object getContent() {
        return content;
    }
}
