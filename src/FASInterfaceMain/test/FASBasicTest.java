package FASInterfaceMain.test;

import java.util.ArrayList;
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
import com.serotonin.bacnet4j.service.unconfirmed.IAmRequest;
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
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RequestUtils;

import FAS.FASDevice;
import FAS.FASZone;
import FAS.SiemensFAS;
import FAS.SibX.SiemensConfig;
import FCMP.*;
// FCMP.Address will collide with another package, so just use FCMP.Address instead
// import FCMP.Address; 
import FCMP.Communication;
import FCMP.SendMessage;
import FCMP.RecvMessage;
public class FASBasicTest {

	public static void main(String[] args)
	{	
		IpNetwork network = new IpNetwork("192.168.200.8");
		Transport transport  = new Transport(network);
		LocalDevice ld = new LocalDevice(200 , transport);
		try{
			ld.initialize();
			System.out.println("I-AM broadcasting!");
			ld.sendGlobalBroadcast(new IAmRequest(new ObjectIdentifier(ObjectType.device, 200),
					new UnsignedInteger(1024),
					Segmentation.segmentedBoth ,
					new UnsignedInteger(1))
							);
			Thread.sleep(1000); // 给FAS主机一段时间做地址绑定
			/* 
			 * 第一次通信必定会出错 
			 */
			RemoteDevice rd = ld.findRemoteDevice(new Address("192.168.200.1"
		    		, 0xbac0), null, 1);
			ReadPropertyAck ack = (ReadPropertyAck)ld.send(
			    	rd, 
			    	new ReadPropertyRequest(
			    			new ObjectIdentifier(ObjectType.device, 1), 
			    			PropertyIdentifier.description)
			    	);
			System.out.println(ack.getValue());
			Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			ld.terminate();
			System.out.println("Initial finished!");
	    }
		try{
			ld.initialize();
			RemoteDevice rd = ld.findRemoteDevice(new Address("192.168.200.1"
		    		, 0xbac0), null, 1);
			ReadPropertyAck ack = (ReadPropertyAck)ld.send(
			    	rd, 
			    	new ReadPropertyRequest(
			    			new ObjectIdentifier(ObjectType.lifeSafetyPoint, 531), 
			    			PropertyIdentifier.trackingValue)
			    	);
			Encodable e = ack.getValue();
			String s = e.toString();
			System.out.println(s);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			ld.terminate();
			System.out.println("Read property finished!");
	    }
		
	}

}
