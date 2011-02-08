
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerXmlDocument.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import xtrackergui.gui.XTrackerGuiComponent;
import xtrackergui.gui.XTrackerGuiComponentFactory;
import xtrackergui.utils.parsers.XmlParser;
import xtrackergui.utils.parsers.XsdSchemaContentType;
import xtrackergui.utils.parsers.XsdSchemaNode;
import xtrackergui.utils.parsers.XsdSchemaSimpleTypeRestriction;

/**
 * A Data control class to coordinate and model the information required to populate the data in a xml document
 *
 * @author andrew bullimore
 */
public class XTrackerXmlDocument {

    String pluginConfigModelName = null;
    String fileName = null;
    File xmlFile = null;
    // The xsd schema element/ node map - a description of each element/ node
    // described in the xsd file
    List<XsdSchemaNode> xsdSchemaNodesMap = null;
    // The rows of information that will form the resulting xml document
    List<XTrackerXmlDocumentRow> xmlDocumentRows = null;
    // Elements/ nodes may be nested in the xsd schema file (and resultant xml file)
    // Only the top level element/ node is retained in this Set
    Set<String> rationalisedReapeatGroupNameList = null;
    //
    Set<String> processedRepeatGroupsList = null;
    // A map keyed on the name of each element/ node described in the xsd file
    // with the value a XTrackerXmlDocumentRowRepeatGroup object which stores
    // information about the number of occrances of the element/ node
    Map<String, XTrackerXmlDocumentRowRepeatGroup> repeatGroups = null;

    /**
     * Constructor, creats an empty XTrackerXmlDocument with no rows of data for a given XTrackerPluginModel
     *
     * @param modelName The name of the XTrackerPluginModel this xml document belongs to
     */
    public XTrackerXmlDocument(String modelName) {

        pluginConfigModelName = modelName;
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        if(xmlDocumentRows != null) {

            for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                 outputString.append(row);
            }
        }

        return outputString.toString();
    }

    /**
     * Initialise the xml document to accept XTrackerXmlDocumentRow data
     *
     * @param xsdSchemaMap The parsed xsd schema map
     */
    public void initXmlDocument(List<XsdSchemaNode> xsdSchemaMap) {

        if(xmlDocumentRows == null) {

            xsdSchemaNodesMap = xsdSchemaMap;
        }

        if(xmlDocumentRows == null) {

            // LinkedList - order by index position
            xmlDocumentRows = new LinkedList<XTrackerXmlDocumentRow>();
        }

        if(rationalisedReapeatGroupNameList == null) {

            // LinkedHashSet - insertion order is retained
            rationalisedReapeatGroupNameList = new LinkedHashSet<String>();
        }

        if(processedRepeatGroupsList == null) {

            // LinkedHashSet - insertion order is retained
            processedRepeatGroupsList = new LinkedHashSet<String>();
        }

        if(repeatGroups == null) {

            // LinkedHashMap - insertion order is retained & fast iteration
            repeatGroups = new LinkedHashMap<String, XTrackerXmlDocumentRowRepeatGroup>();
        }

        // Initialise the xml document with the number of possible occurances of each
        // xsd element/ node type to control the xml document rows resulting governed
        // by the element/ node types
        createRepeatGroups();
        allocateNodesToRepeatGroups();
    }

    /**
     * Build the XTrackerXmlDocument
     *
     */
    public void buildXmlDocumentFromXsdSchema() {

        // populate the root/ document element/ node
        XsdSchemaNode rootNode = getXsdSchemaNodeByType(XsdSchemaNode.XsdSchemaNodeType.ROOTNODE);
        processXsdSchemaNode(rootNode);

        if(rationalisedReapeatGroupNameList != null) {

            // Iterate over each 'top level' element/ node ie immedately below the root/ document element
            // Retrieve the element/ node occurance number
            for(String groupName : rationalisedReapeatGroupNameList) {

                processRepeatGroup(groupName);
            }
        }
    }

    /**
     *
     *
     */
    public void buildXmlDocumentFromXmlParametersFile(File xmlParametersFile) {

        if(xmlParametersFile != null) {

            Document document = XmlParser.getDomDocumentForXmlFile(xmlParametersFile);
            document.getDocumentElement().normalize();

            if(document != null) {

                if(xsdSchemaNodesMap != null) {

                    // populate the root/ document element/ node
                    XsdSchemaNode rootNode = getXsdSchemaNodeByType(XsdSchemaNode.XsdSchemaNodeType.ROOTNODE);
                    XTrackerXmlDocumentRow row = createXmlDocumentRow(rootNode, "");
                    xmlDocumentRows.add(row);

                    // Iterate over each 'top level' element/ node ie immedately below the root/ document element
                    // Retrieve the element/ node occurance number
                    for(String groupName : rationalisedReapeatGroupNameList) {
                    
                        readNode(document, groupName);
                    }
                }

             //   updateRepeatGroupOccuranceCount();
            }
        }     
    }

    public void updateRepeatGroupOccuranceCount() {

    /*    for(Map.Entry<String, XTrackerXmlDocumentRowRepeatGroup> entry : repeatGroups.entrySet()) {

            XTrackerXmlDocumentRowRepeatGroup repeatGroup = entry.getValue();
            if((repeatGroup.getRepeatGroupType() == XTrackerXmlDocumentRowRepeatGroup.RepeatGroupType.MULTIPLEUNBOUNDED) ||
               (repeatGroup.getRepeatGroupType() == XTrackerXmlDocumentRowRepeatGroup.RepeatGroupType.MULTIPLESETRANGE)) {

                int occurCount = 0;
                String repeatGroupParent = entry.getKey();
                int repeatGroupRefNum = 0;

                for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                    if(row.getTagName().equals(repeatGroupParent) == true) {

                        repeatGroupRefNum++;
                        row.setGroupRef(repeatGroupRefNum);
                        occurCount++;
                    }

                    if(repeatGroup.checkNodeNameIsInRepeatGroup(row.getTagName()) == true) {

                        row.setGroupRef(repeatGroupRefNum);
                    }
                } */

            //    entry.getValue().setCurrentNumberOfOccurances(occurCount);

            //    System.out.println("Rep Group " + repeatGroupParent + ' ' + " occurs " + occurCount);
      //      }
      //  }

        if(rationalisedReapeatGroupNameList != null) {

            // Iterate over each 'top level' element/ node ie immedately below the root/ document element
            // Retrieve the element/ node occurance number
            for(String groupName : rationalisedReapeatGroupNameList) {

                processsRepeatGroup(groupName, 0);
            }
        }
    }

    /**
     *
     *
     * @param groupParentName
     */
    private void processsRepeatGroup(String groupParentName, int num) {

        XTrackerXmlDocumentRowRepeatGroup group = repeatGroups.get(groupParentName);

        if((group.getRepeatGroupType() == XTrackerXmlDocumentRowRepeatGroup.RepeatGroupType.MULTIPLEUNBOUNDED) ||
           (group.getRepeatGroupType() == XTrackerXmlDocumentRowRepeatGroup.RepeatGroupType.MULTIPLESETRANGE)) {

            for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                int repeatGroupRefNum = num;
                if(row.getTagName().equals(groupParentName) == true) {

                    repeatGroupRefNum++;
                    row.setGroupRef(repeatGroupRefNum);
                    processRepeatsGroupMembers(group.getGroupMembers(), repeatGroupRefNum);
                }



                if(group.checkNodeNameIsInRepeatGroup(row.getTagName()) == true) {

                    row.setGroupRef(repeatGroupRefNum);
                }
            }
        }
    }

    /**
     * Process the xsdSchemaNode data for each member of the repeat group (XTrackerXmlDocumentRowRepeatGroup)
     *
     * @param groupMembers The child element/ nodes of the repeat group parent element/ node
     */
    private void processRepeatsGroupMembers(List<String> groupMembers, int num) {

        processedRepeatGroupsList.clear();
        for(String member : groupMembers) {

            if(processedRepeatGroupsList.contains(member) == false) {

                processsRepeatGroup(member, num);

                processedRepeatGroupsList.add(member);
            }
        }
    }

    /**
     *
     *
     */
    public List<XTrackerXmlDocumentRow> getXTrackerXmlDocument() {

        return xmlDocumentRows;
    }

    /**
     * Return the name of the XTrackerPluginModel this xml document belongs to
     *
     * @return The name of this xml document plugin model
     */
    public String getXTrackerXmlDocumentName() {

        return pluginConfigModelName;
    }

    /**
     * Add a XTrackerXmlDocumentRow object
     *
     * @param row The XTrackerXmlDocumentRow object to add
     */
    public void addXmlDocumentRow(XTrackerXmlDocumentRow row) {

        // Check variable xmlDocumentRows is initialised
        if(xmlDocumentRows == null) {

            xmlDocumentRows.add(row);
        }
    }

    /**
     * Check if this xml document has any rows of data
     *
     * @return True if the xml document is populated
     */
    public boolean xmlDocumentHasContent() {

        boolean hasContent = false;

        if(xmlDocumentRows != null && xmlDocumentRows.size() > 0) {

            hasContent = true;
        }

        return hasContent;
    }

    /**
     * Return the total number of rows in this document - data rows and section label rows
     *
     * @return The total number of rows
     */
    public int getXmlDocumentRowCount() {

        int numberOfRows = 0;

        if(xmlDocumentRows != null) {

            numberOfRows = xmlDocumentRows.size();
        }

        return numberOfRows;
    }

    /**
     * Check if the xml document has changed since it was created
     *
     * @return True if the data value has changed
     */
    public boolean xmlDocumentHasEdits() {

        boolean hasEdits = false;

        if(xmlDocumentRows != null) {

            for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                if(row.xmlDocumentRowHasEdits()) {

                    // stop as soon as we find a unsaved change
                    hasEdits = true;
                    break;
                }
            }
        }

        return hasEdits;
    }

    /**
     * Save any changes to the xml document
     *
     */
    public void saveXmlDocumentEdits() {

        if(xmlDocumentRows != null) {

            for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                row.saveXmlDocumentRowEdits();
            }
        }
    }

    /**
     * Save any changes to the xml document
     *
     */
    public void rollBackXmlDocumentEdits() {

        if(xmlDocumentRows != null) {

            for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                if(row.xmlDocumentRowHasEdits()) {

                    row.rollBackXmlDocumentRowEdits();
                }
            }
        }
    }

    /**
     *
     *
     */
    public void refreshXmlDocument() {

        if(xmlDocumentRows != null) {

            for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                row.refreshTagValue();
            }
        }
    }

    /**
     * Find the index of the last XTrackerXmlDocumentRow for a given parent element/ node
     * NB if the node is of complextype with complex content then it will have nested
     * elements node and it is the last of these that this function is designed to find
     *
     * @param tagName The name of the parent element/ node to search for
     * @return The index of the last child element node of the parent node
     */
    public int getRepeatGroupInsertionIndex(String parentTagName) {

        int index = -1;

        if(xmlDocumentRows != null) {

            index = xmlDocumentRows.size();
            ListIterator xmlDocumentRowsIterator = xmlDocumentRows.listIterator(index);
            while(xmlDocumentRowsIterator.hasPrevious()) {

                index--;
                XTrackerXmlDocumentRow row = (XTrackerXmlDocumentRow) xmlDocumentRowsIterator.previous();
                if(row.getParentTagName().equals(parentTagName) == true) {

                    break;
                }
            }
        }

        return index;
    }

    /**
     * Find the index of the last XTrackerXmlDocumentRow for a given parent element/ node
     * NB if the node is of complextype with complex content then it will have nested
     * elements node and it is the last of these that this function is designed to find
     *
     * @param tagName The name of the element/ node to search for
     * @return The index of the last element/ node with this tag name
     */
    public int getLastXmlRowIndexForTagName(String tagName) {

        int index = -1;

        if(xmlDocumentRows != null) {

            index = xmlDocumentRows.size();
            ListIterator xmlDocumentRowsIterator = xmlDocumentRows.listIterator(index);
            while(xmlDocumentRowsIterator.hasPrevious()) {

                index--;
                XTrackerXmlDocumentRow row = (XTrackerXmlDocumentRow) xmlDocumentRowsIterator.previous();
                if(row.getTagName().equals(tagName) == true) {

                    break;
                }
            }
        }

        return index;
    }

    /**
     *
     *
     */
    public void readNode(Document document, String tagName) {

        NodeList nodes = document.getElementsByTagName(tagName);
        int numNodes = (nodes != null) ? nodes.getLength() : 0;
        for(int i = 0; i < numNodes; i++) {

            Node node = nodes.item(i);
            switch(node.getNodeType()) {

                case Node.ELEMENT_NODE: {
                    XsdSchemaNode xsdNode = getXsdSchemaNodeByName(node.getNodeName());
                    readElementNode(xsdNode, node);
                    break;
                }
                default:{
                }
            }
        }
    }

    /**
     * Return a repeatGroup (The occurance information for a given element/ node)
     *
     * @param name The name of the element/ node
     * @return The XTrackerXmlDocumentRowRepeatGroup
     */
    public XTrackerXmlDocumentRowRepeatGroup getRepeatGroup(String name) {
        
        return repeatGroups.get(name);
    }

    /**
     *
     *
     *
     */
    public Document createDomDocument(String xsdFileName) {

        Document document = XmlParser.getEmptyDomDocument();

        if(document != null) {

            if(xmlDocumentRows != null) {

                Element rootElement = null;
                List<Element> sectionStartTags = new LinkedList<Element>();

                for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                    if(row.isDocumentRootElement()) {

                        rootElement = document.createElement(row.getTagName());
                        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                        rootElement.setAttribute("xsi:noNamespaceSchemaLocation", xsdFileName);
                        document.appendChild(rootElement);
                        sectionStartTags.add(rootElement);

                    } else {

                        if(rootElement != null) {

                            int index = sectionStartTags.size();
                            ListIterator sectionStartTagsIterator = sectionStartTags.listIterator(index);
                            while(sectionStartTagsIterator.hasPrevious()) {
                                
                                Element elem = (Element) sectionStartTagsIterator.previous();


                         //   for(Element elem : sectionStartTags) {

                                if(elem.getTagName().equals(row.getParentTagName())) {

                                    Element xmlElement = document.createElement(row.getTagName());

                                    if(row.hasAttributes()) {
                                        
                                        List<XTrackerXmlDocumentRowAttribute> rowAttributes = row.getAllAttributes();
                                        for(XTrackerXmlDocumentRowAttribute attribute : rowAttributes) {

                                            xmlElement.setAttribute(attribute.getAttributeName(), attribute.getAttributeValue());
                                        }
                                    }

                                    if(row.isSectionStartTag()) {

                                        sectionStartTags.add(xmlElement);

                                    } else {

                                        Text textNode = document.createTextNode(row.getTagValue());
                                        xmlElement.appendChild(textNode);
                                    }
                                    
                                    elem.appendChild(xmlElement);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return document;
    }

    /**
     *
     * 
     * @param groupParentName
     */
    private void processRepeatGroup(String groupParentName) {

        XTrackerXmlDocumentRowRepeatGroup group = repeatGroups.get(groupParentName);

        switch(group.getRepeatGroupType()) {

            case SINGLE: {

                XsdSchemaNode node = getXsdSchemaNodeByName(group.getRepeatGroupParent());
                processXsdSchemaNode(node);

                processRepeatGroupMembers(group.getGroupMembers());
                break;
            }
            case MULTIPLESETNUMBER: {

                for(int i = 0; i < group.getMinOccurs(); i++) {

                    XsdSchemaNode node = getXsdSchemaNodeByName(group.getRepeatGroupParent());
                    processXsdSchemaNode(node);

                    processRepeatGroupMembers(group.getGroupMembers());
                }
                break;
            }
            case MULTIPLESETRANGE: {

                for(int i = 0; i < group.getMaxOccurs(); i++) {

                    XsdSchemaNode node = getXsdSchemaNodeByName(group.getRepeatGroupParent());
                    processXsdSchemaNode(node);

                    processRepeatGroupMembers(group.getGroupMembers());
                }
                break;
            }
            case MULTIPLEUNBOUNDED: {

                XsdSchemaNode node = getXsdSchemaNodeByName(group.getRepeatGroupParent());
                processXsdSchemaNode(node);

                processRepeatGroupMembers(group.getGroupMembers());
                break;
            }
        }
    }

    /**
     * Process the xsdSchemaNode data for each member of the repeat group (XTrackerXmlDocumentRowRepeatGroup)
     *
     * @param groupMembers The child element/ nodes of the repeat group parent element/ node
     */
    private void processRepeatGroupMembers(List<String> groupMembers) {

        processedRepeatGroupsList.clear();
        for(String member : groupMembers) {

            if(processedRepeatGroupsList.contains(member) == false) {
            
                processRepeatGroup(member);

                processedRepeatGroupsList.add(member);
            }
        }
    }

    /**
     * Create XTrackerXmlDocumentRowRepeatGroup from the xsd schema node (XsdSchemaNode) information
     *
     */
    private void createRepeatGroups() {

        // Make sure the xsd schema node map is populated
        if(xsdSchemaNodesMap != null) {

            for(XsdSchemaNode node : xsdSchemaNodesMap) {

                int minOccurs = node.getMinOccurs();
                int maxOccurs = node.getMaxOccurs();

                if(node.getNodeType() != XsdSchemaNode.XsdSchemaNodeType.ROOTNODE) {

                    XTrackerXmlDocumentRowRepeatGroup repeatGroup = new XTrackerXmlDocumentRowRepeatGroup(node.getNodeName(),
                                                                                                          minOccurs,
                                                                                                          maxOccurs);

                    repeatGroups.put(node.getNodeName(), repeatGroup);
                    rationalisedReapeatGroupNameList.add(node.getNodeName());
                }
            }
        }
    }

    /**
     * Allocate xsd nodes/ elements to the correct XTrackerXmlDocumentRowRepeatGroup repeat group
     *
     */
    private void allocateNodesToRepeatGroups() {

        // check just in case
        if(repeatGroups != null) {

            for(Map.Entry<String, XTrackerXmlDocumentRowRepeatGroup> entry : repeatGroups.entrySet()) {

                XTrackerXmlDocumentRowRepeatGroup group = entry.getValue();

                String repeatGroupParent = entry.getKey();

                for(XsdSchemaNode node : xsdSchemaNodesMap) {

                    if(node.getNodeName().equals(repeatGroupParent) == false) {

                        String ancestryPath = node.getAncestryPath();

                        String[] nodesNamesOnAncestryPath = ancestryPath.split(":");

                        if(nodesNamesOnAncestryPath.length > 1) {

                            List<String> nodesOnPath = Arrays.asList(nodesNamesOnAncestryPath);
                            if(nodesOnPath.contains(repeatGroupParent)) {

                                 group.addNodeNameToRepeatGroup(node.getNodeName());
                                 rationalisedReapeatGroupNameList.remove(node.getNodeName());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Return the XsdSchemaNode of this XsdSchemaNode.XsdSchemaNodeType type
     *
     * @param type The XsdSchemaNode.XsdSchemaNodeType type
     */
    private XsdSchemaNode getXsdSchemaNodeByType(XsdSchemaNode.XsdSchemaNodeType type) {

        XsdSchemaNode nodeFound = null;

        for(XsdSchemaNode node : xsdSchemaNodesMap) {

            if(node.getNodeType() == type) {

                nodeFound = node;
                break;
            }
        }

        return nodeFound;
    }

    /**
     * Return the XsdSchemaNode with the given name
     *
     * @param nodeName The name of the XsdSchemaNode to retrieve
     */
    private XsdSchemaNode getXsdSchemaNodeByName(String nodeName) {

        XsdSchemaNode nodeFound = null;

        for(XsdSchemaNode node : xsdSchemaNodesMap) {

            if(node.getNodeName().equals(nodeName)) {

                nodeFound = node;
                break;
            }
        }

        return nodeFound;
    }

    /**
     *
     *
     * @param
     */
    private boolean doesStringContainSubString(String targetString, String testSubString) {

        boolean testSubStringFound = false;

        if(targetString.toLowerCase().contains(testSubString.toLowerCase())) {

            testSubStringFound = true;
        }
        
        return testSubStringFound;
    }

    /**
     *
     *
     */
    private void readElementNode(XsdSchemaNode xsdNode, Node elementNode) {

        XTrackerXmlDocumentRow row = createXmlDocumentRow(xsdNode, "");
        List<XTrackerXmlDocumentRowAttribute> attributeList = null;

        NamedNodeMap attributes = elementNode.getAttributes();
        int attrCount = (attributes != null) ? attributes.getLength() : 0;
        if(attrCount > 0) {

            attributeList = new ArrayList<XTrackerXmlDocumentRowAttribute>();

            for(int i = 0; i < attrCount; i++) {

                Node attr = attributes.item(i);
                XTrackerXmlDocumentRowAttribute attribute = creatXmlDocumetRowAttribute(row,
                                                                                        xsdNode,
                                                                                        attr.getNodeName(),
                                                                                        attr.getNodeValue());

                attributeList.add(attribute);
            }
        }

        if(attributeList != null) {

            row.addAttributeList(attributeList);
            row.initialiseAttributeList();
        }
        xmlDocumentRows.add(row);

        NodeList children = elementNode.getChildNodes();
        int len = (children != null) ? children.getLength() : 0;
        for (int i = 0; i < len; i++) {

            Node node = children.item(i);
            switch (node.getNodeType()) {

                case Node.TEXT_NODE: {

                    String value = node.getNodeValue().trim();
                    if(value != null && value.equals("") == false) {

                        row.initialiseTagValue(value);
                    }
                    break;
                }
                case Node.ELEMENT_NODE: {
                    XsdSchemaNode xsdSchemaNode = getXsdSchemaNodeByName(node.getNodeName());
                    readElementNode(xsdSchemaNode, node);
                    break;
                }
                default: {
                }
            }
        }
    }

    private XTrackerXmlDocumentRow createXmlDocumentRow(XsdSchemaNode node, String value) {

        XTrackerXmlDocumentRow xTrackerXmlDocumentRow = null;

        XTrackerGuiComponentFactory guiComponentFactory =  XTrackerGuiComponentFactory.createInstance();
        String ancestryPath = node.getAncestryPath();
        String[] nodesNamesOnAncestryPath = ancestryPath.split(":");
        String parentNode = "";

        if(nodesNamesOnAncestryPath.length > 1) {

            parentNode = nodesNamesOnAncestryPath[(nodesNamesOnAncestryPath.length - 1) - 1];
        }

        if(node.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.ROOTNODE) {

            xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(true,
                                                                true,
                                                                nodesNamesOnAncestryPath[0], // to be sure
                                                                node.getNodeName(),
                                                                1,
                                                                1,
                                                                "",
                                                                null,
                                                                false);

            xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                   node.getNodeType(),
                                                                                                   false,
                                                                                                   false,
                                                                                                   node.getNodeName(),
                                                                                                   node.getPrimDataType(),
                                                                                                   null));
        } else {

            boolean isFixed = false;
            String fixedValue = node.getFixedValue();
            if(fixedValue != null) {

                isFixed = true;
                value = fixedValue;
            }

            boolean isFileData = doesStringContainSubString(node.getNodeName(), "file");

            int minOccurs = node.getMinOccurs();
            int maxOccurs = node.getMaxOccurs();

            if(node.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.COMPLEXNODE) {

                if(node.getContentType() == XsdSchemaContentType.COMPLEXTYPE) {

                    xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(false,
                                                                        true,
                                                                        parentNode,
                                                                        node.getNodeName(),
                                                                        minOccurs,
                                                                        maxOccurs,
                                                                        "",
                                                                        null,
                                                                        false);

                    xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                           node.getNodeType(),
                                                                                                           true,
                                                                                                           false,
                                                                                                           node.getNodeName(),
                                                                                                           node.getPrimDataType(),
                                                                                                           null));

                } else if(node.getContentType() == XsdSchemaContentType.SIMPLETYPE) {

                    xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(false,
                                                                        false,
                                                                        parentNode,
                                                                        node.getNodeName(),
                                                                        minOccurs,
                                                                        maxOccurs,
                                                                        value,
                                                                        node.getDefaultValue(),
                                                                        isFixed);

                    XsdSchemaSimpleTypeRestriction restrictions = node.getSimpleTypeRestrictions();
                    xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                           node.getNodeType(),
                                                                                                           false,
                                                                                                           isFileData,
                                                                                                           node.getNodeName(),
                                                                                                           node.getPrimDataType(),
                                                                                                           restrictions));
                }

            } else if(node.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.ELEMENTNODE) {

                xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(false,
                                                                    false,
                                                                    parentNode,
                                                                    node.getNodeName(),
                                                                    minOccurs,
                                                                    maxOccurs,
                                                                    value,
                                                                    node.getDefaultValue(),
                                                                    isFixed);

                XsdSchemaSimpleTypeRestriction restrictions = node.getSimpleTypeRestrictions();
                xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                       node.getNodeType(),
                                                                                                       false,
                                                                                                       isFileData,
                                                                                                       node.getNodeName(),
                                                                                                       node.getPrimDataType(),
                                                                                                       restrictions));
            }
        }

        return xTrackerXmlDocumentRow;
    }

    /**
     * For the given node create XTrackerXmlDocumentRow objects for the
     * modelled xsd element/ node
     *
     * @param node The schema node to process
     */
    private void processXsdSchemaNode(XsdSchemaNode node) {

        XTrackerGuiComponentFactory guiComponentFactory =  XTrackerGuiComponentFactory.createInstance();
        String ancestryPath = node.getAncestryPath();
        String[] nodesNamesOnAncestryPath = ancestryPath.split(":");
        String parentNode = "";

        if(nodesNamesOnAncestryPath.length > 1) {

            parentNode = nodesNamesOnAncestryPath[(nodesNamesOnAncestryPath.length - 1) - 1];
        }

        if(node.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.ROOTNODE) {

            XTrackerXmlDocumentRow xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(true,
                                                                                       true,
                                                                                       nodesNamesOnAncestryPath[0], // to be sure
                                                                                       node.getNodeName(),
                                                                                       1,
                                                                                       1,
                                                                                       "",
                                                                                       null,
                                                                                       false);

            xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                   node.getNodeType(),
                                                                                                   false,
                                                                                                   false,
                                                                                                   node.getNodeName(),
                                                                                                   node.getPrimDataType(),
                                                                                                   null));

            xmlDocumentRows.add(xTrackerXmlDocumentRow);

        } else {

            String value = "";
            boolean isFixed = false;
            String fixedValue = node.getFixedValue();
            if(fixedValue != null) {

                isFixed = true;
                value = fixedValue;
            }

            boolean isFileData = doesStringContainSubString(node.getNodeName(), "file");

            int minOccurs = node.getMinOccurs();
            int maxOccurs = node.getMaxOccurs();

            if(node.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.COMPLEXNODE) {

                if(node.getContentType() == XsdSchemaContentType.COMPLEXTYPE) {

                    XTrackerXmlDocumentRow xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(false,
                                                                                               true,
                                                                                               parentNode,
                                                                                               node.getNodeName(),
                                                                                               minOccurs,
                                                                                               maxOccurs,
                                                                                               "",
                                                                                               null,
                                                                                               false);

                    xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                           node.getNodeType(),
                                                                                                           true,
                                                                                                           false,
                                                                                                           node.getNodeName(),
                                                                                                           node.getPrimDataType(),
                                                                                                           null));

                    loadAttributes(node, xTrackerXmlDocumentRow);
                    xmlDocumentRows.add(xTrackerXmlDocumentRow);

                } else if(node.getContentType() == XsdSchemaContentType.SIMPLETYPE) {

                    XTrackerXmlDocumentRow xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(false,
                                                                                               false,
                                                                                               parentNode,
                                                                                               node.getNodeName(),
                                                                                               minOccurs,
                                                                                               maxOccurs,
                                                                                               value,
                                                                                               node.getDefaultValue(),
                                                                                               isFixed);

                    XsdSchemaSimpleTypeRestriction restrictions = node.getSimpleTypeRestrictions();
                    xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                           node.getNodeType(),
                                                                                                           false,
                                                                                                           isFileData,
                                                                                                           node.getNodeName(),
                                                                                                           node.getPrimDataType(),
                                                                                                           restrictions));

                    loadAttributes(node, xTrackerXmlDocumentRow);
                    xmlDocumentRows.add(xTrackerXmlDocumentRow);
                }

            } else if(node.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.ELEMENTNODE) {

                XTrackerXmlDocumentRow xTrackerXmlDocumentRow = new XTrackerXmlDocumentRow(false,
                                                                                           false,
                                                                                           parentNode,
                                                                                           node.getNodeName(),
                                                                                           minOccurs,
                                                                                           maxOccurs,
                                                                                           value,
                                                                                           node.getDefaultValue(),
                                                                                           isFixed);

                XsdSchemaSimpleTypeRestriction restrictions = node.getSimpleTypeRestrictions();
                xTrackerXmlDocumentRow.setDisplayComponent(guiComponentFactory.getXTrackerGuiComponent(xTrackerXmlDocumentRow,
                                                                                                       node.getNodeType(),
                                                                                                       false,
                                                                                                       isFileData,
                                                                                                       node.getNodeName(),
                                                                                                       node.getPrimDataType(),
                                                                                                       restrictions));

                xmlDocumentRows.add(xTrackerXmlDocumentRow);
            }
        }
    }

    /**
     *
     *
     */
    private void loadAttributes(XsdSchemaNode node, XTrackerXmlDocumentRow row) {

        if(node.getNumberOfAttributes() > 0) {

            List<XsdSchemaNode> attributes = node.getAttributes();

            for(XsdSchemaNode attributeNode : attributes) {

                // Double check this is an xsd attribute node
                if(attributeNode.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.ATTRIBUTENODE) {

                    String initialValue = "";
                    boolean isFixed = false;
                    String fixedValue = attributeNode.getFixedValue();
                    if(fixedValue != null) {

                        isFixed = true;
                        initialValue = fixedValue;
                    }

                    boolean isFileData = doesStringContainSubString(node.getNodeName(), "file");

                    XTrackerXmlDocumentRowAttribute attribute = new XTrackerXmlDocumentRowAttribute(node.getNodeName(),
                                                                                                    attributeNode.getNodeName(),
                                                                                                    initialValue,
                                                                                                    attributeNode.getDefaultValue(),
                                                                                                    isFixed,
                                                                                                    attributeNode.getIsAttributeRequired());
                    
                    row.addAttribute(attribute);
                    
                    XsdSchemaSimpleTypeRestriction restrictions = attributeNode.getSimpleTypeRestrictions();
                    XTrackerGuiComponentFactory guiComponentFactory =  XTrackerGuiComponentFactory.createInstance();
                    XTrackerGuiComponent component = guiComponentFactory.getXTrackerGuiComponent(row,
                                                                                                 attributeNode.getNodeType(),
                                                                                                 false,
                                                                                                 isFileData,
                                                                                                 attributeNode.getNodeName(),
                                                                                                 attributeNode.getPrimDataType(),
                                                                                                 restrictions);
                    attribute.setDisplayComponent(component);
                }
            }
        }
    }

    /**
     *
     *
     */
    private XTrackerXmlDocumentRowAttribute creatXmlDocumetRowAttribute(XTrackerXmlDocumentRow row, XsdSchemaNode node, String attributeName, String attributeValue) {

        XTrackerXmlDocumentRowAttribute attribute = null;

        if(node.getNumberOfAttributes() > 0) {

            List<XsdSchemaNode> attributes = node.getAttributes();

            for(XsdSchemaNode attributeNode : attributes) {

                if(attributeNode.getNodeName().equals(attributeName)) {

                    // Double check this is an xsd attribute node
                    if(attributeNode.getNodeType() == XsdSchemaNode.XsdSchemaNodeType.ATTRIBUTENODE) {

                        // if the value supplied in attributeValue is not "" (empty string) and is a fixed value
                        // then it should be! the same as that returned by attributeNode.getFixedValue();
                        String value = attributeValue;
                        boolean isFixed = false;
                        String fixedValue = attributeNode.getFixedValue();
                        if(fixedValue != null) {

                            isFixed = true;
                            if(attributeValue.equals("") == true) {

                                value = fixedValue;
                            }
                        }

                        boolean isFileData = doesStringContainSubString(node.getNodeName(), "file");

                        attribute = new XTrackerXmlDocumentRowAttribute(node.getNodeName(),
                                                                        attributeNode.getNodeName(),
                                                                        value,
                                                                        attributeNode.getDefaultValue(),
                                                                        isFixed,
                                                                        attributeNode.getIsAttributeRequired());

                        XsdSchemaSimpleTypeRestriction restrictions = attributeNode.getSimpleTypeRestrictions();
                        XTrackerGuiComponentFactory guiComponentFactory =  XTrackerGuiComponentFactory.createInstance();
                        XTrackerGuiComponent component = guiComponentFactory.getXTrackerGuiComponent(row,
                                                                                                     attributeNode.getNodeType(),
                                                                                                     false,
                                                                                                     isFileData,
                                                                                                     attributeNode.getNodeName(),
                                                                                                     attributeNode.getPrimDataType(),
                                                                                                     restrictions);
                        
                        attribute.setDisplayComponent(component);
                    }
                    break;
                }
            }
        }

        return attribute;
    }
}
