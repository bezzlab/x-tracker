
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XsdSchemaNode.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author andrew bullimore
 */
public class XsdSchemaNode {

    // Not the best or correct OO way to model XSD schemas but trying to get things going
    // at the moment

    public enum XsdSchemaNodeType {

        ROOTNODE("rootnode"),
        COMPLEXNODE("complexnode"),
        ELEMENTNODE("elementnode"),
        ATTRIBUTENODE("attributenode"),
        NONODETYPESET("nonodetypeset");
        private String xsdSchemaNodeType;

        /**
         *
         * @param nodeType
         */
        XsdSchemaNodeType(String nodeType) {

            xsdSchemaNodeType = nodeType;
        }

        @Override
        public String toString() {

            return xsdSchemaNodeType;
        }

        /**
         *
         * @return
         */
        public String getXsdSchemaNodeType() {

            return xsdSchemaNodeType;
        }
    }

    private XsdSchemaNodeType nodeType = XsdSchemaNodeType.NONODETYPESET;
    private XsdSchemaContentType contentType = XsdSchemaContentType.NOCONTENTTYPESET;
    // only relavant for complex type content - not distinguishing really between
    // ALL and SEQUENCE currently
    private XsdSchemaCompositorType compositorType = XsdSchemaCompositorType.NOCOMPOSITORSET;
    private String nodeName = "";
    private String nodeTypeDefAsString = "";
    private XsdPrimativeDataType primativeDataType = XsdPrimativeDataType.UNKNOWN;
    private String fixedValue = null;
    private String defaultValue = null;
    private boolean isAttributeRequired = false;
    private int minOccurs;
    private int maxOccurs;

    private String ancestryPath = "";

    // if this node has attributes
    private List<XsdSchemaNode> attributes = null;
    private XsdSchemaSimpleTypeRestriction simpleTypeRestrictions = null;

    /**
     *
     *
     */
    public XsdSchemaNode() {
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        outputString.append("\nNode Details -" +
                            "\nNode Type: " + nodeType.getXsdSchemaNodeType() +
                            "\nContent Type: " + contentType.getXsdSchemaContentType() +
                            "\nCompositor: " + compositorType.getXsdSchemaCompositorType() +
                            "\nNode name " + nodeName +
                            "\nPrimative Data Type: " + primativeDataType +
                            "\nNode Type Def " + nodeTypeDefAsString);

        if(fixedValue != null) {

             outputString.append("\nFixed value: " + fixedValue);
        }

        if(defaultValue != null) {

            outputString.append("\nDefault value: " + defaultValue);
        }
                      
        outputString.append("\nAttribute Required: " + isAttributeRequired +
                            "\nMin Occurs: " + minOccurs +
                            "\nMax Occurs " + maxOccurs +
                            "\nAncestry " + ancestryPath + "\n");

        if(simpleTypeRestrictions != null) {

            outputString.append(simpleTypeRestrictions);
        }

        if(attributes != null) {

            outputString.append("Number Attributes: " + attributes.size());

            for(XsdSchemaNode attr : attributes) {

                outputString.append(attr);
            }
        }

        return outputString.toString();
    }

    /**
     *
     *
     */
    public void initAttributesList() {

        if(attributes == null) {

            attributes = new ArrayList<XsdSchemaNode>();
        }
    }

    /**
     *
     *
     */
    public String getAncestryPath() {

        return ancestryPath;
    }

    /**
     *
     *
     */
    public void setAncestryPath(String path) {

        ancestryPath = path;
    }

    /**
     *
     *
     */
    public XsdSchemaNodeType getNodeType() {

        return nodeType;
    }

    /**
     *
     *
     */
    public void setNodeType(XsdSchemaNodeType elemType) {

        nodeType = elemType;
    }

    /**
     *
     *
     */
    public XsdSchemaContentType getContentType() {

        return contentType;
    }

    /**
     *
     *
     */
    public void setContentType(XsdSchemaContentType conType) {

        contentType = conType;
    }

    /**
     *
     *
     */
    public XsdSchemaCompositorType getCompositorType() {

        return compositorType;
    }

    /**
     *
     *
     */
    public void setCompositorType(XsdSchemaCompositorType compType) {

        compositorType = compType;
    }

    /**
     *
     *
     */
    public String getNodeName() {

        return nodeName;
    }

    /**
     *
     *
     */
    public void setNodeName(String name) {

        nodeName = name;
    }

    /**
     *
     *
     */
    public XsdPrimativeDataType getPrimDataType() {

        return primativeDataType;
    }

    /**
     *
     *
     */
    public void setPrimDataType(XsdPrimativeDataType primataType) {

        primativeDataType = primataType;
    }

    /**
     *
     *
     */
    public String getFixedValue() {

        return fixedValue;
    }

    /**
     *
     *
     */
    public void setFixedValue(String value) {

        fixedValue = value;
    }

    /**
     *
     *
     */
    public String getDefaultValue() {

        return defaultValue;
    }

    /**
     *
     *
     */
    public void setDefaultValue(String value) {

        defaultValue = value;
    }

    /**
     *
     *
     */
    public boolean getIsAttributeRequired() {

        return isAttributeRequired;
    }

    /**
     *
     *
     */
    public void setIsAttributeRequired(boolean required) {

        isAttributeRequired = true;
    }

    /**
     *
     *
     */
    public String getNodeTypeAsString() {

        return nodeTypeDefAsString;
    }

    /**
     *
     *
     */
    public void setNodeTypeAsString(String nodeTypeAsString) {

        nodeTypeDefAsString = nodeTypeAsString;
    }

    /**
     *
     *
     */
    public int getMaxOccurs() {

        return maxOccurs;
    }

    /**
     *
     *
     */
    public void setMaxOccurs(int maxOcurrance) {

        maxOccurs = maxOcurrance;
    }

    /**
     *
     *
     */
    public int getMinOccurs() {

        return minOccurs;
    }

    /**
     *
     *
     */
    public void setMinoccurs(int minOccurance) {

        minOccurs = minOccurance;
    }

    /**
     *
     *
     */
    public XsdSchemaSimpleTypeRestriction getSimpleTypeRestrictions() {

        return simpleTypeRestrictions;
    }

    /**
     *
     *
     */
    public void setSimpleTypeRestrictions(XsdSchemaSimpleTypeRestriction restriction) {

        simpleTypeRestrictions = restriction;
    }

    /**
     *
     *
     */
    public List<XsdSchemaNode> getAttributes() {

        return attributes;
    }

    /**
     *
     *
     */
    public void setAttribute(XsdSchemaNode attr) {

        attributes.add(attr);
    }

    /**
     *
     *
     */
    public int getNumberOfAttributes() {

        int numAttributes = 0;

        if(attributes != null) {

            numAttributes = attributes.size();
        }

        return numAttributes;
    }
}
