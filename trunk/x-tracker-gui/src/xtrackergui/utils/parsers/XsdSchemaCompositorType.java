
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XsdSchemaCompositorType.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

/**
 * A enumeration class to model the different compositor types possible in xsd schemas
 * (They are enumerations in xsd schemas but a unknown type was not available so this enumeration
 *  class exists for this reason alone - the need for NOCOMPOSITORSET)
 *
 * @author andrew bullimore
 */
public enum XsdSchemaCompositorType {

    ALL("all"),
    SEQUENCE("sequence"),
    CHOICE("choice"),
    NOCOMPOSITORSET("nocompositorset");

    private String xsdSchemaCompositortType;

    /**
     * Create a xsd schema compositor type enumeration
     *
     * @param compositorType
     */
    XsdSchemaCompositorType(String compositorType){

        xsdSchemaCompositortType = compositorType;
    }

    @Override
    public String toString() {

        return xsdSchemaCompositortType;
    }

    /**
     * Get the xsd schema element/ node compositor type as a string
     *
     * @return
     */
    public String getXsdSchemaCompositorType() {

        return xsdSchemaCompositortType;
    }
}

