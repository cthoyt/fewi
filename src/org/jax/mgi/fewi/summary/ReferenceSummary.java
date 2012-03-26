package org.jax.mgi.fewi.summary;

import mgi.frontend.datamodel.Reference;

import org.jax.mgi.fewi.config.ContextLoader;
import org.jax.mgi.fewi.util.Highlighter;

public class ReferenceSummary {
	private Reference reference;
	private String score;
	private String pmUrl = ContextLoader.getExternalUrls().getProperty("PubMed");
	private String doiUrl = ContextLoader.getExternalUrls().getProperty("DXDOI");
	private Highlighter titleHL = new Highlighter(null);
	private Highlighter abstractHL = new Highlighter(null);
	private Highlighter authorHL = new Highlighter(null);

	public ReferenceSummary(Reference reference) {
		this.reference = reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public void setScore(String score){
		this.score = score;
	}

	public String getScore() {
		return score;
	}	
	
	public String getVol(){
		StringBuffer sb = new StringBuffer();
		if (this.reference.getVol() != null && !"".equals(this.reference.getVol())){
			sb.append(this.reference.getVol());
		}
		if (this.reference.getIssue() != null && !"".equals(this.reference.getIssue())){
			sb.append(" (" + this.reference.getIssue() + ") ");
		} else {
			sb.append(" ");
		}
		if (this.reference.getPages() != null && !"".equals(this.reference.getPages())){
			sb.append(this.reference.getPages());
		}		
		return sb.toString();
	}

	public String getId() {		
		StringBuffer sb = new StringBuffer();
		
		String pId = reference.getPubMedID();
		if(pId != null && !"".equals(pId)){
			String p = pmUrl.replace("@@@@", pId);
			sb.append(String.format("<a href=\"%s\" target=\"_blank\" class=\"extUrl\">%s</a><br/>", 
					p, pId));			
		}
		sb.append(reference.getJnumID());
		if (this.reference.getFullTextLink() != null) {
			sb.append(String.format("<br/><a href=\"%s\" target=\"_blank\" class=\"extUrl\">Full Text</a>", 
					this.reference.getFullTextLink()));
		}
		if(!"".equals(this.reference.getDoiId())){
			String du = doiUrl.replace("@@@@", this.reference.getDoiId());
			sb.append(String.format("<br/><a href=\"%s\" target=\"_blank\" class=\"extUrl\">Journal Link</a>", 
					du));
		}
		return sb.toString();
	}

	public String getAuthors() {
		StringBuffer sb = new StringBuffer();
		if ("BOOK".equalsIgnoreCase(this.reference.getReferenceType()) && 
				this.reference.getBookEditor() != null){
			sb.append(String.format("<br/><br/><span class=\"ital\">Editors</span>: %s", this.reference.getBookEditor()));
		}
		return authorHL.highLight(reference.getAuthors()) + sb.toString();
	}

	public String getTitle() {
		StringBuffer sb = new StringBuffer();
		if ("BOOK".equalsIgnoreCase(this.reference.getReferenceType())){
			if(this.reference.getBookTitle() != null){
				sb.append(String.format("<span class=\"ital\">Chapter</span>: %s<br/><br/>", titleHL.highLight(this.reference.getTitle())));
				sb.append(String.format("<span class=\"ital\">Book</span>: %s", titleHL.highLight(this.reference.getBookTitle())));
			} else if(this.reference.getTitle() != null) {
				sb.append(String.format("<span class=\"ital\">Book</span>: %s", titleHL.highLight(this.reference.getTitle())));
			}
		} else if(this.reference.getTitle() != null) {
			sb.append(titleHL.highLight(this.reference.getTitle()));
		}
		return sb.toString();
	}

	public String getJournal() {
		if("BOOK".equalsIgnoreCase(this.reference.getReferenceType()) && 
				this.reference.getBookPublisher() != null){
			StringBuffer sb = new StringBuffer();
			sb.append("<span class=\"ital\">Publisher</span>: " + this.reference.getBookPublisher());
			if (this.reference.getBookPlace() != null){
				sb.append(", " + this.reference.getBookPlace());
			}
			return sb.toString();
		}
		return reference.getJournal();
	}

	public String getYear() {
		return reference.getYear();
	}
	
	public String getCuratedData(){
		StringBuffer sb = new StringBuffer();
		
		String fewiUrl = ContextLoader.getConfigBean().getProperty("FEWI_URL");
		String wiUrl = ContextLoader.getConfigBean().getProperty("WI_URL");
		
        sb.append("<ul class=\"curatedData\">");
        
        int expTotal = reference.getCountOfGXDAssays() + reference.getCountOfGXDResults();
        if (expTotal > 0){
        	sb.append(String.format("<li>Expression assays: <a href=\"%ssearches/expression_report.cgi?_Refs_key=%s&returnType=assays&sort=Gene symbol\">%,d</a>,", wiUrl, this.reference.getReferenceKey(), this.reference.getCountOfGXDAssays()));
        	if (this.reference.getCountOfGXDResults().intValue() > 1000){       	
        		sb.append(String.format(" results: %,d</li>", this.reference.getCountOfGXDResults()));
        	} else {
        		sb.append(String.format(" results: <a href=\"%ssearches/expression_report.cgi?_Refs_key=%s&returnType=assay results&sort=Gene symbol\">%,d</a></li>", wiUrl, this.reference.getReferenceKey(), this.reference.getCountOfGXDResults()));
        	}
        }
        if (reference.getCountOfGXDIndex() > 0){
        	sb.append(String.format("<li>Expression literature records: <a href=\"%sgxdlit/reference/%s\">%,d</a></li>", fewiUrl, this.reference.getJnumID(), this.reference.getCountOfGXDIndex()));
        }
        if(reference.getCountOfMarkers() > 0){
        	sb.append(String.format("<li>Genome features: <a href=\"%ssearches/marker_report_by_reference.cgi?%s\">%,d</a></li>", wiUrl, this.reference.getReferenceKey(), this.reference.getCountOfMarkers()));
        }
        if(reference.getCountOfAlleles() > 0){
        	sb.append(String.format("<li>Phenotypic alleles: <a href=\"%ssearches/allele_report.cgi?_Refs_key=%s\">%,d</a></li>", wiUrl, this.reference.getReferenceKey(), this.reference.getCountOfAlleles()));
        }
        if(reference.getCountOfMappingResults() > 0){
        	sb.append(String.format("<li>Mapping data: <a href=\"%ssearches/mapdata_report_by_reference.cgi?%s\">%,d</a></li>", wiUrl, this.reference.getReferenceKey(), this.reference.getCountOfMappingResults()));
        }
        if(reference.getCountOfProbes() > 0){
        	sb.append(String.format("<li>Molecular probes and clones: <a href=\"%ssearches/probe_report.cgi?_Refs_key=%s\">%,d</a></li>", wiUrl, this.reference.getReferenceKey(), this.reference.getCountOfProbes()));
        }
        if(reference.getCountOfSequenceResults() >0){
        	sb.append(String.format("<li>Sequences: <a href=\"%ssequence/reference/%s\">%,d</a></li>", fewiUrl, this.reference.getJnumID(), this.reference.getCountOfSequenceResults()));  
        }
        sb.append("</ul>"); 		
		
		return sb.toString();
	}
	
	public String getAbstract(){
		String abs = "";
		if (this.reference.getAbstract() != null 
				&& !"".equals(this.reference.getAbstract())){
			abs = abstractHL.highLight(this.reference.getAbstract());
		} else {
			abs = abstractHL.highLight("this reference has no abstract");
		}
		return abs.replaceAll("\n\n", "<p>");
	}

	public String getBookEdition() {
		return reference.getBookEdition();
	}

	public String getBookEditor() {
		return reference.getBookEditor();
	}

	public String getBookPlace() {
		return reference.getBookPlace();
	}

	public String getBookPublisher() {
		return reference.getBookPublisher();
	}

	public String getBookTitle() {
		return reference.getBookTitle();
	}

	public void setTitleHL(Highlighter highlighter) {
		this.titleHL = highlighter;
	}
	
	public void setAbstractHL(Highlighter highlighter) {
		this.abstractHL = highlighter;
	}
	
	public void setAuthorHL(Highlighter highlighter) {
		this.authorHL = highlighter;
	}

}
