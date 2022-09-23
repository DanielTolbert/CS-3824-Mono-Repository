import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class PathLinkerResults {

    private PathParser.PathType pathType;
    private File rankedEdgesFile;
    private File pathsFile;

    private ArrayList<ArrayList<Edge>> rankedEdges;
    private ArrayList<ArrayList<GraphPath>> graphPaths;
    private Double[] recall;
    private Double[] precision;
    private int totalDistinctEdges;

    public PathLinkerResults (PathParser.PathType pathType) {
        this.pathType = pathType;
        retrievePathsFiles();
        parseRankedEdgesFile();
        parsePathsFile();
        this.totalDistinctEdges = getDistinctTotalEdges();
        computeAllPrecisionAndRecall();
    }

    public void retrievePathsFiles() {
        rankedEdgesFile = new File( String.format("NetPath-pathways/PathLinker-results/%s-k_20000-ranked-edges.txt", pathType.getName()) );
        pathsFile = new File( String.format("NetPath-pathways/PathLinker-results/%s-k_20000-paths.txt", pathType.getName()) );
    }

    public void parseRankedEdgesFile() {
        rankedEdges = new ArrayList<>( 1000 );
        Scanner scanner = null;
        try {
            scanner = new Scanner( rankedEdgesFile );
        } catch ( Exception e) {
            e.printStackTrace();
        }
        String header = scanner.nextLine();
        while(scanner.hasNext()) {
            String[] info = scanner.nextLine().split( "\\s+" );
            if ( info.length == 3 ) {
                int rank = Integer.parseInt( info[2] );
                while(rankedEdges.size() <= rank - 1) {
                    rankedEdges.add( new ArrayList<>(  ) );
                }
                rankedEdges.get( rank - 1 ).add( new Edge( info[0], info[1] ) );
            }
        }
    }


    private int getDistinctTotalEdges() {
        Set<Edge> edgeSet = new HashSet<>(  );
        for ( ArrayList<GraphPath> graphPath : graphPaths ) {
            for ( GraphPath path : graphPath ) {
                edgeSet.addAll( path.getEdges() );
            }
        }
        return edgeSet.size();
    }

    public void parsePathsFile() {
        //TODO
        graphPaths = new ArrayList<>(  );
        Scanner scanner = null;
        try {
            scanner = new Scanner( pathsFile );
        } catch ( Exception e) {
            e.printStackTrace();
        }
        String header = scanner.nextLine();
        while ( scanner.hasNext() ) {
            String[] info = scanner.nextLine().split( "\\s+" );
            int rank = Integer.parseInt( info[0] );
            String[] nodeIDs = info[2].split( "\\|" );
            while(graphPaths.size() <= rank - 1) {
                graphPaths.add( new ArrayList<>(  ) );
            }
//            graphPaths.add( rank - 1, new ArrayList<>(  ) );
            graphPaths.get( rank - 1 ).add( new GraphPath( nodeIDs ) );
        }

    }
    
    private void computeAllPrecisionAndRecall() {
        recall = new Double[rankedEdges.size()];
        precision = new Double[rankedEdges.size()];
        int totalPathwayEdges = totalDistinctEdges;
        int truePositives = 0;
        for ( int i = 0; i < recall.length; i++ ) {
            if ( rankedEdges.get( i ).isEmpty() ) {
                recall[i] = Double.NaN;
                precision[i] = Double.NaN;
                continue;
            }
            for ( Edge edge : rankedEdges.get( i ) ) {
                for ( GraphPath graphPath : graphPaths.get( i ) ) {
                    if ( graphPath.containsEdge( edge ) ) {
                        truePositives++;
                    }
                }
            }
            recall[i] = ((double)truePositives)/totalPathwayEdges;
            precision[i] = ((double)truePositives)/i;
        }
    }
    

    public double getRecall(int rank) {
        return recall[rank];
    }

    public double getPrecision(int rank) {
        return precision[rank];
    }

    public ArrayList<ArrayList<Edge>> getRankedEdges() {
        return rankedEdges;
    }

    public ArrayList<ArrayList<GraphPath>> getGraphPaths() {
        return graphPaths;
    }
}
