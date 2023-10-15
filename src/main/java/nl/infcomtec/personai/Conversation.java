package nl.infcomtec.personai;

import com.google.gson.Gson;
import java.io.File;
import java.util.LinkedList;
import java.util.TreeSet;
import nl.infcomtec.graphs.ClEdge;
import nl.infcomtec.graphs.ClGraph;
import nl.infcomtec.graphs.ClNode;
import nl.infcomtec.simpleimage.ImageViewer;
import nl.infcomtec.simpleimage.Marker;
import nl.infcomtec.tools.PandocConverter;

/**
 * Application extended version of ClGraph.
 *
 * @author Walter Stroebel
 */
public class Conversation extends ClGraph {

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
                insNode = (ClNode) getNode(selectedNodes.last());
            } else {
                insNode = null;
            }
        }
        reMark(dotViewer);
        return ret;
    }

    public String getSelNodeText() {
        if (null != insNode) {
            return getText(insNode);
        }
        return "";
    }

    public static String getText(ClNode node) {
        return node.getUserStr();
    }

    public static String getHTML(ClNode node) {
        return new PandocConverter().convertMarkdownToHTML(node.getUserStr());
    }

    public static void setHTML(ClNode node, String text) {
        node.setUserStr(new PandocConverter().convertHTMLToMarkdown(text));
    }

    public static void setText(ClNode node, String text) {
        node.setUserStr(text);
    }

    public StringBuilder getSelectedText(String userInput) {
        StringBuilder sb = new StringBuilder(userInput.trim());
        for (String k : selectedNodes) {
            sb.append(EOLN);
            sb.append(getText(getNode(k)));
        }
        return sb;
    }

    public void clearSelection() {
        selectedNodes.clear();
    }

    public ClNode newNode(String label, String shape, String text) {
        ClNode ret = addNode(new ClNode(this, label).withShape(shape));
        setText(ret, text);
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
        setText(a, sections.removeFirst().toString());
        addNode(new ClEdge(q, a, tagLine));
        insNode = a;
        selectedNodes.add(a.getName());
        while (!sections.isEmpty()) {
            section = sections.removeFirst();
            int nl = section.indexOf("\n");
            ClNode sub = new ClNode(this, (nl >= 0) ? section.substring(0, nl) : "# Header?").withShape("box");
            setText(sub, section.toString());
            addNode(new ClEdge(a, sub, "topic"));
        }
    }

    public void loadConvo(File selectedFile, Gson gson) {
        load(selectedFile, gson);
    }

}
