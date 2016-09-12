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
	private String siemensFASIP; // FAS 主机 ip
	private int siemensFASID; // FAS 主机的 BACnet ID
	private String interfaceFASIP; // 接口机 ip
	private int interfaceFASID; // 接口机的BACnet ID
	
	private List<FASZone> localFASZone; // 区间列表
	boolean hasZoneInit = false; // 是否已经从xml文档中读取了zone 配置
	private List<FASDevice> localFASDevice; // 设备列表
	boolean hasDeviceInit = false; // 是否已经从xml文档中读取了device 配置
	
	private FASCommChannel fasCommChan; // 存储了 network, transport, siemensFASDevice 和 interfaceFASDevice 对象
	
	private Logger logger = FASInterfaceMain.FASLogger;
	
	// 构造函数
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
	
	// 设备连接，SibX文件读取，状态变量初始化
	public void SiemensFASInit(String interfaceFASIP, int interfaceFASID, String siemensFASIP, int siemensFASID) 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigZoneAndDeviceException{
		/* ************************************************ 
		 *        与 FAS 主机通信信道创建于初始化                              *
		 ************************************************ */
		fasCommChan = new FASCommChannel(siemensFASIP, siemensFASID, interfaceFASIP, interfaceFASID); 

		/* ************************************************ 
		 * 通过 FASCommChannel 和西门子xml文档来获取设备信息  *
		 * 初始化 localFASZone 和 LocalFASDevice            *
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
		 * 刷新报警和故障信息   *
		 ******************* */
		LocalFASRefresh();
	}
	
	// 通过 FASCommChannel 和西门子xml文档刷新区间和设备信息
	public void LocalFASRefresh(){
		LocalFASZoneRefresh();
		LocalFASDeviceRefresh();
	}
	public void LocalFASZoneRefresh(){
		// 通过 FASCommChannel 和西门子xml文档刷新区间信息
		if(!hasZoneInit){ // 当区间分区信息未加载
			try {
				SiemensConfig.configZoneAndDevice(localFASZone, localFASDevice);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
			hasZoneInit = true;
			hasDeviceInit = true;
		}
		// 分区火警状态刷新
		localFASZone.forEach(zone -> zone.setAlarmStatus(getZoneStatusByInstantNumber(zone.getInstantNumber())));
	}
	public void LocalFASDeviceRefresh(){
		// 通过 FASCommChannel 和西门子xml文档刷新设备信息
		if(!hasDeviceInit){ // 当设备信息未加载
			try {
				SiemensConfig.configZoneAndDevice(localFASZone, localFASDevice);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
			hasZoneInit = true;
			hasDeviceInit = true;
		}
		// 设备故障状态刷新
		localFASDevice.forEach(device -> device.setFaultStatus(getDeviceStatusByInstantNumber(device.getInstantNumber())));
	}

	// 读取区间或设备状态
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
		// 实时的读取FAS区间的报警信息
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
		// 实时的读取FAS设备的故障信息
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