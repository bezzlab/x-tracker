
//
//    xTrackerGui
//
//    Package:    xtrackergui.gui
//    File:       XTrackerGuiComponent.java
//    Date:       01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import xtrackergui.model.XTrackerXmlDocumentRow;

/**
 * Interface describing the requirements of a xTracker GUI display component
 *
 * @author andrew bullimore
 */
public interface XTrackerGuiComponent {

    /**
     * Return the a reference to the component (cannot be null)
     *
     */
    JComponent getComponent();

    /**
     *
     *
     */
    void setGuiComponentParentDisplayPanel(JPanel parentPanel);

    /**
     * Set the XTrackerGuiComponent
     *
     */
    void setXTrackerXmlDocumentRow(XTrackerXmlDocumentRow documentRow);

    /**
     * Set XTrackerInputFilter object to validae data entry
     *
     */
    void setXTrackerInputFilter(XTrackerInputFilter inputFilter);

    /**
     * Set whether the value entered via this component is to be included in the xml file produced by xTracker GUI
     *
     */
    void setDoNotAddValueToXml();
    
    /**
     * Update the value entered by the user
     *
     */
    void updateEnteredValue();
}
