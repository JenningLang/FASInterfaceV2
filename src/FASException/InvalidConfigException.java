package FASException;

public class InvalidConfigException extends Exception{
	public InvalidConfigException(){
		super();
	}
	public InvalidConfigException(String invalidConfig, String configName){
		super(invalidConfig + " is not a valid " + configName);
	}
}
