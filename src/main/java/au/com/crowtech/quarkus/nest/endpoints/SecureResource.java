package au.com.crowtech.quarkus.nest.endpoints;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import au.com.crowtech.quarkus.nest.models.GennyToken;
import au.com.crowtech.quarkus.nest.models.user.KeycloakUser;

public abstract class SecureResource extends GenericResource {

	public static final Response RESPONSE_LOCKOUT = Response.status(Status.FORBIDDEN).entity("Not Authorised").build();
	public static final ResponseBuilder RESPONSE_NOT_FOUND = Response.status(Status.NOT_FOUND);
	public static final ResponseBuilder RESPONSE_OK = Response.status(Status.OK);

	@ConfigProperty(name = "nest.debug.user.uuid", defaultValue = "")
	public static String DEBUG_UUID;
	
	public static KeycloakUser DEBUG_USER;
	
	@Inject
	JsonWebToken token;

	@Context
	private SecurityContext securityContext; 
	
	public String getRawToken() {
		if(token != null && !"".equals(token.getRawToken().strip())) {
			return token.getRawToken();
		}
		genLog.warn("Failed to get Token through JsonWebToken. Using SecurityContext workaround");
		if(securityContext != null) {
			String token = securityContext.getUserPrincipal().getName();
			genLog.info("Found token through SecurityContext: " + token);
			return token;
		}

		return null;
	}

	/**
	 * Return a {@link KeycloakUser} based on the provided {@link JsonWebToken}.
	 * Throws {@link WebApplicationException} if there are supplied roles to check against
	 * and AUser related to token UUID does not have at least one of the specified roles
	 * 
	 * Returns AUser otherwise
	 * @param roles - roles to check against (or blank if none)
	 * @return AUser object matching the UUID in the jsonToken (if allowed)
	 * @throws WebApplicationException - if roles are not blank and user doesn't match at least one of supplied roles
	 */
	protected KeycloakUser getAuthenticatedUser(String... roles) throws WebApplicationException {
		String tokenString = getRawToken();
		if (StringUtils.isBlank(tokenString)) {
			throw new WebApplicationException("Token is Null or Empty", Status.FORBIDDEN);
		}
		
		GennyToken userToken = new GennyToken(tokenString);

		if (roles.length != 0 && !userToken.hasRole(roles)) {
			KeycloakUser user = this.getAuthenticatedUser();
			info("User: " + user.getFullName() + " missing one of the following roles: " + arrToString(roles));
			throw new WebApplicationException("Access denied", Status.FORBIDDEN);
		}

		String uuid = userToken.getUuid();
		KeycloakUser user = KeycloakUser.findByUuid(uuid);
		
		info("Looking for user with uuid: "+uuid);
		if (user == null) {
			info("User on keycloak but not on database, or uuid is dodgy");
			info("Uuid: " + uuid);
			error("NOT FOUND!");
			throw new WebApplicationException("User not recognised. Access denied", Status.FORBIDDEN);
		}
		info("FOUND USER!");
		return user;
	}
	
	private String arrToString(String[] roles) {
		String string = "";
		for(int i = 0; i < roles.length - 1; i++) {
			string += roles + ",";
		}
		string += roles;
		
		return string;
	}

	public String getTokenUuid() {
		return getGennyToken().getUuid();
	}
	
	public GennyToken getGennyToken() {
		return new GennyToken(getRawToken());
	}
	
	protected boolean checkTokenRole(String... roles) {
		GennyToken genny = this.getGennyToken();
		return genny.hasRole(roles);
	}
}
