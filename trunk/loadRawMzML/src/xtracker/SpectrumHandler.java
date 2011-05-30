
package xtracker;

/**
 * Class used as a handler for a spectrum. This class is called by the SAX parser and implements the event methods
 * called when a tag is find. It create a spectrum and populate its attributes with the getter methods.
 * Part of xtracker and mzML parser
 * This class is adapted from the SAX2ScanHandler class written by Robert M. Hubley
 */

import java.util.zip.DataFormatException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author laurie Tonon for X-Tracker
 */
public final class SpectrumHandler extends DefaultHandler{
    
        /**
         * a spectrum to populate
         */
         protected Spectrum tmpSpec;
    
    	/** A string to hold the Base64 mass data */
	protected StringBuffer mzData = new StringBuffer();
        
        /** A string to hold the Base64 intensity data */
        protected StringBuffer intensData=new StringBuffer();

	/** Flag to indicate we are reading a peak tag */
	protected boolean inPeak = false;

	/** Buffer to hold characters while getting precursorMZ value */
	protected StringBuffer precursorBuffer;

	/** Flag to indicate if we are reading the precursor MZ value */
	protected boolean inPrecursorMZ = false;
        
        /**
         * flag to indicate that we are reading mass data
         */
        protected boolean MZarray=false;
        
        /**
         * flag to indicate that we are reading intensity data
         */
        protected boolean IntensArray=false;
        
        /**
         * integer to store temporarly the precision of the data, as we don't know yet if it is mass or intensity data
         */
        protected int precisionTemp=0;
        
        /**
         * a flag to indicate if the data are compressed
         */
        protected boolean compression=false;
        
        /**
         * an array to store the mass values for the spectrum
         */
        protected  double[] valuesMZ=null;
        
        /**
         * an array to store the intensity values for the spectrum
         */
        protected double[] valuesIntens=null;
        
        
        /**
         * 
         * @return the spectrum created
         */
        public Spectrum getSpectrum(){
		return (tmpSpec);
	}
        
        /**
         * method to get the value of an attribute of a tag that is an integer value
         * @param attre the attributes of the tag considered
         * @param name the name of the attribute considered
         * @return the integer value of the attribute considered
         */
        private int getIntAttribute(Attributes attrs, String name){
		int result;

		if (attrs.getValue(name) == null) // attribute not present
			return -1;

		try{
			result = Integer.parseInt(attrs.getValue(name));
		}
                catch (NumberFormatException e){
			result = -1;
		}
		return (result);
	}

        /**
         * method to get the value of an attribute of a tag that is a float value
         * @param attrs the attributes of the tag considered
         * @param name the name of the attribute considered
         * @return the float value of the attribute considered
         */
	private float getFloatAttribute(Attributes attrs, String name){
		float result;

		if (attrs.getValue(name) == null) // attribute not present
			return -1;

		try{
			result = Float.parseFloat(attrs.getValue(name));
		} 
                catch (NumberFormatException e){
//			
			result = -1;
		} 
                catch (NullPointerException e1) {
//			
			result = -1;
		}
		return (result);
	}

        	/** Start document. */
	public void startDocument() throws SAXException
	{
		// Nothing to do
	} // startDocument()

        /**
         * event called each time an open tag is found
         * @param uri
         * @param local
         * @param raw
         * @param attrs
         * @throws org.xml.sax.SAXException
         */
        public void startElement(String uri, String local, String raw, Attributes attrs)throws SAXException{
                    
            if (raw.equals("spectrum")){ // the open tag of a spectrum is found
                        this.tmpSpec=new Spectrum(); //create a new spetrum
                        tmpSpec.setNum(getIntAttribute(attrs, "index")); // get the index of the spectrum
                        tmpSpec.setPeaksCount(getIntAttribute(attrs, "defaultArrayLength")); //get the number of peaks
                        
            }
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000511")){ // get the ms level
                        tmpSpec.setMsLevel(getIntAttribute(attrs,"value"));
            }
            // get the selected ion m/Z value if exists
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000744") ){
                    tmpSpec.setPrecursorMz(getFloatAttribute(attrs,"value"));
                }
            else if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000040")){
                     tmpSpec.setPrecursorMz(getFloatAttribute(attrs,"value"));
            }
            // get the charge sate if exists
           if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000041")){ 
                     tmpSpec.setPrecursorCharge(getIntAttribute(attrs,"value"));
             }
             // get the retention time
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000016")){
                
                String ret=attrs.getValue("unitAccession");
                

                    tmpSpec.setRetentionTime(attrs.getValue("value"));
                    

            }
            
            // get the precision of the data read
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000523")){ // 64-bit float
                    precisionTemp=64;
            }
            
            // get the precision of the data read
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000521")){ // 32-bit float
                  precisionTemp=32;
            }
            
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000574")){ // get if compression or not
                compression=true; 
            }
            
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000514")){ //  detect we will read m/z array
                MZarray=true;
                IntensArray=false;
                tmpSpec.setPrecisionMass(precisionTemp);
            }
            
            if(raw.equals("cvParam") && attrs.getValue("accession").equalsIgnoreCase("MS:1000515")){ // detect we will read intensity array
                IntensArray=true;
                MZarray=false;
                tmpSpec.setPrecisionIntens(precisionTemp);
            }
            
            if(raw.equals("binary")){ // detect the start of a binary array
                inPeak=true;
            }
        }
        
        /**
         * Event called each time a closing tag is found
         * @param uri
         * @param local
         * @param raw
         * @throws org.xml.sax.SAXException
         */
        public void endElement(String uri, String local, String raw) throws SAXException
	{
            byte[] tmpArrIntens;
            byte[] tmpArrMZ;

             
            if(raw.equals("binary")){ // detect that we finished reading a binary array
               
     
                    if(MZarray){ // if we finished reading mass values, we create an array with these values
                        
                          
                        tmpArrMZ = Base64.decode(mzData.toString()); // data encoded in base64, so decode it
                        
                        if(compression){ // if the data are compressed with zlib compression
                                // decompress
                                try{
                                   tmpArrMZ=ToolmzML.inflate(tmpArrMZ); 
                                  
                                }
                                catch(DataFormatException e){
                                    throw new SAXException("data could not be uncompressed");
                                    
                                }
                                
                        }
                        // the data are encoded with a precision that is needed to decode it
                        if(tmpSpec.getPrecisionMass() == 64){ // if precision is 64
                             valuesMZ= ToolmzML.unpackFloat64(tmpArrMZ);
                          }
                        else if(tmpSpec.getPrecisionMass() == 32){ // if precision is 32
                             valuesMZ= ToolmzML.unpackFloat32(tmpArrMZ);
                          }
                        else{
                            
                                valuesMZ=new double[1];
                          }
                    }
 
                    // if we finished reading intensities values
                    if(IntensArray){ 


                           tmpArrIntens=Base64.decode(intensData.toString()); // data encoded in base64, so decode it
                           
                            if(compression){ // if the data are compressed with zlib compression
                                // decompress
                                try{
                                   
                                   tmpArrIntens=ToolmzML.inflate(tmpArrIntens);
                                }
                                catch(DataFormatException e){
                                    throw new SAXException("data could not be uncompressed");
                                }
                                
                            }
                           
                             // the data are encoded with a precision that is needed to decode it
                             if(tmpSpec.getPrecisionIntens() == 64){ //if precision is 64
                                 valuesIntens=ToolmzML.unpackFloat64(tmpArrIntens);
                             }
                             else if(tmpSpec.getPrecisionIntens() == 32){ // if precision is 32
                                valuesIntens=ToolmzML.unpackFloat32(tmpArrIntens);
                             }
                             else{
                                valuesIntens=new double[1];
                             }

                      }

                
                inPeak = false; // signals that we finished reading binaries
                compression=false;//signals that no compression anymore until maybe the next binary

          }  
            
          if(raw.equals("binaryDataArrayList")){ // we finished reading all the data
                // the mass-intensities array for the spectrum
                float[][] tmpMassIntensityList;
                
                
                if(valuesMZ.length != valuesIntens.length){ // if not the same number of values for intensities and mass, big problem

                              throw new SAXException("m/z and intensity data are not the same size");
                              
                }
                else{
                     
                    tmpMassIntensityList = new float[valuesMZ.length][2];
                     
                    try{
                    // put the data from 2 arrays: intensities and mass array, in one big array
                        for(int i=0; i<valuesIntens.length;i++){ 
                                  int j=0;
                                  tmpMassIntensityList[i][j+1]=(float)valuesIntens[i];
                                  tmpMassIntensityList[i][j]=(float)valuesMZ[i];

                         }
                    }
                    catch(Exception e){
                        
                        throw new SAXException(e.getMessage());
                    }
                     tmpSpec.setMassIntensityList(tmpMassIntensityList); // give the mass/intensities array to the spectrum
                        
                     }
                     // finished , empty everything
                     tmpArrMZ = null;
                     tmpArrIntens=null;
                     tmpMassIntensityList = null;
                     mzData.delete(0, mzData.capacity());
                     intensData.delete(0, intensData.capacity());
                
          }
        
            
        }
        
        /**
         * Event called each time characters between two tags are found
         * @param ch
         * @param start
         * @param length
         * @throws org.xml.sax.SAXException
         */
        public void characters(char ch[], int start, int length)throws SAXException{
            
            // if we found characters and we are reading binary data
            if (inPeak){
                if(MZarray){ // if it is mass data
                    
                    mzData.append(ch, start, length); // put the characters into a buffer
                     
                    
                }
                else if(IntensArray){ // if it is intensity data
                    intensData.append(ch, start, length); // put the characters into a buffer
                }
            }
            
        }
        
        /** Ignorable whitespace. */
	public void ignorableWhitespace(char ch[], int start, int length)
		throws SAXException
	{
		// Do nothing
	} // ignorableWhitespace(char[],int,int);

	/** Processing instruction. */
	public void processingInstruction(String target, String data)
		throws SAXException
	{
		// Do nothing
	} // processingInstruction(String,String)

	//
	// ErrorHandler methods
	//

	/** Warning. */
	public void warning(SAXParseException ex) throws SAXException
	{
		// Do nothing
		//printError("Warning", ex);
	} // warning(SAXParseException)

	/** Error. */
	public void error(SAXParseException ex) throws SAXException
	{
		// Do nothing
		//printError("Error", ex);
	} // error(SAXParseException)

	/** Fatal error. */
	public void fatalError(SAXParseException ex) throws SAXException
	{
		// Do nothing
		//printError("Fatal Error", ex);
	} // fatalError(SAXParseException)

	

}
