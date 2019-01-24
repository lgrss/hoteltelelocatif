import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

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
    
    public int CodeRequest(String mac)
    {
    try {
        int etat = CheckRequest(mac);
        if(etat != 0) return etat;
        String request = "SELECT code FROM `t_reservation` WHERE id_chambre=" + RoomRequest(mac);
        ResultSet resultat;
        resultat = SelectRequest("hotel", request);
        resultat.beforeFirst();
        resultat.next();
        int code = resultat.getInt(1);
        return code;
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }
   
   public int BlockRequest(String mac)
   {
    try {
        String request = "UPDATE `t_reservation` SET `PorteBloque`=1 WHERE id_chambre=" + RoomRequest(mac);
        return UpdateRequest("hotel", request);
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }
   
   public int CheckRequest(String mac){
    try {
        ResultSet resultat;
        
        int idChambre = RoomRequest(mac);
        
        String request = "SELECT `porteBloque` FROM `t_reservation` WHERE id_chambre=" + idChambre;
        resultat = SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        int resultat1 = resultat.getInt(1);
        if (resultat1 == 1) return 1;
        
        request = "SELECT `commandePaye` FROM `t_reservation` WHERE id_chambre=" + idChambre;
        resultat = SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        int resultat2 = resultat.getInt(1);
        if (resultat2 == 0) return 2;
        
        int resultat3 = DateRequest(mac, idChambre);
        if (resultat3 == 0) return 3;
        
        return 0;
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }

   public int DateRequest(String mac, int idChambre){
       try {
        ResultSet resultat;
        
        String request = "SELECT `dateDebut` FROM `t_reservation` WHERE id_chambre=" + idChambre;
        resultat = SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        Date date1 = resultat.getDate(1);
        
        request = "SELECT `dateFin` FROM `t_reservation` WHERE id_chambre=" + idChambre;
        resultat = SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        Date date2 = resultat.getDate(1);
        
        Date date = new Date();
        if(date1.before(date) && date2.after(date)) return 1;
        else return 0;
        
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }
   
   public int RoomRequest(String mac)
   {
    try {
        String request = "SELECT id_chambre FROM `t_chambre` WHERE addrMac = \"" + mac + "\"";
        ResultSet resultat;
        resultat = SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        return resultat.getInt(1);
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }  
   }
}
