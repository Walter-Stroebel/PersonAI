package nl.infcomtec.pubsub;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.infcomtec.tools.PandocConverter;

/**
 * Holds a blob of text for generic editing and representation purposes.
 *
 * @author Walter Stroebel
 */
public class TextBlob {

    private final StringBuilder asPlain;
    private final StringBuilder asHTML;
    private TextType lastSet = TextType.MARKDOWN;
    private boolean mdValid = true;
    private boolean htmlValid = true;
    public AtomicLong lastConversionNanos = new AtomicLong();
    public AtomicInteger version = new AtomicInteger(0);
    private final ConcurrentSkipListMap<String, BlobPool.Consumer> rxTopics = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<String, BlobPool.Producer> txTopics = new ConcurrentSkipListMap<>();
    private final StringBuilder asMarkDown;
    private boolean plainValid = true;
    private final BlobPool pool;

    public TextBlob(BlobPool pool) {
        this.pool = pool;
        asPlain = new StringBuilder();
        asMarkDown = new StringBuilder();
        asHTML = new StringBuilder();
    }

    public TextBlob withConsumer(String topic) {
        rxTopics.put(topic, pool.getConsumer(topic));
        return this;
    }

    public TextBlob withProducer(String topic) {
        txTopics.put(topic, pool.getProducer(topic));
        return this;
    }

    public synchronized String getPlain() {
        if (!plainValid) {
            long nanos = System.nanoTime();
            asPlain.setLength(0);
            asPlain.append(new PandocConverter().convertMarkdownToText(getMarkDown()));
            plainValid = true;
            lastConversionNanos.set(System.nanoTime() - nanos);
        }
        return asPlain.toString();
    }

    public synchronized String getHTML() {
        if (!htmlValid) {
            long nanos = System.nanoTime();
            asHTML.setLength(0);
            asHTML.append(new PandocConverter().convertMarkdownToHTML(getMarkDown()));
            htmlValid = true;
            lastConversionNanos.set(System.nanoTime() - nanos);
        }
        return asHTML.toString();
    }

    public synchronized String getMarkDown() {
        if (!mdValid) {
            long nanos = System.nanoTime();
            asMarkDown.setLength(0);
            asMarkDown.append(new PandocConverter().convertHTMLToMarkdown(asHTML.toString()));
            mdValid = true;
            lastConversionNanos.set(System.nanoTime() - nanos);
        }
        return asMarkDown.toString();
    }

    public synchronized void setMarkDown(String text) {
        version.incrementAndGet();
        asMarkDown.setLength(0);
        asMarkDown.append(text);
        lastSet = TextType.MARKDOWN;
        mdValid = true;
        htmlValid = false;
        plainValid = false;
        if (!txTopics.isEmpty()) {
            tellAll(new TypedText(TextType.MARKDOWN, text));
        }
    }

    public synchronized void setHTML(String text) {
        version.incrementAndGet();
        asHTML.setLength(0);
        asHTML.append(text);
        lastSet = TextType.HTML;
        htmlValid = true;
        mdValid = false;
        plainValid = false;
        if (!txTopics.isEmpty()) {
            tellAll(new TypedText(TextType.HTML, text));
        }
    }

    private void tellAll(TypedText tt) {
        for (Map.Entry<String, BlobPool.Producer> ent : txTopics.entrySet()) {
            ent.getValue().send(tt);
        }
    }

    /**
     * Set from typed text.
     * <p>
     * <b>Note</b> that there is no direct support to set plain text -- it is
     * handled as MarkDown.
     * </p>
     *
     * @param tt
     */
    public void set(TypedText tt) {
        switch (tt.type) {
            case HTML:
                setHTML(tt.text);
                break;
            case MARKDOWN:
            case PLAIN:
                setMarkDown(tt.text);
                break;
        }
    }

    public void feedTopic(String topic, final BlobPool.Producer prod) {
        txTopics.put(topic, prod);
    }

    public void followTopic(final String topic, final BlobPool.Consumer cons) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Object messageFromTopic = cons.call();
                        if (null != messageFromTopic) {
                            if (messageFromTopic instanceof TypedText) {
                                set((TypedText) messageFromTopic);
                            } else {
                                Logger.getLogger(TextBlob.class.getName()).log(Level.WARNING,
                                        "Not a TypedText: {0}", messageFromTopic.getClass().getName());
                            }
                        } else {
                            Logger.getLogger(TextBlob.class.getName()).log(Level.INFO,
                                    "Lost connection to topic: {0}", topic);
                            break;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(TextBlob.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * Defer to the pool.
     *
     * @param topic
     * @return
     */
    public BlobPool.Consumer getConsumer(String topic) {
        return pool.getConsumer(topic);
    }

    /**
     * Defer to the pool.
     *
     * @param topic
     * @return
     */
    public BlobPool.Producer getProducer(String topic) {
        return pool.getProducer(topic);
    }

    /**
     * For Pub-Sub.
     *
     */
    public static class TypedText {

        public final TextType type;
        public final String text;

        public TypedText(TextType type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String toString() {
            return "TypedText{"
                    + "type=" + type
                    + ", text="
                    + (null != text && text.length() > 20 ? text.substring(0, 20) : text)
                    + '}';
        }
    }

    /**
     * Simple text type.
     */
    public enum TextType {
        MARKDOWN, HTML, PLAIN
    }
}
