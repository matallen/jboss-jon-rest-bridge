package org.jboss.jon.bridge;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@Provider
@ServerInterceptor
public class ResponseGeneratorInterceptor implements PostProcessInterceptor{

	@Override
	public void postProcess(ServerResponse response) {
//		System.out.println("ContentType="+response.getMetadata().get("Content-Type"));
	  System.out.println("XXXX");
	  String type=MediaType.APPLICATION_XML;
		if (response.getMetadata().containsKey("Content-Type")){
			Object o=response.getMetadata().get("Content-Type").get(0);
			if (MediaType.class.isAssignableFrom(o.getClass())){
				type=((MediaType)o).getSubtype();
			}
		}
		try {
			if (type!=null && type.toLowerCase().endsWith("json")){
				response.setEntity(new GenericEntity<String>(ToJson.parse2(response.getEntity()), String.class));
			}else // Xml (default)
				response.setEntity(new GenericEntity<String>(ToXml.parse2(response.getEntity()), String.class));
			
			response.setStatus(200);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
