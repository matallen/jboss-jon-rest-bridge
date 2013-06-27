package org.jboss.jon.bridge.model;

public class Resource extends ModelBase{
	private int id;
	private String name;
	private String uuid;
	private String resourceTypeName;
	private String version;
	private String operationJobId;
	private String operationStatus;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getResourceTypeName() {
		return resourceTypeName;
	}
	public void setResourceTypeName(String type) {
		this.resourceTypeName = type;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setOperationJobId(String job) {
		this.operationJobId=job;
	}
	public String getOperationJobId() {
		return operationJobId;
	}
	public void setOperationStatus(String status) {
		this.operationStatus=status;
	}
	public String getOperationStatus(){
		return operationStatus;
	}
}
