package uk.ac.cranfield.xTracker;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.validation.Validator;
import uk.ac.liv.jmzqml.model.mzqml.Software;
import uk.ac.liv.jmzqml.model.mzqml.AbstractParam;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.DataProcessing;
import uk.ac.liv.jmzqml.model.mzqml.IdentificationFile;
import uk.ac.liv.jmzqml.model.mzqml.IdentificationFiles;
import uk.ac.liv.jmzqml.model.mzqml.InputFiles;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.ProcessingMethod;
import uk.ac.liv.jmzqml.model.mzqml.RawFile;
import uk.ac.liv.jmzqml.model.mzqml.RawFilesGroup;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLUnmarshaller;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.QuantitationLevel;
import uk.ac.cranfield.xTracker.data.Study;
import uk.ac.cranfield.xTracker.data.xRatio;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.RatioList;

/**
 * xTracker is the main class of the whole project. 
 * It contains the main and it manages plugins and data structures.
 * <p>
 * It loads the four plugins and checks if they are of the correct type before running each one of them.
 * X-tracker also checks the integrity of data structures after the execution of each script.
 * @see        xTracker
 * @author Jun Fan (j.fan@cranfield.ac.uk) -- Cranfield Health, Cranfield University --
 * @version 2.0
 * X-Tracker Project
 */
public class xTracker {
    /**
     * the main data structure
     */
    public static Study study = new Study();
    /**
     * the flags of the pipeline
     */
    public static boolean SUPPORT_MS1 = true;
    public static boolean SUPPORT_MS2 = true;
    /**
     * the parameters for the pipeline
     */
    private final static String version = "2.0";
    public static final String IDENTIFICATION_LOAD_TYPE = "load identification plugin";
    public static final String RAWDATA_LOAD_TYPE = "load raw spectra plugin";
    public static final String QUANTITATION_TYPE = "feature detection and quantitation plugin";
    public static final String OUTPUT_TYPE = "output plugin";
    /**
     * the pre-defined values for inference methods
     */
    public static int PEPTIDE_FEATURE_INFERENCE = QuantitationLevel.SUM;
    public static int PROTEIN_PEPTIDE_INFERENCE = QuantitationLevel.MEDIAN;
    public static int SV_ASSAY_INFERENCE = QuantitationLevel.MEAN;
    public static int PROTEIN_GROUP_PROTEIN_INFERENCE = QuantitationLevel.SUM;
    /**
     * defines the location of plugins
     */
    public static String PLUGIN_PATH = "Plugins/";
    /**
     * the plugin manager, populated during parsing the configuration file and then execute
     */
    private PluginManager manager;
    /**
     * the name of the xsd file for mzQuantML format
     */
    private final String MZQ_XSD = "mzQuantML_1_0_0-rc2.xsd";

    public static void main(String[] args) {
//        System.out.println(UnimodParser.getUnimodID("iTRAQ4plex"));
//        System.exit(0);
//        new xTracker("paper_iTraq4plex\\iTraqMzIDmzMLmzTab.mzq");
//        new xTracker("paper_iTraq4plex\\iTraqMzIDmzMLmzq.mzq");
//        new xTracker("paper_iTraq4plex\\iTraqMzIDmzMLcsv.mzq");
//        new xTracker("paper_iTraq4plex\\iTraqMzIDmgfCsv.mzq");
//        new xTracker("paper_iTraq4plex\\iTraqMascotMzMLmzq.mzq");
//        new xTracker("paper_iTraq4plex\\iTraqMascotMGFcsvSingle.mzq");
//        new xTracker("paper_iTraq4plex\\iTraqMascotMGFmzqMultiple.mzq");
//        System.exit(0);
        switch (args.length) {
            case 1: {
                new xTracker(args[0]);
                break;
            }
            case 2: {
                PLUGIN_PATH = args[1]+"/";
                new xTracker(args[0]);
                break;
            }
            default: {
                System.out.println("Usage: java xTracker <configuration.mzq> [plugin folder]");
                System.out.println("The configuration file is in mzQuantML format, which defines the parameters of the pipeline");
                System.out.println("The optional plugin folder indicates the folder where plugins and related files should be looked for, i.e. jar files, xtp files and xsd files");
                break;
            }
        }
    }
    /**
     * parse the given configuration file which is in mzQuantML format
     * @param filename the configuration file name
     */
    private void parseMzQuantML(String filename){
        Validator validator = XMLparser.getValidator(MZQ_XSD);
        boolean validFlag = XMLparser.validate(validator, filename);
        if(!validFlag){
            System.out.println("The mzQuantML validation went wrong, program terminated");
            System.exit(1);
        }
        //load the mzQuantML file into memory
        MzQuantMLUnmarshaller unmarshaller = new MzQuantMLUnmarshaller(filename);
        MzQuantML mzQuantML = unmarshaller.unmarshall();
        //assign to Study.mzQuantML
        study.setMzQuantML(mzQuantML);
        study.setFilename(filename);
        //deal with the CVs
        for(Cv cv:mzQuantML.getCvList().getCv()){
            xTracker.study.addCv(cv.getId(),cv);
        }

        //Set pipeline type and quantitation level flags from AnalysisSummary
        List<AbstractParam> paramList = mzQuantML.getAnalysisSummary().getParamGroup();
        Pattern pattern = Pattern.compile("^MS:(\\d+)");//PSI-MS CV accession pattern
        for(AbstractParam param: paramList){
            if(param instanceof CvParam){
                CvParam cvParam = (CvParam)param;
                String accession = cvParam.getAccession();
                if(accession != null){
                    Matcher m = pattern.matcher(accession);
                    if (m.find()) {//accession found
                        setFlagByAccession(Integer.parseInt(m.group(1)),cvParam);
                    }else{//accession not found 
                        setFlagByName(cvParam.getName(),cvParam.getValue());
                    }
                }
            }
        }
        study.autoSetQuantitationFlag();
        System.out.println("This is a MS"+study.getPipelineType()+" pipeline");
        
        //get pipeline configuration from DataProcessingList element which is mandotary
        List<DataProcessing> dpList = mzQuantML.getDataProcessingList().getDataProcessing();
        boolean pipelineGenerated = false;
        for(DataProcessing dp:dpList){
            if(pipelineGenerated) break;//all pipeline for xtracker is expected to under one DataProcessing
            Object tmp=dp.getSoftwareRef();
            if(tmp==null) continue;
            String swName = ((Software)tmp).getId();
            if(!swName.equalsIgnoreCase("xtracker")) continue;
            System.out.println("Pipeline consists of");
            pipelineGenerated = true;
            List<ProcessingMethod> pmList = dp.getProcessingMethod();
            for(ProcessingMethod pm:pmList){
                int step = pm.getOrder().intValue();
                List<AbstractParam> params = pm.getParamGroup();
                String pluginName = "";
                String pluginParam = "";
                String pluginType = "";
                for(AbstractParam param:params){
                    if(param instanceof UserParam){
                        UserParam userParam = (UserParam)param;
                        String name = userParam.getName().toLowerCase();
                        if(name.equals("plugin name")){
                            pluginName = userParam.getValue();
                        }else if(name.equals("plugin configuration file")){
                            pluginParam = userParam.getValue();
                        }else if(name.equals("plugin type")){
                            pluginType = userParam.getValue();
                        }else if(name.equals("feature to peptide inference method")){
                            PEPTIDE_FEATURE_INFERENCE = getInferenceMethod(userParam.getValue());
                        }else if(name.equals("peptide to protein inference method")){
                            PROTEIN_PEPTIDE_INFERENCE = getInferenceMethod(userParam.getValue());
//                        }else if(name.equals("protein to protein group inference method")){
//                            PROTEIN_GROUP_PROTEIN_INFERENCE = getInferenceMethod(userParam.getValue());
                        }else if(name.equals("assay to study variables inference method")){
                            SV_ASSAY_INFERENCE = getInferenceMethod(userParam.getValue());
                        }else if(name.equals("protein ratio calculation infer from peptide ratio")){
                            String value = userParam.getValue();
                            if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
                                study.setRatioInferFromPeptide(Boolean.parseBoolean(value));
                            }else{
                                System.out.println("Please only use true or false for userParam \"Protein ratio calculation infer from peptide ratio\"");
                                System.exit(1);
                            }
                        }
                    }
                }
                System.out.println("step "+step+": plugin "+pluginName+" for "+pluginType+" with parameter file "+pluginParam);
                if(pluginName.length()>0) manager.addPlugin(pluginName, pluginParam, pluginType);
            }
        }
        System.out.println("from feature to peptide:"+PEPTIDE_FEATURE_INFERENCE);
        System.out.println("from peptide to protein:"+PROTEIN_PEPTIDE_INFERENCE);
//        System.out.println("from protein to protein group:"+PROTEIN_GROUP_PROTEIN_INFERENCE);
        System.out.println("from assay to study variable:"+SV_ASSAY_INFERENCE);
        if(!pipelineGenerated){
            System.out.println("No pipeline found for xTracker in the DataProcessingList, program terminated");
            System.exit(1);
        }
        
        //the defined spectra and identification files
        InputFiles inputFiles = mzQuantML.getInputFiles();
        //raw files
        List<RawFilesGroup> rawFileGroups = inputFiles.getRawFilesGroup();
        for(RawFilesGroup rawFileGroup:rawFileGroups){
            MSRun msrun = new MSRun(rawFileGroup);
            study.addMSRun(rawFileGroup.getId(),msrun);
            List<RawFile> rawFileList = rawFileGroup.getRawFile();
            for(RawFile raw: rawFileList){
                File file = new File(raw.getLocation());
                if(!file.exists()){
                    System.out.println("The required raw spectral file "+file.getAbsolutePath()+ " can not be found");
                    System.exit(1);
                }
                System.out.println("Raw file: "+file.getAbsolutePath() );
                study.addRawfileMSRunMap(raw.getLocation(),rawFileGroup.getId());
            }
        }
        //identification files
        IdentificationFiles identificationFiles = inputFiles.getIdentificationFiles();
        if(identificationFiles==null){
            System.out.println("No identification files defined in the mzq configuration file, the pipeline can not be executed.");
            System.exit(1);
        }
        List<IdentificationFile> idenList = identificationFiles.getIdentificationFile();
        for(IdentificationFile iden:idenList){
            File file = new File(iden.getLocation());
            if (!file.exists()) {
                System.out.println("The required identification file " + file.getAbsolutePath() + " can not be found");
                System.exit(1);
            }
            System.out.println("Identification file: " + file.getAbsolutePath());
            study.addIdentificationFile(iden.getLocation());
        }
        
        //Assays
        List<Assay> assayList = mzQuantML.getAssayList().getAssay();
        for(Assay assay:assayList){
            Object obj = assay.getRawFilesGroupRef();
            if(obj == null) continue;
            MSRun msrun = study.getMSRun(((RawFilesGroup)obj).getId());
            msrun.addAssay(assay);
        }
        
        //Ratios
        RatioList ratioList = mzQuantML.getRatioList();
        if(ratioList!=null){
            if(!study.requirePeptideQuantitation()){
                System.out.println("Ratio calculation is defined, which however can not applied to peptide or protein as they are set to do no quantitation");
                System.exit(1);
            }
            for (Ratio qratio : ratioList.getRatio()) {
                xRatio ratio = new xRatio(qratio);
                study.addRatio(ratio);
                if(ratio.getType().equals(xRatio.ASSAY)) study.addAssayRatio(qratio);
            }
        }
    }
    /**
     * convert the string into integer which is due to the java 6 does not support switch on String which Java 7 does
     * @param value
     * @return 
     */
    private int getInferenceMethod(String value) {
        if(value.equalsIgnoreCase("mean")) return QuantitationLevel.MEAN;
        if(value.equalsIgnoreCase("median")) return QuantitationLevel.MEDIAN;
        if(value.equalsIgnoreCase("sum")) return QuantitationLevel.SUM;
        if(value.equalsIgnoreCase("weightedAverage")) return QuantitationLevel.WEIGHTED_AVERAGE;
        return -1;
    }
    /**
     * Set the pipeline type and quantitation flag from the MS accession number
     * @param accession PSI MS accession
     * @param value the flag value in the cvParam
     */
    private void setFlagByAccession(int accession,CvParam cvParam){
        boolean flag = false;
        String value = cvParam.getValue();
        if(value != null && value.equalsIgnoreCase("true")) flag = true;
        switch(accession){
            case 1001834: //LC-MS label-free quantitation analysis
            case 1001835: //SILAC quantitation analysis
            case 1001839: //metabolic labeling 14N / 15N quantitation analysis
            case 1002018: //MS1 label-based analysis
                study.setPipelineType(Study.MS1_TYPE);
                study.setQuantitationMethod(cvParam);
                return;
            case 1001836: //spectral counting quantitation analysis
            case 1001837: //iTRAQ quantitation analysis
            case 1001838: //SRM quantitation analysis
            case 1002009: //isobaric label quantitation analysis
            case 1002010: //TMT quantitation analysis
            case 1002023: //MS2 tag-based analysis
                study.setPipelineType(Study.MS2_TYPE);
                study.setQuantitationMethod(cvParam);
                return;
            case 1002001: //MS1 label-based raw feature quantitation
            case 1002019: //label-free raw feature quantitation is_a: MS:1001834
                study.setPipelineType(Study.MS1_TYPE);
                study.setFeatureQuantitationFlag(flag);
                return;
            case 1002002: //MS1 label-based peptide level quantitation
            case 1002020: //label-free peptide level quantitation is_a: MS:1001834
                study.setPipelineType(Study.MS1_TYPE);
                study.setPeptideQuantitationFlag(flag);
                return;
            case 1002003: //MS1 label-based protein level quantitation
            case 1002021: //label-free protein level quantitation is_a: MS:1001834
                study.setPipelineType(Study.MS1_TYPE);
                study.setProteinQuantitationFlag(flag);
                return;
            case 1002004: //MS1 label-based proteingroup level quantitation
            case 1002022: //label-free proteingroup level quantitation is_a: MS:1001834
                study.setPipelineType(Study.MS1_TYPE);
//                study.setProteinGroupQuantitationFlag(flag);
                return;
            case 1002024: //MS2 tag-based feature level quantitation
                study.setPipelineType(Study.MS2_TYPE);
                study.setFeatureQuantitationFlag(flag);
                return;
            case 1002015: //spectral count peptide level quantitation
            case 1002025: //MS2 tag-based peptide level quantitation
                study.setPipelineType(Study.MS2_TYPE);
                study.setPeptideQuantitationFlag(flag);
                return;
            case 1002016: //spectral count protein level quantitation
            case 1002026: //MS2 tag-based protein level quantitation
                study.setPipelineType(Study.MS2_TYPE);
                study.setProteinQuantitationFlag(flag);
                return;
            case 1002017: //spectral count proteingroup level quantitation
            case 1002027: //MS2 tag-based proteingroup level quantitation
                study.setPipelineType(Study.MS2_TYPE);
//                study.setProteinGroupQuantitationFlag(flag);
                return;
        }
    }
    /**
     * Set the pipeline type and quantitation flag from the cvParam name by guess
     * @param name PSI MS name
     * @param value the flag value in the cvParam
     */
    private void setFlagByName(String name,String value) {
        name = name.toLowerCase();
        boolean flag = false;
        if(value.equalsIgnoreCase("true")) flag = true;
        if(name.contains("protein")) {
            if (name.contains("group")){
//                study.setProteinGroupQuantitationFlag(flag);
            }else{
                study.setProteinQuantitationFlag(flag);
            }
            return;
        }
        if(name.contains("peptide")) {
            study.setPeptideQuantitationFlag(flag);
            return;
        }
        if(name.contains("feature")) {
            study.setFeatureQuantitationFlag(flag);
            return;
        }
    }
    
    public xTracker(String filename) {
        System.out.println("The plugin folder is "+PLUGIN_PATH);
        manager = new PluginManager();
        parseMzQuantML(filename);
//        outputMzTab plugin = new outputMzTab();
//        plugin.start("paper_iTraq4Plex/outputMzTab.xtp");
//        System.exit(0);
        manager.execute();
        System.out.println("*************************************************");
        System.out.println("** xTracker finished execution without errors! **");
        System.out.println("*************************************************");
    }

    /**
     * This method dynamically creates the index file with the description of
     * the parameters of all plugins.
     * @param descPath the path containing all the .html parameter file descriptions.
     * the old codes left since version 1.3, for the legacy purpose only
     */
    public void printHtmlHelp(String descPath) {

        String indexPath = "Documentation/pluginInfo.html";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String htmlPage = "";

        htmlPage += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n<title>X-Tracker's Plugin Description Page</title>\n</head>";
        htmlPage += "\n<body style=\"color: rgb(0, 0, 0);\" alink=\"#ee0000\" link=\"#0000ee\" vlink=\"#551a8b\">\n<table style=\"text-align: left; width: 100%; height: 100%;\">\n";
        htmlPage += "\n<tbody>\n<tr>\n\t<td style=\"width: 30px; background-color: rgb(51, 102, 255); vertical-align: top;\"><img alt=\"X-Tracker's parameters\" src=\"img/name.png\"></td>\n\t";
        htmlPage += "<td style=\"width: 7px;\">&nbsp;</td>\n\t<td style=\"vertical-align: top;\">\n\t\t";
        htmlPage += "<h1>This page contains information on all available X-Tracker plugins</h1>\n\t\t";
        htmlPage += "<div align=\"center\"><h3>[Page automatically generated by X-Tracker v." + getVersion() + " on " + sdf.format(cal.getTime()) + "]</h3></div><br>\n\t\t";
        htmlPage += "X-Tracker is a new piece of software allowing Mass Spectrometry-based protein quantitation. Through an abstraction of the main steps involved in quantitation, X-Tracker is able to support quantitation by means of several current protocols, both at MS or Tandem MS level, and provide a flexible, platform-independent and easy-to-use quantitation environment.<br><br>\n\t\t";
        htmlPage += "X-Tracker has been developed in the Bioinformatics Group of Cranfield University, UK.<br><br>Please remember to update this page whenever a new plugin is installed by calling:<br><br><code>java -jar xTracker.jar</code></td><td style=\"text-align: right; height: 100%; width: 39%; vertical-align: top;\"><img style=\"width: 400px;\" alt=\"xTracker\" src=\"img/XtrackerLogo.png\"></td><tr><td style=\"width: 30px; background-color: rgb(51, 102, 255); vertical-align: top;\">&nbsp;</td><td style=\"width: 7px;\">&nbsp;</td><td colspan=\"2\"><br><br><h2>Plugin parameter pages</h2>\n\t\t";

        htmlPage += "\t<table><tr align=\"left\"><th>File</th><th>Name</th><th>Type</th><th>Short Description</th></tr>";
        //Let's list now possible plugin descriptions
        File f = new File(descPath);  //plugins
        File files[] = f.listFiles();   //array of available plugins
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && (files[i].getName().indexOf(".html")) > 0 && (files[i].getName().indexOf("index.html")) == -1) {
                String pN = files[i].getName().substring(0, files[i].getName().indexOf(".html"));
                String pluginData[] = getPluginData(pN);
                if (pluginData[0].length() == 0) {
                    pluginData[0] = "Not available.";
                    pluginData[1] = "Not available.";
                    pluginData[2] = "Not available.";
                } else {
                    if (pluginData[2].length() > 60) {
                        pluginData[2] = pluginData[2].substring(0, 60) + "...";
                    }
                }
                htmlPage += "<tr><td><a href=\"..\\Plugins\\" + files[i].getName() + "\" >" + files[i].getName() + "</a><br></td><td>" + pluginData[1] + "</td><td>" + pluginData[0] + "</td><td>" + pluginData[2] + "</td></tr>\n\t\t";
            }
        }
        htmlPage += "\n\t\t\t</table>";

        htmlPage += "\n\t\t";
        htmlPage += "\n\t\t<br><br></td>\n</tr>\n</tbody>\n</table>\n</body>\n</html>";


        try {
            // Create file
            FileWriter fstream = new FileWriter(indexPath);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(htmlPage);
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error, couldn't write pluginInfo.html file: " + e.getMessage());
        }
        System.out.println("Descriptions of plugin parameters can be found at " + indexPath);
    }

    /**
     * Given a plugin name in input, this plugin retrieves information about it like type, name, description. 
     * @param pluginName the plugin name (without .jar extension)
     * @return a String array where:
     * retVal[0] is pluginType
     * retVal[1] is pluginName and version in parenthesis ex. myPlugin (v.1.00)
     * retVal[2] a short plugin description (max 60 chars).
     * the old codes left since version 1.3, for the legacy purpose only
     */
    public static String[] getPluginData(String pluginName) {
        String[] retVal = {"", "", ""};
        pluginName += ".jar";
        String pluginPath = "Plugins/";
        File pFile = new File(pluginPath + pluginName);
        //if the plugin exists then let's get some information about it otherwise just return empty values
        if (pFile.exists()) {
            pluginInterface plugin = null; //the plugin to load
            String className = "";    //the classname should be like the filename without the .jar
            int index = -1; //a counter to check if it is a jar or not
            if ((index = pluginName.indexOf(".jar")) >= 0) {
                className = pluginName.substring(0, index);
                try {
//                    pluginLoader = new PluginClassLoader(pluginPath + pluginName);
                } catch (Exception e) {
                }

                // Loading the class
                try {
//                    plugin = (pluginInterface) pluginLoader.findClass("xtracker." + className).newInstance();
                    retVal[0] = plugin.getType();
                    retVal[1] = plugin.getName() + " (v." + plugin.getVersion() + ")";
                    retVal[2] = plugin.getDescription();
                } catch (Exception e) {
                }
            }
        }
        return retVal;
    }
    /**
     * the old codes left since version 1.3, for the legacy purpose only
     */
    private void errorUsage() {
        printUsage();
        //Let's list now possible plugins (i.e. plugins that are in the folder Plugins\
        File f = new File(PLUGIN_PATH);  //plugins
        File[] files = f.listFiles();   //array of available plugins
        if (files == null) {
            System.out.println("There are no plugins in the fold " + f.getAbsolutePath());
        } else {
            System.out.println("\n\tThe following plugins are currently available (in \"Plugins\" folder):");
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && (files[i].getName().indexOf(".jar")) > 0) {
                    System.out.println("\t - " + files[i].getName());
                }
            }
        }
        //Let's print the index.html help file
//        printHtmlHelp("./Plugins/");
        System.exit(0);
    }
    /**
     * the old codes left since version 1.3, for the legacy purpose only
     */
    private void printUsage() {
        printVersion();
        System.out.println("     Usage: java -jar xTracker.jar configuration_file.mzq");
        System.out.println("");
        System.out.println("           where configuration_file.mzq is an mzQuantML file specifying the plugins and their parameter files and other experimental design information.");
        System.out.println("");
    }
    /**
     * the old codes left since version 1.3, for the legacy purpose only
     */
    private void printVersion() {
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("    **************************************");
        System.out.println("    *****  Welcome to X-Tracker v" + version + " *****");
        System.out.println("    **************************************");
        System.out.println("");
        System.out.println("");
    }
    /**
     * Return X-Tracker version number
     * @return version as a string
     */
    public String getVersion() {
        return version;
    }

}
