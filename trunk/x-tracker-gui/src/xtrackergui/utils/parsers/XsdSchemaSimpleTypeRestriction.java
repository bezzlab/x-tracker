
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XsdSchemaSimpleTypeRestriction.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data class to store the restrictions on a xsd schema simple type element
 * See http://www.w3schools.com/Schema/ and
 * http://www.docjar.com/docs/api/com/sun/xml/internal/xsom/parser/XSOMParser.html
 * for more information
 *
 * @author andrew bullimore
 */
public class XsdSchemaSimpleTypeRestriction {

    private List<String> enumerations = null;
    private String maxValue = "";
    private String minValue = "";
    private String length = "";
    private String maxLength = "";
    private String minLength = "";
    private String pattern = "";
    private String totalDigits = "";

    /**
     * Default constructor - create an empty XsdSchemaSimpleTypeRestriction instance
     *
     */
    public XsdSchemaSimpleTypeRestriction() {
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();
        outputString.append("\nRestrictions-" +
                              "\nMax Value: " + maxValue +
                              "\nMin Value: " + minValue +
                              "\nLength: " + length +
                              "\nMax Length: " + maxLength +
                              "\nMin Length: " + minLength +
                              "\nPattern: " + pattern +
                              "\nTotal digits: ");

        if(enumerations != null) {

            for(String enumeration : enumerations) {

                outputString.append("\nEnum: " + enumeration);
            }
        }

        outputString.append("\n");

        return outputString.toString();
    }

    /**
     * Initialise the restriction data
     *
     * @param xsRestSimpleType The restriction data
     */
    public void initXsdSchemaSimpleTypeRestriction(XSRestrictionSimpleType xsRestSimpleType) {

        List<String> facetEnumerations = new ArrayList<String>();

        Iterator<XSFacet> facetIterator = xsRestSimpleType.iterateDeclaredFacets();
        while(facetIterator.hasNext()) {

            XSFacet xsFacet = facetIterator.next();

            if(xsFacet.getName().equals(xsFacet.FACET_ENUMERATION)) {

                facetEnumerations.add(xsFacet.getValue().value);
            }

            if(xsFacet.getName().equals(xsFacet.FACET_MAXINCLUSIVE)) {

                maxValue = xsFacet.getValue().value;
            }

            if(xsFacet.getName().equals(xsFacet.FACET_MININCLUSIVE)) {

                minValue = xsFacet.getValue().value;
            }

            if(xsFacet.getName().equals(xsFacet.FACET_MAXEXCLUSIVE)) {

                maxValue = String.valueOf(Integer.parseInt(xsFacet.getValue().value) - 1);
            }

            if(xsFacet.getName().equals(xsFacet.FACET_MINEXCLUSIVE)) {

                minValue = String.valueOf(Integer.parseInt(xsFacet.getValue().value) + 1);
            }

            if(xsFacet.getName().equals(xsFacet.FACET_LENGTH)) {

                length = xsFacet.getValue().value;
            }

            if(xsFacet.getName().equals(xsFacet.FACET_MAXLENGTH)) {

                maxLength = xsFacet.getValue().value;
            }

            if(xsFacet.getName().equals(xsFacet.FACET_MINLENGTH)) {

                minLength = xsFacet.getValue().value;
            }

            if(xsFacet.getName().equals(xsFacet.FACET_PATTERN)) {

                pattern = xsFacet.getValue().value;
            }

            if(xsFacet.getName().equals(xsFacet.FACET_TOTALDIGITS)) {

                totalDigits = xsFacet.getValue().value;
            }
        }

        if(facetEnumerations.size() > 0) {

            enumerations = facetEnumerations;
            enumerations.add(0, "");
        }
    }

    /**
     * Check if an enumerated selection list exists
     *
     * @return True if an enumerated list of alternatives exists
     */
    public boolean getHasEnumerations() {

        boolean hasEnumeration = false;

        // only ever not null whne enumerations are added but
        // checking length too
        if(enumerations != null) {

            if(enumerations.size() > 0) {

                hasEnumeration = true;
            }
        }

        return hasEnumeration;
    }

    /**
     * Get enumeration data ie list of alternate choices
     *
     * @return The list of alternatives
     */
    public List<String> getEnumerations() {

        return enumerations;
    }

    /**
     * Get the max value for this xsd element
     *
     * @return The max value as a string
     */
    public String getMaxValue() {

        return maxValue;
    }

    /**
     * Get the min value for this xsd element
     *
     * @return The min value as a string
     */
    public String getMinValue() {

        return minValue;
    }

    /**
     * Get the length of the data required for this xsd element
     *
     * @return The length value as a string
     */
    public String getLength() {

        return length;
    }

    /**
     *
     *
     * @return
     */
    public String getMaxLength() {

        return maxLength;
    }

    /**
     *
     *
     * @return
     */
    public String getMinLength() {

        return minLength;
    }

    /**
     *
     *
     * @return
     */
    public String getPattern() {

        return pattern;
    }

    /**
     *
     *
     * @return
     */
    public String getTotalDigits() {

        return totalDigits;
    }
}
