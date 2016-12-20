package fasException;

public class InvalidConfigException extends Exception{
	
	private static final long serialVersionUID = 4463411127720307436L;
	
	public InvalidConfigException(){
		super();
	}
	public InvalidConfigException(String invalidConfig, String configName){
		super(invalidConfig + " is not a valid " + configName);
	}
}
