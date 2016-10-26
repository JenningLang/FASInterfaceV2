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
	private String siemensFASIP; // FAS 主机 ip
	private int siemensFASID; // FAS 主机的 BACnet ID
	private String interfaceFASIP; // 接口机 ip
	private int interfaceFASID; // 接口机的BACnet ID
	
	private List<FASNode> fasNodeList; // 区间列表
	private boolean hasInit = false; // 是否已经从xml文档中读取了zone 配置
	
	private FASCommChannel fasCommChan; // 存储了 network, transport, siemensFASDevice 和 interfaceFASDevice 对象
	
	private Logger logger = FASInterfaceMain.FASLogger;
	
	// 构造函数
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
	
	// 设备连接，SibX文件读取，状态变量初始化
	public void SiemensFASInit(String interfaceFASIP, int interfaceFASID, String siemensFASIP, int siemensFASID) 
			throws FASLocalDeviceInitException, FASRemoteDeviceConnException, ConfigFASNodeException{
		/* ************************************************ 
		 *        与 FAS 主机通信信道创建于初始化                              *
		 ************************************************ */
		fasCommChan = new FASCommChannel(siemensFASIP, siemensFASID, interfaceFASIP, interfaceFASID); 

		/* ************************************************ 
		 * 通过 FASCommChannel 和西门子xml文档来获取设备信息  *
		 * 初始化 localFASZone 和 LocalFASDevice            *
		 ************************************************ */
		fasNodeList = new ArrayList<FASNode>(); // 所有火灾点位信息
		try {
			SiemensConfig.configFASNodes(fasNodeList);
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new ConfigFASNodeException();
		}
		hasInit = true;
		/* ******************* 
		 * 刷新报警和故障信息   *
		 ******************* */
		refreshLocalFAS();
	}
	
	// 通过 FASCommChannel 和西门子xml文档刷新区间和设备信息
	public void refreshLocalFAS() throws ConfigFASNodeException{
		if(!hasInit){ // 当配置信息未加载
			try{
				SiemensConfig.configFASNodes(fasNodeList);
			}catch(Exception e){
				logger.debug(e.getMessage(), e);
				throw new ConfigFASNodeException();
			}
			hasInit = true;
		}
		// 状态刷新
		fasNodeList.forEach(node -> {
			node.setNodeStatus(getNodeStatusByInstantNumber(node.getObjType(), node.getInstantNumber()));
		});
	}
	
	// 读取节点状态
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
		// 实时的读取FAS区间的报警信息
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
	 * 从一个字符串中取出非数字，并转换为整数
	 */
	private int extractNumber(String s){
		s = (s == null) ? "" : s;
		String str = "";
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) <= '9' && s.charAt(i) >= '0'){
				str += s.charAt(i);
			}
		}
		str = (str.equals(""))? "-1" : str; // -1 表示unknown
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
	 * 打印所有FAS节点信息
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
	 * 迭代的树状打印
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
		
		// 递归调用
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