/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.xTracker.plugins.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.xModification;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.xTracker;
import uk.ac.ebi.pride.jmztab.MzTabFile;
import uk.ac.ebi.pride.jmztab.MzTabParsingException;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.MsFile;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.Peptide;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.jmztab.model.Subsample;
import uk.ac.ebi.pride.jmztab.model.Unit;
import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.liv.jmzqml.model.mzqml.RawFile;
import uk.ac.liv.jmzqml.model.mzqml.SearchDatabase;

/**
 *
 * @author Jun Fan@cranfield
 */
public class outputMzTab extends outPlugin{
    private String name = "Output mzTab";
    private String version = "1.0";
    private String description = "export the quantitation result into the lightweighted mzTab file specified in the parameter file";
    private String outputFilename;

    @Override
    public void start(String paramFile) {
        try {
            System.out.println(getName()+" starts");
            outputFilename = getOutputFileName(paramFile);
            if(outputFilename == null){
                System.out.println("Can not get the output file name. There are several reasons: the wrong plugin parameter file using wrong xsd file, not defined in the parameter file");
                System.exit(1);
            }
            System.out.println(outputFilename);
            MzTabFile mztab = new MzTabFile();
            //only one quantitation method within one pipeline
            final Param quantitationMethod = getQuantitationMethod();
            for(MSRun msrun:xTracker.study.getMSRuns()){
                Unit unit = new Unit();
                unit.setUnitId(msrun.getID());
                //6.2.14 {UNIT_ID}-mod 0..1 A list of “|” separated parameters describing all (distinct) PTMs reported in this unit
                //6.2.16 {UNIT_ID}-quantification_method 0..1
                if(quantitationMethod!=null) unit.setQuantificationMethod(quantitationMethod);
                //6.2.19 {UNIT_ID}-ms_file[1-n]-format 0..*
                //6.2.20 {UNIT_ID}-ms_file[1-n]-location 0..*
                List<RawFile> rawfiles = msrun.getRawFilesGroup().getRawFile();
                for (int i = 0; i < rawfiles.size(); i++) {
                    RawFile rawFile = rawfiles.get(i);
                    MsFile ms = new MsFile();
                    ms.setLocation(rawFile.getLocation());
                    unit.setMsFile(i+1, ms);
                }
                
                List<Assay> assays = msrun.getAssays();
                ArrayList<Subsample> subs = new ArrayList();
                for (int i = 0; i < assays.size(); i++) {
                    Assay assay = assays.get(i);
                    Subsample sub = new Subsample(unit.getUnitId(), i+1);
                    sub.setDescription(assay.getId());
                    subs.add(sub);
                }
                unit.setSubsamples(subs);
                
                unit.setSoftware(xTracker.study.getMetadata().getSoftware());
                mztab.setUnit(unit);
            }
            //as mzTab only allow one abundance in the column, rest quantities are outputed in the optional columns
            //this section treats the first quantity type having abundance in its name as the abundance and the rest are put into optional columns
            ArrayList<CvParam> quantitations = new ArrayList<CvParam>();
            String abundanceName = "";
            for(String quantitationName:xTracker.study.getQuantitationNames()){
                if(quantitationName.toLowerCase().contains("abundance")&&abundanceName.length()==0) {
                    abundanceName = quantitationName;
                }else{
                    quantitations.add(xTracker.study.getQuantitationNameCvParam(quantitationName).getCvParam());
                }
            }
            //msrun corresponds to unit
            for(MSRun msrun:xTracker.study.getMSRuns()){
                for(xProtein protein:xTracker.study.getProteins()){
                    SearchDatabase sd = protein.getProtein().getSearchDatabase();
//                    String database = sd.getDatabaseName().getParamGroup().getName();
                    String database;
                    if(sd.getDatabaseName().getCvParam()!=null){
                        database = sd.getDatabaseName().getCvParam().getName();
                    }else{
                        database = sd.getDatabaseName().getUserParam().getName();
                    }
                    String dbVersion = sd.getVersion();
                    if(xTracker.study.needProteinQuantitation()){
                        Protein tabProt = new Protein();
                        tabProt.setAccession(protein.getAccession());
                        tabProt.setUnitId(msrun.getID());
                        //database
                        tabProt.setDatabase(database);
                        if(dbVersion!=null) tabProt.setDatabaseVersion(dbVersion);
                        //quantitation
                        for (int i = 0; i < msrun.getAssays().size(); i++) {
                            final String assayID = msrun.getAssays().get(i).getId();
                            if (abundanceName.length() > 0) {
                                tabProt.setAbundance(i + 1, protein.getQuantity(abundanceName, assayID), Double.NaN, Double.NaN);
                            }
                            for (CvParam param : quantitations) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("opt_cv_");
                                if (param.getAccession().length() > 0) {
                                    sb.append(param.getAccession());
                                } else {
                                    sb.append(param.getName());
                                }
                                sb.append("_");
                                sb.append(assayID);
                                tabProt.setCustomColumn(sb.toString(), String.valueOf(protein.getQuantity(param.getName(), assayID)));
                            }
                        }
                        mztab.addProtein(tabProt);
                    }
                    for(xPeptide peptide:protein.getAllPeptides()){
                        if(xTracker.study.needPeptideQuantitation()){
                            Peptide tabPep = new Peptide();
                            tabPep.setSequence(peptide.getSeq());
                            tabPep.setAccession(protein.getAccession());
                            tabPep.setUnitId(msrun.getID());
                            tabPep.setDatabase(database);
                            if(dbVersion!=null) tabPep.setDatabaseVersion(dbVersion);
                            //modification
                            ArrayList<Modification> mods = new ArrayList<Modification>();
                            for (xModification mod : peptide.getModifications()) {
                                int pos = mod.getPosition();
                                if (pos == xModification.C_TERM_LOCATION) {
                                    pos = peptide.getSeq().length() + 1;
                                }
                                mods.add(new Modification(mod.convertToQmodification().getCvParam().get(0).getAccession(), pos));
                            }
                            tabPep.setModification(mods);
                            for (int i = 0; i < msrun.getAssays().size(); i++) {
                                final String assayID = msrun.getAssays().get(i).getId();
                                if (abundanceName.length() > 0) {
                                    tabPep.setAbundance(i + 1, peptide.getQuantity(abundanceName, assayID), Double.NaN, Double.NaN);
                                }
                                for (CvParam param : quantitations) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("opt_cv_");
                                    if (param.getAccession().length() > 0) {
                                        sb.append(param.getAccession());
                                    } else {
                                        sb.append(param.getName());
                                    }
                                    sb.append("_");
                                    sb.append(assayID);
                                    tabPep.setCustomColumn(sb.toString(), String.valueOf(peptide.getQuantity(param.getName(), assayID)));
                                }
                            }
                            mztab.addPeptide(tabPep);
                        }
                    }
                }
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));
            out.append(mztab.toMzTab());
            out.close();
        } catch (IOException e) {
            System.out.println("Problems while writing to file " + outputFilename + "!\n" + e);
        } catch (MzTabParsingException ex) {
            Logger.getLogger(outputMzTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Param getQuantitationMethod() throws MzTabParsingException{
        CvParam param = xTracker.study.getQuantitationMethod();
        if (param == null) return null;
        return new Param(param.getCv().getId(),param.getAccession(),param.getName(),param.getValue());
    }
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
