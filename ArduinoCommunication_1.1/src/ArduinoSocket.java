import java.net.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ArduinoSocket extends Thread
{
   private BDD bdd;
   private Socket socket = null;
   private BufferedInputStream in = null;
   private DataOutputStream out = null;
   private JLabel label;
   private JTable table;
   private String text;
   private Object[] ligne;
   private boolean firstMsg; 
   private int nbRow;
   
   public ArduinoSocket(Socket newSocket, BDD newBDD, JLabel newLabel, JTable newTable)
   {  
       socket = newSocket;
       bdd = newBDD;
       label = newLabel;
       table = newTable;
       ligne = new Object[4];
       firstMsg = true;
       nbRow = table.getRowCount();
       
       label.setText(String.valueOf(Integer.parseInt(label.getText()) + 1));
   }
   
   public void run()
   {
       try
      { 
         open();
         while (!socket.isClosed())
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
                
                if(firstMsg)
                {
                    updateTable(mac);
                    firstMsg = false;
                }
                
                if (text.substring(0, 3).equals("#C:"))
                {
                    int code = bdd.CodeRequest(mac);
                    
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
                    int etat = bdd.CheckRequest(mac);
                    
                    if (etat == 0) out.write('V');
                    else if (etat == 1) out.write('P');
                    else if (etat == 2) out.write('I');
                    else if (etat == 3) out.write('D');
                    else if (etat == -1) out.write('R');
                    else if (etat == -2) out.write('S');
                }
                else if (text.substring(0, 3).equals("#B:"))
                {
                    if (bdd.BlockRequest(mac) == -2) out.write('S');
                }
            }
            else text += userChar;
         }
         close();
      }
      catch(IOException ioe)
      {  
        System.out.println(ioe); 
        close();
      }
   }
   
   public void open() throws IOException
   {  
      in = new BufferedInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());
   }
   
   public void close()
   {  
      try {
          if (out != null) out.close();
          if (in != null)  in.close();
      } catch (IOException ex) {
          Logger.getLogger(ArduinoSocket.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   private void updateTable(String mac){
       try {
            ResultSet resultat;
            String requete;

            requete = "SELECT id_hotel FROM `t_chambre` WHERE addrMac = \"" + mac + "\"";
            resultat = bdd.SelectRequest("hotel", requete);
            resultat.beforeFirst();
            resultat.next();
            int idHotel = resultat.getInt(1);

            requete = "SELECT `nom` FROM `t_hotel` WHERE id_hotel=" + idHotel;
            resultat = bdd.SelectRequest("hotel", requete);
            resultat.beforeFirst();
            resultat.next();
            ligne[0] = resultat.getString(1);

            requete = "SELECT numero FROM `t_chambre` WHERE addrMac = \"" + mac + "\"";
            resultat = bdd.SelectRequest("hotel", requete);
            resultat.beforeFirst();
            resultat.next();
            ligne[1] = resultat.getInt(1);

            ligne[2] = mac;

            ligne[3] = "connected";

            DefaultTableModel md = (DefaultTableModel) table.getModel();
            md.addRow(ligne);
            table.setModel(md);
            
        } catch (SQLException ex) {
            Logger.getLogger(ArduinoSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
}