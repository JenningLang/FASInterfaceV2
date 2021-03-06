package FASInterfaceMain.test;

import java.util.ArrayList;
import java.util.List;

import com.InterConnect.Communication;
import com.InterConnect.SendMessage;



public class FCMPTestSender {
	public static void main(String args[]) throws Exception{
		// FCMP test
		Communication co = new Communication(8001, 8002);
		co.initialize();
		List<com.InterConnect.Address> addrList = new ArrayList<com.InterConnect.Address>();
		com.InterConnect.Address addr = new com.InterConnect.Address((byte)8, (byte)2, (short)0, (byte)1, (short)1);
		addrList.add(addr);
		SendMessage smsg = new SendMessage("", addrList);
		
		System.out.println("Send");
		smsg.setMsg("The first message!");
		co.sendMsg(smsg);
		System.out.println("Send");
		smsg.setMsg("The second message!");
		co.sendMsg(smsg);
		System.out.println("Send");
		smsg.setMsg("The third message!");
		co.sendMsg(smsg);
		
		co.close();
	}
}
