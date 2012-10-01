package uk.ac.cranfield.xTracker.plugins.quantitation;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;
import uk.ac.cranfield.xTracker.xTracker;

/**
 *
 * @author Jun Fan@cranfield
 */
public abstract class quantitationPlugin implements pluginInterface {
    @Override
    public String getType(){
        return xTracker.QUANTITATION_TYPE;
    }
    /**
     * tell the program what are the quantitation types, e.g.
     * xTracker.study.addQuantitationName("iTRAQ intensities", "");
     * xTracker.study.addQuantitationName("peptide raw area", "MS:1001130");
     */
    abstract public void setQuantitationNames();
}
