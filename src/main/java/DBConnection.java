import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
     static Connection getConnection() throws ClassNotFoundException, SQLException {
         Class.forName("com.mysql.jdbc.Driver");
         String myUrl = "jdbc:mysql://localhost/bitcoin";
        Connection conn = DriverManager.getConnection(myUrl, "root", "");
        return conn;
    }
}
