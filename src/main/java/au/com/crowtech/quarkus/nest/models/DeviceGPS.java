package au.com.crowtech.quarkus.nest.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/* 
* @author      Adam Crow
* @version     %I%, %G%
* @since       1.0
*/
@Entity
@Table(name = "devicegps")
public class DeviceGPS extends PanacheEntity {
	@JsonbProperty("created")
	public LocalDateTime created;

	@ManyToOne
	public Device device;

	public String deviceCode;

	public GPSLocation position;

	@SuppressWarnings("unused")
	public DeviceGPS() {
	}

	public DeviceGPS(Device device, GPSLocation position) {
		this.created = LocalDateTime.now(ZoneId.of("UTC"));
		this.device = device;
		this.deviceCode = device.code;
		this.position = position;
	}

	
	
	
	@Override
	public String toString() {
		return "DeviceGPS [" + (created != null ? "created=" + created + ", " : "")
				+ (device != null ? "device=" + device + ", " : "")
				+ (deviceCode != null ? "deviceCode=" + deviceCode + ", " : "")
				+ (position != null ? "position=" + position : "") + "]";
	}

	public static DeviceGPS findByCode(final String code) {
		return find("code", code).firstResult();
	}

	public static List<DeviceGPS> findByDeviceCode(final String code) {
		return list("device.code", code);
	}
}
