package nl.infcomtec.graphs;

import java.awt.Color;

/**
 *
 * @author walter
 */
public class ClEdge extends ClNode {

    public final ClNode fromNode;
    public final ClNode toNode;

    public ClEdge(ClNode from, ClNode to, String label, Color fgColor, Color bgColor, String extraAttributes) {
        super(from.graph, label, fgColor, bgColor, extraAttributes);
        this.fromNode = from;
        this.toNode = to;
    }

    public ClEdge(ClNode from, ClNode to, String label, String extraAttributes) {
        super(from.graph, label, from.graph.defaultEdgeForegroundColor.get(), from.graph.defaultEdgeBackgroundColor.get(), extraAttributes);
        this.fromNode = from;
        this.toNode = to;
    }

    @Override
    public String generateDotRepresentation() {
        String n1= "N"+fromNode.getId();
        String n2= "N"+toNode.getId();
        StringBuilder sb = new StringBuilder();
 
        sb.append(n1).append(" -> ").append(n2)
                .append(" [label=\"").append(label).append("\", ")
                .append("color=").append(ClGraph.dotColor(foreColor))
                .append(", fillcolor=").append(ClGraph.dotColor(backColor))
                .append(", fontcolor=").append(ClGraph.dotColor(foreColor));

        if (attributes != null && !attributes.isEmpty()) {
            sb.append(", ").append(attributes);
        }
        sb.append("];");
        return sb.toString();
    }

}
