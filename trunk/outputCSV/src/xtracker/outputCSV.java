// Every plugin has to be in the xtracker package.
package xtracker;

// Some useful imports for XML etc.
import java.io.BufferedWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class outputCSV implements outPlugin
{
    /**
     * Prints the "3D" matrix with the results into a .csv file specified in the inputs of the plugin (in the xml file).
     * @param InputData the data to be printed.
     */
    public void start(xQuant InputData, String paramFile)
    {
       //Let's first load the CSV output file name in from the xml parameter file.
        loadParams(paramFile);

        BufferedWriter out=null;

        //If there are results to write about create a new file "outputFileName" to write the results in.
        int dataSize=InputData.getQuantificationDataSize();
        if(dataSize>0){
        try {
            out = new BufferedWriter(new FileWriter(outputFileName));

        } catch (IOException e) {
            System.out.println("Problems while writing to file " +outputFileName +"!\n" +e);
        }

        }
        //Let's loop through the quantification results of all the available datasets
        for(int dtaCnt=0;dtaCnt<dataSize;dtaCnt++){
            xQuantData myQntData=InputData.getElementAtIndex(dtaCnt);
            System.out.println("Writing results for dataset " + myQntData.getFileName());

            //Remember that combinations of proteinId, peptideSeq are unique!
            int pepInd=myQntData.getQuantitativeDataSize();
            int condInd=myQntData.getLabelsSize();

            //The header: for every different dataset the name is reported and all the quantification labels
            String header="Dataset " + myQntData.getFileName();

            for(int cCnt=0;cCnt<condInd;cCnt++){
               header+= "," + myQntData.getLabelAtIndex(cCnt) +" ,Error " + myQntData.getLabelAtIndex(cCnt);
            }
                header+="\n";
                try {
                out.write(header);
             }
            catch (IOException e) {
            System.out.println("Problems while writing to file " +outputFileName +"!\n" +e);
        }
            String outString="";


            //Lines after the header contain proteinID (peptideSeq) Amount1, ..., AmountN
            for(int i=0;i < pepInd;i++){
                    xQuantities myXqnt= myQntData.getQuantitativeDataElemAt(i);
                    outString=myXqnt.getProteinId() +" (" +myXqnt.getPeptideSeq() + ")";

                    //Let's loop through all the amounts and write them in the file
                    for(int k=0;k<condInd;k++){
                        String qntError=Float.valueOf(myXqnt.getQuantErrorAt(k)).toString();
                        if((qntError == "-1") || (qntError.length()==0)){
                            qntError=" - ";
                        }
                        outString+=" ,"+myXqnt.getQuantityAt(k) + "," +qntError;


                }
                 outString+="\n";
                try {
                out.write(outString);
             }
                catch (IOException e) {
                System.out.println("Problems while writing to file " +outputFileName +"!\n" +e);
        }

        }
    }
         try {
            out.close();

        } catch (IOException e) {
            System.out.println("Problems while closing file " +outputFileName +"!\n" +e);
        }

 }
    /**
     * Loads the parameters
     * @param dataFile a string with the xml file containing the parameters.
     * A sample xml file follows:
     * <?xml version="1.0" encoding="utf-8"?>
     *   <!--
     *       This is the outputCSV parameters file.
     *       It specifies the filename of the CSV file where quantitative results have to be written to.
     *   -->
     *  <param>
     *      <CSVfileName>./iTraqOutCSV.csv</CSVfileName>
     *  </param>
     *
     */
    public void loadParams(String dataFile){

        int i=0;
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try{
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("param").item(0);

            NodeList itemList=  nodeLst.getChildNodes();
            for(i=0; i<itemList.getLength(); i++){
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){
                   if(item.getNodeName().equals("CSVfileName")){
                        outputFileName=item.getTextContent();

                   }
                }
            }
        }
        catch(Exception e){System.out.println(e);}
    }

    
    /**
     * Gets the plugin name.
     * @return pluginName
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the plugin version.
     * @return pluginVersion
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Gets the plugin type.
     * @return pluginType
     */
    public String getType()
    {

        return type;
    }

    /**
     * Gets the plugin description.
     * @return pluginDescription
     */
    public String getDescription()
    {

        return description;
    }

    //the filename .csv where results are gonna be written
    public String outputFileName = "";
    //The plugin name
    private final static String name = "outputCSV";
    //The plugin version
    private final static String version = "1.0";
    //The plugin type
    private final static String type = "OUTPUT_plugin";
    //The plugin description
    private final static String description = "Writes to a .csv file the results of the quantification.\n\tInputs of the program are in a XML file specifying the output file name\n\t that has to be put in a <CVSfileName> tag.\n\tInformation in the .csv file will be arranged as follows:\n\t\tDataset Name 1, , ,...,\n\t\tProtein ID (Peptide sequence),Amount_1,Amount_2, ..., Amount_n\n\t\tDataset Name 2, , ,...,\n\t\tProtein ID (Peptide sequence),Amount_1,Amount_2, ..., Amount_n\n\twhere we assume to have n different experimental conditions.\n";
}