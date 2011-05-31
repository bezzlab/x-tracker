package xtracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
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
import org.xml.sax.SAXException;

/**
 * This class is a plugin to export results from X-tracker into an Excel file. It reads the xQuant data structure and
 * copy the quantification results of each peptide into the Excel sheet.
 * This plugin is designed to be used for all quantification at the peptide level, with differently labeled peptides.
 *
 * @author Laurie Tonon ------ Cranfield University, X-tracker project
 */

public class outputExcel implements outPlugin {

    /**
     * The start method.
     * @param inputData is the xQuant structure to work on.
     * @param paramFile a string (that might be empty) containing the file name of parameters if any.
     * No need to return anything.
     */
    public void start(xQuant InputData, String paramFile) {


        try {

            // load the file to create
            this.loadParams(paramFile);

            //=================================================================================
            // CREATE A NEW WORKBOOK, WITH ONE SHEET AND A TITLE
            //=================================================================================

            WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
            WritableSheet sheet = workbook.createSheet("X-tracker results", 0);

            WritableFont font = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD, false);
            WritableCellFormat formatH = new WritableCellFormat(font);
            formatH.setAlignment(Alignment.CENTRE);
            formatH.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.CORAL);
            Label header = new Label(2, 1, "Quantification results from X-tracker pipeline");
            header.setCellFormat(formatH);
            sheet.addCell(header);
            sheet.mergeCells(2, 1, 8, 1);

            // ===============================================================
            // READ ALL THE DATAFILE AND COPY THEIR RESULTS IN THE EXCEL FILE
            // ===============================================================

            if (InputData.getQuantificationDataSize() > 0) {

                // list of the trio proteinId/peptideSeq/modifs already written in the excel file
                ArrayList<String[]> pepWritten = new ArrayList<String[]>();


                // Put the headers of the table
                WritableFont boldfont = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false);
                WritableCellFormat formatHeader = new WritableCellFormat(boldfont);
                formatHeader.setAlignment(Alignment.CENTRE);
                formatHeader.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.DARK_BLUE);

                Label labelProt = new Label(1, 5, "ProtID");
                labelProt.setCellFormat(formatHeader);
                 sheet.setColumnView(1, 20);
                sheet.addCell(labelProt);

                Label labelPep = new Label(2, 5, "Pept sequence");
                labelPep.setCellFormat(formatHeader);
                sheet.addCell(labelPep);
                sheet.setColumnView(2, 20);

                Label labelMod = new Label(3, 5, "Modifications");
                labelMod.setCellFormat(formatHeader);
                sheet.addCell(labelMod);
                sheet.setColumnView(3, 20);

                // index to avoid confusion in columns with the labels. Incremented for each quantData
                int r = 0;

                for (int q = 0; q < InputData.getQuantificationDataSize(); q++) {

                    xQuantData quantData = InputData.getElementAtIndex(q);

                    if (quantData.getQuantitativeDataSize() > 0) {

                        // number of different label for peptides
                        int label = quantData.getLabelsSize();

                        // *******************************************
                        // WRITE THE HEADERS FOR THE CURRENT DATA FILE
                        // *******************************************

                        // format the file name correctly and put it in the excel sheet
                        int index = quantData.getFileName().lastIndexOf('/');
                        Label LabelDataSet = new Label(r + 4, 4, quantData.getFileName().substring(index + 1));

                        // format for header cells
                        WritableCellFormat format = new WritableCellFormat();
                         format.setAlignment(Alignment.CENTRE);
                        format.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.DARK_BLUE);

                        LabelDataSet.setCellFormat(format);
                        sheet.addCell(LabelDataSet);
                        sheet.mergeCells(r + 4, 4, r + 3 + (label * 2), 4);

                        // format for normal cells
                        WritableCellFormat formatNoColour = new WritableCellFormat();
                        formatNoColour.setAlignment(Alignment.CENTRE);
                        formatNoColour.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.DARK_BLUE);



                        // index to jump 2 columns for each peptide, in order to put quantity+error for each
                        int strike = r;


                        for (int l = 0; l < label; l++) {
                            Label LabelLab = new Label(strike + 4 + l, 5, quantData.getAllLabels()[l]);
                            LabelLab.setCellFormat(formatNoColour);
                            sheet.addCell(LabelLab);
                            sheet.mergeCells(strike + 4 + l, 5, strike + 5 + l, 5);


                            Label LabelQuant = new Label(strike + 4 + l, 6, "Quantity");
                            LabelQuant.setCellFormat(formatNoColour);
                            sheet.addCell(LabelQuant);

                            Label LabelErr = new Label(strike + 5 + l, 6, "Error");
                            LabelErr.setCellFormat(formatNoColour);
                            sheet.addCell(LabelErr);

                            strike++;
                        }

                        // ****************************************************
                        // WRITE THE DATA FOR THE CURRENT DATA FILE
                        // ****************************************************


                        // index to know that we start putting the data at line 7
                        int line = 7;

                        // format for the protein ids
                        WritableCellFormat formatProt = new WritableCellFormat();
                        formatProt.setAlignment(Alignment.CENTRE);

                        for (int i = 0; i < quantData.getQuantitativeDataSize(); i++) {

                            xQuantities quantity = quantData.getQuantitativeDataElemAt(i);

                            // create a string with the modifications, in order to compare and display them easily
                            String modifs = "";
                                for (int m = 0; m < quantData.getQuantitativeDataElemAt(i).modifications.size(); m++) {
                                    modifs += quantData.getQuantitativeDataElemAt(i).getModificationAtIndex(m) + " (" + quantData.getQuantitativeDataElemAt(i).getModPositionAtIndex(m) + "); ";
                                }


                            String[] trio = {quantity.getProteinId(), quantity.getPeptideSeq(), modifs};

                            // If the same peptide for the same protein was already written
                            // only add the quantity, no need to write again the id and sequence
                            if (this.containsTrio(pepWritten, trio)) {

                                strike = r;

                                for (int l = 0; l < label; l++) {

                                    jxl.write.Number labelQty = new jxl.write.Number(strike + 4 + l, line +this.indexInList(pepWritten, trio), quantity.getQuantityAt(l));
                                    labelQty.setCellFormat(formatProt);
                                    sheet.addCell(labelQty);

                                    jxl.write.Number labelErr = new jxl.write.Number(strike + 5 + l, line +this.indexInList(pepWritten, trio), quantity.getQuantErrorAt(l));
                                    labelErr.setCellFormat(formatProt);
                                    sheet.addCell(labelErr);

                                    strike++;
                                }

                            }
                            // If the peptide had never been written before
                            // add all its information
                            else{
                                
                                Label labelID = new Label(1, line + pepWritten.size(), quantity.getProteinId());
                                labelID.setCellFormat(formatProt);
                                sheet.addCell(labelID);

                                Label labelSeq = new Label(2, line + pepWritten.size(), quantity.getPeptideSeq());
                                labelSeq.setCellFormat(formatProt);
                                sheet.addCell(labelSeq);

                                
                                Label mod = new Label(3, line + pepWritten.size(), modifs);
                                sheet.addCell(mod);

                                strike = r;

                                for (int lab = 0; lab < label; lab++) {

                                    jxl.write.Number labelQty = new jxl.write.Number(strike + 4 + lab, line + pepWritten.size(), quantity.getQuantityAt(lab));
                                    labelQty.setCellFormat(formatProt);
                                    sheet.addCell(labelQty);

                                    jxl.write.Number labelErr = new jxl.write.Number(strike + 5 + lab, line +pepWritten.size(), quantity.getQuantErrorAt(lab));
                                    labelErr.setCellFormat(formatProt);
                                    sheet.addCell(labelErr);

                                    strike++;
                                }

                                // Add this peptide to the list of written ones
                                pepWritten.add(trio);
                                
                            }
                        }

                    }

                    // jump of two columns to avoid writing the quantity of the next peptide on the error of this one
                    r += (quantData.getLabelsSize() * 2);

                }


            }

            //========================================================
            // COLOR EACH LINE OF THE TABLE IN A DIFFERENT COLOUR
            // =======================================================

                        int c = 0;

                        // color each row
                        for(int row=7; row<sheet.getRows(); row++){

                            WritableCellFormat formatLine = new WritableCellFormat();
                            formatLine.setAlignment(Alignment.CENTRE);

                            if(c==0){
                               formatLine.setBackground(Colour.GREY_25_PERCENT);
                            }

                            for(int column=1; column<sheet.getColumns(); column++){

                                WritableCell cel= sheet.getWritableCell(column, row);

                                if(cel.getContents().equalsIgnoreCase("")){
                                    // if the cell is empty, put empty string, if not no colour is applied
                                    Label labelEmpty=new Label(column, row,"");
                                    labelEmpty.setCellFormat(formatLine);
                                    sheet.addCell(labelEmpty);
                                }
                                else{
                                    cel.setCellFormat(formatLine);
                              }
                            }

                            // restart c to have colour every two lines
                           if(c== 0){
                                c++;
                            }
                            else{
                                c=0;
                            }
                        }

            // save the work and clost the workbook
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
             // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("param").item(0);

            String schemaLocation="";

            if(nodeLst.getAttributes().getNamedItem("xsi:schemaLocation") != null){
                schemaLocation=nodeLst.getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
            }
            else {
                if(nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation")!= null){
                schemaLocation=nodeLst.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation").getTextContent();
                }
                else{
                    System.out.println("ERROR: No .xsd schema is provided for " + dataFile );
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
                System.out.println("\n\nERRROR - could not validate the input file " + dataFile+"!");
                System.out.print(e);
                System.exit(1);
            }


            nodeLst = doc.getElementsByTagName("param").item(0); // get the first tag

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
            System.err.println("Exception while reading the parameter file " + dataFile + "\n" + e);
            System.err.println("Impossible to continue the quantifiation");
            System.exit(1);
        }

    }
    String filename;


    /**
     * Method to know if a trio of proteinId/peptideSeq/modifs already exists in an arraylist of written peptides
     * @param list the arraylist of the peptides already written in the Excel file
     * @param trio array of string containing the proteinId, peptideSeq and modifs belonging to one peptide
     * @return true if the peptide is contained in the list, false otherwise
     */
    private boolean containsTrio(ArrayList<String[]> list,String [] trio){

        boolean contained=false;

        for(int i=0; i<list.size(); i++){
            if(trio[0].equals(list.get(i)[0]) && trio[1].equals(list.get(i)[1]) && trio[2].equals(list.get(i)[2])){
                contained =true;
            }
        }

        return contained;

    }

    /**
     * Method to know the index of an array of String in an ArrayList
     * @param list the ArrayList of String arrays
     * @param trio the array of String containing the proteinID, peptideSeq and modifs of a peptide
     * @return the index in the list of the array of String
     */
    private int indexInList(ArrayList<String[]> list,String [] trio){

        int index=0;

        for(int i=0; i<list.size(); i++){

            if(trio[0].equals(list.get(i)[0]) && trio[1].equals(list.get(i)[1]) && trio[2].equals(list.get(i)[2])){
                index =i;
            }

        }

        return index;
    }

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
     * Put in this String the name of your plugin.
     */
    private final static String name = "outputExcel";
    /**
     * The version of the plugin goes here.
     */
    private final static String version = "1.0";
    /**
     * The plugin type. For an OUTPUT plugin it must be OUTPUT_plugin (do not change it).
     */
    private final static String type = "OUTPUT_plugin";
    /**
     * A string with the description of the plugin.
     */
    private final static String description = "This class is a plugin to export results from X-tracker into an Excel file. It reads the xQuant data structure and"+
 "copy the quantification results of each peptide into the Excel sheet."+
 "This plugin is designed to be used for all quantification at the peptide level, with differently labeled peptides.";
}
