package au.com.crowtech.quarkus.nest.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.json.JsonException;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.crowtech.quarkus.nest.models.GennyToken;
import io.vertx.core.json.JsonObject;

public class KeycloakUtils {
	private static final Logger log = Logger.getLogger(KeycloakUtils.class);
	private static Integer ACCESS_TOKEN_EXPIRY_LIMIT_SECONDS = 3600;
	private static Integer MAX_USERS_FETCHED = 100000;

	public static String getAccessToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {

		try {

			JsonObject content = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password);
			if (content != null) {
				return content.getString("access_token");
			}

			return null;
		} catch (Exception e) {
			log.error("Cannot get Token for Username " + username + " for realm " + realm + " on " + keycloakUrl
					+ " and clientId " + clientId);
		}
		return null;
	}

	public static AccessTokenResponse getAccessTokenResponse(String keycloakUrl, String realm, String clientId,
			String secret, String username, String password) throws IOException {

		JsonObject content = KeycloakUtils.getSecureTokenPayload(keycloakUrl, realm, clientId, secret, username,
				password);
		if (content != null) {
			JsonbConfig config = new JsonbConfig().withNullValues(false).withFormatting(true);

			Jsonb jsonb = JsonbBuilder.create(config);

			return jsonb.fromJson(content.toString(), AccessTokenResponse.class);
		}

		return null;
	}

	public static JsonObject getSecureTokenPayload(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password, String refreshToken) throws IOException {

		JsonObject fullTokenPayload = KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password,
				refreshToken);
		JsonObject secureTokenPayload = new JsonObject();
		secureTokenPayload.put("access_token", fullTokenPayload.getString("access_token"));
		secureTokenPayload.put("refresh_token", fullTokenPayload.getString("refresh_token"));
		return secureTokenPayload;
	}

	public static JsonObject getSecureTokenPayload(String keycloakUrl, String realm, String clientId, String secret,
			String username, String password) throws IOException {
		return KeycloakUtils.getSecureTokenPayload(keycloakUrl, realm, clientId, secret, username, password, null);
	}

	public static JsonObject getToken(String keycloakUrl, String realm, String clientId, String secret, String username,
			String password, String refreshToken) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {

			URI uri = new URI(keycloakUrl + "/auth/realms/" + realm + "/protocol/openid-connect/token");
			HttpPost post = new HttpPost(uri);

			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> formParams = new ArrayList<NameValuePair>();

			log.info("===================== Generating new token (KeycloakUtils) =====================");

			/* if we have a refresh token */
			if (refreshToken != null) {

				/* we decode it */
				JsonObject decodedServiceToken = KeycloakUtils.getDecodedToken(refreshToken);

				/* we get the expiry timestamp */
				long expiryTime = decodedServiceToken.getLong("exp");

				/* we get the current time */
				long nowTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();

				/* we calculate the differencr */
				long duration = expiryTime - nowTime;

				/* if we don't have a refresh token, we generate a new token using username and password */
				if(refreshToken == null) {
					formParams.add(new BasicNameValuePair("username", username));
					formParams.add(new BasicNameValuePair("password", password));
					log.info("using username: " + username);
					formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
				}
				else {
					formParams.add(new BasicNameValuePair("refresh_token", refreshToken));
					formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "refresh_token"));
					log.info("using refresh token");
					log.info(refreshToken);
				}
				/*
				 * if the difference is negative it means the expiry time is less than the
				 * nowTime if the difference < 180000, it means the token will expire in 3 hours
				 */
				if (duration <= ACCESS_TOKEN_EXPIRY_LIMIT_SECONDS) {

					/* if the refresh token is about to expire, we must re-generate a new one */
					refreshToken = null;
				}
			}

			/*
			 * if we don't have a refresh token, we generate a new token using username and
			 * password
			 */
			if (refreshToken == null) {
				formParams.add(new BasicNameValuePair("username", username));
				formParams.add(new BasicNameValuePair("password", password));
				log.info("using username");
				formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
			} else {
				formParams.add(new BasicNameValuePair("refresh_token", refreshToken));
				formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "refresh_token"));
				log.info("using refresh token");
				log.info(refreshToken);
			}

			formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, clientId));
			if (secret != null) {
				formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_SECRET, secret));
			}
			UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");

			post.setEntity(form);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode != 200) {
				content = getContent(entity);
				throw new IOException("" + statusCode);
			}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
			}

			try {

				JsonObject obj = new JsonObject(content);
				return obj;
			} catch (Exception e) {

			}

		} catch (URISyntaxException e) {

			httpClient.getConnectionManager().shutdown();
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return null;
	}

	public static JsonObject getToken(String keycloakUrl, String realm, String clientId, String secret, String username,
			String password) throws IOException {
		return KeycloakUtils.getToken(keycloakUrl, realm, clientId, secret, username, password, null);
	}

	public static String getContent(final HttpEntity httpEntity) throws IOException {
		if (httpEntity == null)
			return null;
		final InputStream is = httpEntity.getContent();
		try {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			int c;
			while ((c = is.read()) != -1) {
				os.write(c);
			}
			final byte[] bytes = os.toByteArray();
			final String data = new String(bytes);
			return data;
		} finally {
			try {
				is.close();
			} catch (final IOException ignored) {

			}
		}

	}

	// Decode the keycloak token string and send back in Json Format
	public static JsonObject getDecodedToken(final String bearerToken) {
		JsonObject jsonObj = null;
		String decodedJson = null;
		try {
			final String[] jwtToken = bearerToken.split("\\.");
			final Base64 decoder = new Base64(true);
			final byte[] decodedClaims = decoder.decode(jwtToken[1]);
			decodedJson = new String(decodedClaims);
			jsonObj = new JsonObject(decodedJson);
		} catch (final JsonException e1) {
			log.info("bearerToken=" + bearerToken + "  decodedJson=" + decodedJson + ":" + e1.getMessage());
		}
		return jsonObj;
	}

	public static String getRealmFromToken(final String bearerToken) {
		return getDecodedToken(bearerToken).getString("azp"); // return the realm

	}

	// Send the decoded Json token in the map
	public static Map<String, Object> getJsonMap(final String json) {
		final JsonObject jsonObj = getDecodedToken(json);
		return getJsonMap(jsonObj);
	}

	public static Map<String, Object> getJsonMap(final JsonObject jsonObj) {
		final String json = jsonObj.toString();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};

			map = mapper.readValue(json, typeRef);

		} catch (final JsonGenerationException e) {
			e.printStackTrace();
		} catch (final JsonMappingException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	/*
	 * Return the Set of access roles of the user logged in based on the keycloak
	 * access_roles
	 */
	public static HashSet<String> getRoleSet(final String role) {
		String accessRole = role.substring(role.indexOf("[") + 1, role.indexOf("]"));
		String[] strs = accessRole.trim().split("\\s*,\\s*");
		HashSet<String> roles = new HashSet<String>(Arrays.asList(strs));
		return roles;

	}

	/*
	 * For testing user with different realm name in dev env as dev/genny keycloak
	 * can't be added with other realms Gets the Project realm from Genny Env if it
	 * is present
	 */
	static String projectRealm = null;

	public static String getPRJRealmFromDevEnv() {
		if (System.getenv("PROJECT_REALM") != null) {
			projectRealm = System.getenv("PROJECT_REALM");
		}
		return projectRealm;
	}

	@SuppressWarnings("deprecation")
	public static String resetUserPassword(String keycloakUrl, String userId, String token, String realm)
			throws ClientProtocolException, IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPut putRequest = new HttpPut(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");
			log.info(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/reset-password");

			putRequest.addHeader("Content-Type", "application/json");
			putRequest.addHeader("Authorization", "Bearer " + token);

			HttpResponse response = httpClient.execute(putRequest);

			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				return content;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				return content;
			} else if (statusCode == 409) {
				throw new IOException("Already exists");
			}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
				throw new IOException(response + "");
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static String createUser(String keycloakUrl, String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail) throws IOException {
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";
		String password = UUID.randomUUID().toString().substring(0, 10);
		return createUser(keycloakUrl, token, realm, newUsername, newFirstname, newLastname, newEmail, password,
				newRealmRoles, newGroupRoles);
	}

	public static String createUser(String keycloakUrl, String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password) throws IOException {
		String newRealmRoles = "user,offline_access,uma_authorization";
		String newGroupRoles = "/users";

		return createUser(keycloakUrl, token, realm, newUsername, newFirstname, newLastname, newEmail, password,
				newRealmRoles, newGroupRoles);
	}

	// This is the one called from rules to create a keycloak user
	public static String createUser(String keycloakUrl, String token, String realm, String newUsername,
			String newFirstname, String newLastname, String newEmail, String password, String newRealmRoles,
			String newGroupRoles) throws IOException {
		return createUser(keycloakUrl, null, token, realm, newUsername, newFirstname, newLastname, newEmail, password,
				newRealmRoles, newGroupRoles);
	}

	// This is the one called from rules to create a keycloak user
	public static String createDummyUser(String keycloakUrl, String token, String realm) throws IOException {

		String randomCode = UUID.randomUUID().toString().substring(0, 18);
		String randomPassword = UUID.randomUUID().toString().substring(0, 18);
		String json = "{ " + "\"username\" : \"" + randomCode + "\"," + "\"email\" : \"" + randomCode
				+ "@gmail.com\" , " + "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \""
				+ randomCode + "\", " + "\"lastName\" : \"" + randomCode + "\", " + "\"groups\" : [" + " \"users\" "
				+ "]," + "\"realmRoles\" : [\"user\"],\"credentials\": [{" + "\"type\":\"password\"," + "\"value\":\""
				+ randomPassword + "\"," + "\"temporary\":true }]}";

		log.info("CreateUserjsonDummy=" + json);

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
		try {
			HttpPost post = new HttpPost(keycloakUrl + "/auth/admin/realms/" + realm + "/users");
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				String keycloakUserId = getKeycloakUserId(keycloakUrl, token, realm, randomCode);
				log.info("Keycloak NestUser ID: " + keycloakUserId);
				return keycloakUserId;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				String keycloakUserId = getKeycloakUserId(keycloakUrl, token, realm, randomCode);
				log.info("Keycloak NestUser ID: " + keycloakUserId);
				return keycloakUserId;
			} else if (statusCode == 409) {
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + randomCode);
				// fetch existing email user
				String userId = getKeycloakUserId(keycloakUrl, token, realm, randomCode);
				return userId;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + randomCode);
				// fetch existing email user
				String userId = getKeycloakUserId(keycloakUrl, token, realm, randomCode);
				return userId;
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
				String keycloakUserId = getKeycloakUserId(keycloakUrl, token, realm, randomCode);
				log.info("Keycloak NestUser ID: " + keycloakUserId);
				return keycloakUserId;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	// This is the one called from rules to create a keycloak user
	public static String getUserToken(String keycloakUrl, String keycloakUUID, String serviceToken, String realm)
			throws IOException {
		keycloakUUID = keycloakUUID.toLowerCase();

		return KeycloakUtils.getImpersonatedToken(keycloakUrl, realm, keycloakUUID, serviceToken);

	}

	// This is the one called from rules to create a keycloak user
	public static String updateUser(String keycloakUrl, String keycloakUUID, String token, String realm,
			String newUsername, String newFirstname, String newLastname, String newEmail, String password,
			String newRealmRoles, String newGroupRoles) throws IOException {
		keycloakUUID = keycloakUUID.toLowerCase();
		if (StringUtils.isBlank(newRealmRoles)) {
			newRealmRoles = "user";
		}

		String json = "{ " + "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";

		log.info("CreateUserjson=" + json);

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
		try {
			HttpPut post = new HttpPut(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + keycloakUUID);
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				KeycloakUtils.setPassword(keycloakUrl, token, realm, keycloakUUID, password);
				return keycloakUUID;
			} else if (statusCode == 204) {
				KeycloakUtils.setPassword(keycloakUrl, token, realm, keycloakUUID, password);
				return keycloakUUID;
			} else if (statusCode == 409) {
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + newUsername);
				// fetch existing email user
				return keycloakUUID;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + newUsername);
				// fetch existing email user
				return keycloakUUID;
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
				log.info("Keycloak NestUser ID: " + keycloakUUID);
				return keycloakUUID;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static java.net.http.HttpResponse<String> setUserRoles(String keycloakUrl, String projectRealm, GennyToken adminToken, 
			String uuid, String[] roles) {
		Map<String, String> roleMap = getKeycloakRoles(keycloakUrl, projectRealm, adminToken.getToken());
		String data;
		// Add roles
		try {
			data = "[";
			
			for (String rolename : roles) {
				String roleid = roleMap.get(rolename);
				if ((roleid != null) && (!"admin".equals(rolename))) {
					data += "{ \"id\": \"" + roleid + "\",\"name\":\"" + rolename + "\" },";
				}
			}
			if (StringUtils.endsWith(data, ",")) {
				data = data.substring(0, data.length() - 1); // remove last comma
			}
			data += "]";
			NestResourceClient client = new NestResourceClient();
			return client.post(
					keycloakUrl + "/auth/admin/realms/" + projectRealm + "/users/" + uuid + "/role-mappings/realm",
					adminToken, data);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			throw new WebApplicationException("Cannot create user, error in keycloak server",
					Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	// This is the one called from rules to create a keycloak user
	public static String createUser(String keycloakUrl, String keycloakUUID, String token, String realm,
			String newUsername, String newFirstname, String newLastname, String newEmail, String password,
			String newRealmRoles, String newGroupRoles) throws IOException {

		String idJson = "";
		if (!StringUtils.isBlank(keycloakUUID)) {
			idJson = "\"id\" : \"" + keycloakUUID.trim().toLowerCase() + "\",";
		}

		if (StringUtils.isBlank(newRealmRoles)) {
			newRealmRoles = "user";
		}

		String json = "{ " + idJson + "\"username\" : \"" + newUsername + "\"," + "\"email\" : \"" + newEmail + "\" , "
				+ "\"enabled\" : true, " + "\"emailVerified\" : true, " + "\"firstName\" : \"" + newFirstname + "\", "
				+ "\"lastName\" : \"" + newLastname + "\", " + "\"groups\" : [" + " \"" + newGroupRoles + "\" " + "],"
				+ "\"realmRoles\" : [" + "\"" + newRealmRoles + "\" " + "]" + "}";
		
		log.info("CreateUserjson=" + json);
		log.info("keycloakUrl = " + keycloakUrl + "/auth/admin/realms/" + realm + "/users");

		HttpClient httpClient = new DefaultHttpClient();
		// log.info("Keycloak token used is "+token);
		try {
			HttpPost post = new HttpPost(keycloakUrl + "/auth/admin/realms/" + realm + "/users");
			// HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(keycloakUrl +
			// "/auth/admin/realms/"+realm+"/users"));

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			post.setEntity(postingString);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode == 201) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				if (content.length() > 0) {
					KeycloakUtils.setPassword(keycloakUrl, token, realm, content, password);
				}
				String keycloakUserId = getKeycloakUserId(keycloakUrl, token, realm, newUsername);
				log.info("Keycloak NestUser ID: " + keycloakUserId);
				return keycloakUserId;
			} else if (statusCode == 204) {
				Header[] headers = response.getHeaders("Location");
				String locationUrl = headers[0].getValue();
				content = locationUrl.replaceFirst(".*/(\\w+)", "$1");
				String keycloakUserId = getKeycloakUserId(keycloakUrl, token, realm, newUsername);
				log.info("Keycloak NestUser ID: " + keycloakUserId);
				return keycloakUserId;
			} else if (statusCode == 409) {
				// throw new IOException("Email is already taken. Please use a different email
				// address.");
				log.warn("Email is already taken for " + newUsername);
				// fetch existing email user
				String userId = getKeycloakUserId(keycloakUrl, token, realm, newUsername);
				return userId;
			} else if (statusCode == 401) {
				// throw new IOException("Account is already taken. Please use a different email
				// address.");
				log.warn("Unauthorized token used to create " + newUsername);
				// fetch existing email user
				String userId = getKeycloakUserId(keycloakUrl, token, realm, newUsername);
				return userId;
			}
			if (entity == null) {
				throw new IOException("We could not create the new user. Please try again.");
			} else {
				String keycloakUserId = getKeycloakUserId(keycloakUrl, token, realm, newUsername);
				log.info("Keycloak NestUser ID: " + keycloakUserId);
				return keycloakUserId;
			}
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	/** Remove user from Keycloak using the URL: /admin/realms/{realm}/users/{id} */
	public static String removeUser(String keycloakUrl, String token, String realm, String userId) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpDelete post = new HttpDelete(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId);

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + token);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				return "Success";
			} else {
				return "Failed";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed";
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public static String getKeycloakUserId(String keycloakUrl, final String token, final String realm,
			final String username) throws IOException {
		final List<LinkedHashMap> users = fetchKeycloakUsers(keycloakUrl, token, realm, username);
		if (!users.isEmpty()) {
			return (String) users.get(0).get("id");
		}
		return null;
	}

	public static List<LinkedHashMap> fetchAllKeycloakUsers(String keycloakUrl, final String token,
			final String realm) {
		return fetchKeycloakUsers(keycloakUrl, token, realm, null);
	}

	public static List<LinkedHashMap> fetchKeycloakUsers(String keycloakUrl, final String token, final String realm,
			final String username) {
		return fetchKeycloakUsers(keycloakUrl, token, realm, username, 0, MAX_USERS_FETCHED);
	}

	public static List<LinkedHashMap> fetchKeycloakUsers(String keycloakUrl, final String token, final String realm,
			final String username, Integer pageIndex, Integer pageSize) {
		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
//		final HttpClient client = new DefaultHttpClient();

		log.info("Keycloak Url to fetch Keycloak User is " + keycloakUrl);
//		try {
//
//			HttpGet get = null;
			String url = null;
			if (username != null) {
				String encodedUsername = encodeValue(username);
				url = keycloakUrl + "/auth/admin/realms/" + realm + "/users?username=" + encodedUsername;
			} else {
				url = keycloakUrl + "/auth/admin/realms/" + realm + "/users?max=" + pageSize + "&first=" + pageIndex;
			}
			
			try {
				String result = sendGET(url,token);

					results = JsonSerialization.readValue(result.getBytes(), (new ArrayList<UserRepresentation>()).getClass());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
//			get = new HttpGet(url);
//			get.addHeader("Authorization", "Bearer " + token);
//			try {
//				final HttpResponse response = client.execute(get);
//				log.info("Keycloak response code is " + response.getStatusLine().getStatusCode());
//				if ((response.getStatusLine().getStatusCode() != 200)
//						&& (response.getStatusLine().getStatusCode() != 204)) {
//					throw new IOException();
//				}
//				final HttpEntity entity = response.getEntity();
//				final InputStream is = entity.getContent();
//				try {
//					results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
//				} finally {
//					is.close();
//				}
//			} catch (final IOException e) {
//				throw new RuntimeException(e);
//			}
//		} finally {
//			client.getConnectionManager().shutdown();
//		}
		return results;
	}

	private static String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}
//		public static String getKeycloakUrl() {
//			String keycloakProto =
//					System.getenv("KEYCLOAK_PROTO") != null ? System.getenv("KEYCLOAK_PROTO") : "http://";
//					String keycloakPort =
//							System.getenv("KEYCLOAK_PORT") != null ? System.getenv("KEYCLOAK_PORT") : "8180";
//							String keycloakIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : "localhost";
	//
//							String keycloakURL = System.getenv("KEYCLOAKURL") != null ? System.getenv("KEYCLOAKURL")
//									: keycloakProto + keycloakIP + ":" + keycloakPort;
//							return keycloakURL;
//		}

	public static int setPassword(String keycloakUrl, String token, String realm, String uuid, String password)
			throws IOException {
		String json = "{\"type\": \"password\", " + "\"temporary\": false," + "\"value\": \"" + password + "\"" + "}";

		HttpClient httpClient = new DefaultHttpClient();

		try {
			String url = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid + "/reset-password";
			log.info("Keycloak password reset = " + url);
			HttpPut put = new HttpPut(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid + "/reset-password");

			put.addHeader("Content-Type", "application/json");
			put.addHeader("Authorization", "Bearer " + token);

			StringEntity postingString = new StringEntity(json);
			put.setEntity(postingString);

			HttpResponse response = httpClient.execute(put);

			int statusCode = response.getStatusLine().getStatusCode();

			HttpEntity entity = response.getEntity();
			String content = null;
			if (statusCode != 204) {
				content = getContent(entity);
				throw new IOException("" + statusCode);
			}
			if (statusCode == 403) {
				throw new IOException("403 Forbidden");
			}

			return statusCode;
		}

		finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	public String createEncryptedPassword(String key, final String customercode, final String password) {
		String newkey = null;
		if (key.length() > 16) {
			newkey = key.substring(0, 16);
		} else {
			newkey = StringUtils.rightPad(key, 16, '*');
		}
		String initVector = "PRJ_" + customercode.toUpperCase();
		initVector = StringUtils.rightPad(initVector, 16, '*');

		String encryptedPassword = encrypt(newkey, initVector, password);
		if (!key.equals(newkey)) {
			log.info("NEW KEY = [" + newkey + "]");
			;
		}
		return encryptedPassword;
	}

	public static String encrypt(String key, String initVector, String value) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());
//	            log.info("encrypted string: "
//	                    + Base64.encodeBase64String(encrypted));

			return Base64.encodeBase64String(encrypted);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static String decrypt(String key, String initVector, String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static String sendVerifyEmail(String realm, final String username, final String serviceToken) {
		return sendVerifyEmail(realm, username, serviceToken);
	}

	public static String sendVerifyEmail(String keycloakUrl, final String realm, final String username,
			final String servicetoken) {
		String userId;
		try {
			userId = getKeycloakUserId(keycloakUrl, servicetoken, realm, username);
			if (userId != null) {
				HttpClient httpClient = new DefaultHttpClient();

				HttpPut putRequest = new HttpPut(
						keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/send-verify-email");

				log.info(keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + userId + "/send-verify-email");

				putRequest.addHeader("Content-Type", "application/json");
				putRequest.addHeader("Authorization", "Bearer " + servicetoken);

				HttpResponse response = null;
				try {
					response = httpClient.execute(putRequest);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int statusCode = response.getStatusLine().getStatusCode();
				log.info("sendVerifyMail statusCode is " + statusCode + " with userId=" + userId);
				if ((statusCode == 200) || (statusCode == 201)) {
					return userId;
				}
			} else {
				log.error("Could not retrieve userId from " + keycloakUrl + " for " + username);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;

	}

	private void sendVerifyTestMail(String keycloakUrl, String realm, String token, String emailusername,
			String firstname, String lastname) {

		String password = UUID.randomUUID().toString().substring(0, 8);
		String userId;
		try {
			userId = KeycloakUtils.createUser(keycloakUrl, token, realm, emailusername, firstname, lastname,
					emailusername, password, "user", "user");
		} catch (IOException e) {
		}
		userId = KeycloakUtils.sendVerifyEmail(realm, emailusername, token);
		System.out.println("UserId=" + userId);
	}

	private void sendVerifyTestMail(String keycloakUrl, String realm, String token, String username, String domain,
			String firstname, String lastname) {
		LocalDateTime now = LocalDateTime.now();
		String mydatetime = new SimpleDateFormat("yyyyMMddHHmmss").format(now.toLocalDate());
		// System.out.println(username+" serviceToken=" + token);
		String emailusername = username + "+" + mydatetime + "@" + domain;

		sendVerifyTestMail(keycloakUrl, realm, token, emailusername, firstname, lastname);
	}

	public static String getKeycloakUUIDByUserCode(String code, HashMap<String, String> userCodeUUIDMapping) {
		String keycloakUUID = null;
		if (userCodeUUIDMapping.containsKey(code)) {
			keycloakUUID = userCodeUUIDMapping.get(code);
			log.debug(String.format("DEBUG:Find user baseentity code:%s, update to keycloak uuid:%s", code,
					keycloakUUID));
		} else {
			keycloakUUID = code;
			log.debug(String.format("DEBUG:Can not find user baseentity code:%s, set keycloak uuid:%s", code,
					keycloakUUID));
		}
		return keycloakUUID;
	}

	public static HashMap<String, String> getUsersByRealm(String keycloakUrl, String realm, String servicePassword) {
		HashMap<String, String> userCodeUUIDMapping = new HashMap<>();
		List<LinkedHashMap> results = new ArrayList<>();

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			String accessToken = getAccessToken(keycloakUrl, realm, "admin-cli", null, "service", servicePassword);
			HttpGet get = new HttpGet(keycloakUrl + "/auth/admin/realms/" + realm + "/users?first=0&max=20000");
			get.addHeader("Authorization", "Bearer " + accessToken);
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new IOException("Get keycloak user response code:" + response.getStatusLine().getStatusCode());
			}
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (LinkedHashMap userMap : results) {
			String username = (String) userMap.get("username");
			String email = (String) userMap.get("email");
			String id = (String) userMap.get("id");
			if (userCodeUUIDMapping.containsKey(username)) {
				log.error(String.format("Duplicate user in keycloak, user code:%s, user name:%s, email:%s.", id,
						username, email));
			} else {
				userCodeUUIDMapping.put(username, id);
			}
		}
		return userCodeUUIDMapping;
	}

	public static String getImpersonatedToken(String keycloakUrl, String realm, String uuid, String exchangedToken)
			throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {
			ArrayList<NameValuePair> postParameters;

			HttpPost post = new HttpPost(
					keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid + "/impersonation");

			post.addHeader("Content-Type", "application/json");
			post.addHeader("Authorization", "Bearer " + exchangedToken);

			HttpResponse response = httpClient.execute(post);

			int statusCode = response.getStatusLine().getStatusCode();
			log.info("KeycloakUtils: StatusCode: " + statusCode);

			HttpEntity entity = response.getEntity();

			String content = null;
			if (statusCode != 200) {
				content = getContent(entity);
				throw new IOException("" + statusCode);
			}
			if (entity == null) {
				throw new IOException("Null Entity");
			} else {
				content = getContent(entity);
				Header[] cookies = response.getHeaders("Set-Cookie");
				log.info("KeycloakUtils: getImpersonationToken before cookies");
				if (cookies.length > 0) {
					log.info("KeycloakUtils: getImpersonationToken cookies > 0");
					for (Header cookie : cookies) {
						String value = cookie.getValue();
						log.info("KeycloakUtils: getImpersonationToken cookie=" + value);
						if (value.startsWith("KEYCLOAK_IDENTITY=")) {
							if (!value.startsWith("KEYCLOAK_IDENTITY=;")) {
								String token = cookie.getValue();

								token = token.substring("KEYCLOAK_IDENTITY=".length());
								log.info(token);
								// JsonObject jsonToken = new JsonObject(token);
								String[] splitStr = token.split(";");// jsonToken.getString("userTokenStr");
								String rawToken = splitStr[0];
								log.info("RAWTOKEN=" + rawToken);
								return rawToken;
							}
						}
					}
				}

			}

//	    				
//	    				System.out.println(content);
		} catch (Exception ee) {

		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	public static String getImpersonatedToken(String keycloakUrl, String realm, String clientId, String secret,
			String username, String exchangedToken) throws IOException {

		HttpClient httpClient = new DefaultHttpClient();

		try {
			ArrayList<NameValuePair> postParameters;

			try {
//    				// this needs -Dkeycloak.profile.feature.token_exchange=enabled
				HttpPost post = new HttpPost(keycloakUrl + "/auth/realms/" + realm + "/protocol/openid-connect/token");
				postParameters = new ArrayList<NameValuePair>(); // urn:ietf:params:oauth:grant-type:token-exchange
				postParameters
						.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange"));
				postParameters.add(new BasicNameValuePair("client_id", clientId));
				if (secret != null) {
					postParameters.add(new BasicNameValuePair("client_secret", secret));
				}
				postParameters.add(new BasicNameValuePair("subject_token", exchangedToken));
				// postParameters.add(new
				// BasicNameValuePair("client_auth_method","client-secret"));
				// postParameters.add(new BasicNameValuePair("audience", "target-client"));
				postParameters.add(new BasicNameValuePair("requested_subject", username));
				// postParameters.add(new BasicNameValuePair("requested_token_type",
				// "urn:ietf:params:oauth:token-type:access_token"));
				post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

				post.addHeader("Content-Type", "application/x-www-form-urlencoded");
				post.addHeader("Authorization", "Bearer " + exchangedToken);

				HttpResponse response = httpClient.execute(post);

				int statusCode = response.getStatusLine().getStatusCode();
				log.info("StatusCode: " + statusCode);

				HttpEntity entity = response.getEntity();

				String content = null;
				if (statusCode != 200) {
					content = getContent(entity);
					throw new IOException("" + statusCode + " " + content);
				}
				if (entity == null) {
					throw new IOException("Null Entity");
				} else {
					content = getContent(entity);
					log.info("IMPERSONATION2 content=" + content);
					JsonObject jsonToken = new JsonObject(content);
					String token = jsonToken.getString("access_token");
					return token;
				}

			} catch (Exception ee) {
				System.out.println(ee.getMessage());
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
		} catch (Exception ee) {

		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	public static String sendGET(String url, String token) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.addRequestProperty("Content-Type", "application/json");
		if (!StringUtils.isBlank(token)) {
			con.addRequestProperty("Authorization", "Bearer " + token);
		}

		// con.setRequestProperty("NestUser-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} else {
			return null;
		}

	}

	public static String executeActions(String keycloakUrl, String realm, String clientId, String secret,
			Integer lifespan, String uuid, String redirectUrl, List<String> actions, String exchangedToken)
			throws IOException {
		log.info("Sending keycloak action to " + uuid + " " + actions);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String urlResetPassword = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid
				+ "/execute-actions-email?redirect_uri=" + redirectUrl + "&client_id=" + clientId + "&lifespan="
				+ lifespan;
		HttpPut putRequest = new HttpPut(urlResetPassword);
		putRequest.addHeader("Authorization", "bearer " + exchangedToken);
		putRequest.addHeader("content-type", MediaType.APPLICATION_JSON);
		putRequest.setHeader("Accept", MediaType.APPLICATION_JSON);
		String actionsArray = "[";
		for (String action : actions) {
			actionsArray += "\"" + action + "\",";
		}
		actionsArray = actionsArray.substring(0, actionsArray.length() - 1);
		actionsArray += "]";
		StringEntity jSonEntity = new StringEntity(actionsArray);
		putRequest.setEntity(jSonEntity);
		CloseableHttpResponse response2 = httpclient.execute(putRequest);
		return "OK";
	}

	public static String getKeycloakUserUuid(String keycloakUrl, GennyToken userToken, final String realm,
			final String uuid) throws IOException {

		String url = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid;
		String result = sendGET(url, userToken.getToken());

		return result;
	}

	public static String getKeycloakUserUuidRoles(String keycloakUrl, GennyToken userToken, final String realm,
			final String uuid) throws IOException {

		String url = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid + "/role-mappings";
		String result = sendGET(url, userToken.getToken());

		return result;
	}

	public static String getKeycloakUserId(String keycloakUrl, GennyToken userToken, final String realm,
			final String username) throws IOException {

		final List<LinkedHashMap> users = fetchKeycloakUsers(keycloakUrl, userToken, realm, username);
		if (!users.isEmpty()) {
			return (String) users.get(0).get("id");
		}
		return null;
	}
//		curl --request POST 'https://path-to-your-host.com/auth/realms/your-realm/account/credentials/password' \
//		--header 'Accept: application/json' \
//		--header "Authorization: Bearer $ACCESS_TOKEN" \
//		--header 'Content-Type: application/json' \
//		--data-raw '{
//		    "currentPassword": "oldPassword",
//		    "newPassword": "newPassword",
//		    "confirmation": "newPassword"
//		}'

	public static Boolean isUsernameVerified(String keycloakUrl, GennyToken adminToken, final String realm,
			final String username) {
		Boolean ret = false;
		List<LinkedHashMap> users = fetchKeycloakUsers(keycloakUrl, adminToken, realm, username);
		if (!users.isEmpty()) {
			LinkedHashMap userMap = users.get(0);
			log.info("Keycloak User is " + userMap);
			if (userMap.containsKey("emailVerified")) {
				ret = (Boolean) userMap.getOrDefault("emailVerified", Boolean.TRUE);
			}
		}
		return ret;
	}

	public static List<LinkedHashMap> fetchKeycloakUsers(String keycloakUrl, GennyToken userToken, final String realm,
			final String username) {
		List<LinkedHashMap> results = new ArrayList<LinkedHashMap>();
		// final HttpClient client = new DefaultHttpClient();

		// try {
		try {
			String encodedUsername = encodeValue(username);
			String url = /* keycloakUrl + */keycloakUrl + "/auth/admin/realms/" + realm + "/users?username="
					+ encodedUsername;

			String result = sendGET(url, userToken.getToken());

			results = JsonSerialization.readValue(result.getBytes(), (new ArrayList<UserRepresentation>()).getClass());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//      final HttpGet get =
//          new HttpGet(url);
//		get.addHeader("Content-Type", "application/json");
//		get.addHeader("Authorization", "Bearer " + userToken.getToken());
//
//      try {
//        final HttpResponse response = client.execute(get);
//        if (response.getStatusLine().getStatusCode() != 200) {
//          throw new IOException();
//        }
//        final HttpEntity entity = response.getEntity();
//        final InputStream is = entity.getContent();
//        try {
//          results = JsonSerialization.readValue(is, (new ArrayList<UserRepresentation>()).getClass());
//        } finally {
//          is.close();
//        }
//      } catch (final IOException e) {
//        throw new RuntimeException(e);
//      }
//    } finally {
//      client.getConnectionManager().shutdown();
//    }
		return results;
	}

	public static Map<String, String> getKeycloakRoles(final String baseKeycloakUrl, final String projectRealm,
			final String token) {
		Map<String, String> roles = new HashMap<String, String>();

		java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder().build();

		URI uri = UriBuilder.fromPath(baseKeycloakUrl + "/auth/admin/realms/" + projectRealm + "/roles").build();

		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().version(java.net.http.HttpClient.Version.HTTP_2)
				.header("Authorization", "Bearer " + token).build();

		try {
			java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode json = mapper.readTree(response.body());
				for(JsonNode role : json) {
					String name = role.get("name").toString().replaceAll("\"","");
					String id = role.get("id").toString().replaceAll("\"","");
					roles.put(name, id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return roles;
	}
}
