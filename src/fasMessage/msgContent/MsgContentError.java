package fasMessage.msgContent;

public class MsgContentError{
	private String errorInfo;

	// constructor
	public MsgContentError() {
		super();
	}
	public MsgContentError(String errorInfo) {
		super();
		this.errorInfo = errorInfo;
	}
	
	// getters and setters
	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
}
