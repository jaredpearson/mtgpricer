package mtgpricer.web.services;

public class PasswordChangeResult {
	private final boolean success;
	private final String message;
	
	public PasswordChangeResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	/**
	 * Determines if changing the password was successful.
	 */
	public boolean isSuccessful() {
		return success;
	}
	
	/**
	 * Gets the message that can be displayed to the user.
	 */
	public String getMessage() {
		return message;
	}
}