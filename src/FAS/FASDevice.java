package FAS;

import com.FASRecoveryMsg.Enum.*;

public class FASDevice {
	private String deviceID;
	private String deviceDescription;
	private ZoneAndDeviceStatusEnum faultStatus;
	private int instantNumber; // BACnet实例号
//	private String fatherZoneID; // 所属区域的ID号
	
	public FASDevice(String deviceID, String deviceDescription, ZoneAndDeviceStatusEnum faultStatus, int instantNumber) {
		super();
		this.deviceID = deviceID;
		this.deviceDescription = deviceDescription;
		this.faultStatus = faultStatus;
		this.instantNumber = instantNumber;
	}

	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public String getDeviceDescription() {
		return deviceDescription;
	}
	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}
	public ZoneAndDeviceStatusEnum getFaultStatus() {
		return faultStatus;
	}
	public void setFaultStatus(ZoneAndDeviceStatusEnum faultStatus) {
		this.faultStatus = faultStatus;
	}
	public int getInstantNumber() {
		return instantNumber;
	}
	public void setInstantNumber(int instantNumber) {
		this.instantNumber = instantNumber;
	}

	@Override
	public String toString() {
		return "FASDevice [deviceID=" + deviceID + ", deviceDescription=" + deviceDescription + ", faultStatus="
				+ faultStatus + ", instantNumber=" + instantNumber + "]";
	}
}
