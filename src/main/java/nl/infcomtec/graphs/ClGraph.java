package nl.infcomtec.graphs;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
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
    public HashMap<Point2D, BitShape> segments;

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

    public void draw(OutputStream out) {
        ToolManager tm = new ToolManager() {
            @Override
            public void run() {
                internalRun();
            }
        };
        StringBuilder gr = new StringBuilder();
        for (ClNode n : nodeMap.values()) {
            if (null != n && !(n instanceof ClEdge)) {
                gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
            }
        }
        for (ClNode n : nodeMap.values()) {
            if (n instanceof ClEdge) {
                gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
            }
        }
        tm.setInput(gr.toString());
        tm.setOutput(out);
        tm.setCommand("dot", "-Tpng");
        tm.run();
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
            StringBuilder gr = new StringBuilder("digraph {").append(System.lineSeparator());
            for (ClNode n : nodeMap.values()) {
                if (null != n && !(n instanceof ClEdge)) {
                    gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
                }
            }
            for (ClNode n : nodeMap.values()) {
                if (n instanceof ClEdge) {
                    gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
                }
            }
            tm.setInput(gr.append("}").append(System.lineSeparator()).toString());
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
            StringBuilder gr = new StringBuilder("digraph {").append(System.lineSeparator());
            for (ClNode n : nodeMap.values()) {
                if (null != n && !(n instanceof ClEdge)) {
                    gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
                }
            }
            for (ClNode n : nodeMap.values()) {
                if (n instanceof ClEdge) {
                    gr.append(n.generateDotRepresentation()).append(System.lineSeparator());
                }
            }
            tm.setInput(gr.append("}").append(System.lineSeparator()).toString());
            tm.setOutput(out);
            tm.setCommand("dot", "-Tplain");
            Thread toolThread = new Thread(tm);
            toolThread.start();

            // Read plain text format from PipedInputStream
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
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

}
