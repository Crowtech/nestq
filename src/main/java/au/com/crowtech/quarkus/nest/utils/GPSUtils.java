package au.com.crowtech.quarkus.nest.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.json.JsonArray;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;

public class GPSUtils {

	private static final Logger log = Logger.getLogger(GPSUtils.class);	

	/*
	 * Sends QCmdGeofenceMessage for the given beg, address and radius
	 */
	static String loadId = null;
	static JsonArray loadAttributes = null;


	/**
	 * 
	 * @param address
	 * @return the latitude and longitude of the given address
	 * @throws Exception
	 */
	public static String[] getLatLong(String address) throws Exception {
		int responseCode = 0;
		String api = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8")
				+ "&sensor=true";
		log.info("API URL : " + api);
		URL url = new URL(api);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.connect();
		responseCode = httpConnection.getResponseCode();
		if (responseCode == 200) {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			;
			Document document = dBuilder.parse(httpConnection.getInputStream());
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("/GeocodeResponse/status");
			String status = (String) expr.evaluate(document, XPathConstants.STRING);
			if (status.equals("OK")) {
				expr = xpath.compile("//geometry/location/lat");
				String latitude = (String) expr.evaluate(document, XPathConstants.STRING);
				expr = xpath.compile("//geometry/location/lng");
				String longitude = (String) expr.evaluate(document, XPathConstants.STRING);
				return new String[] { latitude, longitude };
			} else {
				throw new Exception("Error from the API - response status: " + status);
			}
		}
		return null;
	}

	/**
	 * 
	 * @param Double[]
	 *            coordinates1
	 * @param Double[]
	 *            coordinates2
	 * @return the distance between passed coordinates in meters
	 */
	public static Double getDistance(Double[] coordinates1, Double[] coordinates2) {
//		try {
//
//			/* Call Google Maps API to know how far the driver is */
//			String response = QwandaUtils.apiGet("https://maps.googleapis.com/maps/api/distancematrix/json?origins="
//					+ coordinates1[0] + "," + coordinates1[1] + "&destinations=" + coordinates2[0] + ","
//					+ coordinates2[1] + "&mode=driving&language=pl-PL", null);
//
//			log.info(response);
//			if (response != null) {
//
//				JsonObject distanceMatrix = new JsonObject(response);
//				JsonArray elements = distanceMatrix.getJsonArray("rows").getJsonObject(0).getJsonArray("elements");
//				JsonObject distance = elements.getJsonObject(0).getJsonObject("distance");
//				Integer distanceValue = distance.getInteger("value");
//				return distanceValue.doubleValue();
//			}
//		} catch (Exception e) {
//
//		}
//
//		return -1.0;
		if ((coordinates1[0] == coordinates2[0]) && (coordinates1[1] == coordinates2[1])) {
			return 0.0;
		}
		else {
			double theta = coordinates1[1] - coordinates2[1];
			double dist = Math.sin(Math.toRadians(coordinates1[0])) * Math.sin(Math.toRadians(coordinates2[0])) + Math.cos(Math.toRadians(coordinates1[0])) * Math.cos(Math.toRadians(coordinates2[0])) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			dist = dist * 1.609344;
			return (dist);
		}
	}

	

}
