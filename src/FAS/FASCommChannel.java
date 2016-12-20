package FAS;

import org.apache.log4j.Logger;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.acknowledgement.AcknowledgementService;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.IAmRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import fasException.*;

public class FASCommChannel {
	
	private String siemensFASIP; // FAS 主机 ip
	private int siemensFASID; // FAS 主机的 BACnet ID
	private String interfaceFASIP; // 接口机 ip
	private int interfaceFASPort; // 接口机的端口号
	private int interfaceFASID; // 接口机的BACnet ID
	
	private IpNetwork FASNetwork; // 接口机的网络
	private Transport FASTransport; // 接口机的Transport
	private LocalDevice interfaceFASDevice; // 将接口机视为一个FAS设备
	private RemoteDevice siemensFASDevice; // 想要连接通讯的FAS设备
	
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;
	
	public FASCommChannel(String siemensFASIP, int siemensFASID, String interfaceFASIP, int interfaceFASPort, int interfaceFASID)
			 throws FASLocalDeviceInitException, FASRemoteDeviceConnException{
		super();
		this.siemensFASIP = siemensFASIP;
		this.siemensFASID = siemensFASID;
		this.interfaceFASIP = interfaceFASIP;
		this.interfaceFASPort = interfaceFASPort;
		this.interfaceFASID = interfaceFASID;
		FASCommChannelInit();
	}
	
	/**
	 * 连接西门子主机
	 * */
	private void FASCommChannelInit() throws FASLocalDeviceInitException, FASRemoteDeviceConnException
			// throws BindException 
	{
		/* ****************** 
		 *   创建本地设备          *
		 ****************** */
	    this.FASNetwork = new IpNetwork(interfaceFASIP, interfaceFASPort, interfaceFASIP);
	    this.FASTransport = new Transport(FASNetwork);
	    this.interfaceFASDevice = new LocalDevice(interfaceFASID , FASTransport); //(int deviceId, Transport transport)
	    
		/* ****************** 
		 * 连接西门子FAS主机   *
		 ****************** */
		// 5次尝试重连
	    int reConnCounter = 1;
	    while(true){
	    	// 1. 启动本地设备
	    	try{
	    		interfaceFASDevice.initialize();
	    	}catch(Exception e){
	    		// 启动本地设备错误
	    		throw new FASLocalDeviceInitException();
	    	}
	    	
	    	// 2. 连接西门子FAS主机
			try{
		    	siemensFASDevice = interfaceFASDevice.findRemoteDevice(
			    		new Address(siemensFASIP, 0xbac0), null, siemensFASID); 
		    	break; // 成功就退出循环
			}catch(Exception e) {
				// 失败就重新连接，共尝试5次
		    	interfaceFASDevice.terminate();
		    	if(reConnCounter >= 6){
		    		throw new FASRemoteDeviceConnException();
		    	}else{
		    		reConnCounter++;
		    	}
		    	// 5秒后重新连接
		    	int t = 5;
		    	logger.info("Cannot connect the Siemens FAS device!: " 
		    			+ this.getClass().getName());
		    	logger.info("Reconnection start in " + t + "s ...");
		    	for( ;t >= 0;t--){
		    		logger.info(t);
		    		try{
		    			Thread.sleep(1000);
		    		}catch(Exception ee){}
		    	}
		    	logger.info("Reconnecting the Siemens FAS device...");
		    }
		}
	    
		/* ************************************
		 *   通信初始化: 广播 I am              *
		 *   首次查询 device description       *
		 ************************************ */
	    
		// 通信初始化: 广播 I am
    	logger.debug("Initialing the communication with Siemens FAS panel...");
    	logger.debug("Broadcasting: I am Device " + interfaceFASID);
	    try{
	    	interfaceFASDevice.sendGlobalBroadcast(
					new IAmRequest(new ObjectIdentifier(ObjectType.device, interfaceFASID), 
					new UnsignedInteger(1024),
		            Segmentation.segmentedBoth , 
		            new UnsignedInteger(1))
			);
			Thread.sleep(1000); // 给FAS主机一段时间做客户端地址绑定
	    }catch(Exception e){}
	    
		// 首次查询 device description，如果是第一次启动FAS报警主机必然异常！！！
	    // 异常后什么都不需要处理，只需要重启本地设备即可
		try{
			logger.debug("Tring the first property reading from the Siemens FAS panel!");
			@SuppressWarnings("unused")
			AcknowledgementService ack = interfaceFASDevice.send(
					siemensFASDevice, 
			    	new ReadPropertyRequest(
			    			new ObjectIdentifier(ObjectType.device, siemensFASID), 
			    			PropertyIdentifier.description)
			    	);
			Thread.sleep(100);
		}
		catch(Exception e){
			logger.debug(e.getMessage(),e);
			interfaceFASDevice.terminate();
	    	try{
	    		interfaceFASDevice.initialize(); // 启动本地设备
	    	}catch(Exception ee){
	    		// 启动本地设备错误
	    		throw new FASLocalDeviceInitException();
	    	}
		}
	}

	/**
	 * Free all variables
	 * */
	public void closeCommChannel(){
		if(interfaceFASDevice != null){
			interfaceFASDevice.terminate();
			FASTransport.terminate();
			FASNetwork.terminate();
		}
	}
	
	// getters
	public LocalDevice getInterfaceFASDevice(){ 
		return this.interfaceFASDevice; 
	}
	public RemoteDevice getSiemensFASDevice(){ 
		return this.siemensFASDevice; 
	}
}
