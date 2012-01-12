//Every plugin has to be in the xtracker package.
package xtracker.plugins.rawdataLoad;

//Some useful imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.*;

import java.util.List;
import java.util.Scanner;
import java.util.Vector;
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
import xtracker.utils.XMLparser;

public class loadRawMGF implements rawData_loadPlugin {

    /**
     * Loads raw data from MGF files specified in the paramFile
     * The details of MGF format can be found at http://www.matrixscience.com/help/data_file_help.html
     * @param paramFile the xml file specifying parameters of the plugin
     * @return xLoad the xLoad structure populated with MS/MS raw data.
     */
    public xLoad start(String paramFile) {
        xLoad ret = new xLoad();
        System.out.println(getName() + ": starting...");
        String rawDataFile = "";
        Pattern pattern;
        //let's load files to process
        loadParams(paramFile);

        //Let's load all raw data files specified in input
        for (int fileCnt = 0; fileCnt < rawDataFiles.size(); fileCnt++) {
            //The raw data file.
            rawDataFile = rawDataFiles.get(fileCnt);
            xLoadData myLoadData = new xLoadData(rawDataFile);

            File myFile = new File(rawDataFile);
            Scanner scanner = null;
            try {
                scanner = new Scanner(myFile);
            } catch (Exception e) {
                System.out.println("Error while opening file " + rawDataFile);
                System.out.println(e);
            }
            try {
                float parIonMz = -1f;
                float retTime = -1f;
                int charge = -1;
                float retTimeLoaded = -1f;

                ArrayList<Float> mzVals = new ArrayList<Float>();
                ArrayList<Float> intVals = new ArrayList<Float>();
                //first use a Scanner to get each line
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    pattern = Pattern.compile("^PEPMASS");
                    Matcher m = pattern.matcher(line);

                    if (m.find()) {
                        line = line.substring(8);
                        Float val = 0f;
                        String[] tmp = new String[2];
                        //If mz/intensity skip intensity
                        if (line.contains(" ")) {
                            tmp = line.split(" ");
                            line = tmp[0];
                        }

                        try {
                            parIonMz = Float.valueOf(line).floatValue();
                        } catch (NumberFormatException e) {
                            System.out.println("Error (LoadRawMGF): The value " + line + " is not a valid parent Ion M/Z value");
                            System.exit(1);
                        }
                    } else {
                        pattern = Pattern.compile("^CHARGE");
                        m = pattern.matcher(line);
                        if (m.find()) {
                            line = line.substring(7, line.length() - 1);
                            try {
                                charge = Integer.valueOf(line).intValue();
                            } catch (NumberFormatException e) {
                                System.out.println("Error (LoadRawMGF): The value " + line + " is not a valid charge state");
                                System.exit(1);
                            }
                        } else {
                            pattern = Pattern.compile("^RTINSECONDS");
                            m = pattern.matcher(line);
                            if (m.find()) {
                                line = line.substring(12, line.length());
                                try {
                                    retTime = Float.valueOf(line).floatValue();
                                    retTimeLoaded = retTime;
                                } catch (NumberFormatException e) {
                                    System.out.println("Error (LoadRawMGF): The value " + line + " is not a valid retention time in seconds value");
                                    System.out.println(e);
                                    System.exit(1);
                                }
                            } else {
                                pattern = Pattern.compile("^TITLE");
                                m = pattern.matcher(line);
                                if (m.find()) {
                                    //Let's try to retrieve the retention time (sometimes called elution time).
                                    //In this case we look for "Elution:" in the title or for "rt="
                                    int elTimeInd = line.indexOf("Elution:");
                                    if (elTimeInd > -1) {
                                        //OK elution time found!
                                        line = line.substring(elTimeInd + 8);
                                        //System.out.println("Elution time:" +line);
                                        line = line.trim();

                                        line = line.substring(0, line.indexOf(" "));
                                        //            System.out.println("El time:" +line);
                                        try {
                                            retTime = Float.valueOf(line).floatValue();
                                            retTimeLoaded = retTime;
                                        } catch (Exception e) {
                                            //Just nop. Value is set to 0 anyway.
                                        }
                                    } else {
                                        int rtTimeInd = line.indexOf("rt=");
                                        if (rtTimeInd > -1) {
                                            //OK retention time found!
                                            line = line.substring(rtTimeInd + 3);
                                            line = line.trim();
                                            line = line.substring(0, line.indexOf(" "));
                                            try {
                                                retTime = Float.valueOf(line).floatValue();
                                                retTimeLoaded = retTime;
                                            } catch (Exception e) {
                                                //Just nop. Value is set to 0 anyway.
                                            }
                                        } else {
                                            int rtTimeIndSN = line.indexOf("Scan Number:");
                                            if (rtTimeIndSN > -1) {
                                                //OK Scan Number found!
                                                line = line.substring(rtTimeIndSN + 12);

                                                line = line.trim();


                                                try {
                                                    retTime = Float.valueOf(line).floatValue();
                                                    retTimeLoaded = retTime;
                                                } catch (Exception e) {
                                                    //Just nop. Value is set to 0 anyway.
                                                }

                                            }
                                            // else{
                                            //   System.out.println("RET. TIME:" +retTime);
                                            //}
                                        }
                                    }
                                } else {
                                    pattern = Pattern.compile("^[0-9]+");
                                    m = pattern.matcher(line);
                                    if (m.find()) {
                                        String[] tmp = new String[2];
                                        String separator = "";
                                        if (line.contains("\t")) {
                                            separator = "\t";
                                        } else {
                                            separator = " ";
                                        }
                                        tmp = line.split(separator);
                                        mzVals.add(Float.valueOf(tmp[0]));
                                        intVals.add(Float.valueOf(tmp[1]));
                                    } else {
                                        pattern = Pattern.compile("^END IONS");
                                        m = pattern.matcher(line);
                                        if (m.find()) {
                                            //Let's reset retTimeLoaded
                                            if (retTimeLoaded == -1) {
                                                System.out.println("WARNING (LoadRawMGF): missing retention time information (zero assumed)");
                                                retTime = 0f;
                                            }
                                            retTimeLoaded = -1f;
                                            //Lets add values!
                                            xSpectrum spec = new xSpectrum();
                                            for (int ii = 0; ii < mzVals.size(); ii++) {
                                                spec.addElem(mzVals.get(ii), intVals.get(ii));
                                            }
                                            //Some error checking before saving values
                                            if (charge == -1) {
                                                System.out.println("Error (LoadRawMGF): Charge value missing!");
                                                System.exit(1);
                                            }
                                            if (retTime == -1f) {
                                                System.out.println("Error (LoadRawMGF): Retention time value missing!");
                                                System.exit(1);

                                            }
                                            if (parIonMz == -1f) {
                                                System.out.println("Error (LoadRawMGF): Parent Ion M/Z value missing!");
                                                System.exit(1);

                                            }
                                            myLoadData.addLcMsMsData(retTime, parIonMz, charge, spec);
                                        } else {
                                            //DO NOTHING or SET TO NOTHING ELEMENTS..
//                                            mzVals.removeAllElements();
//                                            intVals.removeAllElements();
                                            mzVals = new ArrayList<Float>();
                                            intVals = new ArrayList<Float>();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Problems while reading file " + rawDataFile);
                System.out.println(e);
            } finally {
                //ensure the underlying stream is always closed
                scanner.close();
            }
            ret.addDataElem(myLoadData);

            System.out.println("Done!");
            System.out.println("Datasize: " + ret.getDataSize());
            System.out.println(" Filename: " + ret.getDataElemAt(fileCnt).getFileName());
            System.out.println(" Lc/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsDataSize());
            System.out.println(" Ms/Ms Datasize: " + ret.getDataElemAt(fileCnt).getLcMsMsDataPointSize());
        }
        return ret;
    }

    /**
     * Opens the dataFile xml file and loads raw data files.
     * @param dataFile the parameter files
     */
    private void loadParams(String dataFile) {
        XMLparser parser = new XMLparser(dataFile);
        parser.validate("param");

        NodeList itemList = parser.getElement("param").getChildNodes();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getNodeName().equals("datafile")) {
                    rawDataFiles.add(item.getTextContent());
                }
            }
        }
    }

    /**
     * Gets the plugin name.
     * @return pluginName
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the plugin version.
     * @return pluginVersion
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the plugin type.
     * @return pluginType
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the plugin description.
     * @return pluginDescription
     */
    public String getDescription() {
        return description;
    }
    //The vector of rawDataFiles
    List<String> rawDataFiles = new ArrayList<String>();
    //The plugin name
    private final static String name = "LoadMGFdata";
    //The plugin version
    private final static String version = "1.00";
    //The plugin type(do not modify the string otherwise xTracker won't recognize the plugin!)
//    private final static String type = "RAWDATA_LOAD_plugin";
    //The plugin description
    private final static String description = "\t\tThis plugin loads MS/MS data from .MGF files.\n\tRetention time has to be specified in the \"TITLE\"\n\tsection of the file as \"Elution: XXX\" or as \"rt=XXX\".\n\tScan numbers are also accepted (though they do not suit some labeling techniques) \"Scan Number:XXX\".";
}