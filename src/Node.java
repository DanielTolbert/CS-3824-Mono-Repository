import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class Node {

    String nodeNumber, nodeType, nodeSymbol;
    HashMap<Node, Edge> incomingNeighbors;
    HashMap<Node, Edge> outgoingNeighbors;

    public Node(String nodeNumber, String nodeType, String nodeSymbol) {
        this.nodeNumber = nodeNumber;
        this.nodeType = nodeType;
        this.nodeSymbol = nodeSymbol;
        this.incomingNeighbors = new HashMap<>(  );
        this.outgoingNeighbors = new HashMap<>(  );
    }

    public HashMap<Node, Edge> getIncomingNeighbors() {
        return incomingNeighbors;
    }

    public HashMap<Node, Edge> getOutgoingNeighbors() {
        return outgoingNeighbors;
    }

    public void appendIncomingNeighbor(Node node, Edge edge) {
        incomingNeighbors.put( node, edge );
    }

    public void appendOutgoingNeighbor(Node node, Edge edge) {
        outgoingNeighbors.put( node, edge );
    }

    public String getNodeNumber() {
        return nodeNumber;
    }

    public Node setNodeNumber( String nodeNumber ) {
        this.nodeNumber = nodeNumber;
        return this;
    }

    public String getNodeType() {
        return nodeType;
    }

    public Node setNodeType( String nodeType ) {
        this.nodeType = nodeType;
        return this;
    }

    public String getNodeSymbol() {
        return nodeSymbol;
    }

    public Node setNodeSymbol( String nodeSymbol ) {
        this.nodeSymbol = nodeSymbol;
        return this;
    }
}
