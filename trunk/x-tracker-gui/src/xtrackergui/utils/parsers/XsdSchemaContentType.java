
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XsdSchemaContentType.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

/**
 * A enumeration class to model the different content types possible in xsd complex type schema elements/ node
 * 
 * @author andrew bullimore
 */
public enum XsdSchemaContentType {

    SIMPLETYPE("simpletype"),
    COMPLEXTYPE("complextype"),
    NOCONTENTTYPESET("nocontenttype");

    private String xsdSchemaContentType;

    /**
     * Create a xsd schema content type enumeration
     * 
     * @param contentType
     */
    XsdSchemaContentType(String contentType){

        xsdSchemaContentType = contentType;
    }

    @Override
    public String toString() {

        return xsdSchemaContentType;
    }

    /**
     * Get the xsd schema element/ node content type as a string
     *
     * @return The content type string
     */
    public String getXsdSchemaContentType() {

        return xsdSchemaContentType;
    }
}
