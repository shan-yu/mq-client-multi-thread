package com.mq.client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mq.util.SystemUtil;

/**
 * An entry class for launching multi-thread MQ Java/JMS clients
 * 
 * @author	yushan
 * @since 	20121219
 *
 */
public class TestMQClientMultiThread {

	private static Logger logger = Logger.getLogger(TestMQClientMultiThread.class);   
	
	public static void main(String[] args) {

		// Specify location of Log4j configuration file
		PropertyConfigurator.configure("./Log4j_MQClientMultiThread.properties");
		
		// Real-time counter for active MQ client threads
		int activeClientNumber = 0;
		
		MQClientCore clientLauncher = null;
		
		MQClientConfig mqClientConfig = new MQClientConfig();
		mqClientConfig.init(logger);
		
		try {		
			if (mqClientConfig.mqClientType.equalsIgnoreCase("JMS")){
				clientLauncher = new MQJMSClientCore(mqClientConfig, logger);
			} else {
				clientLauncher = new MQJavaClientCore(mqClientConfig, logger);
			}		
			
			// Initialize client state
			for(int i=0; i<mqClientConfig.clientNumber; i++){
				clientLauncher.mqClientState[i] = false;
			}
			
			long startTime = System.currentTimeMillis();
			clientLauncher.launch();
			
			activeClientNumber = -1;
			
			while (true && activeClientNumber != 0) {
				try {
					// Display interval is 5 seconds
					Thread.sleep(5000); 
				} catch (InterruptedException e) {
					logger.error(e);
				} 
				
				activeClientNumber = 0;
				for(int i=0; i<mqClientConfig.clientNumber; i++){
					if (clientLauncher.mqClientState[i]){
						activeClientNumber++;
					}
				}
								
				logger.info("*** MQ Client launched thread number: " + activeClientNumber + 
						"  Time elapsed (in seconds): " + SystemUtil.getElapsedTimeStringInSec(startTime));
			}						
		} catch (NullPointerException npe) {
			logger.error("NullPointerException has been caught! Please check if properties file is correct", npe);
			return;
		}
		
		logger.info("Program exits!");		
	}
}
