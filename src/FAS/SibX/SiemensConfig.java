package FAS.SibX;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;

import Enum.*;
import FAS.FASNode;


public class SiemensConfig {
	private static String sibxFileName;
	private static int panelNumber;
	private static Map<String, Integer> sibxID2ArrayIndex = new HashMap<String, Integer>();
//	private static ArrayList<String> physicalChannelID;
	
	/**
	 * ���Բ���    Area Ԫ��
	 * ���Բ���    Section Ԫ��
	 * ���Բ���    Zone Ԫ��
	 * ���Բ���    Device Ԫ��
	 * ������״��ϵ
	 * 
	 * */
	public static void configFASNodes(List<FASNode> nodeList) 
			throws Exception{
		// ��ȡ SibX �ļ���λ�ú��ļ���
		SAXBuilder saxBuilder = new SAXBuilder();
        Document document;
		document = saxBuilder.build(new File("Config\\LocalConfig.xml")); // ������
		Element sibxFileNameEle = document.getRootElement().getChild("FASConfig").getChild("sibxFileName");
		sibxFileName = sibxFileNameEle.getText().trim();
		
		// ͳ�ƹ��ж����豸
		document = saxBuilder.build(new File(sibxFileName));
		Element rootEle = document.getRootElement();
		List hierarchyRootRefEleList = rootEle.
				getChild("Content", rootEle.getNamespace()).
				getChild("InstanceFeature", rootEle.getNamespace()).
				getChild("Site", rootEle.getNamespace()).
				getChild("StandardFeature", rootEle.getNamespace()).
				getChild("HierarchyRoots", rootEle.getNamespace()).
				getChildren("HierarchyRootRef", rootEle.getNamespace()); // jdom ��� getChild ʱ���������ռ�ͻ᷵�� null
		panelNumber = (hierarchyRootRefEleList.size() - 1) / 6; // �豸����
		
		/* ********************************************************************
		 *           ���Բ���    Area/Section/Zone/Device Ԫ��                                                    *
		 ******************************************************************** */
		// �� n ��panel������ n + 1 �� objectContainer Ԫ��
		List objectContainerEleList = rootEle.
				getChild("Content", rootEle.getNamespace()).
				getChild("InstanceFeature", rootEle.getNamespace()).
				getChild("Site", rootEle.getNamespace()).
				getChild("GroupFeature", rootEle.getNamespace()).
				getChild("EngineeringGroup", rootEle.getNamespace()).
				getChild("ObjectContainerFeature", rootEle.getNamespace()).
				getChildren("ObjectContainer", rootEle.getNamespace());
		// for each panel
		int areaIDNum = 1;
		int sectionIDNum = 1;
		int zoneIDNum = 1;
		int deviceIDNum = 1;
		
		for(int panelCounter = 1; panelCounter <= panelNumber ; panelCounter++){ // �����˵�0��Ԫ��
			// e�����б�l������һ��  panel ������Ԫ�ص���ϸ��Ϣ
			Element e = (Element)objectContainerEleList.get(panelCounter);
			List l = e.getChild("ObjectFeature", e.getNamespace()).
					getChildren("EngineeringObject", e.getNamespace());
			// ���� panel ������
			String panelName = extractPanelName(e.getAttributeValue("ID")) + "_" + panelCounter;
			// ѭ���б� l
			for(Iterator<Element> it = l.iterator(); it.hasNext();){
				Element engineeringObjectEle = (Element)it.next();
				String eleID = engineeringObjectEle.getAttributeValue("ID");
				/* ********************************************************************
				 *         �������"-FI_AreaElem"��������һ��Detection Area            *
				 ******************************************************************** */
				if(eleID.contains("-FI_AreaElem")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet �豸����
					int instantNumber; // BACnet ʵ����
					/* dynamic info */
//					String nodeStatus;  // ����ʱȫ����Unknown
					Element svoEle;
					
					// nodeType
					nodeType = NodeEnum.Area;
					// nodeID
					nodeID = "Area " + areaIDNum++;
					// nodeDescription: �û�������Ϣ
					nodeDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					// fatherNodeID
					fatherNodeID = panelName;
					// childNodeIDList
					svoEle = (Element)it.next();
					List childrenList = svoEle.getChild("HierarchyFeature", svoEle.getNamespace()).
							getChild("Hierarchy", svoEle.getNamespace()).
							getChild("Children", svoEle.getNamespace()).
							getChildren("ElementRef", svoEle.getNamespace());
					for(Iterator<Element> itChild = childrenList.iterator(); itChild.hasNext();){
						Element childEle = (Element)itChild.next();
						if(childEle.getAttributeValue("Target").contains("-FI_SVOSectionElem")){
							childNodeIDList.add(extractID(childEle.getAttributeValue("Target")));
						}
					}
					// bacnet �豸����
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet ʵ����
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// ���Ԫ��
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// ��ϣ�������Ԫ��
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // ����ӵ�Ԫ��λ�������һλ
				}
				/* ********************************************************************
				 * �������"-FI_Zone"��������һ��Section������������Ϣ��������� zoneList �� *
				 ******************************************************************** */
				else if(eleID.contains("-FI_SectionElem")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet �豸����
					int instantNumber; // BACnet ʵ����
					/* dynamic info */
//					String nodeStatus;  // ����ʱȫ����Unknown
					Element svoEle;
					
					// nodeType
					nodeType = NodeEnum.Section;
					// nodeID
					nodeID = "Section " + sectionIDNum++;
					// nodeDescription: �û�������Ϣ
					nodeDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					// fatherNodeID
					fatherNodeID = ""; // ���ͳһ���㸸�ڵ���˭
					// childNodeIDList
					svoEle = (Element)it.next();
					List childrenList = svoEle.getChild("HierarchyFeature", svoEle.getNamespace()).
							getChild("Hierarchy", svoEle.getNamespace()).
							getChild("Children", svoEle.getNamespace()).
							getChildren("ElementRef", svoEle.getNamespace());
					for(Iterator<Element> itChild = childrenList.iterator(); itChild.hasNext();){
						Element childEle = (Element)itChild.next();
						if(childEle.getAttributeValue("Target").contains("-FI_SVOZone")){
							childNodeIDList.add(extractID(childEle.getAttributeValue("Target")));
						}
					}
					// bacnet �豸����
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet ʵ����
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// ���Ԫ��
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// ��ϣ�������Ԫ��
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // ����ӵ�Ԫ��λ�������һλ
				}
				/* ********************************************************************
				 * �������"-FI_Zone"��������һ��Zone������������Ϣ��������� zoneList �� *
				 ******************************************************************** */
				else if(eleID.contains("-FI_Zone")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet �豸����
					int instantNumber; // BACnet ʵ����
					/* dynamic info */
//					String nodeStatus;  // ����ʱȫ����Unknown
					Element svoEle;
					
					// nodeType
					nodeType = NodeEnum.Zone;
					// nodeID
					nodeID = "Zone " + zoneIDNum++;
					if(eleID.contains("Automatic")){
						nodeID = "Automatic" + nodeID;
					}else if(eleID.contains("Manual")){
						nodeID = "Manual" + nodeID;
					}
					// nodeDescription: �û�������Ϣ
					nodeDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					// fatherNodeID
					fatherNodeID = ""; // ���ͳһ���㸸�ڵ���˭
					// childNodeIDList
					svoEle = (Element)it.next();
					List childrenList = svoEle.getChild("HierarchyFeature", svoEle.getNamespace()).
							getChild("Hierarchy", svoEle.getNamespace()).
							getChild("Children", svoEle.getNamespace()).
							getChildren("ElementRef", svoEle.getNamespace());
					for(Iterator<Element> itChild = childrenList.iterator(); itChild.hasNext();){
						Element childEle = (Element)itChild.next();
						if(childEle.getAttributeValue("Target").contains("-FI_SVOChannelLog")){
							childNodeIDList.add(extractID(childEle.getAttributeValue("Target")));
						}
					}
					// bacnet �豸����
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet ʵ����
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// ���Ԫ��
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// ��ϣ�������Ԫ��
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // ����ӵ�Ԫ��λ�������һλ
				}
				/* ***********************************************************************
				 *   �������"-FI_ChannelLog"��������һ���߼�ͨ����������� deviceList ��      *
				 *   ���п��������� Control ����Ҫ��������޳�                                                                             *
				 ********************************************************************** */
				else if(eleID.contains("-FI_ChannelLog")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet �豸����
					int instantNumber; // BACnet ʵ����
					/* dynamic info */
//					String nodeStatus;  // ����ʱȫ����Unknown
					Element svoEle;
					
					// �޳� Control ��
					svoEle = (Element)it.next();
					String domainName = svoEle.getChild("HierarchyFeature", svoEle.getNamespace()).
							getChild("Hierarchy", svoEle.getNamespace()).
							getChild("HierarchyType", svoEle.getNamespace()).
							getAttributeValue("Value").trim().toLowerCase();
					if(!domainName.equals("detection")){
						continue;
					}

					// nodeType
					nodeType = NodeEnum.Device;
					// nodeID
					nodeID = "Device " + deviceIDNum++;
					// nodeDescription: ��2���ֹ��ɣ��ͻ��ı� + �ͺ�
					String customDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					String deviceModel = getDeviceModel(l, engineeringObjectEle);
					nodeDescription = "CustomText is " + customDescription + 
							", and DeviceType is " + deviceModel;
					// fatherNodeID
					fatherNodeID = ""; // ���ͳһ���㸸�ڵ���˭
					// childNodeIDList
					// ���ӽڵ�
					// bacnet �豸����
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet ʵ����
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// ���Ԫ��
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// ��ϣ�������Ԫ��
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // ����ӵ�Ԫ��λ�������һλ
				}
			}
		}
		
		/* ********************************************************************
		 *                ������״��ϵ(��������Ԫ�صĸ��ڵ�)                    *
		 ******************************************************************** */
		List<String> childNodeList = new LinkedList();
		Integer childNodeIndex;
		String childNodeName;
		for(FASNode node: nodeList){
			childNodeList = node.getChildNodeIDList();
			for(int childNodeCounter = 0; childNodeCounter < childNodeList.size(); childNodeCounter++){
				childNodeName = substractSVO(childNodeList.get(childNodeCounter));
				childNodeIndex = sibxID2ArrayIndex.get(childNodeName);
				nodeList.get(childNodeIndex).setFatherNodeID(node.getNodeID());
				childNodeList.set(childNodeCounter, nodeList.get(childNodeIndex).getNodeID());
			}
		}
	}
	
	/**
	 * 
	 * */
    private static String getDeviceModel(List l, Element engineeringObjectEle) {
		Element paraEle = searchElementListByAtt(
				engineeringObjectEle.getChild("ParameterFeature", engineeringObjectEle.getNamespace()).
				getChildren("EOParameter", engineeringObjectEle.getNamespace()), 
				"Name", "physicalChannel");
		if(paraEle == null){
			return "";
		}
		String phyChannelSVOID = addSVO(
				extractID(
						paraEle.getChild("EORefInstance", paraEle.getNamespace()).
						getChild("Address", paraEle.getNamespace()).
						getAttributeValue("Target"))
				);
		Element fatherEle = findFather(l, phyChannelSVOID);
		if(fatherEle == null){
			return "";
		}
		return extractDeviceModel(fatherEle.getAttributeValue("ID"));
	}

    /** 
     * ͨ��svo��id���������ڵ�
     * @param l
     * @param childID
     * @return
     */
    private static Element findFather(List l, String childID){
    	for(Iterator<Element> it = l.iterator(); it.hasNext();){
			Element engineeringObjectEle = (Element)it.next();
			String eleID = engineeringObjectEle.getAttributeValue("ID");
			if(eleID.contains("-FI_SVO")){
				List childrenList = engineeringObjectEle.
						getChild("HierarchyFeature", engineeringObjectEle.getNamespace()).
						getChild("Hierarchy", engineeringObjectEle.getNamespace()).
						getChild("Children", engineeringObjectEle.getNamespace()).
						getChildren("ElementRef", engineeringObjectEle.getNamespace());
				for(Iterator<Element> itChild = childrenList.iterator(); itChild.hasNext();){
					String childIDStr = extractID(
							((Element)itChild.next()).getAttributeValue("Target")
							);
					if(childIDStr.equals(childID)){
						return engineeringObjectEle;
					}
				}
			}
    		
    	}
    	return null;
    }
    
    /**
     *  ���豸��SibX ID�Ż�ȡ�豸�ͺ�
     *  ��˼·��ȥ��Elem��Ȼ��Ӻ���ǰ��������һ��Сд��ĸ����֮��ľ������ͺ�
     *  ���磺1/4193827-FI_SVODeviceP2CallPointFDHM230Elem --> FDHM230
     */
    private static String extractDeviceModel(String s){
    	s = s.replace("Elem", "");
    	char c;
    	int i;
    	for(i = s.length() - 1; i >= 0 ; i--){
    		c = s.charAt(i);
    		if(c <= 'z' && c >= 'a'){
    			break;
    		}
    	}
    	return s.substring(i + 1);
    }
    
    /**
     * ��һ����ͨ������ID����SVO��ɽṹ����ID��SVO��λ�����»���"_"��
     * ���磺1/133-FI_AreaElem --> 1/133-FI_SVOAreaElem
     */
    private static String addSVO(String s){
    	if(!s.contains("_")){
    		return s;
    	}
    	StringBuffer sb = new StringBuffer(s);
    	sb.insert(s.indexOf("_") + 1, "SVO");
    	return new String(sb);
    }
    
    /**
     * ��һ���ṹ����IDȥ��SVO�����ͨ����ID
     * ���磺1/133-FI_SVOAreaElem --> 1/133-FI_AreaElem
     * */
    private static String substractSVO(String s){
    	return s.replace("SVO", "");
    }
    
	// ���ض�Ԫ���г�ȡ�û�������Ϣ
	private static String extractDescription(Element e){
		return (e.getChild("StandardFeature", e.getNamespace()).
				getChild("Description", e.getNamespace()).
				getAttributeValue("Value"));
	}    
	
	// ���ض�Ԫ���г�ȡʵ����
	private static int extractInstantNumber(Element e){
		return Integer.parseInt(e.getChild("EOBACnetFeature", e.getNamespace()).
				getChild("ObjectIdentifier", e.getNamespace()).
				getChild("InstanceNumber", e.getNamespace()).
				getAttributeValue("Value"));
	}
	
	/** 
	 * 
	 * ��Element��list�в�ѯ�ض�����ֵ�Ľڵ�
	 * @param List<Element> ��������List
	 * @param attName ����ֵ������
	 * @param attValue ������������ֵ
	 * @return ��һ�����������ľ��и�����ֵ��Element
	 */
	private static Element searchElementListByAtt(List<Element> l, String attName, String attValue){
		for(Iterator<Element> it = l.iterator(); it.hasNext();){
			Element e = it.next();
			if(e.getAttributeValue(attName).trim().equals(attValue)){
				return e;
			}
		}
		return null;
	}
	
	/**
	 * ��Target���Ե�ֵ��ȡ��ID������ȡ�����������м���ַ���
	 * ���磺#ID='1/4193433-FI_ChannelPhysSensorP2AutomaticCLineElem' --> 1/4193433-FI_ChannelPhysSensorP2AutomaticCLineElem
	 * */
	private static String extractID(String s){
		if(s.contains("'")){
				return s.substring(s.indexOf("'") + 1, s.lastIndexOf("'") );
		}else{
			return "";
		}
	}
	
	/**
	 * ��ObjectContainers��ID���Ե�ֵ��ȡPanel������
	 * ˼·Ϊȥ��Elem����ȡ���һ���»��ߵ�ĩβ�����ַ���
	 * ���磺1/1-OC_FI_PanelFc0726Elem --> PanelFc0726
	 * */
	private static String extractPanelName(String s){
		if(s.contains("Elem") && s.contains("_")){
	    	s = s.replace("Elem", "");
	    	return s.substring(s.lastIndexOf('_') + 1);
    	}else{
    		return "Panel";
    	}
	}
}
