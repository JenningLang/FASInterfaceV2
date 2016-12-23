package fasMessage;

import java.util.List;

import fasEnum.MsgTypeEnum;
import fasEnum.NodeEnum;
import fasMessage.msgContent.MsgContentConfig;
import fasUtil.ConfigUtil;

public class ConfigReplyMsg extends FASMessage{
	private String msgfrom;
	private MsgTypeEnum msgType;
	private Long msgTime;
	private MsgContentConfig msgContent;
	
	// constructor
	public ConfigReplyMsg() {
		super();
	}
	public ConfigReplyMsg(NodeEnum node, 
			String nodeID, 
			String nodeDescription, 
			String fatherNodeID,
			List<String> childNodeIDList) {
		super();
		this.msgfrom = "Station *" + ConfigUtil.getStationID() + "*";
		this.msgType = MsgTypeEnum.ConfigReply;
		this.msgTime = System.currentTimeMillis();
		this.msgContent = new MsgContentConfig(node, nodeID, nodeDescription, fatherNodeID, childNodeIDList);
	}
	
	public ConfigReplyMsg(String msgfrom, MsgTypeEnum msgType, Long msgTime, MsgContentConfig msgContent) {
		super();
		this.msgfrom = msgfrom;
		this.msgType = msgType;
		this.msgTime = msgTime;
		this.msgContent = msgContent;
	}
	
	// getters and setters
	public String getMsgfrom() {
		return msgfrom;
	}
	public void setMsgfrom(String msgfrom) {
		this.msgfrom = msgfrom;
	}
	public MsgTypeEnum getMsgType() {
		return msgType;
	}
	public void setMsgType(MsgTypeEnum msgType) {
		this.msgType = msgType;
	}
	public Long getMsgTime() {
		return msgTime;
	}
	public void setMsgTime(Long msgTime) {
		this.msgTime = msgTime;
	}
	public MsgContentConfig getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(MsgContentConfig msgContent) {
		this.msgContent = msgContent;
	}
}
