/*
 * --------------------------------------------------------------------------
 * loadRawMzML110.java
 * --------------------------------------------------------------------------
 * Description:       Plugin to load mzML files (Version 1.1)
 * Developer:         Faviel Gonzalez
 * Created:           03 February 2012
 * Read our documentation file under our Google SVN repository
 * SVN: http://code.google.com/p/x-tracker/
 * Project Website: http://www.x-tracker.info/
 * --------------------------------------------------------------------------
 */

package xtracker.plugins.rawdataLoad;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import xtracker.data.xLoad;
import xtracker.data.xLoadData;
import xtracker.data.xSpectrum;

/**
 *
 * @author Faviel Gonzalez
 */
public class loadRawMzML111 implements rawData_loadPlugin {
    
    Vector<String> rawDataFiles = new Vector<String>();
    private final static String name = "loadRawMzML111";
    private final static String version = "1.0";
    private final static String description = "This plugin loads raw data (MS and MS/MS) from .mzML raw files (Version 1.1.1)";
    private ArrayList<MzMLUnmarshaller> iUnmarshaller = null;    
    
    /**
     * The start method. 
     * @param paramFile a string (that might be empty) containing the file name of parameters if any.	
     * @return a valid xLoad structure filled with raw data information only.
     */    
    public xLoad start(String paramFile) {        
        
        //... Loading data structure to fill raw data ...//
        xLoad ret = new xLoad();        
        System.out.println("Loading " + getName() + " plugin ...");
        String sDataType = "MS1";

        //... Initializing mzML file to read ...//
        String rawDataFile = "";
        
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("Current heap size " + heapSize/(1024*1024) + " MB  ...");
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("Max heap size " + heapMaxSize/(1024*1024) + " MB  ...");
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println("Free memory " + heapFreeSize/(1024*1024) + " MB  ...");                
        
        //... Loading all files included in the .xtp file ...//
        System.out.println("Reading " + paramFile + " file ...");
        this.loadParams(paramFile);        
        for (int iFiles = 0; iFiles < rawDataFiles.size(); iFiles++)
        {
                rawDataFile = rawDataFiles.elementAt(iFiles);
                File xmlFile = new File(rawDataFile);
                        
                //... Unmarshall data using jzmzML API ...//
                System.out.println("Unmarshalling starts at " + System.currentTimeMillis());
                iUnmarshaller = new ArrayList<MzMLUnmarshaller>();
                MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(xmlFile);
                iUnmarshaller.add(unmarshaller);
                System.out.println("Unmarshalling ends at " + System.currentTimeMillis());                                

                //... Creating object for samples ...//
                CVList cvList = unmarshaller.unmarshalFromXpath("/cvList", CVList.class);              
                System.out.println("mzML file = " + unmarshaller.getMzMLId());
                System.out.println("Version = " + unmarshaller.getMzMLVersion());
                System.out.println("Accession = " + unmarshaller.getMzMLAccession());
                System.out.println("cvList = " + cvList.getCount());

                //... Calculating number of spectra ...//
                int iSpectrums = unmarshaller.getObjectCountForXpath("/run/spectrumList/spectrum");
                System.out.println("Scans = " + iSpectrums);
                
                //... Reading spectrum data ...//
                MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
                xLoadData inputData = new xLoadData(rawDataFile);
                xSpectrum msSpectrum;
                while (spectrumIterator.hasNext())
                {
                    //... Reading each spectrum ...//
                    Spectrum spectrum = spectrumIterator.next();

                    //... Reading CvParam to identify the MS level (1, 2) ...//
                    String mslevel = "";
                    List<CVParam> specParam = spectrum.getCvParam();
                    for (Iterator lCVParamIterator = specParam.iterator(); lCVParamIterator.hasNext();)
                    {
                        CVParam lCVParam = (CVParam) lCVParamIterator.next();
                        if (lCVParam.getAccession().equals("MS:1000511"))
                        {
                            mslevel = lCVParam.getValue().trim();
                        }
                    }
                    System.out.println("SpectrumID = " + spectrum.getIndex().longValue() + " with MS level = " + mslevel);

                    //... Getting Retention Time (rt) ...//
                    float rt = 0;
                    String unitRT = "";
                    List<CVParam> scanParam = spectrum.getScanList().getScan().get(0).getCvParam();
                    for (Iterator lCVParamIterator = scanParam.iterator(); lCVParamIterator.hasNext();)
                    {
                        CVParam lCVParam = (CVParam) lCVParamIterator.next();
                        if (lCVParam.getAccession().equals("MS:1000016"))
                        {
                            unitRT = lCVParam.getUnitAccession().trim();
                            if (unitRT.equals("UO:0000031"))
                            {    
                                rt = Float.parseFloat(lCVParam.getValue().trim()) * 60;
                            }
                            else
                            {
                                rt = Float.parseFloat(lCVParam.getValue().trim());
                            }
                        }                        
                    }            
                    // System.out.println("Retention time = " + rt);                 

                    if ((mslevel.toString().indexOf("1") >= 0)  && (sDataType.toString().indexOf("MS1") >= 0)) //... For MS1 Data ...//
                    {
                        //... Reading mz and intensities from binary data ...//
                        List<BinaryDataArray> bdal = spectrum.getBinaryDataArrayList().getBinaryDataArray();
                        BinaryDataArray mzBinaryDataArray = (BinaryDataArray) bdal.get(0);
                        BinaryDataArray intenBinaryDataArray = (BinaryDataArray) bdal.get(1);
                        Number[] mzValues = mzBinaryDataArray.getBinaryDataAsNumberArray();
                        Number[] intenValues = intenBinaryDataArray.getBinaryDataAsNumberArray();
                        // System.out.println("mzElements = " + mzValues.length);
                        // System.out.println("IntElements = " + intenValues.length);

                        int iJ=0;
                        float mzVal;
                        float intVal;
                        msSpectrum = new xSpectrum();
                        while (iJ < mzValues.length)
                        {
                            mzVal = mzValues[iJ].floatValue();                                              
                            intVal = intenValues[iJ].floatValue();
                            if (intVal > 0)
                            {
                                msSpectrum.addElem(mzVal, intVal);
                            }                            
                            iJ++;
                        }
                        System.out.println(" Specs = " + iJ);
                        inputData.addLcMsData(rt, msSpectrum);
                        // System.out.println("It has read the first spectrum with mz and intensities at a given rt. ");                        
                    }
                    else                                  //... For MS2 Data ...//
                    {                        
                        if ((mslevel.toString().indexOf("2") >= 0) && (sDataType.toString().indexOf("MS2") >= 0))
                        {
                            PrecursorList plist = spectrum.getPrecursorList(); //... Get precursor ion ...//
                            if (plist != null)
                            {
                                if (plist.getCount().intValue() == 1)
                                {                    
                                    List<CVParam> scanPrecParam = plist.getPrecursor().get(0).getSelectedIonList().getSelectedIon().get(0).getCvParam();
                                    float parIonMz =0;
                                    int parCharge = 0;
                                    
                                    //... Detect parent ion m/z and charge 
                                    for (Iterator lCVParamIterator = scanPrecParam.iterator(); lCVParamIterator.hasNext();)
                                    {
                                        CVParam lCVParam = (CVParam) lCVParamIterator.next();
                                        if (lCVParam.getAccession().equals("MS:1000744"))
                                        {
                                            parIonMz = Float.parseFloat(lCVParam.getValue().trim());
                                        }
                                        if (lCVParam.getAccession().equals("MS:1000041"))
                                        {
                                            parCharge = Integer.parseInt(lCVParam.getValue().trim());
                                        }
                                    }      
                                    // System.out.println("parIonMz = " + parIonMz);
                                    // System.out.println("parCharge = " + parCharge);
                                    xSpectrum msMsSpectrum = new xSpectrum();

                                    //... Binary data ...//
                                    List<BinaryDataArray> bdal = spectrum.getBinaryDataArrayList().getBinaryDataArray();

                                    //... Reading mz Values (Peaks) ...//
                                    BinaryDataArray mzBinaryDataArray = (BinaryDataArray) bdal.get(0);
                                    BinaryDataArray intenBinaryDataArray = (BinaryDataArray) bdal.get(1);
                                    Number[] mzValues = mzBinaryDataArray.getBinaryDataAsNumberArray();
                                    Number[] intenValues = intenBinaryDataArray.getBinaryDataAsNumberArray();
                                    // System.out.println("mzElements = " + mzValues.length);
                                    // System.out.println("IntElements = " + intenValues.length);                                    

                                    int iJ=0;
                                    float mzVal;
                                    float intVal;
                                    while (iJ < mzValues.length)
                                    {
                                        mzVal = mzValues[iJ].floatValue();                                              
                                        intVal = intenValues[iJ].floatValue();
                                        if (intVal > 0)
                                        {
                                            msMsSpectrum.addElem(mzVal, intVal);
                                        }
                                        iJ++;
                                    }           
                                    System.out.println(" Specs = " + iJ);
                                    inputData.addLcMsMsData(rt, parIonMz, parCharge, msMsSpectrum);
                                }
                            }  //... If precursor ion
                        } //... If MS2
                    } //... Else        
                }   //... While
                // add the complete dataset to xLoad
                ret.addDataElem(inputData);                                

                System.out.println("loadMzML Done!");
                System.out.println("Data size: " + ret.getDataSize());
                System.out.println("Filename: " + ret.getDataElemAt(iFiles).getFileName());
                System.out.println("Lc/Ms Datasize: " + ret.getDataElemAt(iFiles).getLcMsDataSize());
                System.out.println("Ms/Ms Datasize: " + ret.getDataElemAt(iFiles).getLcMsMsDataPointSize());                          
    
        } //... for ...//
        
        return ret;

    }
    /**
     * Method that loads the parameters used in the plugin from an xml file
     * @param dataFile The complete path to the xml file to read in
     */
    public void loadParams(String dataFile) {

        //... Open the xml file ...//
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);        
        try 
        {
            //... Parsing the file ...//
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);                        

            Node nodeLst = doc.getElementsByTagName("param").item(0);
            NodeList itemList = nodeLst.getChildNodes();

            //... Read all the tags inside param
            for (int iI = 0; iI < itemList.getLength(); iI++) 
            {
                Node item = itemList.item(iI);
                if (item.getNodeType() == Node.ELEMENT_NODE) 
                {
                    if (item.getNodeName().equals("datafile")) 
                    { 
                        rawDataFiles.addElement(item.getTextContent()); 
                        System.out.println(item.getTextContent() + " file loaded");
                    }
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(0);
        }

    }    
    /**
     * Method to retrieve the name of the plugin.
     * @return A string with the plugin name.	
     */    
    public String getName() 
    {
        return name;
    }
    /**
     * Method to retrieve the version of the plugin.
     * @return A string with the plugin version.	
     */    
    public String getVersion() 
    {
        return version;
    }
    /**
     * Method to retrieve the type of the plugin.
     * @return A string with the plugin type.	
     */
    public String getType() 
    {
        return type;
    }
    /**
     * Method to retrieve the description of the plugin.
     * @return A string with the plugin description.	
     */    
    public String getDescription() 
    {
        return description;
    }       
}
