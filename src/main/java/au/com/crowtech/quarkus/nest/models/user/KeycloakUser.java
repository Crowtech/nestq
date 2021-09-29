package au.com.crowtech.quarkus.nest.models.user;

import java.lang.invoke.MethodHandles;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.jboss.logging.Logger;

import au.com.crowtech.quarkus.nest.adapters.EmailAdapter;
import au.com.crowtech.quarkus.nest.models.Email;
import au.com.crowtech.quarkus.nest.models.nest.TrackedNestModel;

/**
 * Mapped Superclass containing key components needed for a keycloak user.<br>
 * Inherit as necessary.<br>
 * 
 * Not Null:<br>
 * - {@link KeycloakUser#firstname}<br>
 * - {@link KeycloakUser#middlename}<br>
 * - {@link KeycloakUser#lastname}<br>
 * @author bryn
 *
 */
@MappedSuperclass
public abstract class KeycloakUser extends TrackedNestModel {
	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@Column(nullable = false, unique = true)
	public String uuid;
	
	@NotNull
	@Column(nullable = false, unique = true)
	public String username;
	
	@NotNull
	@Column(nullable = false)
	public String firstname;
	
	public String middlename;
	
	@NotNull
	@Column(nullable = false)
	public String lastname;
	
	@NotNull
	@JsonbTypeAdapter(EmailAdapter.class)
	@Column(nullable = false, unique = true)
	public Email email;
	
	public KeycloakUser() {
		
	}
	
	public KeycloakUser(String firstname, String middlename, String lastname, String email) throws Exception {
		this.email = new Email(email);
		this.firstname = firstname;
		this.middlename = middlename;
		this.lastname = lastname;
	}
	
	// FINDING METHODS ==================================================

	public static KeycloakUser findByUuid(final String uuid) {
		return KeycloakUser.find("uuid", uuid).firstResult();
	}
	
	public static KeycloakUser findByEmail(String email) {
		return KeycloakUser.find("email.name", email).firstResult();
	}	
	
	// GETTERS AND SETTERS ==============================================
	
	/**
	 * Get the User's full name
	 * @return
	 */
	public String getFullName() {
		if(middlename == null)
			middlename = "";
		else middlename = " " + middlename;
		return firstname + middlename + " " + lastname;
	}

}
