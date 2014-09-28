package de.unibonn.iai.eis.linda.helper;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.WordUtils;

/**
 * @author gsingharoy
 * 
 *         This class will have common helper methods
 **/

public class CommonHelper {

	public static String decode(String encodedString) throws UnsupportedEncodingException {
		byte[] decoded = Base64.decodeBase64(encodedString);
		return new String(decoded, "UTF-8");
	}
	
	//This method returns a camel cased  variable name from the title
	public static String getVariableName(String title, String defaultName, Boolean firstCharSmall){
		String result = defaultName;
		if(title != null && !title.equals(""))
		{
			result = WordUtils.capitalizeFully(title);
			if(firstCharSmall)
				result = Character.toLowerCase(result.charAt(0))+result.substring(1,result.length());
		}
		return result;
	}
	
	public static String getVariableName(String title, String defaultName){
		return CommonHelper.getVariableName(title, defaultName,true);
	}
}
