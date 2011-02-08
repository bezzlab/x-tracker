
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerXmlDocumentRowAttribute.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import javax.swing.JComponent;
import xtrackergui.gui.XTrackerGuiComponent;

/**
 * A Data control class to coordinate the information required to populate attribute data for an xml document
 *
 * @author andrew bullimore
 */
public class XTrackerXmlDocumentRowAttribute {

    String ownerTagName = "";
    String attributeName = "";
    XTrackerXmlDocumentRowData rowData = null;
    XTrackerGuiComponent displayComponent = null;

    /**
     * Default constructor, create an empty XTrackerXmlDocumentRowAttribute
     *
     */
    public XTrackerXmlDocumentRowAttribute() {
    }

    /**
     * Create and populate a XTrackerXmlDocumentRowAttribute object
     *
     * @param owner The name xml document row this attribute belongs to
     * @param name The name of this xml document row  attribute
     * @param defaultVal The default value, if any for this xml document row attribute
     * @param isFixedVal True if this value is fixed at a set value
     * @param isRequiredVal True if this xml document row is a required/ mandatory row
     */
    public XTrackerXmlDocumentRowAttribute(String owner,
                                           String name,
                                           String value,
                                           String defaultVal,
                                           boolean isFixedVal,
                                           boolean isRequiredVal) {

        ownerTagName = owner;
        attributeName = name;

        rowData = new XTrackerXmlDocumentRowData(value,
                                                 defaultVal,
                                                 isFixedVal,
                                                 isRequiredVal);
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        outputString.append("Attributes - " +
                            " name: " + attributeName +
                            ": owner tag: " + ownerTagName + '\n');

        outputString.append(rowData);

        return outputString.toString();
    }

    /**
     * Get the xml document row to whih this attribute row belongs
     *
     * @return The xml document row to whih this attribute row belongs
     */
    public String getOwnerTagName() {

        return ownerTagName;
    }

    /**
     * Get the name of this xml document row attribute
     *
     * @return The name of the attribute
     */
    public String getAttributeName() {

        return attributeName;
    }

    /**
     * Check the data value for this xml document attribute is a fixed value
     * 
     * @param The fixed value
     */
    public boolean getIsFixedValue() {

        return rowData.getIsFixedTagValue();
    }


    /**
     * Get the data value for this xml document row attribute
     *
     * @return The data value
     */
    public String getAttributeValue() {

        return rowData.getTagValue();
    }

    /**
     * Set the value of this xml document row attribute
     *
     * @param value The value to set this attribute to
     */
    public void setAttributeValue(String value) {

        rowData.setTagValue(value);
    }

    /**
     *
     *
     */
    public void resetTagValue() {

        rowData.resetTagValue();
    }


    /**
     *
     *
     */
    public void initialiseAttributeValue() {

        if(displayComponent != null) {

            displayComponent.updateEnteredValue();
        }
    }

    /**
     * Check if the xml document row attribute has been updated since it was created
     *
     * @return True if the data value has unsaved edits
     */
    public boolean xmlDocumentRowAttributeHasEdits() {

        return rowData.xmlDocumentRowDataHasEdits();
    }

    /**
     * Save changes to the xml document row attribute data
     *
     */
    public void saveXmlDocumentRowAttributeEdits() {

        rowData.saveXmlDocumentRowDataEdits();
    }

    /**
     * Save changes to the xml document row attribute data
     *
     */
    public void rollBackXmlDocumentRowAttributeEdits() {

        rowData.rollBackXmlDocumentRowDataEdits();
    }

    /**
     * Set the GUI component for this xml document row attribute
     *
     * @param The GUI component for this xml document row attribute
     */
    public void setDisplayComponent(XTrackerGuiComponent component) {

        displayComponent = component;
    }

    /**
     * Return the GUI component for this xml document row attribute
     *
     * @return The GUI component for this xml document row attribute
     */
    public JComponent getDisplayComponent() {

        if(displayComponent != null) {

            return displayComponent.getComponent();
        }

        return null;
    }
}
