package org.jboss.jon.bridge;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class StringUtils {

  public static String toCamelCase(String in){
    return in.substring(0,1).toLowerCase()+in.substring(1);
  }
  public static String toUpperCaseFirstLetter(String in){
    return in.substring(0,1).toUpperCase()+in.substring(1);
  }

  public static Map<String,Object> parseParameters(HttpServletRequest request){
  	Map<String,Object> result=new HashMap<String,Object>();
    Enumeration<String> e=request.getParameterNames();
    while (e.hasMoreElements()){
      String key=(String)e.nextElement();
      String value=request.getParameter(key);
      result.put(key, value);
    }
    return result;
  }

}
