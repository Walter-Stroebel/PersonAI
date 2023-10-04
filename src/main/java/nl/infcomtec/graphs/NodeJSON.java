/*
 */
package nl.infcomtec.graphs;

/**
 *
 * @author walter
 */
public class NodeJSON {

    int fCol;
    int bCol;
    String label;
    String userObj;
    Integer from;
    Integer to;
    String shape;
    int id;

    public NodeJSON(int id, int fCol, int bCol) {
        this.id = id;
        this.fCol = fCol;
        this.bCol = bCol;
    }

}
