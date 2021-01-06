package au.com.crowtech.quarkus.nest.endpoints;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import au.com.crowtech.quarkus.nest.models.GennyToken;
import au.com.crowtech.quarkus.nest.models.NestUser;
import au.com.crowtech.quarkus.nest.utils.KeycloakUtils;
import au.com.crowtech.quarkus.nest.utils.NestResourceClient;




@Path("/v1/api/nest")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HelloResource {

	private static final Logger log = Logger.getLogger(HelloResource.class);

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

	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello";
    }
    
    @Path("/users")
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
			user = newKeycloakUser(baseKeycloakUrl, adminToken, user, password, roles, grouproles);
		} catch (WebApplicationException e) {
			return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
		}

		return Response.status(Status.OK).entity(user).build();
	}
    
    
	private NestUser newKeycloakUser(String keycloakUrl, GennyToken adminToken, NestUser user, String password, String roles,
			String grouproles) throws WebApplicationException {

		if (StringUtils.isBlank(roles)) {
			roles = "user";
		}

		if (StringUtils.isBlank(password)) {
			password = UUID.randomUUID().toString().substring(0, 12);
		}

		
		if (StringUtils.isBlank(grouproles)) {
			grouproles = "users";
		}

		log.info("Creating new Keycloak NestUser " + user);
		String uuid = null;

		String data = "{ " + "\"username\" : \"" + user.username.toLowerCase() + "\"," + "\"email\" : \""
				+ user.email.getName() + "\" , " + "\"enabled\" : true, " + "\"emailVerified\" : false, "
				+ "\"firstName\" : \"" + user.firstname + "\", " + "\"lastName\" : \"" + user.lastname
				+ "\",\"realmRoles\" : [\"" + roles + "\"]}";

		

		log.info("CreateUserjsonDummy="+data);


			HttpResponse<String> response = null;
			
			try {
				response = nestResourceClient
						.post(keycloakUrl + "/auth/admin/realms/" + projectRealm + "/users", adminToken, data);
				if (response.statusCode()>201) {
					throw new WebApplicationException(response.body(),
							response.statusCode());
				}

			} catch (Exception e) {
				throw new WebApplicationException(response.body(),
						response.statusCode());
			}
			Optional<String> locationUrl = response.headers().firstValue("location");
			if (locationUrl.isPresent()) {
				uuid = StringUtils.substringAfterLast(locationUrl.get(), "/");
				log.info("Created keycloak user with uuid " + uuid);
			} else {
				log.error("Could not create user");
				throw new WebApplicationException("Cannot create user, error in keycloak server",
						Status.INTERNAL_SERVER_ERROR);
			}
		

		if (uuid == null) {
			throw new WebApplicationException("Cannot create user, error in server", Status.INTERNAL_SERVER_ERROR);
		}

		Map<String, String> roleMap = getKeycloakRoles(baseKeycloakUrl, projectRealm, adminToken.getToken());

		// Add roles
		try {

			data = "[";
			String[] roleNames = roles.split(",");
			for (String rolename : roleNames) {
				String roleid = roleMap.get(rolename);
				if ((roleid != null) && (!"admin".equals(rolename))) {
					data += "{ \"id\": \"" + roleid + "\",\"name\":\"" + rolename + "\" },";
				}
			}
			if (StringUtils.endsWith(data, ",")) {
				data = data.substring(0, data.length() - 1); // remove last comma
			}
			data += "]";

			log.info("Setting user roles");
			response = nestResourceClient.post(
					baseKeycloakUrl + "/auth/admin/realms/" + projectRealm + "/users/" + uuid + "/role-mappings/realm",
					adminToken, data);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			throw new WebApplicationException("Cannot create user, error in keycloak server",
					Status.INTERNAL_SERVER_ERROR);
		}

		try {
			KeycloakUtils.setPassword(keycloakUrl, adminToken.getToken(), projectRealm, uuid, password);
			log.info("Saving user to database");
			// Now create the user in the database
			user.uuid = uuid;
			user.name = user.firstname + " " + user.lastname;
			user.name = user.name.trim();
			
			user.persist();

			log.info("NestUser created! " + user + " with roles " + roles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return user;
	}

	private void setKeycloakPassword(GennyToken gToken, final String uuid, final String password) {
		String data = "{ " + "\"type\" : \"password\"," + "\"temporary\" : false , " + "\"value\" :\"" + password
				+ "\"}";

		try {
			HttpResponse<String> response = nestResourceClient.put(
					baseKeycloakUrl + "/auth/admin/realms/" + gToken.getRealm() + "/users/" + uuid + "/reset-password",
					gToken, data);
			log.info(response.statusCode() + ": password set");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private Map<String, String> getKeycloakRoles(final String baseKeycloakUrl, final String projectRealm,
			final String token) {
		Map<String, String> roles = new HashMap<String, String>();

		HttpClient client = HttpClient.newBuilder().build();

		URI uri = UriBuilder.fromPath(baseKeycloakUrl + "/auth/admin/realms/" + projectRealm + "/roles").build();

		System.out.println(uri.toString());
		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().version(HttpClient.Version.HTTP_2)
				.header("Authorization", "Bearer " + token).build();

		try {
			HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

			System.out.println(response.body());
			Jsonb jsonb = JsonbBuilder.create();
			List<JsonObject> serializedList = jsonb.fromJson((String) response.body(), new ArrayList<JsonObject>() {
			}.getClass().getGenericSuperclass());

			for (JsonObject jsonObject : serializedList) {
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
				roles.put(name, id);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return roles;
	}
    
}