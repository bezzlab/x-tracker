

package xtracker;

import java.io.File;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Plugin to load empty data in case the quantification is made only on the
 *  identification data. It reads the parameters file of the plugin to load
 *  identifications and create an xLoad structure with the name of all the files
 *  specified. The raw part of xLoad  will never be used.
 *  The aim is to be able to load the identification data later, without raw data.
 * @author laurie Tonon for X-Tracker
 */
public class loadDummyRaw implements rawData_loadPlugin {

    /**
     * The start method. 
     * @param paramFile a string (that might be empty) containing the file name of parameters if any.	
     * @return a valid xLoad structure filled with raw data information only.
     */
    public xLoad start(String paramFile) {
        xLoad ret = new xLoad();
        String filename = "";

        // loads the name of all the identifications files that will be loaded later
        this.loadParams(paramFile);

        // for each file, create and xLoadData structure with dummy infos and add it to xLoad
        for (int fileCnt = 0; fileCnt < rawDataFiles.size(); fileCnt++) {
            
            filename = rawDataFiles.get(fileCnt);

            xLoadData inputData = new xLoadData(filename);

            xSpectrum msSpectrum = new xSpectrum();

            msSpectrum.addElem(1, 1);

            inputData.addLcMsData(1, msSpectrum);

            ret.addDataElem(inputData);

           
        }
         return ret;
    }

    /**
     * Method to load the parameters from an xml file. Uses a dom parser to read the tags
     * @param dataFile the path to the xml file with the parameters
     */
    public void loadParams(String dataFile) {
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        int i = 0;
        try {
            //open and read the file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("param").item(0);
            NodeList itemList = nodeLst.getChildNodes();
            for (i = 0; i < itemList.getLength(); i++) {
                // read all the tags inside param
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("filename")) {
                        rawDataFiles.addElement(item.getTextContent());
                      
                    }
                }
            }

            if (rawDataFiles.size() == 0) {
                System.out.println("ERROR, no filename in the parameter file (" +dataFile + "), impossible to create the data structure");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Exception while reading the parameter file " + dataFile + "\n" + e);
            System.err.println("Impossible to continue the quantifiation");
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * The list of file names to load
     */
    Vector<String> rawDataFiles = new Vector<String>();

    /**
     * Method to retrieve the name of the plugin.
     * @return A string with the plugin name.	
     */
    public String getName() {
        return name;
    }

    /**
     * Method to retrieve the version of the plugin.
     * @return A string with the plugin version.	
     */
    public String getVersion() {
        return version;
    }

    /**
     * Method to retrieve the type of the plugin.
     * @return A string with the plugin type.	
     */
    public String getType() {

        return type;
    }

    /**
     * Method to retrieve the description of the plugin.
     * @return A string with the plugin description.	
     */
    public String getDescription() {

        return description;
    }
    /**
     * Put in this String the name of your plugin.
     */
    private final static String name = "loadEmptyRawData";
    /**
     * The version of the plugin goes here.
     */
    private final static String version = "1.0";
    /**
     * The plugin type. For an RAW DATA LOAD plugin it must be RAWDATA_LOAD_plugin (do not change it).
     */
    private final static String type = "RAWDATA_LOAD_plugin";
    /**
     * A string with the description of the plugin.
     */
    private final static String description = "This is a plugin to load empty raw data when the quantification is made" +
            " only with the identification data. It use the name of the identification file specified in a parameters file" +
            " to create an almost empty xLoad structure ";
}
