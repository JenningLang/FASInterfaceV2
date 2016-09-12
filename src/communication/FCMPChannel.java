package communication;

import org.apache.log4j.Logger;

import FASException.FCMPInitialException;
import FCMP.Communication;
import fasUtil.ConfigUtil;

public class FCMPChannel{
	
	private Communication commChannel;
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;
	
	public FCMPChannel() throws FCMPInitialException{
		commChannel = new Communication(ConfigUtil.getFCMPLocalPort(), ConfigUtil.getFCMPRemotePort());
		boolean commInitResult = commChannel.initialize();
		if(!commInitResult){
			logger.info("FCMP initial failed!");
			throw new FCMPInitialException();
		}
	}
	
	public void closeCommChannel(){
		if(commChannel != null){
			commChannel.close();
		}
	}
	
	public FCMPChannel(int port1, int port2){
		commChannel = new Communication(port1, port2);
		commChannel.initialize();
	}
	
	public Communication getCommChannel(){
		return this.commChannel;
	}
}
