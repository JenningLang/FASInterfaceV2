package FASInterfaceMain.test;

import java.util.ArrayList;
import java.util.List;

import Enum.bacnetValueEnum;
import FAS.FASNode;
import FAS.SibX.SiemensConfig;

public class ConfigTest {

	public static void main(String[] args) throws Exception
	{	
		List<FASNode> localFASNode = new ArrayList<FASNode>();
		try{
			SiemensConfig.configFASNodes(localFASNode);
		}catch(Exception e){
			e.printStackTrace();
		}
		localFASNode.forEach(node -> System.out.println(node.toString()));
	}

}
