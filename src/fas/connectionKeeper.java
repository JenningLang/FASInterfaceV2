package fas;

import org.apache.log4j.Logger;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.service.acknowledgement.AcknowledgementService;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public class connectionKeeper implements Runnable{
	
	private int handInterval;
	private LocalDevice ld;
	private RemoteDevice rd;
	private int rdInstanceNumber;
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;

	public connectionKeeper(LocalDevice ld, RemoteDevice rd, int rdInstanceNumber) {
		this(25000, ld, rd, rdInstanceNumber); // 25s
	}

	public connectionKeeper(int handInterval, LocalDevice ld, RemoteDevice rd, int rdInstanceNumber) {
		super();
		this.handInterval = handInterval;
		this.ld = ld;
		this.rd = rd;
		this.rdInstanceNumber = rdInstanceNumber;
	}
	
	@Override
	public void run(){
		while(true){
			try {
		    @SuppressWarnings("unused")
			AcknowledgementService ack = ld.send(
		    		rd, 
		    		new ReadPropertyRequest(
		    				new ObjectIdentifier(ObjectType.device, rdInstanceNumber), 
		    				PropertyIdentifier.description)
		    		);
				Thread.sleep((long)handInterval);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
				logger.error("Bad connection with FAS panel!");
			}
		}
	}
	
	public int getHandInterval() {
		return handInterval;
	}

	public void setHandInterval(int handInterval) {
		this.handInterval = handInterval;
	}

	public LocalDevice getLd() {
		return ld;
	}

	public void setLd(LocalDevice ld) {
		this.ld = ld;
	}

	public RemoteDevice getRd() {
		return rd;
	}

	public void setRd(RemoteDevice rd) {
		this.rd = rd;
	}

	public int getRdInstanceNumber() {
		return rdInstanceNumber;
	}

	public void setRdInstanceNumber(int rdInstanceNumber) {
		this.rdInstanceNumber = rdInstanceNumber;
	}
	
}
