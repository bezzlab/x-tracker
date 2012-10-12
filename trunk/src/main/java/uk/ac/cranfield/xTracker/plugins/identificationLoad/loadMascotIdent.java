package uk.ac.cranfield.xTracker.plugins.identificationLoad;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import javax.xml.validation.Validator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import uk.ac.cranfield.xTracker.Utils;
import uk.ac.ebi.pride.jmztab.MzTabParsingException;
import uk.ac.liv.jmzqml.model.mzqml.Param;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xModification;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.cranfield.xTracker.xTracker;
/**
 * 
 * @author Jun Fan<j.fan@cranfield.ac.uk>
 */
public class loadMascotIdent extends identData_loadPlugin {
    /**
     * reflect the relationship between raw spectral files and their corresponding identification files
     */
    private HashMap<String, String> spectra_identification_map = new HashMap<String, String>();
    /**
     * the modifications and their shift values stored in the plugin configuration file
     * only used when the modification is not found in the mascot file
     */
    private HashMap<String, Float> modsFromXtp = new HashMap<String, Float>();
    /**
     * the modifications and their shift values contained in the mascot identification file
     */
    private HashMap<String, Float> modsFromMascot = new HashMap<String, Float>();
    /**
     * list of fixed modifications on the terminal acids
     */
    private ArrayList<xModification> fixedTerms = new ArrayList<xModification>();
    /**
     * keys are the amino acids which are modified, and values are the modification e.g. Oxidation (M)
     */
    private HashMap<String, String> fixedModifications = new HashMap<String, String>(); 
    /**
     *keys are the indices of the variable modification which is used in the modification position string, and values are the modification e.g. Oxidation (M) 
     */
    private HashMap<Integer, String> variableModifications = new HashMap<Integer, String>(); 
    /**
     * the set of peptides identified with more than one protein
     */
    private HashSet<String> removedPeptide = new HashSet<String>();
    /**
     * the threshold of the mascot score, above which the identification will be used
     */
    private float scoreThreshold = 0f;
    /**
     * the location of mascot xsd file
     */
    private final static String MASCOT_XSD = "mascot_search_results_2.xsd";//the xsd file downloaded from http://www.matrixscience.com/help/export_help.html
    /**
     * the validator to validate mascot result file according to the mascot xsd file
     */
    private Validator validator;
    /**
     * plugin information variables
     */
    private final String name = "Load_MascotXML";
    private final String version = "1.00";
    private final String description = "Loads Mascot xml identification files\n\tThis plugin loads peptide identifications from a Mascot .xml file\n\tor set of files. It expects retention \n\ttimes (associated to spectra to which identifications belong)\n\twithin the <pep_scan_title> tag.\n\tPeptides with same sequence identified in more than a protein are\n\tremoved.\n\n";

    @Override
    public void start(String paramFile) {
        System.out.println(getName() + ": starting...");
        //parse the parameter file and populate the field
        loadParams(paramFile);
        //get the validator
        validator = XMLparser.getValidator(MASCOT_XSD);
        //spectra_identification_map contain the original location of files defined in the parameter file
        for(String rawSpectra:spectra_identification_map.keySet()){
            String identFile = spectra_identification_map.get(rawSpectra);
            String msrunRaw = xTracker.study.getMSRunIDfromRawFile(rawSpectra);
            if(msrunRaw==null){
                System.out.println("The raw file "+rawSpectra+" defined in "+paramFile+" can not be found in the mzQuantML file "+xTracker.study.getFilename());
                System.exit(1);
            }
            String msrunIdent = xTracker.study.getMSRunIDfromIdentificationFile(identFile);
            if(msrunIdent == null){
                System.out.println("The identification file "+identFile+" defined in "+paramFile+" can not be found in the mzQuantML file "+xTracker.study.getFilename());
                System.exit(1);
            }
            if(msrunIdent.equals(xTracker.study.UNASSIGNED)){//this identification file has not been parsed
                xTracker.study.setIdentificationFileMSRunMap(identFile, msrunRaw);
                String identLocation = Utils.locateFile(identFile, xTracker.folders);
//                boolean validFlag = XMLparser.validate(validator, identFile);
                boolean validFlag = XMLparser.validate(validator, identLocation);
                if(!validFlag){
                    System.out.println("The identification file "+identFile+" is not a proper Mascot file");
                    System.exit(1);
                }
                System.out.println(identFile+" is valid, now start to parse...");

                File file = new File(identLocation);
                //parse the mascot result XML file
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);

                try {
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(file);
                    doc.getDocumentElement().normalize();
                    loadMascotMods(doc);
                    NodeList header = doc.getElementsByTagName("header").item(0).getChildNodes();
                    String fasta = "";
                    String database = "";
                    for (int i = 0; i < header.getLength(); i++) {
                        Node item = header.item(i);
                        if (item.getNodeType() == Node.ELEMENT_NODE) {
                            if (item.getNodeName().equals("FastaVer")) {
                                fasta = item.getTextContent();
                            } else if (item.getNodeName().equals("DB")) {
                                database = item.getTextContent();
                            }
                        }
                    }
                    SearchDatabase sd = xTracker.study.getSearchDatabase(database);
                    if (sd == null) {//the first time to see the currect searchDatabaseRef
                        //autoResolving set to true in line 211 for DBSequence
                        sd = new SearchDatabase();
                        sd.setId(database);
                        sd.setLocation(fasta);
                        UserParam userParam = new UserParam();
                        userParam.setName(fasta);
                        Param param = new Param();
                        param.setParamGroup(userParam);
                        sd.setDatabaseName(param);
                        xTracker.study.addSearchDatabase(database, sd);
                    }

                    //<xs:element name="hits" minOccurs="0">
                    NodeList hits = doc.getElementsByTagName("hits");
                    if (hits == null || hits.getLength() == 0) {
                        System.out.println("No identification found in the xml file " + file.getAbsolutePath());
                    }
                    NodeList hitsList = hits.item(0).getChildNodes();
                    String proteinId = "";
                    String peptideSeq = "";
                    double parentMz = 0.0;
                    int charge = -1;
                    float hitScore = 0.0f;
                    String mascotPos = "";
                    String scanTitle = "";
                    Identification identification;

                    for (int i = 0; i < hitsList.getLength(); i++) {
                        //<xs:sequence>
                        //<xs:element name="hit" minOccurs="0" maxOccurs="unbounded">  line 63
                        NodeList subHitsList = hitsList.item(i).getChildNodes();
                        for (int j = 0; j < subHitsList.getLength(); j++) {
                            Node proteinItem = subHitsList.item(j);
                            //<xs:sequence>
                            //<xs:element name="protein" maxOccurs="unbounded">  line 69
                            if (proteinItem.getNodeType() == Node.ELEMENT_NODE && proteinItem.getNodeName().equals("protein")) {
                                //<xs:attribute name="accession" type="xs:string" use="required" /> 
                                proteinId = proteinItem.getAttributes().getNamedItem("accession").getTextContent();
//                                xProtein protein = xTracker.study.retrieveProtein(proteinId, proteinId, sd);
                                NodeList subProteinList = proteinItem.getChildNodes();
                                //<xs:sequence>
                                //<xs:element name="prot_score" minOccurs="0"> <xs:documentation>Mascot protein score </xs:documentation>
                                for (int k = 0; k < subProteinList.getLength(); k++) {
                                    Node peptideItem = subProteinList.item(k);
                                    //<xs:element name="peptide" type="msr:peptideType" minOccurs="0" maxOccurs="unbounded"/> line 205
                                    if (peptideItem.getNodeType() == Node.ELEMENT_NODE && peptideItem.getNodeName().equals("peptide")) {
                                        //the elements listed under msr:peptideType
                                        NodeList peptideDataList = peptideItem.getChildNodes();
                                        //reset peptide related parameters
                                        peptideSeq = "";
                                        parentMz = 0.0;
                                        charge = -1;
                                        hitScore = 0.0f;
                                        mascotPos = "";
                                        scanTitle = "";

                                        for (int ii = 0; ii < peptideDataList.getLength(); ii++) {
                                            Node peptideElem = peptideDataList.item(ii);
                                            if (peptideElem.getNodeType() == Node.ELEMENT_NODE) {
                                                if (peptideElem.getNodeName().equals("pep_seq")) {
                                                    peptideSeq = peptideElem.getTextContent();
                                                    //<xs:element name="pep_exp_mz" type="xs:double">   line 135 <xs:documentation>Experimental m/z</xs:documentation>
                                                } else if (peptideElem.getNodeName().equals("pep_exp_mz")) {
                                                    parentMz = Double.valueOf(peptideElem.getTextContent());
                                                    //<xs:element name="pep_exp_z" minOccurs="0">  line 145 <xs:documentation>Experimental charge</xs:documentation>
                                                } else if (peptideElem.getNodeName().equals("pep_exp_z")) {
                                                    charge = Integer.valueOf(peptideElem.getTextContent()).intValue();
                                                    //<xs:element name="pep_score" minOccurs="0">  line 199 <xs:documentation>Mascot ions score</xs:documentation>
                                                } else if (peptideElem.getNodeName().equals("pep_score")) {
                                                    hitScore = Float.valueOf(peptideElem.getTextContent());
                                                    //<xs:element name="pep_var_mod_pos" type="xs:string" minOccurs="0">   line 260 
                                                    //<xs:documentation>Variable modifications encoded as string</xs:documentation>
                                                } else if (peptideElem.getNodeName().equals("pep_var_mod_pos")) {
                                                    mascotPos = peptideElem.getTextContent();
                                                } else if (peptideElem.getNodeName().equals("pep_scan_title")) {
                                                    scanTitle = peptideElem.getTextContent();
                                                }
                                            }
                                        }//peptideDataList
                                        if (hitScore >= this.scoreThreshold && !removedPeptide.contains(peptideSeq)) {
                                            //TODO: add cvParam list to include hitScore information
                                            identification = new Identification(scanTitle, rawSpectra, scanTitle, null, null, identFile);
                                            identification.setMz(parentMz);
                                            HashSet<xModification> mods = new HashSet<xModification> ();
                                            //deal with variable modifications
                                            if (mascotPos.length() != 0) { //variable modification exists
                                                for (int idx = 2; idx < mascotPos.length() - 2; idx++) {
                                                    int modIndex = Integer.parseInt(Character.toString(mascotPos.charAt(idx)));
                                                    //0 indicates no modification
                                                    if (modIndex > 0) {
                                                        String modification = variableModifications.get(modIndex);
                                                        float shift;
                                                        if (modsFromMascot.containsKey(modification)) {
                                                            shift = modsFromMascot.get(modification);
                                                        } else {//the existance is guaranteed from loadMascotMods (missing one will be reported there)
                                                            shift = modsFromXtp.get(modification);
                                                        }
                                                        //location is 1 for the first acid, here the idx would be 2, so use idx-1
                                                        xModification mod = new xModification(modification, shift, idx - 1);
                                                        mods.add(mod);
                                                    }
                                                }
                                            }//variable modifications
                                            //deal with fixed modifications!
                                            for (int idx = 0; idx < peptideSeq.length(); idx++) {
                                                String residue = Character.toString(peptideSeq.charAt(idx));
                                                if (fixedModifications.containsKey(residue)) {
                                                    String modification = fixedModifications.get(residue);
                                                    float shift;
                                                    if (modsFromMascot.containsKey(modification)) {
                                                        shift = modsFromMascot.get(modification);
                                                    } else {//the existance is guaranteed from loadMascotMods (missing one will be reported there)
                                                        shift = modsFromXtp.get(modification);
                                                    }
                                                    //location is 1 for the first acid, here the idx would be 0, so use idx+1
                                                    xModification mod = new xModification(modification, shift, idx + 1);
                                                    mods.add(mod);
                                                }
                                            }
                                            //Checking for N-Term and C-Term fixed modifications
                                            for (xModification mod : fixedTerms) {
                                                mods.add(mod);
                                            }
                                            //create the modification location string
                                            if(mascotPos.length()==0){
                                                char[] tmp = new char[peptideSeq.length()];
                                                Arrays.fill(tmp, '0');
                                                mascotPos = new String(tmp);
                                            }else{
                                                mascotPos = mascotPos.substring(2, mascotPos.length() - 2);
                                            }
                                            proteinId = proteinId.replace("|", "-");
                                            xProtein protein = xTracker.study.retrieveProtein(proteinId, proteinId, sd);
                                            xPeptide peptide = protein.getPeptide(peptideSeq, mascotPos, mods);
//                                            xPeptide peptide = protein.getPeptide(peptideSeq, mods);
//                                            peptide.setPeptideID(protein.getAccession()+"|"+peptideSeq+"_"+mascotPos);
                                            xFeature feature = peptide.getFeature(msrunRaw,charge);
                                            if(feature == null){
                                                feature = new xFeature(msrunRaw,peptide.getPeptideID(), charge);
//                                                feature = new xFeature(protein.getAccession(), peptide.getPeptideID(), charge);
//                                                feature = new xFeature(protein.getAccession(), peptideSeq, charge);
                                                peptide.addFeature(msrunRaw,feature);
                                            }
                                            identification.setFeature_ref(feature.getId());
                                            feature.addIdentification(identification);
                                        }//if exceeding score threshold
                                    }//peptide node
                                }//protein node
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("LoadMascotIdent: Exception while reading " + file + "\n" + e);
                    System.exit(1);
                }
                xTracker.study.setIdentificationFileMSRunMap(identFile, msrunRaw);
            }else{//the mzIdentML has been done
                if(!msrunIdent.equals(msrunRaw)){
                    System.out.println("Warning: one mzIdentML file "+identFile+" has been related to more than one RawFilesGroup: "+msrunIdent+" "+msrunRaw);
                    System.exit(1);
                }
            }
        }
        populateMetadata();
        System.out.println("LoadMascotIdent completed!");
    }
    /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     */
    public void loadParams(String dataFile) {
        XMLparser parser = new XMLparser(dataFile);
        parser.validate("param");

        NodeList itemList = parser.getElement("param").getChildNodes();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getNodeName().equals("inputFiles")) {
                    NodeList itemListIn = parser.getElement("inputFiles").getChildNodes();
                    for (int j = 0; j < itemListIn.getLength(); j++) {
                        Node itemI = itemListIn.item(j);
                        if (itemI.getNodeName().equals("datafile")) {
                            spectra_identification_map.put(itemI.getTextContent(), itemI.getAttributes().getNamedItem("identification_file").getTextContent());
                        }
                    }
                } else if (item.getNodeName().equals("modificationData")) {
                    NodeList itemListM = parser.getElement("modificationData").getChildNodes();
                    for (int j = 0; j < itemListM.getLength(); j++) {
                        Node itemM = itemListM.item(j);
                        if (itemM.getNodeName().equals("modification")) {
                            String mod = itemM.getTextContent();
                            Float shift = Float.valueOf(itemM.getAttributes().item(0).getTextContent());
                            modsFromXtp.put(mod, shift);
                        }
                    }
                } else if (item.getNodeName().equals("pep_score_threshold")) {
                    this.scoreThreshold = Float.valueOf(item.getTextContent()).floatValue();
                }
            }
        }
    }

    private void validatorInitialization() {
        try {
            String location = Utils.locateFile(MASCOT_XSD, xTracker.folders);
            File xsdFile = new File(location);
            System.out.println(xsdFile.getAbsolutePath());
            if (!xsdFile.exists()) {
                System.out.println("ERROR: Can not find the specified xsd file " + MASCOT_XSD + " to validate the Mascot XML file");
                System.exit(1);
            }
            Source schemaFile = new StreamSource(xsdFile);
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(schemaFile);
            // create a Validator instance
            validator = schema.newValidator();
        } catch (Exception e) {
            System.out.println("Exception while creating the mascot validator \n" + e);
            System.exit(1);
        }
    }

    /**
     * Opens the mascot xml file and loads mass shifts, and loads the information on variable and fixed modifications.
     */
    private void loadMascotMods(Document doc) {
        modsFromMascot = new HashMap<String, Float>();
        //<xs:element name="fixed_mods" minOccurs="0">  line 164
        //read shift data for fixed modifications from mascot xml file if available
        NodeList fixedList = doc.getElementsByTagName("fixed_mods");
        if(fixedList!=null && fixedList.getLength()>0){
            NodeList fixedValueList = fixedList.item(0).getChildNodes();
            for (int i = 0; i < fixedValueList.getLength(); i++) {
            //<xs:sequence> line 170
            //  <xs:element name="modification" minOccurs="0" maxOccurs="unbounded">
            //only modification element allowed, therefore no need to check
                NodeList modificationAttributes = fixedValueList.item(i).getChildNodes();
                String modName = null;
                float delta = Float.NaN;
                for (int j = 0; j < modificationAttributes.getLength(); j++) {
                    Node item = modificationAttributes.item(j);
                    //<xs:element name="name" type="xs:string"> line 173
                    if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("name")) {
                        modName = item.getTextContent();
                    }
                    //<xs:element name="delta" type="xs:double"> line 178
                    if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("delta")) {
                        delta = Float.valueOf(item.getTextContent());
                    }
                }
                if(modName!=null && delta != Float.NaN) modsFromMascot.put(modName, delta);
            }
        }
        //<xs:element name="variable_mods" minOccurs="0">  line 195
        //read shift data for variable modifications from mascot xml file if available
        NodeList variableList = doc.getElementsByTagName("variable_mods");
        if(variableList!=null && variableList.getLength()>0){
            NodeList variableValueList = variableList.item(0).getChildNodes();
            for (int i = 0; i < variableValueList.getLength(); i++) {
                //same as in the fixed modifications
                NodeList modificationAttributes = variableValueList.item(i).getChildNodes();
                String modName = null;
                float delta = Float.NaN;
                for (int j = 0; j < modificationAttributes.getLength(); j++) {
                    Node item = modificationAttributes.item(j);
                    //<xs:element name="name" type="xs:string"> line 173
                    if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("name")) {
                        modName = item.getTextContent();
                    }
                    //<xs:element name="delta" type="xs:double"> line 178
                    if(item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals("delta")) {
                        delta = Float.valueOf(item.getTextContent());
                    }
                }
               if(modName!=null && delta != Float.NaN) modsFromMascot.put(modName, delta);
            }
        }
        System.out.println("Modification information in the mascot xml file");
        for(String key:modsFromMascot.keySet()){
            System.out.println("Modification: "+key+" with shift "+modsFromMascot.get(key));
        }
        //<xs:element name="MODS" type="xs:string">
        //as element, must be there, but could be empty
        NodeList fixed = doc.getElementsByTagName("MODS");
        if(fixed != null){
            String allFixMods = fixed.item(0).getTextContent();
            if(allFixMods.length()>0){
                String[] tokens = allFixMods.split(",");
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    if (modsFromMascot.containsKey(token) || modsFromXtp.containsKey(token)) {
                        int ind = token.indexOf("(");
                        int ind1 = token.indexOf(")");
                        String amino = token.substring(ind + 1, ind1);
                        fixedModifications.put(amino, token);
                        if (amino.indexOf("-term") > -1) {
                            float shift;
                            if (modsFromMascot.containsKey(token)) {
                                shift = modsFromMascot.get(token);
                            } else {//the existance is guaranteed from loadMascotMods (missing one will be reported there)
                                shift = modsFromXtp.get(token);
                            }
                            xModification mod;
                            if (amino.startsWith("N")) {
//                            mod = new xModificationOld(token, shift, false, xModificationOld.N_TERM_LOCATION);
                                mod = new xModification(token, shift, xModification.N_TERM_LOCATION);
                            } else {
//                            mod = new xModification(token, shift, false, xModification.C_TERM_LOCATION);
                                mod = new xModification(token, shift, xModification.C_TERM_LOCATION);
                            }
                            fixedTerms.add(mod);
                        }
                    } else {
                        System.out.println("Error (loadMascotIdent): trying to add a fixed modification not specified in the parameters file (" + token + ").");
                        System.exit(1);
                    }
                }
            }
        }
        //<xs:element name="IT_MODS" type="xs:string">
        //the variable modifications are represented in pep_var_mod_pos element by numbers (indice)
        NodeList variable = doc.getElementsByTagName("IT_MODS");
        if(variable != null){
            String allVariableMods = variable.item(0).getTextContent();
            if(allVariableMods.length()>0){
                String[] tokens = allVariableMods.split(",");
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    if (modsFromMascot.containsKey(token) || modsFromXtp.containsKey(token)) {
                        variableModifications.put(i + 1, token);
                    } else {
                        System.out.println("Error (loadMascotIdent): trying to add a variable modification not specified in the parameters file (" + token + ").");
                        System.exit(1);
                    }
                }
            }
        }
    }

    @Override
    public boolean supportMS1(){
        return true;
    }

    @Override
    public boolean supportMS2(){
        return true;
    }

    public boolean stop() {
        System.out.println(getName() + ": stopping...");
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    void populateMetadata() {
        try {
            uk.ac.ebi.pride.jmztab.model.Param software = new uk.ac.ebi.pride.jmztab.model.Param("PSI-MS", "MS:1001207", "Mascot", "");
            xTracker.study.getMetadata().addSoftware(software);
        } catch (MzTabParsingException ex) {
            Logger.getLogger(loadMascotIdent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
