package FCMP;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

public class Log{
	public  static Logger logger;
	private static Log log = new Log();
	private Log(){
		logger = Logger.getLogger(Log.class.getName());
		//PropertyConfigurator.configure("C:/Users/HP/workspace/FCMPTest1/src/test/log4j.properties");
		PropertyConfigurator.configure("./log4j.properties");
	}
}