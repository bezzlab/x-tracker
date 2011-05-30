

package xtracker;

/**
 * Class implementing a parser to read mzML files and export information, using a DOM type parser
 * 
 */



import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 *
 * @author laurie Tonon for X-Tracker
 */

public class mzMLDomParser {

    /**
     * The file to parse
     */
    File file;

    /**
     * Constructor
     * @param name A String with the name of the file to parse
     */
    public mzMLDomParser(String name){
        File f=new File(name);
    }
 
 /**
  * method that returns a list of indexes of spectra in a mzML file
  * @return a list with the index of each spectrum in the file. The size of this list give the number of spectra
  * @throws java.lang.ArithmeticException
  */
 public  ArrayList getSpectrumCounts() throws java.lang.ArithmeticException{
     ArrayList indexes = new ArrayList();
     try{
         
        // read the file and parse it => create a node tree with the tags
            
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        
        
        // seek for every spectrum
        NodeList nodeLst = doc.getElementsByTagName("spectrum");
        
            for(int i=0; i<nodeLst.getLength();i++){ // for each spectrum, add the index to the list
             Node fstNode = nodeLst.item(i);
                Element fstElmnt = (Element) fstNode;
                String index=fstElmnt.getAttribute("index");
            
                indexes.add(Integer.parseInt(index));
            
            
            }
        
        }
        catch(Exception e){
            System.err.println("Error reading file " + file.getName() +" . Impossible to load data");
            e.printStackTrace();
            System.exit(1);
         }
         catch(java.lang.OutOfMemoryError e){
              throw new java.lang.ArithmeticException();
        }
     
     return indexes;
 }
 
 /**
  * method the parses one spectrum, and gatheres information to create a spectrum object
  * @param indx the index of the spectrum to parse
  * @return
  */
 public Spectrum rap(int indx){
     
     Spectrum spec=new Spectrum();
     
     try{
        
        // read the file and parse it => create a node tree with the tags 
            
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
         
        // seek for every spectrum
        NodeList nodeLst = doc.getElementsByTagName("spectrum");
         
  
        for (int s = 0; s < nodeLst.getLength(); s++) {

            Node fstNode = nodeLst.item(s);
              double[] mzArray =null;
              double[] intensArray=null;
    
            
                // select the spectrum of interest
                Element fstElmnt = (Element) fstNode;
                String index=fstElmnt.getAttribute("index");
                
                if (Integer.parseInt(index) == indx) {    
                
                    
                    spec.setNum(Integer.parseInt(index)); //select the num
                    
                    String nbPeaks=fstElmnt.getAttribute("defaultArrayLength"); // select the number of peaks
                    spec.setPeaksCount(Integer.parseInt(nbPeaks));
      
                    // select the ms level (need to read all cvParam tag because all the same
                    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("cvParam");
      
                    for(int i=0;i<fstNmElmntLst.getLength();i++){
       
                    Element fstNmElmnt = (Element) fstNmElmntLst.item(i);
                        if(fstNmElmnt.getAttribute("accession").equalsIgnoreCase("MS:1000511")){
                            String mslev=fstNmElmnt.getAttribute("value");
                            
                            spec.setMsLevel(Integer.parseInt(mslev));

                         } 
                    }
      
                //==============================
                // select the information about the selected ion
                //===============================
                    
                NodeList SelectIonLst = fstElmnt.getElementsByTagName("selectedIon"); //select the tag "selected ion"
      

                for(int i=0;i<SelectIonLst.getLength();i++){ 
          
                     Element SelectIonElmnt = (Element) SelectIonLst.item(i);
          
                     NodeList SelectIonParamLst = SelectIonElmnt.getElementsByTagName("cvParam");
          
                    for(int j=0;j<SelectIonParamLst.getLength();j++){ //read all the param
              
                        Element SelectIonParam = (Element) SelectIonParamLst.item(j);
              
                        if(SelectIonParam.getAttribute("accession").equalsIgnoreCase("MS:1000040")){// select precursor m/z

                            spec.setPrecursorMz(Float.parseFloat(SelectIonParam.getAttribute("value")));
                            
                         }
                        else if(SelectIonParam.getAttribute("accession").equalsIgnoreCase("MS:1000744")){// select precursor m/z (other possibility)
                         
                            spec.setPrecursorMz(Float.parseFloat(SelectIonParam.getAttribute("value")));
                            
                        }
                        if(SelectIonParam.getAttribute("accession").equalsIgnoreCase("MS:1000041")){ //select the charge state
                            
                            spec.setPrecursorCharge(Integer.parseInt(SelectIonParam.getAttribute("value")));
                            
                        }
              
                    }
          
   
                 }
      
                //==============================
                // select the information about the scan
                //===============================
      
                NodeList ScanLst = fstElmnt.getElementsByTagName("scan"); //select the "scan" tag
       
                for(int i=0;i<ScanLst.getLength();i++){
           
                     Element ScanElmnt = (Element) ScanLst.item(i);
          
                     NodeList ScanParamLst = ScanElmnt.getElementsByTagName("cvParam");
          
                     for(int j=0;j<ScanParamLst.getLength();j++){ //read all the param
              
                        Element ScanParam = (Element) ScanParamLst.item(j);
              
                        if(ScanParam.getAttribute("accession").equalsIgnoreCase("MS:1000016")){ // select the retention time
                            String unit=ScanParam.getAttribute("unitAccession");
                            float retTime=0;
                        
                            if(unit.equalsIgnoreCase("UO:0000031")){ // if unit = minute
                                
                                retTime=(Float.parseFloat(ScanParam.getAttribute("value")))*60;
                   
                             }
                            else if(unit.equalsIgnoreCase("UO:0000010")){ // if unit = second
                                
                                 retTime=Float.parseFloat(ScanParam.getAttribute("value"));
                                 
                            }
                         
                            spec.setRetentionTime(String.valueOf(retTime));
                        }
              
                    }
          
                }
       
       
                //==============================
                // select the information about the binary data
                //===============================
       
       
                NodeList BinaryLst = fstElmnt.getElementsByTagName("binaryDataArray"); // seek for an array of binary
        
                for(int i=0;i<BinaryLst.getLength();i++){
            
                        Element BinaryElmnt = (Element) BinaryLst.item(i);
            
                        NodeList BinaryParamLst = BinaryElmnt.getElementsByTagName("cvParam");
                        boolean compression =false; // flag to know of data compressed or not
                        int precision=0; //flag to know the precision (32 or 64)
                        int arrayType=-1; // flag to store the type or array read (intensity or m/z)
            
                        for(int j=0;j<BinaryParamLst.getLength();j++){ //read all the param
                
                            Element BinaryParam = (Element) BinaryParamLst.item(j);
                
                
                            if(BinaryParam.getAttribute("accession").equalsIgnoreCase("MS:1000523")){ // precision 64
                               
                                precision=64;
                                
                            }
                            if(BinaryParam.getAttribute("accession").equalsIgnoreCase("MS:1000521")){ // precision 32
                                
                                 precision=32;
                                 
                            }   
                            if(BinaryParam.getAttribute("accession").equalsIgnoreCase("MS:1000574")){ // zlib compression
                                 
                                 compression=true;
                                 
                            }
                            if(BinaryParam.getAttribute("accession").equalsIgnoreCase("MS:1000576")){ // no compression
                                 
                                 compression=false;
                                 
                            }
                            if(BinaryParam.getAttribute("accession").equalsIgnoreCase("MS:1000514")){ // mz array
                                
                                arrayType=0;
                                
                    
                            }
                            if(BinaryParam.getAttribute("accession").equalsIgnoreCase("MS:1000515")){ // intensity array
                             
                                arrayType=1;
                                
                            }
                
                   
                        }
            
                        
           
                        NodeList BinaryValLst = BinaryElmnt.getElementsByTagName("binary"); // seek fot the tag "binary", where the array starts
                        Element Binary = (Element) BinaryValLst.item(0);
           
                        
                        // decode the binary data
                        String BinaryValues=Binary.getTextContent();
                        byte[] tempArray=Base64.decode(BinaryValues);
                
              
                        // if compression, uncompress
                        if(compression){
                            
                             tempArray=ToolmzML.inflate(tempArray);
                    
                        }
                        
                        // if m/z array
                        if(arrayType == 0){
                    
                            if(precision== 64){
                                mzArray=ToolmzML.unpackFloat64(tempArray);
                            }
                             else if(precision == 32){
                                 mzArray=ToolmzML.unpackFloat32(tempArray);
                            }
                            else{
                                mzArray=new double[0];
                            }
                        }
                        //if intensity array
                        else if(arrayType==1){
                    
                            if(precision== 64){
                                intensArray=ToolmzML.unpackFloat64(tempArray);
                            }
                            else if(precision == 32){
                                intensArray=ToolmzML.unpackFloat32(tempArray);
                            }
                            else{
                                intensArray=new double[0];
                            }
                    }

                }
              
        
                // create the mzintensity list
                float[][] tmpMassIntensityList;
                if(mzArray.length != intensArray.length){
                    System.err.println("ERROR: two arrays don't have the same size");
                }
                else{
                    tmpMassIntensityList = new float[mzArray.length][2];
                    
                     for(int i=0; i<intensArray.length;i++){ 
                                  int j=0;
                                  tmpMassIntensityList[i][j+1]=(float)intensArray[i];
                                  tmpMassIntensityList[i][j]=(float)mzArray[i];

                    }
                    
                    spec.setMassIntensityList(tmpMassIntensityList);
                }
      
      
        }

        }
      } 
      catch (Exception e) {
            System.err.println("ERROR: an error occured while parsinge the file " + file.getName() +": impossible to load the data.");
            System.exit(1);
      }
  
     return spec;
 }
}