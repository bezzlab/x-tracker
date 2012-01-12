package xtracker.plugins.rawdataLoad;

import xtracker.plugins.rawdataLoad.misc.Scan;
import xtracker.plugins.rawdataLoad.misc.MSXMLParser;
import java.io.File;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import xtracker.data.xLoad;
import xtracker.data.xLoadData;
import xtracker.data.xSpectrum;

public class loadRawMzXML implements rawData_loadPlugin {

    public xLoad start(String paramFile) {


        xLoad ret = new xLoad();
        System.out.println(getName() + ": starting...");
        String rawDataFile = "";
        String inputDataFile = "";
        //let's load files to process
        this.loadParams(paramFile);

//        System.out.println("Please type the raw data file's path in...");
//        BufferedReader keyb = new BufferedReader(new InputStreamReader(System.in));
//        try{
//            rawDataFile = keyb.readLine();
//        }
//        catch(Exception e){
//            System.out.println("Error: Input problems!");
//            System.exit(1);
//        }

        //Let's read the raw data (note example works for .mgf only).




        for (int fileCnt = 0; fileCnt < rawDataFiles.size(); fileCnt++) {

            Scan myscan = null;
            rawDataFile = rawDataFiles.elementAt(fileCnt);
            //inputDataFile=identificationFiles.elementAt(fileCnt);
            MSXMLParser myParser = new MSXMLParser(rawDataFile);

            System.out.println("OK parsing " + myParser.getScanCount() + " scans.");

            xLoadData inputData = new xLoadData(rawDataFile);

            xSpectrum msSpectrum;
            int i = 0; //the scan counter.
            int totScans = myParser.getScanCount();
            for (i = 1; i <= totScans; i++) {
                //       System.out.println("Inserting scan " + i + "/" + totScans);
                myscan = myParser.rap(i);
                if (myscan.getMsLevel() == 1) {
                    //OK it is a Ms scan: populate xLcMsData
                    float retVal = Double.valueOf(myscan.getDoubleRetentionTime()).floatValue();

                    int numPeaks = myscan.getPeaksCount();
                    float mzVal;
                    float intVal;
                    float peaksVect[][] = myscan.getMassIntensityList();

                    msSpectrum = new xSpectrum();
                    for (int j = 0; j < numPeaks; j++) {
                        mzVal = peaksVect[0][j];
                        intVal = peaksVect[1][j];

                        //Only values with intensity>0 are stored
                        if (intVal > 0) {
                            msSpectrum.addElem(mzVal, intVal);
                            // msSpectraTmp.addElem(retVal, mzVal, intVal);
                            //  System.out.println("Values inserted: (" + retVal + "," + mzVal + "," + intVal + ")");

                        }
                    }

                    inputData.addLcMsData(retVal, msSpectrum);

                    //Following lines to debug ms data
                    //   if(i==3){
                    //                     System.out.println(myscan);
                    // }


                } else {
                    if (myscan.getMsLevel() == 2) {
                        //OK it is a Ms/Ms scan: populate xLcMsMsData
                        float retTime = Double.valueOf(myscan.getDoubleRetentionTime()).floatValue();
                        float parIonMz = myscan.getPrecursorMz();
                        int chargeValue = myscan.getPrecursorCharge();

                        xSpectrum msMsSpectrum = new xSpectrum();
                        float peaksVect[][] = myscan.getMassIntensityList();
                        int numPeaks = myscan.getPeaksCount();
                        float mzVal;
                        float intVal;
                        for (int j = 0; j < numPeaks; j++) {
                            mzVal = peaksVect[0][j];
                            intVal = peaksVect[1][j];
                            //Only values with intensity>0 are stored
                            if (intVal > 0) {

                                msMsSpectrum.addElem(mzVal, intVal);


                            }
                        }
                        if (chargeValue == -1) {
                            System.out.println("Error (LoadRawMzXML): no charge value specified!");
                            System.exit(1);
                        }
                        if (retTime == -1) {
                            System.out.println("Error (LoadRawMzXML): retention time specified!");
                            System.exit(1);
                        }
                        if (parIonMz == -1) {
                            System.out.println("Error (LoadRawMzXML): no parent Ion MZ  specified!");
                            System.exit(1);
                        }
                        inputData.addLcMsMsData(retTime, parIonMz, chargeValue, msMsSpectrum);
                        //System.out.println(myscan);
                    }


                    //      System.out.println(myscan);
                }
                //      System.out.println(myscan);
            }



            ret.addDataElem(inputData);

            System.out.println("Done!");
            System.out.println("Datasize: " + ret.getDataSize());
            System.out.println(" Filename: " + ret.getDataElemAt(fileCnt).getFileName());
            System.out.println(" Lc/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsDataSize());
            System.out.println(" Ms/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsMsDataPointSize());


        }

        /*  //FOR DEBUGGING
        xLoadData myLD = ret.getDataElemAt(0);
        System.out.println("Showing data from: " + myLD.getFileName());
        xLcMsMsData myMSMS= myLD.getLcMsMsDataElemAt(0);
        System.out.println(" -- RET TIME:" + myMSMS.getRetTime());
        xLcMsMsElem myMSMSel = myMSMS.getLcMsMsElemAt(0);
        
        System.out.println(" --- charge: " + myMSMSel.getCharge() + " par ion mz:" + myMSMSel.getParentIonMz() );
        
        float[][] mySp = myMSMSel.getSpectrum().getSpectrum();
        
        for(int spInd=0; spInd<mySp.length;spInd ++){
        System.out.println(mySp[spInd][0] + " " + mySp[spInd][1]);
        
        }
        System.exit(1);
         */
        return ret;
    }

    /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     * @return ret an array of float mass shifts.
     */
    public void loadParams(String dataFile) {
        //mass shifts.
        //the order is the following:




        int i = 0;
        int index = 2;
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("param").item(0);

            String schemaLocation = "";

            if (nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null) {
                schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
            } else {
                if (nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation") != null) {
                    schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                } else {
                    System.out.println("ERROR: No .xsd schema is provided for " + dataFile);
                    System.exit(1);
                }
            }



            // load the xtracker WXS schema
            Source schemaFile = new StreamSource(new File(schemaLocation));
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance
            Validator validator = schema.newValidator();

            try {
                validator.validate(new DOMSource(doc));
            } catch (SAXException e) {
                // instance document is invalid!
                System.out.println("\n\nERRROR - could not validate the input file " + dataFile + "!");
                System.out.print(e);
                System.exit(1);
            }
            nodeLst = doc.getElementsByTagName("param").item(0);

            NodeList itemList = nodeLst.getChildNodes();
            for (i = 0; i < itemList.getLength(); i++) {
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("inputFiles")) {

                        Node nodeLstIn = doc.getElementsByTagName("inputFiles").item(0);
                        NodeList itemListIn = nodeLstIn.getChildNodes();
                        for (int j = 0; j < itemListIn.getLength(); j++) {
                            Node itemI = itemListIn.item(j);

                            if (itemI.getNodeName().equals("datafile")) {
                                rawDataFiles.addElement(itemI.getTextContent());
                                //  identificationFiles.addElement(itemI.getAttributes().item(0).getTextContent().toString());
                                // System.out.println("Read files:" + itemI.getTextContent() + " " + itemI.getAttributes().item(0).getTextContent().toString());
                            }
                        }
                    }

                    //  if(item.getNodeName().equals("datafile")){
                    //  rawDataFiles.addElement(item.getTextContent());
                    //   identificationFiles.addElement(item.getAttributes().item(0).getTextContent().toString());
                    //  System.out.println("Read files:" + item.getTextContent() + " " + item.getAttributes().item(0).getTextContent().toString());
                    //}


                }
            }


        } catch (Exception e) {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(1);
        }


    }

    public boolean stop() {
        System.out.println(getName() + ": stopping...");
        return true;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
    Vector<String> rawDataFiles = new Vector<String>();
    Vector<String> identificationFiles = new Vector<String>();
    private final static String name = "Load MZXML files";
    private final static String version = "1.00";
//    private final static String type = "RAWDATA_LOAD_plugin";
    private final static String description = "This plugin loads data (both MS and MS/MS) from .mzXML files thanks to preoteome center jrap library.";
}