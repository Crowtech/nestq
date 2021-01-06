package au.com.crowtech.quarkus.nest.models;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbTypeAdapter;

//import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.jboss.logging.Logger;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import au.com.crowtech.quarkus.nest.adapters.LocalDateTimeAdapter;
import au.com.crowtech.quarkus.nest.models.Device;
import au.com.crowtech.quarkus.nest.models.Email;
import au.com.crowtech.quarkus.nest.models.GennyToken;
import au.com.crowtech.quarkus.nest.models.Message;
import au.com.crowtech.quarkus.nest.adapters.EmailAdapter;

/**
 * Useris the main class that contains information
 * about a user.
 * <ul>
 * <li>created    : This is the datetime for when the user record was created.
 * <li>code       : The  keycloak String identity for this user. 
 * <lifirstname   : This is the user's firstname
 * <li>Lastname   : This is the user's lastname
 * <li>mobile     : The user's mobile
 * <li>email	  : The user's email
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
@Entity
@Table(name="nuser")
public class NestUser extends PanacheEntity {
	 private static final Logger log = Logger.getLogger(NestUser.class);	


	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime created = LocalDateTime.now(ZoneId.of("UTC"));;

	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime lastUpdate;

	@JsonbTypeAdapter(EmailAdapter.class)
	public Email email;
	public String mobile;
	public String firstname;
	public String lastname;
	public String name;
	@Column(name = "uuid", unique = true, nullable = true)
	public String uuid; // from keycloak, uniquely specifies the user
	@NotEmpty
	public String username;
	public Boolean active = true;

	@ManyToMany
	@JoinTable(name = "user_device", joinColumns =  @JoinColumn(name = "user_id",referencedColumnName="id") , inverseJoinColumns = 
			@JoinColumn(name = "device_id",referencedColumnName="id") )
	@JsonbTransient
	public Set<Device> devices = new HashSet<>();
    
    
	@SuppressWarnings("unused")
	public NestUser() {
	}

	/**
	 * @param firstname
	 * @param lastname
	 * @param email
	 * @param username
	 */
	public NestUser(String firstname, String lastname, Email email, String username) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.username = username;
		this.name = (this.firstname + " " + this.lastname).trim();
	}

	
	public NestUser(SecurityIdentity securityIdentity) {
		this.username = securityIdentity.getPrincipal().getName();
	}

	public String getUserName() {
		return username;
	}
	
	@Override
	public String toString() {
		return "AUser [" + (firstname != null ? "firstname=" + firstname + ", " : "")
				+ (lastname != null ? "lastname=" + lastname + ", " : "")
				+ (username != null ? "username=" + username + ", " : "") + (email != null ? "email=" + email : "")
				+ "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NestUser other = (NestUser) obj;
		return Objects.equals(uuid, other.uuid);
	}

	public static NestUser findByUsername(String username) {
		NestUser nestUser = null;
		
		try {
			nestUser = find("username", username).firstResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nestUser;
	}
	


	public static NestUser findByEmail(String email) {
		return find("email.name", email).firstResult();
	}

	
	public static NestUser findByUuid(String uuid) {
		return find("uuid", uuid).firstResult();
	}

	
	public static Optional<NestUser> findById(Long id) {
		PanacheQuery<NestUser> items = null;
		items = find("id",id);
		return items.firstResultOptional();
	}

	public static long deleteById(final Long id) {
		return delete("id", id);
	}

	public static List<NestUser> findByDeviceCode(String code) {
		return find("select a from AUser a JOIN ADevice d where d.code =: code", code).list();
	}

	public static Message<NestUser> findAllItems(Page page) {
		PanacheQuery<NestUser> items = null;
		Long total = 0L;

		items = NestUser.find("select u from AUser u  order by u.created DESC");

		if (NestUser.count() > 0) {
			total = count("from AUser u ");
		}

		Message<NestUser> itemsMsg = new Message<NestUser>(items.page(page).list(), total,NestUser[].class);
		return itemsMsg;
	}
	
	public static Message<NestUser> find(final GennyToken userToken, final String sorts,Page page) {

		PanacheQuery<NestUser> nestUsers = null;
		Long total = 0L;

			nestUsers = NestUser.find("select from AUser  order by "+sorts);
			

		List<NestUser> userList = nestUsers.page(page).list();
		if (userList.size()>0 ) {
			total = NestUser.count();
		}

		
		Message<NestUser> msg = new Message<NestUser>(userList ,total,NestUser[].class);
		return msg;
	}
 
 
}