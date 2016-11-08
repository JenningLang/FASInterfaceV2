package fasUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import fasException.NoAvailablePortException;

public class NetUtil {

	/**
     *  对于给定的ip和端口号port，从port开始尝试，一直到port+10，来获取可用的端口号
     * @param host
     * @param port
     * @throws NoAvailablePortException
     */  
	public static int getAvailablePort(String host, int port) throws NoAvailablePortException{
		if(host.equals("0.0.0.0")){
			return port;
		}
		int counter = 0;
		do{
			if(isPortUsing(host, port + counter)){
				counter++;
			}else{
				return (port + counter);
			}
		}
		while((counter <= 10) && (port + counter <= 65535));
		throw new NoAvailablePortException(host, port);
	}
	
	/**
     *  true:already in using  false:not using  
     * @param host 
     * @param port 
     * @throws UnknownHostException  
     */  
    private static boolean isPortUsing(String host,int port) {  
        boolean flag = false;  
        try {  
            InetAddress theAddress = InetAddress.getByName(host);  
            Socket socket = new Socket(theAddress,port);  
            flag = true;  
        } catch (IOException e){
        }  
        return flag;  
    }
    
	/**
     *  检测ip地址是否合法
     * @param host 
     * @param port 
     * @throws UnknownHostException  
     */  
    public static boolean isIpValid(String ipAddr){
    	String[] ipValue = ipAddr.split("\\.");  
        if (ipValue.length != 4){  
            return false;  
        }
        for (int i = 0; i < ipValue.length; i++){  
            String temp = ipValue[i];  
            try{
                Integer q = Integer.valueOf(ipValue[i]);  
                if (q > 255 || q < 0){  
                    return false;  
                }  
            }  
            catch (Exception e){  
                return false;  
            }  
        }  
        return true;  
    }
    
    
}
