package au.com.crowtech.quarkus.nest.models.gps;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import au.com.crowtech.quarkus.nest.models.GPSLocation;

public class LatLong {
	private static final Double LAT_RANGE = 0.0003;
	private static final Double LONG_RANGE = 0.0003;
	
	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	public Double latitude, longitude;

	public LatLong() {
	}
	
	public LatLong(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public LatLong(GPSLocation location) {
		this(location.getLatitude(), location.getLongitude());
	}
	
	public String toLongLatString() {
		return Double.toString(this.longitude) + "," + Double.toString(this.latitude);
	}

	/**
	 * Determine whether or not a given LatLong is within 
	 * {@link LatLong#LAT_RANGE}, {@link LatLong#LONG_RANGE} (bounding box)
	 * @param other - LatLong to compare to
	 * @return - True or False based on distance
	 */
	public boolean inVicinityOf(LatLong other) {
		return this.inVicinityOf(other, LAT_RANGE, LONG_RANGE);
	}
	
	/**
	 * Determine whether or not a given LatLong is within a range (Bounding box)
	 * @param other - LatLong to compare to
	 * @return - True or False based on distance
	 * 
	 * Default bounding box: {@link LatLong#LAT_RANGE}, {@link LatLong#LONG_RANGE}
	 */
	public boolean inVicinityOf(LatLong other, Double latRange, Double longRange) {
		Double latDiff = Math.abs(this.latitude - other.latitude);
		Double longDiff = Math.abs(this.longitude - other.longitude);
		
		return (latDiff < latRange / 2 && longDiff < longRange / 2);
	}
	
	/**
	 * Take in a set of coordinates of the form (lat.long) and return a LatLong
	 * object
	 * 
	 * @param data - "latitude.longitude"
	 * @return A new LatLong object containing the coords or null if the data is
	 *         malformed/invalid
	 */
	@Deprecated
	public static LatLong parse(String data) {
		String[] coords = data.split(".");
		try {
			double lat = Double.parseDouble(coords[0]);
			double lon = Double.parseDouble(coords[1]);
			return new LatLong(lat, lon);
		} catch (NumberFormatException e) {
			log.severe("Could not parse LatLong: " + data);
			return null;
		}

	}
	
	/**
	 * Get the distance to another LatLong pair in kilometres
	 * @param other - Other LatLong pair to get the distance to
	 * @return - distance as a Double in kilometres
	 */
	public Double distanceTo(LatLong other) {
		return distanceTo(other.latitude, other.longitude);
	}

	/**
	 * Get the distance to another LatLong pair in kilometres
	 * @param other - Other LatLong pair to get the distance to
	 * @return - distance as a Double in kilometres
	 */
	public Double distanceTo(GPSLocation location) {
		return distanceTo(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Get the distance to another LatLong pair in kilometres
	 * @param latitude - latitude of the other coordinate
	 * @param longitude - longitude of the other coordinate
	 * @return - distance as a Double in kilometres
	 */
	public Double distanceTo(Double latitude, Double longitude) {
		if ((this.latitude == latitude) && (this.longitude == longitude)) {
			return 0.0;
		} else {
			double theta = this.longitude - longitude;
			double dist = Math.sin(Math.toRadians(this.latitude)) * Math.sin(Math.toRadians(latitude))
					+ Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			dist = dist * 1.609344;
			return (dist);
		}
	}
	
	/**
	 * Get the True north bearing using the haversine formula
	 * @param otherLat - latitude (in degrees) of the point to get the true north bearing of
	 * @param otherLon - longitude (in degrees) of the point to get the true north bearing of
	 * @return bearing in degrees, relative to North
	 * 
	 * @see <a href="https://www.movable-type.co.uk/scripts/latlong.html">moveable-type.co.uk/scripts/latlong</a>
	 * @see <a href="https://stackoverflow.com/questions/3932502/calculate-angle-between-two-latitude-longitude-points">Stack Overflow Topic</a>
	 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine Formula</a>
	 * @see <a href="https://gis.stackexchange.com/questions/29239/calculate-bearing-between-two-decimal-gps-coordinates/29240#29240?s=17f63f5fbdf14239bc4caf823f8414b5">Google Stack Exchange Topic</a>
	 */
	public Double getBearing(Double targetLat, Double targetLon) {
		targetLat = Math.toRadians(targetLat);
		targetLon = Math.toRadians(targetLon);
		double deltaLong = targetLon - Math.toRadians(longitude);
	
		double deltaPhi = Math.log(Math.tan(targetLat/2.0 + Math.PI / 4.0)/Math.tan(Math.toRadians(latitude) / 2.0+Math.PI / 4.0));
		if (Math.abs(deltaLong) > Math.PI) {
		     if (deltaLong > 0.0)
		         deltaLong = -(2.0 * Math.PI - deltaLong);
		     else
		         deltaLong = (2.0 * Math.PI + deltaLong);
	
		}
		
		return (Math.toDegrees(Math.atan2(deltaLong, deltaPhi)) + 360.0) % 360.0;
	}

	/**
	 * Get the True north bearing using the haversine formula
	 * @param other - LatLong pair (in degrees) to get the true north bearing of
	 * @return bearing in degrees, relative to North
	 * 
	 * @see <a href="https://www.movable-type.co.uk/scripts/latlong.html">moveable-type.co.uk/scripts/latlong</a>
	 * @see <a href="https://stackoverflow.com/questions/3932502/calculate-angle-between-two-latitude-longitude-points">Stack Overflow Topic</a>
	 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine Formula</a>
	 * @see <a href="https://gis.stackexchange.com/questions/29239/calculate-bearing-between-two-decimal-gps-coordinates/29240#29240?s=17f63f5fbdf14239bc4caf823f8414b5">Google Stack Exchange Topic</a>
	 */
	public Double getBearing(LatLong other) {
		return getBearing(other.latitude, other.longitude);
	}

	@Override
	public String toString() {
		return latitude + "," + longitude;
	}
	
	public boolean isValid() {
		if(latitude == null)
			return false;
		if(longitude == null)
			return false;
		return !isZero();
	}
	
	public boolean isZero() {
		return (latitude == 0 && longitude == 0);
	}
}