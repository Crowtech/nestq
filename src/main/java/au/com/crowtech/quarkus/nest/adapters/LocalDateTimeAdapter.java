package au.com.crowtech.quarkus.nest.adapters;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class LocalDateTimeAdapter implements JsonbAdapter<LocalDateTime, JsonValue> {
	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	public LocalDateTimeAdapter() {
	}

	@Override
	public JsonValue adaptToJson(LocalDateTime obj) throws Exception {

		String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
		String localDateTimeStr = obj.format(dateFormatter);
		return Json.createValue(localDateTimeStr);

	}

	@Override
	public LocalDateTime adaptFromJson(JsonValue obj) throws Exception {
		String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
		String str = obj.toString();
		str = str.substring(0, str.length() - 1);
		String value = str.substring(1);
		LocalDateTime ret = null;

		try {
			ret = dateFormatter.parse(value, LocalDateTime::from);
		} catch (Exception e) {
			dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
			dateFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
			ret = dateFormatter.parse(value, LocalDateTime::from);
		}

		return ret;
	}

	static public LocalDateTime getLocalDateTimeFromString(final String dateTimeStr, ZoneOffset zoneOffset) {
		TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(dateTimeStr);
		Instant i = Instant.from(ta);
		LocalDateTime dt = LocalDateTime.ofInstant(i, zoneOffset);
		return dt;
	}
}