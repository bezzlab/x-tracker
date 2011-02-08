
//
//    xTrackerGui
//
//    Package: xtrackergui.exceptions
//    File: XsdSchemaFileNotSet
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.exceptions;

/**
 *
 *
 * * @author andrew bullimore
 */
public class XsdSchemaFileNotSet extends Exception {

    public XsdSchemaFileNotSet() {

        super("xsd schema file not set");
    }

    public XsdSchemaFileNotSet(String message) {

        super(message);
    }
}
