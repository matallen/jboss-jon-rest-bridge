package org.jboss.jon.bridge.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface InventoryController {
  
  public Response showHelp() throws Exception;
  public Object agents() throws Exception;
  public Object find(HttpServletRequest request) throws Exception;
  public Object operate(String operation, String command, HttpServletRequest request) throws Exception;
  public Object operationSync(String resourceId, String operation, HttpServletRequest request) throws Exception;
  public Object operationAsync(String resourceId, String operation, HttpServletRequest request) throws Exception;
  
}
