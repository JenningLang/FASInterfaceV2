package fasException;

public class NoAvailablePortException extends Exception{
	
	public NoAvailablePortException(){
		super();
	}
	public NoAvailablePortException(String host, int port){
		super("From port " + port 
				+ " to prot " + (port + 10) 
				+ " at address " + host  
				+ " are all not avaiable!");
	}
}
