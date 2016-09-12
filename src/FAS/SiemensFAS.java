package FAS;

import FASInterfaceMain.*;
import fasUtil.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.FASRecoveryMsg.Enum.*;
import FAS.SibX.SiemensConfig;
import FASException.*;

public class SiemensFAS {
	private String siemensFASIP; // FAS ���� ip
	private int siemensFASID; // FAS ������ BACnet ID
	private String interfaceFASIP; // �ӿڻ� ip
	private int interfaceFASID; // �ӿڻ���BACnet ID
	
	private List<FASZone> localFASZone; // �����б�
	boolean hasZoneInit = false; // �Ƿ��Ѿ���xml�ĵ��ж�ȡ��zone ����
	private List<FASDevice> localFASDevice; // �豸�б�
	boolean hasDeviceInit = false; // �Ƿ��Ѿ���xml�ĵ��ж�ȡ��device ����
	
	private FASCommChannel fasCommChan; // �洢�� network, transport, siemensFASDevice �� interfaceFASDevice ����
	
	private Logger logger = FASInterfaceMain.FASLogger;
	
	// ���캯��
	public SiemensFAS() 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigZoneAndDeviceException{
		siemensFASIP = ConfigUtil.getSiemensFASIP();
		interfaceFASIP = ConfigUtil.getInterfaceFASIP();
		siemensFASID = ConfigUtil.getSiemensFASID();
		interfaceFASID = ConfigUtil.getInterfaceFASID();
		SiemensFASInit(interfaceFASIP, interfaceFASID, siemensFASIP, siemensFASID);
	}
	public SiemensFAS(String interfaceFASIP, int interfaceFASID, String siemensFASIP, int siemensFASID) 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigZoneAndDeviceException{
		this.interfaceFASIP = interfaceFASIP;
		this.interfaceFASID = interfaceFASID;
		this.siemensFASIP = siemensFASIP;
		this.siemensFASID = siemensFASID;
		SiemensFASInit(interfaceFASIP, interfaceFASID, siemensFASIP, siemensFASID);
	}
	
	// �豸���ӣ�SibX�ļ���ȡ��״̬������ʼ��
	public void SiemensFASInit(String interfaceFASIP, int interfaceFASID, String siemensFASIP, int siemensFASID) 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigZoneAndDeviceException{
		/* ************************************************ 
		 *        �� FAS ����ͨ���ŵ������ڳ�ʼ��                              *
		 ************************************************ */
		fasCommChan = new FASCommChannel(siemensFASIP, siemensFASID, interfaceFASIP, interfaceFASID); 

		/* ************************************************ 
		 * ͨ�� FASCommChannel ��������xml�ĵ�����ȡ�豸��Ϣ  *
		 * ��ʼ�� localFASZone �� LocalFASDevice            *
		 ************************************************ */
		localFASZone = new ArrayList<FASZone>();
		localFASDevice = new ArrayList<FASDevice>();
		try {
			SiemensConfig.configZoneAndDevice(localFASZone, localFASDevice);
			hasZoneInit = true;
			hasDeviceInit = true;
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new ConfigZoneAndDeviceException();
		}
		/* ******************* 
		 * ˢ�±����͹�����Ϣ   *
		 ******************* */
		LocalFASRefresh();
	}
	
	// ͨ�� FASCommChannel ��������xml�ĵ�ˢ��������豸��Ϣ
	public void LocalFASRefresh(){
		LocalFASZoneRefresh();
		LocalFASDeviceRefresh();
	}
	public void LocalFASZoneRefresh(){
		// ͨ�� FASCommChannel ��������xml�ĵ�ˢ��������Ϣ
		if(!hasZoneInit){ // �����������Ϣδ����
			try {
				SiemensConfig.configZoneAndDevice(localFASZone, localFASDevice);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
			hasZoneInit = true;
			hasDeviceInit = true;
		}
		// ������״̬ˢ��
		localFASZone.forEach(zone -> zone.setAlarmStatus(getZoneStatusByInstantNumber(zone.getInstantNumber())));
	}
	public void LocalFASDeviceRefresh(){
		// ͨ�� FASCommChannel ��������xml�ĵ�ˢ���豸��Ϣ
		if(!hasDeviceInit){ // ���豸��Ϣδ����
			try {
				SiemensConfig.configZoneAndDevice(localFASZone, localFASDevice);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
			hasZoneInit = true;
			hasDeviceInit = true;
		}
		// �豸����״̬ˢ��
		localFASDevice.forEach(device -> device.setFaultStatus(getDeviceStatusByInstantNumber(device.getInstantNumber())));
	}

	// ��ȡ������豸״̬
	public ZoneAndDeviceStatusEnum getZoneStatusByID(String zoneID)
	{
		for(FASZone zone: localFASZone){
			if(zone.getZoneID().toLowerCase() == zoneID.toLowerCase()){
				return getZoneStatusByInstantNumber(zone.getInstantNumber());
			}
		}
		return ZoneAndDeviceStatusEnum.Unknown;
	}
	public ZoneAndDeviceStatusEnum getDeviceStatusByID(String deviceID)
	{
		for( FASDevice device : localFASDevice ){
			if(device.getDeviceID().toLowerCase() == deviceID.toLowerCase()){
				return getZoneStatusByInstantNumber(device.getInstantNumber());
			}
		}
		return ZoneAndDeviceStatusEnum.Unknown;
	}
	
	/* ************************************************************************** */
	public ZoneAndDeviceStatusEnum getZoneStatusByInstantNumber(int instantNumber)
	{
		// ʵʱ�Ķ�ȡFAS����ı�����Ϣ
		try{
			ReadPropertyAck ack = (ReadPropertyAck)fasCommChan.getInterfaceFASDevice().send(
					fasCommChan.getSiemensFASDevice(), 
			    	new ReadPropertyRequest(
			    			new ObjectIdentifier(ObjectType.lifeSafetyZone, instantNumber), 
			    			PropertyIdentifier.trackingValue)
			    	);
			String value = ack.getValue().toString().trim().toLowerCase();
			logger.debug("Zone: " + instantNumber + ": " + value);
			return getStatusFromString(value);
		}catch(Exception e){
			logger.debug("In " + this.getClass().getName());
			logger.debug("Get zone status error, instance number: " + instantNumber);
			logger.debug(e.getMessage(), e);
		}
		return ZoneAndDeviceStatusEnum.Unknown;
	}
	public ZoneAndDeviceStatusEnum getDeviceStatusByInstantNumber(int instantNumber)
	{
		// ʵʱ�Ķ�ȡFAS�豸�Ĺ�����Ϣ
		try{
			ReadPropertyAck ack = (ReadPropertyAck)fasCommChan.getInterfaceFASDevice().send(
					fasCommChan.getSiemensFASDevice(), 
			    	new ReadPropertyRequest(
			    			new ObjectIdentifier(ObjectType.lifeSafetyPoint, instantNumber), 
			    			PropertyIdentifier.trackingValue)
			    	);
			String value = ack.getValue().toString().trim().toLowerCase();
			logger.debug("Device: " + instantNumber + ": " + value);
			return getStatusFromString(value);
		}catch(Exception e){
			logger.debug("In " + this.getClass().getName());
			logger.debug("Get zone status error, instance number: " + instantNumber);
			logger.debug(e.getMessage(), e);
		}
		return ZoneAndDeviceStatusEnum.Unknown;
	}
	
	private ZoneAndDeviceStatusEnum getStatusFromString(String s) {
		s = s.toLowerCase();
		if(s.contains("alarm")){
			return ZoneAndDeviceStatusEnum.Alarm;
		}else if(s.contains("fault")){
			return ZoneAndDeviceStatusEnum.Fault;
		}else if(s.contains("quiet")){
			return ZoneAndDeviceStatusEnum.Normal;
		}else{
			return ZoneAndDeviceStatusEnum.Unknown;
		}
	}
	/* ************************************************************************** */
	
	public void printFASStatus(){
		System.out.println("Status of zones:");
		printFASZoneStatus();
		System.out.println("Status of devices:");		
		printFASDeviceStatus();
	}
	
	public void printFASZoneStatus(){
		LocalFASZoneRefresh();
		localFASZone.forEach(zone -> System.out.println(zone.toString()));
	}	
	
	public void printFASDeviceStatus(){
		LocalFASDeviceRefresh();
		localFASDevice.forEach(device -> System.out.println(device.toString()));
	}
	// getters and setters
	public String getSiemensFASIP() {
		return siemensFASIP;
	}
	public void setSiemensFASIP(String siemensFASIP) {
		this.siemensFASIP = siemensFASIP;
	}
	public int getSiemensFASID() {
		return siemensFASID;
	}
	public void setSiemensFASID(int siemensFASID) {
		this.siemensFASID = siemensFASID;
	}
	public String getInterfaceFASIP() {
		return interfaceFASIP;
	}
	public void setInterfaceFASIP(String interfaceFASIP) {
		this.interfaceFASIP = interfaceFASIP;
	}
	public int getInterfaceFASID() {
		return interfaceFASID;
	}
	public void setInterfaceFASID(int interfaceFASID) {
		this.interfaceFASID = interfaceFASID;
	}
	public List<FASZone> getLocalFASZone() {
		return localFASZone;
	}
	public void setLocalFASZone(List<FASZone> localFASZone) {
		this.localFASZone = localFASZone;
	}
	public List<FASDevice> getLocalFASDevice() {
		return localFASDevice;
	}
	public void setLocalFASDevice(List<FASDevice> localFASDevice) {
		this.localFASDevice = localFASDevice;
	}
	public FASCommChannel getFasCommChan() {
		return fasCommChan;
	}
}