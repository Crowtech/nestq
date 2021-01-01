package au.com.crowtech.quarkus.nest.adapters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class LocalTimeAdapter implements JsonbAdapter<LocalTime, JsonValue> {
	 
	 public LocalTimeAdapter() {}

	@Override
	public JsonValue adaptToJson(LocalTime obj) throws Exception {
		
		String dateTimePattern = "HH:mm:ss";
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
		String localDateTimeStr = obj.format(dateFormatter);
		return Json.createValue(localDateTimeStr);

	}

	@Override
	public LocalTime adaptFromJson(JsonValue obj) throws Exception {
		String dateTimePattern = "HH:mm:ss";
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
		String str = obj.toString();
		str = str.substring(0,str.length()-1);
		String value = str.substring(1);
		LocalTime ret = null;
		
		try {
			ret = dateFormatter.parse(value, LocalTime::from);
		} catch (Exception e) {
			dateTimePattern = "HH:mm:ss";
			dateFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
			ret = dateFormatter.parse(value, LocalTime::from);
		}

	return ret;
	}
	
	static public  LocalDateTime getLocalDateTimeFromString(final String dateTimeStr, ZoneOffset zoneOffset)
	{
		TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(dateTimeStr);
	    Instant i = Instant.from(ta);
	    LocalDateTime dt  = LocalDateTime.ofInstant(i, zoneOffset);
	    return dt;
	}
}