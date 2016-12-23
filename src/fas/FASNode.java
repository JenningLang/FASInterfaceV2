package fas;

import java.util.List;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;

import fasEnum.NodeEnum;

public class FASNode {
	/* static info */
	private NodeEnum nodeType;
	private String nodeID;
	private String nodeDescription;
	private String fatherNodeID;
	private List<String> childNodeIDList;
	private ObjectType objType;// BACnet 设备类型
	private int instantNumber; // BACnet 实例号
	/* dynamic info */
	private String nodeStatus;
	
	// constructor
	public FASNode(NodeEnum nodeType, String nodeID, String nodeDescription, String fatherNodeID,
			List<String> childNodeIDList, ObjectType objType, int instantNumber, String nodeStatus) {
		super();
		this.nodeType = nodeType;
		this.nodeID = nodeID;
		this.nodeDescription = nodeDescription;
		this.fatherNodeID = fatherNodeID;
		this.childNodeIDList = childNodeIDList;
		this.objType = objType;
		this.instantNumber = instantNumber;
		this.nodeStatus = nodeStatus;
	}

	// toString
	@Override
	public String toString() {
		return "FASNode [nodeType=" + nodeType + ", nodeID=" + nodeID + ", nodeDescription=" + nodeDescription
				+ ", fatherNodeID=" + fatherNodeID + ", childNodeIDList=" + childNodeIDList + ", objType=" + objType
				+ ", instantNumber=" + instantNumber + ", nodeStatus=" + nodeStatus + "]";
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

	public ObjectType getObjType() {
		return objType;
	}

	public void setObjType(ObjectType objType) {
		this.objType = objType;
	}
	
	public int getInstantNumber() {
		return instantNumber;
	}

	public void setInstantNumber(int instantNumber) {
		this.instantNumber = instantNumber;
	}

	public String getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(String nodeStatus) {
		this.nodeStatus = nodeStatus;
	}
	
}
