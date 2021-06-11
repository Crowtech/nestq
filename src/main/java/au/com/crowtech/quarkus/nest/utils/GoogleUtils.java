package au.com.crowtech.quarkus.nest.utils;

import java.io.IOException;

import org.jboss.logging.Logger;

import io.vertx.core.json.JsonObject;

public class GoogleUtils {
	 private static final Logger log = Logger.getLogger(KeycloakUtils.class);	

	public static String getTimeZoneId(String latitude, String longitude, String googleApiKey)
	{
		// FInd their location, call Google
		String url = "https://maps.googleapis.com/maps/api/timezone/json?location="+latitude+","+longitude+"&timestamp="+java.time.Instant.now().getEpochSecond()+"&key="+googleApiKey;
		String timezoneJsonStr;
		try {
			timezoneJsonStr = KeycloakUtils.sendGET(url , null);
			JsonObject json = new JsonObject(timezoneJsonStr);
			String timezoneID = json.getString("timeZoneId");
			return timezoneID;
		} catch (IOException e) {
			log.error("Cannot find timezone! ");
		}

		return "Melbourne/Australia";
	}
}
