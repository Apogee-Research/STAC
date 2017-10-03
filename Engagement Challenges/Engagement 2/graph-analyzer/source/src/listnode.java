
public class listnode {

    int xpos, ypos;  // where on the circuit is it
    boolean up;      // up layer or down layer
    boolean block;   // does it block (is it a via)
    int net;         // what net does it correspond to
    listnode link;
}
