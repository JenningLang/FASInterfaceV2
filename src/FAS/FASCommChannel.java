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
	
	private String siemensFASIP; // FAS ���� ip
	private int siemensFASID; // FAS ������ BACnet ID
	private String interfaceFASIP; // �ӿڻ� ip
	private int interfaceFASPort; // �ӿڻ��Ķ˿ں�
	private int interfaceFASID; // �ӿڻ���BACnet ID
	
	private IpNetwork FASNetwork; // �ӿڻ�������
	private Transport FASTransport; // �ӿڻ���Transport
	private LocalDevice interfaceFASDevice; // ���ӿڻ���Ϊһ��FAS�豸
	private RemoteDevice siemensFASDevice; // ��Ҫ����ͨѶ��FAS�豸
	
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
	 * ��������������
	 * */
	private void FASCommChannelInit() throws FASLocalDeviceInitException, FASRemoteDeviceConnException
			// throws BindException 
	{
		/* ****************** 
		 *   ���������豸          *
		 ****************** */
	    this.FASNetwork = new IpNetwork(interfaceFASIP, interfaceFASPort, interfaceFASIP);
	    this.FASTransport = new Transport(FASNetwork);
	    this.interfaceFASDevice = new LocalDevice(interfaceFASID , FASTransport); //(int deviceId, Transport transport)
	    
		/* ****************** 
		 * ����������FAS����   *
		 ****************** */
		// 5�γ�������
	    int reConnCounter = 1;
	    while(true){
	    	// 1. ���������豸
	    	try{
	    		interfaceFASDevice.initialize();
	    	}catch(Exception e){
	    		// ���������豸����
	    		throw new FASLocalDeviceInitException();
	    	}
	    	
	    	// 2. ����������FAS����
			try{
		    	siemensFASDevice = interfaceFASDevice.findRemoteDevice(
			    		new Address(siemensFASIP, 0xbac0), null, siemensFASID); 
		    	break; // �ɹ����˳�ѭ��
			}catch(Exception e) {
				// ʧ�ܾ��������ӣ�������5��
		    	interfaceFASDevice.terminate();
		    	if(reConnCounter >= 6){
		    		throw new FASRemoteDeviceConnException();
		    	}else{
		    		reConnCounter++;
		    	}
		    	// 5�����������
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
		 *   ͨ�ų�ʼ��: �㲥 I am              *
		 *   �״β�ѯ device description       *
		 ************************************ */
	    
		// ͨ�ų�ʼ��: �㲥 I am
    	logger.debug("Initialing the communication with Siemens FAS panel...");
    	logger.debug("Broadcasting: I am Device " + interfaceFASID);
	    try{
	    	interfaceFASDevice.sendGlobalBroadcast(
					new IAmRequest(new ObjectIdentifier(ObjectType.device, interfaceFASID), 
					new UnsignedInteger(1024),
		            Segmentation.segmentedBoth , 
		            new UnsignedInteger(1))
			);
			Thread.sleep(1000); // ��FAS����һ��ʱ�����ͻ��˵�ַ��
	    }catch(Exception e){}
	    
		// �״β�ѯ device description������ǵ�һ������FAS����������Ȼ�쳣������
	    // �쳣��ʲô������Ҫ����ֻ��Ҫ���������豸����
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
	    		interfaceFASDevice.initialize(); // ���������豸
	    	}catch(Exception ee){
	    		// ���������豸����
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
