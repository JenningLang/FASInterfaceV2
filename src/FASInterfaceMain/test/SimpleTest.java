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

import FAS.SibX.SiemensConfig;
import FCMP.*;
// FCMP.Address will collide with another package, so just use FCMP.Address instead
// import FCMP.Address; 
import FCMP.Communication;
import FCMP.SendMessage;
import FCMP.RecvMessage;
public class SimpleTest {

	public static void main(String[] args) throws Exception
	{	
		// BACnet test
//        IpNetwork network = new IpNetwork();
//        Transport transport = new Transport(network);
//        LocalDevice ld = new LocalDevice(200, transport);
//        ld.initialize();
//        RemoteDevice rd = ld.findRemoteDevice(new Address("192.168.200.1", 0xbac0), null, 1);
//
//        AcknowledgementService ack = ld.send(rd, new ReadPropertyRequest(
//        		new ObjectIdentifier(ObjectType.lifeSafetyZone, 459), PropertyIdentifier.description)
//        		);
//        System.out.println(ack);
//        ld.terminate();
		
		// FCMP test
//		Communication co = new Communication(8001, 8002);
//		co.initialize();
//		FCMP.Address addr = new FCMP.Address((byte)8, (byte)2, (short)0, (byte)1, (short)1);
//		List<FCMP.Address> addrList = new LinkedList<FCMP.Address>();;
//		addrList.add(addr);
//		SendMessage msg = new SendMessage("Hello the World!", addrList);
//		//
//		Communication coServer = new Communication(8003, 8004);
//		coServer.initialize();
//		RecvMessage rmsg = new RecvMessage();
//		while(true){
//			// Thread.sleep(5000);
//			co.sendMsg(msg);
//			coServer.recvMsg(rmsg);
//			co.sendMsg(msg);
//			msg.setMsg("Another");
//			co.sendMsg(msg);
//			coServer.recvMsg(rmsg);
//			coServer.recvMsg(rmsg);
//			co.close();
//			coServer.close();
//		}
	}

}
