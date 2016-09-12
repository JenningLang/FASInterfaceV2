package FCMP;

import org.apache.thrift.TException;
import java.util.concurrent.*;

public class ServerToAppImpl implements ServerToApp.Iface {
	private BlockingQueue queue = null;
    public ServerToAppImpl(BlockingQueue queue) {
    	this.queue = queue;
    }  
	@Override
	public void send(RecvMessage s) {
		// TODO Auto-generated method stub
		queue.offer(s);
		
	}
	@Override
	public void ping() throws TException {
		// TODO Auto-generated method stub
	}  
}  
