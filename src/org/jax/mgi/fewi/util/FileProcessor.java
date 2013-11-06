package org.jax.mgi.fewi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


/*
 * Utility class for processing Files as user input
 * 
 * @author kstone
 */
public class FileProcessor 
{
	 private static Logger logger = LoggerFactory.getLogger(FileProcessor.class);
	 
	 
	 private static char VCF_COMMENT_CHAR = '#';
	 private static char VCF_COL_DELIM = '\t';
	 private static int VCF_CHROMOSOME_COL = 0;
	 private static int VCF_COORDINATE_COL = 1;
	 private static int VCF_ROW_LIMIT = 100000;
	 
	 /*
	  * Reads throught a VCF file to find all the coordinates.
	  * 	Appends them all to the result string as comma separated list.
	  */
	 public static String processVCFCoordinates(MultipartFile vcf) throws IOException
	 {
		 InputStream inputStream = vcf.getInputStream();
		 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		 
		 StringBuilder sb = new StringBuilder("");
		 String line;
		 int count=0;
		 while ((line = bufferedReader.readLine()) != null)
		 {
			 if(count++ > VCF_ROW_LIMIT) break;
			 
			 // ignore comment lines
			 if(line.length()<1) continue;
			 if(line.charAt(0) == VCF_COMMENT_CHAR) continue;
			 
			 // we will work backwards from the highest column number to limit how much of the file we need to read.
			 int coordColStringIndex = getNthIndexOfCharacter(line,VCF_COL_DELIM,VCF_COORDINATE_COL);
			 if(coordColStringIndex<2) continue; // this must be atleast 2 to have any values
			 
			 String chromCoordSubStr = line.substring(0,coordColStringIndex);
			 int chromColStringIndex =  chromCoordSubStr.indexOf(VCF_COL_DELIM);
			 String chromosome = chromCoordSubStr.substring(0,chromColStringIndex);
			 String coordinate = chromCoordSubStr.substring(chromColStringIndex+1);
			 //logger.debug("processed line "+count+" chromosome="+chromosome+",coordinate="+coordinate);
			 
			 if(!"".equals(chromosome)) sb.append(chromosome).append(":");
			 sb.append(coordinate).append(",");
		 }
		 bufferedReader.close();
		 inputStream.close();
		 
		 return sb.toString();
	 }
	 
	 
	 /*
	  * utility function to get nth occurance of a character in a sourceString 
	  */
	 public static int getNthIndexOfCharacter(String sourceString,char character, int n)
	 {
		 int pos = sourceString.indexOf(character, 0);
		 while (n-- > 0 && pos != -1)
			 pos = sourceString.indexOf(character, pos+1);
		 return pos;
	 }
}
