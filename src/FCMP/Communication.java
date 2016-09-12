package FCMP;

import java.io.IOException;
import java.net.ConnectException;

import org.apache.thrift.*;  
import org.apache.thrift.protocol.*;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class Communication{
	private int remoteServerPort = 8001;
	private int selfServerPort = 8000;
	private BlockingQueue<RecvMessage> queue = new LinkedBlockingQueue();
	private TTransport transport = null;
    private TBinaryProtocol protocol = null;
    private AppToServer.Client client = null;
  
    class MsgReceiver implements Runnable{
    	private int serverPort;
    	private BlockingQueue<String>  queue = null;
    	private  TServerSocket serverTransport;  
		private	 ServerToApp.Processor  processor;
		private	 Factory protFactory;
		private	 TServer server;
		
    	public MsgReceiver(int serverPort_, BlockingQueue queue){
    		serverPort = serverPort_;
    		this.queue = queue;
    		try {
				serverTransport = new TServerSocket(serverPort);
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    		processor = new ServerToApp.Processor(new ServerToAppImpl(queue));
    		protFactory = new TBinaryProtocol.Factory(true, true); 
    		server = new TSimpleServer(new TSimpleServer.Args(serverTransport).processor(processor).protocolFactory(protFactory));  
    	}
    	
    	@Override
	     public void run(){
	    	 try{
	    		 //System.out.println("\nRecving Thread: Starting server on port : "+ serverPort);
	    		 Log.logger.info("Recving Thread: Starting server on port : " + serverPort);
			     server.serve();
			 }catch (Exception e) {  
			       //e.printStackTrace();  
				 	Log.logger.error("In running process.", e);
			 }  
	    	 //System.out.println("\nOut of the Loop");
	    	 Log.logger.info("Out of the Loop.");
		}  
    	public void stopThread(){
    		server.stop();
    	}
	}
    
    
    private MsgReceiver msgR = null;
    private Thread thread = null;

    
    public Communication(){
    	transport = new TSocket("localhost", remoteServerPort);
        protocol = new TBinaryProtocol(transport);
        client = new AppToServer.Client(protocol);
        msgR = new MsgReceiver(selfServerPort, queue);
        thread = new Thread(msgR, "msgRecv");
        thread.start();
    }
    public Communication(int selfServerPort_, int remoteServerPort_){
    	selfServerPort = selfServerPort_;
    	remoteServerPort = remoteServerPort_;
    	
    	transport = new TSocket("localhost", remoteServerPort);
        protocol = new TBinaryProtocol(transport);
        client = new AppToServer.Client(protocol);
        msgR = new MsgReceiver(selfServerPort, queue);
        thread = new Thread(msgR, "msgRecv");
        thread.start();
    }
    
    public boolean initialize(){
    	boolean isSuc = true;
    	try {
			transport.open();
		} catch (TTransportException e) {
			//e.printStackTrace();
			Log.logger.error("In initialize process.", e);
			isSuc = false;
		}
    	if(isSuc){
	    	protocol.setReadLength(10000);
	    	
	    	try {
				client.disconnect(selfServerPort);
			} catch (TException e) {
				//e1.printStackTrace();
				Log.logger.error("In initialize process.", e);
				isSuc = false;
			}
	    	if(isSuc){
		    	try {
					client.connect(selfServerPort);
				} catch (TException e) {
					//e.printStackTrace();
					Log.logger.error("In initialize process.", e);
					isSuc = false;
				}
	    	}
    	}
    	if (!isSuc){
    		transport.close();
    		Log.logger.fatal("Initialize fail, can not continue next!");
    	}else{
    		Log.logger.info("Initialize success!");
    	}
    	return isSuc;
    }
    public void close(){
    	try {
			client.disconnect(selfServerPort);
		} catch (TException e) {
			//e1.printStackTrace();
			Log.logger.error("In close process.", e);
		}
    	
    	transport.close();
   
    	msgR.stopThread();

    	try {
			thread.join();
		} catch (InterruptedException e) {
			//e.printStackTrace();
			Log.logger.error("In close process.", e);
		}
    	Log.logger.info("close finished!");
    }
    
    /*void parseAddr(Address addr){
		System.out.println("bureauCode : " + addr.bureauCode);
		System.out.println("devId : " + addr.devId);
		System.out.println("devType : " + addr.devType);
		System.out.println("nodeId : " + addr.nodeId);
		System.out.println("nodetype : " + addr.nodeType);
	}*/
   
    public synchronized boolean recvMsg(RecvMessage msg){
    	RecvMessage tmsg = queue.poll();
    	if (tmsg == null)
    		return false;
    	else{
    		msg.msg = tmsg.msg;
    		msg.address = tmsg.address;
    		return true;
    	}
    }
    
    private boolean reconnect(){
    	try{
			transport.open();
			client.connect(selfServerPort);
		}catch(TException e) {
			//System.out.println("reconnect fail to server!");
			//e.printStackTrace();
			Log.logger.error("reconnect fail to server!", e);
			transport.close();
			return false;
		}
    	return true;
    }
    
    public synchronized boolean sendMsg(SendMessage msg){
    	boolean isSuc = true;
    	if (!(transport.isOpen())){
    		if (!reconnect()){
    			//System.out.println("sendMsg operator fails");
    			Log.logger.error("sendMsg operator fails!");
    			isSuc = false;
    		}
    	}else{
    		protocol.setReadLength(10000);
   
        	try {
    			client.send(msg);
    		} catch (TException e) {
    			//System.out.println("In sendMsg process: send fail!");
    			//e.printStackTrace();
    			Log.logger.error("In sendMsg process: send fail!", e);
    			isSuc = false;
    			transport.close();
    		}
    	}
    	return isSuc;
    }
    
    public synchronized boolean getAliveServer(List<Address> addrList){
    	boolean isSuc = true;
    	if (!(transport.isOpen())){
    		if (!reconnect()){
    			//System.out.println("getAliveServer operator fails");
    			Log.logger.error("getAliveServer operator fails!");
    			isSuc = false;
    		}
    	}else{    		
			protocol.setReadLength(10000);        	        	
			try {
        		addrList.addAll(client.getaliveServer());
    		} catch (TException e) {
    			//System.out.println("In getAliveServer process: getAliveServer fail!!!");
    			//e.printStackTrace();
    			Log.logger.error("In getAliveServer process: getAliveServer fail!", e);
    			isSuc = false;
    			transport.close();
    		}
    	}
    	return isSuc;
    }
    
    public synchronized boolean getAliveClt(List<Address> addrList){
    	boolean isSuc = true;
    	if (!(transport.isOpen())){
    		if (!reconnect()){
    			//System.out.println("getAliveClt operator fails");
    			Log.logger.error("getAliveClt operator fails!");
    			isSuc = false;
    		}
    	}else{
    		protocol.setReadLength(10000);
        	
        	try {
        		addrList.addAll(client.getaliveClt());
    		} catch (TException e) {
    			//System.out.println("In getAliveClt process: getAliveClt fail!!!");
    			//e.printStackTrace();
    			Log.logger.error("In getAliveClt process: getAliveClt fail!", e);
    			isSuc = false;
    			transport.close();
    		}
    	}
    	return isSuc;
    }
    
};
