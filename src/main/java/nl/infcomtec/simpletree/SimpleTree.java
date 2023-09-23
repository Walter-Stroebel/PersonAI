package nl.infcomtec.simpletree;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Basic tree stuff.
 *
 * @author Walter Stroebel
 */
public class SimpleTree {

    /* Demo usage
    public static void main(String[] args) {
        SimpleTree st = new SimpleTree("Root");
        st.addNode(null, "Child1");
        st.addNode(null, "Child2");
        // Create a JFrame to display the tree
        final JFrame frame = new JFrame("My JTree Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JScrollPane(st.createTree()), BorderLayout.WEST); // Add the tree to a scroll pane
        final JPanel disp = new JPanel();
        disp.setPreferredSize(new Dimension(320, 200));
        frame.getContentPane().add(disp, BorderLayout.CENTER);
        st.setHandler(new TreeNodeSelectionHandler() {
            @Override
            public void nodeSelected(DefaultTreeModel treeModel, DefaultMutableTreeNode node) {
                disp.removeAll();
                disp.add(new JLabel(node.getUserObject().toString()));
                disp.revalidate();
                disp.repaint();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
     */
    private final DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private JTree tree;
    private TreeNodeSelectionHandler handler;

    /**
     * Construct the tree by defining the root.
     *
     * @param root Must implement toString().
     */
    public SimpleTree(Object root) {
        this.rootNode = new DefaultMutableTreeNode(root);
    }

    /**
     * Add a node.
     *
     * @param parent null for the root, else a member of this tree.
     * @param node Must implement toString().
     * @return
     */
    public DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, Object node) {
        DefaultMutableTreeNode par = (null == parent) ? rootNode : parent;
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(node);
        par.add(child);
        return child;
    }

    /**
     * Create the model and the tree.
     *
     * @return A JTree.
     */
    public JTree createTree() {
        // Create a tree model using the root node
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        // Add a TreeSelectionListener to the JTree
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (null != getHandler()) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        // Call the handler's method with the tree model and selected node
                        getHandler().nodeSelected(treeModel, selectedNode);
                    }
                }
            }
        });
        return tree;
    }

    /**
     * @return the handler
     */
    public TreeNodeSelectionHandler getHandler() {
        return handler;
    }

    /**
     * @param handler the handler to set
     */
    public void setHandler(TreeNodeSelectionHandler handler) {
        this.handler = handler;
    }

    /**
     * Will be called whenever the tree reports a selection event.
     *
     */
    public interface TreeNodeSelectionHandler {

        /**
         * Will be called whenever the tree reports a selection event.
         *
         * @param treeModel The current model.
         * @param node The selected node.
         */
        void nodeSelected(DefaultTreeModel treeModel, DefaultMutableTreeNode node);
    }
}
