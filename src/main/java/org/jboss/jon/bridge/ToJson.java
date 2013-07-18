package org.jboss.jon.bridge;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;

public class ToJson {

	class LowerCaseAnnotationIntrospector extends JacksonAnnotationIntrospector {
		@Override
		public String findRootName(AnnotatedClass ac) {
			return ac.getAnnotated().getSimpleName().toLowerCase();
		}
	}
	public static String parse2(Object entity) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(
				org.codehaus.jackson.map.SerializationConfig.Feature.WRAP_ROOT_VALUE,
				true);
		mapper.setAnnotationIntrospector(new ToJson().new LowerCaseAnnotationIntrospector()); //why?
		final String result = mapper.writeValueAsString(entity);
		return result;
	}
}
