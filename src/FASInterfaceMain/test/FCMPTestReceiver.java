package FASInterfaceMain.test;

import com.zhenninglang.variableTimer.TimerTask;
import com.zhenninglang.variableTimer.VariableTimer;

import FCMP.*;

public class FCMPTestReceiver {
	public static void main(String args[]) throws Exception{
		Communication coServer = new Communication(8003, 8004);
			coServer.initialize();
			RecvMessage rmsg = new RecvMessage();
			
			ExitFlag ef = new ExitFlag(false);
			// 定时器，receiveWaitingTime 时间后没有收到消息，则跳出循环
			long t1 = System.currentTimeMillis();
			VariableTimer vt = new VariableTimer(
					new TimerTask(){
						@Override
						public void run() {
							ef.setExitFlag(true);
							System.out.println(System.currentTimeMillis() - t1);
						}
					}, 
					15000);
			while(!ef.getExitFlag()){
				if(coServer.recvMsg(rmsg)){
					vt.extendTimer(1000);
					System.out.println(rmsg.getMsg());
				}
			}
			coServer.close();
		}
}

class ExitFlag{
	private boolean exitFlag;

	public ExitFlag(boolean exitFlag) {
		this.exitFlag = exitFlag;
	}
	public boolean getExitFlag() {
		return exitFlag;
	}
	public void setExitFlag(boolean exitFlag) {
		this.exitFlag = exitFlag;
	}
}