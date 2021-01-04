package au.com.crowtech.quarkus.nest.adapters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;

import org.jboss.logging.Logger;

import au.com.crowtech.quarkus.nest.models.Value;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ValueAdapter implements JsonbAdapter<Value, JsonObject> {

	private static final Logger log = Logger.getLogger(ValueAdapter.class);

	public ValueAdapter() {
	}

	@Override
	public JsonObject adaptToJson(Value value) throws Exception {
		JsonbConfig config = new JsonbConfig()
				.withAdapters(new LocalDateTimeAdapter(), new LocalDateAdapter(), new LocalTimeAdapter())
				.withNullValues(false).withFormatting(true);

		Jsonb jsonb = JsonbBuilder.create(config);

		String stringValue2 = jsonb.toJson(value);
		return Json.createObjectBuilder().add("value", stringValue2).build();
	}

	@Override
	public Value adaptFromJson(JsonObject valueJson) throws Exception {
		Value value = new Value();
		if (valueJson.containsKey("valueString") && !valueJson.isNull("valueString")) {
			String vs = valueJson.getString("valueString");
			log.info("VS=[" + vs + "]");
			value.valueString = vs;

			if (value.valueString != null) {
				return value;
			}
		} else if (valueJson.containsKey("valueBoolean") && !valueJson.isNull("valueBoolean")) {
			value.valueBoolean = valueJson.getBoolean("valueBoolean");
			if (value.valueBoolean != null) {
				return value;
			}
		} else if (valueJson.containsKey("valueInteger") && !valueJson.isNull("valueInteger")) {
			value.valueInteger = valueJson.getInt("valueInteger");
			if (value.valueInteger != null) {
				return value;
			}
		} else if (valueJson.containsKey("valueDouble") && !valueJson.isNull("valueDouble")) {
			JsonNumber jn = valueJson.getJsonNumber("valueDouble");
			if (jn != null) {
				value.valueDouble = jn.doubleValue();
				return value;
			}
		} else if (valueJson.containsKey("valueDateTime") && !valueJson.isNull("valueDateTime")) {
			String ldtStr = valueJson.getString("valueDateTime");
			if (ldtStr != null) {
				JsonbConfig config = new JsonbConfig().withAdapters(new LocalDateTimeAdapter());
				Jsonb jsonb = JsonbBuilder.create(config);

				LocalDateTime ldt = jsonb.fromJson(ldtStr, LocalDateTime.class);
				value.valueDateTime = ldt;
				return value;
			}
		} else if (valueJson.containsKey("valueDate") && !valueJson.isNull("valueDate")) {
			String ldStr = valueJson.getString("valueDate");
			if (ldStr != null) {
				JsonbConfig config = new JsonbConfig().withAdapters(new LocalDateAdapter());
				Jsonb jsonb = JsonbBuilder.create(config);

				LocalDate ld = jsonb.fromJson(ldStr, LocalDate.class);
				value.valueDate = ld;
				return value;
			}
		} else if (valueJson.containsKey("valueTime") && !valueJson.isNull("valueTime")) {
			String ltStr = valueJson.getString("valueTime");
			if (ltStr != null) {
				JsonbConfig config = new JsonbConfig().withAdapters(new LocalTimeAdapter());
				Jsonb jsonb = JsonbBuilder.create(config);

				LocalTime lt = jsonb.fromJson(ltStr, LocalTime.class);
				value.valueTime = lt;
				return value;
			}
		}

		return value;
	}

}