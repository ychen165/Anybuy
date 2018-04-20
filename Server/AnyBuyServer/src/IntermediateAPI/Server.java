package IntermediateAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends ServerSocket {
    private static final int SERVER_PORT = 18416;
    
    public static void main (String[] args) throws IOException {
    		new Server();
    }
 
    public Server()throws IOException {
        super(SERVER_PORT);
        API.writeLog("Server ready. Port " + this.getLocalPort());
 
        try {
            while (true) {
                Socket socket = accept();
                //Start another thread when requests received.
                new CreateServerThread(socket);
            }
        }catch (IOException e) {
        }finally {
            close();
        }
    }
 
    //Thread Class
    class CreateServerThread extends Thread {
        private Socket client;
        private BufferedReader bufferedReader;
        private PrintWriter printWriter;
 
        public CreateServerThread(Socket s)throws IOException {
            client = s;
 
            bufferedReader =new BufferedReader(new InputStreamReader(client.getInputStream()));
             
            printWriter =new PrintWriter(client.getOutputStream(),true);
            System.out.println("Client(" + getName() +") come in...");
             
            start();
        }
 
        public void run() {
            try {
                String line = bufferedReader.readLine();
                
                while (line != null) {
                    printWriter.println(line.hashCode());
                    System.out.println("Client(" + getName() +") say: " + line);
                    System.out.println("" + API.getCommand(line));
                    line = bufferedReader.readLine();
                }
                
                printWriter.println("bye, Client(" + getName() +")!");
                 
                System.out.println("Client(" + getName() +") exit!");
                printWriter.close();
                bufferedReader.close();
                client.close();
            }catch (IOException e) {
            }
        }
    }
}