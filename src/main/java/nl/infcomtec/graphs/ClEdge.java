package nl.infcomtec.graphs;

import java.awt.Color;

/**
 *
 * @author walter
 */
public class ClEdge extends ClNode {

    public final ClNode fromNode;
    public final ClNode toNode;

    public ClEdge(ClNode from, ClNode to, String label, Color fgColor, Color bgColor) {
        super(from.graph, label, fgColor, bgColor);
        this.fromNode = from;
        this.toNode = to;
        shape = "edge";
    }

    public ClEdge(ClNode from, ClNode to, String label) {
        super(from.graph, label, from.graph.defaultEdgeForegroundColor.get(), from.graph.defaultEdgeBackgroundColor.get());
        this.fromNode = from;
        this.toNode = to;
        shape = "edge";
    }

    public ClEdge(ClGraph g, NodeJSON nj) {
        super(g, nj);
        this.fromNode = g.getNode(nj.from);
        this.toNode = g.getNode(nj.to);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClEdge{");
        sb.append("\n\tfromNode=").append(fromNode);
        sb.append("\n\ttoNode=").append(toNode);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String generateDotRepresentation() {
        String n1 = "N" + fromNode.getId();
        String n2 = "N" + toNode.getId();
        StringBuilder sb = new StringBuilder();

        sb.append(n1).append(" -> ").append(n2)
                .append(" [label=\"").append(label).append("\", ")
                .append("color=").append(ClGraph.dotColor(foreColor))
                .append(", fillcolor=").append(ClGraph.dotColor(backColor))
                .append(", fontcolor=").append(ClGraph.dotColor(foreColor));

        sb.append("];");
        return sb.toString();
    }

}
