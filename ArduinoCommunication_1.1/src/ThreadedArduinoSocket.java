import java.awt.Color;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

public class ThreadedArduinoSocket extends Thread
{  private Socket socket = null;
   private ServerSocket server = null;
   
   private int port;
   private BDD bdd;
   private JPanel panel;
   private JLabel label;
   private JTable table;
   
   
   public ThreadedArduinoSocket(int newPort, JPanel newPanel, JLabel newLabel, JTable newTable)
   {  
       port = newPort;
       bdd = new BDD("jdbc:mysql://localhost:3306/", "root", "");
       panel = newPanel;
       label = newLabel;
       table = newTable;
   }
   
   public void run()
   {
       try{
          server = new ServerSocket(port);  
          panel.setBackground(Color.orange);
         
          while (server.isBound()) {
            try {
                socket = server.accept();
            } catch (IOException e) {
                panel.setBackground(Color.red);
            }
            
            new ArduinoSocket(socket, bdd, label, table).start();
            panel.setBackground(Color.green);
        }
        close();
         
    } catch (IOException ex) {
         Logger.getLogger(ThreadedArduinoSocket.class.getName()).log(Level.SEVERE, null, ex);
    }
   }
   
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (server != null)    server.close();
      panel.setBackground(Color.red);
      label.setText("0");
   }
  
}