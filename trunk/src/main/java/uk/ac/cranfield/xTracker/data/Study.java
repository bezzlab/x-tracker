package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.jmzqml.model.mzqml.IdentificationFile;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;

/**
 *
 * @author Jun Fan@cranfield
 */
public class Study {
    /**
     * the file name of the mzQuantML configuration file
     */
    private String filename;
    /**
     * the name of quantitation method defined in the AnalysisSummary in the configuration file
     */
    private CvParam quantitationMethod = null;

    public CvParam getQuantitationMethod() {
        return quantitationMethod;
    }

    public void setQuantitationMethod(CvParam quantitationMethod) {
        if(this.quantitationMethod != null && (!this.quantitationMethod.getAccession().equals(quantitationMethod.getAccession()))){
            System.out.println("More than one different quantitation methods defined in the AnalysisSummary");
            System.exit(1);
        }
        this.quantitationMethod = quantitationMethod;
    }
    
    private Metadata metadata = new Metadata();
    /**
     * the mzQuantML object of the configuration file
     */
    private MzQuantML mzQuantML;
    /**
     * the map between MSRun id and the actual MSRun object
     */
    private HashMap<String, MSRun> msruns;
    /**
     * the flag for four quantitation levels 
     */
    private boolean featureQuantitationFlag = false;
    private boolean peptideQuantitationFlag = false;
    private boolean proteinQuantitationFlag = false;
//    private boolean proteinGroupQuantitationFlag = false;
    private static final int FEATURE_FLAG = 1;
    private static final int PEPTIDE_FLAG = 2;
    private static final int PROTEIN_FLAG = 4;
//    private static final int PROTEIN_GROUP_FLAG = 8;
    /**
     * the flag used to decide which level of quantitation is required
     */
    private int quantitationFlag = 0;
    /**
     * the flag determining whether the protein ratio values are calculated from peptide ratios (true) or from protein quantitation (false)
     */
    private boolean ratioInferFromPeptide = false;

    /**
     * the type of the pipeline
     */
    private int pipelineType = 0;
    public static final int MS1_TYPE = 1;
    public static final int MS2_TYPE = 2;
    /**
     * the map between raw file and msrun
     */
    private HashMap<String,String> rawfile_msrun_map;
    /**
     * the map between raw identification file and msrun
     * the default value is "unassigned" indicating this identification file has not been processed
     */
    private HashMap<String,String> identificationFile_msrun_map;
    public final String UNASSIGNED = "unassigned";
    /**
     * the list of identified proteins
     */
    private ArrayList<xProtein> proteins;
    /**
     * accessory structure to avoid generate duplicate search database
     */
    private HashMap <String,SearchDatabase> searchDatabases;
//    private HashMap <String,xModification> modifications;
    /**
     * the collections of mzML unmarshallers, to save time by only generating the unmarshaller once for one mzML file
     */
    private HashMap<String,MzMLUnmarshaller> rawfile_unmarshallers;
    /**
     * accessory structure to avoid generate duplicate Cv (e.g. PSI-MS, UNIMOD etc.)
     */
    private HashMap<String,Cv> cvs = new HashMap<String, Cv>();
    /**
     * the collections of quantitation type used in quantitation plugins, must be specified by the plugin developer
     */
    private HashMap<String,CvParamRef> quantitationNames = new HashMap<String, CvParamRef>();
    /**
     * the list of all ratios defined in the mzQuantML configuration file
     */
    private ArrayList<xRatio> ratios = new ArrayList<xRatio>();
    /**
     * the list of all ratios based on assays defined in the mzQuantML configuration file
     */
    private ArrayList<Ratio> assayRatios = new ArrayList<Ratio>();
    
    private HashSet<String> ratioMeasurements = new HashSet<String>();
    
    private HashMap<String,IdentificationFile> identificationFiles = new HashMap<String, IdentificationFile>(); 
//    private HashSet<CvParam> ratioMeasurements = new HashSet<CvParam>();
    
    public HashSet<String> getRatioMeasurements(){
//    public HashSet<CvParam> getRatioMeasurements(){
        return ratioMeasurements;
    }
    
    public void addRatioMeasurement(String measurement){
//    public void addRatioMeasurement(CvParam measurement){
        ratioMeasurements.add(measurement);
    }
    /**
     * return the list of all ratios based on assays
     * @return 
     */
    public ArrayList<Ratio> getAssayRatios() {
        return assayRatios;
    }
    /**
     * return the list of all ratios
     * @return 
     */
    public ArrayList<xRatio> getRatios(){
        return ratios;
    }
    /**
     * return whether ratio calculation is required in the mzQuantML configuration file
     * @return 
     */
    public boolean isRatioRequired(){
        if(ratios.size()>0) return true;
        return false;
    }

    public Study() {
        msruns = new HashMap<String, MSRun>();
        rawfile_msrun_map = new HashMap<String, String>();
        identificationFile_msrun_map = new HashMap<String, String>();
        proteins = new ArrayList<xProtein>();
        searchDatabases = new HashMap<String, SearchDatabase>();
//        modifications = new HashMap<String, xModification>();
        rawfile_unmarshallers = new HashMap<String, MzMLUnmarshaller>();
    }
    /**
     * return the mzQuantML object from the configuration file as the starting point to export the result
     * @return 
     */
    public MzQuantML getMzQuantML() {
        return mzQuantML;
    }
    /**
     * set the mzQuantML object
     * @param mzQuantML 
     */
    public void setMzQuantML(MzQuantML mzQuantML) {
        this.mzQuantML = mzQuantML;
    }
    /**
     * get the configuration file name
     * @return 
     */
    public String getFilename() {
        return filename;
    }
    /**
     * set the configuration file name
     * @param filename 
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    /**
     * set the quantitation flag for feature level
     * @param featureQuantitationFlag 
     */
    public void setFeatureQuantitationFlag(boolean featureQuantitationFlag) {
        this.featureQuantitationFlag = featureQuantitationFlag;
    }
    /**
     * get the quantitation flag defined in the configuration file for feature
     * @return 
     */
    public boolean needFeatureQuantitation() {
        return featureQuantitationFlag;
    }
    /**
     * set the quantitation flag for peptide level
     * @param featureQuantitationFlag 
     */
    public void setPeptideQuantitationFlag(boolean peptideQuantitationFlag) {
        this.peptideQuantitationFlag = peptideQuantitationFlag;
    }
    /**
     * get the quantitation flag defined in the configuration file for peptide
     * @return 
     */
    public boolean needPeptideQuantitation() {
        return peptideQuantitationFlag;
    }
    /**
     * set the quantitation flag for protein level
     * @param featureQuantitationFlag 
     */
    public void setProteinQuantitationFlag(boolean proteinQuantitationFlag) {
        this.proteinQuantitationFlag = proteinQuantitationFlag;
    }
    /**
     * get the quantitation flag defined in the configuration file for protein
     * @return 
     */
    public boolean needProteinQuantitation() {
        return proteinQuantitationFlag;
    }

//    public void setProteinGroupQuantitationFlag(boolean proteinGroupQuantitationFlag) {
//        this.proteinGroupQuantitationFlag = proteinGroupQuantitationFlag;
//    }
//
//    public boolean needProteinGroupQuantitation() {
//        return proteinGroupQuantitationFlag;
//    }
    /**
     * determine whether feature quantitation is required, which maybe is due to a higher level quantitation is needed
     * @return 
     */
    public boolean requireFeatureQuantitation(){
        return quantitationFlag > FEATURE_FLAG;
    }
    /**
     * determine whether peptide quantitation is required, which maybe is due to a higher level quantitation is needed
     * @return 
     */
    public boolean requirePeptideQuantitation(){
        return quantitationFlag > PEPTIDE_FLAG;
    }
    /**
     * determine whether protein quantitation is required, which maybe is due to a higher level quantitation is needed
     * @return 
     */
    public boolean requireProteinQuantitation(){
        return quantitationFlag > PROTEIN_FLAG;
    }
    /**
     * set the pipeline type
     * @param pipelineType 
     */
    public void setPipelineType(int pipelineType){
        int current = getPipelineType();
        if(current == 0) {
            this.pipelineType = pipelineType;
            return;
        }
        if(current!=pipelineType){
            System.out.println("Conflict pipeline type, program exits. Please check the settings under AnalysisSummary");
            System.exit(1);
        }
    }
    /**
     * get the pipeline type
     * @param pipelineType 
     */
    public int getPipelineType() {
        return pipelineType;
    }
    /**
     * get the flag which tells whether protein ratio is calculated from peptide ratios
     * @return 
     */
    public boolean isRatioInferFromPeptide() {
        return ratioInferFromPeptide;
    }
    /**
     * set the flag which tells whether protein ratio is calculated from peptide ratios
     * @return 
     */
    public void setRatioInferFromPeptide(boolean ratioInferFromPeptide) {
        this.ratioInferFromPeptide = ratioInferFromPeptide;
    }
    /**
     * get the quantitation flag
     * @return 
     */
    public int getQuantitationFlag(){
        return quantitationFlag;
    }
    /**
     * set value to quantitation flag according to all individual quantitation level flag
     */
    public void autoSetQuantitationFlag(){
        quantitationFlag = 0;
        if(featureQuantitationFlag) quantitationFlag += FEATURE_FLAG;
        if(peptideQuantitationFlag) quantitationFlag += PEPTIDE_FLAG;
        if(proteinQuantitationFlag) quantitationFlag += PROTEIN_FLAG;
//        if(proteinGroupQuantitationFlag) quantitationFlag += PROTEIN_GROUP_FLAG;
        //if no quantitation flag is set, the program sets to only get quantitation for peptides as default
        if(quantitationFlag == 0) quantitationFlag = PEPTIDE_FLAG;
    }
    /**
     * add the new MSRun and link it with its id
     * @param id
     * @param msrun 
     */
    public void addMSRun(String id, MSRun msrun) {
        msruns.put(id, msrun);
    }
    /**
     * get the MSRun according to the id
     * @param id
     * @return MSRun
     */
    public MSRun getMSRun(String id){
        if(msruns.containsKey(id)){
            return msruns.get(id);
        }
        return null;
    }
    /**
     * Link the raw file to the corresponding MSRun (actually its id)
     * @param rawfile
     * @param MSRunId 
     */
    public void addRawfileMSRunMap(String rawfile, String MSRunId) {
        rawfile_msrun_map.put(rawfile, MSRunId);
    }
    /**
     * add the identification file with the unassigned MSRun id.
     * @param identificationFile 
     */
//    public void addIdentificationFile(String identificationFile){
    public void addIdentificationFile(IdentificationFile identificationFile){
        identificationFiles.put(identificationFile.getLocation(), identificationFile);
        identificationFile_msrun_map.put(identificationFile.getLocation(), UNASSIGNED);
    }
    /**
     * assign the MSRun ID to the identification file
     * @param identificationFile 
     */
    public void setIdentificationFileMSRunMap(String identificationFile,String MSRunId){
        identificationFile_msrun_map.put(identificationFile,MSRunId);
    }
    
    public IdentificationFile getIdentificationFile(String location){
        if(identificationFiles.containsKey(location)) return identificationFiles.get(location);
        return null;
    }
    /**
     * get the msrun id from the raw file
     * @param rawfile
     * @return 
     */
    public String getMSRunIDfromRawFile(String rawfile){
        if(rawfile_msrun_map.containsKey(rawfile)) return rawfile_msrun_map.get(rawfile);
        return null;
    }
    /**
     * get the msrun id from the identification file
     * @param identficationFile
     * @return 
     */
    public String getMSRunIDfromIdentificationFile(String identficationFile){
        if(identificationFile_msrun_map.containsKey(identficationFile)) return identificationFile_msrun_map.get(identficationFile);
        return null;
    }
    /**
     * get the search database according to the search database id
     * @param sdID
     * @return 
     */
    public SearchDatabase getSearchDatabase(String sdID){
        if(searchDatabases.containsKey(sdID)){
            return searchDatabases.get(sdID);
        }
        return null;
    }
    /**
     * add the search database to the list
     * @param id
     * @param sd 
     */
    public void addSearchDatabase(String id,SearchDatabase sd){
        searchDatabases.put(id, sd);
    }
    /**
     * get all available search databases
     * @return 
     */
    public Collection<SearchDatabase> getSearchDatabases(){
        return searchDatabases.values();
    }
//    public xModification getModification(String id){
//        if(modifications.containsKey(id)){
//            return modifications.get(id);
//        }
//        return null;
//    }
//    
//    public void addModification(xModification mod){
//        modifications.put(mod.getID(), mod);
//    }
    /**
     * Add a unmarshaller to the list with its raw file name
     * @param rawfile
     * @param unmarshaller 
     */
    public void addMzMLUnmarshaller(String rawfile, MzMLUnmarshaller unmarshaller){
        rawfile_unmarshallers.put(rawfile, unmarshaller);
    }
    /**
     * Get the unmarshaller for the mzML file
     * @param rawfile
     * @return 
     */
    public MzMLUnmarshaller getMzMLUnmarshaller(String rawfile){
        if(rawfile_unmarshallers.containsKey(rawfile)) return rawfile_unmarshallers.get(rawfile);
        return null;
    }
    /**
     * Get the protein from the protein list corresponding to the accession
     * If not, create a new one, add into the protein list and return the newly created one
     * @param accession the protein accession
     * @param id the protein id
     * @param searchDatabase the search database
     * @return the protein having the specified accession
     */
    public xProtein retrieveProtein(String accession,String id,SearchDatabase searchDatabase){
        for(xProtein protein: proteins){
            if(protein.getAccession().equals(accession)) return protein;
        }
        //no existing protein matched, create a new one
        //first create a mzQuantML protein element
        Protein protein = new Protein();
        protein.setAccession(accession);
        protein.setId(id);
        protein.setSearchDatabase(searchDatabase);
        xProtein xprotein = new xProtein(protein);
        proteins.add(xprotein);
        return xprotein;
    }
    /**
     * Get all proteins
     * @return 
     */
    public ArrayList<xProtein> getProteins(){
        return proteins;
    }
    /**
     * Get all MS Runs
     * @return 
     */
    public Collection<MSRun> getMSRuns(){
        return msruns.values();
    }
    /**
     * Add CV to the list
     * @param name
     * @param cv 
     */
    public void addCv(String name,Cv cv){
        cvs.put(name, cv);
    }
    /**
     * Get the CV according to the name
     * @param name
     * @return 
     */
    public Cv getCv(String name){
        return cvs.get(name);
    }
    /**
     * Add quantitation label into the list with the correspondingly generated PSI-MS term 
     * @param name
     * @param accession 
     */
    public void addQuantitationName(CvParam param){
        CvParamRef ref = new CvParamRef();
        ref.setCvParam(param);
        quantitationNames.put(param.getName(), ref);
    }

//    public void addQuantitationName(String name,String accession){
////        CvParamRef ref = createMSCvParam(name, accession);
//        CvParamRef ref = new CvParamRef();
//        CvParam param = createMSCvParam(name, accession);
//        ref.setCvParam(param);
//        quantitationNames.put(name, ref);
//    }
    /**
     * Get all quantitation labels
     * @return 
     */
    public Collection<String> getQuantitationNames(){
        return quantitationNames.keySet();
    }
    /**
     * Get the PSI-MS term for the given quantitation label
     * @param name
     * @return 
     */
    public CvParamRef getQuantitationNameCvParam(String name){
        if (quantitationNames.containsKey(name)){
            return quantitationNames.get(name);
        }
        return null;
    }
    /**
     * Generate the PSI-MS term according to the given name and accession
     * @param name the PSI-MS term name
     * @param accession the PSI-MS term accession
     * @return 
     */
    public CvParam createMSCvParam(String name, String accession) {
        CvParam param = new CvParam();
        param.setName(name);
        param.setAccession(accession);
        Cv cv = getCv("PSI-MS");
        if (cv == null) {
            cv = new Cv();
            cv.setFullName("Proteomics Standards Initiative Mass Spectrometry Vocabularies");
            cv.setId("PSI-MS");
            cv.setUri("http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo");
            addCv(cv.getId(), cv);
            getMzQuantML().getCvList().getCv().add(cv);
        }
        param.setCv(cv);
        return param;
    }
    /**
     * add the ratio to the list which may be based on either assay or SV
     * @param ratio 
     */
    public void addRatio(xRatio ratio) {
        ratios.add(ratio);
    }
    /**
     * add the ratio which is only based on assay to the list as peptide ratios at the moment only need assay level
     * @param ratio 
     */
    public void addAssayRatio(Ratio ratio) {
        assayRatios.add(ratio);
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
