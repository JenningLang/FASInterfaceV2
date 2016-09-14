package fasMessage;

import org.codehaus.jackson.annotate.JsonIgnore;

import Enum.MsgTypeEnum;
import fasMessage.msgContent.MsgContentNull;
import fasUtil.ConfigUtil;

public class StatusReqMsg extends FASMessage{

	private String msgfrom;
	private MsgTypeEnum msgType;
	private Long msgTime;
	@JsonIgnore
	private MsgContentNull msgContent;
	
	// constructor
	public StatusReqMsg() {
		super();
		this.msgfrom = "Server";
		this.msgType = MsgTypeEnum.StatusReq;
		this.msgTime = System.currentTimeMillis();
		this.msgContent = new MsgContentNull();
	}
	
	public StatusReqMsg(String msgfrom, MsgTypeEnum msgType, Long msgTime, MsgContentNull msgContent) {
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
	public MsgContentNull getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(MsgContentNull msgContent) {
		this.msgContent = msgContent;
	}

}
