package uk.ac.cranfield.xTracker.plugins.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.CvParamRef;
import uk.ac.liv.jmzqml.model.mzqml.DataMatrix;
import uk.ac.liv.jmzqml.model.mzqml.EvidenceRef;
import uk.ac.liv.jmzqml.model.mzqml.Feature;
import uk.ac.liv.jmzqml.model.mzqml.FeatureList;
import uk.ac.liv.jmzqml.model.mzqml.MzQuantML;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensus;
import uk.ac.liv.jmzqml.model.mzqml.PeptideConsensusList;
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
import uk.ac.liv.jmzqml.model.mzqml.DataProcessing;
import uk.ac.liv.jmzqml.model.mzqml.ProcessingMethod;
import uk.ac.liv.jmzqml.model.mzqml.Software;
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
    
    @Override
    public void start(String paramFile) {
        System.out.println(getName()+" starts");
        outputFilename = getOutputFileName(paramFile);
        if(outputFilename == null){
            System.out.println("Can not get the output file name. There are several reasons: the wrong plugin parameter file using wrong xsd file, not defined in the parameter file");
            System.exit(1);
        }
        System.out.println(outputFilename);
        MzQuantML mzq = xTracker.study.getMzQuantML();
        ProteinList proteinList = new ProteinList();
        PeptideConsensusList pcList = new PeptideConsensusList();
        HashMap<String,QuantLayer> peptideQLs = new HashMap<String,QuantLayer>();
        HashMap<String,QuantLayer> proteinQLs = new HashMap<String,QuantLayer>();
        HashMap<String,QuantLayer> peptideRatioQLs = new HashMap<String,QuantLayer>();
        HashMap<String,QuantLayer> proteinRatioQLs = new HashMap<String,QuantLayer>();
        HashMap<String,QuantLayer> svQLs = new HashMap<String,QuantLayer>();
        HashMap<String,ArrayList<String>> msrun_assayIDs_map = new HashMap<String, ArrayList<String>>();
        HashMap<String,HashMap<String, QuantLayer>> msrun_featureQL_map = new HashMap<String, HashMap<String, QuantLayer>>();
        
        ArrayList<Assay> allAssays = new ArrayList<Assay>();
        for (MSRun msrun : xTracker.study.getMSRuns()) {
            FeatureList featureList = new FeatureList();
            featureList.setId("featureList_"+msrun.getID());
            featureList.setRawFilesGroupRef(msrun.getRawFilesGroup());
            featureLists.put(msrun.getID(),featureList);
            
            ArrayList<String> assayIDs = new ArrayList<String>();
            for (Assay assay : msrun.getAssays()) {
                assayIDs.add(assay.getId());
            }
            msrun_assayIDs_map.put(msrun.getID(), assayIDs);
            allAssays.addAll(msrun.getAssays());

            HashMap<String, QuantLayer> featureQLs = new HashMap<String, QuantLayer>();
            if (xTracker.study.needFeatureQuantitation()) {
                for (String quantitationName : xTracker.study.getQuantitationNames()) {
                    QuantLayer ql = new QuantLayer();
                    ql.setId("MS2AssayQuantLayer_" + msrun.getID() + "_" + quantitationName);
                    ql.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
                    ql.getColumnIndex().addAll(msrun.getAssays());
                    DataMatrix matrix = new DataMatrix();
                    ql.setDataMatrix(matrix);
                    featureQLs.put(quantitationName, ql);
                }
            }
            msrun_featureQL_map.put(msrun.getID(), featureQLs);
        }
        
        CvParamRef peptideRatioCvParam = xTracker.study.createCvParam("Peptide ratio", "MS:1001132");
        CvParamRef proteinRatioCvParam = xTracker.study.createCvParam("Protein ratio", "MS:1001134");

        List<StudyVariable> svs = xTracker.study.getMzQuantML().getStudyVariableList().getStudyVariable();
        if (svs.size()>1){
            for(String quantitationName:xTracker.study.getQuantitationNames()){
                QuantLayer svQL = new QuantLayer();
                svQL.setId("SV_QuantLayer_Protein_"+quantitationName);
                svQL.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
                svQL.getColumnIndex().addAll(svs);
                DataMatrix svMatrix = new DataMatrix();
                svQL.setDataMatrix(svMatrix);
                svQLs.put(quantitationName, svQL);
            }
        }
        
        for (String quantitationName : xTracker.study.getQuantitationNames()) {
            QuantLayer peptideQL = new QuantLayer();
            QuantLayer proteinQL = new QuantLayer();
            peptideQL.setId("MS2AssayQuantLayer_Peptides_" + quantitationName);
            proteinQL.setId("MS2AssayQuantLayer_Proteins_" + quantitationName);
            peptideQL.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
            proteinQL.setDataType(xTracker.study.getQuantitationNameCvParam(quantitationName));
            peptideQL.getColumnIndex().addAll(allAssays);
            proteinQL.getColumnIndex().addAll(allAssays);
            DataMatrix pepMatrix = new DataMatrix();
            DataMatrix proMatrix = new DataMatrix();
            peptideQL.setDataMatrix(pepMatrix);
            proteinQL.setDataMatrix(proMatrix);
            peptideQLs.put(quantitationName, peptideQL);
            proteinQLs.put(quantitationName, proteinQL);
            if(xTracker.study.isRatioRequired()){//there is ratio calculation requirement
                if(!xTracker.study.getAssayRatios().isEmpty()){
                    QuantLayer peptideRatioQL = new QuantLayer();
                    peptideRatioQL.setId("RatioQuantLayer_Peptides_" + quantitationName);
                    peptideRatioQL.setDataType(peptideRatioCvParam);
                    peptideRatioQL.getColumnIndex().addAll(xTracker.study.getAssayRatios());
                    DataMatrix pepRatioMatrix = new DataMatrix();
                    peptideRatioQL.setDataMatrix(pepRatioMatrix);
                    peptideRatioQLs.put(quantitationName, peptideRatioQL);
                }

                QuantLayer proteinRatioQL = new QuantLayer();
                proteinRatioQL.setId("RatioQuantLayer_Proteins_"+quantitationName);
                proteinRatioQL.setDataType(proteinRatioCvParam);
                proteinRatioQL.getColumnIndex().addAll(mzq.getRatioList().getRatio());
                DataMatrix proRatioMatrix = new DataMatrix();
                proteinRatioQL.setDataMatrix(proRatioMatrix);
                proteinRatioQLs.put(quantitationName, proteinRatioQL);
            }
        }

        if(xTracker.study.getPipelineType()==Study.MS2_TYPE){
//            for (xProtein protein : xTracker.study.getProteins()) {
//                proteinList.getProtein().add(protein.getProtein());
//                for (String msrun : protein.getMSRunIDs()) {
//                    for (xPeptideConsensus pepCon : protein.getPeptideCons(msrun)) {
                    
//                outloop:
//                    ArrayList<Assay> assays = xTracker.study.getMSRun(msrun).getAssays();
            HashSet<xProtein> proteinSet = new HashSet<xProtein>();

            for (xProtein protein : xTracker.study.getProteins()) {
                proteinSet.add(protein);
                ArrayList<xPeptideConsensus> peptideCons = protein.getPeptides();
                if (peptideCons == null) {
                    continue;
                }
                for (xPeptideConsensus pepCon : peptideCons) {
                    for (xPeptide peptide : pepCon.getPeptides()) {
                        PeptideConsensus pc = peptide.convertToQpeptideConsensus();
                        pc.setSearchDatabaseRef(protein.getProtein().getSearchDatabaseRef());
                        for (String msrunID : peptide.getMSRunIDs()) {
                            for (xFeature feature : peptide.getFeatures(msrunID)) {
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
                                for(Identification identification : feature.getIdentifications()){
                                    //FeatureList
                                    Feature qFeature = identification.convertToQfeature();
                                    qFeature.setId(feature.getId()+"-"+qFeature.getId());
                                    qFeature.setCharge(String.valueOf(feature.getCharge()));
                                    qFeature.setRt("null");
                                    featureLists.get(msrunID).getFeature().add(qFeature);
                                    //FeatureList quantitation
                                    if (xTracker.study.needFeatureQuantitation()) {
                                        for (String quantitationName : xTracker.study.getQuantitationNames()) {
                                            Row row = new Row();
                                            row.setObjectRef(qFeature);
                                            HashMap<String, Double> quants = identification.getQuantities(quantitationName);
                                            for (String assayID : msrun_assayIDs_map.get(msrunID)) {
                                                row.getValue().add(String.valueOf(quants.get(assayID)));
                                            }
                                            msrun_featureQL_map.get(msrunID).get(quantitationName).getDataMatrix().getRow().add(row);
                                        }
                                    }
                                    //PeptideConsensus EvidenceRef
                                    EvidenceRef evidence = new EvidenceRef();
                                    evidence.getAssayRefs().addAll(xTracker.study.getMSRun(msrunID).getAssays());
                                    evidence.setFeatureRef(qFeature);
                                    if (identification.getSii() == null) {//not from mzIdentML file
                                        evidence.getIdRefs().add(identification.getId());
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
                                row.setObjectRef(pc);
                                HashMap<String, Double> quants = peptide.getQuantities(quantitationName);
                                for (Assay assay : allAssays) {
                                    row.getValue().add(String.valueOf(quants.get(assay.getId())));
                                }
                                peptideQLs.get(quantitationName).getDataMatrix().getRow().add(row);
                                if(!xTracker.study.getAssayRatios().isEmpty()){
                                    Row ratioRow = new Row();
                                    ratioRow.setObjectRef(pc);
                                    HashMap<String, Double> ratioValues = peptide.getRatios(quantitationName);
                                    for (xRatio ratio:xTracker.study.getRatios()){
                                        if(ratio.getType().equals(xRatio.ASSAY)) ratioRow.getValue().add(String.valueOf(ratioValues.get(ratio.getId())));
                                    }
                                    peptideRatioQLs.get(quantitationName).getDataMatrix().getRow().add(ratioRow);
                                }
                            }
                        }
                    }
                }
            }
            if (xTracker.study.needFeatureQuantitation()) {
                for(String msrunID:msrun_featureQL_map.keySet()){
                    HashMap<String,QuantLayer> featureQLs = msrun_featureQL_map.get(msrunID);
                    for (QuantLayer ql : featureQLs.values()) {
                        featureLists.get(msrunID).getMS2AssayQuantLayer().add(ql);
                    }
                }
            }
            //ProteinList
            proteinList.setId("ProteinList");
            for(xProtein pro:proteinSet){
                proteinList.getProtein().add(pro.getProtein());
                if (xTracker.study.needProteinQuantitation()) {
                    for (String quantitationName : xTracker.study.getQuantitationNames()) {
                        Row row = new Row();
                        row.setObjectRef(pro.getProtein());
                        HashMap<String, Double> quants = pro.getQuantities(quantitationName);
                        for (Assay assay : allAssays) {
                            row.getValue().add(String.valueOf(quants.get(assay.getId())));
                        }
                        proteinQLs.get(quantitationName).getDataMatrix().getRow().add(row);
                        
                        if(svs.size()>1){
                            Row svRow = new Row();
                            svRow.setObjectRef(pro.getProtein());
                            HashMap<String,Double> svValues = pro.getStudyVariableQuantities(quantitationName);
                            for(StudyVariable sv:svs){
                                svRow.getValue().add(String.valueOf(svValues.get(sv.getId())));
                            }
                            svQLs.get(quantitationName).getDataMatrix().getRow().add(svRow);
                        }
                        
                        if (xTracker.study.isRatioRequired()) {
                            Row ratioRow = new Row();
                            ratioRow.setObjectRef(pro.getProtein());
                            HashMap<String, Double> ratioValues = pro.getRatios(quantitationName);
                            for (xRatio ratio:xTracker.study.getRatios()){
                                ratioRow.getValue().add(String.valueOf(ratioValues.get(ratio.getId())));
                            }
                            proteinRatioQLs.get(quantitationName).getDataMatrix().getRow().add(ratioRow);
                        }
                    }
                }
            }
            if(xTracker.study.needProteinQuantitation()){
                for (QuantLayer ql:proteinQLs.values()) {
                    proteinList.getAssayQuantLayer().add(ql);
                }
                
                if(svs.size()>1){
                    for (QuantLayer svQL: svQLs.values()){
                        proteinList.getStudyVariableQuantLayer().add(svQL);
                    }
                }
                
                if (xTracker.study.isRatioRequired()) {
                    for (QuantLayer ratioQL : proteinRatioQLs.values()) {
                        proteinList.getRatioQuantLayer().add(ratioQL);
                    }
                }
            }
            
            mzq.setProteinList(proteinList);
            //PeptideConsensusList
            pcList.setId("PeptideList");
            pcList.setFinalResult(true);
            if(xTracker.study.needPeptideQuantitation()){
                for (QuantLayer ql:peptideQLs.values()) {
                    pcList.getAssayQuantLayer().add(ql);
                }
                if(!xTracker.study.getAssayRatios().isEmpty()){
                    for (QuantLayer ratioQL: peptideRatioQLs.values()){
                        pcList.getRatioQuantLayer().add(ratioQL);
                    }
                }
            }
            mzq.getPeptideConsensusList().add(pcList);
            //searchDatabase
            for(SearchDatabase sd:xTracker.study.getSearchDatabases()){
                mzq.getInputFiles().getSearchDatabase().add(sd);
            }
            //FeatureList
            for(FeatureList featureList:featureLists.values()){
                mzq.getFeatureList().add(featureList);
            }
            //data processing inference methods
            List<DataProcessing> dpList = mzq.getDataProcessingList().getDataProcessing();
            for(DataProcessing dp:dpList){
                Object tmp = dp.getSoftwareRef();
                if (tmp == null) continue;
                String swName = ((Software) tmp).getId();
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
