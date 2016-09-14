package fasMessage;

import Enum.MsgTypeEnum;
import Enum.NodeEnum;
import fasMessage.msgContent.MsgContentError;
import fasMessage.msgContent.MsgContentStatus;
import fasUtil.ConfigUtil;

public class StatusReplyMsg extends FASMessage{
	
	private String msgfrom;
	private MsgTypeEnum msgType;
	private Long msgTime;
	private MsgContentStatus msgContent;
	
	// constructor
	public StatusReplyMsg() {
		super();
	}
	public StatusReplyMsg(NodeEnum nodeType, String nodeID, String nodeStatus) {
		super();
		this.msgfrom = "Station *" + ConfigUtil.getStationID() + "*";
		this.msgType = MsgTypeEnum.StatusReply;
		this.msgTime = System.currentTimeMillis();
		this.msgContent = new MsgContentStatus(nodeType, nodeID, nodeStatus);
	}
	
	public StatusReplyMsg(String msgfrom, MsgTypeEnum msgType, Long msgTime, MsgContentStatus msgContent) {
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
	public MsgContentStatus getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(MsgContentStatus msgContent) {
		this.msgContent = msgContent;
	}

}
