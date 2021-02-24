package au.com.crowtech.quarkus.nest.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.logging.Logger;

public class FileUtils {
	 private static final Logger log = Logger.getLogger(KeycloakUtils.class);	


	static public byte[] fetchRemoteFile(String location) throws Exception {
		URL url = new URL(location);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
		  is = url.openStream ();
		  byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
		  int n;

		  while ( (n = is.read(byteChunk)) > 0 ) {
		    baos.write(byteChunk, 0, n);
		  }
		}
		catch (IOException e) {
		  log.error ("Failed while reading bytes from "+url.toExternalForm()+" "+e.getMessage());
		  e.printStackTrace ();
		  // Perform any other exception handling that's appropriate.
		}
		finally {
		  if (is != null) { is.close(); }
		}
		return baos.toByteArray();
		}
}
