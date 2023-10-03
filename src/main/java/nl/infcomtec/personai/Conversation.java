package nl.infcomtec.personai;

import java.util.LinkedList;
import java.util.TreeSet;
import nl.infcomtec.graphs.ClEdge;
import nl.infcomtec.graphs.ClGraph;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.simpleimage.ImageViewer;
import nl.infcomtec.simpleimage.Marker;

/**
 *
 * @author Walter Stroebel
 */
public class Conversation extends ClGraph {

    private static final String EOLN = System.lineSeparator();
    public final TreeSet<String> selectedNodes = new TreeSet<>();
    public ClNode insNode;

    public boolean hasSelection() {
        return !selectedNodes.isEmpty();
    }

    public boolean selectNode(ClNode node, ImageViewer dotViewer) {
        insNode = node;
        boolean ret = selectedNodes.add(node.getName());
        reMark(dotViewer);
        return ret;
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

    public boolean unselectNode(ClNode node, ImageViewer dotViewer) {
        boolean ret = selectedNodes.remove(node.getName());
        if (null != insNode && insNode.equals(node)) {
            if (hasSelection()) {
                insNode = getNode(selectedNodes.last());
            } else {
                insNode = null;
            }
        }
        reMark(dotViewer);
        return ret;
    }

    public String getSelNodeText() {
        if (null != insNode) {
            return insNode.getUserStr();
        }
        return "";
    }

    public StringBuilder getSelectedText(String userInput) {
        StringBuilder sb = new StringBuilder(userInput.trim());
        for (String k : selectedNodes) {
            sb.append(EOLN);
            sb.append(getNode(k).getUserStr());
        }
        return sb;
    }

    public void clearSelection() {
        selectedNodes.clear();
    }

    public ClNode newNode(String label, String shape) {
        ClNode ret = addNode(new ClNode(this, label).withShape(shape));
        if (null != insNode) {
            addNode(new ClEdge(insNode, ret, "question"));
        }
        return ret;
    }
    
    public void addAnswer(ClNode q, String tagLine, String answer) {
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
        ClNode a = addNode(new ClNode(this, tagLine).withShape("box"));
        a.setUserObj(sections.removeFirst().toString());
        addNode(new ClEdge(q, a, tagLine));
        insNode=a;
        selectedNodes.add(a.getName());
        while (!sections.isEmpty()) {
            section = sections.removeFirst();
            int nl = section.indexOf("\n");
            ClNode sub = new ClNode(this, (nl >= 0) ? section.substring(0, nl) : "# Header?");
            sub.setUserObj(section.toString());
            addNode(new ClEdge(a, sub, "topic"));
        }
    }
}
