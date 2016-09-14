package Enum;

public enum MsgTypeEnum {
	ConfigReq, ConfigReply, StatusReq, StatusReply, ErrorInfo;
	public static MsgTypeEnum parseInt(int n) throws Exception{
		if(n == MsgTypeEnum.ConfigReq.ordinal()){
			return MsgTypeEnum.ConfigReq;
		}else if(n == MsgTypeEnum.ConfigReply.ordinal()){
			return MsgTypeEnum.ConfigReply;
		}else if(n == MsgTypeEnum.StatusReq.ordinal()){
			return MsgTypeEnum.StatusReq;
		}else if(n == MsgTypeEnum.ErrorInfo.ordinal()){
			return MsgTypeEnum.ErrorInfo;
		}else if(n == MsgTypeEnum.StatusReply.ordinal()){
			return MsgTypeEnum.StatusReply;
		}else{
			throw new Exception("FCMPMsgTypeEnum: parseInt error");
		}
	}
}
