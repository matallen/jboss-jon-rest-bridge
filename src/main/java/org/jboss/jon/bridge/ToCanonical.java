package org.jboss.jon.bridge;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;
import org.rhq.core.domain.resource.Resource;
import com.google.common.collect.Lists;

public class ToCanonical {

	public static org.jboss.jon.bridge.model.Resource toCanonical(Resource resource) {
		try {
			Object instance;
			if (null != resource.getAgent() && resource.getResourceType().getName().equals("RHQ Agent")) {
				instance = Class.forName("org.jboss.jon.bridge.model.Agent").newInstance();
				BeanUtils.copyProperties(instance, resource.getAgent());
				BeanUtils.setProperty(instance, "version", resource.getVersion());
				BeanUtils.setProperty(instance, "resourceTypeName", resource.getResourceType().getName());
//				BeanUtils.setProperty(instance, "uuid", resource.getUuid());
				BeanUtils.setProperty(instance, "description", resource.getDescription());
				// org.jboss.jon.bridge.model.Agent x;
			} else {
				instance = Class.forName("org.jboss.jon.bridge.model.Resource").newInstance();
				BeanUtils.copyProperties(instance, resource);
				BeanUtils.setProperty(instance, "resourceTypeName", resource.getResourceType().getName());
				// org.jboss.jon.bridge.model.Resource x;
			}
			return (org.jboss.jon.bridge.model.Resource)instance;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<org.jboss.jon.bridge.model.Resource> toCanonical(List<Resource> inResources) {
		List resources = Lists.newArrayList();
		for (Resource resource : inResources){
			Object o=toCanonical(resource);
			if (null!=o)
				resources.add(o);
		}
		return resources;
	}
}
