package SMTP;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Authenticate_client {
    private final String FILENAME = null;
    socketManager clientSocObj;

    public void startClient() throws UnknownHostException, IOException{
        //Create Socket connection
        Socket clientSoc = new Socket("localhost", 6000);
        System.out.println("connection to server");
        clientSocObj = new socketManager(clientSoc);   

        // NOTE : PHASE 1 - give username
        //prompt for user name
        Scanner clObjScan = new Scanner(System.in);
        System.out.print ("Enter User Name: ");
        String uname = clObjScan.next();          //read all input as line
        clientSocObj.output.writeUTF(uname);        // send indo socket
        clientSocObj.output.flush();                //empty output buffer

        // NOTE : PHASE 2 - check username
        //rewind input stream for reading
        String AuthUserResponse = clientSocObj.input.readUTF();
        System.out.println(AuthUserResponse);

        //NOTE : PHASE 3 - check password
        if(AuthUserResponse.contains("334")){       //server replied that user found
            //System.out.println(AuthUserResponse);
            System.out.println ("Enter User Password:");
            String upass = clObjScan.next(); //PLAIN
            SaltpassHash sph = new SaltpassHash(upass);  //HASHED n SALTED
            String hashedPassw = sph.start();
            clientSocObj.output.writeUTF(hashedPassw);    
            // PLAIN clientSocObj.output.writeUTF(upass);
            clientSocObj.output.flush();
            
        // NOTE : PHASE 4 - check Passw
        //rewind input stream for reading
        String AuthPassResponse = clientSocObj.input.readUTF();
        System.out.println(AuthPassResponse);
            
        }
        else                
        {
            System.out.print ("user not found");
            return;
        }
    }

    public static void main(String args[]){
        Authenticate_client client = new Authenticate_client();
        try {
            client.startClient();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}