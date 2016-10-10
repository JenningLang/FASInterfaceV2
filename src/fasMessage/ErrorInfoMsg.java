package fasMessage;

import Enum.MsgTypeEnum;
import fasMessage.msgContent.MsgContentError;
import fasUtil.ConfigUtil;

public class ErrorInfoMsg extends FASMessage{
	private String msgfrom;
	private MsgTypeEnum msgType;
	private Long msgTime;
	private MsgContentError msgContent;
	
	// constructor
	public ErrorInfoMsg() {
		super();
	}
	public ErrorInfoMsg(String errorInfo) {
		super();
		this.msgfrom = "Station *" + ConfigUtil.getStationID() + "*";
		this.msgType = MsgTypeEnum.ErrorInfo;
		this.msgTime = System.currentTimeMillis();
		this.msgContent = new MsgContentError(errorInfo);
	}
	
	public ErrorInfoMsg(String msgfrom, MsgTypeEnum msgType, Long msgTime, MsgContentError msgContent) {
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
	public MsgContentError getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(MsgContentError msgContent) {
		this.msgContent = msgContent;
	}
}
