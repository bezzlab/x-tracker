package xtracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Plugin to perform the APEX quantification method in X-tracker
 * The plugin reads the xPeaks structure, calls the methods in the corresponding class
 * and load the results in the xQuant structure.
 * @author laurie tonon for X-Tracker
 */




public class quantAPEXbasic implements quantPlugin
{
    
	
     /**
      * The start method.
      * @params inputData the xPeaks structure to work on.
      * @params paramFile a string containing the file name of parameters.	
      * @returns A valid xQuant data structure.	
      */	
    public xQuant start(xPeaks inputData, String paramFile)
    {
        /**
         * xQuant structure to return
         */
        xQuant ret=new xQuant();

        // load the parameters from the xml file
        loadParams(paramFile);

        // read infos from all the files in xPeaks
        for (int i = 0; i < inputData.getSize(); i++) {


            xCorrespondences Correspondences = inputData.getElemAt(i); // get the data from one file

            // create an xQuantData structure to store the results for this file
            xQuantData quantData=new xQuantData(Correspondences.getFileName(),1);

            APEXProteinList list=new APEXProteinList();

            // read all the correspondences to create a list of APEXProteins,
            // containing all the identified proteins.
            for (int j = 0; j < Correspondences.getPeptideCorrespondenceDataSize(); j++) {

                 xCorrespondenceData CorrData = Correspondences.getPeptideCorrespondenceDataElemtAt(j);

                 APEXProtein protein=new APEXProtein(CorrData.getProteinId());


                     for(int ms=0; ms<CorrData.getLcMsCorrSize(); ms++){

                       String[] modifs=new String[CorrData.getLcMsCorrElemAt(ms).getModificationSize()];
                      int[] modpos=new int[CorrData.getLcMsCorrElemAt(ms).getModificationSize()];

                        for(int m=0;m<CorrData.getLcMsCorrElemAt(ms).getModificationSize();m++){
                            modifs[m]=CorrData.getLcMsCorrElemAt(ms).getModificationNameAtIndex(m);
                            modpos[m]=CorrData.getLcMsCorrElemAt(ms).getModPositionAtIndex(m);
                        }
                         Peptide peptide=new Peptide(CorrData.getPeptideSeq(),modifs,modpos);

                         protein.addPeptide(peptide);

                     }

                     for(int msms=0;msms<CorrData.getLcMsMsCorrSize();msms++){

                      String[] modifs=new String[CorrData.getLcMsMsCorrElemAt(msms).getModificationSize()];
                      int[] modpos=new int[CorrData.getLcMsMsCorrElemAt(msms).getModificationSize()];

                        for(int m=0;m<CorrData.getLcMsMsCorrElemAt(msms).getModificationSize();m++){
                            
                            modifs[m]=CorrData.getLcMsMsCorrElemAt(msms).getModificationNameAtIndex(m);
                            modpos[m]=CorrData.getLcMsMsCorrElemAt(msms).getModPositionAtIndex(m);
                        }

                        Peptide peptide=new Peptide(CorrData.getPeptideSeq(),modifs,modpos);

                        protein.addPeptide(peptide);
         

                     
                 }

                 list.addProtein(protein);
            }


            // reads the ProteinProphet file

            APEXProteinProphetXMLParser parser=new APEXProteinProphetXMLParser();
            HashMap listOfPi=new HashMap(); // HashMap with the protein ids and their Oi values

            try{

                listOfPi=parser.readProtXML(ProphetFile.get(i));


            }
            catch(IOException ex){
                System.out.println("A fatal error occured reading the file "+ProphetFile.get(i));
                System.out.println(ex.getMessage());
                System.out.println("impossible to continue");
                System.exit(1);
            }
            catch(SAXException e){
                System.out.println("A fatal error occured reading the file "+ProphetFile.get(i));
                System.out.println(e.getMessage());
                System.out.println("impossible to continue");
                System.exit(1);

            }

            // retrieves the probability for each identified protein
            for(int p=0; p<list.size(); p++){

                APEXProtein protein=list.getProteinAt(p);

                if(listOfPi.containsKey(protein.getId())){

                    protein.setPi((Double)listOfPi.get(protein.getId()));


                }
                else{
                    System.out.println("one protein without pi equivalent");
                    System.out.println(protein.getId());
                    protein.setPi(-1);
                   
                }
            }

            // Reads the fasta file to get the sequence of each identified protein

                methods.FastaReader(fastaFileName, regexAccession, regexDescription, list);


            // Performs trypsin digestion on the sequence of each identified protein
            // Store the list of tryptic peptides
            methods.cleave(list);

            // Calculates number of observable peptides for each protein
            for(int p=0 ; p<list.size(); p++){

                APEXProtein protein=list.getProteinAt(p);
 
                protein.setOi(methods.number_observable(protein.getListCleavedPeptides(), massRange,rTRange ,gradientDelay, slope));
              
                
            }

            // Computes APEX score for each proteins
            methods.computeAPEXScore(list, NormFactor);

            // Calculates the false positive error rate for each protein
            methods.calculateFPER(list);

            // Read the list of APEXProteins and store the results of quantification in xQuant
            for(int prot=0; prot< list.size(); prot++){
                APEXProtein CurrentProt=list.getProteinAt(prot);

                for(int pept=0; pept<CurrentProt.getNi(); pept++){
                    Peptide CurrentPept=CurrentProt.getPeptideAt(pept);

                    xQuantities quantity= new xQuantities(CurrentProt.getId(), CurrentPept.getSequence(),1);

                    for(int mod=0 ;mod<CurrentPept.getModifs().size(); mod++){

                        quantity.addModification(CurrentPept.getModificationAtIndex(mod), CurrentPept.getModPositionAtIndex(mod));
                    }

                    quantity.addQuantity(0, (float)CurrentProt.getNormalizedAPEXScore(), (float)CurrentProt.getFper());

                    quantData.addQuantitativeDataElem(quantity);
                }


            }

            ret.addQuantificationDataElem(quantData);


        }


                

        return ret;
    }

   


     /**
      * Method to retrieve the name of the plugin.
      * @returns A string with the plugin name.	
      */
    public String getName()
    {
        return name;
    }

     /**
      * Method to retrieve the version of the plugin.
      * @returns A string with the plugin version.	
      */
    public String getVersion()
    {
        return version;
    }

     /**
      * Method to retrieve the type of the plugin.
      * @returns A string with the plugin type.	
      */	
    public String getType()
    {

        return type;
    }

     /**
      * Method to retrieve the description of the plugin.
      * @returns A string with the plugin description.	
      */
        public String getDescription()
    {

        return description;
    }


        /**
         * Method to load the parameters from an xml file
         * @param dataFile The path to the file to read in
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

           

            Node nodeLst = doc.getElementsByTagName("APEX").item(0); // get the first tag

            NodeList itemList=  nodeLst.getChildNodes();



            for(i=0; i<itemList.getLength(); i++){ // read all the tags inside APEX

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

                    else if(item.getNodeName().equals("Normalisation")){
                        NormFactor=Double.parseDouble(item.getTextContent());
                    }
                    else if(item.getNodeName().equals("ProteinProphet")){

                        NodeList fileList=  item.getChildNodes();
                        System.out.println(fileList.getLength());

                        for(int j=0; j<fileList.getLength(); j++){

                            Node fileItem= fileList.item(j);
                            if(fileItem.getNodeType() == Node.ELEMENT_NODE){

                            
                                if(fileItem.getNodeName().equals("file")){
                                    System.out.println(fileItem.getTextContent());

                                    ProphetFile.add(fileItem.getTextContent());
                                }
                            }
                        }
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
            if(ProphetFile.size() ==0){
                error=true;
                errorMessage += "ProteinProphet files not found \n";
            }
            if(NormFactor == 0){
                error=true;
                errorMessage += "Normalisation factor incorrect \n";
            }

            if(error){
                System.out.print(errorMessage);
                System.out.println("impossible to continue spectral counting.");
                System.exit(1);
            }

        }
        catch (Exception e) {
            System.err.println("Exception while reading the parameter file" + dataFile + "\n" + e);
            System.err.println("Impossible to continue the quantifiation!!");
            System.exit(1);
        }

      }


    double[] massRange=new double[2]; // mass range of the experiment
    double[] rTRange=new double[2]; // retention time range of the experiment
    double gradientDelay=0; // gradient delay time of the experiment
    double slope=0; // slope of the experiment
    String fastaFileName=""; // path to the fasta file with the sequence
    String regexAccession=""; // regular expression to retrieve the accession number of a protein in the fasta file
    // this parameter is not used in this version
    String regexDescription=""; // regular expression to retrieve the description of a protein in the fasta file
    ArrayList<String> ProphetFile=new ArrayList<String>(); // path to the ProteinProphet file
    double NormFactor=0; // normalisation factor



     /**
      * The name of your plugin.
      */ 
    private final static String name = "quantAPEXbasic";

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
    private final static String description = "Plugin to perform APEX quantification using a basic estimation of the" +
            " number of observable peptides by estimating their mass and retention time and see if they fit in the ranges" +
            " of the experiment";
}
