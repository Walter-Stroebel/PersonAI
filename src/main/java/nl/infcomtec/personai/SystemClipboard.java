package nl.infcomtec.personai;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * Very limited system clipboard support.
 *
 * @author walter
 */
public class SystemClipboard {

    private final Clipboard clipboard;

    public SystemClipboard() {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    /**
     * Get text from the clipboard or a message why that failed.
     *
     * @return Some text.
     */
    public String getText() {
        Transferable contents = clipboard.getContents(null);
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return contents.getTransferData(DataFlavor.stringFlavor).toString();
            } catch (Exception ex) {
                return "Failed to get text from clipboard: " + ex.getMessage();
            }
        }
        return "";
    }
    
    /**
     * Put some text on the clipboard.
     * @param text Text to place.
     */
    public void putText(String text) {
        StringSelection contents=new StringSelection(text);
        clipboard.setContents(contents, contents);
    }

}
