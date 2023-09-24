package nl.infcomtec.graphs;

import java.awt.Color;

/**
 * Clickable node base class.
 *
 * @author walter
 */
public class ClNode {

    public String label;
    public final int id;
    protected final ClGraph graph;
    protected final Color foreColor;
    protected final Color backColor;
    protected String shape="ellipse";
    private Object userObj;

    /**
     * Constructor to initialize a ClNode.
     *
     * @param graph Owning graph.
     * @param label Label for the node.
     * @param fgColor Original foreground color.
     * @param bgColor Original background color.
     */
    public ClNode(ClGraph graph, String label, Color fgColor, Color bgColor) {
        this.label = label;
        this.foreColor = fgColor;
        this.backColor = bgColor;
        this.graph = graph;
        id = graph.genId();
        graph.addNode(this);
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape=shape;
    }

    public ClNode withShape(String shape) {
        this.shape=shape;
        return this;
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
     */
    public ClNode(ClGraph graph, String label) {
        this(graph, label, graph.defaultNodeForegroundColor.get(), graph.defaultNodeBackgroundColor.get());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return "N" + id;
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
                .append("color=").append(ClGraph.dotColor(foreColor))
                .append(", fillcolor=").append(ClGraph.dotColor(backColor))
                .append(", fontcolor=").append(ClGraph.dotColor(foreColor));

        if (shape != null && !shape.isEmpty()) {
            sb.append(", shape=").append(shape);
        }
        sb.append("];");
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
