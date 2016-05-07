package com.mq.client;

/**
 * Abstract class for MQ client in multi-thread model
 * 
 * @author	yushan
 * @since	20150604
 */
public abstract class MQClientCore {
	
	public static int SuccessNumber = 0;
	public static int FailureNumber = 0;
	public static boolean[] mqClientState = null;
	
	public void launch(){}

}
