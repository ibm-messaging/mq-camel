/**
 * 
 */
package camel.wmq.samples;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author shashikanth
 *
 */

/*
 * Customised handler to process MFT publication
 */
public class MFTPublicationHandler extends DefaultHandler {
	
	private TransferDetails xferDetails = new TransferDetails();
	boolean bAction = false;
	boolean bRetryCount = false;
	boolean bFailedCount = false;
	boolean bWarningCount = false;
	
	/*
	 * returns parsed details
	 */
	public TransferDetails getTransferDetails() {
		return xferDetails;
	}
		

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("transaction")) {
	         xferDetails.setTransferID(attributes.getValue("ID"));
	      } else if (qName.equalsIgnoreCase("action")) {
	         bAction = true;
	      } else if (qName.equalsIgnoreCase("status")) {
	         xferDetails.setResultCode(Integer.parseInt(attributes.getValue("resultCode")));
	      } else if (qName.equalsIgnoreCase("retryCount")) {
	         bRetryCount = true;
	      } else if(qName.equalsIgnoreCase("numFileWarnings")) {
	    	  bWarningCount = true;
	      }else if(qName.equalsIgnoreCase("numFileFailures")) {
	    	  bFailedCount = true;
	      }else if(qName.equalsIgnoreCase("transferSet")) {
	    	  xferDetails.setTransferredCount(Integer.parseInt(attributes.getValue("total")));
	      }
	   }

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	   @Override
	   public void endElement(String uri, 
	      String localName, String qName) throws SAXException {
	      if (qName.equalsIgnoreCase("Transaction")) {
	         ;
	      }
	   }

	   /*
	    * (non-Javadoc)
	    * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	    */
	   @Override
	   public void characters(char ch[], 
	      int start, int length) throws SAXException {
	      if (bAction) {
	    	 xferDetails.setTransferStatus(new String(ch, start, length));
	         bAction = false;
	      } else if (bFailedCount) {
	    	  xferDetails.setFailedCount(Integer.parseInt(new String(ch, start, length)));
	         bFailedCount = false;
	      } else if (bWarningCount) {
	    	  xferDetails.setWarningCount(Integer.parseInt(new String(ch, start, length)));
	         bWarningCount = false;
	      } else if (bRetryCount) {
	    	 xferDetails.setRetryCount(Integer.parseInt(new String(ch, start, length)));
	         bRetryCount = false;
	      }
	   }
}
