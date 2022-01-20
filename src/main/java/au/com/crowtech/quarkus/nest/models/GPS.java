package au.com.crowtech.quarkus.nest.models;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

import au.com.crowtech.quarkus.nest.models.gps.LatLong;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * gps is the abstract base class for all gps managed in the Qwanda library. An
 * GPS object is used as a means of storing information from a source about a
 * target GPS. This gps information includes:
 * <ul>
 * <li>GPS Location (Latitude/Longitude)
 * <li>The time at which the gps was created
 * <li>Bearing
 * </ul>
 * <p>
 * 
 * <p>
 * 
 * 
 * @author Adam Crow
 * @version %I%, %G%
 * @since 1.0
 */

@Embeddable
@RegisterForReflection
public class GPS implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Stores the Created UMT DateTime that this object was created
	 */
//	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	@Column(name = "received")
	private LocalDateTime received;

	/*
	 * A field that stores the location. <p>
	 */
//	@Embedded
	// private GPSLocation position;

	// Sue me if this works
	@Column(name = "latitude", updatable = false, nullable = false)
	private Double latitude;
	@Column(name = "longitude", updatable = false, nullable = false)
	private Double longitude;

	/**
	 * A field that stores the timestamp.
	 * <p>
	 */
	// @NotNull
	@Size(max = 32)
	@Column(name = "timestamp", updatable = false, nullable = true)
	private String timestamp;

	/**
	 * A field that stores the accuracy.
	 * <p>
	 */
	// @NotNull
	@Column(name = "accuracy", updatable = false, nullable = true)
	private Double accuracy;

	/**
	 * A field that stores the bearing/heading.
	 * <p>
	 */
	// @NotNull
	@Column(name = "bearing", updatable = false, nullable = true)
	private Double bearing;

	/**
	 * A field that stores the altitude.
	 * <p>
	 */
	// @NotNull
	@Column(name = "altitude", updatable = false, nullable = true)
	private Double altitude;

	/**
	 * A field that stores the altitude accuracy.
	 * <p>
	 */
	// @NotNull
	@Column(name = "altitude_accuracy", updatable = false, nullable = true)
	private Double altitude_accuracy;

	/**
	 * A field that stores the speed.
	 * <p>
	 */
	// @NotNull
	@Column(name = "speed", updatable = false, nullable = true)
	private Double speed;

	/**
	 * Constructor.
	 * 
	 * @param none
	 */
	@SuppressWarnings("unused")
	public GPS() {
		// dummy for hibernate
	}

	/**
	 * Constructor.
	 * 
	 * @param aCode The unique code for the attribute associated with this Answer
	 * @param value The associated String value
	 */
	public GPS(final GPSLocation position) {
		//this.position = position;
		this.latitude = position.getLatitude();
		this.longitude = position.getLongitude();
	}

	public GPS(final Double latitude, final Double longitude) {
		this(new GPSLocation(latitude, longitude));
	}

	public GPS(final Double latitude, final Double longitude, final String timestamp, final Double accuracy,
			final Double bearing, final Double altitude, final Double altitude_accuracy, final Double speed) {

		//this.position = new GPSLocation(latitude, longitude);
		this.latitude = latitude;
		this.longitude = longitude;
		this.timestamp = timestamp;
		this.accuracy = accuracy;
		this.bearing = bearing;
		this.altitude = altitude;
		this.altitude_accuracy = altitude_accuracy;
		this.speed = speed;
	}
	
	// TODO: Finish implementing this pending testing of the bearing
	
	public Boolean isFacing(LatLong coords, double range) {
		Double bearingToPoint = new LatLong(latitude, longitude).getBearing(coords);
		double minBearing = this.bearing - range / 2;
		double maxBearing = this.bearing + range / 2;
		if(maxBearing > 360) {
			maxBearing -= 360;
		}
		
		if(minBearing < 0) {
			minBearing += 360;
		}
		
		if(bearingToPoint <= maxBearing) {
			return bearingToPoint >= minBearing;
		}
		
		return false;
	}
	
	
	/**
	 * @return the received
	 */
	public LocalDateTime getReceived() {
		return received;
	}
	/*
		*//**
			 * @return the position
			 *//*
				 * public GPSLocation getPosition() { return position; }
				 */

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the accuracy
	 */
	public Double getAccuracy() {
		return accuracy;
	}

	/**
	 * @return the bearing
	 */
	public Double getBearing() {
		return bearing;
	}

	/**
	 * @return the altitude
	 */
	public Double getAltitude() {
		return altitude;
	}

	/**
	 * @return the altitude_accuracy
	 */
	public Double getAltitude_accuracy() {
		return altitude_accuracy;
	}

	/**
	 * @return the speed
	 */
	public Double getSpeed() {
		return speed;
	}

	/**
	 * @param received the received to set
	 */
	public void setReceived(LocalDateTime received) {
		this.received = received;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(GPSLocation position) {
		//this.position = position;
		this.setPosition(position.getLatitude(), position.getLongitude());
	}

	public void setPosition(Double lat, Double lon) {
		this.latitude = lat;
		this.longitude = lon;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * @param bearing the bearing to set
	 */
	public void setBearing(Double bearing) {
		this.bearing = bearing;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	/**
	 * @param altitude_accuracy the altitude_accuracy to set
	 */
	public void setAltitude_accuracy(Double altitude_accuracy) {
		this.altitude_accuracy = altitude_accuracy;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GPS [ lat/long=" + new GPSLocation(latitude, longitude) 
				+ ", bearing=" + bearing 
				+ ", speed=" + speed
				+ "]";
	}

	@JsonbTransient
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@JsonbTransient
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	public GPSLocation getPosition() {
		return new GPSLocation(latitude, longitude);
	}

}
