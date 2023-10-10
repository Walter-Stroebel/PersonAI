package nl.infcomtec.advswing;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.infcomtec.pubsub.Consumer;
import nl.infcomtec.pubsub.Producer;
import nl.infcomtec.tools.PandocConverter;

/**
 * Holds a blob of text for generic editing and representation purposes.
 *
 * @author Walter Stroebel
 */
public class TextBlob {

    private final StringBuilder asPlain;
    private final StringBuilder asHTML;
    private final StringBuilder asRTF;

    private TextType lastSet = TextType.PLAIN;
    private boolean plainValid = true;
    private boolean htmlValid = true;
    private boolean rtfValid = true;
    public AtomicLong lastConversionNanos = new AtomicLong();
    public AtomicInteger version = new AtomicInteger(0);

    public TextBlob() {
        asPlain = new StringBuilder();
        asHTML = new StringBuilder();
        asRTF = new StringBuilder();
    }

    public synchronized String getPlain() {
        if (!plainValid) {
            long nanos = System.nanoTime();
            asPlain.setLength(0);
            switch (lastSet) {
                case HTML:
                    asPlain.append(new PandocConverter().convertHTMLToMarkdown(asHTML.toString()));
                    break;
                case RTF:
                    asPlain.append(new PandocConverter().convertRTFToMarkdown(asHTML.toString()));
                    break;
                case PLAIN:
                    break;
            }
            plainValid = true;
            lastConversionNanos.set(System.nanoTime() - nanos);
        }
        return asPlain.toString();
    }

    public synchronized String getHTML() {
        if (!htmlValid) {
            long nanos = System.nanoTime();
            asHTML.setLength(0);
            switch (lastSet) {
                case HTML:
                    break;
                case RTF:
                    asHTML.append(new PandocConverter().convertMarkdownToHTML(getPlain()));
                    break;
                case PLAIN:
                    asHTML.append(new PandocConverter().convertMarkdownToHTML(getPlain()));
                    break;
            }
            htmlValid = true;
            lastConversionNanos.set(System.nanoTime() - nanos);
        }
        return asHTML.toString();
    }

    public synchronized String getRTF() {
        if (!rtfValid) {
            long nanos = System.nanoTime();
            asRTF.setLength(0);
            switch (lastSet) {
                case HTML:
                    asRTF.append(new PandocConverter().convertMarkdownToRTF(getPlain()));
                    break;
                case RTF:
                    break;
                case PLAIN:
                    asRTF.append(new PandocConverter().convertMarkdownToRTF(getPlain()));
                    break;
            }
            rtfValid = true;
            lastConversionNanos.set(System.nanoTime() - nanos);
        }
        return asRTF.toString();
    }

    public synchronized void setPlain(String text) {
        version.incrementAndGet();
        asPlain.setLength(0);
        asPlain.append(text);
        lastSet = TextType.PLAIN;
        plainValid = true;
        if (!tellTheWorld.isEmpty()) {
            tellAll(new TypedText(TextType.PLAIN, text));
        }
    }

    public synchronized void setHTML(String text) {
        version.incrementAndGet();
        asHTML.setLength(0);
        asHTML.append(text);
        lastSet = TextType.HTML;
        htmlValid = true;
        if (!tellTheWorld.isEmpty()) {
            tellAll(new TypedText(TextType.HTML, text));
        }
    }

    public synchronized void setRTF(String text) {
        version.incrementAndGet();
        asRTF.setLength(0);
        asRTF.append(text);
        lastSet = TextType.RTF;
        rtfValid = true;
        if (!tellTheWorld.isEmpty()) {
            tellAll(new TypedText(TextType.RTF, text));
        }
    }

    private void tellAll(TypedText tt) {
        for (Iterator<Producer<TypedText>> it = tellTheWorld.iterator(); it.hasNext();) {
            Producer<TypedText> p = it.next();
            try {
                if (!p.sendMessage(tt)) {
                    it.remove();
                }
            } catch (Exception ex) {
                Logger.getLogger(TextBlob.class.getName()).log(Level.SEVERE, null, ex);
                it.remove();
            }
        }
    }

    public void set(TypedText tt) {
        switch (tt.type) {
            case HTML:
                setHTML(tt.text);
                break;
            case RTF:
                setRTF(tt.text);
                break;
            case PLAIN:
                setPlain(tt.text);
                break;
        }
    }
    private ConcurrentLinkedQueue<Producer<TypedText>> tellTheWorld = new ConcurrentLinkedQueue<>();

    public void feedTopic(final Producer<TypedText> prod) {
        tellTheWorld.add(prod);
    }

    public void followTopic(final Consumer<TypedText> cons) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TypedText messageFromTopic = cons.getMessageFromTopic(Duration.ofSeconds(1));
                        if (null != messageFromTopic) {
                            set(messageFromTopic);
                        } else {
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
        PLAIN, HTML, RTF
    }
}
