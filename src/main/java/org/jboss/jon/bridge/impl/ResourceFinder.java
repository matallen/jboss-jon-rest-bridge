package org.jboss.jon.bridge.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.jon.bridge.StringUtils;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.resource.Resource;
import org.rhq.enterprise.clientapi.RemoteClient;

public class ResourceFinder {
  
  public static List<Resource> find(Map<String,Object> params, RemoteClient rc) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
    ResourceCriteria resourceCriteria = new ResourceCriteria();
    List<String> availabilityList=Arrays.asList("availability", "currentavailability");
    for(Map.Entry<String, Object> e:params.entrySet()){
      String key=e.getKey();
      Object value=e.getValue();
      if ("timeout".equals(key.toLowerCase())) continue;
      if (availabilityList.contains(key.toLowerCase())){
        Method method=resourceCriteria.getClass().getDeclaredMethod("addFilterCurrentAvailability", new Class[]{AvailabilityType.class});
        method.invoke(resourceCriteria, AvailabilityType.valueOf(((String)value).toUpperCase()));
      }else{
        Method method=resourceCriteria.getClass().getDeclaredMethod("addFilter"+StringUtils.toUpperCaseFirstLetter(key), new Class[]{value.getClass()});
        method.invoke(resourceCriteria, value);
      }
      System.out.println("adding filter ["+key+" = "+value+"]");
    }
    
//    resourceCriteria.addFilterName(param);
//    resourceCriteria.addFilterCurrentAvailability(AvailabilityType.UNKNOWN);
//    resourceCriteria.addFilterVersion("4.4");
//    resourceCriteria.addFilterResourceTypeName("RHQ Agent"); // why do we need this?
    List<Resource> resources = rc.getResourceManager().findResourcesByCriteria(rc.getSubject(), resourceCriteria);
  
    Iterator<Resource> it=resources.iterator();
    List<Resource> result=new ArrayList<Resource>();
    while (it.hasNext()){
      result.add(it.next());
    }
    return result;
  }

}
