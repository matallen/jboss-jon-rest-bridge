package org.jboss.jon.bridge;

public interface JonManager {
	
	public void inventoryAgents(); // /inventory/agents
	public void inventoryFind(); // /inventory/find?key=value&key=value
	
	public void inventoryAll(); //inventory everything in discovery queue
	public void inventoryMatches(); //inventory items in discovery queue based on search criteria
}
