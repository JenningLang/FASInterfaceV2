package fasMessage.msgContent;

import java.util.List;

import Enum.NodeEnum;

public class MsgContentConfig {
	private NodeEnum nodeType;
	private String nodeID;
	private String nodeDescription;
	private String fatherNodeID;
	private List<String> childNodeIDList;
	
	// constructor
	public MsgContentConfig() {
		super();
	}
	
	public MsgContentConfig(NodeEnum nodeType, String nodeID, String nodeDescription, String fatherNodeID,
			List<String> childNodeIDList) {
		super();
		this.nodeType = nodeType;
		this.nodeID = nodeID;
		this.nodeDescription = nodeDescription;
		this.fatherNodeID = fatherNodeID;
		this.childNodeIDList = childNodeIDList;
	}
	
	// getters and setters
	public NodeEnum getNodeType() {
		return nodeType;
	}
	public void setNodeType(NodeEnum nodeType) {
		this.nodeType = nodeType;
	}
	public String getNodeID() {
		return nodeID;
	}
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	public String getNodeDescription() {
		return nodeDescription;
	}
	public void setNodeDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}
	public String getFatherNodeID() {
		return fatherNodeID;
	}
	public void setFatherNodeID(String fatherNodeID) {
		this.fatherNodeID = fatherNodeID;
	}
	public List<String> getChildNodeIDList() {
		return childNodeIDList;
	}
	public void setChildNodeIDList(List<String> childNodeIDList) {
		this.childNodeIDList = childNodeIDList;
	}
}
