import java.awt.Color;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import org.apache.commons.io.IOUtils;

public class ArduinoSocket extends Thread
{  private Socket socket = null;
   private ServerSocket server = null;
   private BufferedInputStream in = null;
   private DataOutputStream out = null;
   
   private int Port;
   private BDD bdd;
   private JPanel Panel;
   private String text;
   
   
   public ArduinoSocket(int port, JPanel panel)
   {  
       Port = port;
       bdd = new BDD("jdbc:mysql://10.73.8.49:3306/", "root", "");
       Panel = panel;
   }
   
   public void run()
   {
       try
      {  System.out.println("Binding to port " + Port + ", please wait  ...");
         server = new ServerSocket(Port);  
         System.out.println("Server started: " + server);
         System.out.println("Waiting for a client ..."); 
         Panel.setBackground(Color.orange);
         socket = server.accept();
         System.out.println("Client accepted: " + socket);
         open();         
         while (server.isBound())
         {  
            int userText = in.read();
            char userChar = (char) userText;
            
            if(userChar == '#') text = "#";
            else if (userChar == '!')
            {
                String mac = "";
                for(int i=3; i<text.length(); i+=2){
                    mac += text.substring(i, i+2) + "-";
                }
                mac = mac.substring(0, mac.length()-1);
                
                if (text.substring(0, 3).equals("#C:"))
                {
                    int code = CodeRequest(mac);
                    
                    if (code == 1) out.write('P');
                    else if (code == -1) out.write('R');
                    else if (code == -2) out.write('S');
                    else{
                        out.write('C');
                        for(int i=1000; i>=1; i=i/10) out.write((code/i)%10);
                    }
                }
                else if (text.substring(0, 3).equals("#E:"))
                {
                    int etat = CheckRequest(mac);
                    
                    if (etat == 0) out.write('V');
                    else if (etat == 1) out.write('P');
                    else if (etat == -1) out.write('R');
                    else if (etat == -2) out.write('S');
                }
                else if (text.substring(0, 3).equals("#B:"))
                {
                    if (BlockRequest(mac) == -2) out.write('S');
                }
            }
            else text += userChar;
         }
         close();
         System.out.println("Connexion closed");
      }
      catch(IOException ioe)
      {  System.out.println(ioe); 
         Panel.setBackground(Color.red);
      }
   }
   public void open() throws IOException
   {  
      in = new BufferedInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());
      Panel.setBackground(Color.green);
   }
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (server != null)    server.close();
      if (out != null) out.close();
      if (in != null)  in.close();
      Panel.setBackground(Color.red);
   }
   public static void main(String args[])
   {  ArduinoSocket server = null;
      if (args.length != 1)
         System.out.println("Usage: java ChatServer port");
      else
         server = new ArduinoSocket(Integer.parseInt(args[0]), null);
   }
   private int CodeRequest(String mac)
   {
    try {
        int etat = CheckRequest(mac);
        if(etat != 0) return etat;
        String request = "SELECT code FROM `t_reservation` WHERE id_chambre=" + RoomRequest(mac);
        ResultSet resultat;
        resultat = bdd.SelectRequest("hotel", request);
        resultat.beforeFirst();
        resultat.next();
        int code = resultat.getInt(1);
        return code;
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }
   
   private int BlockRequest(String mac)
   {
    try {
        String request = "UPDATE `t_reservation` SET `PorteBloque`=1 WHERE id_chambre=" + RoomRequest(mac);
        return bdd.UpdateRequest("hotel", request);
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }
   
   private int CheckRequest(String mac){
    try {
        String request = "SELECT `PorteBloque` FROM `t_reservation` WHERE id_chambre=" + RoomRequest(mac);
        ResultSet resultat;
        resultat = bdd.SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        return resultat.getInt(1);
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }
   }

   private int RoomRequest(String mac)
   {
    try {
        String request = "SELECT id_chambre FROM `t_chambre` WHERE addrMac = \"" + mac + "\"";
        ResultSet resultat;
        resultat = bdd.SelectRequest("hotel", request);
        resultat.beforeFirst();
        if(!resultat.next()) return -1;
        return resultat.getInt(1);
    } catch (SQLException ex) {
        System.out.println(ex);
        return -2;
    }  
   }
}