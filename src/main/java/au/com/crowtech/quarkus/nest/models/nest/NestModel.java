package au.com.crowtech.quarkus.nest.models.nest;

import java.time.LocalDateTime;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import au.com.crowtech.quarkus.nest.adapters.LocalDateTimeAdapter;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * Generic NestModel structure extending from {@link PanacheEntity} and containing<br>
 * some database friendly attributes and methods
 * @author bryn
 *
 */
@MappedSuperclass
public abstract class NestModel extends PanacheEntity {

	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime created = LocalDateTime.now();

	/**
	 * "Active" or not in the database
	 */
	@JsonbTransient
	@Column(nullable = false)
	public Boolean dbActive = true;
	
	public NestModel() {
		
	}
	
	public void archive() {
		dbActive = false;
	}
}