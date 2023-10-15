/*
 */
package nl.infcomtec.graphs;

/**
 * JSON version of a ClNode.
 *
 * @author Walter Stroebel
 */
public class NodeJSON {

    int fCol;
    int bCol;
    String label;
    String userObj;
    Integer from;
    Integer to;
    String shape;
    int id;

    /**
     * Create directly.
     *
     * @param uid Required.
     * @param fCol Foreground color.
     * @param bCol Background color.
     */
    public NodeJSON(int uid, int fCol, int bCol) {
        this.id = uid;
        this.fCol = fCol;
        this.bCol = bCol;
    }

    /**
     * Create from an existing node.
     *
     * @param org Existing node.
     */
    public NodeJSON(ClNode org) {
        this.id = org.uid;
        this.fCol = org.foreColor.getRGB();
        this.bCol = org.backColor.getRGB();
        this.from = (org instanceof ClEdge) ? ((ClEdge) org).fromNode.uid : null;
        this.to = (org instanceof ClEdge) ? ((ClEdge) org).toNode.uid : null;
        this.label = org.label;
        this.shape = org.shape;
        this.userObj = org.getUserStr();
    }

}
