package com.mq.util;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;

// Applicable since Java 7
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.Charset;

/**
 * A file utility class providing common operations on files and directories
 *
 * @author	yushan
 * @version	0.2
 * @since	20121202
 */
public class FileUtil {
	
	/**
	 * Read file contents into String
	 * 
	 * @param	String 	File path
	 * @return	String 	File contents
	 */	
	public static String readFileToString(String path) throws IOException{		
		int nextByte;
		
		StringBuilder strBuilder = new StringBuilder("");
		FileInputStream fin = new FileInputStream(path);

		while ( (nextByte = fin.read()) != -1){
			strBuilder.append((char)nextByte);
		}
		
		fin.close();

		return strBuilder.toString();
	}

	/**
	 * Read file contents into byte array, which is suitable for small files.
	 * 
	 * @param	String 	File path
	 * @return	byte[] 	File contents
	 */	
	public static byte[] readFileToBytes(String path) throws IOException, FileNotFoundException {		
		File file = new File(path);
		FileInputStream fin = new FileInputStream(path);

		// First, we need to test if the file size is too large for maximum byte array length
		if (file.length() >= Integer.MAX_VALUE) {
			System.out.println("File size is too big. Please use another function to read the contents!");
			return null;
		}

		byte[] contents = new byte[(int)(file.length())];
		
		fin.read(contents);
		
		fin.close();
		
		return contents;
	}	
	
}
