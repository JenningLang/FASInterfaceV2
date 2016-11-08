package fcmpCommunication;

import org.apache.log4j.Logger;

import FCMP.Communication;
import fasException.FCMPInitialException;
import fasUtil.ConfigUtil;
import FCMP.RecvMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FCMPChannel implements Runnable{
	
	private Communication commChannel;
	// 消息队列，Creates a LinkedBlockingQueue with a capacity of Integer.MAX_VALUE = 2^31 - 1
	private static BlockingQueue<RecvMessage> messageQueue = new LinkedBlockingQueue<RecvMessage>();
	
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;
	
	public FCMPChannel() throws FCMPInitialException{
		commChannel = new Communication(ConfigUtil.getFCMPLocalPort(), ConfigUtil.getFCMPRemotePort());
		boolean commInitResult = commChannel.initialize();
		if(!commInitResult){
			logger.info("FCMP initial failed!");
			throw new FCMPInitialException();
		}
	}

	/**
	 * 
	 * 接收消息线程
	 * 
	 * */
	@Override
	public void run() {
		logger.info("FCMP receiving thread starts...");
		while(true){
			RecvMessage rmsg = new RecvMessage();
			if(commChannel.recvMsg(rmsg)){
				messageQueue.add(rmsg);
				logger.debug(rmsg.getAddress());
				logger.debug(rmsg.getMsg());
			}
		}
		
	}
	
	/**
	 * 关闭FCMP通信
	 * */
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
	
	public static BlockingQueue<RecvMessage> getMessageQueue(){
		return messageQueue;
	}

}
