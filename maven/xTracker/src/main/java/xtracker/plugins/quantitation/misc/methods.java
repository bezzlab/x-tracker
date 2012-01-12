
package xtracker.plugins.quantitation.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that contains all the methods used to compute the APEXbasic quantification.
 * These methods are called by other methods in this file, or by the quantAPEXbasic.java class.
 * @author laurie Tonon for X-Tracker
 */
public class methods {

 

    /**
     * Method that computes the retention time of a peptide, tranlsation from the method in MRMaid,
     * from the paper: Krokhin, O. V., Craig, R., Spicer, V., Ens, W., Standing, K. G., Beavis, R. C. and Wilkins, J. A. (2004),
     * "An improved model for prediction of retention times of tryptic peptides in ion pair reversed-phase HPLC",
     * molecular and cellular proteomics, vol. 3, no. 9, pp. 908-919.
     * @param sequence String with the amino acid sequence of the peptide
     * @param a double value of the gradient delay time
     * @param b double value of the slope of acetonitrile gradient
     * @return an array with the retention time and the hydrophobicity
     */
    public static double[] retentionPredict(String sequence, double a, double b) {
        int size = 0;
        char[] string_array; //array with all the amino acids of the sequence
        HashMap amino_array; //hash map with relations between amino acids and their retention coefficients

        sequence = sequence.toUpperCase(); //transform every character into uppercase
        sequence = sequence.trim(); // delete white spaces before and after the sequence
        string_array = sequence.toCharArray(); //transform the sequence into an array of amino acids

        size = sequence.length(); // length of the initial sequence

        amino_array = new HashMap();

        char[] AAs = {'A', 'R', 'N', 'D', 'C', 'G', 'E', 'Q', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V'};
        double[] retention_coefficients = {0.8, -1.3, -1.2, -0.5, -0.8, -0.9, 0.0, -0.9, -1.3, 8.4, 9.6, -1.9, 5.8, 10.5, 0.2, -0.8, 0.4, 11.0, 4.0, 5.0};

        for (int i = 0; i < AAs.length; i++) { //create the hasmap by entering all the keys and values

            amino_array.put(AAs[i], retention_coefficients[i]);

        }


        double sum1 = 0; //sum of the retention coefficients

        for (int i = 0; i < size; i++) { //sum the retention coefficients for all the amino acids of the sequence

            if (amino_array.containsKey(string_array[i])) {
                sum1 = sum1 + (Double) amino_array.get(string_array[i]);
            }

        }


        String sub = sequence.substring(0, 3); //select the three first amino_acids (N-terminal position)
        char[] first_three = sub.toCharArray(); //create a array with the three first amino acids
        double r1 = 0; //retention coefficient special for first amino acid
        double r2 = 0;
        double r3 = 0;

        HashMap amino_new = new HashMap(); //hashmap with the retention coefficient for the N-terminal amino-acids
        double[] retention_coefficients_new = {-1.5, 8.0, 5.0, 9.0, 4.0, 5.0, 7.0, 1.0, 4.0, -8.0, -9.0, 4.6, -5.5, -7.0, 4.0, 5.0, 5.0, -4.0, -3.0, -5.5};

        for (int i = 0; i < AAs.length; i++) {

            amino_new.put(AAs[i], retention_coefficients_new[i]);

        }


        if (amino_new.containsKey(first_three[0])) {
            r1 = (Double) amino_new.get(first_three[0]);//select the right coefficient for the first amino-acid
        }
        if (amino_new.containsKey(first_three[1])) {
            r2 = (Double) amino_new.get(first_three[1]);//select the right coefficient for the second amino_acid
        }
        if (amino_new.containsKey(first_three[2])) {
            r3 = (Double) amino_new.get(first_three[2]); // select the right coefficient for the third amino_acid
        }

        double KL = 0; // correction coefficient related to the size of the peptide
        if (size < 10) { //if peptide short
            KL = 1 - (0.027 * (10 - size));
        } else if (size > 20) { //if peptide long
            KL = 1 - (0.014 * (size - 20));
        } else {
            KL = 1;
        }

        double hydrophobicity = 0; // first estimation of hydrophobicity
        hydrophobicity = sum1 + (0.5 * r1) + (0.3 * r2) + (0.1 * r3);

        double hydrophobicity1 = 0; //second estimation of hydrophobicity
        hydrophobicity1 = sum1 + (0.42 * r1) + (0.22 * r2) + (0.05 * r3);

        double hydrophobicity_final = 0; // final hydrophobicity
        if (hydrophobicity < 38) {
            hydrophobicity_final = KL * hydrophobicity1;
        } else {
            hydrophobicity_final = (KL * hydrophobicity1) - (0.3 * ((KL * hydrophobicity1) - 38));
        }

        // round the hydrophobicity with two decimals
        BigDecimal bd = new BigDecimal(hydrophobicity_final);
        BigDecimal bd2 = bd.setScale(2, BigDecimal.ROUND_DOWN);
        hydrophobicity_final = bd2.doubleValue();


        double retention_time = 0; //retention time estimated for this peptide
        retention_time = a + (b * hydrophobicity_final); // retention time computed with the parametres of the function a and b

        //round the retention time with one decimal
        BigDecimal bd_ret = new BigDecimal(retention_time);
        BigDecimal bd2_ret = bd_ret.setScale(2, BigDecimal.ROUND_DOWN);
        retention_time = bd2_ret.doubleValue();

        double[] answer = {retention_time, hydrophobicity_final}; //array with the retention_time and the hydrophobicity, returned by the method
        return answer;
    }

    /**
     * Method to calculate the molecular mass of a peptide
     * @param peptide the amino acid sequence of a peptide
     * @return the molecular mass of the peptide
     */
    public static double massPeptide(String peptide) {
        HashMap pept_mass = new HashMap();
        char[] pept_array;

        peptide = peptide.toUpperCase(); //transform every character into uppercase
        peptide = peptide.trim(); // delete white spaces before and after the sequence
        pept_array = peptide.toCharArray(); //transform the sequence into an array of amino acids

        char[] AAs = {'A', 'R', 'N', 'D', 'C', 'G', 'E', 'Q', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V'};
        double[] mono_mass = {71.03711, 156.10111, 114.04293, 115.02694, 103.00919, 57.02146, 129.04259, 128.05858, 137.05891, 113.08406, 113.08406, 128.09496, 131.04049, 147.06841, 97.05276, 87.03203, 101.04768, 186.07931, 163.06333, 99.06841};

        for (int i = 0; i < AAs.length; i++) { //create the hashmap by entering all the keys and values

            pept_mass.put(AAs[i], mono_mass[i]);

        }

        double mass_final = 0;

        // for each amino acid of the sequence, add its mass to the total mass of the peptide
        for (int i = 0; i < pept_array.length; i++) {
            if (pept_mass.containsKey(pept_array[i])) {
                mass_final = mass_final + (Double) pept_mass.get(pept_array[i]);
            }
        }

        mass_final = mass_final + 18.01056; // add the mass of water

        return mass_final;

    }

    /**
     * Method that counts the number of observable peptides inside a list of them.
     * This method use the retentionPredict method and the massPeptide method
     * @param peptides an array of peptides
     * @param mass_range mass range of the experiment
     * @param Rt_range retention time range of the experiment
     * @param a gradient delay time
     * @param b slope
     * @return the number of observable peptides
     */
    public static int number_observable(String[] peptides, double[] mass_range, double[] Rt_range, double a, double b) {

        int sum = 0;
        int nb=0;
        for (int i = 0; i < peptides.length; i++) { // for every peptide
            if (peptides[i].length() > 3) { // remove the one smaller than 4 AA, the prediction of retention time does not work on them
                nb++;
                 double retention_time = (retentionPredict(peptides[i], a, b)[0]);// predict the retention time of the peptide
 
                double mass = massPeptide(peptides[i]); // calculate the mass of the peptide

                if ((retention_time > Rt_range[0] && retention_time < Rt_range[1]) && (mass > mass_range[0] && mass < mass_range[1])) { // if the peptide fits in the mass and retention time ranges, it is observable
                    sum = sum + 1;

                }


            }
        }
        return sum;
    }



    /**
     * Method that simulates the trypsin digestion of a protein sequence. 
     * based on http://dogeno.us/code/peptidemap21.txt, Copyright (c) 2004 - Liang CAI - coronin@unc.edu
     * @param protein a protein sequence
     * @return an array of peptides obtained after digestion
     */
    public static String[] tryspin_digestion(String protein) {

        protein = protein.toUpperCase(); // put everything on uppercases 

        protein = protein.trim(); // remove white spaces before and after the sequence
        protein = protein.replaceAll(" ", ""); // remove white spaces inside the sequence

        String patt = "[0-9]"; // remove the numbers inside the sequence
        protein = protein.replaceAll(patt, "");

        // based on http://us.expasy.org/tools/peptidecutter/peptidecutter_enzymes.html#Tryps, add ":" to mark the trypsin digestion sites
        protein = protein.replaceAll("R", "R:");
        protein = protein.replaceAll("K", "K:");
        protein = protein.replaceAll("R:P", "RP");
        protein = protein.replaceAll("K:P", "KP");
        protein = protein.replaceAll("WKP", "WK:P");
        protein = protein.replaceAll("MRP", "MR:P");
        protein = protein.replaceAll("CK:D", "CKD");
        protein = protein.replaceAll("DK:D", "DKD");
        protein = protein.replaceAll("CK:H", "CKH");
        protein = protein.replaceAll("CK:Y", "CKY");
        protein = protein.replaceAll("CR:K", "CRK");
        protein = protein.replaceAll("R:R:H", "R:RH");
        protein = protein.replaceAll("RR:H", "RRH");
        protein = protein.replaceAll("R:R:R", "R:RR");
        protein = protein.replaceAll("RR:R", "RRR");
        protein = protein.replaceAll(":$", "");


        //create an array of peptides by cutting the sequence according to the ":"
        String[] peptides = protein.split(":");

        return peptides;


    }

    /**
     * Method that reads a fasta file and retrieve the sequence of each protein
     * specified in a list. The method use regular expressions to parse the fasta file
     * and set the sequence of each APEXProtein contained inside the list
     * @param fileName the path towards the fasta file
     * @param regexAccession the regular expression to retrieve the accession number in the header
     * @param regexDescription the regular expression to retrieve the description in the header
     * @param ListProtIdent the list of APEXProtein to retrieve the sequence
     */
    public static void FastaReader(String fileName, String regexAccession, String regexDescription, APEXProteinList ListProtIdent) {


        String line;
        String id = "";
        String desc = "";
        String seq = "";


        // boolean to know if we are reading the first protein of the file or not.
        // If we are, we cannot save the seq yet because it does not exist
        boolean firstProt = true;

        try {
            FileReader fr = new FileReader(new File(fileName));
            BufferedReader br = new BufferedReader(fr);




            while ((line = br.readLine()) != null) {

                // if there is an empty line, read the following one
                if (line.length() == 0) {
                    continue;
                }

                // create patterns to match the regular expressions
                Pattern pattern = Pattern.compile(regexAccession);
                Matcher matcher = pattern.matcher(line);

                Pattern pat = Pattern.compile(regexDescription);
                Matcher mat = pat.matcher(line);

                // if regex for accession matches
                if (matcher.find()) {

                    // if we are not reading the first protein, save the sequence
                    if (!firstProt) {
                        id = id.trim();
                        desc = desc.trim();
                        seq = seq.trim().toUpperCase();

                        if (ListProtIdent.containID(id)) {

                            APEXProtein protein=ListProtIdent.getProtein(id);
                            protein.setSequence(seq);

                        }
                    }

                    firstProt = false;
                    seq = "";

                    id = matcher.group(1);

                    

                } else {
                   
                    seq += line;
                    seq=seq.replaceAll("\\W", "");
                    seq=seq.replaceAll("\\d", "");
                }
            


            }

            // save the sequence of the first protein
            if (ListProtIdent.containID(id)) {
                id = id.trim();
                desc = desc.trim();
                seq = seq.trim().toUpperCase();

                // set the sequence of the corresponding protein
                APEXProtein protein=ListProtIdent.getProtein(id);
                            protein.setSequence(seq);
            }
         

        } catch (FileNotFoundException fnfe) {
            System.err.println("A fatal error occured reading the fasta file");
            System.err.println(fnfe.getMessage());
            System.err.println("Impossible to continue");
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println("A fatal error occured reading the fasta file");
            System.err.println(ioe.getMessage());
            System.err.println("Impossible to continue");
            System.exit(1);
        }


    }



    /**
     * method  that simulates trypsin digestion on all the proteins of a list,
     * and store the theoretical peptides in each protein
     * @param listOfProt the list of APEXProteins to generate the peptides
     */
    public static void cleave(APEXProteinList listOfProt){

        for(int i=0; i<listOfProt.size(); i++){

            APEXProtein protein=listOfProt.getProteinAt(i);

            protein.setListCleavedPeptides(tryspin_digestion(protein.getSequence()));
        }
    }

    /**
     * Method that calculates the APEX score for each protein of a list.
     * Sets the score in the protein once it is calculated
     * @param listOfProt the list of APEXProtein
     * @param C the normalisation factor
     */
    public static void computeAPEXScore(APEXProteinList listOfProt, double C){

        double totalNiPiOverOi=0.0;
        APEXProtein CurrentProt;

        // calculate the sum of ni*pi/Oi for all the proteins
        for(int i=0; i<listOfProt.size(); i++){

            CurrentProt=listOfProt.getProteinAt(i);

            if(CurrentProt.getPi() != -1){

                totalNiPiOverOi += (((double)(CurrentProt.getNi()))*CurrentProt.getPi())/CurrentProt.getOi();
            }
        }

        double apexScore;

        // calculate the APEX scores
        for(int j=0; j<listOfProt.size(); j++){
            CurrentProt=listOfProt.getProteinAt(j);

            if(CurrentProt.getPi() != -1){
                apexScore= (((double)(CurrentProt.getNi()))*CurrentProt.getPi())/CurrentProt.getOi();
                apexScore /= totalNiPiOverOi;
                CurrentProt.setRelAPEXScore(apexScore);
                CurrentProt.setNormalizedAPEXScore(apexScore*C);
            }
            else{
                CurrentProt.setRelAPEXScore(-1);
                CurrentProt.setNormalizedAPEXScore(-1);
            }
        }
    }

    /**
     * Method that calculates the false positive error rate of all the proteins in a list
     * @param listOfProt the list of APEXProteins
     */
    public static void calculateFPER(APEXProteinList listOfProt){

        double fper=-1.0;
        double sumError=0.0;
        APEXProtein CurrentProt;

        for(int i=0; i<listOfProt.size(); i++){
            CurrentProt=listOfProt.getProteinAt(i);

            if(CurrentProt.getPi() != -1){
                 sumError += (1.0 - CurrentProt.getPi());
                 fper=sumError/(double)(i+1.0d);
                 CurrentProt.setFper(fper);
            }
            else{
                CurrentProt.setFper(-1);
            }
        }

    }




}
