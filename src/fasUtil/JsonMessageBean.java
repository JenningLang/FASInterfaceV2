package fasUtil;

public class JsonMessageBean {
	/*消息发送者*/
	private String from;
	/*消息类型*/
	private String type;
	/*消息发送时间戳*/
	private String time;
	/*素材信息列表*/
	private String content; 
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
