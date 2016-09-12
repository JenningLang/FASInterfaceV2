package FAS.SibX;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.FASRecoveryMsg.Enum.*;
import FAS.FASZone;
import FAS.FASDevice;

public class SiemensConfig {
	private static String sibxFileName;
	private static int panelNumber;
//	private static ArrayList<String> physicalChannelID;
	
	public static void configZoneAndDevice(List<FASZone> zoneList, List<FASDevice> deviceList) 
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
		 *                      ���Բ��� Fire Zone Ԫ��                                                                 *
		 ******************************************************************** */
		// �� n ��panel������ n+1 �� objectContainer Ԫ��
		List objectContainerEleList = rootEle.
				getChild("Content", rootEle.getNamespace()).
				getChild("InstanceFeature", rootEle.getNamespace()).
				getChild("Site", rootEle.getNamespace()).
				getChild("GroupFeature", rootEle.getNamespace()).
				getChild("EngineeringGroup", rootEle.getNamespace()).
				getChild("ObjectContainerFeature", rootEle.getNamespace()).
				getChildren("ObjectContainer", rootEle.getNamespace());
		// for each panel
		int zoneIDNum = 1;
		int deviceIDNum = 1;
		for(int panelCounter = 1; panelCounter <= panelNumber ; panelCounter++){
			Element e = (Element)objectContainerEleList.get(panelCounter);
			List l = e.getChild("ObjectFeature", e.getNamespace()).
					getChildren("EngineeringObject", e.getNamespace());
			for(Iterator<Element> it = l.iterator(); it.hasNext();){
				Element engineeringObjectEle = (Element)it.next();
				String eleID = engineeringObjectEle.getAttributeValue("ID");
				if(eleID.contains("-FI_Zone")){
					/* ********************************************************************
					 * �������"-FI_Zone"��������һ�����䣬����������Ϣ��������� zoneList �� *
					 ******************************************************************** */
					// zoneID
					String zoneIDStr = "Zone " + zoneIDNum++;
					if(eleID.contains("Automatic")){
						zoneIDStr = "Automatic" + zoneIDStr;
					}else if(eleID.contains("Manual")){
						zoneIDStr = "Manual" + zoneIDStr;
					}
					// �û�������Ϣ
					String zoneDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					// bacnet ʵ����
					int zoneInstantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// ���Ԫ��
					zoneList.add(new FASZone(
							zoneIDStr, 
							zoneDescription, 
							ZoneAndDeviceStatusEnum.Unknown, 
							zoneInstantNumber));
				}else if(eleID.contains("-FI_ChannelLog")){
					/* ***********************************************************************
					 *    �������"-FI_ChannelLog"��������һ���߼�ͨ����������� deviceList ��      *
					 *    ���п��������� Control ����Ҫ��������޳�                                                                                         *
					 *********************************************************************** */
					// �޳� Control ��
					String SVOID = addSVO(eleID);
					Element SVOEle = searchElementListByAtt(l, "ID", SVOID);
					String domainName = SVOEle.getChild("HierarchyFeature", SVOEle.getNamespace()).
							getChild("Hierarchy", SVOEle.getNamespace()).
							getChild("HierarchyType", SVOEle.getNamespace()).
							getAttributeValue("Value").trim().toLowerCase();
					if(!domainName.equals("detection")){
						continue;
					}
					// deviceID
					String deviceIDStr = "Device " + deviceIDNum++;
					// ������Ϣ ��2���ֹ��ɣ��ͻ��ı� + �ͺ�
					String customDescription = engineeringObjectEle.
							getChild("StandardFeature", engineeringObjectEle.getNamespace()).
							getChild("Description", engineeringObjectEle.getNamespace()).
							getAttributeValue("Value").trim();
					String deviceModel = getDeviceModel(l, engineeringObjectEle);
					String deviceDescription = "CustomText: " + customDescription + 
							"-----Device:" + deviceModel;
					// bacnet ʵ����
					int deviceInstantNumber = Integer.parseInt(
								engineeringObjectEle.
								getChild("EOBACnetFeature", engineeringObjectEle.getNamespace()).
								getChild("ObjectIdentifier", engineeringObjectEle.getNamespace()).
								getChild("InstanceNumber", engineeringObjectEle.getNamespace()).
								getAttributeValue("Value")
							);
					// ���Ԫ��
					deviceList.add(new FASDevice(
							deviceIDStr, 
							deviceDescription, 
							ZoneAndDeviceStatusEnum.Unknown, 
							deviceInstantNumber));
					///// debug���
//					System.out.println(deviceList.get(deviceList.size() - 1).toString());
				}
			}
		}
	}
	
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

    // ͨ��svo��id���������ڵ�
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
    
    // ���豸��ID�Ż�ȡ�豸�ͺ�
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
    
    private static String addSVO(String s){
    	if(s.equals("")){
    		return "";
    	}
    	StringBuffer sb = new StringBuffer(s);
    	sb.insert(s.indexOf("_") + 1, "SVO");
    	return new String(sb);
    }
    
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
	
	// ��Element��list�в�ѯ�ض�id�Ľڵ�
	private static Element searchElementListByAtt(List<Element> l, String attName, String attValue){
		for(Iterator<Element> it = l.iterator(); it.hasNext();){
			Element e = it.next();
			if(e.getAttributeValue(attName).trim().equals(attValue)){
				return e;
			}
		}
		return null;
	}
	
	private static String extractID(String s){
		if(s.contains("'")){
				return s.substring(s.indexOf("'") + 1, s.lastIndexOf("'") );
		}else{
			return "";
		}
	}
}
