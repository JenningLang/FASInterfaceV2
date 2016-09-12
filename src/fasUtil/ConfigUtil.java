package fasUtil;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import FASException.InvalidConfigException;
import FASException.NoAvailablePortException;

public class ConfigUtil {

	private static String siemensFASIP; // FAS ���� ip
	private static int siemensFASID; // FAS ������ BACnet ID
	private static String interfaceFASIP; // �ӿڻ� ip
	private static int interfaceFASID; // �ӿڻ���BACnet ID

	private static String FCMPAppAddr; // ����FCMPͨ�ŵ�ip��ַ
	private static int FCMPLocalPort; // FCMP��˳���˿�
	private static int FCMPRemotePort; // FCMPǰ�˳��򱾵ض˿�
	
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;
	/**
	 * 
	 * @throws JDOMException, IOException, UnknownHostException, NoAvailablePortException
	 * */
	public ConfigUtil() throws JDOMException, IOException, // �������ĵ��쳣
								NoAvailablePortException, // �޷������˿��쳣
								InvalidConfigException // �Ƿ��������쳣
	{
		SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(new File("Config\\LocalConfig.xml"));// ������
        // FAS Elements
		Element FASConfEle = document.getRootElement().getChild("FASConfig");
		Element siemensFASIPEle = FASConfEle.getChild("siemensFASIP");
		Element interfaceFASIPEle = FASConfEle.getChild("interfaceFASIP");
		Element siemensFASIDEle = FASConfEle.getChild("siemensFASID");
		Element interfaceFASIDEle = FASConfEle.getChild("interfaceFASID");
		// FCMP Elements
		Element FCMPEle = document.getRootElement().getChild("FCMPConfig");
		Element FCMPAppIPEle = FCMPEle.getChild("appIP");
		Element FCMPAppPortsConfEle = FCMPEle.getChild("appPorts");
		Element FCMPLocalPortEle = FCMPAppPortsConfEle.getChild("localPort");
		Element FCMPRemotePortEle = FCMPAppPortsConfEle.getChild("remotePort");
		
		siemensFASIP = siemensFASIPEle.getText().trim();
		interfaceFASIP = interfaceFASIPEle.getText().trim();
		siemensFASID = Math.abs(Integer.parseInt(siemensFASIDEle.getText().trim()));
		interfaceFASID = Math.abs(Integer.parseInt(interfaceFASIDEle.getText().trim()));
		
		FCMPAppAddr = FCMPAppIPEle.getText().trim();
		FCMPLocalPort = Integer.parseInt(FCMPLocalPortEle.getText().trim());
		if(FCMPLocalPort > 65535 || FCMPLocalPort < 0){
			throw new InvalidConfigException(String.valueOf(FCMPLocalPort), "port number: localPort");
		}
		FCMPLocalPort = NetUtil.getAvailablePort(FCMPAppAddr, FCMPLocalPort);
		FCMPRemotePort = Integer.parseInt(FCMPRemotePortEle.getText().trim());
		
		// �Ϸ��Լ��
		if(!NetUtil.isIpValid(siemensFASIP)){
			throw new InvalidConfigException(siemensFASIP, "ip address: siemensFASIP");
		}
		if(!NetUtil.isIpValid(interfaceFASIP)){
			throw new InvalidConfigException(interfaceFASIP, "ip address: interfaceFASIP");
		}
		if(!NetUtil.isIpValid(FCMPAppAddr)){
			throw new InvalidConfigException(FCMPAppAddr, "ip address: appIP");
		}
		if(FCMPRemotePort > 65535 || FCMPRemotePort < 0){
			throw new InvalidConfigException(String.valueOf(FCMPRemotePort), "port number: remotePort");
		}
		
		// �������ȡ���
		logger.info("Config result:");
		logger.info("FAS: siemensFASIP: " + siemensFASIP);
		logger.info("FAS: siemensFASID: " + siemensFASID);
		logger.info("FAS: interfaceFASIP: " + interfaceFASIP);
		logger.info("FAS: interfaceFASID: " + interfaceFASID);
		logger.info("FCMP: AppAddr: " + FCMPAppAddr);
		logger.info("FCMP: LocalPort: " + FCMPLocalPort);
		logger.info("FCMP: RemotePort: " + FCMPRemotePort);
	}
	
	// getters
	public static String getSiemensFASIP() {
		return siemensFASIP;
	}
	public static int getSiemensFASID() {
		return siemensFASID;
	}
	public static String getInterfaceFASIP() {
		return interfaceFASIP;
	}
	public static int getInterfaceFASID() {
		return interfaceFASID;
	}
	public static String getFCMPAppAddr() {
		return FCMPAppAddr;
	}
	public static int getFCMPLocalPort() {
		return FCMPLocalPort;
	}
	public static int getFCMPRemotePort() {
		return FCMPRemotePort;
	}
	public Logger getLogger() {
		return logger;
	}
}
