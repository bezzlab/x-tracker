/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.xTracker.plugins.quantitation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.cranfield.xTracker.Utils;
import uk.ac.cranfield.xTracker.data.Identification;
import uk.ac.cranfield.xTracker.data.MSRun;
import uk.ac.cranfield.xTracker.data.xFeature;
import uk.ac.cranfield.xTracker.data.xPeptide;
import uk.ac.cranfield.xTracker.data.xPeptideConsensus;
import uk.ac.cranfield.xTracker.data.xProtein;
import uk.ac.cranfield.xTracker.utils.XMLparser;
import uk.ac.cranfield.xTracker.xTracker;

/**
 *
 * @author Jun Fan@cranfield
 */
public class emPAIquantitation  extends quantitationPlugin{
    private String name = "emPAI calculation";
    private String version = "1.0";
    private String description = "The plugin computes spectral counting with the exponentially modified protein abundance index."
            + "For each identified protein, it counts the number of observed peptides. Then it reads a fasta file with"
            + " the sequence of every protein, and estimates the number of observable peptides for each protein."
            + "Finally, it calculates the protein abundance index for every protein";
    private final String EMPAI_VALUE = "emPAI value";
    private int lowerLimit;
    private int upperLimit;
    private ArrayList<String> fastaFiles = new ArrayList<String>();
    private HashSet<String> identifiedProteins = new HashSet<String>(); 
    private String accessionRegExp = "";
    private String enzyme = "Trypsin";
    private HashMap<String,String> enzymeRegExps = new HashMap<String, String>();
    private HashMap<String,Double> aaMap = new HashMap<String, Double>();
    private HashMap<String,Integer> observable = new HashMap<String, Integer>();
    private HashMap<String,HashMap<String,Integer>> observed = new HashMap<String,HashMap<String, Integer>>();
    private final double hmass = 1.007825;
    @Override
    public void start(String paramFile) {
        System.out.println("Plugin "+getName()+" starts");
        initializeAAMap();
        initializeEnzymeRegExpMap();
        loadParam(paramFile);
        setQuantitationNames();
        int originalUpper = upperLimit;
        int originalLower = lowerLimit;
        
        for(xProtein protein:xTracker.study.getProteins()){
            identifiedProteins.add(protein.getAccession());
            //keys are assay ids, values are the peptide sequence set observed in that msrun
            HashMap<String,HashSet<String>> observedPeptides = new HashMap<String, HashSet<String>>();
            for(MSRun msrun:xTracker.study.getMSRuns()){
                HashSet<String> set = new HashSet<String>();
                observedPeptides.put(msrun.getAssays().get(0).getId(), set);
            }
            ArrayList<xPeptideConsensus> peptideCons = protein.getPeptides();
            for(xPeptideConsensus pepCon:peptideCons){
                for(xPeptide peptide:pepCon.getPeptides()){
                    for (String msrunID : peptide.getMSRunIDs()) {
                        for (xFeature feature : peptide.getFeatures(msrunID)){
                            String assayID = xTracker.study.getMSRun(msrunID).getAssays().get(0).getId();
                            observedPeptides.get(assayID).add(peptide.getSeq());
                            int charge = feature.getCharge();
                            for (Identification identification : feature.getIdentifications()) {
                                double mz = identification.getMz();
                                if (mz == 0) {
                                    continue;
                                }
                                //mz = (mw+(n-1)*h)/n
                                double mw = mz * charge - (charge - 1) * hmass;
                                if (mw > upperLimit) {
                                    upperLimit = (int) Math.ceil(mw / 100) * 100;
                                } else if (mw < lowerLimit) {
                                    lowerLimit = (int) Math.floor(mw / 100) * 100;
                                }
                            }
                        }
                    }
                }
            }
            //keys are assayIDs, and values are observed peptides
            HashMap<String,Integer> map = new HashMap<String, Integer>();
            for(String assayID:observedPeptides.keySet()){
                map.put(assayID, observedPeptides.get(assayID).size());
            }
            observed.put(protein.getAccession(), map);
        }
        if(upperLimit>originalUpper){
            System.out.println("Warning: the upper limit of mw has been increased to "+upperLimit+" due to the identified peptides");
        }
        if(lowerLimit<originalLower){
            System.out.println("Warning: the lower limit of mw has been increased to "+lowerLimit+" due to the identified peptides");
        }
        
        for(String filename:fastaFiles){
            readFasta(filename);
        }
//        for(String accession:observable.keySet()){
//            System.out.println("Protein "+accession+" has "+observable.get(accession) +" observable peptides");
//        }
        for(xProtein protein:xTracker.study.getProteins()){
            String accession = protein.getAccession();
            if(observable.containsKey(accession)){
                HashMap<String,Integer> one = observed.get(accession);
                HashMap<String,Double> quantities = new HashMap<String, Double>();
                for(String assayID:one.keySet()){
                    quantities.put(assayID, modified_index(one.get(assayID), observable.get(accession)));
                }
                protein.setQuantities(EMPAI_VALUE,quantities) ;
            }
        }
        System.out.println("identified proteins not found in the fasta file:"+identifiedProteins.toString());
        System.out.println("Plugin "+getName()+" finished");
    }

    private void loadParam(String paramFile){
        final String baseElement = "emPaiQuantitation";
        XMLparser parser = new XMLparser(paramFile);
        parser.validate(baseElement);
        System.out.println("Validation successful");
        NodeList pepRangeList = parser.getElement("peptideMwRange").getChildNodes();
        for (int i = 0; i < pepRangeList.getLength(); i++) {
            Node node = pepRangeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("minimum")) {
                    lowerLimit = Integer.parseInt(node.getTextContent());
                } else if (node.getNodeName().equals("maximum")) {
                    upperLimit = Integer.parseInt(node.getTextContent());
                }
            }
        }
        if (lowerLimit>=upperLimit){
            System.out.println("Wrong setting on peptide mw range");
            System.exit(1);
        }
        NodeList fastaList = parser.getElement("fastaFiles").getChildNodes();
        for (int i = 0; i < fastaList.getLength(); i++) {
            Node node = fastaList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("fastaFile")) {
                fastaFiles.add(Utils.locateFile(node.getTextContent(),xTracker.folders));
            }
        }
    }
    
    private void readFasta(String filename){
        String line;
        String accession = "";
        StringBuilder seq = new StringBuilder();
        try {
            FileReader fr = new FileReader(new File(filename));
            BufferedReader br = new BufferedReader(fr);
            // read all the lines
            while ((line = br.readLine()) != null) {
                if (line.length() == 0) continue;
                line = line.replaceAll("\n", "");
                if(line.charAt(0)=='>'){//the new protein sequence starts
                    if(accession.length()>0){//not the first one
                        //deal with the previous 
                        if(identifiedProteins.contains(accession)){
                            calculateObservable(accession,seq.toString());
                            identifiedProteins.remove(accession);
                        }
                        seq = new StringBuilder();
                    }
                    if (accessionRegExp.length() == 0) {//no regular expression defined, use space as separator
                        int idx = line.indexOf(" ");
                        if(idx==-1){//no space in the headline
                            accession = line.substring(1);//only need to remove >
                        }else{
                            accession = line.substring(1,idx);
                        }
                    } else {
                        Pattern pattern = Pattern.compile(accessionRegExp);
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()){
                            accession = matcher.group();
                        }
                    }
                }else{
                    line = line.replaceAll("\\s", "").toUpperCase();
                    seq.append(line);
                }
            }
            if(identifiedProteins.contains(accession)) {
                calculateObservable(accession, seq.toString());
                identifiedProteins.remove(accession);
            }
        }catch(IOException ioe){
        }
    }

    @Override
    public void setQuantitationNames() {
        xTracker.study.addQuantitationName(EMPAI_VALUE, "MS:1001905");
    }

    @Override
    public boolean supportMS1(){
        return false;
    }

    @Override
    public boolean supportMS2(){
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

    private void calculateObservable(String accession, String seq) {
//        System.out.println("Accession:"+accession);
//        System.out.println("Sequence:"+seq);
        int count = 0;
        String[] peptides = seq.split(enzymeRegExps.get(enzyme));
        for (int i = 0; i < peptides.length; i++) {
            String peptide = peptides[i];
            double mass = getPeptideMW(peptide);
//            System.out.println("Peptide "+i+": "+peptide+" with mw "+mass);
            if(mass<= upperLimit && mass>=lowerLimit) count++;
        }
//        System.out.println("Protein "+accession+" has "+count+" observable peptides among total "+peptides.length );
        observable.put(accession, count);
    }
    /**
     * Method that calculates the modified protein abundance index (emPAI) of a protein, accordinf to the paper:
     * Ishihama, Y., Oda, Y., Tabata, T., Sato, T., Nagasu, T., Rappsilber, J. and Mann, M. (2005),
     * "Exponentially Modified Protein Abundance Index (emPAI) for Estimation of Absolute Protein Amount in Proteomics
     * by the Number of Sequenced Peptides per Protein", Molecular and cellular proteomics,
     * vol. 4, pp. 1265-1272.
     * @param nb_observed number of observed peptides for this protein
     * @param nb_observable number of observable peptides for this protein
     * @return the emPAI value
     */
    public double modified_index(int nb_observed, int nb_observable) {
        double PAI = (double) nb_observed / (double) nb_observable; // calculate the Protein Abundance Index
        double emPAI = Math.pow(10, PAI) - 1; // calculate the modified Protein Abundance Index

        //round the emPAI with three decimals
//        BigDecimal bd = new BigDecimal(emPAI);
//        BigDecimal bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
//        emPAI = bd2.doubleValue();

        return emPAI;
    }
    
    private double getPeptideMW(String seq){
        double mass = 0.0d;
        for(int i=0;i<seq.length();i++){
            String aa = seq.substring(i, i+1);
            if(aaMap.containsKey(aa)){
                mass += aaMap.get(aa);
            }else{
                System.out.println("Warning: unrecognized AA "+aa+" in peptide sequence "+seq);
            }
        }
        return mass;
    }

    private void initializeAAMap(){
        aaMap.put("A",71.037114);
        aaMap.put("R",156.101111);
        aaMap.put("N",114.042927);
        aaMap.put("D",115.026943);
        aaMap.put("C",103.009185);
        aaMap.put("E",129.042593);
        aaMap.put("Q",128.058578);
        aaMap.put("G",57.021464);
        aaMap.put("H",137.058912);
        aaMap.put("I",113.084064);
        aaMap.put("L",113.084064);
        aaMap.put("K",128.094963);
        aaMap.put("M",131.040485);
        aaMap.put("F",147.068414);
        aaMap.put("P",97.052764);
        aaMap.put("S",87.032028);
        aaMap.put("T",101.047679);
        aaMap.put("U",150.95363);
        aaMap.put("W",186.079313);
        aaMap.put("Y",163.06332);
        aaMap.put("V",99.068414);
    }
    
    private void initializeEnzymeRegExpMap(){
        enzymeRegExps.put("Trypsin", "(?<=[KR])(?=[^P])");
    }
}
