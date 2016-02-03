/**
 * 
 */
package camel.wmq.samples;

/**
 * @author shashikanth
 *
 */

/*
 * Class for encapsulating MFT File Transfer Details
 */
public class TransferDetails {
	private String xferID;
	private String xferStatus;
	private int resultCode;
	private int retryCount;
	private int failedCount;
	private int warningCount;
	private int totalTransferred;
	
	public String toString() {
		StringBuilder strDetails = new StringBuilder();
		strDetails.append("TransferID:       " + xferID);
		strDetails.append("Transfer Status:  " + xferStatus);
		strDetails.append("ResultCode:       " + resultCode);
		strDetails.append("Transferred:       " + totalTransferred);		
		strDetails.append("Retry Count:      " + retryCount);
		strDetails.append("Failed Transfers: " + failedCount);
		strDetails.append("Completed with Warnings: " + warningCount);
		return strDetails.toString();
	}
	
	/*
	 * @return transfer id as a string
	 */
	public String getTransferID(){
		return xferID;
	}
	
	/*
	 * Set transfer id
	 */
	public void setTransferID(String xferID) {
		this.xferID = xferID;
	}
	
	/*
	 * Get transfer status
	 */
	public String getTransferStatus(){
		return xferStatus;
	}
	
	/*
	 * Set transfer status
	 */
	public void setTransferStatus(String status) {
		this.xferStatus = status;
	}
	
	/*
	 * Returns number of files transferred in a batch
	 */
	public int getTransferredCount(){
		return totalTransferred;
	}
	
	/*
	 * Set count of file transferred.
	 */
	public void setTransferredCount(int count){
		this.totalTransferred = count;
	}

	/*
	 * Get result code. 0 - Success, 40- Failed
	 */
	public int getResultCode(){
		return resultCode;
	}
	
	/*
	 * Set result code
	 */
	public void setResultCode(int code){
		this.resultCode = code;
	}
	
	/*
	 * Get retry count
	 */
	public int getRetryCount() {
		return retryCount;
	}
	
	/*
	 * Set retry count
	 */
	public void setRetryCount(int count) {
		this.retryCount = count;
	}
	
	/*
	 * Get number of failed file transfers
	 */
	public int getFailedCount() {
		return failedCount;
	}
	
	/*
	 * Set number files failed to transfer
	 */
	public void setFailedCount(int count){
		this.failedCount = count;
	}
	
	/*
	 * Get file transfers completed with warning
	 */
	public int getWarningCount() {
		return warningCount;
	}
	
	/*
	 * Set count of file transfers completed with warning
	 */
	public void setWarningCount(int count) {
		this.warningCount = count;
	}
}
