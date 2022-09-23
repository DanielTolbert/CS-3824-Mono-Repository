import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.HashMap;
import java.util.Objects;

public class MyGraph {
    private static HashMap<String, Node> graph = new HashMap<>(  );
    public static void addNode(Node node) {
        graph.put( node.nodeNumber, node );
    }

    public static void addEdge( Edge edge) {
        edge.getHeadNode().appendIncomingNeighbor( edge.getTailNode(), edge );
        edge.getTailNode().appendOutgoingNeighbor( edge.getHeadNode(), edge );
    }
    public static Node getNode(String identifier) {
        return graph.get( identifier );
    }

    public static Graph convertToDisplayableGraph() {
        Graph singleGraph = new SingleGraph( PathParser.getCurrentPathType().getName(), false, true );
        singleGraph.setStrict( true );
//        singleGraph.setAutoCreate( true );
        for ( Node source : graph.values() ) {
            if( Objects.isNull(singleGraph.getNode( source.getNodeSymbol() ))) {
                singleGraph.addNode( source.getNodeSymbol() );
            }
            for ( Node target : source.getOutgoingNeighbors().keySet() ) {
                if( Objects.isNull(singleGraph.getNode( target.getNodeSymbol() ))) {
                    singleGraph.addNode( target.getNodeSymbol() );
                }
                singleGraph.addEdge( String.format( "%s%s", source.getNodeSymbol(), target.getNodeSymbol() ), source.getNodeSymbol(), target.getNodeSymbol(), true ).setAttribute( "weight", source.getOutgoingNeighbors().get( target ).weight );
            }
        }
        return singleGraph;
    }

}
