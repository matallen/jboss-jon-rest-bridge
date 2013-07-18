package org.jboss.jon.bridge.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.io.IOUtils;
import org.jboss.jon.bridge.MapBuilder;
import org.jboss.jon.bridge.StringUtils;
import org.jboss.jon.bridge.ToCanonical;
import org.jboss.jon.bridge.api.Configuration;
import org.jboss.jon.bridge.api.InventoryController;
import org.rhq.core.clientapi.agent.bundle.BundleScheduleRequest;
import org.rhq.core.domain.criteria.BundleCriteria;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.resource.Resource;
import org.rhq.enterprise.clientapi.RemoteClient;
import org.rhq.enterprise.server.bundle.BundleManagerBean;
import org.rhq.enterprise.server.bundle.BundleManagerHelper;

import com.google.common.collect.Maps;

@Path("/inventory")
public class InventoryControllerImpl implements InventoryController, Configuration {

  @Override public String getHost()     {return "localhost";}
  @Override public String getPort()     {return "7080";}
  @Override public String getUsername() {return "rhqadmin";}
  @Override public String getPassword() {return "rhqadmin";}
  
  @GET @Path("/help") @Produces(value=MediaType.APPLICATION_JSON)
  public Response showHelp() throws Exception {
    String help=IOUtils.toString(this.getClass().getClassLoader().getResource("help.html"));
    return Response.status(200).entity(help).build();
  }

  @GET @Path("/agents") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object agents() throws Exception{
    Map<String,Object> params=Maps.newHashMap();
    params.put("resourceTypeName", "RHQ Agent");
    params.put("name", "RHQ Agent");
//    Map<String,Object> params=MapBuilder.builder().with("resourceTypeName", "RHQ Agent").with("name", "RHQ Agent").build();
    List<org.jboss.jon.bridge.model.Resource> resources=ToCanonical.toCanonical(ResourceFinder.find(params, getRemoteClient(this)));
    return resources;
  }
  
  @GET @Path("/find") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object find(@Context HttpServletRequest request) throws Exception{
    Map<String,Object> params=StringUtils.parseParameters(request);
    List<org.jboss.jon.bridge.model.Resource> resources=ToCanonical.toCanonical(ResourceFinder.find(params, getRemoteClient(this)));
    return resources;
  }
  @GET @Path("/operation/{operation}/{command}") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object operate(@PathParam("operation") String operation, @PathParam("command") String command, @Context HttpServletRequest request) throws Exception{
    Map<String,Object> params=StringUtils.parseParameters(request);
    RemoteClient rc = getRemoteClient(this);
    List<Resource> resources = ResourceFinder.find(params, rc);
    return OperationExecutor.operation(true, resources, operation, command, params.containsKey("timeout")?Integer.parseInt((String)params.get("timeout")):5000, rc);
  }
  
  @GET @Path("/resource/{resourceId}/operation/{operation}") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object operationSync(@PathParam("resourceId") String resourceId, @PathParam("operation") String operation, @Context HttpServletRequest request) throws Exception{
    Map<String,Object> params=StringUtils.parseParameters(request);
    RemoteClient rc = getRemoteClient(this);
    params.put("Id", Integer.parseInt(resourceId));
    List<Resource> resources=ResourceFinder.find(params, rc);
    return OperationExecutor.operation(true, resources, "executePromptCommand", operation, 5000, rc);
  }
  
  @GET @Path("/resource/{resourceId}/operationAsync/{operation}") @Produces(value={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Object operationAsync(@PathParam("resourceId") String resourceId, @PathParam("operation") String operation, @Context HttpServletRequest request) throws Exception{
    Map<String,Object> params=StringUtils.parseParameters(request);
    RemoteClient rc = getRemoteClient(this);
    params.put("Id", Integer.parseInt(resourceId));
    List<Resource> resources=ResourceFinder.find(params, rc);
    return OperationExecutor.operation(false, resources, "executePromptCommand", operation, 5000, rc);
  }
  
  public void createBundle(){
//    BundleManagerHelper.getPluginContainer().getBundleServerPluginManager().get
    BundleScheduleRequest x;
    BundleCriteria bundleCrit = new BundleCriteria();
    bundleCrit.addFilterName("bundleName");
    var bundles = BundleManager.findBundlesByCriteria(bundleCrit);
    
//    BundleManager.scheduleRevertBundleDeployment(dest.get(0).id, description, true)
  }
  
//  public Object test(@Context HttpServletResponse response, @Context HttpServletRequest request) throws Exception{
//    
//  }


  private RemoteClient getRemoteClient(Configuration cfg){
    RemoteClient remoteClient = new RemoteClient(getHost(), Integer.parseInt(getPort()));
    try{
      remoteClient.login(getUsername(), getPassword());
    }catch (Exception e){
      final String msg = "Failed to login to the JON server";
      System.err.println(msg);
      throw new RuntimeException(msg, e);
    }
    return remoteClient;
  }
}
