
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerXmlDocumentRow.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import xtrackergui.gui.XTrackerGuiComponent;

/**
 * A Data control class to coordinate the information required to populate a row of data in an xml document
 *
 * @author andrew bullimore
 */
public class XTrackerXmlDocumentRow {

    int groupRef = 0;
    boolean excludeFromXmlDocument = false;
    boolean sectionStartTag = false;
    boolean documentRootElement = false;
    String parentTagName = "";
    String tagName = "";
    int minOccurs = -10;
    int maxOccurs = -10;
    XTrackerXmlDocumentRowData rowData = null;
    List<XTrackerXmlDocumentRowAttribute> tagAttributes = null;
    XTrackerGuiComponent displayComponent = null;

    /**
     * Default constructor, creates an empty XTrackerXmlDocumentRow
     *
     */
    public XTrackerXmlDocumentRow() {
    }

    /**
     * Create and populate a XTrackerXmlDocumentRow
     *
     * @param isRoot Set to true if the row is the document or root element of the resulting xml document
     * @param isSectionStart Set to true if this is a xml section start tag
     * @param parentName Set to the name of the xml section start tag
     * @param name The name of this tag in the resulting xml document
     * @param min The minimum number of occurances for this row/ tag type
     * @param max The maximum number of occurances for this row/ tag type
     * @param value The current data value stored in this row
     * @param defaultVal The dafault value for this row/ tag
     * @param isFixedVal True if the data value for this row/ tag is fixed at a set value
     */
    public XTrackerXmlDocumentRow(boolean isRoot,
                                  boolean isSectionStart,
                                  String parentName,
                                  String name,
                                  int min,
                                  int max,
                                  String value,
                                  String defaultVal,
                                  boolean isFixedVal) {

        documentRootElement = isRoot;
        sectionStartTag = isSectionStart;
        parentTagName = parentName;
        tagName = name;
        minOccurs = min;
        maxOccurs = max;

        // if the value of minOccurs is 0 then this field/ row is not a mandatory/ required row
        // is this correct??
        boolean isRequiredVal = false;
        if(minOccurs > 0) {

            isRequiredVal = true;
        }

        // Populate the row data object
        rowData = new XTrackerXmlDocumentRowData(value,
                                                 defaultVal,
                                                 isFixedVal,
                                                 isRequiredVal);
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        outputString.append("\nXML Document Row - ");

        if(documentRootElement) {

            outputString.append(" root: " + documentRootElement);
        }

        outputString.append(": section tag: " + sectionStartTag +
                            ": Tag name: " + tagName +
                            ": Parent: " + parentTagName +
                            ": Repeat Group Ref " + groupRef + '\n');

        if(rowData != null) {

            outputString.append(rowData);
        }

        if(tagAttributes != null) {

            for(XTrackerXmlDocumentRowAttribute attribute : tagAttributes) {

                outputString.append(attribute);
            }
        }

        return outputString.toString();
    }

    /**
     * Determine if this xml document row is the root/ document element
     *
     * @return True if the xml document row is the root/ document element
     */
    public boolean isDocumentRootElement() {

        return documentRootElement;
    }

    /**
     * Determine if this row is a xml section start
     *
     * @return True if this is a xml section start tag
     */
    public boolean isSectionStartTag() {

        return sectionStartTag;
    }

    /**
     * Return if this xml document row is to be excluded from the resulting xml document
     *
     * @return True if the row is to be excluded
     */
    public boolean getExcludeFromXmlDocument() {

        return excludeFromXmlDocument;
    }

    /**
     * Sets whether to include or exclude the xml document row
     * 
     * @param exclude If set true, include row, false, exclude
     */
    public void setExcludeFromXmlDocument(boolean exclude) {

        excludeFromXmlDocument = exclude;
    }

    /**
     * Set the GUI component for this xml document row
     *
     * @param The GUI component for this xml document row
     */
    public void setDisplayComponent(XTrackerGuiComponent component) {

        displayComponent = component;
    }

    /**
     * Return the GUI component for this xml document row
     *
     * @return The GUI component for this xml document row
     */
    public JComponent getDisplayComponent() {

        if(displayComponent != null) {

            return displayComponent.getComponent();
        }

        return null;
    }

    /**
     * Returns the repeat group reference number this row belongs too
     *
     * @return The repeat group ref number
     */
    public int getGroupRef() {

        return groupRef;
    }

    public void setGroupRef(int ref) {

        groupRef = ref;
    }

    /**
     * Return the name of the parent tag this xml document row belongs to
     *
     * @return The name of the parent tag
     */
    public String getParentTagName() {

        return parentTagName;
    }

    /**
     * Return this xml document rows name
     *
     * @param The tag name of this row
     */
    public String getTagName() {

        return tagName;
    }

    /**
     * Return the original value set
     *
     * @return The original tag value (the starting value this tag was set as and
     *         the tag value after a save has been performed)
     */
    public String getOriginalTagValue() {

        return rowData.getOriginalTagValue();
    }

    /**
     * Return the current data value of this xml document row
     *
     * @return The data value for this row
     */
    public String getTagValue() {

        return rowData.getTagValue();
    }
    
    /**
     * Set the current data value of this xml document row
     *
     * @param The data value to set for this row
     */
    public void setTagValue(String tagValue) {

        rowData.setTagValue(tagValue);
    }

    /**
     *
     *
     *
     */
    public void resetTagValue() {

        rowData.resetTagValue();
    }

    public void refreshTagValue() {

        if(displayComponent != null) {

            displayComponent.updateEnteredValue();
        }
    }

    /**
     *
     *
     */
    public void initialiseTagValue(String value) {

        rowData.setTagValue(value);
        rowData.setOriginalTagValue(value);

        if(displayComponent != null) {

            displayComponent.updateEnteredValue();
        }
    }

    public void initialiseAttributeList() {

        if(tagAttributes != null) {

            for(XTrackerXmlDocumentRowAttribute attr : tagAttributes) {

                attr.initialiseAttributeValue();
            }
        }
    }

    /**
     * Return the number of attributes associated with this xml document row
     *
     * @return  The number of attributes
     */
    public int getAttributeCount() {

        return tagAttributes.size();
    }

    /**
     * Check if the xml document row has attributes
     *
     * @return True if this xml document row has attributes
     */
    public boolean hasAttributes() {

        boolean hasAttributes = false;

        if(tagAttributes != null && tagAttributes.size() > 0) {

            hasAttributes = true;
        }

        return hasAttributes;
    }

    /**
     * Return the list of attributes associated with this xml document row
     *
     * @return List of XTrackerXmlDocumentRowAttribute
     */
    public List<XTrackerXmlDocumentRowAttribute> getAllAttributes() {

        return tagAttributes;
    }

    /**
     * Initialise the variable tagAttributes as an ArrayList
     *
     */
    private void initAttributes() {

        if(tagAttributes == null) {

            tagAttributes = new LinkedList<XTrackerXmlDocumentRowAttribute>();
        }
    }

    /**
     * Create and add a XTrackerXmlDocumentRowAttribute (attribute) to this xml document row
     *
     * @param ownerName The tag name of the xml document row the added attribute belongs to
     * @param attributeName The name of the added attribute
     * @param value The value (if any) of the added attribute
     * @param defaultVal The default value for this added attribute (if any)
     * @param isFixed True if the value of this attribute is fixed at a set value
     * @param isRequired True if the attribute is required
     */
    public void addAttribute(String ownerName,
                             String attributeName,
                             String value,
                             String defaultVal,
                             boolean isFixed,
                             boolean isRequired,
                             XTrackerGuiComponent component) {

        // make sure the variable tagAttributes has been initialised
        initAttributes();

        XTrackerXmlDocumentRowAttribute attribute = new XTrackerXmlDocumentRowAttribute(ownerName,
                                                                                        attributeName,
                                                                                        value,
                                                                                        defaultVal,
                                                                                        isFixed,
                                                                                        isRequired);
        attribute.setDisplayComponent(component);

        tagAttributes.add(attribute);
    }

     /**
     * Add an already created XTrackerXmlDocumentRowAttribute (attribute) to this xml document row
     *
     * @param attribute The XTrackerXmlDocumentRowAttribute to add
     */
    public void addAttribute(XTrackerXmlDocumentRowAttribute attribute) {

        // make sure the variable tagAttributes has been initialised
        initAttributes();
        tagAttributes.add(attribute);
    }

    public void addAttributeList(List<XTrackerXmlDocumentRowAttribute> attributeList) {

        tagAttributes = attributeList;
    }

    /**
     * Return the XTrackerXmlDocumentRowAttribute (attribute) with the name passed in parameter attributeName
     *
     * @return Return a XTrackerXmlDocumentRowAttribute
     */
    public XTrackerXmlDocumentRowAttribute getAttribute(String attributeName) {

        XTrackerXmlDocumentRowAttribute attributeFound = null;

        for(XTrackerXmlDocumentRowAttribute attr : tagAttributes) {

            if(attr.getAttributeName().equals(attributeName)) {

                attributeFound = attr;
            }
        }

        return attributeFound;
    }

    /**
     * Check if the xml document row or any attributes of the row
     * have changed since they were created
     *
     * @return True if the data value has changed
     */
    public boolean xmlDocumentRowHasEdits() {

        boolean hasEdits = false;

        if(rowData.xmlDocumentRowDataHasEdits()) {

            hasEdits = true;
        }

        if(hasEdits == false) {

            if(tagAttributes != null) {
            
                for(XTrackerXmlDocumentRowAttribute attr : tagAttributes) {

                    if(attr.xmlDocumentRowAttributeHasEdits()) {

                        hasEdits = true;
                        break;
                    }
                }
            }
        }

        return hasEdits;
    }

    /**
     * Save any changes to the xml document row including any attributes
     *
     *
     */
    public void saveXmlDocumentRowEdits() {

        rowData.saveXmlDocumentRowDataEdits();

        if(tagAttributes != null) {

            for(XTrackerXmlDocumentRowAttribute attr : tagAttributes) {

                attr.saveXmlDocumentRowAttributeEdits();
            }
        }
    }

    /**
     * Save any changes to the xml document row including any attributes
     *
     *
     */
    public void rollBackXmlDocumentRowEdits() {

        rowData.rollBackXmlDocumentRowDataEdits();

        if(tagAttributes != null) {

            for(XTrackerXmlDocumentRowAttribute attr : tagAttributes) {

                attr.rollBackXmlDocumentRowAttributeEdits();
            }
        }
    }
}
