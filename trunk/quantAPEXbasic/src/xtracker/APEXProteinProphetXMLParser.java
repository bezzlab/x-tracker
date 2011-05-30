/*
 *  Original code within this program is � J. Craig Venter Institute.  All rights reserved.  April 15, 2008.
 *
 *
THIS SOFTWARE IS PROVIDED ìAS ISî WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
INCLUDING WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR USE OF A
PARTICULAR PURPOSE, OR NONINFRINGEMENT;


IN UTILIZING THIS SOFTWARE, USER AGREES THAT JCVI WILL NOT BE LIABLE FOR ANY LOSS OR DAMAGES,
EITHER ACTUAL OR CONSEQUENTIAL, ARISING OUT OF OR RELATING TO THESE TERMS, OR TO USERíS OR ANY
THIRD PARTIES USE OR INABILITY TO USE THE SOFTWARE, OR TO USERíS RELIANCE UPON INFORMATION
OBTAINED FROM OR THROUGH A JCVI WEBSITE. IN PARTICULAR, JCVI WILL HAVE NO LIABILITY FOR ANY
CONSEQUENTIAL, INDIRECT, PUNITIVE, SPECIAL OR INCIDENTAL DAMAGES, WHETHER FORSEEABLE OR
UNFORSEEABLE (INCLUDING, BUT NOT LIMITED TO, CLAIMS FOR DEFAMATION, ERRORS, LOSS OF DATA,
OR INTERRUPTION IN AVAILABILITY OF DATA), ARISING OUT OF OR RELATING TO THESE TERMS, USERíS
USE OR INABILITY TO USE THE SOFTWARE, OR ANY PURCHASES ON A JCVI SITE, OR TO USERíS RELIANCE
UPON INFORMATION OBTAINED FROM OR THROUGH A JCVI SITE, WHETHER BASED ON CONTRACT, TORT, STATUTORY,
OR OTHER LAW, EXCEPT ONLY IN THE CASE OF DEATH OR PERSONAL INJURY WHERE AND ONLY TO THE
EXTENT THAT APPLICABLE LAW REQUIRES SUCH LIABILITY.

 *  Author: John C. Braisted
 *
 *  Modified by: Laurie Tonon for X-tracker
*/
package xtracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The APEXProteinProphetXMLParser parses a ProteinProphet protein XML file and
 * constructs a HashMap with the protein id and its corresponding probability.
 *  
 * @author John C. Braisted Modified by Laurie Tonon for X-Tracker
 *
 */
public class APEXProteinProphetXMLParser {
	
	/**
	 * Protein list represented in the XML file, with their pi value
	 */
	//private APEXProteinList proteinList;
	
        private HashMap proteinList;

	
	/**
	 * Default constructor
	 */
	public APEXProteinProphetXMLParser() { 
		proteinList = new HashMap();
	}
	
	/**
	 * Reads an input ProteinProphetXML file and returns a HashMap
         * with protein id and probability
	 * @param fileURI file URI
	 * @throws SAXException
	 * @throws IOException
	 */
	public HashMap readProtXML(String filePath) throws SAXException, IOException {
		XMLContentHandler handler = new XMLContentHandler();
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(handler);		
		InputSource input = new InputSource("file:///"+filePath);				
		reader.parse(input);
		//handler has populated list
		return proteinList;
	}

        /**
         *
         * @return the HashMap with all the proteins
         */
	 public HashMap getProteinList() {
		 return proteinList;
	 }
	 
	 
	/**
	 * The XMLContentHandler reads the input XML and parses the key attributes
	 * and creates the APEXProteinList
	 * 
	 * @author braisted
	 */
	private class XMLContentHandler extends DefaultHandler {
		
		private String id;

		private double prob;

                private boolean acceptProtein;
		
		/**
		 * Constructor
		 */
		public XMLContentHandler() { 
			
			acceptProtein = true;
		}
		
		/**
		 * content handler indicates start of an element
		 */
		 public void startElement(String uri, String localName, 
			String qName, Attributes atts) {
			 if(localName.equals("protein")) {
				 
				 //if it's not the 'a' sibling, first, move on
				 if( !(atts.getValue("group_sibling_id").equals("a")) ) {
					 acceptProtein = false;
					 return;
				 }
				 
				 acceptProtein = true;
				 id = atts.getValue("protein_name");
				
				 prob = Double.parseDouble(atts.getValue("probability"));

		 }
                         }
		 
		 /**
		  * content end element tag reached
		  */
		 public void endElement(String uri, String localName, String qName) {
			 if(localName.equals("protein")) {
				 
				 //if it's not the 'a' sibling, first, move on
				 if(!acceptProtein) {
					 acceptProtein = true;
					 return;
				 }
				 				 
				
					 proteinList.put(parsePrimaryAcc(id), prob);

		 }

		 }

		
		 
		 /**
		  * parses for the primary accession typ
		  * @param description description line
		  * @return
		  */
		 private String parsePrimaryAccType(String description) {
			 String idType = "UNK";
			 int endIndex = description.indexOf('|');			 
			 //iff | is found
			 if(endIndex != -1)
				 idType = description.substring(0, endIndex);
			 return idType;
		 }

		 
		 /**
		  * parses the accession
		  * @param description description line
		  * @return
		  */
		 private String parsePrimaryAcc(String description) {

			 //jcb 6/12/2008 altered code to accomodate sp headers
			 description = description.trim();			 
			 String id = "";
			 int index;			 
			 String [] splitLine;
			 
			 //if we have a pipe
			 if(description.indexOf("|") != -1) {

				 splitLine = description.split("\\|");
				 
				 //if we have two tokens
				 if(splitLine.length > 1) {
					 if(splitLine[0].equals("gi") || splitLine[0].equals("sp") || splitLine[0].equals("ref")) {
						 //id is second token
						 id = splitLine[1].trim();
					 } else {
						 //else split line is the first token, old sp or uniprot
						 id = splitLine[0].trim();
					 }
				 }
			 } else {
				 //no pipes
				 index = description.indexOf(" ");
				 if( index != -1) {
					 //have a space delimited format header
					 //Use indexOf to parse and pull identifier as first token.
					 id = description.substring(0,index-1);					 					 
				 } else {
					 //no pipes no spaces take full description
					 id = description;					 
				 }				 
			 }
			 
			 
			 
			 return id;
		 }
		 
	}
	


}
