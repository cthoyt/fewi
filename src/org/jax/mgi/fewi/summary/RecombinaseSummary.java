package org.jax.mgi.fewi.summary;

import mgi.frontend.datamodel.Allele;
import mgi.frontend.datamodel.AlleleSystem;
import mgi.frontend.datamodel.RecombinaseInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.persistence.Column;
import mgi.frontend.datamodel.AlleleSynonym;
import org.jax.mgi.fewi.util.FormatHelper;

/** wrapper around an allele, to expose only certain data for a recombinase
 * summary page.  This will aid in efficient conversion to JSON notation and
 * transportation of data across the wire.  (We won't serialize more data
 * than needed.
 * @author jsb
 */
public class RecombinaseSummary {
	//-------------------
	// instance variables
	//-------------------

	// primary object for a RecombinaseSummary is an Allele
	private Allele allele;
	
	// for convenience, this is the allele's RecombinaseInfo object
	private RecombinaseInfo recombinaseInfo;
	
	// shared counter to generate unique DIV IDs
	private static int divCounter = 0;
	
	// shared variable for use in synchronizing access to divCounter
	private static Integer counterLock = new Integer(0);
	
	// maps from anatomical system abbreviation to its system key
	private static HashMap systemKeys = new HashMap();
	
	// expand and collapse arrows for Recombinase Data field
	private static String rightArrow = "<img src='http://cardolan.informatics.jax.org/webshare/images/rightArrow.gif' alt='right arrow'>";
	private static String downArrow = "<img src='http://cardolan.informatics.jax.org/webshare/images/downArrow.gif' alt='down arrow'>";
	
	//-------------
	// constructors
	//-------------
	
	// hide the default constructor
    private RecombinaseSummary () {}

    public RecombinaseSummary (Allele allele) {
    	this.allele = allele;
    	this.recombinaseInfo = allele.getRecombinaseInfo();
    	return;
    }
    
    //------------------------
    // public instance methods
    //------------------------
    
//    public List<AlleleSystemSummary> getAffectedSystems() {
//    	return this.wrapSystems(this.recombinaseInfo.getAffectedSystems());
//    }
    
//    public List<AlleleSystemSummary> getAlleleSystems() {
//    	return this.wrapSystems(this.recombinaseInfo.getAlleleSystems());
//    }
    
    public String getAlleleType() {
    	return this.allele.getAlleleType();
    }
    
    public String getCountOfReferences() {
    	return "<A HREF='/reference/allele/" + this.allele.getPrimaryID()
    		+ "'>" + this.allele.getCountOfReferences().toString() + "</A>";
    }
    
    public String getDetectedCount() {
    	List<AlleleSystem> affectedSystems = 
    		this.recombinaseInfo.getAffectedSystems();
    	List<AlleleSystem> unaffectedSystems = 
    		this.recombinaseInfo.getUnaffectedSystems();
    	
    	StringBuffer div1 = new StringBuffer();
    	StringBuffer div2 = new StringBuffer();
    	StringBuffer div3 = new StringBuffer();
    	StringBuffer div4 = new StringBuffer();

    	AlleleSystem myAS = null;
    	String system = null;
    	
    	boolean hasData = false;
    	
    	if (affectedSystems.size() > 0) {
    		String div1ID = nextDivID();
    		String div2ID = nextDivID();
    		String plSystems = FormatHelper.plural(affectedSystems.size(), "system");
    		
    		div1.append("<div id='" + div1ID + "' class='small' ");
    		div1.append("style='color:blue; cursor:pointer;' ");
    		div1.append("onClick='show(\"" + div2ID + "\"); hide(\"" + div1ID + "\");'>");
    		div1.append(rightArrow);
    		div1.append(" Detected in " + affectedSystems.size() + " " 
    			+ plSystems + ".</div>");

    		div2.append("<div id='" + div2ID + "' class='small' style='display:none;'><div ");
    		div2.append("style='color:blue; cursor:pointer;' ");
    		div2.append("onClick='show(\"" + div1ID + "\"); hide(\"" + div2ID + "\");'>");
    		div2.append (downArrow);
    		div2.append(" Detected in " + affectedSystems.size() + " "
    			+ plSystems + ".</div>");
    		
    		Iterator<AlleleSystem> affectedIterator = affectedSystems.iterator();
    		while (affectedIterator.hasNext()) {
    			myAS = affectedIterator.next();
    			system = myAS.getSystem();
    			div2.append(specificityLink(myAS.getAlleleID(), myAS.getSystemKey(),
    				myAS.getSystem()) );
    			if (affectedIterator.hasNext()) {
    				div2.append (", ");
    			}
    			
    			// if we have not already cached this system/key pair, do so
    			if (myAS.getSystemKey() != null) {
    				if (!isCachedSystem(system)) {
    					cacheSystem(system, myAS.getSystemKey());
    				}
    				hasData = true;
    			}
    		}
    		div2.append("</div>");
    	}

    	if (unaffectedSystems.size() > 0) {
    		String div3ID = nextDivID();
    		String div4ID = nextDivID();
    		String plSystems = FormatHelper.plural(unaffectedSystems.size(), "system");
    		
    		div3.append("<div id='" + div3ID + "' class='small' ");
    		div3.append("style='color:blue; cursor:pointer;' ");
    		div3.append("onClick='show(\"" + div4ID + "\"); hide(\"" + div3ID + "\");'>");
    		div3.append(rightArrow);
    		div3.append(" Not detected in " + unaffectedSystems.size() + " "
    			+ plSystems + ".</div>");

    		div4.append("<div id='" + div4ID + "' class='small' style='display:none;'><div ");
    		div4.append("style='color:blue; cursor:pointer;' ");
    		div4.append("onClick='show(\"" + div3ID + "\"); hide(\"" + div4ID + "\");'>");
    		div4.append(downArrow);
    		div4.append(" Not detected in " + unaffectedSystems.size() + " "
    			+ plSystems + ".</div>");
    		
    		Iterator<AlleleSystem> unaffectedIterator = unaffectedSystems.iterator();
    		while (unaffectedIterator.hasNext()) {
    			myAS = unaffectedIterator.next();
    			system = myAS.getSystem();
    			div4.append(specificityLink(myAS.getAlleleID(), myAS.getSystemKey(),
        				myAS.getSystem()) );
    			if (unaffectedIterator.hasNext()) {
    				div4.append (", ");
    			}
    			
    			// if we have not already cached this system/key pair, do so
    			if (myAS.getSystemKey() != null) {
    				if (!isCachedSystem(system)) {
    					cacheSystem(system, myAS.getSystemKey());
    				}
    				hasData = true;
    			}
    		}
    		div4.append("</div>");
    	}

    	if (!hasData) { return "No data available"; }
    	
    	div1.append (div2);
    	div1.append (div3);
    	div1.append (div4);
    	return div1.toString();
	}
    
    public String getDriver() {
    	return this.allele.getDriver();
    }
    
//    public String getGeneName() {
//    	return this.allele.getGeneName();
//    }
    
    public String getImsrCount() {
    	Integer count = this.allele.getImsrStrainCount();
    	if ((count == null) || count.equals(0)) { return null; }
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append("<a href='http://lindon.informatics.jax.org:18080/imsr/fetch?page=imsrSummary&gaccid=");
    	sb.append(this.allele.getPrimaryID());
    	sb.append("&state=LM&state=OV&state=EM&state=SP'>");
    	sb.append(this.allele.getImsrStrainCount());
    	sb.append("</a>");
    	return sb.toString();
    }
    
    public String getInAdiposeTissue() {
		return this.flagToString(this.recombinaseInfo.getInAdiposeTissue(), 
			"adipose");
	}
    
    public String getInAlimentarySystem() {
		return this.flagToString(this.recombinaseInfo.getInAlimentarySystem(),
			"alimentary");
	}

	public String getInBranchialArches() {
		return this.flagToString(this.recombinaseInfo.getInBranchialArches(),
			"branchial");
	}

    public String getInCardiovascularSystem() {
		return this.flagToString(this.recombinaseInfo.getInCardiovascularSystem(),
			"cardiovascular");
	}

	public String getInCavitiesAndLinings() {
		return this.flagToString(this.recombinaseInfo.getInCavitiesAndLinings(),
			"cavities");
	}

    public String getInducibleNote() {
    	return this.allele.getInducibleNote();
    }

	public String getInEarlyEmbryo() {
		return this.flagToString(this.recombinaseInfo.getInEarlyEmbryo(), "early");
	}

	public String getInEmbryoOther() {
		return this.flagToString(this.recombinaseInfo.getInEmbryoOther(), "embryo");
	}

	public String getInEndocrineSystem() {
		return this.flagToString(this.recombinaseInfo.getInEndocrineSystem(),
			"endocrine");
	}

	public String getInExtraembryonicComponent() {
		return this.flagToString(this.recombinaseInfo.getInExtraembryonicComponent(),
			"extraembryonic");
	}

	public String getInHead() {
		return this.flagToString(this.recombinaseInfo.getInHead(), "head");
	}

	public String getInHemolymphoidSystem() {
		return this.flagToString(this.recombinaseInfo.getInHemolymphoidSystem(),
			"hemolymphoid");
	}

	public String getInIntegumentalSystem() {
		return this.flagToString(this.recombinaseInfo.getInIntegumentalSystem(),
			"integumental");
	}

	public String getInLimbs() {
		return this.flagToString(this.recombinaseInfo.getInLimbs(), "limbs");
	}

	public String getInLiverAndBiliarySystem() {
		return this.flagToString(this.recombinaseInfo.getInLiverAndBiliarySystem(),
			"liver");
	}

	public String getInMesenchyme() {
		return this.flagToString(this.recombinaseInfo.getInMesenchyme(),
			"mesenchyme");
	}

	public String getInMuscle() {
		return this.flagToString(this.recombinaseInfo.getInMuscle(), "muscle");
	}

	public String getInNervousSystem() {
		return this.flagToString(this.recombinaseInfo.getInNervousSystem(),
			"nervous");
	}

	public String getInPostnatalOther() {
		return this.flagToString(this.recombinaseInfo.getInPostnatalOther(),
			"postnatal");
	}

	public String getInRenalAndUrinarySystem() {
		return this.flagToString(this.recombinaseInfo.getInRenalAndUrinarySystem(),
			"urinary");
	}

	public String getInReproductiveSystem() {
		return this.flagToString(this.recombinaseInfo.getInReproductiveSystem(),
			"reproductive");
	}

	public String getInRespiratorySystem() {
		return this.flagToString(this.recombinaseInfo.getInRespiratorySystem(),
			"respiratory");
	}

	public String getInSensoryOrgans() {
		return this.flagToString(this.recombinaseInfo.getInSensoryOrgans(),
			"sensory");
	}

	public String getInSkeletalSystem() {
		return this.flagToString(this.recombinaseInfo.getInSkeletalSystem(),
			"skeletal");
	}

	public String getInTail() {
		return this.flagToString(this.recombinaseInfo.getInTail(), "tail");
	}

//	public String getName() {
//   	return this.allele.getName();
//    }

	public String getNomenclature() {
		StringBuffer sb = new StringBuffer();
		sb.append(FormatHelper.superscript(this.allele.getSymbol()));
		sb.append("<BR/><SPAN CLASS='small'>");
		sb.append(FormatHelper.superscript(this.allele.getGeneName()));
		if (!this.allele.getGeneName().equals(this.allele.getName())) {
			sb.append("; ");
			sb.append(FormatHelper.superscript(this.allele.getName()));
		}
		sb.append("<BR/>");
		sb.append("(<A HREF='/allele/" + this.allele.getPrimaryID()
				+ "'>phenotype data</A>)</SPAN>");
		return sb.toString();
	}
	
//	public Integer getNotDetectedCount() {
//		return this.recombinaseInfo.getNotDetectedCount();
//	}

//	public String getSymbol() {
//   	return this.allele.getSymbol();
//    }

	public String getSynonyms() {
		Set<AlleleSynonym> alleleSynonyms = this.allele.getSynonyms();
		ArrayList<String> synonyms = new ArrayList<String>();
		Iterator<AlleleSynonym> it = alleleSynonyms.iterator();
		AlleleSynonym alleleSynonym = null;
		
		while (it.hasNext()) {
			alleleSynonym = it.next();
			synonyms.add(alleleSynonym.getSynonym());
		}
		Collections.sort(synonyms);
		
		Iterator<String> si = synonyms.iterator();
		StringBuffer sb = new StringBuffer();
		while (si.hasNext()) {
			sb.append (FormatHelper.superscript(si.next()));
			if (si.hasNext()) {
				sb.append (", ");
			}
		}
		return sb.toString();
	}
	
//    public List<AlleleSystemSummary> getUnaffectedSystems() {
//    	return this.wrapSystems(this.recombinaseInfo.getUnaffectedSystems());
//    }

    //-------------------------
    // private instance methods
    //-------------------------
    
    private String flagToString (Integer isDetected, String system) {
    	if (isDetected == null) {
    		return "";
    	} 

    	String label;
    	if (isDetected.intValue() == 1) {
    		label = "Detected"; 
    	}
    	else { 
    		label = "Not Detected"; 
    	}
    	
    	Integer systemKey = lookupSystem(getSystemCode(system));
    	if (systemKey == null) {
    		// if the key for this sysetm has not been cached, call 
    		// getDetectedCount() to ensure that the cache has been updated,
    		// then try again (should happen rarely, if ever)
    		String ignore = this.getDetectedCount();
    		systemKey = lookupSystem(getSystemCode(system));
    		if (systemKey == null) {
    			systemKey = new Integer(0);
    		}
    	}
    	
    	return specificityLink (this.allele.getPrimaryID(), systemKey, label);
    }
    
    private List<AlleleSystemSummary> wrapSystems (List<AlleleSystem> systems) {
    	ArrayList<AlleleSystemSummary> summaries = new ArrayList<AlleleSystemSummary> ();
    	Iterator<AlleleSystem> it = systems.iterator();
    	while (it.hasNext()) {
    		summaries.add(new AlleleSystemSummary(it.next()));
    	}
    	return summaries;
    }

    //-----------------------
    // private static methods
    //-----------------------
    
    /** get the next ID to use for a DIV.  These are unique until we reach
     * the maximum value for an int, then they start over at 1.
     */
    private static String nextDivID () {
    	int myCount = 0;
    	synchronized (counterLock) {
    		divCounter++;
    		
    		// protect against wrap-around so we just start over at 1
    		if (divCounter < 0) {
    			divCounter = 1;
    		}
    		myCount = divCounter;
    	}
    	return "div" + myCount;
    }

    /** return the unique 4-letter lowercase prefix that will uniquely
    * identify an anatomical system (this lets us be more flexible in
    * terms of capitalization and exact term contents)
    */
    private static String getSystemCode (String s) {
    	if (s == null) {
    		return null;
    	}
    	return s.substring(0,4).toLowerCase();
    }

    /** determine whether this anatomical system has already had its
     * system key cached globally
     */
    private static boolean isCachedSystem (String system) {
    	return systemKeys.containsKey(getSystemCode(system));
    }
    
    /** cache the given system key for this anatomical system
     */
    private static void cacheSystem (String system, int systemKey) {
    	synchronized (systemKeys) {
    		systemKeys.put(getSystemCode(system), new Integer(systemKey));
    	}
    	return;
    }
    
    /** find the system key for the given anatomical system
     */
    private static Integer lookupSystem (String system) {
    	return (Integer) systemKeys.get(getSystemCode(system));
    }

    /** return a link to a recombinase specificity detail page for the
     * given alleleID and systemKey, and with the given label as the text
     * of the link
     */
    private static String specificityLink (String alleleID, Integer systemKey, 
    		String label) {
    	if (systemKey == null) { return label; }
    	StringBuffer sb = new StringBuffer();
    	sb.append("<a href='/recombinase/specificity?id=");
    	sb.append(alleleID);
    	sb.append("&systemKey=");
    	sb.append(systemKey.toString());
    	sb.append("'>");
    	sb.append(label);
    	sb.append("</a>");
    	return sb.toString();
    }
    
    /** wrapper for convenience of using an int systemKey
     */
    private static String specificityLink (String alleleID, int systemKey,
    		String label) {
    	return specificityLink (alleleID, new Integer(systemKey), label);
    }
}
