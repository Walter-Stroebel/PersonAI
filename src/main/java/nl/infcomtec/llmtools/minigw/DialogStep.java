/*
 */
package nl.infcomtec.llmtools.minigw;

import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;

public class DialogStep {
    public String subject;
    public String text;
    public DialogStep parent;
    public List<DialogStep> children=new LinkedList<>();
    public DialogStep link;

    public Box display(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }
}
