import org.graphstream.graph.Edge;

public class EdgeWrapper {
    String tailID, headID, pathwayName, edgeType, tailSymbol, headSymbol;
    int weight, pathwayID;
    double flux;

    private Edge actualEdge;

    public EdgeWrapper( String tailID, String headID, String pathwayName, String edgeType, String tailSymbol, String headSymbol, int weight, int pathwayID ) {
        this.tailID = tailID;
        this.headID = headID;
        this.pathwayName = pathwayName;
        this.edgeType = edgeType;
        this.tailSymbol = tailSymbol;
        this.headSymbol = headSymbol;
        this.weight = weight;
        this.pathwayID = pathwayID;
    }

    public EdgeWrapper( String tailID, String headID) {
        this.tailID = tailID;
        this.headID = headID;
    }

    public EdgeWrapper(Edge edge) {
        this.actualEdge = edge;
    }

    public Edge getActualEdge() {
        return actualEdge;
    }

    public String getTailID() {
        return tailID;
    }

    public String getHeadID() {
        return headID;
    }

    public String getPathwayName() {
        return pathwayName;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public String getTailSymbol() {
        return tailSymbol;
    }

    public String getHeadSymbol() {
        return headSymbol;
    }

    public int getWeight() {
        return weight;
    }

    public int getPathwayID() {
        return pathwayID;
    }

    @Override
    public boolean equals( Object o ) {
        if ( ! (o instanceof EdgeWrapper ) ) return false;
        EdgeWrapper other = (( EdgeWrapper )o);
        return other.tailID.equals( this.tailID ) && other.headID.equals( this.headID );
    }

    public double getFlux() {
        return flux;
    }

    public EdgeWrapper setFlux( double flux ) {
        this.flux = flux;
        return this;
    }

    public Node getHeadNode() {
        return MyGraph.getNode( headID );
    }

    public Node getTailNode() {
        return MyGraph.getNode( tailID );
    }
}
