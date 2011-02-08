
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerPluginInformation.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import xtrackergui.utils.guiutils.PluginType;

/**
 * A data class which stores all the information obtainable from an xTracker plugin
 *
 * @author andrew bullimore
 */
public class XTrackerPluginInformation {

    String fileName = null;
    // not derived from the plugin itself
    String xsdSchemaFileName = null;
    String className = null;
    String name = null;
    String version = null;
    String description = null;
    PluginType type = null;
    String htmlInformation = null;
    // How many words to a line to use when displaying the contained information
    // in html format
    final static int lineWordCount = 10;

    /**
     * Create a plugin information object
     *
     * @param pluginFileName The plugin jar file name
     * @param pluginClassName The plugin class name (same as jar file name minus the .jar extension)
     * @param pluginName The plugin name
     * @param pluginVersion The current version of the plugin
     * @param pluginDescription A description of the plugin
     * @param pluginType The type of the plugin
     *
     */
    public XTrackerPluginInformation(String pluginFileName,
                                     String pluginClassName,
                                     String pluginName,
                                     String pluginVersion,
                                     String pluginDescription,
                                     PluginType pluginType) {

        fileName = pluginFileName;
        className = pluginClassName;
        name = pluginName;
        version = pluginVersion;
        description = pluginDescription;
        type = pluginType;
    }

    @Override
    public String toString() {

        StringBuffer outputString = new StringBuffer();

        outputString.append("\nJar file: " + fileName +
                            " Class name: " + className +
                            " Plugin name: " + name +
                            " Plugin version " + version);

        if(xsdSchemaFileName != null) {

            outputString.append(" xsd file name: " + xsdSchemaFileName + '\n');
        } else {

            outputString.append(" xsd file name: not set\n");
        }

        return outputString.toString();
    }

    /**
     * Return the plugin jar file name
     *
     * @return The plugin file name (including the .jar extension)
     */
    public String getPluginFileName() {

        return fileName;
    }

    /**
     * Set the xsd schema filename associated with this plugin
     *
     * @param fileName The name of the xsd schema file
     */
    public void setXsdSchemaFileName(String fileName) {

        xsdSchemaFileName = fileName;
    }

    /**
     * Return the xsd schema filename associated with this plugin
     *
     * @return The name of the xsd schema file
     */
    public String getXsdSchemaFileName() {

        return xsdSchemaFileName;
    }

    /**
     * Return the plugin class name (same as jar file but without .jar file extension)
     *
     * @return The plugin class name
     */
    public String getPluginClassName() {

        return className;
    }

    /**
     * Return the plugin name
     *
     * @return Plugin Name
     */
    public String getName() {

        return name;
    }

    /**
     * Return the plugin version number
     *
     * @return The plugin version number
     */
    public String getVersion() {

        return version;
    }

    /**
     * Return the description supplying details about the plugin
     *
     * @return The description string
     */
    public String getDescription() {

        return description;
    }

    /**
     * Return the type of this plugin
     *
     * @return The enumerated type of the plugin (PluginType)
     */
    public PluginType getType() {

        return type;
    }

    /**
     * Generates a html message string detailing the data stored in an instance of XTrackerPluginInformation
     *
     * @param addHeadInfo An optional string of information to include at the beginning of the returned information string
     * @param addTailInfo An optional string of information to include at the end of the returned information string
     */
    public String getHtmlInformation(String addHeadInfo, String addTailInfo) {

        // only create the html string when required - it may subsequently be re-used
        if(htmlInformation == null) {

            // Remove any formatting newlines and tabs from the description field
            // - we want to format the output of this string here
            description = description.replaceAll("\n", " ");
            description = description.replaceAll("\t", " ");
            description = description.replaceAll("\n\t", " ");

            // Split the description into words so it can be more easily formatted
            // into lines of set number of words governed by the static lineWordCount
            String[] descriptionWords = description.split(" ");

            // Create the return string
            htmlInformation = "<p><center>Plugin Name: " + name + "</center></p>" +
                              "<p><center>Jar file: " + fileName + "</center></p>" +
                              "<p><center>Version: " + version + "</center></p><br />";

            // add the description field information
            int wordCount = 0;
            StringBuffer line = new StringBuffer();
            for(String word : descriptionWords) {

                // keep to the word count per line - each line is a new paragraph
                if(wordCount < lineWordCount) {

                    if(wordCount == 0) {

                        // open paragraph
                        line.append("<p>");
                    }

                    line.append(word);
                }

                wordCount++;
                if(wordCount == lineWordCount) {

                    wordCount = 0;
                    // hit the word cound, close this paragraph
                    line.append("</p>");
                    
                } else {

                    // put spaces between the words
                    line.append(" ");
                }

            }

            // if we fall short of the required number of words make sure we
            // close the paragraph
            if(wordCount > 0 && wordCount < lineWordCount) {

                line.append("</p>");
            }

            // set the string
            htmlInformation += line.toString();
        }

        // return the information with any required preceeding or trailing information stored in the
        // parameters addheadInfo/ addTailInfo included
        String information = "<html>" + addHeadInfo + htmlInformation + addTailInfo + "</html>";

        return information;
    }
}
