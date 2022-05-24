package SMTP;
  
import java.io.*;
import java.net.*;

public class Authenticate_server {
    private int currentTot;
    Socket client;
    socketManager clientSocObj;
    String bytesRead;
    String _username;

    public void start() throws IOException{
        ServerSocket serverSoc = new ServerSocket(6000);
        System.out.println("Waiting for connection from client");
         //accept connection from client
        Socket client = serverSoc.accept();
        clientSocObj = new socketManager(client);        
        try {
            System.out.println("client connected at port " + clientSocObj.soc.getPort());
            logInfo();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void logInfo() throws Exception{
        String usercheck;
        String username = clientSocObj.input.readUTF();
        PassFileRead check_uname = new PassFileRead();       // use class PassFileRead to access AuthFile
        usercheck = check_uname.SearchFile(username); // use method in class PassFileRead to check
        
        if(usercheck!="")
        {
            clientSocObj.output.writeUTF("334 AUTH PLAIN OK");        // send indo socket

            // check pasword
            String userpass = clientSocObj.input.readUTF();
            if (usercheck.equals(userpass)){
                _username = username;
                clientSocObj.output.writeUTF("235 2.7.0 Authentication successful");    //page 7 RFC4954
                clientSocObj.output.flush();  //lempty output buffer
            }
            else
            {
                clientSocObj.output.writeUTF("535 5.7.8p  Authentication credentials invalid");
                clientSocObj.output.flush();  //lempty output buffer
            }
        }
        else
        {
            clientSocObj.output.writeUTF("535 5.7.8u  Authentication credentials invalid");
            System.out.println("user not found");
        }
        clientSocObj.output.close();

    }
    
    public String GetUsername()
    {
        return _username;
    }
    
    public static void main(String[] args){
        Authenticate_server server = new Authenticate_server();
        try {
            server.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
    }       
}
