package fasMessage.msgContent;

import fasEnum.NodeEnum;

public class MsgContentStatus {
	private NodeEnum nodeType;
	private String nodeID;
	private String nodeStatus;
	
	// constructor

	public MsgContentStatus() {
		super();
	}
	
	public MsgContentStatus(NodeEnum nodeType, String nodeID, String nodeStatus) {
		super();
		this.nodeType = nodeType;
		this.nodeID = nodeID;
		this.nodeStatus = nodeStatus;
	}
	
	// getters and setter
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
	public String getNodeStatus() {
		return nodeStatus;
	}
	public void setNodeStatus(String nodeStatus) {
		this.nodeStatus = nodeStatus;
	}
}
