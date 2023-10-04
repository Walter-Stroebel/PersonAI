package nl.infcomtec.personai;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import nl.infcomtec.graphs.ClEdge;
import nl.infcomtec.graphs.ClGraph;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.simpleimage.ImageViewer;
import nl.infcomtec.simpleimage.Marker;

/**
 * Application extended version of ClGraph.
 *
 * @author Walter Stroebel
 */
public class Conversation extends ClGraph {

    private static final String EOLN = System.lineSeparator();
    public final TreeSet<String> selectedNodes = new TreeSet<>();
    public ConvoNode insNode;

    public boolean hasSelection() {
        return !selectedNodes.isEmpty();
    }

    public boolean selectNode(ConvoNode node, ImageViewer dotViewer) {
        insNode = node;
        boolean ret = selectedNodes.add(node.getName());
        reMark(dotViewer);
        return ret;
    }

    @Override
    public ConvoNode getFirstNode() {
        ClNode ret = super.getFirstNode();
        if (!(ret instanceof ConvoNode)) {
            throw new RuntimeException("Override error");
        }
        return (ConvoNode) ret;
    }

    private void reMark(ImageViewer dotViewer) {
        dotViewer.clearMarkers();
        for (String nn : selectedNodes) {
            Marker m = insNode.label.equals(nn)
                    ? new Marker(segments.get(nn), 0xFFE0E0E0, 0x007F00)
                    : new Marker(segments.get(nn), 0xFFE0E0E0, 0x7F0000);
            dotViewer.addMarker(m);
        }
    }

    public boolean unselectNode(ConvoNode node, ImageViewer dotViewer) {
        boolean ret = selectedNodes.remove(node.getName());
        if (null != insNode && insNode.equals(node)) {
            if (hasSelection()) {
                insNode = (ConvoNode) getNode(selectedNodes.last());
            } else {
                insNode = null;
            }
        }
        reMark(dotViewer);
        return ret;
    }

    public String getSelNodeText() {
        if (null != insNode) {
            return insNode.getText();
        }
        return "";
    }

    public StringBuilder getSelectedText(String userInput) {
        StringBuilder sb = new StringBuilder(userInput.trim());
        for (String k : selectedNodes) {
            sb.append(EOLN);
            sb.append(((ConvoNode) getNode(k)).getText());
        }
        return sb;
    }

    public void clearSelection() {
        selectedNodes.clear();
    }

    public ConvoNode newNode(String label, String shape, String text) {
        ConvoNode ret = addNode(new ConvoNode(this, label, shape, text));
        if (null != insNode) {
            addNode(new ClEdge(insNode, ret, "question"));
        }
        return ret;
    }

    @Override
    public ConvoNode addNode(ClNode node) {
        if (!(node instanceof ConvoNode)) {
            throw new RuntimeException("Override error");
        }
        return (ConvoNode) super.addNode(node);
    }

    public synchronized List<ConvoNode> getConvoNodes() {
        List<ConvoNode> ret = new LinkedList<>();
        for (ClNode n : getNodes()) {
            if (n instanceof ConvoNode) {
                ret.add((ConvoNode) n);
            } else {
                throw new RuntimeException("Override error");
            }
        }
        return ret;
    }

    public void addAnswer(ConvoNode q, String tagLine, String answer) {
        String[] lines = answer.split("\n");
        LinkedList<StringBuilder> sections = new LinkedList<>();
        StringBuilder section = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("# ")) {
                sections.add(section);
                section = new StringBuilder(line);
                section.append(EOLN);
            } else {
                section.append(line);
                section.append(EOLN);
            }
        }
        if (section.length() > 0) {
            sections.add(section);
        }
        ConvoNode a = addNode(new ConvoNode(this, tagLine, "box", sections.removeFirst().toString()));
        addNode(new ClEdge(q, a, tagLine));
        insNode = a;
        selectedNodes.add(a.getName());
        while (!sections.isEmpty()) {
            section = sections.removeFirst();
            int nl = section.indexOf("\n");
            ConvoNode sub = new ConvoNode(this, (nl >= 0) ? section.substring(0, nl) : "# Header?", "box", section.toString());
            addNode(new ClEdge(a, sub, "topic"));
        }
    }
}
