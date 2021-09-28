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