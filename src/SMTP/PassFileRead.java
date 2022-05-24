package SMTP;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class PassFileRead {
    static String file_path = "PassFile\\";
    static String file_name = "passfile.txt";
    String where_to_read = file_path + file_name;

    public void FromFile() throws IOException{
        try {
            File myObj = new File(where_to_read);
            Scanner myReader = new Scanner(myObj);
            System.out.println("List of registered users");
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println(data);
            }
            
            myReader.close();
            
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
   }     

    public String HandleData(String dataIn, String usernameIn){
        int point = dataIn.indexOf(" ");      //separate uname from passw
        String uname = dataIn.subSequence(0, point).toString();
        String upass = dataIn.subSequence(point+1,dataIn.length()).toString();
        
        if(uname.equals(usernameIn)) //compare found with given name
        {
            System.out.println("found "+ usernameIn);// ERR HANDLING  
            
            SaltpassHash sph = new SaltpassHash(upass);
            
            return sph.start();  //PLAIN upass;
        }
        else {
            System.out.println("found not "+usernameIn);// ERR HANDLING 
            return "";
       }        
    }
   
    
     public String SearchFile(String username) throws IOException{
        String result=null;

        System.out.println("searching "+ username);
        try {
            File myObj = new File(where_to_read);
            Scanner myReader = new Scanner(myObj);
            ArrayList<String> list = new ArrayList<String>();
            
            long lines = Files.lines(Paths.get(where_to_read)).count();
            
            for (int i = 0; i < lines; i++)
            {
                String data = myReader.nextLine();  //read lines from file
                
                if (data.startsWith(username)){
                    result = HandleData(data, username);
                }
            }
            
            myReader.close();              
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
        return result;
    }
     
    public static void main(String[] args) throws IOException {
        //presentation of code to use method
        PassFileRead fr = new PassFileRead();
        fr.FromFile();
        fr.SearchFile("adam");
    }
}
