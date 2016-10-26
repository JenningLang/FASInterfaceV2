package FASInterfaceMain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import FAS.*;
import FCMP.RecvMessage;
import FCMP.SendMessage;
import fasException.*;
import fasMessage.*;
import fasUtil.*;
import fcmpCommunication.FCMPChannel;
import Enum.*;

public class FASInterfaceMachine implements Runnable{
	private SiemensFAS fireAlarmSystem;
	private FCMPChannel FCMPChan;
	private Thread fcmpReceiveThread;
	
	private BlockingQueue<RecvMessage> fcmpMQ = FCMPChannel.getMessageQueue();
	private Logger logger = FASInterfaceMain.FASLogger;

	public FASInterfaceMachine(){}

	public void fireAlarmSystemConfig() throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigFASNodeException{
		// 创建 FAS 对象
		try{
			fireAlarmSystem = new SiemensFAS();
		}catch(FASLocalDeviceInitException | FASRemoteDeviceConnException | ConfigFASNodeException e){
			fireAlarmSystem.getFasCommChan().closeCommChannel();
			throw e;
		}
	}
	public void fcmpConfig() throws FCMPInitialException{
		// 创建 FCMP 对象
		try{
			FCMPChan = new FCMPChannel();
		}catch(FCMPInitialException e){
			throw e;
		}
		// 运行FCMP接收数据线程
		fcmpReceiveThread = new Thread(FCMPChan, "FCMPReceiveThread");
		fcmpReceiveThread.start();
	}
	
	/**
	 * 主逻辑
	 * @author ZhenningLang
	 * */
	@Override
	public void run(){
		RecvMessage rmsg = new RecvMessage();
		while(true){
			// 收消息
			if(!fcmpMQ.isEmpty()){ // 如果不做这个判断，那么当队列为空时该进程会阻塞
				rmsg = fcmpMQ.poll(); // 消息体
				FCMP.Address rmsgAddr = rmsg.getAddress();  // 消息地址
				String rmsgStr = rmsg.getMsg(); // 消息内容，String
				logger.debug("Message received from: " + rmsgAddr.toString());
				logger.debug("Message content: " + rmsgStr);
				// 消息内容预处理，将消息变为对应的类
				FASMessage rmsgClass = null;
				try{
					if(rmsgStr.contains(MsgTypeEnum.ConfigReq.toString())){
						rmsgClass = JsonUtil.fromJSON(rmsgStr, ConfigReqMsg.class);
					}else if(rmsgStr.contains(MsgTypeEnum.StatusReq.toString())){
						rmsgClass = JsonUtil.fromJSON(rmsgStr, StatusReqMsg.class);
					}else{
						throw new Exception();
					}
				}catch(Exception e){
					// 发回错误信息
					sendMessage(JsonUtil.toJSON(new ErrorInfoMsg("Error Info: Bad request!: " + rmsgStr)), 
							rmsgAddr);
					logger.info("Error Info: Bad request!: " + rmsgStr);
					logger.error(e.getMessage(), e);
					continue;
				}
				/*** 消息已经被转为类，分类处理 --- 主逻辑 ***/
				if(rmsgClass.getClass().equals(ConfigReqMsg.class)){ // 配置信息请求处理
					for(FASNode node : fireAlarmSystem.getFasNodeList()){
						ConfigReplyMsg smsgClass = 
								new ConfigReplyMsg(node.getNodeType(), node.getNodeID(), node.getNodeDescription(), 
										node.getFatherNodeID(), node.getChildNodeIDList());
						sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr);
					}
					// 发送 Finish
					ConfigReplyMsg smsgClass = new ConfigReplyMsg(NodeEnum.Finish, null, null, null, new ArrayList<String>());
					sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr);
				}else if(rmsgClass.getClass().equals(StatusReqMsg.class)){ // 区域报警 和 设备故障 信息请求处理
					// 刷新信息
					try{
						fireAlarmSystem.refreshLocalFAS();
					}catch(ConfigFASNodeException e){
						// 发回错误信息
						sendMessage(JsonUtil.toJSON(new ErrorInfoMsg("ConfigFASNodeException")), 
								rmsgAddr);
						logger.info("ConfigFASNodeException");
						logger.error(e.getMessage(), e);
						continue;
					}
					// 逐条发送 node 状态信息
					for(FASNode node : fireAlarmSystem.getFasNodeList()){
						StatusReplyMsg smsgClass = new StatusReplyMsg(node.getNodeType(), 
								node.getNodeID(), 
								node.getNodeStatus());
						sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr);
					}
					// 发送 Finish
					StatusReplyMsg smsgClass = new StatusReplyMsg(NodeEnum.Finish, null, null);
					sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr);
				}
			}
		}
	}
	
	/**
	 * 封装了发送消息过程
	 * */
	private void sendMessage(String msgStr, FCMP.Address addr){
		SendMessage smsg = new SendMessage();
		// address
		List<FCMP.Address> addrList = new LinkedList<FCMP.Address>();
		addrList.add(addr);
		smsg.setAddresses(addrList);
		// content
		JsonMessageBean jmb = new JsonMessageBean();
		jmb.setFrom("FAS");
		jmb.setType("");
		jmb.setTime("");
		jmb.setContent(msgStr);
		smsg.setMsg(JsonUtil.toJSON(jmb));
		// send
		FCMPChan.getCommChannel().sendMsg(smsg);
	} 
	
	// getters
	public SiemensFAS getFireAlarmSystem() {
		return fireAlarmSystem;
	}
	
	public FCMPChannel getFCMPChan() {
		return FCMPChan;
	}
	
	public Thread getFCMPReceiveThread(){
		return fcmpReceiveThread;
	}
}