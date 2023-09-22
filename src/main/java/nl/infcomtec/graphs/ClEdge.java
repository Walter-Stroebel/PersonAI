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

}
