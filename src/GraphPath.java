import org.graphstream.graph.Node;
import scala.util.parsing.combinator.testing.Str;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class GraphPath {

    private ArrayList<Edge> edges;
    private String[] nodeIDs;

    public GraphPath(String...nodeIDs) {
        this.nodeIDs = nodeIDs;
        constructEdges();
    }

    public GraphPath( Collection<Node> nodes ) {
        nodeIDs = new String[nodes.size()];
        ArrayList<org.graphstream.graph.Node> arrayList = new ArrayList<>( nodes );
        for ( int i = 0; i < arrayList.size() ; i++ ) {
            nodeIDs[i] = arrayList.get( i ).getId();
        }
        constructEdges();
    }

    private void constructEdges() {
        edges = new ArrayList<>(  );
        for ( int i = 0; i < nodeIDs.length - 1; i++ ) {
            edges.add( new Edge( nodeIDs[i], nodeIDs[i + 1] ) );
        }
    }

    public boolean containsEdge(Edge e) {
        boolean found = false;
        for ( Edge edge : edges ) {
            if ( edge.equals( e ) ) {
                found = true;
            }
        }
        return found;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

}
