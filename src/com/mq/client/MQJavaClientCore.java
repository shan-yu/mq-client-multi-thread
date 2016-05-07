package com.mq.client;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.mq.util.FileUtil;


/**
 * A MQ Java client class for testing MQ Java client throughput.
 * The client will launch multiple threads. In each thread, it will 
 * connect to queue manager, open the request queue, and 
 * put a specified number of MQ messages to it. 
 * 
 * @author	yushan
 * @since	20150604
 */
public class MQJavaClientCore extends MQClientCore {

	public MQClientConfig mqCliCfg = null;
	public static Logger LOGGER = null;
	
	// Byte array to store data from reqMsgDataFileName
	private byte[] reqMsgDataBytes = null;
	
	MQJavaClientCore(MQClientConfig mqClientConfig, Logger logger) {		
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
			LOGGER.info("MQ Java Client thread No." + this.clientId + " starts!");
			
			mqClientState[this.clientId] = false; 
			
			MQQueueManager qMgr = null;
			MQQueue requestQueue = null;
			
			try {			
				Hashtable prop = new Hashtable();
											
				if (mqCliCfg.portNumber != -1) {
					prop.put(CMQC.PORT_PROPERTY, mqCliCfg.portNumber);
				}
				
				if (!mqCliCfg.hostName.trim().equalsIgnoreCase("")){
					prop.put(CMQC.HOST_NAME_PROPERTY, mqCliCfg.hostName);
				}
				
				if (!mqCliCfg.channelName.trim().equalsIgnoreCase("")){
					prop.put(CMQC.CHANNEL_PROPERTY, mqCliCfg.channelName);
				}
				
				prop.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES);	

				qMgr = new MQQueueManager(mqCliCfg.qmgrName, prop);
				
			    // Only set the client status to true when the connection succeeds
				mqClientState[this.clientId] = true; 
				
				int openOptionForPut = MQConstants.MQOO_OUTPUT | MQConstants.MQOO_FAIL_IF_QUIESCING;
				
				requestQueue = qMgr.accessQueue(mqCliCfg.requestQName, openOptionForPut);
				
				MQMessage requestMsg = new MQMessage();
				requestMsg.format = MQConstants.MQFMT_STRING; // for MQGMO_CONVERT if needed
				
				// Specify message persistence.
				if (mqCliCfg.persistenceFlag){
					requestMsg.persistence = MQConstants.MQPER_PERSISTENT;  
				} else {
					requestMsg.persistence = MQConstants.MQPER_NOT_PERSISTENT;
				}
				
				// Set message payload data
				requestMsg.write(reqMsgDataBytes);
				
				MQPutMessageOptions putMsgOpt = new MQPutMessageOptions();
				
				putMsgOpt.options = MQConstants.MQPMO_FAIL_IF_QUIESCING | MQConstants.MQPMO_NEW_MSG_ID;
				
				// specify sync point for MQPUT.
				if (mqCliCfg.syncPointFlag){
					putMsgOpt.options |= MQConstants.MQPMO_SYNCPOINT;  
				} else {
					putMsgOpt.options |= MQConstants.MQPMO_NO_SYNCPOINT;  
				}
							
				// Calculate the time spent on putting operations
				long startTime = System.currentTimeMillis();
				
				// Put messages to queue - for syn point case and no sync point case
				if (mqCliCfg.syncPointFlag){
					
					int commitCount = mqCliCfg.messageNumber / mqCliCfg.syncPointSize;
					int remainMsgs = mqCliCfg.messageNumber % mqCliCfg.syncPointSize;
					
					for (int i = 0; i < commitCount; i++) {			
						for (int j = 0; j < mqCliCfg.syncPointSize; j++) {
							requestQueue.put(requestMsg, putMsgOpt);
						}
						// Commit after sending syncPointSize messages
						qMgr.commit();
					}
					
					// if messageNumber cannot be divided by syncPointSize completely
					// then finish putting the remaining messages and commit
					for (int j = 0; j < remainMsgs; j++) {
						requestQueue.put(requestMsg, putMsgOpt);
					}
					qMgr.commit();
					
				} else {
					for (int i = 0; i< mqCliCfg.messageNumber; i++){
						requestQueue.put(requestMsg, putMsgOpt);
					}
				}
				
				// Record the time in milliseconds
				long endTime = System.currentTimeMillis();
				long elapsedTimeMillisec = endTime - startTime;
				
				LOGGER.info("MQ Java Client thread No." + this.clientId +  
				" has sent " + mqCliCfg.messageNumber + " messages in " + 
				elapsedTimeMillisec + " milliseconds");
			
				requestQueue.close();
				qMgr.disconnect();			
			} 
			catch (Exception e) {
				LOGGER.error(e);
			}
			
			// Set the state of this MQ client to be inactive
			mqClientState[this.clientId] = false; 
			
			LOGGER.info("MQ Java Client thread No." + this.clientId + " ends!");
		}		
	}


	public void launch(){
		LOGGER.info("Launching MQ Java Clients in " + mqCliCfg.clientNumber + " threads ...");
		
		for (int threadNo = 0; threadNo < mqCliCfg.clientNumber; threadNo++) {
			new mqClient(String.valueOf(threadNo)).start();
			
		}
	}

}
