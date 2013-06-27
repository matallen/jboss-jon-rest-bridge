package org.jboss.jon.bridge.model;

public class Agent extends Resource{
	private String agentToken;
	private String address;
	private String description;
	
	public String getAgentToken() {
		return agentToken;
	}
	public void setAgentToken(String agentToken) {
		this.agentToken = agentToken;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
