package au.com.crowtech.quarkus.nest.models;

import java.io.Serializable;
import java.util.regex.Matcher;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;

import io.quarkus.runtime.annotations.RegisterForReflection;

@Embeddable
@RegisterForReflection
public class Email implements Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private static final String regex  = "^[\\\\w!#$%&'*+/=?`{|}~^-]+(?:\\\\.[\\\\w\\+!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,6}$";  // RFC 5322 Email
	private static final String regex = "^.*\\@.*$";
	// also restrict number of characters in top level domain
	@Column(name = "email")
	@Pattern(regexp = regex, message = "Incorrect format")

	private String name;
    private Boolean validated = false;
    
    public Email() {}
    
	/**
	 * @param name
	 * @param value
	 * @throws Exception 
	 */
	public Email(String name) throws Exception {
		this.name = name;
		this.validated = false;
		if (!Email.validate(name)) {
			throw new Exception("Email "+name+" does not meet RFC 5322 requirements");
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the validated
	 */
	public Boolean getValidated() {
		return validated;
	}

	/**
	 * @param validated the validated to set
	 */
	public void setValidated(Boolean validated) {
		this.validated = validated;
	}

	static public Boolean validate(final String email)
	{
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		 
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	@Override
	public String toString() {
		return "Email [" + (name != null ? "name=" + name + ", " : "")
				+ (validated != null ? "validated=" + validated : "") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Email other = (Email) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}  

 
    
}