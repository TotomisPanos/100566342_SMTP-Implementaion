package SMTP;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

class ValidationHandler
{
    // Using Regex to validate if an email has the correct format
    public static boolean isValidEmail(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                            "[a-zA-Z0-9_+&*-]+)*@" +
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                            "A-Z]{2,7}$";
                              
        Pattern pat = Pattern.compile(emailRegex);
        
        // Returns if valid or not
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
    
    // Using Regex to validate the username
    public static boolean isValidUsername(String username)
    {
  
        // Regex to check valid username.
        String usernameRegex = "^[A-Za-z]\\w{5,29}$";
  
        // Compile the ReGex
        Pattern pat = Pattern.compile(usernameRegex);
  
        // If the username is empty
        // return false
        if (username == null) {
            return false;
        }
  
        // Pattern class contains matcher() method
        // to find matching between given username
        // and regular expression.
        Matcher m = pat.matcher(username);
  
        // Return if the username
        // matched the ReGex
        return m.matches();
        
        //Validation Specifications
        //The username consists of 6 to 30 characters inclusive.
        //The username can only contain alphanumeric characters and underscores (_).
        //The first character of the username must be an alphabetic character.
    }
    
    // Function to validate the password.
    public static boolean isValidPassword(String password)
    {
  
        // Regex to check valid password.
        String regex = "^(?=.*[0-9])"
                       + "(?=.*[a-z])(?=.*[A-Z])"
                       + "(?=.*[@#$%^&+=])"
                       + "(?=\\S+$).{8,20}$";
  
        // Compile the ReGex
        Pattern p = Pattern.compile(regex);
  
        // If the password is empty
        // return false
        if (password == null) {
            return false;
        }
  
        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher m = p.matcher(password);
  
        // Return if the password
        // matched the ReGex
        return m.matches();
        
        //Validation Specifications
        //It contains at least 8 characters and at most 20 characters.
        //It contains at least one digit.
        //It contains at least one upper case alphabet.
        //It contains at least one lower case alphabet.
        //It contains at least one special character which includes !@#$%&*()-+=^.
        //It doesn’t contain any white space.
    }
}