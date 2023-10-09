package nl.infcomtec.advswing;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

    private enum LastSet {
        PLAIN, HTML, RTF
    }
    private LastSet lastSet = LastSet.PLAIN;
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
        lastSet = LastSet.PLAIN;
        plainValid = true;
    }

    public synchronized void setHTML(String text) {
        version.incrementAndGet();
        asHTML.setLength(0);
        asHTML.append(text);
        lastSet = LastSet.HTML;
        htmlValid = true;
    }

    public synchronized void setRTF(String text) {
        version.incrementAndGet();
        asRTF.setLength(0);
        asRTF.append(text);
        lastSet = LastSet.RTF;
        rtfValid = true;
    }
}
