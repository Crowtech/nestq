package au.com.crowtech.quarkus.nest.models;


import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;

import au.com.crowtech.quarkus.nest.utils.SecurityUtils;
import io.quarkus.runtime.annotations.RegisterForReflection;


@RegisterForReflection
@RequestScoped
public class GennyToken implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GennyToken.class);	

	
//	@Inject
//	@IdToken
//	JsonWebToken accessToken;


	String code;
	String userCode;
	String token;
	Map<String, Object> adecodedTokenMap = null;
	String realm = null;
	Set<String> userRoles = new HashSet<String>();

	
	@Deprecated
	public GennyToken()
	{
//		String token = accessToken.getRawToken();
//		processToken(token);
	}
	
	public GennyToken(final String token) {
		processToken(token);

	}

	public GennyToken(final String code, final String token) {
		this(token);
		log.info("[!]Token: " + token);
		this.code = code;
	}

	public GennyToken(final String code, final String id, final String issuer, final String subject, final long ttl,
			final String secret, final String realm, final String username, final String name, final String role) {

		this(code, id, issuer, subject, ttl, secret, realm, username, name, role,
				LocalDateTime.now().plusSeconds(24 * 60 * 60)); // 1 day expiry
	}

	
	public GennyToken(final String code, final String id, final String issuer, final String subject, final long ttl,
			final String secret, final String realm, final String username, final String name, final String role,
			final LocalDateTime expiryDateTime) {
		adecodedTokenMap = new HashMap<String, Object>();
		adecodedTokenMap.put("preferred_username", username);
		adecodedTokenMap.put("name", name);
		if (username.contains("@")) {
			adecodedTokenMap.put("email", username);
		} else {
			adecodedTokenMap.put("email", username+"@gmail.com");
		}
		String[] names = name.split(" ");
		adecodedTokenMap.put("given_name", names[0].trim());
		adecodedTokenMap.put("family_name", names[1].trim());
		adecodedTokenMap.put("jti", UUID.randomUUID().toString().substring(0, 20));
		adecodedTokenMap.put("sub", id);
		adecodedTokenMap.put("realm", realm);
		adecodedTokenMap.put("azp", realm);
		adecodedTokenMap.put("aud", realm);
	//	adecodedTokenMap.put("realm_access", "{ \"roles\": [\"user\",\"" + role + "\"] }");
		adecodedTokenMap.put("exp", expiryDateTime.atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("session_state", UUID.randomUUID().toString().substring(0, 32)); // TODO set size ot same
																								// as keycloak

		userRoles = new HashSet<String>();
//		  "realm_access": {
//		    "roles": [
//		      "test",
//		      "dev",
//		      "offline_access",
//		      "admin",
//		      "uma_authorization",
//		      "user",
//		      "supervisor"
//		    ]
//		  },
		    


		JsonArrayBuilder rj = Json.createArrayBuilder();
		userRoles.add("user");
		String[] roles = role.split(",:;");
		for (String r : roles) {
			userRoles.add(r);
			rj.add(r);
		}

		adecodedTokenMap.put("realm_access", rj);

		
		
		String jwtToken = null;

		jwtToken = SecurityUtils.createJwt(id, issuer, subject, ttl, secret, adecodedTokenMap);
		token = jwtToken;
		this.realm = realm;
		String normalisedUsername = getNormalisedUsername(username);
		if (normalisedUsername.toUpperCase().startsWith("PER_")) {
			this.userCode = normalisedUsername.toUpperCase();
		} else {
			this.userCode = "PER_" + normalisedUsername.toUpperCase();
		}

		this.code = code;
		setupRoles();
	}

	public GennyToken(final String code, final String realm, final String username, final String name,
			final String role) {
		this(code, "ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role,
				LocalDateTime.now().plusSeconds(24 * 60 * 60));
	}

	public GennyToken(final String uuid,final String code, final String realm, final String username, final String name,
			final String role, LocalDateTime expiryDateTime) {
		this(code, uuid, "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role,
				expiryDateTime);
	}
	public GennyToken(final String code, final String realm, final String username, final String name,
			final String role, LocalDateTime expiryDateTime) {
		this(code, "ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role,
				expiryDateTime);
	}

	public String getToken() {
		return token;
	}

	public Map<String, Object> getAdecodedTokenMap() {
		return adecodedTokenMap;
	}

	public void setAdecodedTokenMap(Map<String, Object> adecodedTokenMap) {
		this.adecodedTokenMap = adecodedTokenMap;
	}

	private void setupRoles() {
		String realm_accessStr = null;
		String realm = (String)adecodedTokenMap.get("realm");
		String pref_username = (String)adecodedTokenMap.get("preferred_username");
		String client_id = (String)adecodedTokenMap.get("azp");
		if("master".equals(realm) && "admin".equals(pref_username) && "admin-cli".equals(client_id)) {
			userRoles.add("admin");
			return;			
		}
		try {
			realm_accessStr = adecodedTokenMap.get("realm_access").toString();
			Pattern p = Pattern.compile("(?<=\\[)([^\\]]+)(?=\\])");
			Matcher m = p.matcher(realm_accessStr);

			if (m.find()) {
				String[] roles = m.group(1).split(",");
				for (String role : roles) {
					userRoles.add((String) role.trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Check if GennyToken contains one or more specified roles
	 * @param roles - role(s) to check for
	 * @return true if at least one of the specified roles is in the GennyToken
	 */
	public boolean hasRole(final String... roles) {
		for(String role : roles) {
			if(userRoles.contains(role))
					return true;
		}
		
		return false;
	}
	
	public boolean hasRole(final String role) {
		return (userRoles.contains(role));
	}

	@Override
	public String toString() {
		return getRealm() + ": " + getCode() + ": " + getUserCode() + ": " + this.userRoles;
	}

	public String getRealm() {
		return realm;
	}

	public String getString(final String key) {
		return (String) adecodedTokenMap.get(key);
	}

	public String getCode() {
		return code;
	}

	public String getSessionCode() {
		return getString("session_state");
	}
	
	public String getKeycloakUrl() {
		String fullUrl = getString("iss");
		URI uri;
		try {
			uri = new URI(fullUrl);
			 String domain = uri.getHost();
			 String proto = uri.getScheme();
			 Integer port = uri.getPort();
			 return proto+"://"+domain+":"+port;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "http://keycloak.genny.life";
	}

	public String getClientCode() {
		return getString("aud");
	}

	/**
	 * @return the userCode
	 */
	public String getUserCode() {
		return userCode;
	}

	@XmlTransient
	@Transient
	public LocalDateTime getAuthDateTime() {
		Long auth_timestamp = ((Number) adecodedTokenMap.get("auth_time")).longValue();
		LocalDateTime authTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(auth_timestamp),
				TimeZone.getDefault().toZoneId());
		return authTime;
	}

	@XmlTransient
	@Transient
	public LocalDateTime getExpiryDateTime() {
		Long exp_timestamp = ((Number) adecodedTokenMap.get("exp")).longValue();
		LocalDateTime expTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(exp_timestamp),
				TimeZone.getDefault().toZoneId());
		return expTime;
	}

	@XmlTransient
	@Transient
	public OffsetDateTime getExpiryDateTimeInUTC() {

		Long exp_timestamp = ((Number) adecodedTokenMap.get("exp")).longValue();
		LocalDateTime expTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(exp_timestamp),
				TimeZone.getDefault().toZoneId());
		ZonedDateTime ldtZoned = expTime.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));

		return utcZoned.toOffsetDateTime();
	}
	
	@XmlTransient
	@Transient
	public Integer getSecondsUntilExpiry() {

		OffsetDateTime expiry = getExpiryDateTimeInUTC();
		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		Long diff = expiry.toEpochSecond() - now.toEpochSecond(ZoneOffset.UTC);
		return diff.intValue();
	}

	// JWT Issue DateTime
	@XmlTransient
	@Transient
	public LocalDateTime getiatDateTime() {
		Long iat_timestamp = ((Number) adecodedTokenMap.get("iat")).longValue();
		LocalDateTime iatTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(iat_timestamp),
				TimeZone.getDefault().toZoneId());
		return iatTime;
	}

	// Unique token id
	@XmlTransient
	@Transient
	public String getUniqueId() {
		return (String) adecodedTokenMap.get("jti");
	}

	// Unique uuid
	@XmlTransient
	@Transient
	public String getUuid() {
		String uuid =  null;
		
		try {
			uuid = (String) adecodedTokenMap.get("sub");
		} catch (Exception e) {
			log.info("Not a valid user");
		}
		
		return uuid;
	}
	
	// Name
	@XmlTransient
	@Transient
	public String getName() {
		return (String) adecodedTokenMap.get("name");
	}
	
	// FirstName
	@XmlTransient
	@Transient
	public String getFirstname() {
		return (String) adecodedTokenMap.get("given_name");
	}

	// LastName
	@XmlTransient
	@Transient
	public String getLastname() {
		return (String) adecodedTokenMap.get("family_name");
	}

	// Email
	@XmlTransient
	@Transient
	public String getEmail() {
		return (String) adecodedTokenMap.get("email");
	}

	// Email Verified
	@XmlTransient
	@Transient
	public Boolean isEmailVerified() {
		return (Boolean) adecodedTokenMap.get("email_verified");
	}

	
	/**
	 * @return the userRoles
	 */
	public Set<String> getUserRoles() {
		return userRoles;
	}

//	// Send the decoded Json token in the map
//	public static Map<String, Object> getJsonMap(final String json) {
//		final JSONObject jsonObj = getDecodedToken(json);
//		return getJsonMap(jsonObj);
//	}

//	public static Map<String, Object> getJsonMap(final JSONObject jsonObj) {
//		final String json = jsonObj.toString();
//		Map<String, Object> map = new HashMap<String, Object>();
//		try {
//			final ObjectMapper mapper = new ObjectMapper();
//			// convert JSON string to Map
//			final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
//			};
//
//			map = mapper.readValue(json, typeRef);
//
//		} catch (final JsonGenerationException e) {
//			e.printStackTrace();
//		} catch (final JsonMappingException e) {
//			e.printStackTrace();
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
//
//		return map;
//	}

	public static String getNormalisedUsername(final String rawUsername) {
		if (rawUsername==null) {
			return null;
		}
		String username = rawUsername.replaceAll("\\&", "_AND_").replaceAll("@", "_AT_").replaceAll("\\.", "_DOT_")
				.replaceAll("\\+", "_PLUS_").toUpperCase();
		// remove bad characters
		username = username.replaceAll("[^a-zA-Z0-9_]", "");
		return username;

	}
	
	// Decode the keycloak token string and send back in Json Format
	public static Map<String,Object> getDecodedToken(final String bearerToken) {


			final String[] jwtToken = bearerToken.split("\\.");
			final Base64 decoder = new Base64(true);
			final byte[] decodedClaims = decoder.decode(jwtToken[1]);
			Map<String, Object> json=null;
			try {
				json = JsonUtil.parseJson(new String(decodedClaims));
			} catch (JoseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return json;
	}
	
	private void processToken(final String token)
	{
		if ((token != null) && (!token.isEmpty())) {
			// Getting decoded token in Hash Map from QwandaUtils
			adecodedTokenMap = getDecodedToken(token);
			if (adecodedTokenMap == null) {
				log.error("Token is not able to be decoded in GennyToken ..");
			} else {

				// Extracting realm name from iss value
				
				String iss = adecodedTokenMap.get("iss").toString();
				String realm = StringUtils.substringAfterLast(iss, "/");
				if(realm == null) {
					realm = (adecodedTokenMap.get("aud").toString()); // handle non Keycloak 6+
				}

				// Adding realm name to the decoded token
				adecodedTokenMap.put("realm", realm);
				this.token = token;
				this.realm = realm;
				String username = (String) adecodedTokenMap.get("preferred_username");
				String normalisedUsername = getNormalisedUsername(username);
				this.userCode = "PER_" + normalisedUsername.toUpperCase();

				setupRoles();
			}

		} else {
			log.error("Token is null or zero length in GennyToken ..");
		}		
	}
	
	public String getUsername() {
		return getString("preferred_username");
	}
}
