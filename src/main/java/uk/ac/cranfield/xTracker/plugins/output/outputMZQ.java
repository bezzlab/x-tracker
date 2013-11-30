package uk.ac.cranfield.xTracker.plugins.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
import uk.ac.liv.jmzqml.model.mzqml.Protein;
import uk.ac.liv.jmzqml.model.mzqml.ProteinList;
import uk.ac.liv.jmzqml.model.mzqml.QuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.Row;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.liv.jmzqml.xml.io.MzQuantMLMarshaller;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.QuantitationLevel;
import uk.ac.cranfield.xTracker.data.Study;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xPeptideConsensus;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.data.xRatio;
import uk.ac.cranfield.xTracker.xTracker;
import uk.ac.liv.jmzqml.model.mzqml.AbstractParam;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.DataProcessing;
import uk.ac.liv.jmzqml.model.mzqml.IdentificationFile;
import uk.ac.liv.jmzqml.model.mzqml.ProcessingMethod;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.RatioQuantLayer;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariable;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;

/**
 *
 * @author Jun Fan@cranfield
 */
public class outputMZQ extends outPlugin{
    private String name = "Output mzQuantML";
    private String version = "1.0";
    private String description = "export the pipeline result along with the identifications into the mzQuantML file specified in the parameter file";
    private String outputFilename;
    private HashMap<String,FeatureList> featureLists = new HashMap<String, FeatureList>();
//  NCName         ::=     (Letter | '_') (NCNameChar)*
//  NCNameChar     ::=     Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender 
    final private Pattern patternNCName = Pattern.compile("([^\\w\\.\\-]+)");
    final private Pattern patternFirstPosition = Pattern.compile("^[a-zA-Z_]");
    
    @Override
    public void start(String paramFile) {
        System.out.println(getName()+" starts");
        //get the output file name
        if(xTracker.OUTPUT.length()>0){
            outputFilename = xTracker.OUTPUT;
        }else{
            outputFilename = getOutputFileName(paramFile);
        }
        if(outputFilename == null){
            System.out.println("Can not get the output file name. There are several reasons: the wrong plugin parameter file using wrong xsd file, not defined in the parameter file");
            System.exit(1);
        }
        System.out.println(outputFilename);
        //prepare the mzq object to be exported
        //it is based on the configure file used as the input
        MzQuantML mzq = xTracker.study.getMzQuantML();
        //prepare objects
        ProteinList proteinList = new ProteinList();
        PeptideConsensusList pcList = new PeptideConsensusList();
        HashMap<String,QuantLayer> peptideQLs = new HashMap<String,QuantLayer>();
        HashMap<String,QuantLayer> proteinQLs = new HashMap<String,QuantLayer>();
//        HashMap<String,QuantLayer> peptideRatioQLs = new HashMap<String,QuantLayer>();
//        HashMap<String,QuantLayer> proteinRatioQLs = new HashMap<String,QuantLayer>();
        RatioQuantLayer peptideRatioQL = new RatioQuantLayer();
        RatioQuantLayer proteinRatioQL = new RatioQuantLayer();
        HashMap<String,QuantLayer> svQLs = new HashMap<String,QuantLayer>();
        HashMap<String,ArrayList<String>> msrun_assayIDs_map = new HashMap<String, ArrayList<String>>();
        HashMap<String,HashMap<String, QuantLayer>> msrun_featureQL_map = new HashMap<String, HashMap<String, QuantLayer>>();
        //list of all assays
        ArrayList<Assay> allAssays = new ArrayList<Assay>();
        //feature is rawFilesGroup specific
        for (MSRun msrun : xTracker.study.getMSRuns()) {
            FeatureList featureList = new FeatureList();
            featureList.setId("featureList_"+msrun.getID());
            featureList.setRawFilesGroup(msrun.getRawFilesGroup());
            featureLists.put(msrun.getID(),featureList);
            
            ArrayList<String> assayIDs = new ArrayList<String>();
            for (Assay assay : msrun.getAssays()) {
                assayIDs.add(assay.getId());
            }
            msrun_assayIDs_map.put(msrun.getID(), assayIDs);
            allAssays.addAll(msrun.getAssays());
            //measurement specific quant layer list
            HashMap<String, QuantLayer> featureQLs = new HashMap<String, QuantLayer>();
            if (xTracker.study.needFeatureQuantitation()) {
                for (String quantitationName : xTracker.study.getQuantitationNames()) {
//                    quantitationName = quantitationName.replace(" ", "_");
                    QuantLayer ql = new QuantLayer();
                    if(xTracker.study.getPipelineType()==Study.MS2_TYPE){
                        ql.setId("MS2AssayQuantLayer_" + msrun.getID() + "_" + quantitationName);
                    }else{
                        ql.setId("AssayQuantLayer_" + msrun.getID() + "_" + quantitationName);
                    }
                    ql.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
                    for(Assay ass: msrun.getAssays()){
                        ql.getColumnIndex().add(ass.getId());
                    }
//                    ql.getColumnIndex().addAll(msrun.getAssays());
                    DataMatrix matrix = new DataMatrix();
                    ql.setDataMatrix(matrix);
                    featureQLs.put(quantitationName, ql);
                }
            }
            msrun_featureQL_map.put(msrun.getID(), featureQLs);
        }
        
        CvParam proteinRatioCvParam = xTracker.study.createMSCvParam("protein ratio", "MS:1001134");
        CvParamRef proteinRatioCvParamRef = new CvParamRef();
        proteinRatioCvParamRef.setCvParam(proteinRatioCvParam);
        CvParam peptideRatioCvParam = xTracker.study.createMSCvParam("peptide ratio", "MS:1001132");
        CvParamRef peptideRatioCvParamRef = new CvParamRef();
        peptideRatioCvParamRef.setCvParam(peptideRatioCvParam);

        List<StudyVariable> svs = xTracker.study.getMzQuantML().getStudyVariableList().getStudyVariable();
        if (svs.size()>1){
            for(String quantitationName:xTracker.study.getQuantitationNames()){
                QuantLayer svQL = new QuantLayer();
                svQL.setId("SV_QuantLayer_Protein_"+quantitationName);
                svQL.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
                for(StudyVariable sv: svs){
                    svQL.getColumnIndex().add(sv.getId());
                }
//                svQL.getColumnIndex().addAll(svs);
                DataMatrix svMatrix = new DataMatrix();
                svQL.setDataMatrix(svMatrix);
                svQLs.put(quantitationName, svQL);
            }
        }
        
        for (String quantitationName : xTracker.study.getQuantitationNames()) {
            QuantLayer peptideQL = new QuantLayer();
            QuantLayer proteinQL = new QuantLayer();
            peptideQL.setId("AssayQuantLayer_Peptides_" + quantitationName);
            proteinQL.setId("AssayQuantLayer_Proteins_" + quantitationName);
            peptideQL.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
            proteinQL.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
            for (Assay ass:allAssays){
                peptideQL.getColumnIndex().add(ass.getId());
                proteinQL.getColumnIndex().add(ass.getId());
            }
//            peptideQL.getColumnIndex().addAll(allAssays);
//            proteinQL.getColumnIndex().addAll(allAssays);
            DataMatrix pepMatrix = new DataMatrix();
            DataMatrix proMatrix = new DataMatrix();
            peptideQL.setDataMatrix(pepMatrix);
            proteinQL.setDataMatrix(proMatrix);
            peptideQLs.put(quantitationName, peptideQL);
            proteinQLs.put(quantitationName, proteinQL);
//            if(xTracker.study.isRatioRequired()){//there is ratio calculation requirement
//                if(!xTracker.study.getAssayRatios().isEmpty()){
//                    QuantLayer peptideRatioQL = new QuantLayer();
//                    peptideRatioQL.setId("RatioQuantLayer_Peptides_" + quantitationName);
//                    peptideRatioQL.setDataType(peptideRatioCvParam);
//                    peptideRatioQL.getColumnIndex().addAll(xTracker.study.getAssayRatios());
//                    DataMatrix pepRatioMatrix = new DataMatrix();
//                    peptideRatioQL.setDataMatrix(pepRatioMatrix);
//                    peptideRatioQLs.put(quantitationName, peptideRatioQL);
//                }
//
//                QuantLayer proteinRatioQL = new QuantLayer();
//                proteinRatioQL.setId("RatioQuantLayer_Proteins_"+quantitationName);
//                proteinRatioQL.setDataType(proteinRatioCvParam);
//                proteinRatioQL.getColumnIndex().addAll(mzq.getRatioList().getRatio());
//                DataMatrix proRatioMatrix = new DataMatrix();
//                proteinRatioQL.setDataMatrix(proRatioMatrix);
//                proteinRatioQLs.put(quantitationName, proteinRatioQL);
//            }
        }
        if (xTracker.study.isRatioRequired()) {//there is ratio calculation requirement
            //peptide only does assay ratio
            if (!xTracker.study.getAssayRatios().isEmpty()) {
                peptideRatioQL.setId("RatioQuantLayer_Peptides");
//                peptideRatioQL.setDataType(peptideRatioCvParamRef);
                for(Ratio ratio:xTracker.study.getAssayRatios()){
                    peptideRatioQL.getColumnIndex().add(ratio.getId());
                }
//                peptideRatioQL.getColumnIndex().addAll(xTracker.study.getAssayRatios());
                DataMatrix pepRatioMatrix = new DataMatrix();
                peptideRatioQL.setDataMatrix(pepRatioMatrix);
            }
            //protein ratio can include ratio for study variables
            proteinRatioQL.setId("RatioQuantLayer_Proteins");
//            proteinRatioQL.setDataType(proteinRatioCvParamRef);
            for(Ratio ratio: mzq.getRatioList().getRatio()){
                proteinRatioQL.getColumnIndex().add(ratio.getId());
            }
//            proteinRatioQL.getColumnIndex().addAll(mzq.getRatioList().getRatio());
            DataMatrix proRatioMatrix = new DataMatrix();
            proteinRatioQL.setDataMatrix(proRatioMatrix);
        }

        if(xTracker.study.getPipelineType()==Study.MS2_TYPE){
            HashSet<xProtein> proteinSet = new HashSet<xProtein>();
            HashMap<xProtein,ArrayList<PeptideConsensus>> proteinPcRefs = new HashMap<xProtein, ArrayList<PeptideConsensus>>();
//            HashMap<String,ArrayList<PeptideConsensus>> proteinPcRefs = new HashMap<String, ArrayList<PeptideConsensus>>();

            for (xProtein protein : xTracker.study.getProteins()) {
                proteinSet.add(protein);
                ArrayList<PeptideConsensus> pcRefs;
                if (proteinPcRefs.containsKey(protein)){
                    pcRefs = proteinPcRefs.get(protein);
                }else{
                    pcRefs = new ArrayList<PeptideConsensus>();
                    proteinPcRefs.put(protein, pcRefs);
                }
                ArrayList<xPeptideConsensus> peptideCons = protein.getPeptides();
                if (peptideCons == null) {
                    continue;
                }
                for (xPeptideConsensus pepCon : peptideCons) {
                    for (xPeptide peptide : pepCon.getPeptides()) {
                        PeptideConsensus pc = peptide.convertToQpeptideConsensus();
                        pcRefs.add(pc);
                        pc.setId(getCorrectNCName(pc.getId()));
                        pc.setSearchDatabase(protein.getProtein().getSearchDatabase());
//                        pc.setSearchDatabaseRef(protein.getProtein().getSearchDatabaseRef());
                        for (String msrunID : peptide.getMSRunIDs()) {
                            for (xFeature feature : peptide.getFeatures(msrunID)) {
                                for(Identification identification : feature.getIdentifications()){
                                    //FeatureList
                                    Feature qFeature = identification.convertToQfeature();
                                    String id = feature.getId()+"-"+qFeature.getId();
                                    //"|" is widely used in protein accessions, which is not allowed in mzQuantML, so need to be corrected according to NCName format
                                    qFeature.setId(getCorrectNCName(id));
                                    qFeature.setCharge(String.valueOf(feature.getCharge()));
                                    qFeature.setRt("null");
                                    featureLists.get(msrunID).getFeature().add(qFeature);
                                    //FeatureList quantitation
                                    if (xTracker.study.needFeatureQuantitation()) {
                                        for (String quantitationName : xTracker.study.getQuantitationNames()) {
//                                            quantitationName = quantitationName.replace(" ", "_");
                                            Row row = new Row();
                                            row.setObject(qFeature);
                                            HashMap<String, Double> quants = identification.getQuantities(quantitationName);
                                            for (String assayID : msrun_assayIDs_map.get(msrunID)) {
                                                row.getValue().add(String.valueOf(quants.get(assayID)));
                                            }
                                            msrun_featureQL_map.get(msrunID).get(quantitationName).getDataMatrix().getRow().add(row);
                                        }
                                    }
                                    //PeptideConsensus EvidenceRef
                                    EvidenceRef evidence = new EvidenceRef();
//                                    evidence.getAssays().addAll(xTracker.study.getMSRun(msrunID).getAssays());
                                    List<String> assayRefs = new ArrayList<String>();
                                    for(Assay ass:xTracker.study.getMSRun(msrunID).getAssays()){
                                        assayRefs.add(ass.getId());
                                    }
                                    evidence.getAssayRefs().addAll(assayRefs);
                                    evidence.setFeature(qFeature);
                                    if (identification.getSii() == null) {//not from mzIdentML file
                                        evidence.getIdRefs().add(identification.getId());
                                        IdentificationFile idFile = xTracker.study.getIdentificationFile(identification.getIdentificationFile());
                                        if(idFile == null){
                                            System.out.println("Please check the identification file "+identification.getIdentificationFile()+" which is not defined in the mzq configuration file");
                                            System.exit(1);
                                        }
                                        evidence.setIdentificationFile(idFile);
                                    } else {
                                        evidence.getIdRefs().add(identification.getSii().getId());
                                    }
                                    
                                    pc.getEvidenceRef().add(evidence);
                                }//end of identification
                            }
                        }
                        pcList.getPeptideConsensus().add(pc);
                        //quantitation
                        if (xTracker.study.needPeptideQuantitation()) {
                            for (String quantitationName : xTracker.study.getQuantitationNames()) {
                                Row row = new Row();
                                row.setObject(pc);
                                HashMap<String, Double> quants = peptide.getQuantities(quantitationName);
                                for (Assay assay : allAssays) {
                                    row.getValue().add(String.valueOf(quants.get(assay.getId())));
                                }
                                peptideQLs.get(quantitationName).getDataMatrix().getRow().add(row);
                            }
                            if(!xTracker.study.getAssayRatios().isEmpty()){
                                Row ratioRow = new Row();
                                ratioRow.setObject(pc);
                                for(Ratio assayRatio:xTracker.study.getAssayRatios()){
                                    String quantitationName = assayRatio.getDenominatorDataType().getCvParam().getName();
                                    HashMap<String, Double> ratioValues = peptide.getRatios(quantitationName);
                                    ratioRow.getValue().add(String.valueOf(ratioValues.get(assayRatio.getId())));
                                }
                                peptideRatioQL.getDataMatrix().getRow().add(ratioRow);
                            }
                        }
                    }
                }
            }
            if (xTracker.study.needFeatureQuantitation()) {
                for(String msrunID:msrun_featureQL_map.keySet()){
                    HashMap<String,QuantLayer> featureQLs = msrun_featureQL_map.get(msrunID);
                    for (QuantLayer ql : featureQLs.values()) {
                        ql.setId(getCorrectNCName(ql.getId()));
                        featureLists.get(msrunID).getMS2AssayQuantLayer().add(ql);
                    }
                }
            }
            //ProteinList
            proteinList.setId("ProteinList");
            for(xProtein pro:proteinSet){
                Protein protein = pro.getProtein();
//                protein.getPeptideConsensuses().addAll(proteinPcRefs.get(pro));
                for(PeptideConsensus pc:proteinPcRefs.get(pro)){
                    protein.getPeptideConsensusRefs().add(pc.getId());
                }
                protein.setId(getCorrectNCName(protein.getId()));
                protein.setAccession(getCorrectNCName(protein.getAccession()));
                proteinList.getProtein().add(protein);
                if (xTracker.study.needProteinQuantitation()) {
                    for (String quantitationName : xTracker.study.getQuantitationNames()) {
                        Row row = new Row();
                        row.setObject(pro.getProtein());
                        HashMap<String, Double> quants = pro.getQuantities(quantitationName);
                        for (Assay assay : allAssays) {
                            row.getValue().add(String.valueOf(quants.get(assay.getId())));
                        }
                        proteinQLs.get(quantitationName).getDataMatrix().getRow().add(row);
                        
                        if(svs.size()>1){
                            Row svRow = new Row();
                            svRow.setObject(pro.getProtein());
                            HashMap<String,Double> svValues = pro.getStudyVariableQuantities(quantitationName);
                            for(StudyVariable sv:svs){
                                svRow.getValue().add(String.valueOf(svValues.get(sv.getId())));
                            }
                            svQLs.get(quantitationName).getDataMatrix().getRow().add(svRow);
                        }
                    }
                    if (xTracker.study.isRatioRequired()) {
                        Row ratioRow = new Row();
                        ratioRow.setObject(pro.getProtein());
                        for (xRatio ratio : xTracker.study.getRatios()) {
                            String quantitationName = ratio.getRatio().getDenominatorDataType().getCvParam().getName();
                            HashMap<String, Double> ratioValues = pro.getRatios(quantitationName);
                            ratioRow.getValue().add(String.valueOf(ratioValues.get(ratio.getId())));
                        }
                        proteinRatioQL.getDataMatrix().getRow().add(ratioRow);
                    }
                }
            }
            if(xTracker.study.needProteinQuantitation()){
                for (QuantLayer ql:proteinQLs.values()) {
                    ql.setId(getCorrectNCName(ql.getId()));
                    proteinList.getAssayQuantLayer().add(ql);
                }
                
                if(svs.size()>1){
                    for (QuantLayer svQL: svQLs.values()){
                        svQL.setId(getCorrectNCName(svQL.getId()));
                        proteinList.getStudyVariableQuantLayer().add(svQL);
                    }
                }
                
                if (xTracker.study.isRatioRequired()) {
                    proteinRatioQL.setId(getCorrectNCName(proteinRatioQL.getId()));
                    proteinList.setRatioQuantLayer(proteinRatioQL);
                }
            }
            
            mzq.setProteinList(proteinList);
            //PeptideConsensusList
            pcList.setId("PeptideList");
            pcList.setFinalResult(true);
            if(xTracker.study.needPeptideQuantitation()){
                for (QuantLayer ql:peptideQLs.values()) {
                    ql.setId(getCorrectNCName(ql.getId()));
                    pcList.getAssayQuantLayer().add(ql);
                }
                if(!xTracker.study.getAssayRatios().isEmpty()){
                    peptideRatioQL.setId(getCorrectNCName(peptideRatioQL.getId()));
                    pcList.setRatioQuantLayer(peptideRatioQL);
                }
            }
            mzq.getPeptideConsensusList().add(pcList);
            //searchDatabase
            for(SearchDatabase sd:xTracker.study.getSearchDatabases()){
                mzq.getInputFiles().getSearchDatabase().add(sd);
            }
            //FeatureList
            for(FeatureList featureList:featureLists.values()){
                featureList.setId(getCorrectNCName(featureList.getId()));
                mzq.getFeatureList().add(featureList);
            }
            //data processing inference methods
            List<DataProcessing> dpList = mzq.getDataProcessingList().getDataProcessing();
            for(DataProcessing dp:dpList){
                String swName = dp.getSoftwareRef();
                if (!swName.equalsIgnoreCase("xtracker")) continue;
                List<ProcessingMethod> pmList = dp.getProcessingMethod();
                ProcessingMethod pm = getQuantitationStep(pmList);
                if(pm == null) continue;
                setInferenceMethod(pm, "Feature to peptide inference method", xTracker.PEPTIDE_FEATURE_INFERENCE);
                setInferenceMethod(pm, "Peptide to protein inference method", xTracker.PROTEIN_PEPTIDE_INFERENCE);
                setInferenceMethod(pm, "Protein to protein group inference method", xTracker.PROTEIN_GROUP_PROTEIN_INFERENCE);
                setInferenceMethod(pm, "Assay to Study Variables inference method", xTracker.SV_ASSAY_INFERENCE);
            }
            MzQuantMLMarshaller marshaller = new MzQuantMLMarshaller(outputFilename);
            marshaller.marshall(mzq);
        }
        //codes for MS1 feature list
                                        //FeatureList
//                                Feature qFeature = feature.convertToQfeature();
//                                qFeature.setId(msrunID+"-"+qFeature.getId());
//                                featureLists.get(msrunID).getFeature().add(qFeature);
//                                //FeatureList quantitation
//                                if (xTracker.study.needFeatureQuantitation()) {
//                                    for (String quantitationName : xTracker.study.getQuantitationNames()) {
//                                        Row row = new Row();
//                                        row.setObjectRef(qFeature);
//                                        HashMap<String, Double> quants = feature.getQuantities(quantitationName);
//                                        for (String assayID : msrun_assayIDs_map.get(msrunID)) {
//                                            row.getValue().add(String.valueOf(quants.get(assayID)));
//                                        }
//                                        msrun_featureQL_map.get(msrunID).get(quantitationName).getDataMatrix().getRow().add(row);
//                                    }
//                                }
                                //PeptideConsensus EvidenceRef
//                                EvidenceRef evidence = new EvidenceRef();
//                                evidence.getAssayRefs().addAll(xTracker.study.getMSRun(msrunID).getAssays());
//                                evidence.setFeatureRef(qFeature);
//                                for (Identification identification : feature.getIdentifications()) {
//                                    if (identification.getSii() == null) {//not from mzIdentML file
//                                        evidence.getIdRefs().add(identification.getId());
//                                    } else {
//                                        evidence.getIdRefs().add(identification.getSii().getId());
//                                    }
//                                }
//                                pc.getEvidenceRef().add(evidence);

        System.out.println("output into mzQuantML file "+outputFilename+" done");
    }

    private ProcessingMethod getQuantitationStep(List<ProcessingMethod> pmList) {
        for (ProcessingMethod pm : pmList) {
            List<AbstractParam> params = pm.getParamGroup();
            for (AbstractParam param : params) {
                if (param instanceof UserParam) {
                    UserParam userParam = (UserParam) param;
                    String paramName = userParam.getName().toLowerCase();
                    if (paramName.equals("plugin type")) {
                        if(userParam.getValue().toLowerCase().contains("quantitation")) return pm;
                    }
                }
            }
        }
        return null;
    }

    private void setInferenceMethod(ProcessingMethod pm,String type,int value) {
        List<AbstractParam> params = pm.getParamGroup();
        for (AbstractParam param : params) {
            if (param instanceof UserParam) {
                UserParam userParam = (UserParam) param;
                if (userParam.getName().equalsIgnoreCase(type)) {//already exist, set the value anyway
                    userParam.setValue(getInferenceMethodName(value));
                    return;
                }
            }
        }
        UserParam param = new UserParam();
        param.setName(type);
        param.setValue(getInferenceMethodName(value));
        pm.getParamGroup().add(param);
    }
    
    private String getInferenceMethodName(int value){
        switch (value) {
            case QuantitationLevel.MEAN:
                return "mean";
            case QuantitationLevel.MEDIAN:
                return "median";
            case QuantitationLevel.SUM:
                return "sum";
            case QuantitationLevel.WEIGHTED_AVERAGE:
                return "weightedAverage";
        }
        return "unrecognized method";       
    }

    private String getCorrectNCName(String input){
        Matcher match = patternNCName.matcher(input);
        input = match.replaceAll("_");
        Matcher matchFirstPosition = patternFirstPosition.matcher(input);
        if(!matchFirstPosition.find()){
            return "_"+input;
        }
        return input;
    }
//    private void loadParams(String dataFile) {
//        XMLparser parser = new XMLparser(dataFile);
//        parser.validate("outputMZQ");
//        int i = 0;
//
//        NodeList itemList = parser.getElement("outputMZQ").getChildNodes();
//        for (i = 0; i < itemList.getLength(); i++) {
//            Node item = itemList.item(i);
//            if (item.getNodeType() == Node.ELEMENT_NODE) {
//                if (item.getNodeName().equals("outputFilename")) {
//                    outputFilename = item.getTextContent();
//                }
//            }
//        }
//    }
    /**
     * Gets the plugin description.
     * @return plugin description
     */
    @Override
    public String getDescription() {
        return description;
    }
    /**
     * Gets the plugin name.
     * @return plugin name
     */
    @Override
    public String getName() {
        return name;
    }
    /**
     * Gets the plugin version.
     * @return pluginv ersion
     */
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean supportMS1() {
        return true;
    }

    @Override
    public boolean supportMS2() {
        return true;
    }
}
