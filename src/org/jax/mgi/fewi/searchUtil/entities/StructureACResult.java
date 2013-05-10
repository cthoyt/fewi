package org.jax.mgi.fewi.searchUtil.entities;


/**
 * Represents a result from the Structure autocomplete hunter.
 * This is necessary so that we can get at both the synonym and base structure of each document
 * 
 * @author kstone
 *
 */
public class StructureACResult implements UniqueableObject
{
	private String structure;
	private String synonym;
	private boolean isStrictSynonym;
	private boolean hasCre=false;
	
	public StructureACResult(){}
	public StructureACResult(String structure,String synonym,boolean isStrictSynonym)
	{
		this.structure=structure;
		this.synonym=synonym;
		this.isStrictSynonym=isStrictSynonym;
	}
	public StructureACResult(String structure,String synonym,boolean isStrictSynonym,boolean hasCre)
	{
		this.structure=structure;
		this.synonym=synonym;
		this.isStrictSynonym=isStrictSynonym;
		this.hasCre=hasCre;
	}
	
	public String getStructure()
	{ return structure; }
	
	public void setStructure(String structure)
	{ this.structure = structure; }
	
	public String getSynonym()
	{ return synonym; }
	
	public void setSynonym(String synonym)
	{ this.synonym = synonym; }
	
	public boolean getIsStrictSynonym()
	{
		return isStrictSynonym;
	}
	public void setIsStrictSynonym(boolean isStrictSynonym)
	{
		this.isStrictSynonym=isStrictSynonym;
	}
	public boolean getHasCre()
	{
		return hasCre;
	}
	public void setHasCre(boolean hasCre)
	{
		this.hasCre=hasCre;
	}
	public Object getUniqueKey()
	{
		return this.synonym.trim();
	}
	public void setUniqueKey(Object uniqueKey)
	{
		// just here to appease the JSON serialiser gods
	}
	
}
