package xtracker.plugins.quantitation;

//=================================================================
// Plugin the compute the emPAI method on identification data.
// It reads xPeaks and counts the number of identified peptides per protein. 
// Then it read a fasta file to create a liste of the identified proteins with their sequence.
// Simulated trypsin digestion is made on the sequences, and for each peptide, its retention time and mass
// are estimated, which are used to decide if it can be seen in the experiment or not. The number of observable 
// peptides per protein is then used to compute the EMPAI.
// ==========================================================================
//
//  author: Laurie Tonon for X-Tracker, June 2009
import java.io.BufferedReader;
import xtracker.plugins.quantitation.misc.Protein;
import xtracker.plugins.quantitation.misc.methods;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xtracker.data.xPeaks;
import xtracker.data.xQuant;
import xtracker.data.xQuantData;
import xtracker.data.xQuantities;
import xtracker.data.xCorrespondenceData;
import xtracker.data.xCorrespondences;

public class quantEMPAI implements quantPlugin {

    /**
     * The start method.  
     * @params inputData is the xPeaks structure to work on.
     * @params paramFile a string (that might be empty) containing the file name of parameters if any.	
     * @returns A valid xQuant data structure.	
     */
    public xQuant start(xPeaks inputData, String paramFile) {
        xQuant ret = new xQuant();
        this.loadParams(paramFile); // load the parameters needed for the following


        boolean MissingProt = false; // boolean to know if a protein cannot be find in the fasta file

        try {
            // for each file in the xPeaks structure
            for (int i = 0; i < inputData.getSize(); i++) {

                HashMap ProtQuant = new HashMap(); // Hash map to store the protein id and the number of observed peptides
                xCorrespondences Correspondences = inputData.getElemAt(i); // get the data from one file

                // =============================================
                // count the number of peptides per protein
                // =============================================
                for (int j = 0; j < Correspondences.getPeptideCorrespondenceDataSize(); j++) {
                    // read all the correspondences
                    xCorrespondenceData CorrData = Correspondences.getPeptideCorrespondenceDataElemtAt(j);

                    int nbCorr = 0;

                    // count the number of peptides (in case several MSMS correspondences)
                    if (CorrData.getLcMsMsCorrSize() > 0) {
                        nbCorr += CorrData.getLcMsMsCorrSize();
                    }
                    if (CorrData.getLcMsCorrSize() > 0) {
                        nbCorr += CorrData.getLcMsCorrSize();
                    }

                    // use property of the hashmap to update the number of peptide each time
                    if (ProtQuant.containsKey(CorrData.getProteinId())) {
                        int nbOrigine = (Integer) ProtQuant.get(CorrData.getProteinId());
                        ProtQuant.put(CorrData.getProteinId(), (nbOrigine + nbCorr));
                    } else {
                        ProtQuant.put(CorrData.getProteinId(), nbCorr);
                    }
                }

                //take the list of identified protein
                ArrayList<String> ListProt = new ArrayList<String>();
                for (int s = 0; s < ProtQuant.keySet().toArray().length; s++) {
                    ListProt.add((String) ProtQuant.keySet().toArray()[s]);
                }

                ArrayList<Protein> ListSeq; // list of object Protein with sequences
                long start = System.nanoTime() / 1000000000;
//                ListSeq = methods.FastaReader(fastaFileName, regexAccession, regexDescription, ListProt);
                ListSeq = FastaReader(fastaFileName, regexAccession, regexDescription, ListProt);
                long end = System.nanoTime() / 1000000000;


                HashMap ProtQuantCalc = new HashMap(); // hashmap to store the index of abundance for each protein

                // =============================================================================
                // for each protein, calculate the number of observable peptides, and the EMPAI
                // =============================================================================

                for (int p = 0; p < ListSeq.size(); p++) {

                    String[] peptides = methods.tryspin_digestion(ListSeq.get(p).getSequence());

                    int nbobs = methods.number_observable(peptides, massRange, rTRange, gradientDelay, slope);

                    double empai = -1;

                    if (nbobs > 0) {

                        empai = modified_index((Integer) ProtQuant.get(ListSeq.get(p).getId()), nbobs);
                    }

                    ProtQuantCalc.put(ListSeq.get(p).getId(), empai);
                }




                // create the corresponding quantification data structure to store the information
                xQuantData quantData = new xQuantData(Correspondences.getFileName(), 1);

                //read again xpeaks to create quantities
                for (int j = 0; j < Correspondences.getPeptideCorrespondenceDataSize(); j++) {

                    xCorrespondenceData CorrDataNew = Correspondences.getPeptideCorrespondenceDataElemtAt(j);

                    xQuantities quantities = new xQuantities(CorrDataNew.getProteinId(), CorrDataNew.getPeptideSeq(), 1);

                    // add modif, quantity, error

                    //add modification from MsMsCorrespondence
                    if (CorrDataNew.getLcMsMsCorrSize() > 0) {
                        for (int c = 0; c < CorrDataNew.getLcMsMsCorrSize(); c++) {

                            if (CorrDataNew.getLcMsMsCorrElemAt(c).getModificationSize() > 0) {
                                for (int m = 0; m < CorrDataNew.getLcMsMsCorrElemAt(c).getModificationSize(); m++) {
                                    quantities.addModification(CorrDataNew.getLcMsMsCorrElemAt(c).getModificationNameAtIndex(m), CorrDataNew.getLcMsMsCorrElemAt(c).getModPositionAtIndex(m));
                                }
                            }
                        }
                    } //add modification from MsCorrespondence
                    else if (CorrDataNew.getLcMsCorrSize() > 0) {
                        for (int c = 0; c < CorrDataNew.getLcMsCorrSize(); c++) {

                            if (CorrDataNew.getLcMsCorrElemAt(c).getModificationSize() > 0) {

                                for (int m = 0; m < CorrDataNew.getLcMsCorrElemAt(c).getModificationSize(); m++) {
                                    quantities.addModification(CorrDataNew.getLcMsCorrElemAt(c).getModificationNameAtIndex(m), CorrDataNew.getLcMsCorrElemAt(c).getModPositionAtIndex(m));
                                }
                            }
                        }
                    }


                    // add error
                    quantities.addQuantError(0, -1);

                    //add quantity


                    Object index = ProtQuantCalc.get(quantities.getProteinId());


                    if (index == null) {

                        quantities.addQuantity(0, 0);
                        MissingProt = true;

                    } else {

                        quantities.addQuantity(0, Float.parseFloat(String.valueOf(index)));
                    }
                    // add the quantities to the quantitation data
                    quantData.addQuantitativeDataElem(quantities);
                }

                if (MissingProt) {
                    System.out.println("WARNING: some proteins are not in the fasta file!");
                    System.out.println("Their quantity is put to 0. You should fix your fasta file.");
                    MissingProt = false;
                }

                // add the quantitation data to xQuant
                ret.addQuantificationDataElem(quantData);



            }

        } catch (Exception e) {
            System.err.println("An error occured in calculated the emPAI.");
            System.err.println("Please check your input parameters");
            e.printStackTrace();
            System.exit(1);
        }


        return ret;
    }

    /**
     * Method to retrieve the name of the plugin.
     * @returns A string with the plugin name.	
     */
    public String getName() {
        return name;
    }

    /**
     * Method to retrieve the version of the plugin.
     * @returns A string with the plugin version.	
     */
    public String getVersion() {
        return version;
    }

    /**
     * Method to retrieve the type of the plugin.
     * @returns A string with the plugin type.	
     */
    public String getType() {

        return type;
    }

    /**
     * Method to retrieve the description of the plugin.
     * @returns A string with the plugin description.	
     */
    public String getDescription() {

        return description;
    }

    /**
     * Method to load the parameters from an xml file. Uses a dom parser to read the tags
     * @param dataFile the path to the xml file with the parameters
     */
    public void loadParams(String dataFile) {
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        int i = 0;

        try {

            //open and read the file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("SpecCount").item(0); // get the first tag

            String schemaLocation = "";

            if (nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null) {
                schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
            } else {
                if (nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation") != null) {
                    schemaLocation = nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                } else {
                    System.out.println("ERROR: No .xsd schema is provided for " + dataFile);
                    System.exit(1);
                }
            }

            // load the xtracker WXS schema
            Source schemaFile = new StreamSource(new File(schemaLocation));
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance
            Validator validator = schema.newValidator();

            try {
                validator.validate(new DOMSource(doc));
            } catch (SAXException e) {
                // instance document is invalid!
                System.out.println("\n\nERRROR - could not validate the input file " + dataFile + "!");
                System.out.print(e);
                System.exit(1);
            }


            NodeList itemList = nodeLst.getChildNodes();

            for (i = 0; i < itemList.getLength(); i++) { // read all the tags inside SpecCount

                Node item = itemList.item(i);

                //store the information in the correct variable
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("lowMass")) {
                        massRange[0] = Double.parseDouble(item.getTextContent());
                    } else if (item.getNodeName().equals("highMass")) {
                        massRange[1] = Double.parseDouble(item.getTextContent());
                    } else if (item.getNodeName().equals("lowRt")) {
                        rTRange[0] = Double.parseDouble(item.getTextContent());
                    } else if (item.getNodeName().equals("highRt")) {
                        rTRange[1] = Double.parseDouble(item.getTextContent());
                    } else if (item.getNodeName().equals("gradientDelayTime")) {
                        gradientDelay = Double.parseDouble(item.getTextContent());
                    } else if (item.getNodeName().equals("slope")) {
                        slope = Double.parseDouble(item.getTextContent());
                    } else if (item.getNodeName().equals("fasta")) {
                        fastaFileName = item.getTextContent();
                    } else if (item.getNodeName().equals("regexAccession")) {
                        regexAccession = item.getTextContent();
                    }

                }

            }

            // check that all fields are correct
            boolean error = false;
            String errorMessage = "Error in the parameter file :\n";

            if (massRange[0] == 0 && massRange[1] == 0) {
                error = true;
                errorMessage += "mass range incorrect\n";
            }
            if (rTRange[0] == 0 && rTRange[1] == 0) {
                error = true;
                errorMessage += "retention time range incorrect\n";
            }
            if (fastaFileName.equalsIgnoreCase("")) {
                error = true;
                errorMessage += "fasta file not not found \n";
            }

            if (regexAccession.equalsIgnoreCase("")) {
                error = true;
                errorMessage += " regex for accession not found \n";
            }

            if (error) {
                System.out.print(errorMessage);
                System.out.println("impossible to continue spectral counting.");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Exception while reading the parameter file" + dataFile + "\n" + e);
            System.err.println("Impossible to continue the quantifiation");
            System.exit(1);
        }


    }
    double[] massRange = new double[2]; // mass range of the experiment
    double[] rTRange = new double[2]; // retention time range of the experiment
    double gradientDelay = 0; // gradient delay time of the experiment
    double slope = 0; // slope of the experiment
    String fastaFileName = ""; // path to the fasta file with the sequence
    String regexAccession = ""; // regular expression to retrieve the accession number of a protein in the fasta file
    //This parameter is not used in this version
    String regexDescription = ""; // regular expression to retrieve the description of a protein in the fasta file
    /**
     * The name of your plugin.
     */
    private final static String name = "quantEMPAI";
    /**
     * The version of the plugin.
     */
    private final static String version = "1.0";
    /**
     * The plugin type. For a quantification plugin it must be QUANT_plugin (do not change it).
     */
//    private final static String type = "QUANT_plugin";
    /**
     * The description of the plugin.
     */
    private final static String description = "Plugin to compute spectral counting with the exponentially modified protein abundance index."
            + "For each identified protein, it counts the number of identified peptides. Then it reads a fasta file with"
            + " the sequence of every protein, and estimates the number of observable peptides for each protein."
            + "Finally, it calculates the protein abundance index for every protein and populate the xQuant structure";

    //TWO METHODS COPIED DIRECTLY FROM METHOD.JAVA
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
    public ArrayList<Protein> FastaReader(String fileName, String regexAccession, String regexDescription, ArrayList<String> ListProtIdent) {

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
        BigDecimal bd = new BigDecimal(emPAI);
        BigDecimal bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
        emPAI = bd2.doubleValue();

        return emPAI;
    }
}
