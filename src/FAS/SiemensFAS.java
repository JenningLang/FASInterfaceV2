package FAS;

import FASInterfaceMain.*;
import fasException.*;
import fasUtil.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import Enum.NodeEnum;
import Enum.bacnetValueEnum;
import FAS.SibX.SiemensConfig;

public class SiemensFAS {
	private String siemensFASIP; // FAS ���� ip
	private int siemensFASID; // FAS ������ BACnet ID
	private String interfaceFASIP; // �ӿڻ� ip
	private int interfaceFASID; // �ӿڻ���BACnet ID
	
	private List<FASNode> fasNodeList; // �����б�
	private boolean hasInit = false; // �Ƿ��Ѿ���xml�ĵ��ж�ȡ��zone ����
	
	private FASCommChannel fasCommChan; // �洢�� network, transport, siemensFASDevice �� interfaceFASDevice ����
	
	private Logger logger = FASInterfaceMain.FASLogger;
	
	// ���캯��
	public SiemensFAS()
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigFASNodeException{
		siemensFASIP = ConfigUtil.getSiemensFASIP();
		interfaceFASIP = ConfigUtil.getInterfaceFASIP();
		siemensFASID = ConfigUtil.getSiemensFASID();
		interfaceFASID = ConfigUtil.getInterfaceFASID();
		SiemensFASInit(interfaceFASIP, interfaceFASID, siemensFASIP, siemensFASID);
	}
	public SiemensFAS(String interfaceFASIP, int interfaceFASID, String siemensFASIP, int siemensFASID) 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigFASNodeException{
		this.interfaceFASIP = interfaceFASIP;
		this.interfaceFASID = interfaceFASID;
		this.siemensFASIP = siemensFASIP;
		this.siemensFASID = siemensFASID;
		SiemensFASInit(interfaceFASIP, interfaceFASID, siemensFASIP, siemensFASID);
	}
	
	// �豸���ӣ�SibX�ļ���ȡ��״̬������ʼ��
	public void SiemensFASInit(String interfaceFASIP, int interfaceFASID, String siemensFASIP, int siemensFASID) 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigFASNodeException{
		/* ************************************************ 
		 *        �� FAS ����ͨ���ŵ������ڳ�ʼ��                              *
		 ************************************************ */
		fasCommChan = new FASCommChannel(siemensFASIP, siemensFASID, interfaceFASIP, interfaceFASID); 

		/* ************************************************ 
		 * ͨ�� FASCommChannel ��������xml�ĵ�����ȡ�豸��Ϣ  *
		 * ��ʼ�� localFASZone �� LocalFASDevice            *
		 ************************************************ */
		fasNodeList = new ArrayList<FASNode>(); // ���л��ֵ�λ��Ϣ
		try {
			SiemensConfig.configFASNodes(fasNodeList);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new ConfigFASNodeException();
		}
		hasInit = true;
		/* ******************* 
		 * ˢ�±����͹�����Ϣ   *
		 ******************* */
		refreshLocalFAS();
	}
	
	// ͨ�� FASCommChannel ��������xml�ĵ�ˢ��������豸��Ϣ
	public void refreshLocalFAS() throws ConfigFASNodeException{
		if(!hasInit){ // ��������Ϣδ����
			try{
				SiemensConfig.configFASNodes(fasNodeList);
			}catch(Exception e){
				logger.debug(e.getMessage(), e);
				throw new ConfigFASNodeException();
			}
			hasInit = true;
		}
		// ״̬ˢ��
		fasNodeList.forEach(node -> {
			node.setNodeStatus(getNodeStatusByInstantNumber(node.getObjType(), node.getInstantNumber()));
		});
	}
	
	// ��ȡ�ڵ�״̬
	public String getNodeStatusByID(String nodeID)
	{
		for(FASNode node: fasNodeList){
			if(node.getNodeID().toLowerCase().equals(nodeID.toLowerCase())){
				return getNodeStatusByInstantNumber(node.getObjType(), node.getInstantNumber());
			}
		}
		return bacnetValueEnum.toString(-1);
	}
	
	/* ************************************************************************** */
	public String getNodeStatusByInstantNumber(ObjectType objType, int instantNumber)
	{
		// ʵʱ�Ķ�ȡFAS����ı�����Ϣ
		try{
			ReadPropertyAck ack = (ReadPropertyAck)fasCommChan.getInterfaceFASDevice().send(
					fasCommChan.getSiemensFASDevice(), 
			    	new ReadPropertyRequest(
			    			new ObjectIdentifier(objType, instantNumber), 
			    			PropertyIdentifier.trackingValue)
			    	);
			String ackStr = ack.getValue().toString().trim().toUpperCase();
			String value = null;
			if(containNumber(ackStr)){
				value = bacnetValueEnum.toString(extractNumber(ackStr));
			}else{
				value = bacnetValueEnum.toString(ackStr);
			}
			logger.debug("Node: " + instantNumber + ": bacnet return: " + ackStr + ": extract: " + value);
			return value;
		}catch(Exception e){
			logger.debug("In " + this.getClass().getName());
			logger.debug("Get node status error, instance number: " + instantNumber);
			logger.debug(e.getMessage(), e);
		}
		return bacnetValueEnum.toString(-1);
	}
	
	/** 
	 * ��һ���ַ�����ȡ�������֣���ת��Ϊ����
	 */
	private int extractNumber(String s){
		s = (s == null) ? "" : s;
		String str = "";
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) <= '9' && s.charAt(i) >= '0'){
				str += s.charAt(i);
			}
		}
		str = (str.equals(""))? "-1" : str; // -1 ��ʾunknown
		return Integer.parseInt(str);
	}
	private boolean containNumber(String s){
		s = (s == null) ? "" : s;
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) <= '9' && s.charAt(i) >= '0'){
				return true;
			}
		}
		return false;
	}
	/* ************************************************************************** */
	/**
	 * ��ӡ����FAS�ڵ���Ϣ
	 * */
	public void printFASStatus(boolean treeFlag) throws ConfigFASNodeException {
		printFASStatus(treeFlag, false);
	}
	public void printFASStatus(boolean treeFlag, boolean fullInfoFlag) throws ConfigFASNodeException {
		if(treeFlag){
			// TODO here needs to be revised 
			refreshLocalFAS();
			for(FASNode node: fasNodeList){
				if(node.getNodeType().equals(NodeEnum.Area)){
					treePrint(0, node.getNodeID(), fullInfoFlag);
				}
			}
		}else{
			refreshLocalFAS();
			fasNodeList.forEach(node -> System.out.println(node.toString()));
		}
	}
	/**
	 * ��������״��ӡ
	 * */
	private void treePrint(int spaceNum, String nodeID, boolean fullInfoFlag){
		if(fasNodeList.size() == 0)
			return;
		int nodeIndex = 0;
		for(FASNode node: fasNodeList){
			if(node.getNodeID().equals(nodeID)){
				nodeIndex = fasNodeList.indexOf(node);
				break;
			}
		}
		for(int i = 0; i < spaceNum; i++){
			System.out.print(" ");
		}
		System.out.print("|-");
		String temp;
		if(fullInfoFlag){
			temp = fasNodeList.get(nodeIndex).getNodeID();
			String desTemp = fasNodeList.get(nodeIndex).getNodeDescription();
			if(fasNodeList.get(nodeIndex).getNodeType().equals(NodeEnum.Device)){
				desTemp = desTemp.substring(14); // "Customtext is": 13 characters
				desTemp = desTemp.substring(0, desTemp.indexOf(',') - 1);
			}
			temp += ": " + desTemp;
			temp += ": " + fasNodeList.get(nodeIndex).getNodeStatus();
		}
		else{
			temp = fasNodeList.get(nodeIndex).getNodeID();
			temp += ": " + fasNodeList.get(nodeIndex).getNodeStatus();
		}
		System.out.print(temp);
		System.out.println("");
		
		// �ݹ����
		for(String childNodeID: fasNodeList.get(nodeIndex).getChildNodeIDList()){
			treePrint(spaceNum + 2 + 1, childNodeID, fullInfoFlag);
		}
	}
	
	
	// getters and setters
	public String getSiemensFASIP() {
		return siemensFASIP;
	}
	public void setSiemensFASIP(String siemensFASIP) {
		this.siemensFASIP = siemensFASIP;
	}
	public int getSiemensFASID() {
		return siemensFASID;
	}
	public void setSiemensFASID(int siemensFASID) {
		this.siemensFASID = siemensFASID;
	}
	public String getInterfaceFASIP() {
		return interfaceFASIP;
	}
	public void setInterfaceFASIP(String interfaceFASIP) {
		this.interfaceFASIP = interfaceFASIP;
	}
	public int getInterfaceFASID() {
		return interfaceFASID;
	}
	public void setInterfaceFASID(int interfaceFASID) {
		this.interfaceFASID = interfaceFASID;
	}
	public List<FASNode> getFasNodeList() {
		return fasNodeList;
	}
	public void setFasNodeList(List<FASNode> fasNodeList) {
		this.fasNodeList = fasNodeList;
	}
	public FASCommChannel getFasCommChan() {
		return fasCommChan;
	}
}