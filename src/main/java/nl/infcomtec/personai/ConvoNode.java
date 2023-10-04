package nl.infcomtec.personai;

import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.tools.PandocConverter;

/**
 * Extends ClNode with a text user object with HTML and MarkDown support.
 *
 * @author Walter Stroebel
 */
public class ConvoNode extends ClNode {

    public ConvoNode(Conversation convo, String label, String shape, String text) {
        super(convo, label);
        setShape(shape);
        setUserObj(text);
    }

    public String getText() {
        return getUserStr();
    }

    public String getHTML() {
        return new PandocConverter().convertMarkdownToHTML(getUserStr());
    }

    void setHTML(String text) {
        setUserObj(new PandocConverter().convertHTMLToMarkdown(text));
    }

    void setText(String text) {
        setUserObj(text);
    }

}
