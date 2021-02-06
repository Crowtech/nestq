package au.com.crowtech.quarkus.nest.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;


public class SendNotification {
	private static final Logger log = Logger.getLogger(SendNotification.class);
//	user=$1
//			title=$2
//			body=$3
//			echo "user is ${user}"
//			echo "title = ${title}"
//			echo "body = ${body}"
//			token=$(curl -s https://artprizes-quarkus.crowtech.com.au/api/public/notify/${user})
//			echo $token
//			echo
//			curl https://fcm.googleapis.com/fcm/send -H "Content-Type:application/json" -X POST -d "{\"notification\": {\"body\": \"${body}\",\"title\": \"${title}\"}, \"priority\": \"high\", \"data\": {\"click_action\": \"FLUTTER_NOTIFICATION_CLICK\", \"id\": \"1\", \"status\": \"done\"}, \"to\": \"${token}\"}" -H "Authorization: key=AAAA_HTuyKw:APA91bEdF4e98KTB8D9eIvhrEj5RY6_v0gpO7glIzcBWazZgh567Bl-AZde6pSqEMQdXVc8eXFylfJPP-pOJbSuMeaDRSWF01EZSDf0dAqNgQhVKHC35ErdFIIwk7oc9gbmW2Js5a8MM"

	public static String send(final String userNotificationToken, final String title, final String body,
			final String apiKey) {

		CloseableHttpResponse response2 = null;
		String firebaseUrl = "https://fcm.googleapis.com/fcm/send";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(firebaseUrl);
		postRequest.addHeader("Authorization", "key=" + apiKey);
		postRequest.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		postRequest.setHeader("Accept", MediaType.APPLICATION_JSON);
		String actionsArray = "{\"notification\": {\"body\": \"" + body + "\",\"title\": \"" + title
				+ "\"}, \"priority\": \"high\", \"data\": {\"click_action\": \"FLUTTER_NOTIFICATION_CLICK\", \"id\": \"1\", \"status\": \"done\"}, \"to\": \""
				+ userNotificationToken + "\"}";
		log.info(actionsArray);
		
		try {
			StringEntity jSonEntity = new StringEntity(actionsArray);
			postRequest.setEntity(jSonEntity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			response2 = httpclient.execute(postRequest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response2.toString();
	}


}
