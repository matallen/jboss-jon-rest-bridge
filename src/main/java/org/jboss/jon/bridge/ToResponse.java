package org.jboss.jon.bridge;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

public class ToResponse {
	public static Response parse2(String type, Object entity) throws JsonGenerationException, JsonMappingException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		if (type!=null && type.toLowerCase().endsWith("json")){
			return Response.status(200).entity(ToJson.parse2(entity)).build();
		}else
			return Response.status(200).entity(ToXml.parse2(entity)).build();
	}
}
