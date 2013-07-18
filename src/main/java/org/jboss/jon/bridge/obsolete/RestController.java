package org.jboss.jon.bridge.obsolete;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.criteria.ResourceOperationHistoryCriteria;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.operation.GroupOperationHistory;
import org.rhq.core.domain.operation.JobId;
import org.rhq.core.domain.operation.OperationRequestStatus;
import org.rhq.core.domain.operation.ResourceOperationHistory;
import org.rhq.core.domain.operation.bean.ResourceOperationSchedule;
import org.rhq.core.domain.resource.Agent;
import org.rhq.core.domain.resource.Resource;
import org.rhq.enterprise.clientapi.RemoteClient;
import org.rhq.enterprise.server.operation.OperationManagerRemote;
import org.rhq.enterprise.server.resource.ResourceManagerRemote;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Path("/inventory")
public class RestController {
  private static final String host="localhost";
  private static final int port=7080;
  private static final String username="rhqadmin";
  private static final String password="rhqadmin";
  
  @GET @Path("/{param}/find/options") @Produces(value=MediaType.APPLICATION_JSON)
  public Response showFindOptions(@PathParam("param") String ignore) {
  	StringBuffer sb=new StringBuffer();
  	sb
  	.append("?name=<value>\n")
  	.append("?version=<value>\n")
  	.append("?currentAvailability=UP|DOWN|UNKNOWN\n")
  	.append("?resourceTypeName=<value>\n")
//  	.append("?=<value>\n")
  	.append("\n");
    return Response.status(200).entity(sb.toString()).build();
  }
  
  @GET @Path("/agents") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object agents() throws Exception{//return agents(null);}
  	Map<String,Object> params=Maps.newHashMap();
  	params.put("resourceTypeName", "RHQ Agent");
  	params.put("name", "RHQ Agent");
  	List<org.jboss.jon.bridge.model.Resource> resources=ToCanonical.toCanonical(findResources(params));
  	return resources;
  }
  
  @GET @Path("/find") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object find(@Context HttpServletResponse response, @Context HttpServletRequest request) throws Exception{
  	Map<String,Object> params=StringUtils.parseParameters(request);
    List<org.jboss.jon.bridge.model.Resource> resources=ToCanonical.toCanonical(findResources(params));
  	return resources;
  }

  private class HashMapBuilder{
  	private Map<String,Object> params=Maps.newHashMap();
  	public HashMapBuilder with(String key, Object value){
  		params.put(key, value); return this;
  	}
  	public HashMap build(){
  		HashMap result=new HashMap(params.size());
  		for(Map.Entry<String, Object> e:params.entrySet())
  			result.put(e.getKey(), e.getValue());
  		return result;
  	}
  }
  
  @GET @Path("/resource/{resourceId}/operation/{operation}") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object operationSync(@PathParam("resourceId") String resourceId, @PathParam("operation") String operation, @Context HttpServletResponse response, @Context HttpServletRequest request) throws Exception{
  	Map<String,Object> params=StringUtils.parseParameters(request);
  	RemoteClient rc = getRemoteClient(host, port, username, password);
  	params.put("Id", Integer.parseInt(resourceId));
  	List<Resource> resources=findResources(params, rc);
  	return operation(true, resources, operation, 5000, rc);
  }
  
  @GET @Path("/resource/{resourceId}/operationAsync/{operation}") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object operationAsync(@PathParam("resourceId") String resourceId, @PathParam("operation") String operation, @Context HttpServletResponse response, @Context HttpServletRequest request) throws Exception{
  	Map<String,Object> params=StringUtils.parseParameters(request);
  	RemoteClient rc = getRemoteClient(host, port, username, password);
  	params.put("Id", Integer.parseInt(resourceId));
  	List<Resource> resources=findResources(params, rc);
  	return operation(false, resources, operation, 5000, rc);
  }
  
  private List<org.jboss.jon.bridge.model.Resource> operation(boolean sync, List<Resource> resources, String operation, long timeoutInMs, RemoteClient rc){
  	Configuration config=new Configuration();
  	config.put(new PropertySimple("command", operation));
  	
  	OperationManagerRemote omr=rc.getOperationManager();
    Map<Integer,org.jboss.jon.bridge.model.Resource> result=Maps.newHashMap();
    
    for(Resource resource:resources){
      int delay=0;
      int repeatInterval=0;
      int repeatCount=0;
      int timout=600;
      System.out.println("Executing executePromptCommand '"+operation+"' against resource "+ resource.getId()+ "(type:'"+resource.getResourceType()+")");
      ResourceOperationSchedule sch=omr.scheduleResourceOperation(rc.getSubject(), resource.getId(), "executePromptCommand", delay, repeatInterval, repeatCount, timout, config, "attempting to force avail");
      result.put(resource.getId(), ToCanonical.toCanonical(resource));
      result.get(resource.getId()).setOperationJobId(sch.getJobId().getJobName() + ":"+sch.getJobId().getJobGroup());
    }
    if (!sync)
    	return Lists.newArrayList(result.values());
    
    // sync... did they succeed?
    for(Resource resource:resources){
    	ResourceOperationHistoryCriteria criteria=new ResourceOperationHistoryCriteria();
    	String[] job=result.get(resource.getId()).getOperationJobId().split(":");
    	criteria.addFilterJobId(new JobId(job[0], job[1]));
    	
    	OperationRequestStatus status=OperationRequestStatus.INPROGRESS;
    	long theTimeout=System.currentTimeMillis()+timeoutInMs;
    	while(status==OperationRequestStatus.INPROGRESS && System.currentTimeMillis()<theTimeout){
      	List<ResourceOperationHistory> history=omr.findResourceOperationHistoriesByCriteria(rc.getSubject(), criteria);
      	if (history.size()==1){
      		status=history.get(0).getStatus();
      		result.get(resource.getId()).setOperationStatus(status.name().toLowerCase());
      	}
      	try{Thread.sleep(500);}catch(Exception sink){}
    	}
    	if (status==OperationRequestStatus.INPROGRESS)
    		result.get(resource.getId()).setOperationStatus("timeout");
    }
    return Lists.newArrayList(result.values());  	
  }
  
  private List<Resource> findResources(Map<String,Object> params, RemoteClient rc) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
    ResourceCriteria resourceCriteria = new ResourceCriteria();
    List<String> availabilityList=Arrays.asList("availability", "currentavailability");
    for(Map.Entry<String, Object> e:params.entrySet()){
      String key=e.getKey();
      Object value=e.getValue();
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
  
  private List<Resource> findResources(Map<String,Object> params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{
    return findResources(params, getRemoteClient(host, port, username, password));
  }
  
//  @GET
//  @Path("/agents/operation/{param}")
//  public Response doSomething(@PathParam("param") String xxx) {
//    RemoteClient remoteClient = getRemoteClient(host, port, username, password);
//
//    try {
//      ResourceManagerRemote resourceManagerRemote = remoteClient.getResourceManager();
//      
//      // Find the JBoss Server from the JON Name
//      ResourceCriteria resourceCriteria = new ResourceCriteria();
//      resourceCriteria.addFilterName("RHQ Agent");
//      resourceCriteria.addFilterCurrentAvailability(AvailabilityType.UNKNOWN);
//      resourceCriteria.addFilterVersion("4.4");
//      resourceCriteria.addFilterResourceTypeName("RHQ Agent"); // why do we need this?
//      
////      resourceCriteria.addFilterName(jonJbossName);
////      resourceCriteria.addFilterInventoryStatus(InventoryStatus.NEW);
//
//
//      List<Resource> resources = resourceManagerRemote.findResourcesByCriteria(remoteClient.getSubject(), resourceCriteria);
//      
//      Configuration config=new Configuration();
//      config.put(new PropertySimple("command", "avail --force"));
//      String operation = "executePromptCommand";
//      
//      OperationManagerRemote omr=remoteClient.getOperationManager();
//      Iterator it=resources.iterator();
//      int count = 0;
//      while (it.hasNext()){
//        Resource resource=(Resource)it.next();
//        Agent agent=resource.getAgent();
//        agent.getAgentToken();
//        int delay=0;
//        int repeatInterval=0;
//        int repeatCount=0;;
//        int timout=600;
//        omr.scheduleResourceOperation(remoteClient.getSubject(), resource.getId(), operation, delay, repeatInterval, repeatCount, timout, config, "attempting to force avail");
//        try { Thread.sleep(2000); } catch (InterruptedException e) {}
//        count++;
//      }
//      
//      System.out.println("completed "+ count+ " agents");
//      remoteClient.logout();
//      
//    } finally {
//      remoteClient.logout();
//    }
//
//    return Response.status(200).entity("doSomething called '" + "X" + "'").build();
//  }
  
  
  private RemoteClient getRemoteClient(String host, int port, String username, String password)  {
    RemoteClient remoteClient = new RemoteClient(host, port);
    try{
      remoteClient.login(username, password);
    }catch (Exception e){
      final String msg = "Failed to login to the JON server";
      System.err.println(msg);
      throw new RuntimeException(msg, e);
    }
    return remoteClient;
  }

}