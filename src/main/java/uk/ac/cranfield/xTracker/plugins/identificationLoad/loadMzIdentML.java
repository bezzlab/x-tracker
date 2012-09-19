package uk.ac.cranfield.xTracker.plugins.identificationLoad;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.xml.validation.Validator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.AnalysisData;
import uk.ac.ebi.jmzidml.model.mzidml.DBSequence;
import uk.ac.ebi.jmzidml.model.mzidml.Modification;
import uk.ac.ebi.jmzidml.model.mzidml.Peptide;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidenceRef;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationItem;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationList;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xModification;
import uk.ac.cranfield.xTracker.data.xParam;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.cranfield.xTracker.xTracker;

/**
 *
 * @author Jun Fan@cranfield
 */
public class loadMzIdentML implements identData_loadPlugin{
    /**
     * reflect the relationship between raw spectral files and their corresponding identification files
     */
    private HashMap<String, String> spectra_identification_map = new HashMap<String, String>();
    /**
     * the validator to validate mzidentml file according to the xsd file
     */
    private Validator validator;
    /**
     * plugin information variables
     */
    private final String name = "Load MzIdentML";
    private final String version = "1.00";
    private final String description = "Loads identification data from PSI standard mzIdentML files";
    private final String MZID_XSD = "mzIdentML1.1.0.xsd";
    
    @Override
    public void start(String paramFile) {
        //parse the parameter file and retrieve spectral file and identification file relationship list
//        java.net.URL xmlFileURL = MzIdentMLElement.class.getClassLoader().getResource("MzIdentMLElement.cfg.xml");

        loadParams(paramFile);
        validator = XMLparser.getValidator(MZID_XSD);
//        int proteinIndex = 1;
        for(String rawSpectra:spectra_identification_map.keySet()){
            String identFile = spectra_identification_map.get(rawSpectra);
            String msrunRaw = xTracker.study.getMSRunIDfromRawFile(rawSpectra);//already checked !=null in the loadParam
            String msrunIdent = xTracker.study.getMSRunIDfromIdentificationFile(identFile);
            if(msrunIdent.equals(xTracker.study.UNASSIGNED)){//this identification file has not been parsed
                xTracker.study.setIdentificationFileMSRunMap(identFile, msrunRaw);
                boolean validFlag = XMLparser.validate(validator, identFile);
                if(!validFlag){
                    System.out.println("The identification file "+identFile+" is not a proper mzIdentML file");
                    System.exit(1);
                }
                System.out.println(identFile+" is valid, now start to parse...");
                MzIdentMLUnmarshaller unmarshaller = new MzIdentMLUnmarshaller(new File(identFile));
                boolean autoResolveFlag = MzIdentMLElement.SpectrumIdentificationItem.isAutoRefResolving()
                        && MzIdentMLElement.SpectrumIdentificationResult.isAutoRefResolving()
                        && MzIdentMLElement.PeptideEvidenceRef.isAutoRefResolving()
                        && MzIdentMLElement.PeptideEvidence.isAutoRefResolving()
                        && MzIdentMLElement.CvParam.isAutoRefResolving()
                        && MzIdentMLElement.DBSequence.isAutoRefResolving();
                if (!autoResolveFlag) {
                    System.out.println("The auto resolving is not allowed for SIR, SII, PE or PERef, please allow them in the configuration file MzIdentMLElement.cfg.xml");
                    System.exit(1);
                }

                AnalysisData analysisData = unmarshaller.unmarshal(MzIdentMLElement.AnalysisData);
                List<SpectrumIdentificationList> silList = analysisData.getSpectrumIdentificationList();
                for (SpectrumIdentificationList sil : silList) {
                    List<SpectrumIdentificationResult> sirList = sil.getSpectrumIdentificationResult();
//          1.	For each available SpectrumIdentificationResult (SIR)
                    for (SpectrumIdentificationResult sir : sirList) {
//              a.	Find the raw spectral file by using the location attribute from the SpectraData via the mandatory attribute spectraDataRef
                        //need to modify the MzIdentMLElement.cgf.xml line 184 autoRefResolving to true for SIR
//                        System.out.println(sir.getSpectraData().getLocation());
                        //otherwise use the statement below
//                System.out.println(unmarshaller.getElementAttributes(sir.getSpectraDataRef(),SpectraData.class).get("location"));
//              b.	The spectrum id is retrieved from the mandatory spectrumID attribute
//                        System.out.println(sir.getSpectrumID());
//              c.	The id is retrieved from the mandatory id attribute
//                        System.out.println(sir.getId());
//              d.	Find the top identification which passes the threshold:
//              For each SSI under the current SIR
                        SpectrumIdentificationItem selected = null;
                        List<SpectrumIdentificationItem> siiList = sir.getSpectrumIdentificationItem();
                        for (SpectrumIdentificationItem sii : siiList) {
//                  i.	If the mandatory passThreshold attribute is false, jump to the next SSI
//                  ii.	If the mandatory rank attribute equals to 1, the top SSI is selected, break the loop;
//                     else jump to next SSI
//                    if(sii.getRank()==1) {
                            if (sii.isPassThreshold() && sii.getRank() == 1) {
                                selected = sii;
                                break;
                            }
                        }//end of sii list
//              e.	If the top SSI found 
                        if (selected != null) {
                            //If the peptide_ref is available (as it is optional)
                            //change autoRefResolving to true in SII line 139
                            Peptide peptide = selected.getPeptide();
                            if (peptide != null) {
//                      a) retrieve the peptide sequence and modification(s) from the subelement PeptideSequence and Modification respectively from the referenced peptide
//                                System.out.println(peptide.getPeptideSequence());
                                Identification identification = new Identification(sir.getId(), sir.getSpectraData().getLocation(), sir.getSpectrumID(), selected, sir.getCvParam(), identFile); 
                                identification.setMz(selected.getExperimentalMassToCharge());
//                      b) for each available PeptideEvidence (PE)
                                List<PeptideEvidenceRef> peRefList = selected.getPeptideEvidenceRef();
//                                boolean foundPE = false;
                                ArrayList<xProtein> proteins = new ArrayList<xProtein>();
//                                xProtein protein;
                                for (PeptideEvidenceRef peRef : peRefList) {
                                    //autoResolving set to true in line 325 for PeptideEvidenceRef
                                    PeptideEvidence pe = peRef.getPeptideEvidence();
//                          i) if isDecoy attribute does not exists or equals to true (does not say if the optional attribute does not exist, which value should be expected), jump to next PE
                                    if (pe.isIsDecoy()) {
                                        continue;
                                    }
//                                    foundPE = true;
//                          ii)retrieve protein id and accession from the mandatory attributes id and accession respectively from the referenced DBSequence in the PE
                                    DBSequence dbs = pe.getDBSequence();
                                    SearchDatabase sd = xTracker.study.getSearchDatabase(dbs.getSearchDatabaseRef());
                                    if(sd==null){//the first time to see the currect searchDatabaseRef
                                        //autoResolving set to true in line 211 for DBSequence
                                        uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase isd = dbs.getSearchDatabase();
                                        sd = new SearchDatabase();
                                        sd.setId(isd.getId());
                                        sd.setLocation(isd.getLocation());
                                        xParam param = new xParam(isd.getDatabaseName());
                                        sd.setDatabaseName(param.convertToQparam());
                                        xTracker.study.addSearchDatabase(isd.getId(), sd);
                                    }
//                                    protein = xTracker.study.retrieveProtein(dbs.getAccession(), dbs.getId(), sd);
                                    proteins.add(xTracker.study.retrieveProtein(dbs.getAccession().replace("|", "-"), dbs.getId().replace("|", "-"), sd));
                                }
//                      c) if no non-decoy PE exists, an artificial protein is generated to be specific for the peptide
//                                if (!foundPE) {
//                                    System.out.println("Protein " + proteinIndex);
//                                    proteinIndex++;
//                                }
                                
                                List<Modification> iMods = peptide.getModification();
                                HashSet<xModification> mods = new HashSet<xModification>();
                                for(Modification iMod:iMods){
                                    mods.add(new xModification(iMod));
                                }
                                for(xProtein protein:proteins){
                                    xPeptide pep = protein.getPeptide(peptide.getPeptideSequence(), mods,peptide.getId());
//                                    xPeptide pep = protein.getPeptide(peptide.getPeptideSequence(), mods);
//                                    pep.setPeptideID(protein.getAccession()+"-"+peptide.getId());
                                    xFeature feature = pep.getFeature(msrunRaw,selected.getChargeState());
                                    if(feature == null){
//                                        feature = new xFeature(protein.getAccession(), peptide.getId(), selected.getChargeState());
//                                        feature = new xFeature(protein.getAccession(), pep.getPeptideID(), selected.getChargeState());
                                        feature = new xFeature(msrunRaw,pep.getPeptideID(), selected.getChargeState());
//                                        feature = new xFeature(protein.getAccession(), pep.getSeq(), selected.getChargeState());
                                        pep.addFeature(msrunRaw,feature);
                                    }
                                    identification.setFeature_ref(feature.getId());
                                    feature.addIdentification(identification);
                                }
                            }//peptide found
                        }//if sii not null
                    }//end of sir list
                }//end of spectrumIdentificationList
                xTracker.study.setIdentificationFileMSRunMap(identFile, msrunRaw);
                
            //if(msrunIdent.equals(xTracker.study.UNASSIGNED))
            }else{//the mzIdentML has been done
                if(!msrunIdent.equals(msrunRaw)){
                    System.out.println("Warning: one mzIdentML file "+identFile+" has been related to more than one RawFilesGroup: "+msrunIdent+" "+msrunRaw);
                    System.exit(1);
                }
            }
        }
        System.out.println("Load mzIdentML plugin done");
    }

    /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     */
    private void loadParams(String dataFile) {
        XMLparser parser = new XMLparser(dataFile);
        parser.validate("SpectralIdentificationList");
        System.out.println("Validation successful");
        
        NodeList itemList = parser.getElement("SpectralIdentificationList").getChildNodes();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getNodeName().equals("SpectralIdentificationPair")) {
                    String spectra = item.getAttributes().getNamedItem("spectra").getTextContent();
                    String identification = item.getAttributes().getNamedItem("identification").getTextContent();
                    if(xTracker.study.getMSRunIDfromRawFile(spectra)==null){
                        System.out.println("The spectral file defined in the xtp file "+spectra+" is not in the mzQuantML configuration file");
                        System.exit(1);
                    }
                    if(xTracker.study.getMSRunIDfromIdentificationFile(identification)==null){
                        System.out.println("The identification file defined in the xtp file "+identification+" is not in the mzQuantML configuration file");
                        System.exit(1);
                    }
                    if(spectra_identification_map.containsKey(spectra)) System.out.println("Warning: the spectra file "+spectra+" has more than one related identification files, now only consider identification file "+identification);
                    //assign msrun id from raw file to the related identification file
                    spectra_identification_map.put(spectra,identification);
                } 
            }
        }
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
    public String getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
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
