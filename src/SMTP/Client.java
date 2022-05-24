package SMTP;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


public class Client {
    //Main Method:- called when running the class file.
    public static void main(String[] args){ 
        Scanner user_input = new Scanner(System.in);
        
        int portNumber = 5000;
        String serverIP = "localhost";   
        
        try{
        //Create a new socket for communication
            Socket soc = new Socket(serverIP,portNumber);
            DataInputStream dataIn = new DataInputStream(soc.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(soc.getOutputStream());
            String choice = "";
            String msgToServer;
            
            // Display start menu
            System.out.println("Welcome to the email service.");
            System.out.println("===============================");
            System.out.println("Give number for your choice.");
            System.out.println("1. Login \n2. Register");
            
            // Input Validation
            do {
            // Read user choice
            choice = user_input.nextLine();
            
            if (!choice.equals("1") && !choice.equals("2"))
                System.out.println("Invalid input. Give new value");
            }while (!choice.equals("1") && !choice.equals("2"));
            
            // Send user choice to server to be handled
            msgToServer = choice;
            dataOut.writeUTF(msgToServer);
            
            switch(choice)
                {
                    case "1":
                    {
                        if (dataIn.readUTF().equals("OK"))// Waits for server to run the authenticate server first
                        {
                            Authenticate_client authClient = new Authenticate_client();
                            try {
                                authClient.startClient();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //System.out.println("authClient created");
                        }
                        break;
                    }
                    case "2":
                    {
                        String username;
                        String password;
                        ValidationHandler valHandler = new ValidationHandler();
                        
                        // Username validation
                        do{
                            System.out.print("Give username: ");
                            username = user_input.nextLine();
                            
                            if (!valHandler.isValidUsername(username))
                                System.out.println("Invalid input. Give new username...");
                            
                        }while (!valHandler.isValidUsername(username));
                        
                        // Password validation
                        do{
                            System.out.print("Give password: ");
                            password = user_input.nextLine();
                            
                            if (!valHandler.isValidPassword(password))
                                System.out.println("Invalid input. Give new password...");
                            
                        }while (!valHandler.isValidPassword(password));
                        
                        dataOut.writeUTF(username);
                        dataOut.writeUTF(password);
                        
                        if (dataIn.readUTF().equals("OK"))// Waits for server to run the authenticate server first
                        {
                            Authenticate_client authClient = new Authenticate_client();
                            try {
                                authClient.startClient();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
            
            
                        
        // use a semaphpre for thread synchronisation
        // AtomicBoolen() can synchronise the value of the variable among threads
            AtomicBoolean isDATA = new AtomicBoolean(false);
        // create new instance of the client writer thread, intialise it and start it running
            ClientReader client_read = new ClientReader(soc, isDATA);
            Thread clientReadThread = new Thread(client_read);
        //AG++++++++++Thread.start() is required to actually create a new thread 
        //so that the runnable's run method is executed in parallel.
        //The difference is that Thread.start() starts a thread that calls the run() method,
        //while Runnable.run() just calls the run() method on the current thread
            clientReadThread.start();
            
        // create new instance of the client writer thread, intialise it and start it running
            ClientWriter client_write = new ClientWriter(soc, isDATA);
            Thread clientWriteThread = new Thread(client_write);
            clientWriteThread.start();
        }
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error in SMTP_Client --> " + except.getMessage());
        }
    }
}



//This thread is responcible for writing messages
class ClientReader implements Runnable
{
    public static String ClientDomainName = "MyTestDomain.gr";
    public static String CRLF = "\r\n";
    
    Socket crSocket = null;
    AtomicBoolean isDATAflag;
    String BYTESin= "";
    String sDataToServer;
    
    public ClientReader (Socket inputSoc, AtomicBoolean isDATA){
        crSocket = inputSoc;
        this.isDATAflag = isDATA;
    }
  
    public void run(){
        // method 'isDATAflag.get()' returns the current value of the smaphore
        while(!crSocket.isClosed() && !isDATAflag.get()){
        // while connection is open and NOT IN DATA exchange STATE
            try
            {
                DataInputStream dataIn = new DataInputStream(crSocket.getInputStream());
                BYTESin = dataIn.readUTF();
                System.out.println(BYTESin);
                if (BYTESin.contains("221"))  
                {
                    System.out.println("Gracefully closing socket - PART 2/2");
                    crSocket.close();
                    return;
                }  
                else if (BYTESin.contains("250"))  
                {
                    System.out.println("OK -> CLIENT going to state SUCCESS");
                }   
                else if (BYTESin.contains("500"))  
                    System.out.println("SERVER Error--> Syntax error, command unrecognized");
                else if (BYTESin.contains("501"))  
                    System.out.println("SERVER Error--> Syntax error in parameters or arguments");        
                else if (BYTESin.contains("504"))  
                    System.out.println("SERVER Error--> Command parameter not implemented");
                else if (BYTESin.contains("421"))  
                    System.out.println("SERVER Error-->Service not available, closing transmission channel");
                else if (BYTESin.contains("354"))
                {
                    System.out.println("OK -> CLIENT going to state I (wait for data)");
                    isDATAflag.set(true);
                }
                else if (BYTESin.contains("334"))
                {
                    System.out.println("OK -> CLIENT autehenticating");
                    isDATAflag.set(true);
                }
            }  
            catch (Exception except){
              //Exception thrown (except) when something went wrong, pushing message to the console
              System.out.println("Error in ClientReader --> " + except.getMessage());
            }
        }
    }
}


class ClientWriter implements Runnable
{
    public static String CRLF = "\r\n";
    public static String ClientDomainName = "MyTestDomain.com";
    public static String ClientEmailAddress = "myEmail@"+ClientDomainName;
    
    Socket cwSocket = null;
    AtomicBoolean isDATAflag;
    
    public ClientWriter (Socket outputSoc, AtomicBoolean isDATA){
        cwSocket = outputSoc;
        this.isDATAflag=isDATA;
    }
    
    public void run(){
        // initialise variables' value
        String msgToServer ="";
        String BYTESin= "";
    
        try{
            System.out.println ("GIVE NUMBER FOR SMTP COMMANDS:\n1. HELO \n2. MAIL FROM \n3. RCPT TO \n4. DATA \n0. QUIT");
            DataOutputStream dataOut = new DataOutputStream(cwSocket.getOutputStream());

            while (!cwSocket.isClosed()) {
                Scanner user_input = new Scanner(System.in);
                switch(user_input.nextInt()){
                    case 1: { // HELO
                        System.out.println("CLIENT WRITER SENDING HELO");
                        //
                        // SYNTAX (page 12 RFC 821)
                        // HELO <SP> <domain> <CRLF>
                        //
                        msgToServer = ("HELO"+" "+ClientDomainName+CRLF);
                        dataOut.writeUTF(msgToServer);
                        // CODE EXPL // When you write data to a stream, it is not written immediately, and it is buffered. 
                        // CODE EXPL // So use flush() when you need your data from buffer to be written
                        dataOut.flush();                         
                        break;
                    }
                    case 2: {
                        System.out.println("CLIENT WRITER SENDING MAIL");
                        //
                        // SYNTAX (page 28 RFC 821)
                        // MAIL <SP> FROM:<reverse-path> <CRLF>
                        //
                        String fromEmail = "";
                        System.out.println("From: ");
                        user_input.nextLine(); // Needed for correct input
                        
                        // Email Validation
                        ValidationHandler emailVal = new ValidationHandler();
                        do
                        {
                            fromEmail = user_input.nextLine();

                            if (!emailVal.isValidEmail(fromEmail))
                                System.out.println("Invalid input. Give new email...");

                        }while(!emailVal.isValidEmail(fromEmail));
                        
                        msgToServer = ("MAIL"+" "+"FROM:"+fromEmail+CRLF);
                        dataOut.writeUTF(msgToServer);
                        dataOut.flush();                         
                        break;
                    }                    
                    case 3: {
                        System.out.println("CLIENT WRITER SENDING RCPT");
                        //
                        // SYNTAX (page 28 RFC 821)
                        // RCPT <SP> TO:<forward-path> <CRLF>
                        //
                        String forwardPath = "";
                        System.out.print("TO: ");
                        user_input.nextLine(); // Needed for correct forward path input
                        
                        // Email Validation
                        ValidationHandler emailVal = new ValidationHandler();
                        do
                        {
                            forwardPath = user_input.nextLine();

                            if (!emailVal.isValidEmail(forwardPath))
                                System.out.println("Invalid input. Give new email...");

                        }while(!emailVal.isValidEmail(forwardPath));
                                        
                        msgToServer = ("RCPT"+" "+"TO:"+forwardPath+CRLF);
                        dataOut.writeUTF(msgToServer);
                        dataOut.flush();                         
                        break;
                    } 
                    case 4: {
                        System.out.println("CLIENT WRITER SENDING DATA");
                        //
                        // SYNTAX (page 5 and 28 RFC 821)
                        // DATA bla_bla_bla <CRLF>.<CRLF>
                        //
                        
                        // Handling user input with subject and body
                        //
                        // DATA message has a certain format in order to differentiate the subject from the body
                        // The client and server both send and recieve with this specific format
                        // The format is the next line
                        // "DATA<subject>-_-_-<body>CRLF.CRLF"
                        // "-_-_-" This string is used to split the subject from the body and save them appropriately using the String.split("-_-_-") method
                        msgToServer = "DATA";
                        
                        String Subject = "";
                        String Body;
                        String read = "";
                        
                        System.out.println("Subject: ");
                        user_input.nextLine();
                        Subject = user_input.nextLine();
                        msgToServer = msgToServer + Subject;
                        msgToServer = msgToServer + "-_-_-"; // Adds divider
                        
                        System.out.println("Press <Enter> twice to end message.");
                        System.out.println("Body:");
                        
                        
                        // User input allows multi-line messages
                        while (user_input.hasNextLine()){
                            read = user_input.nextLine();
                            if (read == null || read.isEmpty())
                            {
                                System.out.println("Breaking");
                                break;
                            }
                            msgToServer = msgToServer + read + CRLF;
                        }
                        
                        msgToServer = msgToServer + "." + CRLF;
                        dataOut.writeUTF(msgToServer);
                        dataOut.flush();                         
                        break;
                    }
                    // Unused case
                    case 5:{
                        System.out.println("CLIENT : EMAIL COMPOSITION");
                        msgToServer = "Date: 23 Oct 81 11:22:33";
                        msgToServer = msgToServer+"From: SMTP@HOSTY.ARPA";
                        msgToServer = msgToServer+"To: JOE@HOSTW.ARPA";
                        msgToServer = msgToServer+"Subject: Mail System Problem";
                        msgToServer = msgToServer+"xaxaxa";
                        msgToServer = msgToServer+CRLF+"."+CRLF;
                        dataOut.writeUTF(msgToServer);
                        dataOut.flush();
                        System.out.println (msgToServer);
                        //
                        isDATAflag.set(false);
                        break;
                    }
                    case 0: {
                        System.out.print("CLIENT : QUITing");
                        //
                        // SYNTAX (page 12 RFC 821)
                        // QUIT <CRLF>
                        //                        
                        msgToServer = ("QUIT"+CRLF);
                        dataOut.writeUTF(msgToServer);
                        dataOut.flush();                         
                        System.out.println("Gracefully closing socket - PART 1/2");
                        // NOTE:  IF STATED cwSocket.close(); THEN SOCKET WOULD BE CLOSED BEFORE SERVER RESPONSE!
                        return; // CODE EXPL // break WOULD NOT BE PROPER AS IT EXITS SWITCH BUT NOT THREAD
                        // NOTE: IF TERMINATING THREAD HERE, SERVER RESPONSE WOULD NOT BE READ AND GRACEFULL CLOSE WOULD NOT BE POSSIBLE
                    }//case 
                }//switch     
            }//while           
        }//try
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error in ClientWriter --> " + except.getMessage());
        }
    }
}


