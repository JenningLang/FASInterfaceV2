package FCMP;
import java.util.LinkedList;
import java.util.List;

import FCMP.Address;
import FCMP.Communication;
import FCMP.SendMessage;
import FCMP.RecvMessage;

public class TestPlay {
	
	public static void main(String [] args) throws InterruptedException{
		Communication co = new Communication(8004, 8005);
		co.initialize();
		
		//List<test.Address> alivecltListo = co.getAliveClt();
		//List<test.Address> alivecltListo = co.getAliveServer();
		//alivecltListo.addAll(aliveserverListo);
		
//		if(alivecltListo.size() > 0){
//			for(int i = 0;i < alivecltListo.size(); i++){
//				Address temp = alivecltListo.get(i);
//				System.out.println("接入的地址有："+ temp.getBureauCode() + " " + temp.getNodeType()
//						+ " " + temp.getNodeId() + " " + temp.getDevType() + " " + temp.getDevId());
//			}
//		}
		
		while(true){
			RecvMessage recv = new RecvMessage();
			boolean suc = co.recvMsg(recv);
			if(suc && null != recv ){
				System.out.println("\n收到数据长度 ： "+recv.msg.length());
				System.out.println("数据的地址是 ： "+recv.address.devId);
				
				List<FCMP.Address> alivecltList = new LinkedList<Address>();
				co.getAliveClt(alivecltList);
				List<FCMP.Address> aliveserverList = new LinkedList<Address>();
				co.getAliveServer(aliveserverList);
				alivecltList.addAll(aliveserverList);
				
				if(alivecltList.size() > 0){
					for(int i = 0;i < alivecltList.size(); i++){
						Address temp = alivecltList.get(i);
						System.out.println("接入的地址有："+ temp.getBureauCode() + " " + temp.getNodeType()
								+ " " + temp.getNodeId() + " " + temp.getDevType() + " " + temp.getDevId());
					}
		    		SendMessage returnMsg = new SendMessage();
	    			//returnMsg.msg =recv.msg+" "+"test" ;
		    		returnMsg.msg ="ack" ;
	    			returnMsg.addresses = new LinkedList<Address>();
	    			returnMsg.addresses.add(recv.address);
	    			if(co.sendMsg(returnMsg)){
	    				System.out.println("发送数据成功 ： "+returnMsg.msg);
	    			}else{
	    				System.out.println("发送数据失败！！！ ");
	    			}
				}
			}else 
				Thread.sleep(10);
		}
	}
}

