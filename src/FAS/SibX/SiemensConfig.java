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
	 * 线性查找    Area 元素
	 * 线性查找    Section 元素
	 * 线性查找    Zone 元素
	 * 线性查找    Device 元素
	 * 建立树状关系
	 * 
	 * */
	public static void configFASNodes(List<FASNode> nodeList) 
			throws Exception{
		// 读取 SibX 文件的位置和文件名
		SAXBuilder saxBuilder = new SAXBuilder();
        Document document;
		document = saxBuilder.build(new File("Config\\LocalConfig.xml")); // 构造器
		Element sibxFileNameEle = document.getRootElement().getChild("FASConfig").getChild("sibxFileName");
		sibxFileName = sibxFileNameEle.getText().trim();
		
		// 统计共有多少设备
		document = saxBuilder.build(new File(sibxFileName));
		Element rootEle = document.getRootElement();
		List hierarchyRootRefEleList = rootEle.
				getChild("Content", rootEle.getNamespace()).
				getChild("InstanceFeature", rootEle.getNamespace()).
				getChild("Site", rootEle.getNamespace()).
				getChild("StandardFeature", rootEle.getNamespace()).
				getChild("HierarchyRoots", rootEle.getNamespace()).
				getChildren("HierarchyRootRef", rootEle.getNamespace()); // jdom 如果 getChild 时不加命名空间就会返回 null
		panelNumber = (hierarchyRootRefEleList.size() - 1) / 6; // 设备数量
		
		/* ********************************************************************
		 *           线性查找    Area/Section/Zone/Device 元素                                                    *
		 ******************************************************************** */
		// 有 n 个panel，则有 n + 1 个 objectContainer 元素
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
		
		for(int panelCounter = 1; panelCounter <= panelNumber ; panelCounter++){ // 跳过了第0个元素
			// e的子列表l包含了一个  panel 的所有元素的详细信息
			Element e = (Element)objectContainerEleList.get(panelCounter);
			List l = e.getChild("ObjectFeature", e.getNamespace()).
					getChildren("EngineeringObject", e.getNamespace());
			// 构造 panel 的名称
			String panelName = extractPanelName(e.getAttributeValue("ID")) + "_" + panelCounter;
			// 循环列表 l
			for(Iterator<Element> it = l.iterator(); it.hasNext();){
				Element engineeringObjectEle = (Element)it.next();
				String eleID = engineeringObjectEle.getAttributeValue("ID");
				/* ********************************************************************
				 *         如果包含"-FI_AreaElem"，代表是一个Detection Area            *
				 ******************************************************************** */
				if(eleID.contains("-FI_AreaElem")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet 设备类型
					int instantNumber; // BACnet 实例号
					/* dynamic info */
//					String nodeStatus;  // 配置时全部是Unknown
					Element svoEle;
					
					// nodeType
					nodeType = NodeEnum.Area;
					// nodeID
					nodeID = "Area " + areaIDNum++;
					// nodeDescription: 用户描述信息
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
					// bacnet 设备类型
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet 实例号
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// 添加元素
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// 哈希表中添加元素
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // 新添加的元素位置是最后一位
				}
				/* ********************************************************************
				 * 如果包含"-FI_Zone"，代表是一个Section，包含报警信息，将其加入 zoneList 中 *
				 ******************************************************************** */
				else if(eleID.contains("-FI_SectionElem")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet 设备类型
					int instantNumber; // BACnet 实例号
					/* dynamic info */
//					String nodeStatus;  // 配置时全部是Unknown
					Element svoEle;
					
					// nodeType
					nodeType = NodeEnum.Section;
					// nodeID
					nodeID = "Section " + sectionIDNum++;
					// nodeDescription: 用户描述信息
					nodeDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					// fatherNodeID
					fatherNodeID = ""; // 最后统一计算父节点是谁
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
					// bacnet 设备类型
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet 实例号
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// 添加元素
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// 哈希表中添加元素
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // 新添加的元素位置是最后一位
				}
				/* ********************************************************************
				 * 如果包含"-FI_Zone"，代表是一个Zone，包含报警信息，将其加入 zoneList 中 *
				 ******************************************************************** */
				else if(eleID.contains("-FI_Zone")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet 设备类型
					int instantNumber; // BACnet 实例号
					/* dynamic info */
//					String nodeStatus;  // 配置时全部是Unknown
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
					// nodeDescription: 用户描述信息
					nodeDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					// fatherNodeID
					fatherNodeID = ""; // 最后统一计算父节点是谁
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
					// bacnet 设备类型
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet 实例号
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// 添加元素
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// 哈希表中添加元素
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // 新添加的元素位置是最后一位
				}
				/* ***********************************************************************
				 *   如果包含"-FI_ChannelLog"，代表是一个逻辑通道，将其加入 deviceList 中      *
				 *   但有可能其属于 Control 树，要将其从中剔除                                                                             *
				 ********************************************************************** */
				else if(eleID.contains("-FI_ChannelLog")){
					/* static info */
					NodeEnum nodeType;
					String nodeID;
					String nodeDescription;
					String fatherNodeID;
					List<String> childNodeIDList = new LinkedList<String>();
					ObjectType objType;// BACnet 设备类型
					int instantNumber; // BACnet 实例号
					/* dynamic info */
//					String nodeStatus;  // 配置时全部是Unknown
					Element svoEle;
					
					// 剔除 Control 树
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
					// nodeDescription: 由2部分构成：客户文本 + 型号
					String customDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					String deviceModel = getDeviceModel(l, engineeringObjectEle);
					nodeDescription = "CustomText is " + customDescription + 
							", and DeviceType is " + deviceModel;
					// fatherNodeID
					fatherNodeID = ""; // 最后统一计算父节点是谁
					// childNodeIDList
					// 无子节点
					// bacnet 设备类型
					objType = new ObjectType(
								Integer.parseInt(
									engineeringObjectEle.
									getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
									getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
									getChild("ObjectType", engineeringObjectEle.getNamespace()).
									getAttributeValue("Value")
								)
							);
					// bacnet 实例号
					instantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// 添加元素
					nodeList.add(new FASNode(nodeType, 
										nodeID, 
										nodeDescription, 
										fatherNodeID,
										childNodeIDList, 
										objType, 
										instantNumber, 
										bacnetValueEnum.toString(-1)));
					// 哈希表中添加元素
					sibxID2ArrayIndex.put(eleID, nodeList.size() - 1); // 新添加的元素位置是最后一位
				}
			}
		}
		
		/* ********************************************************************
		 *                建立树状关系(补充所有元素的父节点)                    *
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
     * 通过svo的id号搜索父节点
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
     *  从设备的SibX ID号获取设备型号
     *  其思路是去掉Elem，然后从后向前搜索到第一个小写字母，这之后的就是其型号
     *  例如：1/4193827-FI_SVODeviceP2CallPointFDHM230Elem --> FDHM230
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
     * 给一个普通的属性ID加上SVO变成结构属性ID，SVO的位置在下划线"_"后
     * 例如：1/133-FI_AreaElem --> 1/133-FI_SVOAreaElem
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
     * 从一个结构属性ID去掉SVO变成普通属性ID
     * 例如：1/133-FI_SVOAreaElem --> 1/133-FI_AreaElem
     * */
    private static String substractSVO(String s){
    	return s.replace("SVO", "");
    }
    
	// 从特定元素中抽取用户描述信息
	private static String extractDescription(Element e){
		return (e.getChild("StandardFeature", e.getNamespace()).
				getChild("Description", e.getNamespace()).
				getAttributeValue("Value"));
	}    
	
	// 从特定元素中抽取实例号
	private static int extractInstantNumber(Element e){
		return Integer.parseInt(e.getChild("EOBACnetFeature", e.getNamespace()).
				getChild("ObjectIdentifier", e.getNamespace()).
				getChild("InstanceNumber", e.getNamespace()).
				getAttributeValue("Value"));
	}
	
	/** 
	 * 
	 * 从Element的list中查询特定属性值的节点
	 * @param List<Element> 待搜索的List
	 * @param attName 属性值的名称
	 * @param attValue 待搜索的属性值
	 * @return 第一个被搜索到的具有该属性值的Element
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
	 * 从Target属性的值抽取其ID，即获取两个单引号中间的字符串
	 * 例如：#ID='1/4193433-FI_ChannelPhysSensorP2AutomaticCLineElem' --> 1/4193433-FI_ChannelPhysSensorP2AutomaticCLineElem
	 * */
	private static String extractID(String s){
		if(s.contains("'")){
				return s.substring(s.indexOf("'") + 1, s.lastIndexOf("'") );
		}else{
			return "";
		}
	}
	
	/**
	 * 从ObjectContainers的ID属性的值抽取Panel的名称
	 * 思路为去掉Elem，截取最后一个下划线到末尾的子字符串
	 * 例如：1/1-OC_FI_PanelFc0726Elem --> PanelFc0726
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
