/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.xTracker.data;

import uk.ac.liv.jmzqml.model.mzqml.Assay;
import uk.ac.liv.jmzqml.model.mzqml.Ratio;
import uk.ac.liv.jmzqml.model.mzqml.StudyVariable;

/**
 *
 * @author Jun Fan@cranfield
 */
public class xRatio {
    String id = "";
    String denominator = "";
    String numerator = "";
    String type = "";
    public final static String ASSAY = "Assay";
    public final static String STUDY_VARIABLE = "StudyVariable";
    Ratio ratio;
    
    public xRatio(Ratio ratio){
        this.ratio = ratio;
        id = ratio.getId();
        Object denominatorObj = ratio.getDenominatorRef();
        Object numeratorObj = ratio.getNumeratorRef();
        if(denominatorObj.getClass() != numeratorObj.getClass()){
            System.out.println("Trying to calculate ratio from two different types of data in Ratio "+ratio.getId());
            System.exit(1);
        }
        String clazz = denominatorObj.getClass().toString();
        clazz = clazz.substring(clazz.lastIndexOf(".") + 1);
        if(denominatorObj instanceof Assay){
            denominator = ((Assay)denominatorObj).getId();
            numerator = ((Assay)numeratorObj).getId();
            type = ASSAY;
        }else if (denominatorObj instanceof StudyVariable){
            denominator = ((StudyVariable)denominatorObj).getId();
            numerator = ((StudyVariable)numeratorObj).getId();
            type = STUDY_VARIABLE;
        }else{
            System.out.println("Unrecognized type "+clazz+" in ratio calculation in Ratio "+ratio.getId());
            System.out.println("Please make sure that only Assay or StudyVariable are used in the ratio calculation");
            System.exit(1);
        }
    }
    /**
     * Get the denominator in the ratio calculation
     * @return 
     */
    public String getDenominator() {
        return denominator;
    }
    /**
     * Get the id
     * @return 
     */
    public String getId() {
        return id;
    }
    /**
     * Get the type of the ratio, either assay or study variable
     * @return 
     */
    public String getType() {
        return type;
    }
    /**
     * Get the numerator in the ratio calculation
     * @return 
     */
    public String getNumerator() {
        return numerator;
    }
    /**
     * Get the ratio mzQuantML element
     * @return 
     */
    public Ratio getRatio(){
        return ratio;
    }
}
