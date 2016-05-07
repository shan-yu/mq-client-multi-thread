package com.mq.client;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.mq.util.FileUtil;


/**
 * A MQ JMS client class for testing MQ JMS client throughput.
 * The client will launch multiple threads. In each thread, it will 
 * connect to queue manager, open the request queue, and 
 * put a specified number of MQ messages to it.
 * 
 * @author	yushan
 * @since	20150604
 */
public class MQJMSClientCore extends MQClientCore {
	
	private MQClientConfig mqCliCfg;
	private static Logger LOGGER = null;
	
	// Byte array to store data from reqMsgDataFileName
	private byte[] reqMsgDataBytes = null;
	
	MQJMSClientCore(MQClientConfig mqClientConfig, Logger logger){
		
		this.mqCliCfg = mqClientConfig;
		this.LOGGER = logger;
		
		mqClientState = new boolean[mqCliCfg.clientNumber];
		
		// Pre-load data into byte array from reqMsgDataFileName to improve efficiency
		if (mqCliCfg.reqMsgDataFileName != null && !(mqCliCfg.reqMsgDataFileName.trim().equalsIgnoreCase(""))){
			try {
				reqMsgDataBytes = FileUtil.readFileToBytes(mqCliCfg.reqMsgDataFileName);
			} catch (Exception e) {
				LOGGER.error("Exception caught when loading reqMsgDataFile", e);
				reqMsgDataBytes = null;				
			}			
		}
	}
	

	class mqClient extends Thread {
		private int clientId;

		mqClient(String clientIdStr){
			super("MQClient" + clientIdStr);
			clientId = Integer.valueOf(clientIdStr);
		}

		public void run() {
						
			LOGGER.info("MQ JMS Client thread No." + this.clientId + " starts!");
			
			mqClientState[this.clientId] = false; 
			
			MQConnectionFactory mqCF = null;
			Connection mqConn = null;
			Session session = null;
			
			try {			
				// Here we use MQ connection factory to create connection to 
				// queue manager in each thread. All connection parameters are 
				// provided in MQJMSClient.properties file.
			    mqCF = new MQConnectionFactory();
			    mqCF.setHostName(mqCliCfg.hostName);
			    mqCF.setPort(mqCliCfg.portNumber);
			    mqCF.setQueueManager(mqCliCfg.qmgrName);
			    mqCF.setChannel(mqCliCfg.channelName);
			    mqCF.setTransportType(WMQConstants.WMQ_CM_CLIENT);
			    mqCF.setClientReconnectOptions(WMQConstants.WMQ_CLIENT_RECONNECT);
			    mqCF.setClientReconnectTimeout(600);
			    mqConn = mqCF.createConnection();		    

			    // Only set the client status to true when the connection succeeds
				mqClientState[this.clientId] = true; 

			    // Create JMS session
				boolean transacted = mqCliCfg.syncPointFlag;
				session = mqConn.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
				Destination destination = session.createQueue(mqCliCfg.requestQName);
				MessageProducer sender = session.createProducer(destination);
				
				// Set message persistence
				if (mqCliCfg.persistenceFlag) {
					sender.setDeliveryMode(DeliveryMode.PERSISTENT); 
				} else {
					sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				}
										
				// Create JMS text message from the contents in data file
				TextMessage message = session.createTextMessage(new String(reqMsgDataBytes));
				
				// Calculate the time spent on putting operations
				long startTime = System.currentTimeMillis();
				
				// Put messages to queue - for syn point case and no sync point case
				if (mqCliCfg.syncPointFlag){					
					int commitCount = mqCliCfg.messageNumber / mqCliCfg.syncPointSize;
					int remainMsgs = mqCliCfg.messageNumber % mqCliCfg.syncPointSize;
					
					for (int i = 0; i < commitCount; i++) {			
						for (int j = 0; j < mqCliCfg.syncPointSize; j++) {
							sender.send(message);
						}
						// Commit after sending syncPointSize messages
						session.commit();
					}
					
					// if messageNumber cannot be divided by syncPointSize completely
					// then finish putting the remaining messages and commit
					for (int j = 0; j < remainMsgs; j++) {
						sender.send(message);
					}
					session.commit();
					
				} else {

					for (int i = 0; i< mqCliCfg.messageNumber; i++){
						sender.send(message);
					}
				}
				
				// Record the time in milliseconds
				long endTime = System.currentTimeMillis();
				long elapsedTimeMillisec = endTime - startTime;
				
				LOGGER.info("MQ JMS Client thread No." + this.clientId +  
						" has sent " + mqCliCfg.messageNumber + " messages in " + 
						elapsedTimeMillisec + " milliseconds");
				
				session.close();
				mqConn.close();
			} 
			catch (Exception e) {
				LOGGER.error(e);
			}
			
			// Set the state of this MQ client to be inactive
			mqClientState[this.clientId] = false; 
			
			LOGGER.info("MQ JMS Client thread No." + this.clientId + " ends!");
		}	
	}


	public void launch(){
		LOGGER.info("Launching MQ JMS Clients in " + mqCliCfg.clientNumber + " threads ...");
	
		for (int threadNo = 0; threadNo < mqCliCfg.clientNumber; threadNo++) {
			new mqClient(String.valueOf(threadNo)).start();			
		}
	}
}
