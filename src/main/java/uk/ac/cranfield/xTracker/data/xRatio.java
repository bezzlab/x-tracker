/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cranfield.xTracker.data;

import uk.ac.cranfield.xTracker.xTracker;
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
        
        denominator = ratio.getDenominatorRef();
        Object denominatorObj;
        String denominatorType = null;
        denominatorObj = xTracker.study.getAssay(denominator);
        if (denominatorObj != null){
            denominatorType = ASSAY;
        }else{
            denominatorObj = xTracker.study.getSV(denominator);
            if (denominatorObj == null){
                System.out.println("Can not find corresponding assay or study variable for denominator id "+denominator);
                System.exit(2);
            }else{
                denominatorType = STUDY_VARIABLE;
            }
        }
        
        numerator = ratio.getNumeratorRef();
        Object numeratorObj;
        String numeratorType = null;
        numeratorObj = xTracker.study.getAssay(numerator);
        if (numeratorObj != null){
            numeratorType = ASSAY;
        }else{
            numeratorObj = xTracker.study.getSV(numerator);
            if (numeratorObj == null){
                System.out.println("Can not find corresponding assay or study variable for numerator id "+numerator);
                System.exit(2);
            }else{
                numeratorType = STUDY_VARIABLE;
            }
        }

        if(!denominatorType.equals(numeratorType)){
            System.out.println("Trying to calculate ratio from two different types of data in Ratio "+ratio.getId());
            System.exit(1);
        }
        type = numeratorType;
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
