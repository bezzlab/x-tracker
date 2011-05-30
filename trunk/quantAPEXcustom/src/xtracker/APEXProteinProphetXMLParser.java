/*
 *  All code within this program is � J. Craig Venter Institute.  All rights reserved.  April 15, 2008.
 *  This software may utilize packages authored by other organizations that have been licensed to JCVI 
 *  via a GNU General Public License (GPL), the GNU Lesser General Public License (LGPL), or some other 
 *  form of copyleft.  This software is licensed to the USER under GPL version three-point-zero (3.0).  
 *  To view the text of this license, please visit: http://www.gnu.org/licenses/gpl.txt.
 *  
 *  THIS SOFTWARE IS PROVIDED �AS IS� WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, 
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR USE OF A PARTICULAR PURPOSE, OR NONINFRINGEMENT.
 *      
 *   
 *  APEX Quantitative Proteomics Tool
 *  Author: John C. Braisted
*/
package xtracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;


import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The APEXProteinProphetXMLParser parses a ProteinProphet protein XML file and
 * constructs an APEXProteinList object.  The constituent APEXProtein object have
 * idType, id, description, ni, and pi set and are ready for APEX computations 
 *  
 * @author braisted
 *
 */
public class APEXProteinProphetXMLParser {
	
	/**
	 * Protein list represented in the XML file
	 */
	private APEXProteinList proteinList;
	/**
	 * Hashtable of FDR values, key = lower protein prob. limit as a String, 
	 * values are Vectors of fdr related values stored in the vector as Strings
	 * Each vector contains fdr string, pred num corr, pred num incorrect
	 */
	//private Hashtable fdrTable;
	
	/**
	 * Default constructor
	 */
	public APEXProteinProphetXMLParser() { 
		proteinList = new APEXProteinList();
	}
	
	/**
	 * Reads an input ProteinProphetXML file and returns an APEXProteinList object
	 * The constituent APEXProtein object have idType, id, description, ni, 
	 * and pi set and are ready for APEX computations
	 * @param fileURI file URI
	 * @return APEXProtein List
	 * @throws SAXException
	 * @throws IOException
	 */
	public APEXProteinList readProtXML(String filePath, boolean includePeptides) throws SAXException, IOException {
		XMLContentHandler handler = new XMLContentHandler(includePeptides);
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(handler);		
		InputSource input = new InputSource("file:///"+filePath);				
		reader.parse(input);
		//handler has populated list
		return proteinList;
	}
	
	 public APEXProteinList getProteinList() {
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
		private String description;
		private int pepCnt;
		private double prob;

		private String lowerProbBin;
		private Vector fdrValueVector;
				
		private boolean includePeptides;		
		private ArrayList<Peptide> peptideVector;
		
		private boolean acceptProtein;
		
		/**
		 * Constructor
		 * @param incPeptides indicator if peptides should be included in load
		 */
		public XMLContentHandler(boolean incPeptides) { 
			this.includePeptides = incPeptides;
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
				 pepCnt = Integer.parseInt(atts.getValue("total_number_peptides"));
				 prob = Double.parseDouble(atts.getValue("probability"));
				 description = "";
				 
				 if(includePeptides)
					 peptideVector = new ArrayList<Peptide>();
				 
			 } else if(localName.equals("annotation")) {
				 description = atts.getValue("protein_description");
			 } 
			 
			 if(includePeptides) {
				 if(localName.equals("peptide")) {
					 peptideVector.add(new Peptide(atts.getValue("peptide_sequence").toUpperCase(Locale.US),new String[0],new int[0]));
				 }
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
				 				 
				 if(!includePeptides)
					 addAPEXProtein(id, description, pepCnt, prob);
				 else
					 addAPEXProtein(id, description, pepCnt, prob, peptideVector);					 
			 }
		 }

		 
		 /**
		  * adds a new apex protein, triggerd by element end
		  * @param id protein id
		  * @param description protein description
		  * @param ni pep count
		  * @param pi protein id prob.
		  */
		 private void addAPEXProtein(String id, String description, int ni, double pi) {
			 APEXProtein protein = new APEXProtein();
			 //protein.setDescription(description);
			 protein.setNi(ni);
			 protein.setPi(pi);
			 //protein.setIdType(parsePrimaryAccType(description));
			 protein.setId(parsePrimaryAcc(id));
			 proteinList.addProtein(protein);
		 }
		 
		 
		 /**
		  * Adds a protein * and a peptide vector
		  * @param id protein id
		  * @param description protein description
		  * @param ni pep count
		  * @param pi protein id prob.
		  * @param peptideList peptide list to append
		  */
		 private void addAPEXProtein(String id, String description, int ni, double pi, ArrayList<Peptide> peptideList) {
			 APEXProtein protein = new APEXProtein();
			 //protein.setDescription(description);
			 protein.setNi(ni);
			 protein.setPi(pi);
			// protein.setIdType(parsePrimaryAccType(description));
			 protein.setId(parsePrimaryAcc(id));
			 //set the peptide list
			 protein.setPeptideList(peptideList);
			 proteinList.addProtein(protein);
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
			 
			 /*
			 Code prior to 6/12/2008
			 
			 String id = description;
			 String trunk = "";
			 int startIndex = description.indexOf('|') + 1;  //added 1
			 //if a single | is found
			 if(startIndex != -1) {
				 //pass the |
				 trunk = description.substring(startIndex);			 
				 //look for another |
				 int endIndex = trunk.indexOf('|');
				 //if found then take the next token
				 if(endIndex != -1)
					 id = trunk.substring(0, endIndex);
				 else //else just take the trunk e.g. 'gi|82779433'
					 id = trunk;				 				 
			 }
			 
			 */
			 
			 return id;
		 }
		 
	}
	

	/*
	public static void main(String [] args) {
		APEXProteinProphetXMLParser parser = new APEXProteinProphetXMLParser();
		try {
			APEXProteinList list = parser.readProtXML("file:\\C:\\PFGRC\\Documents\\Proteomics Docs\\APEX\\devel_stuff\\interact-F086687-prot.xml", false);

			System.out.println("num prot = "+list.getProteinCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
