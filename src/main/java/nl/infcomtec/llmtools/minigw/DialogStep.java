/*
 */
package nl.infcomtec.llmtools.minigw;

import java.util.LinkedList;
import java.util.List;

public class DialogStep {

    public String subject;
    public String text;
    public DialogStep parent;
    public List<DialogStep> children = new LinkedList<>();
    public DialogStep link;

    public DialogStep(String subject, String text) {
        this.subject = subject;
        this.text = text;
    }
 }
