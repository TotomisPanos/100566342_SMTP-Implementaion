package SMTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class mailFileHandler {

    static String file_path = "Mailboxes\\";
    static String file_name = ".txt";
    String mailUserName = "";
    mailMessageContainer mailContent;
    String where_to_write = file_path;// + file_name;
    String CRLF = "\r\n";

    // Constructor used for creating a mailbox
    public mailFileHandler(String uname)
    {
        mailUserName = uname;
        file_path = file_path + mailUserName;
    }
    
    // Constructor user for handling mail data
    public mailFileHandler(String uname, mailMessageContainer email) {
        mailUserName = uname;
        mailContent = email;
        file_path = file_path + mailUserName + file_name;
    }

    public void CreateMailbox() throws IOException {
        boolean result = false;

        // Checks if the mailbox exists
        File theDir = new File(file_path);
        if (!theDir.exists()) {
            System.out.println("Creating directory: " + theDir.getName());
            result = false;
            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {}
        }
        if (result) {
            System.out.println("Directory created");
        }
    }

    public void HandleMail() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss");
        LocalDateTime now = LocalDateTime.now();
        String nowString = dtf.format(now);

        File file = new File("Mailboxes\\" + mailUserName + "\\" + nowString + ".txt");
        if (file.createNewFile())
        {
            System.out.println("file created");
        }
        else 
        {
            System.out.println("File already exists.");
        }

        FileOutputStream fos = new FileOutputStream("Mailboxes\\" + mailUserName + "\\" + nowString + ".txt", true);

        
        String data = "";
        
        // Write in file
        data += mailContent.date_container + CRLF;
        data += mailContent.from_container;
        data += mailContent.to_container + CRLF;
        data += mailContent.subject_container + CRLF;
        data += mailContent.mail_body_container + CRLF;

        byte[] strToBytes = data.getBytes();
        fos.write(strToBytes);
        fos.close();

    }

    public static void main(String[] args) throws IOException {
        mailMessageContainer test = new mailMessageContainer("test date", "test from", "test to", "test subject", "test body");
        mailFileHandler self = new mailFileHandler("testMailbox", test);
        self.CreateMailbox();
    }
}
