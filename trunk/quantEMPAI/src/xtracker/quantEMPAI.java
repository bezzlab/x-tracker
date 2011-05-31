package xtracker;

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


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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


        boolean MissingProt=false; // boolean to know if a protein cannot be find in the fasta file

        try{
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
            ArrayList<String> ListProt =new ArrayList<String>(); 
            
            for(int s=0; s<ProtQuant.keySet().toArray().length;s++){
                ListProt.add((String)ProtQuant.keySet().toArray()[s]);
            }

            
            ArrayList<Protein> ListSeq; // list of object Protein with sequences


                long start=System.nanoTime()/1000000000;
                ListSeq = methods.FastaReader(fastaFileName, regexAccession, regexDescription, ListProt);
                long end=System.nanoTime()/1000000000;


            HashMap ProtQuantCalc = new HashMap(); // hashmap to store the index of abundance for each protein

            // =============================================================================
            // for each protein, calculate the number of observable peptides, and the EMPAI
            // =============================================================================

            for (int p = 0; p < ListSeq.size(); p++) {

                String[] peptides = methods.tryspin_digestion(ListSeq.get(p).getSequence());

                int nbobs = methods.number_observable(peptides, massRange, rTRange, gradientDelay, slope);

                double empai=-1;

                if(nbobs >0){

                    empai=methods.modified_index((Integer)ProtQuant.get(ListSeq.get(p).getId()), nbobs);
                }

                ProtQuantCalc.put(ListSeq.get(p).getId(), empai);
            }


           
            
            // create the corresponding quantification data structure to store the information
            xQuantData quantData=new xQuantData(Correspondences.getFileName(),1);
            
            //read again xpeaks to create quantities
            for (int j = 0; j < Correspondences.getPeptideCorrespondenceDataSize(); j++) {
                
                xCorrespondenceData CorrDataNew = Correspondences.getPeptideCorrespondenceDataElemtAt(j);
                
                xQuantities quantities=new xQuantities(CorrDataNew.getProteinId(),CorrDataNew.getPeptideSeq(),1);
                
                // add modif, quantity, error
                
                //add modification from MsMsCorrespondence
                if(CorrDataNew.getLcMsMsCorrSize()>0){
                    for (int c = 0; c < CorrDataNew.getLcMsMsCorrSize(); c++) {
                        
                        if (CorrDataNew.getLcMsMsCorrElemAt(c).getModificationSize() > 0) {
                            for (int m = 0; m < CorrDataNew.getLcMsMsCorrElemAt(c).getModificationSize(); m++) {
                                quantities.addModification(CorrDataNew.getLcMsMsCorrElemAt(c).getModificationNameAtIndex(m), CorrDataNew.getLcMsMsCorrElemAt(c).getModPositionAtIndex(m));
                            }
                        }
                    }
                }
                //add modification from MsCorrespondence
                else if(CorrDataNew.getLcMsCorrSize()>0){
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

                
                Object index=ProtQuantCalc.get(quantities.getProteinId());


                if(index == null){
                    
                    quantities.addQuantity(0, 0);
                    MissingProt=true;
                    
                }
                else{
                    
                    quantities.addQuantity(0, Float.parseFloat(String.valueOf(index)));
                }
                // add the quantities to the quantitation data
                quantData.addQuantitativeDataElem(quantities);
            }

            if(MissingProt){
                System.out.println("WARNING: some proteins are not in the fasta file!");
                System.out.println("Their quantity is put to 0. You should fix your fasta file.");
                MissingProt=false;
            }
            
            // add the quantitation data to xQuant
            ret.addQuantificationDataElem(quantData);
            
           

        }

}
        catch(Exception e){
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
        int i=0;
        
        try {

            //open and read the file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("SpecCount").item(0); // get the first tag
            
            NodeList itemList=  nodeLst.getChildNodes();
            
            for(i=0; i<itemList.getLength(); i++){ // read all the tags inside SpecCount
                
                Node item= itemList.item(i);
                
                //store the information in the correct variable
                if(item.getNodeType() == Node.ELEMENT_NODE){
                    if(item.getNodeName().equals("lowMass")){
                        massRange[0]=Double.parseDouble(item.getTextContent());   
                    }
                    else if(item.getNodeName().equals("highMass")){
                        massRange[1]=Double.parseDouble(item.getTextContent());
                    }
                    else if(item.getNodeName().equals("lowRt")){
                        rTRange[0]=Double.parseDouble(item.getTextContent());
                    }
                    else if(item.getNodeName().equals("highRt")){
                        rTRange[1]=Double.parseDouble(item.getTextContent());
                    }
                    else if(item.getNodeName().equals("gradientDelayTime")){
                        gradientDelay=Double.parseDouble(item.getTextContent());
                    }
                    else if(item.getNodeName().equals("slope")){
                        slope=Double.parseDouble(item.getTextContent());
                    }
                    else if(item.getNodeName().equals("fasta")){
                        fastaFileName=item.getTextContent();
                    }

                    else if(item.getNodeName().equals("regexAccession")){
                        regexAccession=item.getTextContent();
                    }

                }
                
            }
            
            // check that all fields are correct
            boolean error=false;
            String errorMessage="Error in the parameter file :\n";
            
            if(massRange[0]==0 && massRange[1]==0 ){
                error=true;
                errorMessage+="mass range incorrect\n";
            }
            if(rTRange[0]==0 && rTRange[1]==0){
                error=true;
                errorMessage+= "retention time range incorrect\n";
            }
            if(fastaFileName.equalsIgnoreCase("")){
                error=true;
                errorMessage += "fasta file not not found \n";
            }

            if(regexAccession.equalsIgnoreCase("")){
                error=true;
                errorMessage += " regex for accession not found \n";
            }
            
            if(error){
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
    
    double[] massRange=new double[2]; // mass range of the experiment
    double[] rTRange=new double[2]; // retention time range of the experiment
    double gradientDelay=0; // gradient delay time of the experiment
    double slope=0; // slope of the experiment
    String fastaFileName=""; // path to the fasta file with the sequence
    String regexAccession=""; // regular expression to retrieve the accession number of a protein in the fasta file

    //This parameter is not used in this version
    String regexDescription=""; // regular expression to retrieve the description of a protein in the fasta file
    
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
    private final static String type = "QUANT_plugin";
    /**
     * The description of the plugin.
     */
    private final static String description = "Plugin to compute spectral counting with the exponentially modified protein abundance index."+
                                                "For each identified protein, it counts the number of identified peptides. Then it reads a fasta file with"+
                                                " the sequence of every protein, and estimates the number of observable peptides for each protein."+
                                                "Finally, it calculates the protein abundance index for every protein and populate the xQuant structure";
}
