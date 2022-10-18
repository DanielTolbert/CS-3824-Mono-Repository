import org.graphstream.graph.Graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class PathParser {

    static PathType currentPathType = PathType.EGFR1;

    public enum PathType {

        EGFR1("EGFR1"), TGF("TGF_beta_Receptor"), TNF("TNFalpha"), WNT("Wnt");

        String name;
        Graph graph;
        PathLinkerResults pathLinkerResults;
        DijkstraHandler dijkstraHandler;
        ArrayList<Node> sourceNodes = new ArrayList<>(  );
        ArrayList<Node> sinkNodes = new ArrayList<>(  );
        ArrayList<GraphPath> shortestPaths = new ArrayList<>(  );
        ArrayList<EdgeWrapper> rwrFluxMappedEdges = new ArrayList<>(  );

        PathType( String name ) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Graph getGraph() {
            return graph;
        }

        public void setGraph(Graph graph) {
            this.graph = graph;
        }

        public PathLinkerResults getPathLinkerResults() {
            return pathLinkerResults;
        }

        public void setPathLinkerResult(PathLinkerResults pathLinkerResult) {
            this.pathLinkerResults = pathLinkerResult;
        }

        public ArrayList<Node> getSourceNodes() {
            return sourceNodes;
        }

        public ArrayList<Node> getSinkNodes() {
            return sinkNodes;
        }

        public ArrayList<GraphPath> getShortestPaths() {
            return shortestPaths;
        }

        public DijkstraHandler getDijkstraHandler() {
            return dijkstraHandler;
        }

        public PathType setDijkstraHandler( DijkstraHandler dijkstraHandler ) {
            this.dijkstraHandler = dijkstraHandler;
            return this;
        }

        public ArrayList<EdgeWrapper> getRwrFluxMappedEdges() {
            return rwrFluxMappedEdges;
        }

        public void setRwrFluxMappedEdges(ArrayList<EdgeWrapper> rwrFluxMappedEdges) {
            this.rwrFluxMappedEdges = rwrFluxMappedEdges;
        }
    }

    public static Graph populateGraph(PathType type) {
        setPathType( type );
        addGraphNodes();
        addGraphConnections();
        return MyGraph.convertToDisplayableGraph();
    }

    private static void addGraphNodes() {
        File nodesFile = obtainNodesFile();
        Scanner scanner = null;
        try {
            scanner = new Scanner( nodesFile );
        } catch ( Exception e ) { }

        String headers = scanner.nextLine();
        while(scanner.hasNext()) {
            String[] nodeInfo = scanner.nextLine().split( "\\s+" );
            Node node = new Node(nodeInfo[0], nodeInfo[1], nodeInfo[2]);
            MyGraph.addNode( node );
            if ( node.getNodeType().equals( "tf" ) ) currentPathType.getSourceNodes().add( node );
            if ( node.getNodeType().equals( "receptor" ) ) currentPathType.getSinkNodes().add( node );

        }
    }

    private static void addGraphConnections() {
        File edgesFile = obtainEdgesFile();
        Scanner scanner = null;
        try { scanner = new Scanner( edgesFile ); }
        catch ( Exception e ){}
        String header = scanner.nextLine();
        while(scanner.hasNext()) {
            String[] nodeInfo = scanner.nextLine().split( "\t+" );
            String tailID = nodeInfo[0];
            String headID = nodeInfo[1];
            int weight = Integer.parseInt( nodeInfo[2] );
            String pathwayName = nodeInfo[3];
            String edgeType;
            int pathwayID;
            String tailSymbol;
            String headSymbol;
            if(nodeInfo.length == 8) {
                pathwayID = Integer.parseInt( nodeInfo[4] );
                edgeType = nodeInfo[5];
                tailSymbol = nodeInfo[6];
                headSymbol = nodeInfo[7];
            } else  {
                pathwayID = -1;
                edgeType = nodeInfo[4];
                tailSymbol = nodeInfo[5];
                headSymbol = nodeInfo[6];
            }
            EdgeWrapper edgeWrapper = new EdgeWrapper( tailID, headID, pathwayName, edgeType, tailSymbol, headSymbol, weight, pathwayID );
            MyGraph.addEdge( edgeWrapper );
            if(edgeType.equalsIgnoreCase( "physical" )) {
                EdgeWrapper secondEdgeWrapper = new EdgeWrapper(  headID, tailID, pathwayName, edgeType, headSymbol, tailSymbol, weight, pathwayID  );
                MyGraph.addEdge( secondEdgeWrapper );
            }
        }
    }

    public static void setPathType( PathType pathType) {
        currentPathType = pathType;
    }

    private static File obtainEdgesFile() {
        return new File( String.format("NetPath-pathways/NetPath-pathways/%s-edges.txt", currentPathType.getName()) );
    }

    public static PathType getCurrentPathType() {
        return currentPathType;
    }

    private static File obtainNodesFile() {
        return new File( String.format("NetPath-pathways/NetPath-pathways/%s-nodes.txt", currentPathType.getName()) );
    }


}
