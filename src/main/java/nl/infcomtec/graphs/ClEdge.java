package nl.infcomtec.graphs;

import java.awt.Color;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author walter
 */
public class ClEdge extends ClNode {

    public final ClNode fromNode;
    public final ClNode toNode;

    public ClEdge(ClNode from, ClNode to, String label, Color fgColor, Color bgColor) {
        super((ClGraph) from.graph, label, fgColor, bgColor);
        this.fromNode = from;
        this.toNode = to;
        shape = "edge";
    }

    public ClEdge(ClNode from, ClNode to, String label) {
        super((ClGraph) from.graph, label, from.graph.defaultEdgeForegroundColor.get(), from.graph.defaultEdgeBackgroundColor.get());
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
        sb.append(getMark());
        sb.append(label).append('#');
        sb.append(fromNode.getUid()).append(" -> ").append(toNode.getUid()).append(ClGraph.EOLN);
        return sb.toString();
    }

    @Override
    public DefaultMutableTreeNode toTreeNode() {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode("EDGE");
        {
            DefaultMutableTreeNode val = new DefaultMutableTreeNode("label=" + label);
            ret.add(val);
        }
        {
            DefaultMutableTreeNode val = new DefaultMutableTreeNode("name=" + getName());
            ret.add(val);
        }
        return ret;
    }

    @Override
    public String generateDotRepresentation() {
        StringBuilder sb = new StringBuilder();

        sb.append(fromNode.getName()).append(" -> ").append(toNode.getName())
                .append(" [label=\"").append(label).append("\", ")
                .append("color=").append(ClGraph.dotColor(foreColor))
                .append(", fillcolor=").append(ClGraph.dotColor(backColor))
                .append(", fontcolor=").append(ClGraph.dotColor(foreColor));

        sb.append("];");
        return sb.toString();
    }

}
