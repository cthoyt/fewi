package org.jax.mgi.fewi.propertyMapper;

import java.util.ArrayList;
import java.util.List;

import org.jax.mgi.fewi.searchUtil.Filter;

/**
 * The SolrPropertyMapper class handles the mapping of operators being passed in from the 
 * hunter, and mapping them specifically to Solr. 
 * 
 * It also handles the cases where we map 1 -> N column relationships between the hunters and
 * the underlying technology.
 * 
 * In theory this class should handle all possible cases for a normal filter, the only exception 
 * is where we might have to do some sort of special text manipulation.  For that we will need to 
 * have extend this class and override the appropriate function.
 * 
 * @author mhall
 * 
 * - refactored by kstone on 2013-05-02 to actually use objects properly. What a complete WTF this code was.
 */

public class SolrPropertyMapper 
{
    ArrayList <String> fieldList = new ArrayList<String>();
    // The default operand is equals.
    int operand = 0;
    protected String singleField = "";
    protected String joinClause = "";
    
    /**
     * The constructor that allows us to have an entire fieldlist.
     * @param fieldList
     * @param joinClause
     */
    
    public SolrPropertyMapper(ArrayList<String> fieldList, String joinClause) {
        this.fieldList = fieldList;
        this.joinClause = joinClause;
    }
    
    /**
     * A single property.
     * @param field
     */
    
    public SolrPropertyMapper(String field) {
        this.singleField = field;
    }
    
    public String getField()
    {
    	return singleField;
    }
    
    public List<String> getFieldList()
    {
    	return fieldList;
    }
    
    public String getJoinClause()
    {
    	return joinClause;
    }
    
    /**
     * This is the standard api, which returns a string that will be passed 
     * to the underlying technology.
     */
    
    public String getClause(String value,int operator)
    {
    	return getClause(value,operator,false);
    }
    public String getClause(Filter filter)
    {
    	return getClause(filter.getValue(),filter.getOperator(),filter.doNegation());
    }
    public String getClause(String value,int operator,boolean negate) 
    {
        String outClause = "";
        
        if (!singleField.equals("")) {
            outClause = handleOperand(operator, value, singleField,negate);
        }
        else {
            for (String field: fieldList) {
                if (outClause.equals("")) {
                   outClause = handleOperand(operator, value, field,negate); 
                }
                else {
                   outClause += " " + joinClause + " " + handleOperand(operator, value, field,negate);
                }
            }
        }
        return outClause;
    }
    
    /***
     * This function handles the mappings from the hunters to the respective solr operands.
     * It basically functions as a mapping from the front end to the back end.
     * @param operand
     * @param value
     * @param field
     * @return
     */
    protected String handleOperand(int operand, String value, String field)
    {
    	return handleOperand(operand,value,field,false);
    }
    protected String handleOperand(int operand, String value, String field,boolean negate) 
    {
    	String val = "";
        if (operand == Filter.OP_EQUAL) {
            val =  field + ":\"" + value + "\"";
        }
        else if (operand == Filter.OP_GREATER_THAN) {
            Integer newValue = new Integer(value);
            newValue++;
            val =  field + ":[" + newValue + " TO *]";
        }
        else if (operand == Filter.OP_LESS_THAN) {
            Integer newValue = new Integer(value);
            newValue--;
            val =  field + ":[* TO "+newValue+"]";
        }
        else if (operand == Filter.OP_WORD_BEGINS || operand == Filter.OP_HAS_WORD) {
            val =  field + ":" + value;
        }
        else if (operand == Filter.OP_GREATER_OR_EQUAL) {
            val =  field + ":[" + value + " TO *]";
        }
        else if (operand == Filter.OP_LESS_OR_EQUAL) {
            val =  field + ":[* TO "+value+"]";
        }
        else if (operand == Filter.OP_NOT_EQUAL) {
            val =  "*:* -" + field + ":" + value;
        }
        else if (operand == Filter.OP_BEGINS) {
            val =  field + ":" + value + "*";
        }
        else if (operand == Filter.OP_ENDS) {
            val =  field + ":" + "*" + value;
        }
        else if (operand == Filter.OP_CONTAINS) {
            val =  field + ":" + "(" + value + ")";
        }
        else if (operand == Filter.OP_GREEDY_BEGINS) {
        	val =  "("+field + ":" + value + " OR "+field + ":" + value+"* )";
        }
        
        if(negate) val = "-("+val+")";
        return val;
    }

}
