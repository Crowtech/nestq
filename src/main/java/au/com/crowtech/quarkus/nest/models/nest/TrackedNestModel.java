package au.com.crowtech.quarkus.nest.models.nest;

import java.time.LocalDateTime;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.MappedSuperclass;

import au.com.crowtech.quarkus.nest.adapters.LocalDateTimeAdapter;

/**
 * Generic Model Structure that includes created and updated datetime attribs
 * @author bryn
 *
 */
@MappedSuperclass
public class TrackedNestModel extends NestModel {
	
	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime lastUpdate = LocalDateTime.now();
	
	
	public TrackedNestModel() {
		
	}
}
