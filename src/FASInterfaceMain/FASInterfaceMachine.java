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
	 * 这里之所以捕捉异常并再次抛出，是为了防止创建到一半时报错，但上一层函数无法释放已经初始化的资源
	 * */
	public FASInterfaceMachine() 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigZoneAndDeviceException, 
			FCMPInitialException{
		// 创建 FAS 对象
		try{
			fireAlarmSystem = new SiemensFAS();
		}catch(FASLocalDeviceInitException | FASRemoteDeviceConnException | ConfigZoneAndDeviceException e){
			fireAlarmSystem.getFasCommChan().closeCommChannel();
			throw e;
		}
		// 创建 FCMP 对象
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
			addrList.clear(); // 虽然只有一个地址，但该方法的输入地址是List，所以每次传输都添加和清空List
			// 收消息
			if(FCMPChan.getCommChannel().recvMsg(rmsg)){
				// 消息的具体处理过程
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
					// 返回给 server 的 message
					SendMessage smsg = new SendMessage();
					addrList.add(rmsg.getAddress());
					smsg.setAddresses(addrList);
					/* ****************
					 * 
					 * 配置信息请求处理
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
						// 发送 Finish
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
					 * 区域报警 和 设备故障 信息请求处理
					 * 
					 * *****************************/
					else if(rmsgClass.getType() == MsgTypeEnum.StatusReq){
						// 刷新信息
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
						// 发送 Finish
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