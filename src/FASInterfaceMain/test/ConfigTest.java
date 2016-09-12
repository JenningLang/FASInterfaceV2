package FASInterfaceMain.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.enums.MaxApduLength;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.acknowledgement.AcknowledgementService;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyMultipleRequest;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.constructed.ReadAccessSpecification;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.RequestUtils;

import FAS.FASDevice;
import FAS.FASZone;
import FAS.SibX.SiemensConfig;
import FCMP.*;
// FCMP.Address will collide with another package, so just use FCMP.Address instead
// import FCMP.Address; 
import FCMP.Communication;
import FCMP.SendMessage;
import FCMP.RecvMessage;
public class ConfigTest {

	public static void main(String[] args) throws Exception
	{	
		List<FASZone> localFASZone; // 区间列表
		List<FASDevice> localFASDevice; // 设备列表
		localFASZone = new ArrayList<FASZone>();
		localFASDevice = new ArrayList<FASDevice>();
		try{
			SiemensConfig.configZoneAndDevice(localFASZone, localFASDevice);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(localFASZone.size());
		System.out.println(localFASDevice.size());
		for(Iterator<FASZone> it = localFASZone.iterator(); it.hasNext();){
			System.out.println(((FASZone)it.next()).toString());
		}
		for(Iterator<FASDevice> it = localFASDevice.iterator(); it.hasNext();){
			System.out.println(((FASDevice)it.next()).toString());
		}
	}

}
