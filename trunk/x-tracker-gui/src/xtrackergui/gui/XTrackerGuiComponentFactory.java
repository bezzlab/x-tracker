
//
//    xTrackerGui
//
//    Package:    xtrackergui.gui
//    File:       XTrackerGuiComponentFactory.java
//    Date:       01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.util.List;
import xtrackergui.model.XTrackerXmlDocumentRow;
import xtrackergui.utils.parsers.XsdPrimativeDataType;
import xtrackergui.utils.parsers.XsdSchemaNode;
import xtrackergui.utils.parsers.XsdSchemaSimpleTypeRestriction;

/**
 * Singleton Class which manages the creation of xTracker Gui components
 *
 * @author andrew bullimore
 */
public class XTrackerGuiComponentFactory {

    // Reference to the single instance of XTrackerGuiComponentFactory class
    private static XTrackerGuiComponentFactory xTrackerGuiComponentFactoryRef;

    /**
     * Get the instance of XTrackerGuiComponentFactory - instantiate if
     * not already done so
     *
     */
    public static XTrackerGuiComponentFactory createInstance() {

        // Singleton - ensure we only have one instance
        if (xTrackerGuiComponentFactoryRef == null) {

            xTrackerGuiComponentFactoryRef = new XTrackerGuiComponentFactory();
        }

        return xTrackerGuiComponentFactoryRef;
    }

    /**
     * Singleton class therefore clones are not supported
     *
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {

        return new String("\nXTrackerGuiComponentFactory class\n");
    }


    public XTrackerGuiComponent getXTrackerGuiComponent(XTrackerXmlDocumentRow row,
                                                        XsdSchemaNode.XsdSchemaNodeType type,
                                                        boolean isSectionStart,
                                                        boolean isFile,
                                                        String labelDesc,
                                                        XsdPrimativeDataType dataType,
                                                        XsdSchemaSimpleTypeRestriction restrictions) {

        XTrackerGuiComponent component = null;
        StringBuffer attributeNodeName = new StringBuffer("");

        if(type == XsdSchemaNode.XsdSchemaNodeType.ATTRIBUTENODE) {

            attributeNodeName.append(labelDesc);
        }

        if(restrictions != null) {

            if(restrictions.getHasEnumerations()) {

                List<String> enumerations = restrictions.getEnumerations();
                
                component = new XTrackerComboBoxPanel(row,
                                                      attributeNodeName.toString(),
                                                      labelDesc,
                                                      true,
                                                      enumerations);
            }
        }

        if(component == null) {

            if(type == XsdSchemaNode.XsdSchemaNodeType.ROOTNODE) {

                component = new XTrackerSectionTagLabelPanel(row, 1, labelDesc);

            } else if(isSectionStart) {

                component = new XTrackerSectionTagLabelPanel(row, 2, labelDesc);

            } else if(isFile) {

                component = new XTrackerFileChooserPanel(row, attributeNodeName.toString(), labelDesc, true);

                String pattern = "";
                if(restrictions != null) {

                    pattern = restrictions.getPattern();
                    if(pattern.equals("") == false) {

                        XTrackerInputFilter inputFilter = new XTrackerInputFilter(true, pattern);
                        component.setXTrackerInputFilter(inputFilter);
                    }
                }

            } else {

                component = new XTrackerTextFieldPanel(row, attributeNodeName.toString(), labelDesc);

                String pattern = "";
                if(restrictions != null) {

                    pattern = restrictions.getPattern();

                }

                if(pattern.equals("") == true) {
                    
                    switch(dataType) {

                        case STRING: {

                            pattern = "(?i)^(\\p{Alnum}).*"; // case insensitive (?i) <- 
                        }
                        break;
                        case POSITIVEINTEGER: {

                            pattern = "(\\+)?(\\d){1,10}";
                        }
                        case NEGATIVEINTEGER: {

                            pattern = "(\\-)(\\d){1,10}";
                        }
                        case NONPOSITIVEINTEGER: {

                            pattern = "(\\-)(\\d){1,10}";
                        }
                        case NONNEGATIVEINTEGER: {

                            pattern = "(\\+)?(\\d){1,10}";
                        }
                        case BYTE: {

                            pattern = "(\\-|\\+)?\\d{0,3}";
                        }
                        break;
                        case SHORT: {

                            pattern = "(\\-|\\+)?(\\d){0,5}";
                        }
                        break;
                        case INT:
                        case INTEGER: {

                            pattern = "(\\-|\\+)?(\\d){0,10}";
                        }
                        break;
                        case LONG: {

                            pattern = "(\\-|\\+)?(\\d){0,20}";
                        }
                        break;
                        case FLOAT:
                        case DECIMAL: {

                            pattern = "(\\-|\\+)?(\\d){0,10}(\\.)?(\\d){1,10}";
                        }
                        break;
                        case DOUBLE: {

                            pattern = "(\\-|\\+)?(\\d){0,20}(\\.)?(\\d){1,20}";
                        }
                        break;
                        default: {

                            // Just in case - no good but stop crashes I hope
                            pattern = ".*";
                        }
                    }
                }

                if(pattern.equals("") == false) {

                    XTrackerInputFilter inputFilter = new XTrackerInputFilter(true, pattern);
                    component.setXTrackerInputFilter(inputFilter);
                }
            }
        }

        return component;
    }
}
