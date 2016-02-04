package camel.wmq.samples;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.xml.sax.InputSource;

import com.ibm.mq.constants.MQConstants;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/*
 * Camel IBM MQ integration sample 
 *
 * This simple sample demonstrates how integrate IBM MQ with
 * Apache Camel. 
 * 
 * This sample listens to IBM MQ Managed File Transfer publications
 * and counts number of successful and failed transfers and publishes
 * them another topic.
 * 
 */
public class MFTStats {
	//Count of failed transfers
	private int failedXfers = 0;
	//Count Successful transfers
	private int successfulXfers = 0;
	// MFT Log topic to receive publication
	private final String topicMFT = "topic:///SYSTEM.FTE/Log/#";
	// Topic to publish Statistics messages  
	private final String topicStats = "topic:///MFT.STATS";
	//Component name
	private final String compName = "wmq-jms";
	private MFTStats() {        
    }
	
	/*
	 * Build the context and parse MFT Publication
	 */
	private void proceMFTPublication() {
		UserCredentialsConnectionFactoryAdapter ucf = null;
		
		try {
			// Get connection factory instance to IBM MQ Queue manager
			ucf = getJmsConnectionFactory();
			
			// Create a Camel context first
	    	CamelContext cContext = new DefaultCamelContext();

	    	// Add the IBM MQ Connection Factory as a component to context.  
	    	cContext.addComponent(compName, JmsComponent.jmsComponentAutoAcknowledge(ucf));

	    	/*
	    	 *  Add custom processor for processing MFT publication. The publication
	    	 *  is processed using SAX processor and update the statistics of completed
	    	 *  and failed transfers.
	    	 *  
	    	 *  The statistics are then published on MFT.STATS topic.
	    	 */
	    	final Processor customProcessor = new Processor() {
	    		public void process(Exchange exch){
	    			try {
	    				// Get the incoming publication
		    			org.apache.camel.Message mftPublication = exch.getIn();
		    			// Get the body of incoming XML publication
		    			String mftPubXml = (String)mftPublication.getBody();
		    			
		    			// Use SAX parser to process the publication
	    		        SAXParserFactory factory = SAXParserFactory.newInstance();
	    		        SAXParser saxParser = factory.newSAXParser();
	    		        MFTPublicationHandler mftPubHandler = new MFTPublicationHandler();
	 	    			saxParser.parse(new InputSource(new StringReader(mftPubXml)), mftPubHandler );
	 	    			
	 	    			// Get the transfer details from the parsed publication 
	 	    			TransferDetails xferDets = mftPubHandler.getTransferDetails();
	 	    			
	 	    			// Update counters and publish the statistics
	 	    			String publishMessage = "";
	 	    			if(xferDets.getTransferStatus().equalsIgnoreCase("completed") && 
	 	    				xferDets.getResultCode() == 0) {
	 	    				successfulXfers += xferDets.getTransferredCount();
	 	    				publishMessage = "Completed Transfers: " + successfulXfers;
	 	    			}else if(xferDets.getTransferStatus().equalsIgnoreCase("completed") && 
		 	    				xferDets.getResultCode() == 40){
	 	    				//Failed transfers
	 	    				failedXfers += xferDets.getFailedCount();
	 	    				publishMessage = "Failed Transfers: " + failedXfers;
	 	    			}else {
	 	    				// In progress publication
	 	    				publishMessage = "Transfer in progress: " + xferDets.getTransferID();
	 	    			}

	 	    			// Publish the statistics message
	 	    			org.apache.camel.Message outCamelMsg = exch.getOut();
	 	    			outCamelMsg.setBody(publishMessage);	 	    				
	    			}catch(Exception ex){
	    				System.out.println(ex);
	    			}	    			
	    		}
	    	};
	    	
	    	/*
	    	 * This processor prints publication received on MFT.STATS topic.  
	    	 */
	    	final Processor pubProcessor = new Processor() {
	    		public void process(Exchange exch){
	    			org.apache.camel.Message fteStats = exch.getIn();
	    			System.out.println(fteStats.getBody());
	    		}
	    	};
	    	
	    	/*
	    	 * Do the required pluming to route the publications from
	    	 * SYSTEM.FTE topic
	    	 */
	    	cContext.addRoutes(new RouteBuilder() {
	            public void configure() {
	                from(compName + ":" + topicMFT)
	                .process(customProcessor)
	                .setExchangePattern(ExchangePattern.InOptionalOut)
	                .to(compName + ":" + topicStats);
	            }
	        });

	    	/*
	    	 * Attach a route to process publication from MFT.STATS topic
	    	 */
	    	cContext.addRoutes(new RouteBuilder() {
	    		public void configure() {
	    			from(compName + ":" + topicStats)
	    			.process(pubProcessor)
	    			.setExchangePattern(ExchangePattern.InOnly);
	    		}
	    	});
	    	
	    	// Start processing
	        cContext.start();
	    
	        // Wait till a key is hit to stop program
	        System.out.println("Hit any key to end");
	    	System.in.read();
	        cContext.stop();

		}catch(Exception ex){
			System.out.println(ex);
		}
	}
	
	/*
	 * Entry point
	 */
	public static void main( String[] args ) throws Exception {
        MFTStats pgm = new MFTStats();
        pgm.proceMFTPublication();
    }
	
	/*
	 * Build a JmsConnection factory to IBM MQ Queue manager
	 */
	private UserCredentialsConnectionFactoryAdapter getJmsConnectionFactory() {
		UserCredentialsConnectionFactoryAdapter ucf = null;
		try {
	        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
	        JmsConnectionFactory cf = ff.createConnectionFactory();

	        // Set the properties
	        cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, "localhost");
	        cf.setIntProperty(WMQConstants.WMQ_PORT, 1414);
	        cf.setStringProperty(WMQConstants.WMQ_CHANNEL, "MFT.SVRCONN");
	    	cf.setBooleanProperty(MQConstants.USE_MQCSP_AUTHENTICATION_PROPERTY, true);
	        cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
	        cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, "MFT.COORD.QM");
	        /*
	         *  Under the covers Camel uses Spring JMS to communicate with a JMS provider.
	         *  Spring JMS does not have a createConnection method that takes userID and
	         *  Password as parameters. Hence use the UserCredentialsConnectionFactoryAdapter
	         *  to set user id and password.
	         */
	        ucf = new UserCredentialsConnectionFactoryAdapter();
	        ucf.setTargetConnectionFactory(cf);
	        ucf.setUsername("mftuserid");
	        ucf.setPassword("mftpassw0rd");			
		}catch (Exception ex){
			System.out.println(ex);
		}
        return ucf;
	}
}
