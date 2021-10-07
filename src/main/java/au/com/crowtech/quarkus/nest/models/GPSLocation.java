package au.com.crowtech.quarkus.nest.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * gpslocation is the abstract base class for all gps location
 * managed.
 * An GPS location object is used as a means of storing information
 * about a 2D location on Earth.  This
 * gps information includes:
 * <ul>
 * <li>Latitude
 * <li>Longitude
 * </ul>
 * <p>
 * 
 * <p>
 * 
 * 
 * @author      Adam Crow
 * @version     %I%, %G%
 * @since       1.0
 */

@Embeddable
@RegisterForReflection
public class GPSLocation  implements  Comparable<Object>, Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	A field that stores the latitude.
	<p>
	*/
	@NotNull
	@Column(name = "latitude", updatable = false, nullable = false)
	private Double latitude;

    /**
    A field that stores the longitude.
    <p>
    */
    @NotNull
     @Column(name = "longitude", updatable = false, nullable = false)
    private Double longitude;

 
	
	/**
	  * Constructor.
	  * 
	  * @param none
	  */
	@SuppressWarnings("unused")
	public GPSLocation()
	{
		// dummy for hibernate
	}

	/**
	  * Constructor.
	  * 
	  * @param targetCode The unique code for the target associated with this Answer
	  * @param aCode The unique code for the attribute associated with this Answer
	  * @param value The associated String value
	  */
	public GPSLocation(final Double latitude, final Double longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;

	}



	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "location " + latitude + ", " + longitude;
	}


	@Override
	public int compareTo(Object anotherItem) throws ClassCastException {
		if (!(anotherItem instanceof GPSLocation))
			throw new ClassCastException("A GPSLocation object expected.");
		GPSLocation anotherLocation = (GPSLocation)anotherItem;
		// Compare by duration
		  return latitude.compareTo(anotherLocation.latitude);
	
	}		
	
}
