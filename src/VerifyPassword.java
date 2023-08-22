import org.jasypt.util.password.StrongPasswordEncryptor;

public class VerifyPassword {

	/*
	 * After you update the passwords in customers table,
	 *   you can use this program as an example to verify the password.
	 *   
	 * Verify the password is simple:
	 * success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
	 * 
	 * Note that you need to use the same StrongPasswordEncryptor when encrypting the passwords
	 * 
	 */

	public static boolean verifyCredentials(String email, String password, String encryptedPassword) {
		boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
		System.out.println("verify " + email + " - " + password);
		return success;
	}

}
