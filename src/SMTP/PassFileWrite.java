package SMTP;

import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PassFileWrite {
    static String file_path = "PassFile\\";
    static String file_name = "passfile.txt";
    String where_to_write = file_path + file_name;
        
    public void ToFile(String username, String password){
         try {
            // true at the end of FIleOutputSteream method allows appening
            FileOutputStream fos = new FileOutputStream(where_to_write, true);
            //NOTE: /r/n   used to force one record per line
            String what_to_save = "\r\n"+username+ " "+password; 
            byte[] strToBytes = what_to_save.getBytes();
            fos.write(strToBytes);            
            fos.close();
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(PassFileWrite.class.getName()).log(Level.SEVERE, null, ex);
            } 
        catch (IOException except){
            //Exception thrown (except) when something went wrong, pushing clientMSG to the console
            System.out.println("Error in File Handler --> " + except.getMessage());
        }       
    }    

    public void InitFile(){
         try {
            // false at the end of FIleOutputSteream method is for overwrite
            FileOutputStream fos = new FileOutputStream(where_to_write, false);
            String what_to_save = ""; 
            byte[] strToBytes = what_to_save.getBytes();
            fos.write(strToBytes);            
            fos.close();
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(PassFileWrite.class.getName()).log(Level.SEVERE, null, ex);
            } 
        catch (IOException except){
            //Exception thrown (except) when something went wrong, pushing clientMSG to the console
            System.out.println("Error in File Handler --> " + except.getMessage());
        }       
    }  

    public void DeleteFile(){
        File file = new File(where_to_write); //Exception thrown (except) when something went wrong, pushing clientMSG to the console
        if(file.delete()){
            System.out.println("File deleted successfully");
        }
        else         {
            System.out.println("Failed to delete the file");
        }       
    }  

    
    public static void main(String[] args){ 
        System.out.println("PASSWORD FILE HANDLER");
        System.out.println("1 initilaise file - erase all records");
        System.out.println("2 append file - insert new record");
        System.out.println("3 delete file - ONLY INTEGERS");
        PassFileWrite pfw = new PassFileWrite();
        
        Scanner ObjScan = new Scanner(System.in);
        Integer choice = ObjScan.nextInt();          
        switch(choice)
        {
            case 1:
                System.out.print(" selected 1 = file initialised");
                pfw.InitFile();
                break;
            case 2:
                System.out.println ("Enter User Name: ");
                String uname = ObjScan.next();
                System.out.println ("Enter User Password:");
                String upass = ObjScan.next();                
                pfw.ToFile(uname,upass);
                System.out.print(" selected 2 = file appended with new user");
                break;
            case 3:
                pfw.DeleteFile();
                System.out.print(" selected 3 = file deleted");
                break;
            default:
                System.out.print(" non recognised selection");
                break;
        }
    }
}