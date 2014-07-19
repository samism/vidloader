package net.samism.java.ytvidloader;

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
	 * @param str String in question
	 * @param phrase Phrase to find the index of
	 * @return The index of where the given string ends rather than where it starts
	 * as per String#indexOf
	 */
	public static int indexOfLastChar(String str, String phrase) {
		int idx = str.indexOf(phrase);
		int len = phrase.length();

		return idx + len;
	}
}
