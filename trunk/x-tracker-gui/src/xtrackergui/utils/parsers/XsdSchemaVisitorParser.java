
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.parsers
//    File: XsdSchemaVisitorParser.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.parsers;

import java.util.Iterator;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Parse XSD Schemas creating a list of XsdSchemaNode objects (uses visitir pattern
 * - implements XSVisitor and XSSimpleTypeVisitor)
 * 
 * @author andrew bullimore
 */
public class XsdSchemaVisitorParser implements XSVisitor, XSSimpleTypeVisitor {

    private List<XsdSchemaNode> xsdSchemaNodes = new ArrayList<XsdSchemaNode>();
    private XsdSchemaNode currXsdSchemaNode = null;
    String beforeModelGroupAncestryPath = "";
    String localTypesAncestryPath = "";
    String globalTypesAncestry = "";
    private boolean firstPass = true;

    private Logger logger = Logger.getLogger(xtrackergui.gui.XTrackerGuiUIFrame.class.getName());

    /**
     * Iterate over the XSSchemaSet to find XSD schemas to parse - will only be one
     * in the set currently
     *
     * @param xsShcemaSet The set of xsSchemas to parse (only be one here)
     * @return The list of XsdSchemaNode nodes parsed
     */
    public List<XsdSchemaNode> visit(XSSchemaSet xsShcemaSet) {

        Iterator xsSchemaSetIterator = xsShcemaSet.getSchemas().iterator();
	while(xsSchemaSetIterator.hasNext()) {

            XSSchema xsSchema = (XSSchema) xsSchemaSetIterator.next();
	    schema(xsSchema);
        }

        return xsdSchemaNodes;
    }

    /**
     * Parse the XSD schema supplied in the parameter xsSchema
     *
     * @param xsSchema The XSSchema object to parse
     */
    @Override
    public void schema(XSSchema xsSchema) {

        if(!xsSchema.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema")) {

	    Iterator xsSchemaElementIterator = xsSchema.getElementDecls().values().iterator();
	    while(xsSchemaElementIterator.hasNext()) {

                XSElementDecl elementDecl = (XSElementDecl) xsSchemaElementIterator.next();
                elementDecl(elementDecl);
	    }
        }

        if(logger.isDebugEnabled()) {

            for(XsdSchemaNode node : xsdSchemaNodes) {

                logger.debug(node);
            }
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void attGroupDecl(XSAttGroupDecl xsAttGroupDecl) {

        if(logger.isDebugEnabled()) {

            logger.debug("attGroupDecl - not implemented");
        }
    }

    /**
     * Parse attribute information
     *
     * @param xsAttributeUse
     */
    @Override
    public void attributeUse(XSAttributeUse xsAttributeUse) {

       XSAttributeDecl decl = xsAttributeUse.getDecl();

        processAttributeDecl(decl, xsAttributeUse.isRequired());
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void attributeDecl(XSAttributeDecl xsAttributeDecl) {

        if(logger.isDebugEnabled()) {

            logger.debug("attributeDecl - not implemented");
        }
    }

    /**
     * Process and parse a XSAttributeDecl object
     *
     * @param xsAttributeDecl XSAttributeDecl object to parse
     * @param required Is the attribute required
     */
    private void processAttributeDecl(XSAttributeDecl xsAttributeDecl, boolean required) {

        XsdSchemaNode attribute = new XsdSchemaNode();
        attribute.setNodeType(XsdSchemaNode.XsdSchemaNodeType.ATTRIBUTENODE);
        attribute.setContentType(XsdSchemaContentType.SIMPLETYPE);
        attribute.setNodeName(xsAttributeDecl.getName());
        if(xsAttributeDecl.getDefaultValue() != null) {

            attribute.setDefaultValue(xsAttributeDecl.getDefaultValue().value);
        }
        if(xsAttributeDecl.getFixedValue() != null) {

            attribute.setFixedValue(xsAttributeDecl.getFixedValue().value);
        }
        attribute.setIsAttributeRequired(required);
        currXsdSchemaNode.initAttributesList();
        currXsdSchemaNode.setAttribute(attribute);

        // Create temporary XsdSchemaNode to store the currXsdSchemaNode
        // ie the current element we're dealing with - we're processing
        // any attribute data for this element.
        //
        // Set the newly create XsdSchemaNode (carrying attribute data)
        // to be the currXsdSchemaNode and carry on processing and parsing
        // attribute information, once complete, reset the currXsdSchemaNode
        // to the original element above.
        XsdSchemaNode tempXsdSchemaNode = currXsdSchemaNode;
        currXsdSchemaNode = attribute;
        XSSimpleType xsSimpleType = xsAttributeDecl.getType();
	simpleType(xsSimpleType);
        currXsdSchemaNode = tempXsdSchemaNode;
    }

    /**
     * Parse XSSimpleType (XSD simple type objects)
     *
     * @param xsSimpleType The simple type to parse
     */
    @Override
    public void simpleType(XSSimpleType xsSimpleType) {

        XsdPrimativeDataType dataType = XsdPrimativeDataType.getPrimDataTypeEnumFromString(xsSimpleType.getName());

        if(dataType != XsdPrimativeDataType.UNKNOWN) {

            currXsdSchemaNode.setPrimDataType(dataType);
            
        } else {

            XSSimpleType baseType = xsSimpleType.getSimpleBaseType();
            XsdPrimativeDataType baseDataType = XsdPrimativeDataType.getPrimDataTypeEnumFromString(baseType.getName());
            if(baseDataType != XsdPrimativeDataType.UNKNOWN) {

                currXsdSchemaNode.setPrimDataType(baseDataType);
                currXsdSchemaNode.setNodeTypeAsString(xsSimpleType.getName());
                
            } else {

                logger.warn("XsdSchemaVisitorParser::simpleType: Problem - cannot set primative data type for " + currXsdSchemaNode.getNodeName());
            }
        }

        xsSimpleType.visit((XSSimpleTypeVisitor)this);
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void listSimpleType(XSListSimpleType xsListSimpleType) {

        if(logger.isDebugEnabled()) {

            logger.debug("listSimpleType - not implemented");
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void unionSimpleType(XSUnionSimpleType xsUnionSimpleType) {

        if(logger.isDebugEnabled()) {

            logger.debug("unionSimpleType - not implemented");
        }
    }

    /**
     * Parse the restriction data for a XSD simple type element
     *
     * @param xsRestrictionSimpleType The XSRestrictionSimpleType to parse
     */
    @Override
    public void restrictionSimpleType(XSRestrictionSimpleType xsRestrictionSimpleType) {

        XsdSchemaSimpleTypeRestriction simpleTypeRestriction = new XsdSchemaSimpleTypeRestriction();
        simpleTypeRestriction.initXsdSchemaSimpleTypeRestriction(xsRestrictionSimpleType);
        currXsdSchemaNode.setSimpleTypeRestrictions(simpleTypeRestriction);
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void facet(XSFacet xsFacet) {

        if(logger.isDebugEnabled()) {

            logger.debug("facet - not implemented");
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void notation(XSNotation notation) {

        if(logger.isDebugEnabled()) {

            logger.debug("notation - not implemented");
        }
    }

    /**
     *
     *
     * @param xsComplexType The XSComplexType object to parse
     */
    @Override
    public void complexType(XSComplexType xsComplexType) {

        if(currXsdSchemaNode.getNodeType() != XsdSchemaNode.XsdSchemaNodeType.ROOTNODE) {

            currXsdSchemaNode.setNodeType(XsdSchemaNode.XsdSchemaNodeType.COMPLEXNODE);
        }

	if(xsComplexType.getContentType().asSimpleType() != null) {

            // Simple Content
            currXsdSchemaNode.setContentType(XsdSchemaContentType.SIMPLETYPE);
            String nodeAncestryPath = localTypesAncestryPath + currXsdSchemaNode.getNodeName();
            currXsdSchemaNode.setAncestryPath(nodeAncestryPath);

            processComplexTypeAttribute(xsComplexType);
            xsComplexType.getContentType().visit(this);

	} else {

            // Complex content
            currXsdSchemaNode.setContentType(XsdSchemaContentType.COMPLEXTYPE);
            localTypesAncestryPath += currXsdSchemaNode.getNodeName() + ":";
            currXsdSchemaNode.setAncestryPath(localTypesAncestryPath);

            if(xsComplexType.isLocal()) {

                XSType baseType = xsComplexType.getBaseType();
                currXsdSchemaNode.setNodeTypeAsString(baseType.getName());
            }
            processComplexTypeAttribute(xsComplexType);
            xsComplexType.getContentType().visit(this);
	}
    }

    /**
     *
     *
     */
    private void processComplexTypeAttribute(XSComplexType xsComplexType) {

        Iterator declAttributeUsesIterator = xsComplexType.iterateDeclaredAttributeUses();
        while(declAttributeUsesIterator.hasNext()) {

	    attributeUse((XSAttributeUse) declAttributeUsesIterator.next());
        }
    }

    /**
     *
     *
     */
    @Override
    public void elementDecl(XSElementDecl xsElementDecl) {

        elementDecl(xsElementDecl, 1, 1);
    }

    /**
     *
     *
     */
    private void elementDecl(XSElementDecl xsElementDecl, int min, int max) {

        XSType xsType = xsElementDecl.getType();

        currXsdSchemaNode = new XsdSchemaNode();

        currXsdSchemaNode.setNodeName(xsElementDecl.getName());

        if(firstPass) {

            currXsdSchemaNode.setNodeType(XsdSchemaNode.XsdSchemaNodeType.ROOTNODE);
            globalTypesAncestry = currXsdSchemaNode.getNodeName();
            firstPass = false;
            
        } else {

            if(xsType.isSimpleType()) {

                currXsdSchemaNode.setNodeType(XsdSchemaNode.XsdSchemaNodeType.ELEMENTNODE);
                currXsdSchemaNode.setContentType(XsdSchemaContentType.SIMPLETYPE);

                if(xsElementDecl.isGlobal()) {

                    String nodeAncestryPath = globalTypesAncestry + ":" + currXsdSchemaNode.getNodeName();
                    currXsdSchemaNode.setAncestryPath(nodeAncestryPath);

                } else {

                    String nodeAncestryPath = localTypesAncestryPath + currXsdSchemaNode.getNodeName();
                    currXsdSchemaNode.setAncestryPath(nodeAncestryPath);
                    
                }
            }

            currXsdSchemaNode.setMinoccurs(min);
            currXsdSchemaNode.setMaxOccurs(max);
        }

        if(!xsType.isLocal()) {

            currXsdSchemaNode.setNodeTypeAsString(xsType.getName());
        }

        xsdSchemaNodes.add(currXsdSchemaNode);

        xsType.visit(this);
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void modelGroupDecl(XSModelGroupDecl decl) {

        if(logger.isDebugEnabled()) {

            logger.debug("modelGroupDecl - not implemented");
        }
    }

    /**
     *
     *
     */
    @Override
    public void modelGroup(XSModelGroup group) {

        modelGroup(group, -10);
    }

    /**
     *
     *
     */
    private void modelGroup(XSModelGroup group, int dummyParameter) {

        int modelGroupSize = group.getSize();
        for (int i = 0; i < modelGroupSize; i++) {

            particle(group.getChild(i));
        }

        localTypesAncestryPath = globalTypesAncestry + ":";
    }

    /**
     *
     *
     */
    @Override
    public void particle(XSParticle xsParticle) {

        final int max = xsParticle.getMaxOccurs();
        final int min = xsParticle.getMinOccurs();

	xsParticle.getTerm().visit(new XSTermVisitor() {

            @Override
            public void elementDecl(XSElementDecl decl) {

                if(decl.isLocal()) {

                    XsdSchemaVisitorParser.this.elementDecl(decl, min, max);

                } else {
		    
                    if(logger.isDebugEnabled()) {

                        logger.debug("XSTermVisitor::elementDecl - element refs not implemented");
                    }
                }
            }

            @Override
            public void modelGroupDecl(XSModelGroupDecl decl) {

                if(logger.isDebugEnabled()) {

                    logger.debug("XSTermVisitor::modelGroupDecl - not implemented");
                }
            }

            @Override
            public void modelGroup(XSModelGroup group) {

                Compositor comp = group.getCompositor();
                if(comp == Compositor.ALL) {

                    currXsdSchemaNode.setCompositorType(XsdSchemaCompositorType.ALL);

                } else if(comp == Compositor.SEQUENCE) {

                    currXsdSchemaNode.setCompositorType(XsdSchemaCompositorType.SEQUENCE);

                } else {

                    currXsdSchemaNode.setCompositorType(XsdSchemaCompositorType.CHOICE);
                }

                XsdSchemaVisitorParser.this.modelGroup(group, -10);
	    }

            @Override
            public void wildcard(XSWildcard wc) {

                if(logger.isDebugEnabled()) {

                    logger.debug("XSTermVisitor::wildcard - not implemented");
                }
	    }
        });
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void wildcard(XSWildcard wc) {

        if(logger.isDebugEnabled()) {

            logger.debug("wildcard - not implemented");
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void annotation(XSAnnotation ann) {

        if(logger.isDebugEnabled()) {

            logger.debug("annotation - not implemented");
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void empty(XSContentType t) {

        if(logger.isDebugEnabled()) {

            logger.debug("empty - not implemented");
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void identityConstraint(XSIdentityConstraint ic) {

        if(logger.isDebugEnabled()) {

            logger.debug("identityConstraint - not implemented");
        }
    }

    /**
     * Override - not implemented
     *
     */
    @Override
    public void xpath(XSXPath xp) {

        if(logger.isDebugEnabled()) {

            logger.debug("xpath - not implemented");
        }
    }
}

