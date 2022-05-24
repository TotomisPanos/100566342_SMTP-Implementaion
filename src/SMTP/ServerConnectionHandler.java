package SMTP;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ServerConnectionHandler implements Runnable
{
    public static String CRLF = "\r\n";
    public static String LocalServerDomainName = "ServerDomain.gr";
    private static String CommandStack = "";
    socketManager socketManagerObjectVariable = null;
    ArrayList<socketManager> ActiveClientsArrayVariable = null;
    public mailMessageContainer messageContainer = new mailMessageContainer();
    String username;
    
    public ServerConnectionHandler (ArrayList<socketManager> inArrayListVar, socketManager inSocMngVar){
        socketManagerObjectVariable = inSocMngVar;
        ActiveClientsArrayVariable = inArrayListVar;
    }
    
    public void run(){
        try{
                System.out.println("SERVER: "+ActiveClientsArrayVariable.size()+" Clients " + socketManagerObjectVariable.soc.getPort() + " Connected");
                String clientMSG;
                String choice = "";
                 
                clientMSG = socketManagerObjectVariable.input.readUTF();
                
                choice = clientMSG;
                switch(choice)
                {
                    case "1":
                    {
                        UserLogin();
                        break;
                    }
                    case "2":
                    {
                        // Reads username and password from the client
                        String username = socketManagerObjectVariable.input.readUTF();
                        String password = socketManagerObjectVariable.input.readUTF();
                        
                        // Adding user to the user file
                        PassFileWrite pfw = new PassFileWrite();
                        pfw.ToFile(username, password);
                        
                        // Adding mailbox for the user
                        mailFileHandler mfh = new mailFileHandler(username);
                        mfh.CreateMailbox();
                        
                        // Login procedure after the initial registration
                        UserLogin();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                
                
                
                while (!socketManagerObjectVariable.soc.isClosed()) {
            	clientMSG = socketManagerObjectVariable.input.readUTF();
                System.out.println("SERVER: message FROM CLIENT : " + socketManagerObjectVariable.soc.getPort() + " --> " + clientMSG); 

                //Check for Quit message for client 
                if (clientMSG.startsWith("QUIT")) {
                    System.out.println("5 SERVER: quiting client");
                    //
                    // SYNTAX (page 12 RFC 821)
                    // QUIT <SP> <SERVER domain> <SP> Service closing transmission channel<CRLF>
                    //          
	            socketManagerObjectVariable.output.writeUTF("221" + " " + LocalServerDomainName + " " + " Service closing transmission channel" + CRLF);                    
                    ActiveClientsArrayVariable.remove(socketManagerObjectVariable);
                    System.out.print("5 SERVER: active clients : "+ActiveClientsArrayVariable.size());
                    CommandStack="";
                    
                    // Creates new file in the mailbox with all the email information recorded
                    mailFileHandler mfh = new mailFileHandler(username, messageContainer);
                    mfh.HandleMail();
                    
                    // exiting thread
                    return;     
                }
                // NOTE: execution of the SMTP inside the thread for the incoming request
                Server_SMTP_Handler(socketManagerObjectVariable, clientMSG);
            }   //while socket NOT CLOSED
        }
        catch (IOException except){
            //Exception thrown (except) when something went wrong, pushing clientMSG to the console
            System.out.println("Error in Server Connection Handler --> " + except.getMessage());
        }
    }

    public void UserLogin() throws IOException
    {
        socketManagerObjectVariable.output.writeUTF("OK"); // Server needs to run the authenticate server first so it makes the client wait
        System.out.println("Creating authServer");
        Authenticate_server authServer = new Authenticate_server();
        try {
            authServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        username = authServer.GetUsername();
    }
    
    
    
    
    private void Server_SMTP_Handler(socketManager sm, String clientMSG) throws IOException {

        boolean REQUESTED_DOMAIN_NOT_AVAILABLE = false;
        String LocalServerDomainName = "ServerDomain.com";       
        boolean SMTP_OUT_OF_STORAGE = false;
        boolean SMTP_INSUFFICIENT_STORAGE = false;
        boolean SMTP_LOCAL_PROCESSING_ERROR = false;
        boolean SUCCESS_STATE = false;
        boolean WAIT_STATE = true;    
        String sResponceToClient = "";

        // Unused arraylists
        ArrayList<String> UsersInServerDomain = new ArrayList<String>();
        UsersInServerDomain.add("Alice");
        UsersInServerDomain.add("Bob");
        UsersInServerDomain.add("Mike");
        
        ArrayList<String> PasswInServerDomain = new ArrayList<String>();
        PasswInServerDomain.add("AlicePassw");
        PasswInServerDomain.add("BobPassw");
        PasswInServerDomain.add("MikePassw");
        
        ArrayList<String> KnownDomains = new ArrayList<String>();
        KnownDomains.add("ThatDomain.gr");
        KnownDomains.add("MyTestDomain.gr");
        KnownDomains.add("ServerDomain.gr");

        String mail_data;
        ArrayList<String> mail_data_buffer = new ArrayList<String>();
        ArrayList<String> forward_path_buffer = new ArrayList<String>();
        ArrayList<String> reverse_path_buffer = new ArrayList<String>();

        boolean GO_ON_CHECKS = true;

        try{
         
            if(clientMSG.contains(CRLF))
            {
                System.out.println("SERVER: from client RECEIVED--> " + clientMSG);
    
                if (clientMSG.contains("QUIT")) 
                {
                    GO_ON_CHECKS = false;
                    CommandStack = "";
                }   
            ////////////////////////////////////////////////////////////////////
            // HELO CMD MESSSAGES PACK
            ////////////////////////////////////////////////////////////////////
                // error 500 -> Line too long ! COMMAND CASE = 512
                else if (clientMSG.length()> 512 && GO_ON_CHECKS) 
                {
                    sResponceToClient = "500"+ CRLF;
                    System.out.println("error 500 -> Line too long");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                }                
                // error 501 -> Syntax error in parameters or arguments
                else if (clientMSG.split(" ").length < 1  && GO_ON_CHECKS) 
                {
                    sResponceToClient = "501"+ CRLF;
                    //System.out.println("error 501 -> Syntax error in parameters or arguments");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 504 -> Command parameter not implemented
                else if (clientMSG.length()<4 && GO_ON_CHECKS) 
                {
                    sResponceToClient = "504"+ CRLF;
                    //System.out.println("error 504 -> Command parameter not implemented");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 421 -> <domain> Service not available
                else if (REQUESTED_DOMAIN_NOT_AVAILABLE && GO_ON_CHECKS) 
                {
                    sResponceToClient = "421"+ CRLF;
                    String domain_not_found = clientMSG.replaceAll("HELO ", "");
                    domain_not_found = domain_not_found.replaceAll(CRLF,"");
                    //System.out.println("error 421 -> "+ domain_not_found +" Service not available");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                else if (clientMSG.contains("HELO") && GO_ON_CHECKS) 
                {
                    sResponceToClient = "250" + " " + LocalServerDomainName + CRLF;
                    //System.out.println("SERVER responce: "+ sResponceToClient);
                    SUCCESS_STATE = true;
                    GO_ON_CHECKS = false;
                    CommandStack = CommandStack + "HELO ";
                    System.out.println("CommandStack "+CommandStack);
                    //rfc 4954 page 7
                    sResponceToClient = "250" + " " + "AUTH " +"PLAIN" + CRLF;
                    
                    // Adds date to messageContainer in order to be saved in the mailbox
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    messageContainer.date_container = now.toString();
                }
            ////////////////////////////////////////////////////////////////////
            // END HELO
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////                               
            //  MAIL CMD MESSSAGES PACK
            ////////////////////////////////////////////////////////////////////
                // error 552 -> Requested mail action aborted: exceeded storage allocation
                else if (SMTP_OUT_OF_STORAGE && GO_ON_CHECKS) 
                {
                    sResponceToClient = "552"+ CRLF;
                    //System.out.println("error 552 -> Requested mail action aborted: exceeded storage allocation");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 451 -> Requested action aborted: local error in processing
                else if (SMTP_LOCAL_PROCESSING_ERROR && GO_ON_CHECKS) 
                {
                    sResponceToClient = "451"+ CRLF;
                    //System.out.println("error 451 -> Requested action aborted: local error in processing");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 452 -> <domain> Service not available
                else if (SMTP_INSUFFICIENT_STORAGE && GO_ON_CHECKS) 
                {
                    sResponceToClient = "452"+ CRLF;
                    //System.out.println("error 452 -> Requested action not taken: insufficient system storage");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // MAIL FROM -> OK
                //
                // SYNTAX (page 28 RFC 821)
                // MAIL <SP> FROM:<reverse-path> <CRLF>
                //                      
                // Upper  and lower case alphabetic characters are to be treated identically
                else if (clientMSG.toUpperCase().contains("MAIL") && GO_ON_CHECKS) 
                {
                    //
                    // SYNTAX (page 5 RFC 821)
                    // 250 <LF> OK
                    // 
                    String[] tmp = clientMSG.split("@");
                    if (tmp.length != 2) {
			System.out.println("501 Syntax error in parameters or arguments");
                        sResponceToClient = "501"+ CRLF;
                    }
                    else
                    {
                        String targetUser = tmp[0];
                        boolean error450 = true;
                        for(String userNames : UsersInServerDomain )
                        {
                            if (  targetUser.toUpperCase().equals( userNames.toUpperCase() )  )
                            {
                                error450 = false;
                                break;
                            }
                        }
                        if (error450)
                        {
                            System.out.println("error 450 -> Requested mail action not taken: mailbox unavailable");                                
                            sResponceToClient = "450"+ CRLF;
                        }
                        
                        String targetHost = tmp[1];                    
                    }
                    sResponceToClient = "250" + " " + "OK" + CRLF;
                    //System.out.println("SERVER responce: "+ sResponceToClient);
                    SUCCESS_STATE = true;
                    GO_ON_CHECKS = false;
                    CommandStack = CommandStack + "MAIL ";
                    System.out.println("CommandStack "+CommandStack);
                    mail_data_buffer = null;
                    forward_path_buffer = null;
                    
                    // Adds sender information to messageContainer in order to be saved in the mailbox
                    messageContainer.from_container = clientMSG;
                    
                    String reversePath = clientMSG.replace("MAIL FROM: ", "").replace(CRLF, "");
                    reverse_path_buffer.add(reversePath); 
                }
             ////////////////////////////////////////////////////////////////////
            // END MAIL FROM
            ////////////////////////////////////////////////////////////////////
            //  RCPT CMD MESSSAGES PACK
            ////////////////////////////////////////////////////////////////////               
                // error 550 -> Requested action not taken: mailbox unavailable
                else if (false) 
                {
                    sResponceToClient = "550"+ CRLF;
                    System.out.println("error 550 -> Requested action not taken: mailbox unavailable");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // 251 -> User not local; will forward to <forward-path>
                else if (false) //!KnownDomains.contains(clientMSG.substring(clientMSG.indexOf("@")+1, clientMSG.indexOf("\\") -1))
                { 
                    sResponceToClient = "251"+ CRLF;
                    System.out.println("error 251 -> User not local; will forward to <forward-path>");
                    SUCCESS_STATE = true;
                }
                // error 551 -> User not local; please try <forward-path>
                else if (false) //clientMSG.contains("ServerDomain.gr") && !UsersInServerDomain.contains(clientMSG.replace("SEND TO: ", "").replace("@ServerDomain.gr"+CRLF, ""))
                {
                    sResponceToClient = "551"+ CRLF;
                    System.out.println("error 551 -> User not local; please try <forward-path>");
                    GO_ON_CHECKS = false;
                } 
                // error 552 -> in MAIL MSG list
                // error 553 -> Requested action not taken: mailbox name not allowed
                else if (false) 
                {
                    sResponceToClient = "553"+ CRLF;
                    System.out.println("error 553 -> Requested action not taken: mailbox name not allowed");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 450 -> Requested mail action not taken: mailbox unavailable
                else if (false) 
                {
                    sResponceToClient = "450"+ CRLF;
                    System.out.println("error 450 -> Requested mail action not taken: mailbox unavailable");
                    SUCCESS_STATE = false;
                    GO_ON_CHECKS = false;
                } 
                // error 451 -> in MAIL MSG list
                // error 452 -> in MAIL MSG list
                // error 500 -> in MAIL MSG list
                // error 501 -> in MAIL MSG list
                // error 421 -> in MAIL MSG list

                // MAIL TO -> OK
                //
                // SYNTAX (page 28 RFC 821)
                // RCPT <SP> TO:<forward-path> <CRLF>
                //                      
                // Upper  and lower case alphabetic characters are to be treated identically
                else if (clientMSG.toUpperCase().contains("RCPT")) 
                {
                    // error 503 -> Bad sequence of commands
                    System.out.println("RCPT");
                    if(!CommandStack.contains("MAIL"))
                    {
                        sResponceToClient = "503";
                        System.out.println("error 503 -> Bad sequence of commands");
                        SUCCESS_STATE = false;
                        GO_ON_CHECKS = false;
                    }
                    else 
                    {
                        //
                        // SYNTAX (page 5 RFC 821)
                        // 250 <LF> OK
                        //                    
                        sResponceToClient = "250" + " " + "OK" + CRLF;
                        System.out.println("SERVER responce: "+ sResponceToClient);
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        CommandStack = CommandStack + "RCPT ";
                        System.out.println("CommandStack "+CommandStack); 
                        String forwardPath = clientMSG.replace("RCPT TO: ", "").replace(CRLF, "");
                        forward_path_buffer.add(forwardPath);    
                        
                        // Adds recipient information to messageContainer in order to be saved in the mailbox
                        messageContainer.to_container = forwardPath;
                    }
                }                
                // DATA
                //
                // SYNTAX (page 28 RFC 821)
                // DATA <CRLF>.<CRLF>
                //                      
                else if (clientMSG.toUpperCase().contains("DATA") && GO_ON_CHECKS) 
                {
                    // error 503 -> Bad sequence of commands
                    System.out.println("SERVER SIDE: DATA");
                    if(!CommandStack.contains("MAIL") && !CommandStack.contains("RCPT"))
                    {
                        sResponceToClient = "503";
                        System.out.println("error 503 -> Bad sequence of commands");
                        SUCCESS_STATE = false;
                        GO_ON_CHECKS = false;
                    }
                    else
                    {
                    //
                    // SYNTAX (page 15 RFC 821)
                    // 354 send the mail data, end with .
                    //                    
                        CommandStack = CommandStack + "DATA ";

                        System.out.println(clientMSG);
                       
                        mail_data = clientMSG.replace(CRLF + "." + CRLF, "");
                        mail_data = mail_data.replace("DATA", "");
                        
                        // Accepts data in specific format given by the client "DATA<subject>-_-_-<body>CRLF.CRLF" as described in the client comments
                        // Splits the message to subject and body
                        String[] arrayOfData = mail_data.split("-_-_-");

                        // Adds subject and body to messageContainer in order to be saved in the mailbox
                        messageContainer.subject_container = "Subject:\n" + arrayOfData[0];
                        messageContainer.mail_body_container = "Body:\n" + arrayOfData[1];

                        System.out.println(mail_data);// _buffer.toString());
                        SUCCESS_STATE = true;
                        GO_ON_CHECKS = false;
                        mail_data_buffer = null;
                        forward_path_buffer = null;
                        reverse_path_buffer = null;   
                        CommandStack = "HELO ";
                        
                        System.out.println("Successful Transfer.");
                    }   //if else in order
                }       //if (clientMSG.toUpperCase().contains("DATA")
            /////////////////////////////////////////////////////////////////
            // end of controls - empty buffer for nextinput
            /////////////////////////////////////////////////////////////////                
                clientMSG = "";      //empty buffer after CRLF   
            }         //if CRLF

            sm.output.writeUTF(sResponceToClient);
        }
        
        catch (Exception except){
            //Exception thrown (except) when something went wrong, pushing message to the console
            System.out.println("Error --> " + except.getMessage());
        }        
    }
}






