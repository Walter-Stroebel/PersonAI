package nl.infcomtec.personai;

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
            }else{
                insNode=null;
            }
        }
        reMark(dotViewer);
        return ret;
    }
    
    public String getSelNodeText(){
        if(null!=insNode){
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
}
