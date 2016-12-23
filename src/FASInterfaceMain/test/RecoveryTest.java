package FASInterfaceMain.test;

import java.util.ArrayList;
import java.util.List;

import fasEnum.NodeEnum;
import fasMessage.*;
import fasUtil.JsonUtil;

public class RecoveryTest {
	public static void main(String args[]){
		
		List<String> l = new ArrayList<String>();
		l.add("child 1");
		l.add("child 2");
		ConfigReplyMsg msg = new ConfigReplyMsg(NodeEnum.Finish, null, null, null, new ArrayList<String>());
		String msgStr = JsonUtil.toJSON(msg);
		System.out.println(msgStr);
		ConfigReplyMsg rMsg = JsonUtil.fromJSON(msgStr, ConfigReplyMsg.class);
		System.out.println(rMsg.getMsgfrom());
		System.out.println(rMsg.getMsgType());
		System.out.println(rMsg.getMsgTime());
		System.out.println(rMsg.getMsgContent().getNodeType());
		System.out.println(rMsg.getMsgContent().getNodeID());
		System.out.println(rMsg.getMsgContent().getNodeDescription());
		System.out.println(rMsg.getMsgContent().getFatherNodeID());
		rMsg.getMsgContent().getChildNodeIDList().forEach(s -> System.out.println(s));
		
//		StatusReplyMsg msg = new StatusReplyMsg(NodeEnum.Finish, "", null);
//		String msgStr = JsonUtil.toJSON(msg);
//		System.out.println(msgStr);
//		FASMessage fasMsg = JsonUtil.fromJSON(msgStr, StatusReplyMsg.class);
//		System.out.println(fasMsg.getClass().equals(StatusReplyMsg.class));
//		System.out.println(fasMsg.getClass().isInstance(FASMessage.class));
//		System.out.println(fasMsg.getClass().isInstance(StatusReplyMsg.class));
//		StatusReplyMsg rMsg = (StatusReplyMsg)fasMsg;
//		System.out.println(rMsg.getMsgfrom());
//		System.out.println(rMsg.getMsgType());
//		System.out.println(rMsg.getMsgTime());
//		System.out.println(rMsg.getMsgContent().getNodeType());
//		System.out.println(rMsg.getMsgContent().getNodeID());
//		System.out.println(rMsg.getMsgContent().getNodeStatus());
		
//		StatusReqMsg msg = new StatusReqMsg();
//		String msgStr = JsonUtil.toJSON(msg);
//		System.out.println(msgStr);
//		StatusReqMsg rMsg = JsonUtil.fromJSON(msgStr, StatusReqMsg.class);
//		System.out.println(rMsg.getMsgfrom());
//		System.out.println(rMsg.getMsgType());
//		System.out.println(rMsg.getMsgTime());
//		System.out.println(rMsg.getMsgContent());
		
//		ConfigReqMsg msg = new ConfigReqMsg();
//		String msgStr = JsonUtil.toJSON(msg);
//		System.out.println(msgStr);
//		ConfigReqMsg rMsg = JsonUtil.fromJSON(msgStr, ConfigReqMsg.class);
//		System.out.println(rMsg.getMsgfrom());
//		System.out.println(rMsg.getMsgType());
//		System.out.println(rMsg.getMsgTime());
//		System.out.println(rMsg.getMsgContent());
		
//		ErrorInfoMsg msg = new ErrorInfoMsg("Error Test");
//		String msgStr = JsonUtil.toJSON(msg);
//		System.out.println(msgStr);
//		ErrorInfoMsg rMsg = JsonUtil.fromJSON(msgStr, ErrorInfoMsg.class);
//		System.out.println(rMsg.getMsgfrom());
//		System.out.println(rMsg.getMsgType());
//		System.out.println(rMsg.getMsgTime());
//		System.out.println(rMsg.getMsgContent().getErrorInfo());
	}
}
