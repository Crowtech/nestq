package au.com.crowtech.quarkus.nest.endpoints;

import java.io.IOException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import au.com.crowtech.quarkus.nest.models.GennyToken;
import au.com.crowtech.quarkus.nest.models.NestUser;
import au.com.crowtech.quarkus.nest.utils.KeycloakUtils;
import au.com.crowtech.quarkus.nest.utils.NestResourceClient;




@Path("/v1/api/nest/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	private static final Logger log = Logger.getLogger(UserResource.class);

	@ConfigProperty(name = "keycloak.url", defaultValue = "http://keycloak:8180")
	String baseKeycloakUrl;

	@ConfigProperty(name = "keycloak.admin.password", defaultValue = "admin")
	String keycloakAdminPassword;

	@ConfigProperty(name = "keycloak.admin.realm", defaultValue = "crowtech")
	String projectRealm;

	@ConfigProperty(name = "quarkus.oidc.auth-server-url", defaultValue = "http://localhost:8180")
	String keycloakUrl;

	@ConfigProperty(name = "keycloak.admin-cli.clientid", defaultValue = "public")
	String admincli_clientid;
	
	
	@Inject
	NestResourceClient nestResourceClient;
	
	@Transactional
	@POST
	public Response newKeycloakUser(@Context UriInfo uriInfo, @Valid NestUser user,
			@QueryParam(value = "password") String password, @QueryParam(value = "roles") String roles,
			@QueryParam(value = "grouproles") String grouproles) {

		if (user == null) {
			throw new WebApplicationException("Cannot create user as it is null", Status.EXPECTATION_FAILED);
		}

		GennyToken adminToken = null;
		try {
			String adminTokenStr = KeycloakUtils.getAccessToken(baseKeycloakUrl, "master", "admin-cli",
					null, "admin", keycloakAdminPassword);
			adminToken = new GennyToken(adminTokenStr);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (adminToken.getRealm() == null) {
			throw new WebApplicationException("Cannot create user, error in server", Status.INTERNAL_SERVER_ERROR);
		}

		if (StringUtils.isBlank(grouproles)) {
			grouproles = "users";
		}

		try {
			String uuid = KeycloakUtils.createUser(baseKeycloakUrl, adminToken.getToken(), projectRealm, user.username,
					user.firstname, user.lastname, user.email.getName(), password,roles, grouproles);
			user.uuid = uuid;
			user.name = user.firstname + " " + user.lastname;
			user.persist();
		} catch (WebApplicationException e) {
			return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
		} catch (IOException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}

		return Response.status(Status.OK).entity(user).build();
	}
    
    

    
}