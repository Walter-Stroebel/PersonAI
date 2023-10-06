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

    public NodeJSON(int uid, int fCol, int bCol) {
        this.id = uid;
        this.fCol = fCol;
        this.bCol = bCol;
    }

}
