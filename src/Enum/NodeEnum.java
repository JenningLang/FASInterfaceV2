package Enum;

public enum NodeEnum {
	Area, Section, Zone, Device, Finish;
	public static NodeEnum parseInt(int n) throws Exception{
		if(n == NodeEnum.Area.ordinal()){
			return NodeEnum.Area;
		}else if(n == NodeEnum.Section.ordinal()){
			return NodeEnum.Section;
		}else if(n == NodeEnum.Zone.ordinal()){
			return NodeEnum.Zone;
		}else if(n == NodeEnum.Device.ordinal()){
			return NodeEnum.Device;
		}else if(n == NodeEnum.Finish.ordinal()){
			return NodeEnum.Finish;
		}else{
			throw new Exception("NodeEnum: parseInt error");
		}
	}
}
