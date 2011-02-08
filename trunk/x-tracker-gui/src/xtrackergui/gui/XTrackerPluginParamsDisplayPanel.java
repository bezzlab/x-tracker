
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerPluginParamsDisplayPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import xtrackergui.model.XTrackerParamFileModel;
import xtrackergui.utils.guiutils.PluginType;

/**
 *
 *
 * @author andrew bullimore
 */
public class XTrackerPluginParamsDisplayPanel extends javax.swing.JPanel {

    private String configurationModelName = null;
    private String pluginJarFileName = null;
    private String pluginClassName = null;
    private String pluginXsdFileName = null;
    private XTrackerParamFileModel pluginParamFileModel = null;
    private PluginType pluginTypeForDisplay = PluginType.PLUGINTYPENOTDEFINED;
    private JLabel fileDescriptionLabel;
    private JButton pluginParamFileGuiSaveButton;
    private static final String xmlParameterFileNotCreated = "parameter data file not created";

    /**
     *
     *
     */
    public XTrackerPluginParamsDisplayPanel(String configModelName,
                                            String jarFileName,
                                            String classFileName,
                                            String xsdFileName,
                                            XTrackerParamFileModel paramFileModel,
                                            PluginType pluginType,
                                            String paramFileName,
                                            JButton paramFileGuiSaveButton) {

        configurationModelName = configModelName;
        pluginJarFileName = jarFileName;
        pluginClassName = classFileName;
        pluginXsdFileName = xsdFileName;
        pluginParamFileModel = paramFileModel;
        pluginTypeForDisplay = pluginType;
        pluginParamFileGuiSaveButton = paramFileGuiSaveButton;

        this.setLayout(new MigLayout("flowy"));

        fileDescriptionLabel = new JLabel();
        fileDescriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fileDescriptionLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        fileDescriptionLabel.setForeground(Color.BLACK);

        if(paramFileName.equals("") == false) {

            fileDescriptionLabel.setText("File: " + paramFileName);

        } else {

            fileDescriptionLabel.setText("File: " + xmlParameterFileNotCreated);

        }

        this.add(fileDescriptionLabel, "width 600!");
    }

    /**
     *
     *
     */
    public String getConfigModelName() {

        return configurationModelName;
    }

    /**
     *
     *
     */
    public void setXmlParameterFileDescriptionLabel(String filePath) {

        fileDescriptionLabel.setText("");
        fileDescriptionLabel.setText(filePath);
    }

    /**
     *
     *
     */
    public String getJarFileName() {

        return pluginJarFileName;
    }

    /**
     *
     *
     */
    public String getClassFileName() {

        return pluginClassName;
    }

    /**
     *
     *
     */
    public String getXsdFileName() {

        return pluginXsdFileName;
    }

    /**
     *
     *
     */
    public PluginType getPluginTypeToDisplay() {

        return pluginTypeForDisplay;
    }

    /**
     *
     *
     */
    public void updatePluginParamsGuiSaveButton() {

        if(checkPluginParamFileModelForUnsavedEdits()) {

            pluginParamFileGuiSaveButton.setEnabled(true);

        } else {

            pluginParamFileGuiSaveButton.setEnabled(false);
        }
    }

    /**
     *
     *
     */
    public void resetParamFileNameToDefaultColour() {

        fileDescriptionLabel.setForeground(Color.BLACK);
    }

    /**
     *
     *
     */
    private boolean checkPluginParamFileModelForUnsavedEdits() {

        boolean displayedParamFileModelHasEdits = false;

        displayedParamFileModelHasEdits = pluginParamFileModel.checkParameterModelForUnsavedEdits();
        if(displayedParamFileModelHasEdits) {

            fileDescriptionLabel.setForeground(Color.red);

        } else {

            fileDescriptionLabel.setForeground(Color.BLACK);
        }

        return displayedParamFileModelHasEdits;
    }
}
