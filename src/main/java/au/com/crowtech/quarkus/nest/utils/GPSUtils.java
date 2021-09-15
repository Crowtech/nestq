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

import au.com.crowtech.quarkus.nest.models.gps.LatLong;

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
	 * @param 
	 * @return the distance between passed coordinates in meters
	 * 			returns -1.0 if invalid argument dimensions. (DEPRECATED)
	 * 
	 * @see {@link GPSUtils#getDistance(LatLong, LatLong)}
	 */
	@Deprecated
	public static Double getDistance(Double[] coordinates1, Double[] coordinates2) {
		if(coordinates1.length != 2 || coordinates2.length != 2) {
			return -1.0;
		}
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

	/**
	 * Get the distance (in km) between two sets of LatLongs.
	 * @param coords1
	 * @param coords2
	 * @return - double distance between the two sets in kilometres
	 */
	public static Double getDistance(LatLong coords1, LatLong coords2) {
		if ((coords1.latitude == coords2.latitude) && (coords1.longitude == coords2.longitude)) {
			return 0.0;
		}
		else {
			double theta = coords1.longitude - coords2.longitude;
			// Calculate Sin Component
			double dist = Math.sin(Math.toRadians(coords1.latitude)) * Math.sin(Math.toRadians(coords2.latitude));
			// Calculate Cos Component and add to Sin Component
			dist += Math.cos(Math.toRadians(coords1.latitude)) * Math.cos(Math.toRadians(coords2.latitude)) * Math.cos(Math.toRadians(theta));
			// Take Arccos of sum and convert
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			dist = dist * 1.609344;
			return (dist);
		}
	}
}
