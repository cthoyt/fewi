package org.jax.mgi.fewi.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mgi.frontend.datamodel.AccessionID;

/**
 * General purpose utility methods
 * 	 (for very basic things that don't fit a specific purpose)
 */
public class FewiUtil {

	/*
	 * Returns a list of the batches of the original list based on batch size
	 */
	public static <T> List<List<T>> getBatches(List<T> collection,int batchSize){
	    int i = 0;
	    List<List<T>> batches = new ArrayList<List<T>>();
	    while(i<collection.size()){
	        int nextInc = Math.min(collection.size()-i,batchSize);
	        List<T> batch = collection.subList(i,i+nextInc);
	        batches.add(batch);
	        i = i + nextInc;
	    }

	    return batches;
	}
	
	/* Returns the logical database of the first accession ID that matches the given ID string
	 * or null if no match.
	 */
	public static String getLogicalDB(List<AccessionID> ids, String desiredID) {
		for (AccessionID id : ids) {
			if (desiredID.equalsIgnoreCase(id.getAccID())) {
				return id.getLogicalDB();
			}
		}
		return null;
	}
	
	/* cleanses the given 'accID' of malicious Javascript (from a reflected cross-site scripting attack),
	 * returning a sanitized version of accID.  Removes quotes (double and single), angle brackets, spaces,
	 * equals signs, parentheses, etc.
	 */
	public static String sanitizeID(String accID) {
		if (accID == null) { return accID; }

		// Allow all letters and numbers, plus underscore, colon, hyphen, and period.
		// Strip out everything else.
		return accID.replaceAll("[^A-Za-z0-9_:\\.\\-]", "");
	}
	
	/* cleanses the given 'symbol' of malicious Javascript (from a reflected cross-site scripting attack),
	 * returning a sanitized version of accID.  Removes quotes (double and single), angle brackets, spaces,
	 * equals signs, etc.
	 */
	public static String sanitizeSymbol(String symbol) {
		if (symbol == null) { return symbol; }

		// Allow all letters and numbers, plus underscore, colon, hyphen, period, slash, and parentheses.
		// (The latter two are needed for transgenes.)  Strip out everything else.
		return symbol.replaceAll("[^A-Za-z0-9_:\\.\\-/\\(\\)]", "");
	}
	
	/* returns true if 'i' can be converted to an integer, false if not
	 */
	public static boolean isPositiveInteger(String i) {
		try {
			int ii = Integer.parseInt(i);
			if (ii < 0) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/* return new list for 'accIDs' where each one has been sanitized
	 */
	public static List<String> sanitizeIDs(List<String> accIDs) {
		if (accIDs == null) { return accIDs; }
		List<String> cleanIDs = new ArrayList<String>(accIDs.size());
		for (String accID : accIDs) {
			cleanIDs.add(sanitizeID(accID));
		}
		return cleanIDs;
	}

	/* return new list for 'symbols' where each one has been sanitized
	 */
	public static List<String> sanitizeSymbols(List<String> symbols) {
		if (symbols == null) { return symbols; }
		List<String> cleanSymbols = new ArrayList<String>(symbols.size());
		for (String symbol : symbols) {
			cleanSymbols.add(sanitizeSymbol(symbol));
		}
		return cleanSymbols;
	}
	
	/* Tokenize the given string for searching by included words.  The string is split
	 * on non-alphanumeric characters.
	 */
	public static List<String> tokenize(String searchString) {
		// Lowercase the string, remove non-alphanumerics, consolidate multiple spaces to single ones,
		// then split it on the single spaces.
		String[] tokenArray = searchString.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("[ +]", " ").split(" ");

		return Arrays.asList(tokenArray);
	}
	
	/* Convenience wrapper over monitoring functions (for looking for slow-running queries and such).  This one
	 * starts monitoring an event of the given type, identified by the given identifier.  Returns a unique key
	 * to identify it for the endMonitoring method.
	 */
	public static String startMonitoring(String eventType, String identifier) {
		return SlowEventMonitor.getSharedMonitor().startEvent(eventType, identifier);
	}

	/* Convenience wrapper over monitoring functions (for looking for slow-running queries and such).  This one
	 * ends monitoring of the event with the given key (which was assigned by the startMonitoring method).
	 */
	public static void endMonitoring(String key) {
		SlowEventMonitor.getSharedMonitor().endEvent(key);
	}
}
