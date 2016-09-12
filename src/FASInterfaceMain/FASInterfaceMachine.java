package FASInterfaceMain;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import FAS.*;
import FASException.*;
import FCMP.RecvMessage;
import FCMP.SendMessage;
import communication.FCMPChannel;
import com.FASRecoveryMsg.Message.*;
import fasUtil.*;

import com.FASRecoveryMsg.Enum.*;

public class FASInterfaceMachine implements Runnable{
	private SiemensFAS fireAlarmSystem;
	private FCMPChannel FCMPChan;
	private Logger logger = FASInterfaceMain.FASLogger;

	/**
	 * ����֮���Բ�׽�쳣���ٴ��׳�����Ϊ�˷�ֹ������һ��ʱ��������һ�㺯���޷��ͷ��Ѿ���ʼ������Դ
	 * */
	public FASInterfaceMachine() 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigZoneAndDeviceException, 
			FCMPInitialException{
		// ���� FAS ����
		try{
			fireAlarmSystem = new SiemensFAS();
		}catch(FASLocalDeviceInitException | FASRemoteDeviceConnException | ConfigZoneAndDeviceException e){
			fireAlarmSystem.getFasCommChan().closeCommChannel();
			throw e;
		}
		// ���� FCMP ����
		try{
			FCMPChan = new FCMPChannel();
		}catch(FCMPInitialException e){
			fireAlarmSystem.getFasCommChan().closeCommChannel();
			throw e;
		}
	}
	
	@Override
	public void run(){
		List<FCMP.Address> addrList = new LinkedList<FCMP.Address>();
		RecvMessage rmsg = new RecvMessage();
		
		while(true){
			addrList.clear(); // ��Ȼֻ��һ����ַ�����÷����������ַ��List������ÿ�δ��䶼��Ӻ����List
			// ����Ϣ
			if(FCMPChan.getCommChannel().recvMsg(rmsg)){
				// ��Ϣ�ľ��崦�����
				String smsgContent;
				JsonMessageBean jmb = new JsonMessageBean();
				
				FCMP.Address rmsgAddr = rmsg.getAddress();
				String rmsgStr = rmsg.getMsg();
				logger.debug("Message received from: " + 
						rmsgAddr.toString());
				logger.debug("Message content: " + 
						rmsgStr);
				RecoveryMessage rmsgClass = RecoveryMessage.recoveryFromString(rmsgStr);
				if(rmsgClass != null && rmsgClass.getFrom() == MsgFromEnum.Server){
					// ���ظ� server �� message
					SendMessage smsg = new SendMessage();
					addrList.add(rmsg.getAddress());
					smsg.setAddresses(addrList);
					/* ****************
					 * 
					 * ������Ϣ������
					 * 
					 * ****************/
					if(rmsgClass.getType() == MsgTypeEnum.SysConfigReq){
						for(FASZone zone : fireAlarmSystem.getLocalFASZone()){
							SysConfigReplyMsg smsgClass = new SysConfigReplyMsg(
										rmsgClass.getFlag(), 
										NodeEnum.Zone,
										zone.getZoneID(),
										zone.getZoneDescription()
									);
							smsgContent = smsgClass.object2String();
							jmb.setFrom("FAS");
							jmb.setType("");
							jmb.setTime("");
							jmb.setContent(smsgContent);
							smsg.setMsg(JsonUtil.toJSON(jmb));
							FCMPChan.getCommChannel().sendMsg(smsg);
						}
						for(FASDevice device : fireAlarmSystem.getLocalFASDevice()){
							SysConfigReplyMsg smsgClass = new SysConfigReplyMsg(
										rmsgClass.getFlag(), 
										NodeEnum.Device,
										device.getDeviceID(),
										device.getDeviceDescription()
									);
							smsgContent = smsgClass.object2String();
							jmb.setFrom("FAS");
							jmb.setType("");
							jmb.setTime("");
							jmb.setContent(smsgContent);
							smsg.setMsg(JsonUtil.toJSON(jmb));
							FCMPChan.getCommChannel().sendMsg(smsg);
						}
						// ���� Finish
						SysConfigReplyMsg smsgClass = new SysConfigReplyMsg(
								rmsgClass.getFlag(), 
								NodeEnum.Finish,
								null,
								null
								);
						smsgContent = smsgClass.object2String();
						jmb.setFrom("FAS");
						jmb.setType("");
						jmb.setTime("");
						jmb.setContent(smsgContent);
						smsg.setMsg(JsonUtil.toJSON(jmb));
						FCMPChan.getCommChannel().sendMsg(smsg);
					}
					/* ******************************
					 * 
					 * ���򱨾� �� �豸���� ��Ϣ������
					 * 
					 * *****************************/
					else if(rmsgClass.getType() == MsgTypeEnum.StatusReq){
						// ˢ����Ϣ
						fireAlarmSystem.LocalFASRefresh();
						for(FASZone zone : fireAlarmSystem.getLocalFASZone()){
							StatusReplyMsg smsgClass = new StatusReplyMsg(
										rmsgClass.getFlag(), 
										NodeEnum.Zone,
										zone.getZoneID(),
										zone.getAlarmStatus()
									);
							smsgContent = smsgClass.object2String();
							jmb.setFrom("FAS");
							jmb.setType("");
							jmb.setTime("");
							jmb.setContent(smsgContent);
							smsg.setMsg(JsonUtil.toJSON(jmb));
							FCMPChan.getCommChannel().sendMsg(smsg);
						}
						for(FASDevice device : fireAlarmSystem.getLocalFASDevice()){
							StatusReplyMsg smsgClass = new StatusReplyMsg(
										rmsgClass.getFlag(), 
										NodeEnum.Device,
										device.getDeviceID(),
										device.getFaultStatus()
									);
							smsgContent = smsgClass.object2String();
							jmb.setFrom("FAS");
							jmb.setType("");
							jmb.setTime("");
							jmb.setContent(smsgContent);
							smsg.setMsg(JsonUtil.toJSON(jmb));
							FCMPChan.getCommChannel().sendMsg(smsg);
						}
						// ���� Finish
						StatusReplyMsg smsgClass = new StatusReplyMsg(
								rmsgClass.getFlag(), 
								NodeEnum.Finish,
								null,
								null
								);
						smsgContent = smsgClass.object2String();
						jmb.setFrom("FAS");
						jmb.setType("");
						jmb.setTime("");
						jmb.setContent(smsgContent);
						smsg.setMsg(JsonUtil.toJSON(jmb));
						FCMPChan.getCommChannel().sendMsg(smsg);
					} else{
						logger.warn(
								"A wrong request " + rmsgStr + " from the server:" + rmsgAddr);
					}
				}
			}
		}
	}
	public SiemensFAS getFireAlarmSystem() {
		return fireAlarmSystem;
	}
	
	public FCMPChannel getFCMPChan() {
		return FCMPChan;
	}
}