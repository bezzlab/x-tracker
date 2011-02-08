
//
//    xTrackerGui
//
//    Package:    xtrackergui.gui
//    File:       XTrackerPluginParamsUIController.java
//    Date:       01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import org.apache.log4j.Logger;
import xtrackergui.model.XTrackeConfigModelManager;
import xtrackergui.model.XTrackerConfigModel;
import xtrackergui.model.XTrackerParamFileModel;
import xtrackergui.model.XTrackerXmlDocument;
import xtrackergui.model.XTrackerXmlDocumentRow;
import xtrackergui.model.XTrackerXmlDocumentRowAttribute;
import xtrackergui.utils.guiutils.PluginType;

/**
 * Controller class. Coordinates the JPanels displaying xTracker parameter file information via a JTabbedPane
 *
 * @author andrew bullimore
 */
public class XTrackerPluginParamsUIController {

    // JTabbedPane where JPanels are displayed
    private JTabbedPane pluginParamsDisplayTabPane;
    // JButton for saving individual param file models/ files
    private JButton pluginParamsGuiSaveButton;
    // Map storing references to the panels displayed in pluginParamsDisplayTabPane keyed by xTracker
    // plugin type
    private Map<PluginType, JPanel> pluginModelDisplayPanelMap = new HashMap<PluginType, JPanel>();

    private Logger logger = Logger.getLogger(xtrackergui.gui.XTrackerPluginParamsUIController.class.getName());

    /**
     * Create the UI controller and set the JTabbedPane where JPanels will be displayed
     *
     * @param tabbedPane The tabbed pane displaying JPanels with xTracker paramter file information
     */
    public XTrackerPluginParamsUIController(JTabbedPane tabbedPane, JButton button) {

        pluginParamsDisplayTabPane = tabbedPane;
        pluginParamsGuiSaveButton = button;
    }

    /**
     * Display a particular tab in the JTabbedPane (pluginParamsDisplayTabPane)
     *
     * @param index Set the tab to display via its index
     */
    public void setPluginParamsTabPaneIndex(int index) {

        pluginParamsDisplayTabPane.setSelectedIndex(index);
    }

    /**
     * Create the UI controller and set the JTabbedPane (pluginParamsDisplayTabPane) where JPanels will be displayed
     *
     */
    public int getPluginParamsTabPaneIndex() {

        return pluginParamsDisplayTabPane.getSelectedIndex();
    }

    /**
     * Return the xTracker plugin type for the tab currently selected in the JTabbedPane (pluginParamsDisplayTabPane)
     *
     */
    public PluginType getPluginTypeForCurrPluginParamsSelectedTab() {

        return PluginType.getPluginEnumByTitleString(pluginParamsDisplayTabPane.getTitleAt(pluginParamsDisplayTabPane.getSelectedIndex()));
    }

    /**
     * Return the xTracker plugin type for the tab currently selected in the JTabbedPane (pluginParamsDisplayTabPane)
     *
     */
    public PluginType getPluginTypeByTabPaneIndex(int index) {

        return PluginType.getPluginEnumByTitleString(pluginParamsDisplayTabPane.getTitleAt(index));
    }
    
    /**
     * Display a default screen for the PluginType specified in parameter pluginType
     *
     * @param pluginType xTracker plugin type
     * @param message A message to diaply in the JLabel of the JPanel
     * @param fontSize The font size of the displayed message
     */
    public void displayDefaultScreenForPlugin(PluginType pluginType, String screenTitle, String message, int fontSize) {

        // update the display map with the default screen - adding appropriate message to the default panel
        pluginModelDisplayPanelMap.put(pluginType, new XTrackerDefaultPluginParamsDisplayPanel(screenTitle, message, fontSize));

        // Get the scrollpane at this tab index
        JScrollPane pane = (JScrollPane)pluginParamsDisplayTabPane.getComponentAt(pluginType.getTabbedPaneIndex());
        JViewport view = pane.getViewport();
        // display the screen
        view.add(pluginModelDisplayPanelMap.get(pluginType));
    }

    /**
     * Display a default screen for all xTracker PluginTypes
     *
     * @param message A message to diaply in the JLabel of the JPanel
     * @param fontSize The font size of the displayed message
     */
    public void displayDefaultScreenAllForPlugins(String screenTitle, String message, int fontSize) {

        for(int i = 0; i < pluginParamsDisplayTabPane.getComponentCount(); i++) {

            // Find the PluginType via the title string of this tab
            PluginType pluginType = PluginType.getPluginEnumByTitleString(pluginParamsDisplayTabPane.getTitleAt(i));

            if(pluginType != PluginType.PLUGINTYPENOTDEFINED) {

                // if any panels are currently being displayed remove them
                removeScreenForPlugin(pluginType);
                // display a default screen outputting the information in String parameter message
                displayDefaultScreenForPlugin(pluginType, screenTitle, message, fontSize);
            }
        }
    }

    /**
     * Update the message displayed in a JPanel for a given xTracker PluginType
     *
     * @param pluginType xTracker plugin type
     * @param message A message to diaply in the JLabel of the JPanel
     * @param fontSize The font size of the displayed message
     */
    public void updateDefaultScreenForPlugin(PluginType pluginType, String message, int fontSize) {

        if(pluginModelDisplayPanelMap.containsKey(pluginType)) {

            JPanel panel = pluginModelDisplayPanelMap.get(pluginType);

            if(panel instanceof XTrackerDefaultPluginParamsDisplayPanel) {
                
                JScrollPane pane = (JScrollPane)pluginParamsDisplayTabPane.getComponentAt(pluginType.getTabbedPaneIndex());
                JViewport view = pane.getViewport();
                view.remove(panel);
                view.validate();
                pluginModelDisplayPanelMap.remove(pluginType);
            }
        }
    }

    public boolean isDefaultDisplayScreenSetForPlugin(PluginType pluginType) {

        boolean isDefaultDisplayScreen = false;

        if(pluginModelDisplayPanelMap.containsKey(pluginType)) {

            JPanel panel = pluginModelDisplayPanelMap.get(pluginType);

            if(panel instanceof XTrackerDefaultPluginParamsDisplayPanel) {

                isDefaultDisplayScreen = true;
            }
        }

        return isDefaultDisplayScreen;
    }

    /**
     * Display the xTracker parameter file information for a given PluginType
     *
     * @param pluginType xTracker plugin type
     */
    public void displayScreenForPlugin(PluginType pluginType, String configModelName, String jarFileName, String classFileName, String xsdFileName) {

        XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
        XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(configModelName);

        XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(xsdFileName);

        XTrackerPluginParamsDisplayPanel pluginDisplayPanel = new XTrackerPluginParamsDisplayPanel(configModelName,
                                                                                                   jarFileName,
                                                                                                   classFileName,
                                                                                                   xsdFileName,
                                                                                                   paramFileModel,
                                                                                                   pluginType,
                                                                                                   paramFileModel.getParametersFileName(),
                                                                                                   pluginParamsGuiSaveButton);

        XTrackerXmlDocument xmlDocument = paramFileModel.getParamFileXmlDocument();
        List<XTrackerXmlDocumentRow> xmlDocumentRows = xmlDocument.getXTrackerXmlDocument();

        for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

            if(row.getDisplayComponent() != null) {

                ((XTrackerGuiComponent)row.getDisplayComponent()).setGuiComponentParentDisplayPanel(pluginDisplayPanel);
                pluginDisplayPanel.add(row.getDisplayComponent());

                if(row.hasAttributes()) {

                    List<XTrackerXmlDocumentRowAttribute> attributes = row.getAllAttributes();
                    for(XTrackerXmlDocumentRowAttribute attr : attributes) {

                        ((XTrackerGuiComponent)row.getDisplayComponent()).setGuiComponentParentDisplayPanel(pluginDisplayPanel);
                        pluginDisplayPanel.add(attr.getDisplayComponent());
                    }
                }
            }
        }

        // update the display map with the default screen - adding appropriate message to the default panel
        pluginModelDisplayPanelMap.put(pluginType, pluginDisplayPanel);

        // Get the scrollpane at this tab index
        JScrollPane pane = (JScrollPane)pluginParamsDisplayTabPane.getComponentAt(pluginType.getTabbedPaneIndex());
        JViewport view = pane.getViewport();
        // display the screen
        view.add(pluginModelDisplayPanelMap.get(pluginType));
    }

    /**
     * Remove the screen/ JPanel being displayed for the PluginType specified in the parameter pluginType
     *
     * @param pluginType xTracker plugin type
     */
    public void removeScreenForPlugin(PluginType pluginType) {

        if(pluginModelDisplayPanelMap.containsKey(pluginType)) {

            JPanel panel = pluginModelDisplayPanelMap.get(pluginType);
            JScrollPane pane = (JScrollPane)pluginParamsDisplayTabPane.getComponentAt(pluginType.getTabbedPaneIndex());
            JViewport view = pane.getViewport();
            view.remove(panel);
            view.validate();
            pluginModelDisplayPanelMap.remove(pluginType);
        }
    }

    /**
     *
     *
     *
     */
    public void refreshScreenForPlugin(PluginType pluginType) {

        if(pluginModelDisplayPanelMap.containsKey(pluginType)) {

            String configModelName = null;
            String xsdFileName = null;
            JPanel panel = pluginModelDisplayPanelMap.get(pluginType);
            if(panel instanceof XTrackerPluginParamsDisplayPanel) {

                configModelName = ((XTrackerPluginParamsDisplayPanel)panel).getConfigModelName();
                xsdFileName = ((XTrackerPluginParamsDisplayPanel)panel).getXsdFileName();
            }

            if(configModelName != null) {

                XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(configModelName);

                if(xsdFileName != null) {

                    XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(xsdFileName);

                    XTrackerXmlDocument xmlDocument = paramFileModel.getParamFileXmlDocument();
                    List<XTrackerXmlDocumentRow> xmlDocumentRows = xmlDocument.getXTrackerXmlDocument();

                    for(XTrackerXmlDocumentRow row : xmlDocumentRows) {

                        if(row.getDisplayComponent() != null) {

                            row.refreshTagValue();

                            if(row.hasAttributes()) {

                                List<XTrackerXmlDocumentRowAttribute> attributes = row.getAllAttributes();
                                for(XTrackerXmlDocumentRowAttribute attr : attributes) {

                                    attr.initialiseAttributeValue();
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     *
     *
     */
    public void updateFileDescriptionInScreenForPlugin(PluginType pluginType, String fileName) {

        JPanel panel = pluginModelDisplayPanelMap.get(pluginType);

        if(panel instanceof XTrackerPluginParamsDisplayPanel) {

            ((XTrackerPluginParamsDisplayPanel)panel).setXmlParameterFileDescriptionLabel(fileName);
        }
    }

    /**
     * Remove all the screen/ JPanels being displayed
     *
     */
    public void removeScreenForAllPlugins() {

        for(int i = 0; i < pluginParamsDisplayTabPane.getComponentCount(); i++) {

            PluginType pluginType = PluginType.getPluginEnumByTitleString(pluginParamsDisplayTabPane.getTitleAt(i));
            removeScreenForPlugin(pluginType);
        }
        
        pluginParamsDisplayTabPane.validate();
    }

    /**
     *
     *
     * @param pluginType xTracker plugin type
     */
    public void resetParamFileNameToDefaultColour(PluginType pluginType) {

        if(pluginModelDisplayPanelMap.containsKey(pluginType)) {

            if(pluginModelDisplayPanelMap.get(pluginType) instanceof XTrackerPluginParamsDisplayPanel) {

                XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)pluginModelDisplayPanelMap.get(pluginType);
                displayedPanel.resetParamFileNameToDefaultColour();
            }
        }
    }

    /**
     *
     *
     */
    public boolean isParamFileModelForJarFileDisplayed(String jarFileName) {

        boolean modelIsDisplayed = false;

        for(Map.Entry<PluginType, JPanel> entry : pluginModelDisplayPanelMap.entrySet()) {

            if(entry.getValue() instanceof XTrackerPluginParamsDisplayPanel) {

                XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)entry.getValue();
                if(displayedPanel.getXsdFileName().equals(jarFileName) == true) {

                    modelIsDisplayed = true;
                    break;
                }
            }
        }

        return modelIsDisplayed;
    }

    /**
     *
     *
     */
    public boolean isParamFileModelForXsdFileDisplayed(String xsdFileName) {

        boolean modelIsDisplayed = false;

        for(Map.Entry<PluginType, JPanel> entry : pluginModelDisplayPanelMap.entrySet()) {

            if(entry.getValue() instanceof XTrackerPluginParamsDisplayPanel) {

                XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)entry.getValue();
                if(displayedPanel.getXsdFileName().equals(xsdFileName) == true) {

                    modelIsDisplayed = true;
                    break;
                }
            }
        }

        return modelIsDisplayed;
    }

    /**
     *
     *
     */
    public String getJarFileForXsdFileDisplayed(String xsdFileName) {

        String jarFile = "";

        for(Map.Entry<PluginType, JPanel> entry : pluginModelDisplayPanelMap.entrySet()) {

            if(entry.getValue() instanceof XTrackerPluginParamsDisplayPanel) {
                
                XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)entry.getValue();
                if(displayedPanel.getXsdFileName().equals(xsdFileName) == true) {

                    jarFile = displayedPanel.getJarFileName();
                    break;
                }
            }
        }
        
        return jarFile;
    }

    /**
     *
     *
     */
    public PluginType getPluginTypeForXsdFileDisplayed(String xsdFileName) {

        PluginType pluginType = PluginType.PLUGINTYPENOTDEFINED;

        for(Map.Entry<PluginType, JPanel> entry : pluginModelDisplayPanelMap.entrySet()) {

            if(entry.getValue() instanceof XTrackerPluginParamsDisplayPanel) {

                XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)entry.getValue();
                if(displayedPanel.getXsdFileName().equals(xsdFileName) == true) {

                    pluginType = displayedPanel.getPluginTypeToDisplay();
                    break;
                }
            }
        }

        return pluginType;
    }

     /**
     *
     *
     */
    public String getJarFileForPluginTypeDisplayed(PluginType pluginType) {

        String jarFile = "";

        if(pluginModelDisplayPanelMap.get(pluginType) instanceof XTrackerPluginParamsDisplayPanel) {

            XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)pluginModelDisplayPanelMap.get(pluginType);
            jarFile = displayedPanel.getJarFileName();
        }

        return jarFile;
    }

    /**
     *
     *
     */
    public String getXsdFileForPluginTypeDisplayed(PluginType pluginType) {

        String xsdFile = "";

        if(pluginModelDisplayPanelMap.get(pluginType) instanceof XTrackerPluginParamsDisplayPanel) {

            XTrackerPluginParamsDisplayPanel displayedPanel = (XTrackerPluginParamsDisplayPanel)pluginModelDisplayPanelMap.get(pluginType);
            xsdFile = displayedPanel.getXsdFileName();
        }

        return xsdFile;
    }
}
