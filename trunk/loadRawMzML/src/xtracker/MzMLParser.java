
package xtracker;

/**
 * Class implementing a SAX parser in order to read mzML files
 * Adapted from the work of Robert M. Hubley
 */

import java.io.FileInputStream;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author laurie Tonon for X-Tracker
 */


    
    public final class MzMLParser{
	//
	// Protected class members
	//

	/** Namespaces feature id (http://xml.org/sax/features/namespaces). */
	protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

	/**
	 * Namespace prefixes feature id
	 * (http://xml.org/sax/features/namespace-prefixes).
	 */
	protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";

	/** Validation feature id (http://xml.org/sax/features/validation). */
	protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

	/**
	 * Schema validation feature id
	 * (http://apache.org/xml/features/validation/schema).
	 */
	protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

	/**
	 * Schema full checking feature id
	 * (http://apache.org/xml/features/validation/schema-full-checking).
	 */
	protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

	/**
	 * Dynamic validation feature id
	 * (http://apache.org/xml/features/validation/dynamic).
	 */
	protected static final String DYNAMIC_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";

	/** Continue after fatal error id. */
	protected static final String CONT_AFTER_FATAL_ERROR_ID = "http://apache.org/xml/features/continue-after-fatal-error";

	/** Default parser name. */
	protected static final String DEFAULT_PARSER_NAME = "xtracker.org.apache.xerces.parsers.SAXParser";

	/** Default namespaces support (false). */
	protected static final boolean DEFAULT_NAMESPACES = false;

	/** Default namespace prefixes (false). */
	protected static final boolean DEFAULT_NAMESPACE_PREFIXES = false;

	/** Default validation support (false). */
	protected static final boolean DEFAULT_VALIDATION = false;

	/** Default Schema validation support (false). */
	protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;

	/** Default Schema full checking support (false). */
	protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

	/** Default dynamic validation support (false). */
	protected static final boolean DEFAULT_DYNAMIC_VALIDATION = false;

	/** Default dynamic validation support (false). */
	protected static final boolean DEFAULT_CONT_AFTER_FATAL_ERROR = false;

	/** A handle to the XML parser */
	protected XMLReader parser = null;

	/** The file we are in charge of reading */
	protected String fileName = null;

	/** The MD5 signature found the last time we read the index */
	protected String fileMD5 = null;

	/** The indexHandler */
	protected IndexHandler indexHandler = null;


        /**
         * method to parse the mzML file
         * @param fileName the path of the file to parse
         */
	public MzMLParser(String fileName) {
            
		this.fileName = fileName;

		// The defaults for the parser
		boolean namespaces = DEFAULT_NAMESPACES;
		boolean namespacePrefixes = DEFAULT_NAMESPACE_PREFIXES;
		boolean validation = DEFAULT_VALIDATION;
		boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
		boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
		boolean dynamicValidation = DEFAULT_DYNAMIC_VALIDATION;

		// Create a new index handler
		indexHandler = new IndexHandler();

		// Create parser
		try
		{

			parser = XMLReaderFactory.createXMLReader();
                        
		} catch (Exception e)
		{
			System.err.println("error: Unable to instantiate parser ("
					+ DEFAULT_PARSER_NAME + ")");
                        System.err.println(e);
                }

		// Set parser features
		try
		{
			parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
		} catch (SAXException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ NAMESPACES_FEATURE_ID + ")");

		}
		try
		{
			parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, namespacePrefixes);
		} catch (SAXException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ NAMESPACE_PREFIXES_FEATURE_ID + ")");
		}
		try
		{
			parser.setFeature(VALIDATION_FEATURE_ID, validation);
		} catch (SAXException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ VALIDATION_FEATURE_ID + ")");
		}
		try
		{
			parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, schemaValidation);
		} catch (SAXNotRecognizedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ SCHEMA_VALIDATION_FEATURE_ID + ")");

		} catch (SAXNotSupportedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ SCHEMA_VALIDATION_FEATURE_ID + ")");
		}
		try
		{
			parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID,
					schemaFullChecking);
		} catch (SAXNotRecognizedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ SCHEMA_FULL_CHECKING_FEATURE_ID + ")");

		} catch (SAXNotSupportedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ SCHEMA_FULL_CHECKING_FEATURE_ID + ")");
		}
		try
		{
			parser.setFeature(DYNAMIC_VALIDATION_FEATURE_ID, dynamicValidation);
		} catch (SAXNotRecognizedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ DYNAMIC_VALIDATION_FEATURE_ID + ")");

		} catch (SAXNotSupportedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ DYNAMIC_VALIDATION_FEATURE_ID + ")");
		}
		try
		{
			parser.setFeature(CONT_AFTER_FATAL_ERROR_ID, false);
		} catch (SAXNotRecognizedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ CONT_AFTER_FATAL_ERROR_ID + ")");

		} catch (SAXNotSupportedException e)
		{
			System.err.println("warning: Parser does not support feature ("
					+ CONT_AFTER_FATAL_ERROR_ID + ")");
		}

		// Seek to the index of offsets;
		FileInputStream fileIN = null;
		int indexPosition = -1;
                
		try{
			fileIN = new FileInputStream(fileName);
			fileIN.skip(fileIN.available() - 500);
			byte[] bytes = new byte[500];
			int bytesRead = fileIN.read(bytes);
			String footer = new String(bytes, 0, bytesRead);
			int offset;
			if ((offset = footer.indexOf("<indexListOffset>")) == -1)
			{
				System.err.println("The index list offset was not found in the file " + fileName + " .");
                                System.err.println("The file may be not valid. Impossible to continue loading data");
                                System.exit(1);
			}
			footer = footer.substring(offset + "<indexListOffset>".length());
			int endIndex = footer.indexOf("</indexListOffset>");
			if (endIndex == -1)
			{
				System.err.println("The index list offset was not found in the file " + fileName + " .");
                                System.err.println("The file may be not valid. Impossible to continue loading data");
                                System.exit(1);
			}

			footer = footer.substring(0, endIndex);
			indexPosition = Integer.parseInt(footer);
		} catch (Exception e)
		{
			System.err.println("The following error occured in parsing the file " + fileName + ": " + e);
                        System.err.println("Impossible to continue loeading the data");
			System.exit(1);
		}

		// Parse to locate indexListOffset tag
		parser.setContentHandler(indexHandler);
		parser.setErrorHandler(indexHandler);
                
                // Now that we located the offset to the file index
		// we have to go back, open the file again at the offset of the index.
		try{
			fileIN.close();
			fileIN = new FileInputStream(fileName);
			fileIN.skip(indexPosition);
                        
		} 
                catch (Exception e){
			System.out.println("File exception in " + fileName +":" + e.getMessage());
			System.exit(1);
		}
		
                try{
			parser.parse(new InputSource(fileIN)); // parse the index of offsets => see IndexHandler class
                        
		} 
                catch(NumberFormatException e){
                     System.err.println("Error: File " + fileName +" contains an invalid index offset!: ");
                     System.exit(1);
                }
                catch(SAXException e){
                    if (!e.getMessage().equals("IdxReadException"))
			{
                    
                    System.err.println("WARNING: in file "+ fileName + ".");
                    System.err.println(e.getMessage());
                    System.err.println("The indexes were not loaded properly,impossible to continue loading the data");
                    System.exit(1);
                    }
                }
                catch (Exception e){
                    
                    System.err.println("error: Parse error occurred - "
						+ e.getMessage());
                    e.printStackTrace(System.err);
			
                    System.exit(1);
		}
                try
		{
			fileIN.close();
			
		} catch (Exception e)
		{
			System.out.println("File exception in " + fileName +":" + e.getMessage());
			System.exit(1);
		}

	}
        
        /**
         * method that return the number of spectra in the file
         * @return the number of spectra
         */
        public int getSpectrumCount(){
		return indexHandler.indexes.size();
                   
	}
         
        /**
         * method that parse a particular spectrum
         * @param specNumber the index of the spectrum
         * @return the spectrum built with the info from the file
         */
        public Spectrum rap(int specNumber){
            
		FileInputStream fileIN = null;

		try {
			fileIN = new FileInputStream(fileName);
			fileIN.skip(indexHandler.getSpectrumOffset(specNumber)); // open the file at the offset of the spectrum
		} 
                catch (Exception e){
			System.out.println("File exception in " + fileName +":" + e.getMessage());
			System.exit(1);
		}

                // a spetrum handler to parse the xml
		SpectrumHandler specHandler = new SpectrumHandler();
                
		try{
                    // parser the xml with the spectrum handler described above
			parser.setContentHandler(specHandler);
			parser.setErrorHandler(specHandler);
			parser.parse(new InputSource(fileIN));
		}

                catch (SAXParseException e){
			// ignore
		}
                catch (Exception e){
                 
                   Exception se = e;
		   if (e instanceof SAXException){
                       // just display a warning
                       se = ((SAXException) e).getException();
                       System.err.println("WARNING: in file "+ fileName + ".");
                       System.err.println(se.getMessage());
                       System.err.println("Please check your file as it can lead to problems in the following steps");
		   }
                   else{
                       //too dangerous, quit the programme
                        System.err.println("Error: Parse error occurred in file " + fileName +" - " + e.getMessage());

                        e.printStackTrace(System.err);
			
                        System.err.println("The file may be not valid. Impossible to continue loading the data.");
                        System.exit(1);
                   }
		  if (se != null){
			se.printStackTrace(System.err);
                  }
		  else{
			e.printStackTrace(System.err);
                  }
			
		}
		return ((Spectrum) specHandler.getSpectrum());
	}

    
}
