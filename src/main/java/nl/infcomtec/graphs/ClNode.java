package nl.infcomtec.graphs;

import java.awt.Color;

/**
 * Clickable node base class.
 *
 * @author walter
 */
public class ClNode {

    public String label;
    public String attributes;
    public final int id;
    protected final ClGraph graph;
    protected final Color foreColor;
    protected final Color backColor;
    private Object userObj;

    /**
     * Constructor to initialize a ClNode.
     *
     * @param graph Owning graph.
     * @param label Label for the node.
     * @param fgColor Original foreground color.
     * @param bgColor Original background color.
     * @param extraAttributes Map containing any extra GraphViz attributes.
     */
    public ClNode(ClGraph graph, String label, Color fgColor, Color bgColor, String extraAttributes) {
        this.label = label;
        this.foreColor = fgColor;
        this.backColor = bgColor;
        this.attributes = extraAttributes;
        this.graph = graph;
        id = graph.genId();
        graph.addNode(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.id;
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
        return this.id == other.id;
    }

    /**
     * Constructor to initialize a ClNode.
     *
     * @param graph Owning graph.
     * @param label Label for the node.
     * @param extraAttributes Map containing any extra Graphviz attributes.
     */
    public ClNode(ClGraph graph, String label, String extraAttributes) {
        this(graph, label, graph.defaultNodeForegroundColor.get(), graph.defaultNodeBackgroundColor.get(), extraAttributes);
    }

    public int getId() {
        return id;
    }

    /**
     * Generate the DOT representation for the node.
     *
     * @return DOT language representation.
     */
    public String generateDotRepresentation() {
        StringBuilder sb = new StringBuilder();
        int nodeId = getId();

        sb.append("N").append(nodeId)
                .append(" [style=filled, label=\"").append(label).append("\", ")
                .append("color=\"#").append(Integer.toHexString(foreColor.getRGB()).substring(2)).append("\", ")
                .append("fillcolor=\"#").append(Integer.toHexString(backColor.getRGB()).substring(2)).append("\", ")
                .append("fontcolor=\"#").append(Integer.toHexString(foreColor.getRGB()).substring(2)).append("\"");

        if (attributes != null && !attributes.isEmpty()) {
            sb.append(", ").append(attributes);
        }
        sb.append("];");
        System.out.println(sb);
        return sb.toString();
    }

    /**
     * @return the userObj
     */
    public Object getUserObj() {
        return userObj;
    }

    public String getUserStr() {
        if (null != userObj) {
            return userObj.toString();
        }
        return "";
    }

    /**
     * @param userObj the userObj to set
     */
    public void setUserObj(Object userObj) {
        this.userObj = userObj;
    }
}
