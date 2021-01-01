package au.com.crowtech.quarkus.nest.adapters;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

import au.com.crowtech.quarkus.nest.models.Email;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class EmailAdapter implements JsonbAdapter<Email, JsonObject> {


	@Override
	public JsonObject adaptToJson(Email obj) throws Exception {
		
		return Json.createObjectBuilder()
	              .add("name", obj.getName())
	              .build();
	}

	@Override
	public Email adaptFromJson(JsonObject obj) throws Exception {
		String name = obj.getString("name");
		Boolean validated = obj.getBoolean("validated");
		Email ret = new Email(name);
		ret.setValidated(validated);
		if (!Email.validate(name)) {
			throw new Exception("Email address is not valid "+name);
		}
		
	return ret;
	}
	

}