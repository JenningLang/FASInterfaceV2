package FAS;

import com.FASRecoveryMsg.Enum.*;


public class FASZone {
	private String zoneID;
	private String zoneDescription;
	private ZoneAndDeviceStatusEnum alarmStatus;
	private int instantNumber; // BACnetÊµÀýºÅ
	
	public FASZone(String zoneID, String zoneDescription, ZoneAndDeviceStatusEnum alarmStatus, int instantNumber) {
		super();
		this.zoneID = zoneID;
		this.zoneDescription = zoneDescription;
		this.alarmStatus = alarmStatus;
		this.instantNumber = instantNumber;
	}
	
	public int getInstantNumber() {
		return instantNumber;
	}
	public void setInstantNumber(int instantNumber) {
		this.instantNumber = instantNumber;
	}
	public String getZoneID(){
		return this.zoneID;
	}
	public void setZoneID(String value){
		this.zoneID = value;
	}
	public String getZoneDescription(){
		return this.zoneDescription;
	}
	public void setZoneDescription(String value){
		this.zoneDescription = value;
	}
	public ZoneAndDeviceStatusEnum getAlarmStatus(){
		return this.alarmStatus;
	}
	public void setAlarmStatus(ZoneAndDeviceStatusEnum value){
		this.alarmStatus = value;
	}
	
	@Override
	public String toString() {
		return "FASZone [zoneID=" + zoneID + ", zoneDescription=" + zoneDescription + ", alarmStatus=" + alarmStatus
				+ ", instantNumber=" + instantNumber + "]";
	}
	
}
