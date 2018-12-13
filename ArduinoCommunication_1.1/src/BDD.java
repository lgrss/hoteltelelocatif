import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class BDD {
    
    String URL;
    String User;
    String Pass;
    
    public BDD(String url, String user, String pass)
    {
        URL = url;
        User = user;
        Pass = pass;
    }
    
    public ResultSet SelectRequest(String BDD, String requete) throws SQLException
    {
        Connection connection = DriverManager.getConnection(URL+BDD, User, Pass);
        Statement statement = connection.createStatement();
        ResultSet resultat = statement.executeQuery(requete);
        return resultat;
    }
    
    public int UpdateRequest(String BDD, String requete) throws SQLException
    {
        Connection connection = DriverManager.getConnection(URL+BDD, User, Pass);
        Statement statement = connection.createStatement();
        return statement.executeUpdate(requete);
    }
}
