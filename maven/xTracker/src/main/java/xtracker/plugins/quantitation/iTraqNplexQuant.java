package xtracker.plugins.quantitation;

import java.util.*;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Jama.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import xtracker.data.xLcMsMsCorr;
import xtracker.data.xPeaks;
import xtracker.data.xQuant;
import xtracker.data.xQuantData;
import xtracker.data.xQuantities;
import xtracker.data.xCorrespondenceData;
import xtracker.data.xCorrespondences;

public class iTraqNplexQuant implements quantPlugin {

    public xQuant start(xPeaks inputData, String paramFile) {
        xQuant ret = new xQuant();
        System.out.println(getName() + ": starting...");
        //Let's load parameters from the file (i.e. reporter ions and puriry correction factors if any)

        loadParams(paramFile);

        Matrix matrixC = null;

        //If purity correction is enabled the coefficient matrix is computed.
        if (computePurityCorr) {
            //Let's compute the coefficients matrix, it will be the same for all elements.
            matrixC = computeCoeffMatrix();
            //  System.out.println("C matrix det:" + matrixC.det());
            //If matrix is singular, print a warning message and carry on without purity correction.
            if (matrixC.det() == 0) {
                for (int spaces = 0; spaces < 10; spaces++) {
                    System.out.println("******");
                }
                System.out.println("*** WARNING: Coefficients Matrix is singular. No purity correction can be performed!");
                for (int spaces = 0; spaces < 10; spaces++) {
                    System.out.println("******");
                }
                computePurityCorr = false;
            }

        }

        //Let's create an array of strings containing all the labels to be added to the xQuantData structure
        String[] mylbls = new String[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            mylbls[i] = labels.elementAt(i);

        }
        //Let's process the data from all raw data files.
        for (int i = 0; i < inputData.getSize(); i++) {



            xCorrespondences myCorr = inputData.getElemAt(i);
            //Let's retrieve the filename, will be handy later on
            String fileNM = myCorr.getFileName();

            //Let's create the xQuantData structure to be added to the results
            xQuantData myQuantDataElem = new xQuantData(fileNM, labels.size());
            //Let's set all the labels in a single go.
            myQuantDataElem.setAllLabels(mylbls);


            //Let's loop through all the correspondences belonging to this raw data file.
            for (int j = 0; j < myCorr.getPeptideCorrespondenceDataSize(); j++) {
                xCorrespondenceData myCorrData = myCorr.getPeptideCorrespondenceDataElemtAt(j);
                //Let's get the proteinId and peptide sequence (remember that they are unique for xCorrespondenceData entries)
                String protId = myCorrData.getProteinId();
                String pepSeq = myCorrData.getPeptideSeq();

                xQuantities myQNT = new xQuantities(protId, pepSeq, labels.size());

                Vector<String> modifications = new Vector<String>();
                Vector<Integer> positions = new Vector<Integer>();

                //Let's create the array of doubles that will contain the quantities for this peptide label by label 
                //if purity correction is enabled, then labelledQuants will be the measured areas (matrixA) to be used in the formula
                //T=matrixC^(-1)*matrixA 
                int labelsSize = labels.size();
                double[][] labelledQuants = new double[labelsSize][1];

                //Let's get peaks information out to perform quantitation.
                //This can be done label by label which is particularly convenient in this
                //case and this guarantees to keep the same ordering as in labels.
                for (int k = 0; k < labelsSize; k++) {
                    labelledQuants[k][0] = 0;
                    //The string with the sperimental conditions.
                    String labL = labels.elementAt(k);
                    Vector<xLcMsMsCorr> myCorrOfLab = myCorrData.getLcMsMsCorrLabelled(labL);
                    //Let's retrieve all the peaks to quantitate on them
                    //iTraq quantitation is done integrating on Tandem MS peaks therefore we will need
                    //only mz values and intensities.
                    Vector<Float> mzValues = new Vector<Float>();
                    Vector<Float> intValues = new Vector<Float>();
                    for (int jj = 0; jj < myCorrOfLab.size(); jj++) {
                        xLcMsMsCorr elem = myCorrOfLab.elementAt(jj);
                        mzValues.addElement(elem.getMz());
                        intValues.addElement(elem.getIntensity());

                        for (int hh = 0; hh < elem.getModificationSize(); hh++) {
                            String modNm = elem.getModificationNameAtIndex(hh);
                            int modPos = elem.getModPositionAtIndex(hh);
                            if ((modifications.indexOf(modNm) == -1) || (modifications.indexOf(modNm) > -1 && positions.elementAt(modifications.indexOf(modNm)) != modPos)) {
                                modifications.add(modNm);
                                positions.add(modPos);
                            }

                        }

                    }
                    //The algorithm chosen depends on what is set in the configuration file
                    if (intMethod.equals("Areas")) {

                        labelledQuants[k][0] = this.computeArea(mzValues, intValues);
                    } else {
                        if (intMethod.equals("AreasSimpson")) {
                            labelledQuants[k][0] = this.computeAreaSimpson(mzValues, intValues);

                        } else {
                            if (intMethod.equals("SumIntensities")) {
                                labelledQuants[k][0] = this.computeSumInt(mzValues, intValues);
                            } else {
                                labelledQuants[k][0] = this.computeArea(mzValues, intValues);
                            }
                        }

                    }



                    //Let's get the modifications (if any)


                }
                //        for(int luca=0;luca<labelledQuants.length;luca++){
                //             System.out.println(luca +") QNTY: " + labelledQuants[luca][0]);

                //         }
                Matrix matrixT;
                //If purity correction is switched on we have to compute the
                if (this.computePurityCorr) {

                    //solving the system of equations to get the corrected area values.
                    //The system to be solved is the following:
                    // matrixC*matrixT=matrixA and we are interested in finding out the values of matrixT.
                    Matrix matrixA = new Matrix(labelledQuants);
                    matrixT = matrixC.solve(matrixA);
                } else {
                    //The matrix of coefficients has determinant =0 therefore it is singular (cannot be inverted).
                    //keep quantities as computed without purity corrections.
                    if (labelledQuants.length > 0) {
                        matrixT = new Matrix(labelledQuants);
                    } else {
                        //   System.out.println("Labels size:" + labels.size());
                        double[][] myArray = new double[labels.size()][1];
                        for (int iii = 0; iii < myArray.length; iii++) {
                            myArray[iii][0] = 0;
                        }
                        matrixT = new Matrix(myArray);

                    }
                }
                //Each element in matrixT represents the quantity of the peptide in different conditions.
                //Let's add this information to the xQuant output structure.
                for (int matTcnt = 0; matTcnt < matrixT.getRowDimension(); matTcnt++) {
                    //System.out.println();
                    float qntty = Double.valueOf(matrixT.get(matTcnt, 0)).floatValue();
                    myQNT.addQuantity(matTcnt, qntty);

                }
                //Let's add modifications to the xQuantities structure
                for (int modCnt = 0; modCnt < modifications.size(); modCnt++) {
                    myQNT.addModification(modifications.elementAt(modCnt), positions.elementAt(modCnt));
                }
                //Let's add the quantities to this quantData element
                myQuantDataElem.addQuantitativeDataElem(myQNT);
            }

            //Let's add the quantitative information from this raw data file to the
            //xQuant data structure.
            // System.out.println("quantifying data from "+ myQuantDataElem.getFileName());
            ret.addQuantificationDataElem(myQuantDataElem);
        }



        System.out.println("Done!");
        return ret;
    }

    /**
     * Computes the coefficients matrix for purity correction.
     * @return ret the coeffiecients matrix.
     */
    public Matrix computeCoeffMatrix() {
        Matrix ret = Jama.Matrix.identity(numRepIons, numRepIons);
        int pSize = matrixP.size();
        for (int i = 0; i < pSize; i++) {
            //The diagonal of C will contain 100-Pi.
            ret.set(i, i, 100 - matrixP.elementAt(i).doubleValue());
        }


        ret.print(ret.getRowDimension(), ret.getColumnDimension());

        for (int i = 0; i < numRepIons; i++) {
            for (int j = 0; j < numRepIons; j++) {
                //Do not do anything on the diagonal, it is already 100-Pi
                if (i != j) {
                    double mzI = reporterIons.elementAt(i);
                    double mzJ = reporterIons.elementAt(j);
                    double shiftsI[] = matrixShifts[i];



                    //      System.out.println(i+","+j+": "+mzI + " " + mzJ);

                    int shiftSize = shiftsI.length;
                    boolean foundK = false;
                    int k = 0;
                    //Let's try to see if we have a contribution of reporter i to reporter j.
                    for (k = 0; k < shiftSize && foundK == false; k++) {

                        if (mzI + shiftsI[k] == mzJ) {
                            foundK = true;


                        }
                    }

                    if (foundK) {
                        //Yes we have a contribution of i to j.

                        ret.set(j, i, matrixM[i][k - 1]);

                    }
                }

            }

        }
//        System.out.println("C matrix:");
//        ret.print(ret.getRowDimension(),ret.getColumnDimension());
        return ret;

    }

    /**
     * Computes sum of intensities contained in input vector intVals.
     * @param mzVals the mzValues (not used in this case)
     * @param intVals intensity values.
     * @return the sum of intensities of the input peaks.
     */
    public double computeSumInt(Vector<Float> mzVals, Vector<Float> intVals) {
        double ret = 0;
        int i = 0;
        for (i = 0; i < mzVals.size(); i++) {
            ret += intVals.elementAt(i);

        }
        return ret;

    }

    /**
     * Computes the area of the peak. NOTE THAT it assumes
     * the vectors ordered by mz values ascendingly.
     * @param IC the vector of Intensities.
     * @param MZ the vector of mz values.
     * @return the area of the peak.
     */
    public float computeArea(Vector<Float> MZ, Vector<Float> IC) {

        float ret = 0;
        float tmp_val = 0;
        int size = MZ.size();

        //for(int j=0;j<MZ.size();j++){
        //    System.out.println(j+ ") MZ[" + MZ.elementAt(j) + "]=" + IC.elementAt(j));
        //}

        for (int i = 0; i < size - 1; i++) {
            tmp_val = (IC.elementAt(i) + IC.elementAt(i + 1)) * (MZ.elementAt(i + 1) - MZ.elementAt(i)) / 2;
            ret += tmp_val;
        }

        return ret;
    }

    /**
     * Computes the area of the peaks. NOTE THAT it assumes
     * the vectors ordered by mz values ascendingly.
     * @param IC the vector of Intensities.
     * @param MZ the vector of MZ values.
     * @return the area of the peak.
     */
    public float computeAreaSimpson(Vector<Float> MZ, Vector<Float> IC) {

        float ret = 0;
        float tmp_val = 0;
        int size = MZ.size();
        int remainder = size % 3;
        int i = 0;
        int starti = 0;
        int endi = size - 1;
        float x1 = 0; //RT values
        float x2 = 0;
        float x3 = 0;
        float y1 = 0; //Intensity values
        float y2 = 0;
        float y3 = 0;
        float h = 0; //the common mz step (it's the average of the two steps)
        //for(int j=0;j<MZ.size();j++){
        //    System.out.println(j+ ") RT[" + MZ.elementAt(j) + "]=" + IC.elementAt(j));
        //}

        if (size > 1) {
            switch (remainder) {
                case 1: {
                    //The number of peaks cannot be divided by three, the first and last two peaks will be integrated with
                    //trapezoid rule.
                    tmp_val = (IC.elementAt(0) + IC.elementAt(1)) * (MZ.elementAt(1) - MZ.elementAt(0)) / 2;
                    ret += tmp_val;
                    starti = 2;
                    tmp_val = (IC.elementAt(size - 2) + IC.elementAt(size - 1)) * (MZ.elementAt(size - 1) - MZ.elementAt(size - 2)) / 2;
                    ret += tmp_val;
                    endi = size - 2;

                }
                case 2: {
                    //The number of peaks cannot be divided by three, the first two peaks will be integrated with
                    //trapezoid rule.
                    tmp_val = (IC.elementAt(0) + IC.elementAt(1)) * (MZ.elementAt(1) - MZ.elementAt(0)) / 2;
                    ret += tmp_val;
                    starti = 2;

                }
            }
        }
        for (i = starti; i < endi; i = i + 3) {
            x1 = MZ.elementAt(i);
            x2 = MZ.elementAt(i + 1);
            x3 = MZ.elementAt(i + 2);
            y1 = IC.elementAt(i);
            y2 = IC.elementAt(i + 1);
            y3 = IC.elementAt(i + 2);

            h = (x3 - x1) / 2;

            // System.out.println("x:" + x1 + " " + x2 + " " + x3 + " y:" + y1 + " " + y2 + " " + y3 + " h:" + h);

            tmp_val = h * (y1 / 3 + (4 * y2) / 3 + y3 / 3);
            ret += tmp_val;
        }

        return ret;
    }

    /**
     * Opens the dataFile xml file and loads reporter ions masses, labels as well as tolerances.
     * @param dataFile the xml file
     */
    public void loadParams(String dataFile) {

        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            // create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


            doc.getDocumentElement().normalize();

            Node nodeLst = doc.getElementsByTagName("iTraq").item(0);

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
            //Let's get tolerances first.
            nodeLst = doc.getElementsByTagName("settings").item(0);

            NodeList itemList = nodeLst.getChildNodes();
            for (int i = 0; i < itemList.getLength(); i++) {
                Node item = itemList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals("NumcorrFactors")) {
                        numCorrFactors = Integer.valueOf(item.getTextContent());
                        //              System.out.println("Num corr factors:" +numCorrFactors);

                    } else if (item.getNodeName().equals("NumReporterIons")) {
                        numRepIons = Integer.valueOf(item.getTextContent());
                        //             System.out.println("Num rep Ions:" +numRepIons);
                    } //Let's get the integration algorithm
                    else if (item.getNodeName().equals("IntegrationMethod")) {
                        intMethod = item.getTextContent();
                        //           System.out.println("Integration Method:" +intMethod);
                    }

                }
            }

            //If we have some reporter ions then we will compute purity correction otherwise nothing will be loaded.
            if (numRepIons > 0 && numCorrFactors > 0) {
                //Let's load reporter ions (and corresponding labels) as well as correction factors.
                this.matrixM = new double[numRepIons][numCorrFactors];
                this.matrixShifts = new double[numRepIons][numCorrFactors];
                for (int cnt = 0; cnt < numRepIons; cnt++) {
                    for (int cnt1 = 0; cnt1 < numCorrFactors; cnt1++) {
                        matrixM[cnt][cnt1] = 0;
                        matrixShifts[cnt][cnt1] = 0;
                    }

                }
                computePurityCorr = true;
                double pSum = 0;
                nodeLst = doc.getElementsByTagName("reporterIons").item(0);
                itemList = nodeLst.getChildNodes();
                int repCounter = 0;
                for (int i = 0; i < itemList.getLength(); i++) {

                    Node item = itemList.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        //System.out.println(item.getTextContent());
                        if (item.getNodeName().equals("reporter")) {
                            String mzVal = item.getAttributes().getNamedItem("mz").toString();
                            String label = item.getAttributes().getNamedItem("label").toString();
                            //Let's remove the mz=" and the final "
                            mzVal = mzVal.substring(4, mzVal.length() - 1);
                            //Let's remove the label=" and the final "
                            label = label.substring(7, label.length() - 1);
                            // System.out.println("Reporter: " +mzVal);
                            reporterIons.addElement(Float.valueOf(mzVal));
                            labels.addElement(label);
                            // System.out.println(" - Label: " +label);
                            //Node subNode = doc.getElementsByTagName("correctionFactors").item(0);
                            NodeList subItemList = item.getChildNodes();//subNode.getChildNodes();

                            for (int j = 0; j < subItemList.getLength(); j++) {

                                Node subItem = subItemList.item(j);
                                if (subItem.getNodeType() == Node.ELEMENT_NODE) {

                                    if (subItem.getNodeName().equals("correctionFactors")) {
                                        NodeList subSubList = subItem.getChildNodes();
                                        //Let's compute the sum of correction factors to save as 100-pSum in the matrixP
                                        pSum = 0;
                                        int corrCounter = 0;
                                        for (int k = 0; k < subSubList.getLength(); k++) {
                                            Node myCorrFact = subSubList.item(k);
                                            if (myCorrFact.getNodeType() == Node.ELEMENT_NODE && myCorrFact.getNodeName().equals("factor")) {
                                                String deltaMass = myCorrFact.getAttributes().getNamedItem("deltaMass").toString();
                                                deltaMass = deltaMass.substring(11, deltaMass.length() - 1);
                                                double dMass = Double.valueOf(deltaMass).doubleValue();
                                                double corrFac = Double.valueOf(myCorrFact.getTextContent().toString()).doubleValue();

                                                matrixM[repCounter][corrCounter] = corrFac;
                                                matrixShifts[repCounter][corrCounter] = dMass;
                                                //System.out.println("Delta Mass: "+ dMass);
                                                // System.out.println("- Corr fact: "+ corrFac);
                                                pSum += corrFac;
                                                corrCounter++;
                                            }
                                        }
                                        repCounter++;
                                    }
                                }

                            }
//                        System.out.println("Matrix M:");
//                        for(int kkk=0;kkk<tmpMvect.length;kkk++){
//                           System.out.println(tmpMvect[kkk]);
//                        }


                            matrixP.addElement(Double.valueOf(pSum));


                        }


                    }
                }


            } else {
                computePurityCorr = false;

                nodeLst = doc.getElementsByTagName("reporterIons").item(0);
                itemList = nodeLst.getChildNodes();

                for (int i = 0; i < itemList.getLength(); i++) {

                    Node item = itemList.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        //System.out.println(item.getTextContent());
                        if (item.getNodeName().equals("reporter")) {
                            String mzVal = item.getAttributes().getNamedItem("mz").toString();
                            String label = item.getAttributes().getNamedItem("label").toString();
                            //Let's remove the mz=" and the final "
                            mzVal = mzVal.substring(4, mzVal.length() - 1);
                            //Let's remove the label=" and the final "
                            label = label.substring(7, label.length() - 1);
                            // System.out.println("Reporter: " +mzVal);
                            reporterIons.addElement(Float.valueOf(mzVal));
                            labels.addElement(label);
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Exception while reading " + dataFile + "\n" + e);
            System.exit(1);
        }


    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
    private int numCorrFactors = 0;
    private int numRepIons = 0;
    private String intMethod = "";
    private Vector<Float> reporterIons = new Vector<Float>();
    private Vector<String> labels = new Vector<String>();
    private double[][] matrixM;
    private Vector<Double> matrixP = new Vector<Double>();
    private double[][] matrixShifts;
    private boolean computePurityCorr = true;
    private final static String name = "iTraqNplexQuant";
    private final static String version = "1.0";
//    private final static String type = "QUANT_plugin";
    private final static String description = "Computes an N-plex iTRAQ quantitation.\n\tA custom number of correction factors can be specified in input\n\tand matrix operations are performed thanks to Jama Java package.\n\tSeveral types of peak integration are also supported  (i.e. trapezoid\n\trule, simpson\'s rule and sum of intensities).";
}