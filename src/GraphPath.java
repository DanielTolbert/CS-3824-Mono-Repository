import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Collection;

public class GraphPath {

    private ArrayList<EdgeWrapper> edgeWrappers;
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
        edgeWrappers = new ArrayList<>(  );
        for ( int i = 0; i < nodeIDs.length - 1; i++ ) {
            edgeWrappers.add( new EdgeWrapper( nodeIDs[i], nodeIDs[i + 1] ) );
        }
    }

    public boolean containsEdge( EdgeWrapper e) {
        boolean found = false;
        for ( EdgeWrapper edgeWrapper : edgeWrappers ) {
            if ( edgeWrapper.equals( e ) ) {
                found = true;
            }
        }
        return found;
    }

    public ArrayList<EdgeWrapper> getEdgeWrappers() {
        return edgeWrappers;
    }

}
