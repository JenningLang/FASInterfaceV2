package FASInterfaceMain.test;

import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.OctetString;

public class SimpleTest {

	public static void main(String[] args) throws Exception
	{
		IpNetwork n = new IpNetwork();
		Transport t = new Transport(n);
		
		byte ipAddr[] = {(byte)192, (byte)168, (byte)200, (byte)1};
		Address addr = new Address(ipAddr, 47808);
		
		OctetString linkService = new OctetString("192.168.200.1");
		
		t.send(addr, linkService, 1024, null, 
				new ReadPropertyRequest(new ObjectIdentifier(ObjectType.lifeSafetyZone, 100), PropertyIdentifier.trackingValue));
	}

}
