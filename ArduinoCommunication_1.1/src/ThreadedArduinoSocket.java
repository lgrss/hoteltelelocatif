import java.awt.Color;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ThreadedArduinoSocket extends Thread
{  private Socket socket = null;
   private ServerSocket server = null;
   
   private int Port;
   private BDD bdd;
   private JPanel Panel;
   private JLabel Label;
   
   
   
   public ThreadedArduinoSocket(int port, JPanel panel, JLabel label)
   {  
       Port = port;
       bdd = new BDD("jdbc:mysql://10.73.8.96:3306/", "root", "");
       Panel = panel;
       Label = label;
   }
   
   public void run()
   {
       int nbHost = 0;
       try
      {  System.out.println("Binding to port " + Port + ", please wait  ...");
         server = new ServerSocket(Port);  
         System.out.println("Server started: " + server);
         System.out.println("Waiting for a client ..."); 
         Panel.setBackground(Color.orange);
         
          while (true) {
            try {
                socket = server.accept();
            } catch (IOException e) {
                Panel.setBackground(Color.red);
            }
            new ArduinoSocket(socket, bdd).start();
            nbHost++;
            Label.setText(String.valueOf(nbHost));
            System.out.println("Client accepted: " + socket);
            Panel.setBackground(Color.green);
        }
         
    } catch (IOException ex) {
         Logger.getLogger(ThreadedArduinoSocket.class.getName()).log(Level.SEVERE, null, ex);
    }
   }
   
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (server != null)    server.close();
      Panel.setBackground(Color.red);
   }
  
}