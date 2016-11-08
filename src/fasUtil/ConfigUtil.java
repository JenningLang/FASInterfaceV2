package fasUtil;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import fasException.InvalidConfigException;
import fasException.NoAvailablePortException;

public class ConfigUtil {
	
	private static ConfigUtil configUtil;// ����
	
	private static int stationID; // վ��
	
	private static String siemensFASIP; // FAS ���� ip
	private static int siemensFASID; // FAS ������ BACnet ID
	private static String interfaceFASIP; // �ӿڻ� ip
	private static int interfaceFASPort; // �ӿڻ� �˿�
	private static int interfaceFASID; // �ӿڻ���BACnet ID

	private static String FCMPAppAddr; // ����FCMPͨ�ŵ�ip��ַ
	private static int FCMPLocalPort; // FCMP��˳���˿�
	private static int FCMPRemotePort; // FCMPǰ�˳��򱾵ض˿�
	
	
	private Logger logger = FASInterfaceMain.FASInterfaceMain.FASLogger;
	
	public static ConfigUtil getConfigUtil() throws JDOMException, IOException, // �������ĵ��쳣
											NoAvailablePortException, // �޷������˿��쳣
											InvalidConfigException // �Ƿ��������쳣
	{
		if(configUtil == null){
			configUtil = new ConfigUtil();
		}
		return configUtil;
	}
	
	/**
	 * 
	 * @throws JDOMException, IOException, UnknownHostException, NoAvailablePortException
	 * */
	private ConfigUtil() throws JDOMException, IOException, // �������ĵ��쳣
								NoAvailablePortException, // �޷������˿��쳣
								InvalidConfigException // �Ƿ��������쳣
	{
		SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(new File("Config\\LocalConfig.xml"));// ������
        // StationID Elements
		Element StationIDEle = document.getRootElement().getChild("StationID");
        // FAS Elements
		Element FASConfEle = document.getRootElement().getChild("FASConfig");
		Element siemensFASIPEle = FASConfEle.getChild("siemensFASIP");
		Element interfaceFASIPEle = FASConfEle.getChild("interfaceFASIP");
		Element siemensFASPortEle = FASConfEle.getChild("interfaceFASPort");
		Element siemensFASIDEle = FASConfEle.getChild("siemensFASID");
		Element interfaceFASIDEle = FASConfEle.getChild("interfaceFASID");
		// FCMP Elements
		Element FCMPEle = document.getRootElement().getChild("FCMPConfig");
		Element FCMPAppIPEle = FCMPEle.getChild("appIP");
		Element FCMPAppPortsConfEle = FCMPEle.getChild("appPorts");
		Element FCMPLocalPortEle = FCMPAppPortsConfEle.getChild("localPort");
		Element FCMPRemotePortEle = FCMPAppPortsConfEle.getChild("remotePort");
		
		stationID = Integer.parseInt(StationIDEle.getText().trim());
		
		siemensFASIP = siemensFASIPEle.getText().trim();
		interfaceFASIP = interfaceFASIPEle.getText().trim();
		interfaceFASPort = Math.abs(Integer.parseInt(siemensFASPortEle.getText().trim()));
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
		if(interfaceFASPort > 65535 || interfaceFASPort < 0){
			throw new InvalidConfigException(String.valueOf(interfaceFASPort), "port number: BACnet");
		}
		
		// �������ȡ���
		logger.info("Config result:");
		logger.info("\t\t stationID: " + stationID);
		logger.info("\t\t FAS: siemensFASIP: " + siemensFASIP);
		logger.info("\t\t FAS: siemensFASID: " + siemensFASID);
		logger.info("\t\t FAS: interfaceFASIP: " + interfaceFASIP);
		logger.info("\t\t FAS: interfaceFASPort: " + interfaceFASPort);
		logger.info("\t\t FAS: interfaceFASID: " + interfaceFASID);
		logger.info("\t\t FCMP: AppAddr: " + FCMPAppAddr);
		logger.info("\t\t FCMP: LocalPort: " + FCMPLocalPort);
		logger.info("\t\t FCMP: RemotePort: " + FCMPRemotePort);
	}
	
	// getters
	public static int getStationID() {
		return stationID;
	}
	public static String getSiemensFASIP() {
		return siemensFASIP;
	}
	public static int getSiemensFASID() {
		return siemensFASID;
	}
	public static String getInterfaceFASIP() {
		return interfaceFASIP;
	}
	public static int getInterfaceFASPort() {
		return interfaceFASPort;
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
