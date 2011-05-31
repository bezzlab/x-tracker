package xtracker;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Plugin to export the results of the APEX quantification in an Excel file with two sheets:
 * One with the quantification results of each identified protein, and the other with the list of peptide
 * and their modifications.
 * Uses the JExcel API.
 * @author laurie Tonon for X-tracker
 */

public class OutputAPEXExcel implements outPlugin {

    /**
     * The start method. Creates and fills the Excel workbook
     * @param inputData is the xQuant structure to work on.
     * @param paramFile a string containing the file name of parameters .
     * No need to return anything.
     */
    public void start(xQuant InputData, String paramFile) {

        try {

            this.loadParams(paramFile);
            // *************************************************
            // create a new Excel workbook with 2 sheets
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
            WritableSheet sheet = workbook.createSheet("quantities", 0);
            WritableSheet sheet2 = workbook.createSheet("peptides", 1);

            // format and create of the main title
            WritableFont font = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD, false);
            WritableCellFormat formatH = new WritableCellFormat(font);
            formatH.setAlignment(Alignment.CENTRE);
            formatH.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.CORAL);
            Label header = new Label(2, 1, "Quantification results from X-tracker using the APEX method");
            header.setCellFormat(formatH);
            sheet.addCell(header);
            sheet.mergeCells(2, 1, 7, 1);

            // ********************************************************

            if (InputData.getQuantificationDataSize() > 0) {

                // Array list of protein ids to store the proteins already written in
                // the excel sheet, in order to put on the same line the quantities of
                // the same protein in different datasets
                ArrayList<String> protWritten = new ArrayList<String>();


                //*****************************************************
                // Write the headers of the 2 sheets
                WritableFont boldfont = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false);
                WritableCellFormat formatHeader = new WritableCellFormat(boldfont);
                formatHeader.setAlignment(Alignment.CENTRE);
                formatHeader.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.DARK_BLUE);

                Label labelProt = new Label(1, 6, "ProtID");
                labelProt.setCellFormat(formatHeader);
                sheet.addCell(labelProt);


                Label labelProt2 = new Label(1, 6, "ProtID");
                labelProt2.setCellFormat(formatHeader);
                sheet2.addCell(labelProt2);
                Label labelPept = new Label(2, 6, "pept sequence");
                labelPept.setCellFormat(formatHeader);
                sheet2.addCell(labelPept);
                sheet2.setColumnView(2, 30);

                Label labelMod = new Label(3, 6, "modifications");
                labelMod.setCellFormat(formatHeader);
                sheet2.addCell(labelMod);
                sheet2.setColumnView(3, 30);


                WritableCellFormat formatProt = new WritableCellFormat();
                formatProt.setAlignment(Alignment.CENTRE);
                
                //index to put the peptides in the second sheet in one single column
                int nbPept = 6;
                //***************************************************************

                int r = 0;


                       

                // read all the quantitation
                for (int q = 0; q < InputData.getQuantificationDataSize(); q++) {
                    xQuantData quantData = InputData.getElementAtIndex(q);

                    if (quantData.getQuantitativeDataSize() > 0) {

                        nbPept++; // put the first peptide on a new line

                        double sumEMPAI = 0;
                        String firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();

                        //***********************************************************
                        // add the first peptide in the second sheet
                        Label prot = new Label(1, nbPept, quantData.getQuantitativeDataElemAt(0).getProteinId());
                        prot.setCellFormat(formatProt);
                        sheet2.addCell(prot);
                        Label peptSeq = new Label(2, nbPept, quantData.getQuantitativeDataElemAt(0).getPeptideSeq());
                        sheet2.addCell(peptSeq);

                        String modifs = "";
                        for (int m = 0; m < quantData.getQuantitativeDataElemAt(0).modifications.size(); m++) {
                            modifs += quantData.getQuantitativeDataElemAt(0).getModificationAtIndex(m) + " (" + quantData.getQuantitativeDataElemAt(0).getModPositionAtIndex(m) + "); ";
                        }

                        Label mod = new Label(3, nbPept, modifs);
                        sheet2.addCell(mod);
                        //*************************************************************

                        // calculate the sum of emPAI for the percentage calculation

                        for (int i = 1; i < quantData.getQuantitativeDataSize(); i++) {
                            nbPept++;
                            if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {
                              
                                firstProt = quantData.getQuantitativeDataElemAt(i).getProteinId();
                            }

                            // ***************************************************
                            // add the list of all the peptides in the second sheet
                            prot = new Label(1, nbPept, quantData.getQuantitativeDataElemAt(i).getProteinId());
                            prot.setCellFormat(formatProt);
                            sheet2.addCell(prot);
                            peptSeq = new Label(2, nbPept, quantData.getQuantitativeDataElemAt(i).getPeptideSeq());
                            sheet2.addCell(peptSeq);

                            modifs = "";
                            for (int m = 0; m < quantData.getQuantitativeDataElemAt(i).modifications.size(); m++) {
                                modifs += quantData.getQuantitativeDataElemAt(i).getModificationAtIndex(m) + " (" + quantData.getQuantitativeDataElemAt(i).getModPositionAtIndex(m) + "); ";
                            }
                            mod = new Label(3, nbPept, modifs);
                            sheet2.addCell(mod);
                            //**********************************************************
                        }

                        //============================================================
                        // set the header of the proteins table
                        //============================================================

                        int index=quantData.getFileName().lastIndexOf('/');
                        Label LabelDataSet = new Label(r + 2, 5, quantData.getFileName().substring(index+1));

                        WritableCellFormat format = new WritableCellFormat();

                        format.setAlignment(Alignment.CENTRE);
                        format.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.DARK_BLUE);

                        LabelDataSet.setCellFormat(format);
                        sheet.addCell(LabelDataSet);
                        sheet.setColumnView(r + 2, quantData.getFileName().length());
                        sheet.mergeCells(r + 2, 5, r + 3, 5);

                        WritableCellFormat formatNoColour=new WritableCellFormat();
                        formatNoColour.setAlignment(Alignment.CENTRE);

                        Label Labelempai = new Label(r + 2, 6, "APEX Score abs");
                        Labelempai.setCellFormat(formatNoColour);
                        sheet.addCell(Labelempai);

                        Label LabelPer = new Label(r + 3, 6, "False positive error");
                        LabelPer.setCellFormat(formatNoColour);
                        sheet.addCell(LabelPer);
                        sheet.setColumnView(r+3, 15);
                        //==============================================================

          //===================================================================================
                        // deal with the first couple(prot/pept)



                        //***************************************************
                        // TAKE THE QUANTITIES INFO FROM XQUANT
                        //****************************************************

                        // take the first protein as a reference to compare the others and select the quantitation by protein
                        firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();
                        xQuantities quantity = quantData.getQuantitativeDataElemAt(0);
                        double APEX = quantity.getQuantityAt(0);

                        BigDecimal bd = new BigDecimal(APEX);
                        BigDecimal bd2 = bd.setScale(5, BigDecimal.ROUND_DOWN);
                        APEX = bd2.doubleValue();

                        //calculate the percentage of emPAI
                        double err = quantity.getQuantErrorAt(0);

                        BigDecimal bdp = new BigDecimal(err);
                        BigDecimal bd2p = bdp.setScale(5, BigDecimal.ROUND_DOWN);
                        err = bd2p.doubleValue();
                        //***************************************************

                        //***********************************************************
                        // WRITE THE QUANTITIES INFO IN THE FIRST SHEET
                        //**********************************************************
                        Label label;
                        jxl.write.Number number;
                        jxl.write.Number percentage;



                        // if the protein was already in a precedent dataset and so already written
                        if (protWritten.contains(firstProt)) {
                            // add the new quantites info on the line corresponding to the protein
                            if(APEX != -1){
                                number = new jxl.write.Number(r + 2, 7 + protWritten.indexOf(firstProt), APEX);
                                sheet.addCell(number);
                            }
                            else{
                                Label MissingProt=new Label(r + 2 , 7+protWritten.indexOf(firstProt),"Nan");
                                sheet.addCell(MissingProt);
                            }

                            if(err != -1){
                                 percentage = new jxl.write.Number(r + 3, 7 + protWritten.indexOf(firstProt),err);
                                 sheet.addCell(percentage);
                            }
                            else{
                                Label MissingProt=new Label(r + 3 , 7+protWritten.indexOf(firstProt),"Nan");
                                sheet.addCell(MissingProt);
                            }



                        } else {
                            //add the new quantities in a new line

                            label = new Label(1, 7 + protWritten.size(), firstProt);
                            label.setCellFormat(formatProt);
                            sheet.addCell(label);

                            if(APEX != -1){
                                number = new jxl.write.Number(r + 2, 7 + protWritten.size(), APEX);
                                sheet.addCell(number);
                            }
                            else{
                                Label MissingProt=new Label(r + 2, 7 + protWritten.size(),"Nan");
                                sheet.addCell(MissingProt);
                            }

                            if(err != -1){
                                percentage = new jxl.write.Number(r + 3, 7 + protWritten.size(), err);
                                sheet.addCell(percentage);
                            }
                            else{
                                Label MissingProt=new Label(r + 3, 7 + protWritten.size(),"Nan");
                                sheet.addCell(MissingProt);
                            }

                            protWritten.add(firstProt); // specify that this new protein was written
                        }
         //==========================================================================================
                        // now deal with all the other couples (prot/pept)

                        int nb = 7; // to specify the lines start from 7


                        for (int i = 0; i < quantData.getQuantitativeDataSize(); i++) {

                         
                            // if the next protein is different from the reference one, add it
                            if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {

                                
                                //***************************************************
                                // TAKE THE QUANTITIES INFO FROM XQUANT
                                //****************************************************
                                quantity = quantData.getQuantitativeDataElemAt(i);
                                APEX = quantity.getQuantityAt(0);

                                bd = new BigDecimal(APEX);
                                bd2 = bd.setScale(5, BigDecimal.ROUND_DOWN);
                                APEX = bd2.doubleValue();

                                err = quantity.getQuantErrorAt(0);

                                bdp = new BigDecimal(err);
                                bd2p = bdp.setScale(5, BigDecimal.ROUND_DOWN);
                                err = bd2p.doubleValue();
                                //=================================================

                                //***********************************************************
                                // WRITE THE QUANTITIES INFO IN THE FIRST SHEET
                                //**********************************************************
                                Label labelN;
                                jxl.write.Number numberN;
                                jxl.write.Number percentageN;


                                // if the protein was already in a precedent dataset and so already written
                                if (protWritten.contains(quantity.getProteinId())) {

                                    // add the new quantites info on the line corresponding to the protein

                                    if(APEX != -1){
                                        numberN = new jxl.write.Number(r + 2, nb + protWritten.indexOf(quantity.getProteinId()), APEX);
                                        sheet.addCell(numberN);
                                    }
                                    else{
                                        Label MissingProt=new Label(r + 2, nb + protWritten.indexOf(quantity.getProteinId()),"Nan");
                                        sheet.addCell(MissingProt);
                                    }

                                    if(err != -1){
                                        percentageN = new jxl.write.Number(r + 3, nb + protWritten.indexOf(quantity.getProteinId()), err);
                                        sheet.addCell(percentageN);
                                    }
                                    else{
                                        Label MissingProt=new Label(r + 3, nb + protWritten.indexOf(quantity.getProteinId()),"Nan");
                                        sheet.addCell(MissingProt);
                                    }
                                } else {
                                    //add the new quantities in a new line

                                    labelN = new Label(1, nb + protWritten.size(), quantity.getProteinId());
                                    labelN.setCellFormat(formatProt);
                                    sheet.addCell(labelN);

                                    if(APEX != -1){
                                        numberN = new jxl.write.Number(r + 2, nb + protWritten.size(), APEX);
                                        sheet.addCell(numberN);
                                    }
                                    else{
                                        Label MissingProt=new Label(r + 2, nb + protWritten.size(),"Nan");
                                        sheet.addCell(MissingProt);
                                    }

                                    if(err != -1){
                                        percentageN = new jxl.write.Number(r + 3, nb + protWritten.size(), err);
                                        sheet.addCell(percentageN);
                                    }
                                    else{
                                        Label MissingProt=new Label(r + 3, nb + protWritten.size(),"Nan");
                                        sheet.addCell(MissingProt);
                                    }
                                    protWritten.add(quantity.getProteinId()); // specify that this new protein was written
                                }


                                // change the reference protein
                                firstProt = quantData.getQuantitativeDataElemAt(i).getProteinId();

                            }

                            

                        }


                        // *************************************************************************************
                    }
                    // move of 2 columns for the next dataset
                    r += 2;


                }

            }

            
            //========================================================
            // COLOR EACH LINE OF THE TABLE IN A DIFFERENT COLOUR
            // =======================================================

                        int c = 0;

                        for(int row=7; row<sheet.getRows(); row++){
                            WritableCellFormat formatLine = new WritableCellFormat();
                            formatLine.setAlignment(Alignment.CENTRE);

                            if(c==0){
                               formatLine.setBackground(Colour.GREY_25_PERCENT);
                            }

                            
                            for(int column=1; column<sheet.getColumns(); column++){

                                WritableCell cel= sheet.getWritableCell(column, row);
                                if(cel.getContents().equalsIgnoreCase("")){
                                    Label labelEmpty=new Label(column, row,"");
                                    labelEmpty.setCellFormat(formatLine);
                                    sheet.addCell(labelEmpty);
                                }
                                else{
                                    cel.setCellFormat(formatLine);
                              }
                            }

                            if(c== 0){
                                c++;
                            }
                            else{
                                c=0;
                            }
                        }

            workbook.write();
            workbook.close();

        } catch (IOException ex) {
            System.out.println("io exception: " + ex.getMessage());
            System.exit(1);
        } catch (jxl.write.WriteException exc) {
            System.out.println("write exception: " + exc.getMessage());
            exc.printStackTrace();
            System.exit(1);
        }

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
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("param").item(0); // get the first tag

            NodeList itemList = nodeLst.getChildNodes();

            for (i = 0; i < itemList.getLength(); i++) { // read all the tags inside

                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("fileName")) {
                        filename = item.getTextContent();
                    }
                }
            }

            if (filename.equalsIgnoreCase("")) {
                System.out.println("WARNING: no excel file specified to export results.");
                System.out.println("impossible to continue");
                System.exit(1);
            } else if (!(filename.contains("xls"))) {
                System.out.println("WARNING: the output file does not have the xls extension");
                System.out.println("impossible to export the results");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Exception while reading the parameter file" + dataFile + "\n" + e);
            System.err.println("Impossible to continue the quantifiation");
            System.exit(1);
        }

    }
    String filename;

    /**
     * Method to retrieve the name of the plugin.
     * @return A string with the plugin name.
     */
    public String getName() {
        return name;
    }

    /**
     * Method to retrieve the version of the plugin.
     * @return A string with the plugin version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Method to retrieve the type of the plugin.
     * @return A string with the plugin type.
     */
    public String getType() {

        return type;
    }

    /**
     * Method to retrieve the description of the plugin.
     * @return A string with the plugin description.
     */
    public String getDescription() {

        return description;
    }
    /**
     * The name of your plugin.
     */
    private final static String name = "OutputAPEXExcel";
    /**
     * The version of the plugin.
     */
    private final static String version = "1.0";
    /**
     * The plugin type. For an OUTPUT plugin it must be OUTPUT_plugin (do not change it).
     */
    private final static String type = "OUTPUT_plugin";
    /**
     * The description of the plugin.
     */
    private final static String description = "Plugin to export the APEX quantification results in an Excel workbook";
}
