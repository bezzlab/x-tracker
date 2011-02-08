
//
//    xTrackerGui
//
//    Package: xtrackergui.exceptions
//    File: XmlFileValidationFailure
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.exceptions;

/**
 *
 *
 * * @author andrew bullimore
 */
public class XmlFileValidationFailure extends Exception {

    public XmlFileValidationFailure() {

        super("file failed validation");
    }

    public XmlFileValidationFailure(String message) {

        super(message);
    }
}
