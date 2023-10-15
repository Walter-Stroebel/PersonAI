package nl.infcomtec.graphs;

import java.awt.Color;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Clickable node base class.
 *
 * @author walter
 */
public class ClNode {

    public String label;
    public final int uid;
    protected final ClGraph graph;
    protected final Color foreColor;
    protected final Color backColor;
    protected String shape = "ellipse";
    private String userStr;

    /**
     * Constructor to initialize a ClNode.
     *
     * @param graph Owning graph.
     * @param label Label for the node.
     * @param fgColor Original foreground color.
     * @param bgColor Original background color.
     */
    public ClNode(ClGraph graph, String label, Color fgColor, Color bgColor) {
        this.label = label.replace('"', '\'');
        this.foreColor = fgColor;
        this.backColor = bgColor;
        this.graph = graph;
        uid = graph.genId();
        graph.addNode(this);
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public ClNode withShape(String shape) {
        this.shape = shape;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.uid;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClNode other = (ClNode) obj;
        return this.uid == other.uid;
    }

    /**
     * Constructor to initialize a ClNode.
     *
     * @param graph Owning graph.
     * @param label Label for the node.
     */
    public ClNode(ClGraph graph, String label) {
        this(graph, label, graph.defaultNodeForegroundColor.get(), graph.defaultNodeBackgroundColor.get());
    }

    public int getId() {
        return uid;
    }

    public String getMark() {
        return ':' + getUid() + ';';
    }

    public String getUid() {
        return Integer.toString(uid, 36);
    }

    public String getName() {
        return "N" + Integer.toString(uid, 36);
    }

    /**
     * Generate the DOT representation for the node.
     *
     * @return DOT language representation.
     */
    public String generateDotRepresentation() {
        StringBuilder sb = new StringBuilder();
        int nodeId = getId();

        sb.append(getName())
                .append(" [style=filled, label=\"").append(label).append("\", ")
                .append("color=").append(ClGraph.dotColor(foreColor))
                .append(", fillcolor=").append(ClGraph.dotColor(backColor))
                .append(", fontcolor=").append(ClGraph.dotColor(foreColor));

        if (shape != null && !shape.isEmpty()) {
            sb.append(", shape=").append(shape);
        }
        sb.append("];");
        return sb.toString();
    }

    public String getUserStr() {
        if (null != userStr) {
            return userStr.toString();
        }
        return "";
    }

    /**
     * @param userObj the userObj to set
     */
    public void setUserStr(String userObj) {
        this.userStr = userObj;
    }

    /**
     * Add a string to or set the userStr to the string.
     *
     * @param str String to add or set.
     */
    public void appendUserStr(String str) {
        StringBuilder cur = new StringBuilder(getUserStr());
        cur.append(System.lineSeparator());
        cur.append(str);
        userStr = cur.toString();
    }

    /**
     * Build from JSON input.
     * @param g Graph to add the new node to.
     * @param nj Node in JSON form.
     */
    public ClNode(ClGraph g, NodeJSON nj) {
        this.backColor = new Color(nj.bCol);
        this.foreColor = new Color(nj.fCol);
        this.uid = nj.id;
        this.graph = g;
        this.label = nj.label;
        this.shape = nj.shape;
        this.userStr = nj.userObj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMark());
        sb.append(label).append('#');
        sb.append(shape).append('#').append(ClGraph.EOLN);
        sb.append(userStr);
        return sb.toString();
    }

    /**
     * For use in a JTree,
     *
     * @return TreeNode branch.
     */
    public DefaultMutableTreeNode toTreeNode() {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode("NODE");
        {
            DefaultMutableTreeNode val = new DefaultMutableTreeNode("label=" + label);
            ret.add(val);
        }
        {
            DefaultMutableTreeNode val = new DefaultMutableTreeNode("name=" + getName());
            ret.add(val);
        }
        {
            DefaultMutableTreeNode val = new DefaultMutableTreeNode("shape=" + shape);
            ret.add(val);
        }
        {
            DefaultMutableTreeNode val = new DefaultMutableTreeNode("text=" + userStr.toString());
            ret.add(val);
        }
        return ret;
    }

}
