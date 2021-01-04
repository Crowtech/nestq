package au.com.crowtech.quarkus.nest.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/* 
* @author      Adam Crow
* @version     %I%, %G%
* @since       1.0
*/
@Entity
@Table(name = "device")
public class Device extends PanacheEntity {
   public LocalDateTime created;
   public LocalDateTime lastUpdate;
   
   public String userCode;
   
   
   public String code;

   public String type;
   
   public String version;
   

   @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) 
   @JsonbTransient
   public List<DeviceGPS> gps;

   
   @SuppressWarnings("unused")
	public Device() {}
 
   public Device(String type, String code, String version)
   {
		this.created = LocalDateTime.now(ZoneId.of("UTC"));
	   this.type = type;
	   this.code = code;
      this.version = version;
      System.out.println(type + code + version );
   }
   
   
   
   @Override
public String toString() {
	return "Device [" + (userCode != null ? "userCode=" + userCode + ", " : "")
			+ (code != null ? "code=" + code + ", " : "") + (type != null ? "type=" + type + ", " : "")
			+ (version != null ? "version=" + version : "") + "]";
}

public static Device findByCode(final String code) {
       return find("code", code).firstResult();
   }

   public static List<Device> findByUserCode(final String code) {
       return list("user.code", code);
   }
}
