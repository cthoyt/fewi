package allele;

import java.util.ArrayList;
import java.util.List;

import mgi.frontend.datamodel.phenotype.DiseaseTableDisease;
import mgi.frontend.datamodel.phenotype.DiseaseTableGenotype;
import mgi.frontend.datamodel.phenotype.PhenoTableGenotype;

import org.jax.mgi.fewi.controller.AlleleController;
import org.jax.mgi.fewi.test.base.BaseConcordionTest;
import org.jax.mgi.fewi.test.mock.MockRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;


public class AlleleDetailMutationDescriptionRibbonTest extends BaseConcordionTest {
	
    private String baseUrl = "/allele/diseasetable/";
    private String legendUrl = "/allele/phenotable/";
    
    public List<DiseaseTableDisease> getDiseases(String alleleID) throws Exception
    {
    	String url = baseUrl+alleleID;
    	MockRequest mr = mockRequest(url);
    	List<DiseaseTableDisease> diseases = (List<DiseaseTableDisease>) mr.get("diseases");
    	return diseases;
    }
    public List<DiseaseTableGenotype> getDiseaseGenotypes(String alleleID) throws Exception
    {
    	String url = baseUrl+alleleID;
    	MockRequest mr = mockRequest(url);
    	List<DiseaseTableGenotype> genotypes = (List<DiseaseTableGenotype>) mr.get("genotypes");
    	return genotypes;
    }
    public List<PhenoTableGenotype> getLegendGenotypes(String alleleID) throws Exception
    {
    	String url = legendUrl+alleleID;
    	MockRequest mr = mockRequest(url);
    	List<PhenoTableGenotype> genotypes = (List<PhenoTableGenotype>) mr.get("phenoTableGenotypes");
    	return genotypes;
    }
    
    /*
     * Actual test functions
     */
    public String getLegendGenotypeID(String alleleID, String genotypeLabel) throws Exception
    {
    	for(PhenoTableGenotype pg : getLegendGenotypes(alleleID))
    	{
    		Integer seq = pg.getGenotypeSeq();
    		String label = pg.getGenotype().getGenotypeType()+seq;
    		if (label.equalsIgnoreCase(genotypeLabel)) return pg.getGenotype().getPrimaryID();
    	}
    	return "";
    }
    public String getDiseaseGenotypeID(String alleleID, String genotypeLabel) throws Exception
    {
    	for(DiseaseTableGenotype pg : getDiseaseGenotypes(alleleID))
    	{
    		Integer seq = pg.getGenotypeSeq();
    		String label = pg.getGenotype().getGenotypeType()+seq;
    		if (label.equalsIgnoreCase(genotypeLabel)) return pg.getGenotype().getPrimaryID();
    	}
    	return "";
    }
    public List<String> getDiseaseNames(String alleleID) throws Exception
    {
    	List<String> diseaseNames = new ArrayList<String>();
    	for(DiseaseTableDisease d : getDiseases(alleleID))
    	{
    		diseaseNames.add(d.getDisease());
    	}
    	return diseaseNames;
    }
}
	
