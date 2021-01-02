package au.com.crowtech.quarkus.nest.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

import au.com.crowtech.quarkus.nest.adapters.LocalDateAdapter;
import au.com.crowtech.quarkus.nest.adapters.LocalDateTimeAdapter;
import au.com.crowtech.quarkus.nest.adapters.LocalTimeAdapter;
import io.quarkus.runtime.annotations.RegisterForReflection;

@Embeddable
@RegisterForReflection
public class Value implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name = "valueString", columnDefinition = "MEDIUMTEXT")
	public String valueString = null;
	@JsonbTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime valueDateTime;
	public Integer valueInteger = null;
	@JsonbTypeAdapter(LocalDateAdapter.class)
	public LocalDate valueDate = null;
	@JsonbTypeAdapter(LocalTimeAdapter.class)
	public LocalTime valueTime = null;
	public Double valueDouble = null;
	public Boolean valueBoolean = null;
	
	public Value(){}
	
	public Value(final String value)
	{
		this.valueString = value;
	}
	
	public Value(final Double value)
	{
		this.valueDouble = value;
	}

	public Value(final Integer value)
	{
		this.valueInteger = value;
	}
	
	public Value(final LocalDateTime value)
	{
		this.valueDateTime = value;
	}
	
	
	public Value(final Boolean value)
	{
		this.valueBoolean = value;
	}
	
	public Value(final LocalDate value)
	{
		this.valueDate = value;
	}
	
	public Value(final LocalTime value)
	{
		this.valueTime = value;
	}
	
	/**
	 * @return the valueString
	 */
	public String getValueString() {
		return valueString;
	}

	/**
	 * @param valueString the valueString to set
	 */
	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	/**
	 * @return the valueDateTime
	 */
	public LocalDateTime getValueDateTime() {
		return valueDateTime;
	}

	/**
	 * @param valueDateTime the valueDateTime to set
	 */
	public void setValueDateTime(LocalDateTime valueDateTime) {
		this.valueDateTime = valueDateTime;
	}

	/**
	 * @return the valueInteger
	 */
	public Integer getValueInteger() {
		return valueInteger;
	}

	/**
	 * @param valueInteger the valueInteger to set
	 */
	public void setValueInteger(Integer valueInteger) {
		this.valueInteger = valueInteger;
	}

	/**
	 * @return the valueDate
	 */
	public LocalDate getValueDate() {
		return valueDate;
	}

	/**
	 * @param valueDate the valueDate to set
	 */
	public void setValueDate(LocalDate valueDate) {
		this.valueDate = valueDate;
	}

	/**
	 * @return the valueTime
	 */
	public LocalTime getValueTime() {
		return valueTime;
	}

	/**
	 * @param valueTime the valueTime to set
	 */
	public void setValueTime(LocalTime valueTime) {
		this.valueTime = valueTime;
	}

	/**
	 * @return the valueDouble
	 */
	public Double getValueDouble() {
		return valueDouble;
	}

	/**
	 * @param valueDouble the valueDouble to set
	 */
	public void setValueDouble(Double valueDouble) {
		this.valueDouble = valueDouble;
	}

	/**
	 * @return the valueBoolean
	 */
	public Boolean getValueBoolean() {
		return valueBoolean;
	}

	/**
	 * @param valueBoolean the valueBoolean to set
	 */
	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	public String getAsString()
	{
		return toString();
	}
	
	public Optional<String> getAsOptionalString()
	{
		String ret = toString();
		if (ret == null) {
			return Optional.empty();
		} else {
			return Optional.of(ret);
		}
	}
	
	@Override
	public String toString() {
		if (valueString!=null) {
			return valueString;
		} else if (valueBoolean != null){
			return Boolean.TRUE.equals(valueBoolean)?"TRUE":"FALSE";
		}else if (valueInteger != null){
			return valueInteger.toString();
		}else if (valueDouble != null){
			return valueDouble.toString();
		}else if (valueDate != null){
			return valueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		}else if (valueDateTime != null){
			return valueDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:SS"));
		}else if (valueTime != null){
			return valueTime.format(DateTimeFormatter.ofPattern("hh:mm:SS"));
		} else {
			return null;
		}
		
	}

	public boolean isAnswered() {
		if (!StringUtils.isBlank(valueString)) {
			return true;
		}
			if ((valueBoolean != null)||(valueInteger != null) || (valueDouble != null) || (valueDate != null) || (valueDateTime != null)||(valueTime != null)) {
				return true;
			} else {
				return false;
			}
	}
	

}
