import java.util.HashMap;

public class Node {

    String nodeNumber, nodeType, nodeSymbol;
    HashMap<Node, EdgeWrapper> incomingNeighbors;
    HashMap<Node, EdgeWrapper> outgoingNeighbors;

    public Node(String nodeNumber, String nodeType, String nodeSymbol) {
        this.nodeNumber = nodeNumber;
        this.nodeType = nodeType;
        this.nodeSymbol = nodeSymbol;
        this.incomingNeighbors = new HashMap<>(  );
        this.outgoingNeighbors = new HashMap<>(  );
    }

    public HashMap<Node, EdgeWrapper> getIncomingNeighbors() {
        return incomingNeighbors;
    }

    public HashMap<Node, EdgeWrapper> getOutgoingNeighbors() {
        return outgoingNeighbors;
    }

    public void appendIncomingNeighbor(Node node, EdgeWrapper edgeWrapper ) {
        incomingNeighbors.put( node, edgeWrapper );
    }

    public void appendOutgoingNeighbor(Node node, EdgeWrapper edgeWrapper ) {
        outgoingNeighbors.put( node, edgeWrapper );
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
