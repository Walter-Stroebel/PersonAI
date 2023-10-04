package nl.infcomtec.graphs;

import com.google.gson.Gson;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import nl.infcomtec.simpleimage.BitShape;
import nl.infcomtec.tools.ToolManager;

/**
 *
 * @author walter
 */
public class ClGraph {

    public static String dotColor(Color c) {
        return "\"#" + Integer.toHexString(c.getRGB()).substring(2) + "\"";
    }

    private final AtomicInteger uid = new AtomicInteger(0);
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
        uid.set(0);
    }

    public ClNode addNode(ClNode node) {
        nodeMap.put(node.getId(), node);
        return node;
    }

    public int genId() {
        return uid.incrementAndGet();
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

    protected synchronized List<ClNode> getNodes() {
        LinkedList<ClNode> ret = new LinkedList<>();
        for (ClNode n : nodeMap.values()) {
            if (!(n instanceof ClEdge)) {
                ret.add(n);
            }
        }
        return ret;
    }

    protected synchronized List<ClEdge> getEdges() {
        LinkedList<ClEdge> ret = new LinkedList<>();
        for (ClNode n : nodeMap.values()) {
            if (n instanceof ClEdge) {
                ret.add((ClEdge) n);
            }
        }
        return ret;
    }

    public void save(File f, Gson gson) {
        List<NodeJSON> l = new LinkedList<>();
        for (ClNode n : getNodes()) {
            NodeJSON nj = new NodeJSON(n.id, n.foreColor.getRGB(), n.backColor.getRGB());
            nj.label = n.label;
            if (!(n instanceof ClEdge)) {
                nj.userObj = n.getUserStr();
                nj.shape = n.shape;
                l.add(nj);
            }
        }
        for (ClEdge e : getEdges()) {
            NodeJSON nj = new NodeJSON(e.id, e.foreColor.getRGB(), e.backColor.getRGB());
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

    public synchronized void load(File f, Gson gson) {
        int lid = 0;
        clear();
        try ( FileReader fr = new FileReader(f)) {
            NodeJSON[] l = gson.fromJson(fr, NodeJSON[].class);
            for (NodeJSON nj : l) {
                lid = Math.max(lid, nj.id);
                if (null == nj.from) {
                    ClNode n = new ClNode(this, nj);
                    nodeMap.put(n.id, n);
                } else {
                    ClEdge e = new ClEdge(this, nj);
                    nodeMap.put(e.id, e);
                }
            }
            uid.set(lid);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from " + f, ex);
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
            int id = Integer.parseInt(nId.substring(1));
            return nodeMap.get(id);
        }
        return null;
    }

    public ClNode getFirstNode() {
        return nodeMap.isEmpty() ? null : nodeMap.firstEntry().getValue();
    }

}
