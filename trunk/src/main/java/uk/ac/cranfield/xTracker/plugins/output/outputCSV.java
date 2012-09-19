package uk.ac.cranfield.xTracker.plugins.output;

// Some useful imports for XML etc.
import java.io.BufferedWriter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xPeptideConsensus;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.data.xRatio;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.cranfield.xTracker.xTracker;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariable;

public class outputCSV implements outPlugin {

    /**
     * Prints the "3D" matrix with the results into a .csv file specified in the inputs of the plugin (in the xml file).
     * @param InputData the data to be printed.
     */
//    public void start(Data inputData1, String paramFile) {
    @Override
    public void start(String paramFile) {
        //Let's first load the CSV output file name in from the xml parameter file.
        System.out.println("Output CSV plugin starts");
        loadParams(paramFile);
        System.out.println(outputFileName);
        BufferedWriter out = null;
        HashMap<String,ArrayList<String>> msrun_assayIDs_map = new HashMap<String, ArrayList<String>>();
        HashMap<String,StringBuilder> featureSbs = new HashMap<String, StringBuilder>();
        List<StudyVariable> svs = xTracker.study.getMzQuantML().getStudyVariableList().getStudyVariable();
        //get assay ids
        ArrayList<String> allAssayIDs = new ArrayList<String>();
        for (MSRun msrun : xTracker.study.getMSRuns()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Feature quantiation in ");
            sb.append(msrun.getID());
            sb.append("\nFeature");
            ArrayList<String> assayIDs = new ArrayList<String>();
            for (Assay assay : msrun.getAssays()) {
                assayIDs.add(assay.getId());
                sb.append(",");
                sb.append(assay.getId());
            }
            sb.append("\n");
            msrun_assayIDs_map.put(msrun.getID(), assayIDs);
            allAssayIDs.addAll(assayIDs);
            featureSbs.put(msrun.getID(), sb);
        }
        
        //initialize strinb builders
        StringBuilder proteinSb = new StringBuilder();
        StringBuilder peptideSb = new StringBuilder();
        proteinSb.append("Protein quantitation\n"); 
        peptideSb.append("Peptide quantiation\n");
        
        if (xTracker.study.needProteinQuantitation()) {
            for (String quantitationName : xTracker.study.getQuantitationNames()) {
                proteinSb.append(quantitationName);
                proteinSb.append("\nProtein");
                for (String assayID : allAssayIDs) {
                    proteinSb.append(",");
                    proteinSb.append(assayID);
                }
                if(svs.size()>1){
                    for(StudyVariable sv:svs){
                        proteinSb.append(",");
                        proteinSb.append(sv.getId());
                    }
                }
                if(xTracker.study.isRatioRequired()){
                    for(xRatio ratio:xTracker.study.getRatios()){
                        proteinSb.append(",");
                        proteinSb.append(ratio.getId());
                    }
                }
                proteinSb.append("\n");
                for (xProtein protein : xTracker.study.getProteins()) {
                    proteinSb.append(protein.getAccession());
                    HashMap<String, Double> quants = protein.getQuantities(quantitationName);
                    for (String assay : allAssayIDs) {
                        proteinSb.append(",");
                        proteinSb.append(quants.get(assay));
                    }
                    if(svs.size()>1){
                        HashMap<String, Double> svValues = protein.getStudyVariableQuantities(quantitationName);
                        for(StudyVariable sv:svs){
                            proteinSb.append(",");
                            proteinSb.append(svValues.get(sv.getId()));
                        }
                    }
                    if(xTracker.study.isRatioRequired()){
                        HashMap<String, Double> ratioValues = protein.getRatios(quantitationName);
                        for(xRatio ratio:xTracker.study.getRatios()){
                            proteinSb.append(",");
                            proteinSb.append(ratioValues.get(ratio.getId()));
                        }
                    }
                    proteinSb.append("\n");
                }
                proteinSb.append("\n");
            }
        }
        
        if (xTracker.study.needPeptideQuantitation()) {
            for (String quantitationName : xTracker.study.getQuantitationNames()) {
                peptideSb.append(quantitationName);
                peptideSb.append("\nPeptide");
                for (String assayID : allAssayIDs) {
                    peptideSb.append(",");
                    peptideSb.append(assayID);
                }
                if(xTracker.study.isRatioRequired()){
                    for(Ratio ratio:xTracker.study.getAssayRatios()){
                        peptideSb.append(",");
                        peptideSb.append(ratio.getId());
                    }
                }
                peptideSb.append("\n");
                for (xProtein protein : xTracker.study.getProteins()) {
                    ArrayList<xPeptideConsensus> peptideCons = protein.getPeptides();
                    if (peptideCons == null) {
                        continue;
                    }
                    for (xPeptideConsensus pepCon : peptideCons) {
                        for (xPeptide peptide : pepCon.getPeptides()) {
                            peptideSb.append(peptide.getPeptideID());
                            HashMap<String, Double> quants = peptide.getQuantities(quantitationName);
                            for (String assay : allAssayIDs) {
                                peptideSb.append(",");
                                peptideSb.append(quants.get(assay));
                            }
                            if(xTracker.study.isRatioRequired()){
                                HashMap<String, Double> ratioValues = peptide.getRatios(quantitationName);
                                for(Ratio ratio:xTracker.study.getAssayRatios()){
                                    peptideSb.append(",");
                                    peptideSb.append(ratioValues.get(ratio.getId()));
                                }
                            }
                            peptideSb.append("\n");
                        }
                    }
                }
                peptideSb.append("\n");
            }
        }
//        //feature, now skipped
//        if(xTracker.study.getPipelineType()==Study.MS2_TYPE){
//        }
        
        try {
            out = new BufferedWriter(new FileWriter(outputFileName));
            if(xTracker.study.needPeptideQuantitation()){
                out.append(peptideSb.toString());
                out.append("\n");
            }
            if(xTracker.study.needProteinQuantitation()){
                out.append(proteinSb.toString());
                out.append("\n");
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while closing file " + outputFileName + "!\n" + e);
        }
        System.out.println("Output to CSV plugin done");
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
    private void loadParams(String dataFile) {
        XMLparser parser = new XMLparser(dataFile);
        parser.validate("param");
        int i = 0;

        NodeList itemList = parser.getElement("param").getChildNodes();
        for (i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getNodeName().equals("CSVfileName")) {
                    outputFileName = item.getTextContent();
                }
            }
        }
    }

    @Override
    public boolean supportMS1() {
        return true;
    }

    @Override
    public boolean supportMS2() {
        return true;
    }

    /**
     * Gets the plugin name.
     * @return pluginName
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the plugin version.
     * @return pluginVersion
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the plugin type.
     * @return pluginType
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Gets the plugin description.
     * @return pluginDescription
     */
    @Override
    public String getDescription() {

        return description;
    }
    //the filename .csv where results are gonna be written
    public String outputFileName = "";
    //The plugin name
    private final static String name = "outputCSV";
    //The plugin version
    private final static String version = "1.0";
    //The plugin type
//    private final static String type = "OUTPUT_plugin";
    //The plugin description
    private final static String description = "Writes to a .csv file the results of the quantification.\n\tInputs of the program are in a XML file specifying the output file name\n\t that has to be put in a <CVSfileName> tag.\n\tInformation in the .csv file will be arranged as follows:\n\t\tDataset Name 1, , ,...,\n\t\tProtein ID (Peptide sequence),Amount_1,Amount_2, ..., Amount_n\n\t\tDataset Name 2, , ,...,\n\t\tProtein ID (Peptide sequence),Amount_1,Amount_2, ..., Amount_n\n\twhere we assume to have n different experimental conditions.\n";
}