package uk.ac.cranfield.xTracker.plugins.quantitation;

import uk.ac.cranfield.xTracker.plugins.pluginInterface;

/**
 *
 * @author Jun Fan@cranfield
 */
public interface quantitationPlugin  extends pluginInterface {
    String type = uk.ac.cranfield.xTracker.xTracker.QUANTITATION_TYPE;
    /**
     * tell the program what are the quantitation types, e.g.
     * xTracker.study.addQuantitationName("iTRAQ intensities", "");
     * xTracker.study.addQuantitationName("peptide raw area", "MS:1001130");
     */
    public void setQuantitationNames();
}
