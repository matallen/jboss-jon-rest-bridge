package org.jboss.jon.bridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.jboss.jon.bridge.model.Resource;

public class ToXml {

	public static String parse2(Object entity) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		StringBuffer sb=new StringBuffer();
		
		if (List.class.isAssignableFrom(entity.getClass())){
			for(Object o:(List)entity)
				appendObject(sb,o);
		}else if (Map.class.isAssignableFrom(entity.getClass())){
			throw new RuntimeException("Maps not yet implemented");
		}else{
			appendObject(sb,entity);
		}
		return sb.toString();
	}
	private static void appendObject(StringBuffer sb, Object resource) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		sb.append("\t<"+resource.getClass().getSimpleName()+">\n");
		Method[] methods=resource.getClass().getMethods();
		for(Method m:methods){
			if(m.getName().startsWith("get") && !m.getName().equals("getClass")){
				try{
  				Object value=m.invoke(resource, new Class[]{});
  				if (null!=value && (value instanceof String || value instanceof Integer) ){
  					String methodName=StringUtils.toCamelCase(m.getName().replaceFirst("get", ""));
  					sb.append("\t\t<"+methodName+">"+value+"</"+methodName+">\n");
  				}
				}catch(IllegalArgumentException sink){
				}
			}
		}
		sb.append("\t</"+resource.getClass().getSimpleName()+">\n");
	}
//	public static String parse(List<Resource> inResources) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
//		StringBuffer sb=new StringBuffer();
//		for(Resource resource:inResources){
//			sb.append("\t<"+resource.getClass().getSimpleName()+">\n");
//			Method[] methods=resource.getClass().getMethods();
//			for(Method m:methods){
//				if(m.getName().startsWith("get")){
//					Object value=m.invoke(resource, new Class[]{});
//					if (null!=value){
//  					String methodName=StringUtils.toCamelCase(m.getName().replaceFirst("get", ""));
//  					sb.append("\t\t<"+methodName+">"+value+"</"+methodName+">\n");
//					}
//				}
//			}
//			sb.append("\t</"+resource.getClass().getSimpleName()+">\n");
//		}
//		return sb.toString();
//	}
}
