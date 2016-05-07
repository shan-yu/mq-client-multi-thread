package com.mq.util;

/**
 * A utility class for getting system information
 * 
 * @author	yushan
 * @since 	20121207
 * @version	0.2
 */
public class SystemUtil {

	/**
	 * Get elapsed time in seconds in long
	 * @param	long	Start time
	 * @return	long	Elapsed time between start time and current system time
	 */
	public static long getElapsedTimeInSec(long startTime) {
		return ((System.currentTimeMillis() - startTime) / 1000);
	}

	/**
	 * Get elapsed time in seconds in String
	 * @param	long	The start time
	 * @return	String	The elapsed time String between start time and current system time
	 */
	public static String getElapsedTimeStringInSec(long startTime) {
		return String.valueOf(getElapsedTimeInSec(startTime));
	}	

	/**
	 * Get current system time in seconds in String with format like "Sun Jan 1 12:00:00 CST 2012"
	 * @param	long	The start time
	 * @return	String	The elapsed time String between start time and current system time
	 */
	public static String getCurrentTimeString() {
		return String.format("%tc", System.currentTimeMillis());
	}
}