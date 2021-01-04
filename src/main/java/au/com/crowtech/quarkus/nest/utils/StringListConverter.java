package au.com.crowtech.quarkus.nest.utils;


import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.jboss.logging.Logger;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  @Override
  public String convertToDatabaseColumn(final List<String> list) {
    String ret = "";
    if (list!=null) {
    for (final String str : list) {
      ret += str + ",";
    }
    }
    return ret;

  }

  @Override
  public List<String> convertToEntityAttribute(final String joined) {
    List<String> strings = new CopyOnWriteArrayList<String>();
    if (joined != null) {
      strings = new CopyOnWriteArrayList<>(Arrays.asList(joined.split(",")));
    }
    return strings;
  }

}
