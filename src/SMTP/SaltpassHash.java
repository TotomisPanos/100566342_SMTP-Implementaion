package SMTP;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SaltpassHash {
    public static final String SALT = "my-salt-text";
    String passTOhash;

    public SaltpassHash(String plainPassword){
        passTOhash = plainPassword;
    }
    
    public static void main(String args[]) {
	SaltpassHash sph = new SaltpassHash("test");
        sph.start();
    }

    public String start() {
	//System.out.println("1 "+passTOhash);
        String saltedPassword = SALT + passTOhash;
	//System.out.println("2 "+saltedPassword);
	String hashedPassword = generateSaltHash(saltedPassword);
	System.out.println("3 "+hashedPassword);
        return hashedPassword;
    }

        public static String generateSaltHash(String input) {
            StringBuilder localHash = new StringBuilder();
            try {
            	MessageDigest sha = MessageDigest.getInstance("SHA-1");
		byte[] hashedBytes = sha.digest(input.getBytes());
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < hashedBytes.length; ++idx) {
                    byte b = hashedBytes[idx];
                    localHash.append(digits[(b & 0xf0) >> 4]);
		localHash.append(digits[b & 0x0f]);
		}
            } catch (NoSuchAlgorithmException e) {
		// handle error here.
            }
            return localHash.toString();
	}

	public Boolean login(String username, String password) {
		Boolean isAuthenticated = false;

		// remember to use the same SALT value use used while storing password
		// for the first time.
		String saltedPassword = SALT + password;
		String hashedPassword = generateSaltHash(saltedPassword);

		if(hashedPassword.equals("")){
			isAuthenticated = true;
		}else{
			isAuthenticated = false;
		}
		return isAuthenticated;
	}


    
}
