package au.com.crowtech.quarkus.nest.models.nest;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * Generic NestModel structure extending from {@link PanacheEntity} and containing<br>
 * some database friendly attributes and methods
 * @author bryn
 *
 */
@MappedSuperclass
public abstract class NestModel extends PanacheEntity {
	
	@JsonbTransient
	@Column(nullable = false)
	public Boolean dbActive = true;
	
	public NestModel() {
		
	}

}