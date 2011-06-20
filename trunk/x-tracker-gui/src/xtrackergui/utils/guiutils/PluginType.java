
//
//    xTrackerGui
//
//    Package: xtrackergui.utils.guiutils
//    File: PluginType.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.utils.guiutils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumerations modelling the different type of x-tracker plugins - RAWDATA_LOAD_plugin etc
 *
 * @author andrew bullimore
 */
public enum PluginType {

    RAWDATAPLUGIN("RAWDATA_LOAD_plugin", "rawdata_loadplugin", "Load Raw Data", 0),
    IDENTDATAPLUGIN("IDENTDATA_LOAD_plugin", "identdata_loadplugin", "Spectral Identification", 1),
    PEAKSELECTIONPLUGIN("PEAKSEL_plugin", "peakselplugin", "Peak Selection", 2),
    QUANTIFICATIONPLUGIN("QUANT_plugin", "quantplugin", "Quantification", 3),
    OUTPUTPLUGIN("OUTPUT_plugin", "outplugin", "Output", 4),
    PLUGINTYPENOTDEFINED("NOTDEFINED_plugin", "notdefinedplugin", "Not Defined", -1);

    // xTracker plugin type ie RAWDATA_LOAD_plugin
    private final String pluginType;
    // xsd file element name for this plugin
    private final String pluginXsdElementName;
    // GUI tabbedpane tab title for PluginType ie Load Raw Data
    private final String pluginTitle;
    // GUI tabbedpane tab index for PluginType
    private final int tabbedPaneIndex;

    // Using two maps but could use one really, both have key = String and value = PluginType
    // Two maps do make the distinction in whats being mapped obvious
    //
    // Map pluginType string against the enum PluginType ie RAWDATA_LOAD_plugin vs RAWDATAPLUGIN
    private static final Map<String, PluginType> pluginTypeByString = new HashMap<String, PluginType>();
    // Map pluginTitle (title used on the GUI plugin tabbed pane) against the enum PluginType
    // ie Load Raw Data vs RAWDATAPLUGIN
    private static final Map<String, PluginType> pluginTypeByTitleString = new HashMap<String, PluginType>();

    static {

        // initialise maps
        for(PluginType type : EnumSet.allOf(PluginType.class)) {

            pluginTypeByString.put(type.getPluginString(), type);
        }

        for(PluginType type : EnumSet.allOf(PluginType.class)) {

            pluginTypeByTitleString.put(type.getPluginTitle(), type);
        }
    }
    
    /**
     * Create a plugin type
     *
     * @param type The type of plugin
     * @param title The plugin title for use by xTracker GUI display JTabbedPane
     * @param index The tab index of the xTracker GUI display JTabbedPane
     */
    private PluginType(String type, String xsdElementName, String title, int index) {

        pluginType = type;
        pluginXsdElementName = xsdElementName;
        pluginTitle = title;
        tabbedPaneIndex = index;
    }

    @Override
    public String toString() {

        // keep as the pluginType string
        return this.pluginType;
    }

    /**
     * Get the pluginType string
     *
     * @return String descrition of the plugin
     *
     */
    public String getPluginString() {

        return this.pluginType;
    }

    /**
     * Get the xsd file element name for this plugin
     *
     * @return The xsd file element name of the plugin
     */
    public String getpluginXsdElementName() {

        return pluginXsdElementName;
    }

    /**
     * Get the title string for this plugin
     *
     * @return The title string of the plugin
     */
    public String getPluginTitle() {

        return this.pluginTitle;
    }

    /**
     * The index of the tabbed pane which displays information for this plugin
     *
     * @return The tabbed pane index for this plugin
     *
     */
    public int getTabbedPaneIndex() {

        return this.tabbedPaneIndex;
    }

    /**
     * Given the string representation of this pluginType return the appropriate enumeration
     *
     *
     * @param pluginTypeStringValue The pluginType string representation of this PluginType
     * @return The PluginType enumeration
     */
    public static PluginType getPluginEnumByTypeString(String pluginTypeStringValue) {

        PluginType returnPluginType = PluginType.PLUGINTYPENOTDEFINED;

        if (pluginTypeByString.containsKey(pluginTypeStringValue)) {

            returnPluginType = (PluginType) pluginTypeByString.get(pluginTypeStringValue);
        }

        return returnPluginType;
    }

    /**
     * Given the string representation of this pluginTitle return the appropriate enumeration
     *
     *
     * @param pluginTitleStringValue The pluginTitle string representation of this PluginType
     * @return The PluginType enumeration
     */
    public static PluginType getPluginEnumByTitleString(String pluginTitleStringValue) {

        PluginType returnPluginType = PluginType.PLUGINTYPENOTDEFINED;

        if (pluginTypeByTitleString.containsKey(pluginTitleStringValue)) {

            returnPluginType = (PluginType) pluginTypeByTitleString.get(pluginTitleStringValue);
        }

        return returnPluginType;
    }
}
