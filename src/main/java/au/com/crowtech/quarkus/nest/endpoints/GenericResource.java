package au.com.crowtech.quarkus.nest.endpoints;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

public abstract class GenericResource {
	
	protected static final Logger genLog = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static void generateOnStart() {
	}
	
	protected Response echoResponse(Response response) {
		Object message = response.getStatus() + " ";
		if(response.hasEntity()) {
			message += response.getEntity().toString();
		}
		
		if(response.getStatus() == Status.NO_CONTENT.getStatusCode() || Math.floor(response.getStatus() / 100) != 2) {
			error(message);
		} else {
			info(message);
		}
		
		return response;
	}

	protected String className() {
		String str = this.getClass().getSimpleName();
		int index = str.indexOf('_');
		if(index != -1)
			str = str.substring(0, index);
		return str;
	}
	
	// LOG FUNCTIONS
	protected void output(Object message, FIOutput output) {
		output.log("[" + this.className() + "]:" + message);
	}
	
	protected void error(Object message) {
		output(message, genLog::error);
	}
	
	protected void info(Object message) {
		output(message, genLog::info);
	}
	
	protected void warn(Object message) {
		output(message, genLog::warn);
	}
	
	protected void debug(Object message) {
		output(message, genLog::debug);
	}
	
	protected String getUrl(UriInfo uriInfo) {
		return "";
	}
}
