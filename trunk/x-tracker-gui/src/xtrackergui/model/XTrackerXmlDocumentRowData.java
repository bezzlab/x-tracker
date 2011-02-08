
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerXmlDocumentRowData.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

/**
 * Data class to store xml data values for a particular element/ attribute
 * 
 * @author andrew bullimore
 */
public class XTrackerXmlDocumentRowData {

    String originalTagValue = "";
    String tagValue = "";
    String defaultTagValue = null;
    boolean isFixedTagValue = false;
    boolean isTagValueRequired = false;

    /**
     * Create a XTrackerXmlDocumentRowData data object
     *
     * @param value
     * @param defaultValue
     * @param isFixed
     * @param isRequired
     */
    XTrackerXmlDocumentRowData(String value,
                               String defaultValue,
                               boolean isFixed,
                               boolean isRequired) {

        if(value.equals("") && defaultValue != null) {

            originalTagValue = defaultValue;
            tagValue = defaultValue;

        } else {

            originalTagValue = value;
            tagValue = value;
        }
        
        defaultTagValue = defaultValue;
        isFixedTagValue = isFixed;
        isTagValueRequired = isRequired;
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        outputString.append("Data - " +
                            " orig value: " + originalTagValue +
                            ": curr value: " + tagValue +
                            ": fixed: " + isFixedTagValue +
                            ": required: " + isTagValueRequired);

        if(defaultTagValue != null) {

            outputString.append(": default value: " + defaultTagValue);
        }

        outputString.append('\n');

        return outputString.toString();
    }

    /**
     * Return the original value set
     *
     * @return The original tag value (the starting value this tag was set as and the tag value after a save has been performed)
     */
    public String getOriginalTagValue() {

        return originalTagValue;
    }

    /**
     * Set the original tag value
     *
     * @param value The value to set the original tag value to
     */
    public void setOriginalTagValue(String value) {

        if(isFixedTagValue == false) {

            if(value.equals("") && defaultTagValue != null) {

                originalTagValue = defaultTagValue;

            } else {

                originalTagValue = value;
            }
        }
    }

    /**
     * Return the data value for this tag
     *
     * @param The data value for this tag
     */
    public String getTagValue() {

        return tagValue;
    }

    /**
     * Set the data value for this tag
     *
     * @param value The data value to set this tag to
     */
    public void setTagValue(String value) {

        if(isFixedTagValue == false) {

            if(value.equals("") && defaultTagValue != null) {

                tagValue = defaultTagValue;
                
            } else {

                tagValue = value;
            }
        }
    }

    public void resetTagValue() {

        tagValue = originalTagValue;
    }

    /**
     * Return the default value this instance was created with
     *
     * @return The default tag value
     */
    public String getDefaultTagValue() {

        return defaultTagValue;
    }

    /**
     * Return if the data value stored is fixed at a set value
     *
     * @return true if the data value is fixed
     */
    public boolean getIsFixedTagValue() {

        return isFixedTagValue;
    }

    /**
     * Return if the data value stored is required/ mandatory
     *
     * @return true if the data value is required/ mandatory
     */
    public boolean getIsTagValueRequired() {

        return isTagValueRequired;
    }

    /**
     * Check if the data value has been updated since it was created
     *
     * @return True if the data value has unsaved edits
     */
    public boolean xmlDocumentRowDataHasEdits() {

        boolean hasEdits = false;

        if(isFixedTagValue == false) {

          //  if(tagValue.equals("") == false &&
            if(tagValue.equals(originalTagValue) == false) {

                hasEdits = true;
            }
        }

        return hasEdits;
    }

    /**
     * Save any changes to the data value
     *
     */
    public void saveXmlDocumentRowDataEdits() {

        if(xmlDocumentRowDataHasEdits()) {

            originalTagValue = tagValue;
        }
    }

    /**
     * Save any changes to the data value
     *
     */
    public void rollBackXmlDocumentRowDataEdits() {

            tagValue = originalTagValue;
    }
}
