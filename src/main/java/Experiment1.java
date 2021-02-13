import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Experiment1 {

    // Main method: simply invoke everything
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        int days = Integer.parseInt(args[0]);
        long filterAmount = Long.parseLong(args[1]);
        LocalDateTime toTime = LocalDateTime.now();
        LocalDateTime fromTime = toTime.minusDays(days);
        System.out.println("We will parse data for " + fromTime.toString() + " to " + toTime.toString());
        GraphExtractor extractor = new GraphExtractor();
        Graph<String, WeightedEdge> graph = extractor.extractGraphFor(fromTime, toTime, 2445);
        HashSet<Object> addresses = new HashSet<>(graph.getVertices());
        Collection<String> vertices = new HashSet<>(graph.getVertices());
        for (String vertex : vertices) {
            boolean flag = false;
            for (WeightedEdge edge : graph.getIncidentEdges(vertex)) {
                if (edge.getValue() >= filterAmount) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                graph.removeVertex(vertex);
            }
        }
        System.out.println(graph.getVertexCount() + " for >" + filterAmount);
        WeakComponentClusterer<String, WeightedEdge> wcc = new WeakComponentClusterer<String, WeightedEdge>();
        //Collection<Graph<String, WeightedEdge>> components = FilterUtils.createAllInducedSubgraphs(wcc.apply(graph), graph);
        Set<Set<String>> components = wcc.apply(graph);
        for (Set<String> subgraph : components) {
            int size = subgraph.size();
            if (size > 15) {
                System.out.println("Component " + size);
            }
        }


    }
}
