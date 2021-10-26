package au.com.crowtech.quarkus.nest.keycloak;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.http.HttpResponse;

import javax.enterprise.context.SessionScoped;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import au.com.crowtech.quarkus.nest.keycloak.exception.BadUserException;
import au.com.crowtech.quarkus.nest.models.GennyToken;
import au.com.crowtech.quarkus.nest.models.user.KeycloakUser;
import au.com.crowtech.quarkus.nest.utils.KeycloakUtils;

@SessionScoped
public class KeycloakAdminUtils {
	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	
	public static GennyToken getAdminToken(String keycloakUrl, String clientSecret, String pass) {
		GennyToken adminToken = null;
		try {
			String adminTokenStr = KeycloakUtils.getAccessToken(keycloakUrl, "master", "admin-cli",
					clientSecret, "admin", pass);
			log.info(adminTokenStr);
			adminToken = new GennyToken(adminTokenStr);
		} catch (IOException e1) {
			log.warn("failed to get admin genny token");
			e1.printStackTrace();
		}

		return adminToken;
	}
	
	/**
	 * 
	 * @param keycloakUrl
	 * @param adminToken
	 * @param user
	 * @param password
	 * @param roles
	 * @param grouproles
	 * @return
	 */
	public static KeycloakUser registerKeycloakUser(String keycloakUrl, String projectRealm, GennyToken adminToken, KeycloakUser user, 
			String password, String roles, String grouproles) throws BadUserException {

		// Assign default value
		if (StringUtils.isBlank(grouproles))
			grouproles = "users";
	
		if (user.username == null) {
			throw new BadUserException(Status.EXPECTATION_FAILED, "Username of user is null");
		}

		KeycloakUser existing = KeycloakUser.findByEmail(user.email.getName());
		log.info("Existing: " + (existing != null));
		if (existing != null) {
			throw new BadUserException(Status.CONFLICT, "User already exists with email: " + user.email.getName());
		}

		if (StringUtils.isBlank(grouproles)) {
			grouproles = "users";
		}

		log.info("Creating new Keycloak User " + user);
		String uuid = null;

		try {
			uuid = KeycloakUtils.createUser(keycloakUrl, adminToken.getToken(), projectRealm, user.username,
					user.firstname, user.lastname, user.email.getName(), password, roles, grouproles);
			/*
			 * uuid = KeycloakUtils.createUser(keycloakUrl, adminToken.getToken(),
			 * "pantatransport", user.username, user.firstname, user.lastname,
			 * user.email.getName(), password);
			 */
			user.uuid = uuid;
		} catch (IOException e2) {
			e2.printStackTrace();
			return null;
		}
		
		// Assuming everything is chill with the UUID
		String[] roleNames = roles.split(",");
		HttpResponse<String> response = KeycloakUtils.setUserRoles(keycloakUrl, projectRealm, adminToken, uuid,
				roleNames);
		
		log.info("Response Status Code: " + response.statusCode());
		if(response.statusCode() != Status.NO_CONTENT.getStatusCode())
			log.info("Response: " + response.body());

		/*
		 * else if (roles.contains("operator")) { Operator operator =
		 * Operator.findByUser(user); if (operator == null) {
		 * log.info("Generating new Operator profile"); } else {
		 * log.info("Preexisting Operator profile detected: " + operator); } }
		 */
		
		return user;
	}
}
