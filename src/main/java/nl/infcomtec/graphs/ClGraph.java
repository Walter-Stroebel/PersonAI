package nl.infcomtec.graphs;

import com.google.gson.Gson;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.infcomtec.simpleimage.BitShape;
import nl.infcomtec.tools.ToolManager;

/**
 *
 * @author walter
 */
public class ClGraph {

    public static final String EOLN = System.lineSeparator();

    public static String dotColor(Color c) {
        return "\"#" + Integer.toHexString(c.getRGB()).substring(2) + "\"";
    }
    public static final Random random = new Random();
    public static final int loMark = Integer.parseInt("1000", 36);
    public static final int hiMark = Integer.parseInt("zzzz", 36);
    public static TreeSet<Integer> nbad = new TreeSet(Arrays.asList(new Integer[]{
        71279, 116021, 116027, 121637, 122344, 126372, 217540, 222638, 255989, 255995, 261605, 262312, 266340, 311924, 340537,
        350992, 368453, 368741, 433602, 444268, 451892, 451895, 459852, 470174, 497450, 497453, 497476, 497483, 497512, 501638,
        501746, 502563, 502574, 502826, 560324, 560759, 561845, 569621, 587153, 587743, 587765, 590999, 591428, 591863, 591899,
        591935, 592724, 593159, 594020, 594455, 599587, 599609, 600905, 608276, 608558, 612312, 612527, 616334, 619728, 619836,
        620064, 620088, 620089, 620090, 620303, 630308, 630579, 630590, 631886, 651315, 651326, 739172, 740468, 747380, 777620,
        778484, 778520, 778556, 779780, 781076, 793968, 797555, 797817, 811353, 811415, 811569, 811806, 811808, 811811, 812053,
        812059, 812073, 825048, 825072, 825073, 825074, 826368, 827664, 889055, 909899, 910835, 911054, 911080, 911087, 935150,
        942926, 957171, 957182, 958478, 972835, 972857, 974153, 977907, 977918, 1028180, 1050212, 1065867, 1065883, 1066443, 1067163,
        1079550, 1086138, 1087074, 1087309, 1087326, 1133442, 1144108, 1151735, 1159692, 1167287, 1168732, 1172322, 1180081, 1180098, 1189828,
        1189936, 1190738, 1190741, 1190764, 1190771, 1191016, 1197527, 1198391, 1198427, 1198463, 1199687, 1200983, 1205556, 1206324, 1206348,
        1206349, 1206350, 1207644, 1308773, 1316549, 1318692, 1328465, 1328471, 1329050, 1329055, 1329076, 1329077, 1329083, 1334671, 1334693,
        1334729, 1335004, 1335390, 1335393, 1335400, 1338132, 1338816, 1339428, 1355392, 1359193, 1366069, 1366969, 1376632, 1377398, 1377401,
        1377424, 1377431, 1394561, 1394669, 1394863, 1394885, 1395151, 1395173, 1485668, 1499024, 1506800, 1610927, 1655669, 1655675, 1661285,
        1661992, 1666020}));

    public static String getUniqueMark(String text) {
        while (true) {
            int mark = random.nextInt(hiMark - loMark + 1) + loMark;
            if (nbad.contains(mark)) {
                continue;
            }
            String ret = Integer.toString(mark, 36) + ':';
            if (!text.contains(ret)) {
                return ':' + ret + ';';
            }
        }
    }

    public static int getUniqueId(String text) {
        while (true) {
            int mark = random.nextInt(hiMark - loMark + 1) + loMark;
            if (nbad.contains(mark)) {
                continue;
            }
            String ret = Integer.toString(mark, 36) + ':';
            if (!text.contains(ret)) {
                return mark;
            }
        }
    }

    private void update(UText ut) {
        ClNode get = nodeMap.get(ut.uid);
        if (null==get){
            JOptionPane.showMessageDialog(null, "You messed up a marker "+ut.uid);
            return;
        }
        if(get instanceof ClEdge) {
            // TODO Edge editing not supported
            return;
        }
        int eoLab=ut.text.indexOf("#");
        int eoShp=ut.text.indexOf("#",eoLab+1);
        get.setUserObj(ut.text.substring(eoShp+1).trim());
        get.shape=ut.text.substring(eoLab+1, eoShp);
        get.label=ut.text.substring(0, eoLab);
    }

    public void parse(String text) {
        List<UText> texts = getTexts(text);
        for (UText ut:texts){
            update(ut);
        }
    }

    public static class UText implements Comparable<UText> {

        public final int uid;
        public final StringBuilder text;

        public UText(int uid, String text) {
            this.uid = uid;
            this.text = new StringBuilder(text);
        }

        public UText(int uid, StringBuilder text) {
            this(uid, text.toString());
        }

        @Override
        public int compareTo(UText t) {
            return Integer.compare(uid, t.uid);
        }

        @Override
        public String toString() {
            return "UText{" + "uid=" + Integer.toString(uid, 36) + ", text=" + text + '}';
        }
    }

    public static String markText(StringBuilder text, int pos) {
        String ret = getUniqueMark(text.toString());
        text.insert(pos, ret);
        return ret;
    }

    public static List<String[]> splitString(String input) {
        Pattern pattern = Pattern.compile(":([^:;]*);");
        Matcher matcher = pattern.matcher(input);

        List<String[]> results = new ArrayList<>();

        int lastEnd = 0;
        while (matcher.find()) {
            String candidate = matcher.group(1).replaceAll("[^a-zA-Z0-9]", "");
            if (candidate.length() == 4 && !nbad.contains(Integer.parseInt(candidate, 36))) {
                String[] segments = new String[3];
                segments[0] = input.substring(lastEnd, matcher.start()); // Optional Text 1
                segments[1] = candidate; // Mark
                lastEnd = matcher.end();
                if (matcher.find()) {
                    segments[2] = input.substring(lastEnd, matcher.start()); // Optional Text 2
                    lastEnd = matcher.start();
                    matcher.region(lastEnd, input.length());
                } else {
                    segments[2] = input.substring(lastEnd); // Remaining Optional Text
                }
                results.add(segments);
            }
        }

        return results;
    }

    public static List<UText> getTexts(String text) {
        List<UText> ret = new ArrayList<>();
        List<String[]> splitString = splitString(text);
        for (String[] m : splitString) {
            if (m.length == 3 && !m[2].isEmpty()) {
                try {
                    int id = Integer.parseInt(m[1], 36);
                    UText elm = new UText(id, m[2]);
                    ret.add(elm);
                } catch (NumberFormatException e) {
                    // Not a valid mark? Should not happen.
                    throw new RuntimeException(text + "\nClaims " + m[1] + " as a mark?");
                }
            }
        }
        return ret;
    }
    public final AtomicReference<Color> defaultNodeForegroundColor = new AtomicReference<>(Color.BLACK);
    public final AtomicReference<Color> defaultNodeBackgroundColor = new AtomicReference<>(Color.WHITE);
    public final AtomicReference<Color> defaultEdgeForegroundColor = new AtomicReference<>(Color.BLACK);
    public final AtomicReference<Color> defaultEdgeBackgroundColor = new AtomicReference<>(Color.WHITE);
    private final TreeMap<Integer, ClNode> nodeMap = new TreeMap<>();
    public HashMap<String, Point2D> nodeCenters;
    public HashMap<String, BitShape> segments;
    /**
     * Default is top-down, set to true for left-to-right.
     */
    public boolean lr = false;

    public synchronized void clear() {
        nodeMap.clear();
        nodeCenters = null;
        segments = null;
    }

    public StringBuilder flat() {
        StringBuilder sb = new StringBuilder();
        for (ClNode node : nodeMap.values()) {
            sb.append(EOLN);
            sb.append(node.toString());
        }
        sb.append(EOLN);
        return sb;
    }

    public void addEdge(ClNode n1, ClNode n2, String label) {
        ClEdge e = new ClEdge(n1, n2, label);
        nodeMap.put(e.getId(), e);
    }

    public boolean isEmpty() {
        return nodeMap.isEmpty();
    }

    protected ClNode addNode(ClNode node) {
        nodeMap.put(node.getId(), node);
        return node;
    }

    public int genId() {
        return getUniqueId(flat().toString());
    }

    public ClNode getNode(int id) {
        return nodeMap.get(id);
    }

    public ClNode getNode(String name) {
        return nodeMap.get(Integer.valueOf(name.substring(1)));
    }

    public void draw(OutputStream out) {
        ToolManager tm = new ToolManager() {
            @Override
            public void run() {
                internalRun();
            }
        };

        tm.setInput(genDot());
        tm.setOutput(out);
        tm.setCommand("dot", "-Tpng");
        tm.run();
    }

    public synchronized List<ClNode> getNodes() {
        LinkedList<ClNode> ret = new LinkedList<>();
        for (ClNode n : nodeMap.values()) {
            if (isNode(n)) {
                ret.add(n);
            }
        }
        return ret;
    }

    public synchronized List<ClEdge> getEdges() {
        LinkedList<ClEdge> ret = new LinkedList<>();
        for (ClNode n : nodeMap.values()) {
            if (n instanceof ClEdge) {
                ret.add((ClEdge) n);
            }
        }
        return ret;
    }

    public void push(Gson gson) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<NodeJSON> l = new LinkedList<>();
        for (ClNode n : getNodes()) {
            NodeJSON nj = new NodeJSON(n.uid, n.foreColor.getRGB(), n.backColor.getRGB());
            nj.label = n.label;
            if (isNode(n)) {
                nj.userObj = n.getUserStr();
                nj.shape = n.shape;
                l.add(nj);
            }
        }
        for (ClEdge e : getEdges()) {
            NodeJSON nj = new NodeJSON(e.uid, e.foreColor.getRGB(), e.backColor.getRGB());
            nj.label = e.label;
            nj.from = e.fromNode.getId();
            nj.to = e.toNode.getId();
            l.add(nj);
        }
        try ( OutputStreamWriter fw = new OutputStreamWriter(baos)) {
            gson.toJson(l, fw);
            fw.write(System.lineSeparator());
        } catch (IOException ex) {
            throw new RuntimeException("Error in push", ex);
        }
        stack.push(baos.toByteArray());
    }

    public JTree toTree() {
        List<ClNode> nodes = getNodes();
        List<ClEdge> edges = getEdges();
        // any node being pointed to is not a root
        for (ClEdge e : getEdges()) {
            nodes.remove(e.toNode);
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        HashMap<ClNode, DefaultMutableTreeNode> subs = new HashMap<>();
        for (ClNode node : nodes) {
            DefaultMutableTreeNode child = node.toTreeNode();
            root.add(child);
            for (Iterator<ClEdge> it = edges.iterator(); it.hasNext();) {
                ClEdge e = it.next();
                if (e.fromNode.equals(node)) {
                    DefaultMutableTreeNode c2 = e.toTreeNode();
                    child.add(c2);
                    DefaultMutableTreeNode c3 = e.toNode.toTreeNode();
                    c2.add(c3);
                    subs.put(e.toNode, c3);
                    it.remove();
                }
            }
        }
        boolean err = true;
        while (!edges.isEmpty()) {
            nodes = new LinkedList<>(subs.keySet());
            for (ClNode node : nodes) {
                DefaultMutableTreeNode child = subs.get(node);
                for (Iterator<ClEdge> it = edges.iterator(); it.hasNext();) {
                    ClEdge e = it.next();
                    if (e.fromNode.equals(node)) {
                        err = false;
                        DefaultMutableTreeNode c2 = e.toTreeNode();
                        child.add(c2);
                        DefaultMutableTreeNode c3 = e.toNode.toTreeNode();
                        c2.add(c3);
                        subs.put(e.toNode, c3);
                        it.remove();
                    }
                }
            }
            if (err) {
                System.err.println("Error: " + edges);
                break;
            }
            err = true;
        }
        return new JTree(root);
    }

    public void delete(ClNode node) {
        if (isNode(node)) {
            // also delete any edges linking from or to the node.
            for (ClEdge e : getEdges()) {
                if (e.fromNode.equals(node) || e.toNode.equals(node)) {
                    delete(e);
                }
            }
        }
        nodeMap.remove(node.uid);
    }

    private static boolean isNode(ClNode node) {
        return !(node instanceof ClEdge);
    }

    public void save(File f, Gson gson) {
        List<NodeJSON> l = new LinkedList<>();
        for (ClNode n : getNodes()) {
            NodeJSON nj = new NodeJSON(n.uid, n.foreColor.getRGB(), n.backColor.getRGB());
            nj.label = n.label;
            if (isNode(n)) {
                nj.userObj = n.getUserStr();
                nj.shape = n.shape;
                l.add(nj);
            }
        }
        for (ClEdge e : getEdges()) {
            NodeJSON nj = new NodeJSON(e.uid, e.foreColor.getRGB(), e.backColor.getRGB());
            nj.label = e.label;
            nj.from = e.fromNode.getId();
            nj.to = e.toNode.getId();
            l.add(nj);
        }
        if (f.exists() && !f.delete()) {
            throw new RuntimeException("Bad save file " + f);
        }
        try ( FileWriter fw = new FileWriter(f)) {
            gson.toJson(l, fw);
            fw.write(System.lineSeparator());
        } catch (IOException ex) {
            throw new RuntimeException("Error writing to " + f, ex);
        }
    }

    protected synchronized void load(File f, Gson gson) {
        clear();
        try ( FileReader fr = new FileReader(f)) {
            NodeJSON[] l = gson.fromJson(fr, NodeJSON[].class);
            for (NodeJSON nj : l) {
                // clean up
                nj.label = nj.label.replace("\"", "");
                nj.label = nj.label.replace("'", "");
                if (nj.label.length() > 20) {
                    nj.label = nj.label.substring(0, 20).trim();
                }
                if (nj.id < loMark) {
                    nj.id += loMark;
                }
                if (null == nj.from) {
                    ClNode n = new ClNode(this, nj);
                    nodeMap.put(n.uid, n);
                } else {
                    if (nj.from < loMark) {
                        nj.from += loMark;
                    }
                    if (nj.to < loMark) {
                        nj.to += loMark;
                    }
                    ClEdge e = new ClEdge(this, nj);
                    nodeMap.put(e.uid, e);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from " + f, ex);
        }
    }
    private final Stack<byte[]> stack = new Stack<>();

    public synchronized void pop(Gson gson) {
        if (stack.isEmpty()) {
            return;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(stack.pop());
        clear();
        try ( InputStreamReader fr = new InputStreamReader(bais)) {
            NodeJSON[] l = gson.fromJson(fr, NodeJSON[].class);
            for (NodeJSON nj : l) {
                if (nj.label.length() > 20) {
                    nj.label = nj.label.substring(0, 20).trim();
                }
                if (null == nj.from) {
                    ClNode n = new ClNode(this, nj);
                    nodeMap.put(n.uid, n);
                } else {
                    ClEdge e = new ClEdge(this, nj);
                    nodeMap.put(e.uid, e);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in pop", ex);
        }
    }

    private synchronized String genDot() {
        StringBuilder gr = new StringBuilder("digraph {").append(System.lineSeparator());
        if (lr) {
            gr.append("rankdir=LR;").append(System.lineSeparator());
        }
        for (ClNode n : getNodes()) {
            gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
        }
        for (ClEdge n : getEdges()) {
            gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
        }
        gr.append("}").append(System.lineSeparator());
        return gr.toString();
    }

    public BufferedImage render() throws Exception {
        BufferedImage image;
        try ( PipedOutputStream out = new PipedOutputStream();  PipedInputStream in = new PipedInputStream(out)) {
            // Create a ToolManager instance as Runnable
            ToolManager tm = new ToolManager() {
                @Override
                public void run() {
                    internalRun();
                }
            };

            tm.setInput(genDot());
            tm.setOutput(out);
            tm.setCommand("dot", "-Tpng");
            // Execute ToolManager in a separate thread
            Thread toolThread = new Thread(tm);
            toolThread.start();
            // Read image from PipedInputStream on the current thread
            image = ImageIO.read(in);
            // Wait for ToolManager thread to complete
            toolThread.join();
            if (tm.exitCode != 0) {
                System.err.println("Running " + tm.getCommand() + " failed with " + tm.exitCode);
            }
        }
        generatePlain(image.getWidth(), image.getHeight());
        return image;
    }

    @SuppressWarnings("ConvertToStringSwitch") // because that would generate a goto
    public void generatePlain(int W, int H) throws Exception {
        nodeCenters = new HashMap<>();
        double scaleX = 1.0;
        double scaleY = 1.0;
        try ( PipedOutputStream out = new PipedOutputStream();  PipedInputStream in = new PipedInputStream(out)) {
            ToolManager tm = new ToolManager() {
                @Override
                public void run() {
                    internalRun();
                }
            };
            tm.setInput(genDot());
            tm.setOutput(out);
            tm.setCommand("dot", "-Tplain");
            Thread toolThread = new Thread(tm);
            toolThread.start();

            // Read plain text format from PipedInputStream
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(" ");
                    if ("graph".equals(fields[0])) {
                        double scale = Double.parseDouble(fields[1]);
                        scaleX = W / Double.parseDouble(fields[2]) * scale;
                        scaleY = H / Double.parseDouble(fields[3]) * scale;
                    } else if ("node".equals(fields[0])) {
                        String id = fields[1];
                        double x = Double.parseDouble(fields[2]) * scaleX;
                        double y = H - Double.parseDouble(fields[3]) * scaleY;
                        nodeCenters.put(id, new Point2D.Double(x, y));
                    } else if ("stop".equals(fields[0])) {
                        break;
                    }
                }
            }
            toolThread.join();
            if (tm.exitCode != 0) {
                System.err.println("Running " + tm.getCommand() + " failed with " + tm.exitCode);
            }
        }
    }

    public ClNode getNode(MouseEvent e) {
        String nId = "";
        Point p = e.getPoint();
        Double d = null;
        for (Map.Entry<String, Point2D> c : nodeCenters.entrySet()) {
            if (null == d || c.getValue().distance(p) < d) {
                d = c.getValue().distance(p);
                nId = c.getKey();
            }
        }
        if (nId.startsWith("N")) {
            int id = Integer.parseInt(nId.substring(1),36);
            return nodeMap.get(id);
        }
        return null;
    }

    public ClNode getFirstNode() {
        return nodeMap.isEmpty() ? null : nodeMap.firstEntry().getValue();
    }

}
