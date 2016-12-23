package FASInterfaceMain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import fasEnum.*;

import com.InterConnect.RecvMessage;
import com.InterConnect.SendMessage;

import fas.*;
import fasException.*;
import fasMessage.*;
import fasUtil.*;
import fcmp.FCMPChannel;

public class FASInterfaceMachine implements Runnable{
	private SiemensFAS fireAlarmSystem;
	private FCMPChannel FCMPChan;
	private Thread fcmpReceiveThread;
	
	private BlockingQueue<RecvMessage> fcmpMQ = FCMPChannel.getMessageQueue();
	private Logger logger = FASInterfaceMain.FASLogger;

	public FASInterfaceMachine(){}

	public void fireAlarmSystemConfig() throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigFASNodeException{
		// ���� FAS ����
		try{
			fireAlarmSystem = new SiemensFAS();
		}catch(FASLocalDeviceInitException | FASRemoteDeviceConnException | ConfigFASNodeException e){
			fireAlarmSystem.getFasCommChan().closeCommChannel();
			throw e;
		}
	}
	public void fcmpConfig() throws FCMPInitialException{
		// ���� FCMP ����
		try{
			FCMPChan = new FCMPChannel();
		}catch(FCMPInitialException e){
			throw e;
		}
		// ����FCMP���������߳�
		fcmpReceiveThread = new Thread(FCMPChan, "FCMPReceiveThread");
		fcmpReceiveThread.start();
	}
	
	/**
	 * ���߼�
	 * @author ZhenningLang
	 * */
	@Override
	public void run(){
		RecvMessage rmsg = new RecvMessage();
		while(true){
			// ����Ϣ
			if(!fcmpMQ.isEmpty()){ // �����������жϣ���ô������Ϊ��ʱ�ý��̻�����
				rmsg = fcmpMQ.poll(); // ��Ϣ��
				com.InterConnect.Address rmsgAddr = rmsg.getAddress();  // ��Ϣ��ַ
				String rmsgStr = rmsg.getMsg(); // ��Ϣ���ݣ�String
				logger.debug("Message received from: " + rmsgAddr.toString());
				logger.debug("Message content: " + rmsgStr);
				// ��Ϣ����Ԥ��������Ϣ��Ϊ��Ӧ����
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
					// ���ش�����Ϣ
					sendMessage(JsonUtil.toJSON(new ErrorInfoMsg("Error Info: Bad request!: " + rmsgStr)), 
							rmsgAddr, 
							"error");
					logger.info("Error Info: Bad request!: " + rmsgStr);
					logger.error(e.getMessage(), e);
					continue;
				}
				/*** ��Ϣ�Ѿ���תΪ�࣬���ദ�� --- ���߼� ***/
				if(rmsgClass.getClass().equals(ConfigReqMsg.class)){ // ������Ϣ������
					for(FASNode node : fireAlarmSystem.getFasNodeList()){
						ConfigReplyMsg smsgClass = 
								new ConfigReplyMsg(node.getNodeType(), node.getNodeID(), node.getNodeDescription(), 
										node.getFatherNodeID(), node.getChildNodeIDList());
						sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr, "config");
					}
					// ���� Finish
					ConfigReplyMsg smsgClass = new ConfigReplyMsg(NodeEnum.Finish, null, null, null, new ArrayList<String>());
					sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr, "config");
				}else if(rmsgClass.getClass().equals(StatusReqMsg.class)){ // ���򱨾� �� �豸���� ��Ϣ������
					// ˢ����Ϣ
					try{
						fireAlarmSystem.refreshLocalFAS();
					}catch(ConfigFASNodeException e){
						// ���ش�����Ϣ
						sendMessage(JsonUtil.toJSON(new ErrorInfoMsg("ConfigFASNodeException")), 
								rmsgAddr,
								"error");
						logger.info("ConfigFASNodeException");
						logger.error(e.getMessage(), e);
						continue;
					}
					// �������� node ״̬��Ϣ
					for(FASNode node : fireAlarmSystem.getFasNodeList()){
						StatusReplyMsg smsgClass = new StatusReplyMsg(node.getNodeType(), 
								node.getNodeID(), 
								node.getNodeStatus());
						sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr, "status");
					}
					// ���� Finish
					StatusReplyMsg smsgClass = new StatusReplyMsg(NodeEnum.Finish, null, null);
					sendMessage(JsonUtil.toJSON(smsgClass), rmsgAddr, "status");
				}
			}
		}
	}
	
	/**
	 * ��װ�˷�����Ϣ����
	 * */
	private void sendMessage(String msgStr, com.InterConnect.Address addr, String msgType){
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {}
		SendMessage smsg = new SendMessage();
		// address
		List<com.InterConnect.Address> addrList = new LinkedList<com.InterConnect.Address>();
		addrList.add(addr);
		smsg.setAddresses(addrList);
		// content
		JsonMessageBean jmb = new JsonMessageBean();
		jmb.setFrom("FAS");
		jmb.setType(msgType);
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