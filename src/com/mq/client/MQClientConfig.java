package com.mq.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A class to load and validate the configurations for running MQ clients.
 * The configurations are read from input properties file, 
 * eg. MQClientMultiThread.properties
 * 
 * @author	yushan
 * @since	20150604
 */
public class MQClientConfig {

	public String mqClientType;
	public String qmgrName;
	public int portNumber;
	public String hostName;
	public String channelName;
	public String requestQName;
	public boolean persistenceFlag;
	public boolean syncPointFlag;
	public int syncPointSize;
	public int clientNumber;
	public int messageNumber;
	public String reqMsgDataFileName;
	public long sleepTime;
	public String workloadType;

	
	private final String testPropFile = "./MQClientMultiThread.properties";
	private static Logger LOGGER = null;
	
	public void init(Logger logger){
		LOGGER = logger;
		Properties testProp;
		String tmpStr = null;
		
		try {
			/*
			 * Load properties file		
			 */
			testProp = new Properties();
			FileInputStream fis = new FileInputStream(testPropFile);
			testProp.load(fis);
			
			/*
			 *  Start to get value for each property
			 */		
			tmpStr = testProp.getProperty("mqClientType");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				// If mqClientType is not set, then default value is "JAVA"
				mqClientType = "JAVA";
			} else {
				mqClientType = tmpStr;
			}
			
			tmpStr = testProp.getProperty("qmgrName");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				LOGGER.error("The queue manager name (qmgrName) is not set! Please check and run again!");
				System.exit(1);
			} else {
				qmgrName = tmpStr;
			}
			
			/*
			 * We allow portNumber, hostName and channelName to be blank/unset, which 
			 * will use server binding mode instead of client mode.
			 */
			tmpStr = testProp.getProperty("portNumber");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				portNumber = -1;
			} else {
				portNumber = Integer.valueOf(tmpStr);
			}
			
			tmpStr = testProp.getProperty("hostName");
			if (null == tmpStr) {
				hostName = "";
			} else {
				hostName = tmpStr;
			}
			
			tmpStr = testProp.getProperty("channelName");
			if (null == tmpStr) {
				channelName = "";
			} else {
				channelName = tmpStr;
			}
			
			tmpStr = testProp.getProperty("requestQName");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				LOGGER.error("The request queue name (requestQName) is not set! Please check and run again!");
				System.exit(1);
			} else {
				requestQName = tmpStr;
			}

			tmpStr = testProp.getProperty("persistenceFlag");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				// If persistenceFlag is not set, then default value is false
				persistenceFlag = false;
			} else {
				persistenceFlag = Boolean.valueOf(tmpStr);
			}
			
			tmpStr = testProp.getProperty("messageNumber");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				LOGGER.error("The message number (messageNumber) is not set! Please check and run again!");
				System.exit(1);
			} else {
				messageNumber = Integer.valueOf(tmpStr);
			}

			tmpStr = testProp.getProperty("syncPointFlag");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				// If syncPointFlag is not set, then default value is false
				syncPointFlag = false;
			} else {
				syncPointFlag = Boolean.valueOf(tmpStr);
			}
			
			tmpStr = testProp.getProperty("syncPointSize");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				// If syncPointSize is not set, then default value is 1
				syncPointSize = 1;
			} else {
				syncPointSize = Integer.valueOf(tmpStr);
			}
			
			if (syncPointFlag && (syncPointSize <= 0 || syncPointSize > messageNumber)) {
				LOGGER.error("The value for syncPointSize is not valid! Please check and run again!");
				System.exit(1);
			}

			tmpStr = testProp.getProperty("clientNumber");
			if (null == tmpStr || tmpStr.trim().equals("")) {
				// If clientNumber is not set, then default value is 1
				clientNumber = 1;
			} else {
				clientNumber = Integer.valueOf(tmpStr);
			}

			reqMsgDataFileName = testProp.getProperty("reqMsgDataFileName");
			
			fis.close();
			
		} catch (FileNotFoundException e) {
			LOGGER.error("FileNotFoundException caught when loading properties file", e);
		} catch (IOException e) {
			LOGGER.error("IOException caught when loading properties file", e);
		}
			
	}
}
