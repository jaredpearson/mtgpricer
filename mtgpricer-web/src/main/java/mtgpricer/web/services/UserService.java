package mtgpricer.web.services;

public interface UserService {
	
	/**
	 * Changes the password of the user from the current value to the new value. The current password
	 * must the current password of the user or the change fails.
	 * 
	 * @param username the user name of the user
	 * @param currentPassword the current password
	 * @param newPassword the new password
	 * @param confirmPassword this should be the same as the new password
	 * @return the result of the password change
	 */
	public PasswordChangeResult changePassword(String username, String currentPassword, String newPassword, String confirmPassword);
	
}
