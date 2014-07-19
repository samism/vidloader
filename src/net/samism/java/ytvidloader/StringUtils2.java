package net.samism.java.ytvidloader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: samism
 * Date: 7/18/14
 * Time: 9:32 PM
 */
public class StringUtils2 {

	/**
	 * Returns the index of where the given string ends rather than where it starts
	 * as per String#indexOf
	 *
	 * @param str    String in question
	 * @param phrase String to find the index of
	 * @return The index of where the given string ends rather than where it starts
	 * as per String#indexOf
	 */
	public static int indexOfLastChar(String str, String phrase) {
		int idx = str.indexOf(phrase);
		int len = phrase.length();

		return idx + len;
	}

	/**
	 * Decodes a url that has been recursively encoded.
	 *
	 * @param encoded String that is URLEncoded
	 * @return String that is completely URLDecoded
	 */
	public static String decodeCompletely(String encoded) {
		String uno = encoded;
		String dos = uno;

		do {
			uno = dos;
			try {
				dos = URLDecoder.decode(uno, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} while (!uno.equals(dos));

		return uno; //can return either one at this point
	}
}
