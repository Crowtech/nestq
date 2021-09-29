package au.com.crowtech.quarkus.nest.keycloak.exception;

import javax.ws.rs.core.Response.Status;

public class BadUserException extends Exception {

	public Status status;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8850654811698452238L;

	public BadUserException(Status status, String message) {
		super("Bad User: " + message + ". HTTP Code: " + status.getStatusCode());
		this.status = status;
	}
	
}
