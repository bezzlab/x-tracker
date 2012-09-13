package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
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
    private int quantitationFlag = 0;
    private boolean ratioInferFromPeptide = false;

    /**
     * the type of the pipeline
     */
    private int pipelineType = 0;
    public static final int MS1_TYPE = 1;
    public static final int MS2_TYPE = 2;
    
    private HashMap<String,String> rawfile_msrun_map;
    private HashMap<String,String> identificationFile_msrun_map;
    public final String UNASSIGNED = "unassigned";
    private ArrayList<xProtein> proteins;
    private HashMap <String,SearchDatabase> searchDatabases;
//    private HashMap <String,xModification> modifications;
    private HashMap<String,MzMLUnmarshaller> rawfile_unmarshallers;
    
    private HashMap<String,Cv> cvs = new HashMap<String, Cv>();
    
    private HashMap<String,CvParamRef> quantitationNames = new HashMap<String, CvParamRef>();
    
    private ArrayList<xRatio> ratios = new ArrayList<xRatio>();
    private ArrayList<Ratio> assayRatios = new ArrayList<Ratio>();

    public ArrayList<Ratio> getAssayRatios() {
        return assayRatios;
    }

    public ArrayList<xRatio> getRatios(){
        return ratios;
    }
    
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

    public MzQuantML getMzQuantML() {
        return mzQuantML;
    }

    public void setMzQuantML(MzQuantML mzQuantML) {
        this.mzQuantML = mzQuantML;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public void setFeatureQuantitationFlag(boolean featureQuantitationFlag) {
        this.featureQuantitationFlag = featureQuantitationFlag;
    }

    public boolean needFeatureQuantitation() {
        return featureQuantitationFlag;
    }

    public void setPeptideQuantitationFlag(boolean peptideQuantitationFlag) {
        this.peptideQuantitationFlag = peptideQuantitationFlag;
    }

    public boolean needPeptideQuantitation() {
        return peptideQuantitationFlag;
    }

    public void setProteinQuantitationFlag(boolean proteinQuantitationFlag) {
        this.proteinQuantitationFlag = proteinQuantitationFlag;
    }

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

    public boolean requireFeatureQuantitation(){
        return quantitationFlag > FEATURE_FLAG;
    }
    
    public boolean requirePeptideQuantitation(){
        return quantitationFlag > PEPTIDE_FLAG;
    }
    
    public boolean requireProteinQuantitation(){
        return quantitationFlag > PROTEIN_FLAG;
    }
    
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
    
    public int getPipelineType() {
        return pipelineType;
    }
    

    public boolean isRatioInferFromPeptide() {
        return ratioInferFromPeptide;
    }

    public void setRatioInferFromPeptide(boolean ratioInferFromPeptide) {
        this.ratioInferFromPeptide = ratioInferFromPeptide;
    }
    
    public int getQuantitationFlag(){
        return quantitationFlag;
    }
    
    public void autoSetQuantitationFlag(){
        quantitationFlag = 0;
        if(featureQuantitationFlag) quantitationFlag += FEATURE_FLAG;
        if(peptideQuantitationFlag) quantitationFlag += PEPTIDE_FLAG;
        if(proteinQuantitationFlag) quantitationFlag += PROTEIN_FLAG;
//        if(proteinGroupQuantitationFlag) quantitationFlag += PROTEIN_GROUP_FLAG;
        if(quantitationFlag == 0) quantitationFlag = PEPTIDE_FLAG;
    }

    public void addMSRun(String id, MSRun msrun) {
        msruns.put(id, msrun);
    }

    public MSRun getMSRun(String id){
        if(msruns.containsKey(id)){
            return msruns.get(id);
        }
        return null;
    }
    
    public void addRawfileMSRunMap(String rawfile, String MSRunId) {
        rawfile_msrun_map.put(rawfile, MSRunId);
    }
    
    public void addIdentificationFile(String identificationFile){
        identificationFile_msrun_map.put(identificationFile, UNASSIGNED);
    }
    
    public void setIdentificationFileMSRunMap(String identificationFile,String MSRunId){
        identificationFile_msrun_map.put(identificationFile,MSRunId);
    }
    
    public String getMSRunIDfromRawFile(String rawfile){
        if(rawfile_msrun_map.containsKey(rawfile)) return rawfile_msrun_map.get(rawfile);
        return null;
    }
    
    public String getMSRunIDfromIdentificationFile(String identficationFile){
        if(identificationFile_msrun_map.containsKey(identficationFile)) return identificationFile_msrun_map.get(identficationFile);
        return null;
    }
    
    public SearchDatabase getSearchDatabase(String sdID){
        if(searchDatabases.containsKey(sdID)){
            return searchDatabases.get(sdID);
        }
        return null;
    }
    
    public void addSearchDatabase(String id,SearchDatabase sd){
        searchDatabases.put(id, sd);
    }
    
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
    public void addMzMLUnmarshaller(String rawfile, MzMLUnmarshaller unmarshaller){
        rawfile_unmarshallers.put(rawfile, unmarshaller);
    }
    
    public MzMLUnmarshaller getMzMLUnmarshaller(String rawfile){
        if(rawfile_unmarshallers.containsKey(rawfile)) return rawfile_unmarshallers.get(rawfile);
        return null;
    }
    
    public xProtein retrieveProtein(String accession,String id,SearchDatabase searchDatabase){
        for(xProtein protein: proteins){
            if(protein.getAccession().equals(accession)) return protein;
        }
        //no existing protein matched, create a new one
        Protein protein = new Protein();
        protein.setAccession(accession);
        protein.setId(id);
//        protein.setAccession(accession.replace("|", "-"));
//        protein.setId(id.replace("|", "-"));
        protein.setSearchDatabaseRef(searchDatabase);
        xProtein xprotein = new xProtein(protein);
        proteins.add(xprotein);
        return xprotein;
    }
    
    public ArrayList<xProtein> getProteins(){
        return proteins;
    }
    
    public Collection<MSRun> getMSRuns(){
        return msruns.values();
    }
    
    public void addCv(String name,Cv cv){
        cvs.put(name, cv);
    }
    
    public Cv getCv(String name){
        return cvs.get(name);
    }
    
    public void addQuantitationName(String name,String accession){
        CvParamRef ref = createCvParam(name, accession);
//        quantitationNames.put(name, param);
        quantitationNames.put(name, ref);
    }
    
    public Collection<String> getQuantitationNames(){
        return quantitationNames.keySet();
    }
    
    public CvParamRef getQuantitationNameCvParam(String name){
//    public CvParam getQuantitationNameCvParam(String name){
        if (quantitationNames.containsKey(name)){
            return quantitationNames.get(name);
        }
        return null;
    }
    
    public CvParamRef createCvParam(String name, String accession) {
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
        param.setCvRef(cv);
        CvParamRef ref = new CvParamRef();
        ref.setCvParam(param);
        return ref;
    }

    public void addRatio(xRatio ratio) {
        ratios.add(ratio);
    }

    public void addAssayRatio(Ratio ratio) {
        assayRatios.add(ratio);
    }
}
