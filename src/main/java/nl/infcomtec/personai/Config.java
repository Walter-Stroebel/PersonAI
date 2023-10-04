package nl.infcomtec.personai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * Standard configuration stuff.
 *
 * @author Walter Stroebel
 */
public class Config {

    public String fontName;
    public int fontStyle;
    public int fontSize;
    public ColorMapping[] colMapping;
    public static TreeMap<Integer, Color> colMap = new TreeMap<>();
    public boolean darkMode;

    public Font getFont() {
        return new Font(fontName, fontStyle, fontSize);
    }

    public Color mapTo(Color fromColor) {
        return mapTo(fromColor.getRGB());
    }

    public Color mapTo(int fromColor) {
        Color to = colMap.get(fromColor);
        if (null == to) {
            if (null != colMapping) {
                for (ColorMapping cm : colMapping) {
                    if (cm.fromColor == fromColor) {
                        colMap.put(fromColor, to = new Color(cm.toColor));
                        break;
                    }
                }
            }
            if (null == to) {
                if (darkMode) {
                    Color fr = new Color(fromColor);
                    to = new Color(255 - fr.getRed(), 255 - fr.getGreen(), 255 - fr.getBlue(), fr.getAlpha());
                    colMap.put(fromColor, to);
                } else {
                    colMap.put(fromColor, to = new Color(fromColor));
                }
                colMapping = new ColorMapping[colMap.size()];
                int i = 0;
                for (Map.Entry<Integer, Color> e : colMap.entrySet()) {
                    colMapping[i++] = new ColorMapping(e.getKey(), e.getValue().getRGB());
                }
            }
        }
        return to;
    }

    public JDialog editColors() {
        return new ColorDialog();
    }

    public static class ColorMapping {

        public int fromColor;
        public int toColor;

        public ColorMapping(int fromColor, int toColor) {
            this.fromColor = fromColor;
            this.toColor = toColor;
        }
    }

    private class ColorDialog extends JDialog {

        public ColorDialog() {
            Container cp = getContentPane();
            cp.setLayout(new BorderLayout());
            Box vert = Box.createVerticalBox();
            cp.add(vert, BorderLayout.CENTER);
            for (Map.Entry<Integer, Color> e : colMap.entrySet()) {
                final Color f = new Color(e.getKey());
                Box hor = Box.createHorizontalBox();
                JLabel lb = new JLabel("    ");
                lb.setOpaque(true);
                lb.setBackground(f);
                lb.setForeground(f);
                hor.add(lb);
                hor.add(new JButton(new AbstractAction("Change") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        Color c = JColorChooser.showDialog(ColorDialog.this, "Change", f);
                        if (null != c) {
                            JButton source = (JButton) ae.getSource();
                            source.setForeground(c);
                            source.setBackground(c.darker());
                            colMap.put(f.getRGB(), c);
                            colMapping = new ColorMapping[colMap.size()];
                            int i = 0;
                            for (Map.Entry<Integer, Color> e : colMap.entrySet()) {
                                colMapping[i++] = new ColorMapping(e.getKey(), e.getValue().getRGB());
                            }
                        }
                    }
                }));
                vert.add(hor);
            }
            pack();
        }
    }
}
