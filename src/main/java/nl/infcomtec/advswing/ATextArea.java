package nl.infcomtec.advswing;

import javax.swing.JTextArea;

/**
 *
 * @author walter
 */
public class ATextArea extends JTextArea{

    public ATextArea(String text) {
        super(text);
    }

    public ATextArea() {
    }

    public ATextArea(int rows, int columns) {
        super(rows, columns);
    }

    public ATextArea(String text, int rows, int columns) {
        super(text, rows, columns);
    }

    
}
