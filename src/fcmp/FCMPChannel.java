package fcmp;

import org.apache.log4j.Logger;

import com.InterConnect.Communication;
import com.InterConnect.RecvMessage;

import fasException.FCMPInitialException;
import fasUtil.ConfigUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FCMPChannel implements Runnable{
	
	private Communication commChannel;
	// ��Ϣ���У�Creates a LinkedBlockingQueue with a capacity of Integer.MAX_VALUE = 2^31 - 1
	private static BlockingQueue<RecvMessage> messageQueue = new LinkedBlockingQueue<RecvMessage>();
	
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;
	
	public FCMPChannel() throws FCMPInitialException{
		commChannel = new Communication(ConfigUtil.getFCMPLocalPort(), ConfigUtil.getFCMPRemotePort());
		boolean commInitResult = commChannel.initialize();
		if(!commInitResult){
			logger.info("FCMP initial failed!");
			throw new FCMPInitialException();
		}
		// TODO: ��� FCMP ���ݣ���Ҫ��Ϊ�˷�ֹ�ӿڻ��ȷ�������������FCMP�л����˴�����ʷ����
		//��ʱ��д����Ϊ FCMP û���ṩ�������
	}

	/**
	 * 
	 * ������Ϣ�߳�
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
	 * �ر�FCMPͨ��
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
