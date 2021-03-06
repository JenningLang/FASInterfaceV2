package fasEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacnetValueEnum {
	
	private static BacnetValueEnum bacnetValueEnum = new BacnetValueEnum();
	private BacnetValueEnum(){}
	
	// Life_Safety_Alarm_Values
	public static final Integer ALARM = new Integer(2);
	public static final Integer LOCAL_ALARM = new Integer(20);
	public static final Integer GENERAL_ALARM = new Integer(21);
	public static final Integer ISA_FIRST_ALARM = new Integer(257);
	public static final Integer ISA_EXTINGUISHING_WARNING = new Integer(258);
	public static final Integer ISA_EXTINGUISHING_RELEASED = new Integer(259);
	// Alarm_Values
	public static final Integer PRE_ALARM = new Integer(1);
	public static final Integer ACTIVE = new Integer(7);
	public static final Integer TEST_ACTIVE = new Integer(10);
	public static final Integer NOT_READY = new Integer(6);
	public static final Integer ABNORMAL = new Integer(16);
	public static final Integer ISA_NON_DEFAULT_MODE = new Integer(267);
	// Fault_Values
	public static final Integer ISA_SYSTEM_FAULT = new Integer(256);
	public static final Integer FAULT = new Integer(3);
	public static final Integer EMERGENCY_POWER = new Integer(17);
	// NORMAL
	public static final Integer QUIET = new Integer(0);
	// UNKNOWN
	public static final Integer UNKNOWN = new Integer(-1);
	

	private static Map<Integer, String> valueMap;
	private static List<String> valueList;
	
	static{
		valueMap = new HashMap<Integer, String>();
		valueMap.put(-1, "UNKNOWN");
		valueMap.put(0, "QUIET");
		valueMap.put(1, "PRE_ALARM");
		valueMap.put(2, "ALARM");
		valueMap.put(3, "FAULT");
		valueMap.put(6, "NOT_READY");
		valueMap.put(7, "ACTIVE");
		valueMap.put(10, "TEST_ACTIVE");
		valueMap.put(16, "ABNORMAL");
		valueMap.put(17, "EMERGENCY_POWER");
		valueMap.put(20, "LOCAL_ALARM");
		valueMap.put(21, "GENERAL_ALARM");
		valueMap.put(256, "ISA_SYSTEM_FAULT");
		valueMap.put(257, "ISA_FIRST_ALARM");
		valueMap.put(258, "ISA_EXTINGUISHING_WARNING");
		valueMap.put(259, "ISA_EXTINGUISHING_RELEASED");
		valueMap.put(267, "ISA_NON_DEFAULT_MODE");
		
		valueList = new ArrayList<String>();
		valueList.add("UNKNOWN");
		valueList.add("QUIET");
		valueList.add("PRE_ALARM");
		valueList.add("ALARM");
		valueList.add("FAULT");
		valueList.add("NOT_READY");
		valueList.add("ACTIVE");
		valueList.add("TEST_ACTIVE");
		valueList.add("ABNORMAL");
		valueList.add("EMERGENCY_POWER");
		valueList.add("LOCAL_ALARM");
		valueList.add("GENERAL_ALARM");
		valueList.add("ISA_SYSTEM_FAULT");
		valueList.add("ISA_FIRST_ALARM");
		valueList.add("ISA_EXTINGUISHING_WARNING");
		valueList.add("ISA_EXTINGUISHING_RELEASED");
		valueList.add("ISA_NON_DEFAULT_MODE");
	}
	
	public static String toString(Integer i){
		if(valueMap.containsKey(i)){
			return valueMap.get(i);
		}else{
			return valueMap.get(-1);
		}
	}
	public static String toString(String s){
		s = s.toUpperCase();
		if(valueList.contains(s)){
			return s;
		}else{
			return valueMap.get(-1);
		}
	}
}
