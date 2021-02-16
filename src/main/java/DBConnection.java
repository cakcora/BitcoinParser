import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        String myUrl = "jdbc:mysql://localhost/bitcoin";
        Connection conn = DriverManager.getConnection(myUrl, "root", "");
        return conn;
    }

    public static void saveEdgesInNetwork(Graph<String, WeightedEdge> graph) throws SQLException, ClassNotFoundException {
        Connection conn = DBConnection.getConnection();
        String resetQuery = "TRUNCATE TABLE `whalegraph`";
        PreparedStatement stt = conn.prepareStatement(resetQuery);
        stt.execute();

        // create a sql date object so we can use it in our INSERT statement
        // the mysql insert statement
        String edgeQuery = " insert into whalegraph (fromAddress, toAddress, amount)"
                + " values (?, ?, ?)";
        PreparedStatement preparedOccurrenceStmt = conn.prepareStatement(edgeQuery);
        int count = 0;
        // create the mysql insert preparedstatement
        for (WeightedEdge edge : graph.getEdges()) {
            try {
                Pair<String> pair = graph.getEndpoints(edge);
                String fromNode = pair.getFirst();
                String toNode = pair.getSecond();
                long value = edge.getValue();

                ++count;
                preparedOccurrenceStmt.setString(1, fromNode);
                preparedOccurrenceStmt.setString(2, toNode);
                preparedOccurrenceStmt.setLong(3, value);
                preparedOccurrenceStmt.addBatch();
                if (fromNode.length() > 70) System.out.println(fromNode);

                if (count == 100000) {
                    preparedOccurrenceStmt.executeBatch();
                    count = 0;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();

            }

        }
        if (count != 0)
            preparedOccurrenceStmt.executeBatch();
        conn.close();
    }
}