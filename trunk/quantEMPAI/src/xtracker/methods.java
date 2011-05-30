
package xtracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Class that contains the methods used in the plugin quantEMPAI to perform the emPAI quantification.
 *  Theses methods are either called by other methods in this file, or by the quantEMPAI.java class itself.
 * @author laurie Tonon for X-Tracker
 */
public class methods {

    public void methods() {
    }

    /**
     * Method that computes the retention time of a peptide, translation from the method in MRMaid,
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
        retention_time = a + (b * hydrophobicity_final); // retention time computed with the parameters of the function a and b

        //round the retention time with one decimal
        BigDecimal bd_ret = new BigDecimal(retention_time);
        BigDecimal bd2_ret = bd_ret.setScale(2, BigDecimal.ROUND_DOWN);
        retention_time = bd2_ret.doubleValue();

        double[] answer = {retention_time, hydrophobicity_final}; //array with the retention_time and the hydrophobicity, returned by the method
        return answer;
    }

    /**
     * method to calculate the molecular mass of a peptide
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
     * Method that counts the number of observable peptides inside a bunch of them.
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
     * Method that calculates the modified protein abundance index (emPAI) of a protein, accordinf to the paper:
     * Ishihama, Y., Oda, Y., Tabata, T., Sato, T., Nagasu, T., Rappsilber, J. and Mann, M. (2005),
     * "Exponentially Modified Protein Abundance Index (emPAI) for Estimation of Absolute Protein Amount in Proteomics
     * by the Number of Sequenced Peptides per Protein", Molecular and cellular proteomics,
     * vol. 4, pp. 1265-1272.
     * @param nb_observed number of observed peptides for this protein
     * @param nb_observable number of observable peptides for this protein
     * @return the emPAI value
     */
    public static double modified_index(int nb_observed, int nb_observable) {

        double PAI = (double) nb_observed / (double) nb_observable; // calculate the Protein Abundance Index
       
        double emPAI = Math.pow(10, PAI) - 1; // calculate the modified Protein Abundance Index

        //round the emPAI with three decimals
        BigDecimal bd = new BigDecimal(emPAI);
        BigDecimal bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
        emPAI = bd2.doubleValue();

        return emPAI;

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
     * Method that reads a fasta file and create a list of objects Protein,
     * containing the id, description and sequence of the identified proteins
     * contained in another list.
     * @param fileName the path to the file to read in.
     * @param regexAccession the regular expression to find the accession number of each protein.
     * @param regexDescription the regular expression to find the description of each protein.
     * @param ListProtIdent a list of proteins that we want to find the information in the fasta file.
     * @return a list of objects Protein, corresponding to the ones in the parameter list. Each Protein has an
     * identification number, a description and a sequence.
     */
    public static ArrayList<Protein> FastaReader(String fileName, String regexAccession, String regexDescription, ArrayList<String> ListProtIdent) {

        //list of proteins to return
        ArrayList<Protein> ListOfProt = new ArrayList<Protein>();

        String line;
        String id = "";
        String desc = "";
        String seq = "";


        // boolean to know if we are reading the first protein of the file or not.
        // If we are, we cannot save the id and seq yet because they do not exist
        boolean firstProt = true;

        try {
            FileReader fr = new FileReader(new File(fileName));
            BufferedReader br = new BufferedReader(fr);



            // read all the lines
            while ((line = br.readLine()) != null) {

                
                if (line.length() == 0) {
                    continue;
                }

                Pattern pattern = Pattern.compile(regexAccession);
                Matcher matcher = pattern.matcher(line);

                Pattern pat = Pattern.compile(regexDescription);
                Matcher mat = pat.matcher(line);

                // if the first regex match
                if (matcher.find()) {

                    if (!firstProt) { // this is not the first protein, we can save the info of the previous one
                        id = id.trim();
                        desc = desc.trim();
                        seq = seq.trim().toUpperCase();
                        if (ListProtIdent.contains(id)) {

                            Protein prot = new Protein(id, desc, seq);
                            ListOfProt.add(prot);
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

            // add the protein if it was identified
            if (ListProtIdent.contains(id)) {
                id = id.trim();
                desc = desc.trim();
                seq = seq.trim().toUpperCase();
                Protein prot = new Protein(id, desc, seq);
                ListOfProt.add(prot);
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


        return ListOfProt;
    }

//    /**
//     * Method to read a fasta file with the help of an index file. The method read first the index file,
//     * take the offsets of every identified protein, and then read their sequence in the fasta file,
//     * opening it only at the precise place.
//     * @param The complete path to the fasta file
//     * @param The complete path to the index file
//     * @param Regular expression to parse the fasta file and get the accession number of a protein
//     * @param Regular expression to parse the fasta file and get the description of a protein
//     * @param The list of all the identification number of the protein identified in the experiment
//     * @return The list of objects Protein, containing the id and sequence of each identified protein
//     */
//    @SuppressWarnings("empty-statement")
//    public static ArrayList<Protein> fastaReaderIndex(String FastaFileName, String IndexFileName, String regexAccession, String regexDescription, ArrayList<String> ListProtIdent) {
//
//        ArrayList<Protein> ListOfProt = new ArrayList<Protein>(); // list of Proteins to return
//
//        String line; // line read in the file
//        String id; // id number of a protein
//        String desc; // description of a protein
//        String seq; // sequence of a protein
//        HashMap map = new HashMap(); // hashMap to store the proteins and their offsets
//        String regexIndex = "(.+),(.+)"; // regular expression to parse the index file and differenciate the id and the offset
//
//        try {
//
//            //Read the index file to get the offset
//            FileReader fr = new FileReader(new File(IndexFileName));
//            BufferedReader br = new BufferedReader(fr);
//            br.readLine(); //skip the first line, it is the header
//
//            // read all the other lines (id and offset) and store the information in the hashMap
//            while ((line = br.readLine()) != null) {
//
//                //use the regex to parse every line
//                Pattern pattern = Pattern.compile(regexIndex);
//                Matcher matcher = pattern.matcher(line);
//
//                if (matcher.find()) {
//
//                    String access = matcher.group(1);//get the id number
//                    // System.out.println(access);
//                    if (ListProtIdent.contains(access)) { // if the id number is one of the identified ones
//                        map.put(access, matcher.group(2));
//                    }
//                }
//            }
//
//            br.close(); //close the index file
////            System.out.println("prot1= "+ListProtIdent.size());
////            System.out.println("prot2= "+map.keySet().size());
////            for(int t=0;t<ListProtIdent.size();t++){
////                System.out.println(ListProtIdent.get(t));
////            }
////            System.out.println("==============");
////            for(int t=0;t<map.keySet().toArray().length;t++){
////                System.out.println(map.keySet().toArray()[t]);
////            }
//            //read the fasta file at every offset
//            File fasta = new File(FastaFileName);
//            FileReader frFasta;
//            BufferedReader brFasta;
//
//            Pattern pattern = Pattern.compile(regexAccession);
//            ;
//            Matcher matcher;
//            Pattern pat = Pattern.compile(regexDescription);
//            Matcher mat;
//
//            //array with the id of all identified proteins
//            String[] protToRead = (String[]) map.keySet().toArray(new String[0]);
//
//            // for each protein and its offset
//            for (int x = 0; x < protToRead.length; x++) {
//                seq = "";
//                id = "";
//                desc = "";
//                frFasta = new FileReader(fasta);
//                brFasta = new BufferedReader(frFasta);
//
//                // open the file directly at the offset of the id
//                brFasta.skip(Long.parseLong((String) map.get(protToRead[x])));
//                String header = brFasta.readLine();// read the complete header line
//
//                // use the regexs to get the id and description
//                matcher = pattern.matcher(header);
//                mat = pat.matcher(header);
//
//                if (matcher.find()) {
//                    id = matcher.group(1).trim(); // substring to remove the >
//
//                    if (mat.find()) {
//                        desc = mat.group(1);
//                    }
//                }
//
//                String lineSeq = "";
//
//                // read the following lines to get the sequence, until we reach another line with a >
//                while ((lineSeq = brFasta.readLine()) != null && !(lineSeq.contains(">"))) {
//                    seq += lineSeq;
//                }
//
//
//                //remove odd characters from the sequence
//                seq = seq.replaceAll("\\W", "");
//                seq = seq.replaceAll("\\d", "");
//
////                 System.out.println("id= "+id);
////                 System.out.println("desc= "+desc);
////                 System.out.println("seq= "+seq);
//
//                Protein p = new Protein(id, desc, seq); //create the object Protein
//                ListOfProt.add(p); // add the protein to the list to return
//
//                //close the fasta file to open it again at the other offset
//                brFasta.close();
//                frFasta.close();
//            }
//
//        } catch (FileNotFoundException fnfe) {
//            System.err.println("A fatal error occured reading the fasta file");
//            System.err.println(fnfe.getMessage());
//            System.err.println("Impossible to continue");
//        } catch (IOException ioe) {
//            System.err.println("A fatal error occured reading the fasta file");
//            System.err.println(ioe.getMessage());
//            System.err.println("Impossible to continue");
//        }
//
//        return ListOfProt;
//
//    }





}
