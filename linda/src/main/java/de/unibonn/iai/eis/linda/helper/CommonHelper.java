package de.unibonn.iai.eis.linda.helper;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

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
}
