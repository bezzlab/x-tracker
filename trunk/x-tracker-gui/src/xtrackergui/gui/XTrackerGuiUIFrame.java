
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerGuiUIFrame.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import xtrackergui.exceptions.XmlFileValidationFailure;
import xtrackergui.exceptions.XsdSchemaFileNotSet;
import xtrackergui.misc.LogTextAreaAppender;
import xtrackergui.model.XTrackeConfigModelManager;
import xtrackergui.model.XTrackerConfigData;
import xtrackergui.model.XTrackerConfigModel;
import xtrackergui.model.XTrackerParamFileModel;
import xtrackergui.model.XTrackerPluginInformation;
import xtrackergui.model.XTrackerXmlDocument;
import xtrackergui.utils.fileutils.FileUtils;
import xtrackergui.utils.guiutils.GuiUtils;
import xtrackergui.utils.guiutils.OverlayBorder;
import xtrackergui.utils.guiutils.PluginType;
import xtrackergui.utils.parsers.XmlParser;

/**
 * Main class for xTrackerGui
 *
 * @author andrew bullimore
 */
public class XTrackerGuiUIFrame extends javax.swing.JFrame {

    // Name of the X-TrackerGui XML configuration file
    private static final String xmlGuiConfigurationFileName = "xTrackerGuiConf.xml";
    // Keep all the information obtainable from each x-tracker plugin module
    private Map<PluginType, List<XTrackerPluginInformation>> xTrackerPluginInformation = null;
    private String currentModelName = "";
    private String currentModelFilePath = "";
    private String pluginDirectoryPath = "C:\\Users\\andrew\\Documents\\MScBioinformaticsThesis\\xTracker_v1_2\\xTracker_v1_2\\Plugins\\";
  //  private String pluginDirectoryPath = "Plugins/";
    private String fileDirectoryPath = "C:\\Users\\andrew\\Documents\\MScBioinformaticsThesis\\xTracker_v1_2\\xTracker_v1_2\\";
  //  private String fileDirectoryPath = "";
    // Keep a record of the plugins showing in the GUI combo boxes
    private String currRawDataPlugin = "";
    private String currSpecIdentDataPlugin = "";
    private String currPeakSelPlugin = "";
    private String currQuantPlugin = "";
    private String currOuputPlugin = "";
    // Record the current ComboBox/ Plugin panel being displayed
    private int currentHighlightedPanel = 0;
    private FileNameExtensionFilter xmlFileFilter = new FileNameExtensionFilter("XML file (*.xml)", "xml");
    // Controls the JTabbed pane displaying plugin parameter information
    private XTrackerPluginParamsUIController pluginParamsUIController;
    // The traversal policy for the ComboBoxes - causes problems, need to know more about this
 //   private XTrackerGuiFocusTraversalPolicy guiComboBoxFocusTraversalPolicy;
    // Controls logging information output to the information panel
    private Logger logger = Logger.getLogger(xtrackergui.gui.XTrackerGuiUIFrame.class.getName());
    
    /**
     *
     *
     */
    public XTrackerGuiUIFrame() {
        
        initComponents();

        informationTextArea.setEditable(false);

        // Properties for the Log4j logger - NB should be in a properties file
        // stored in the jar <==TODO
        Properties logProperties = new Properties();
        logProperties.put("log4j.rootLogger", "INFO, TEXTAREA");

	logProperties.put("log4j.appender.TEXTAREA", "xtrackergui.misc.LogTextAreaAppender");
	logProperties.put("log4j.appender.TEXTAREA.layout", "org.apache.log4j.PatternLayout");
	logProperties.put("log4j.appender.TEXTAREA.layout.ConversionPattern", "%5.5p:  %m%n");

	PropertyConfigurator.configure(logProperties);
        LogTextAreaAppender.setLogInfoTextArea(informationTextArea);

        pluginParamsUIController = new XTrackerPluginParamsUIController(pluginParamsTabbedPane,
                                                                        saveParamsFileButton);

        if(logger.isDebugEnabled()) {

            logger.debug("XTrackerGuiUIFrame: Plugin Path set to: " + pluginDirectoryPath);
            logger.debug("XTrackerGuiUIFrame: XML/XSD File Path set to: " + fileDirectoryPath);
        }

        // Initialise the combo boxes with the appropriate plugins
        // Once only initialisation at the moment, could create a
        // combobox model to keep an eye on the plugins directory
        // maybe, then if plugins are added or removed the combo boxes
        // would update without having to re-start the application
        //
        // Checking comboboxes load okay
        if(initComboBoxData()) {

            // set up the gui controls - whether enabled etc
            initGUIControls(false);

            // catching exceptions in constructor, not great
            try {

               File xTrackerGuiConfFile = FileUtils.getFile(fileDirectoryPath + xmlGuiConfigurationFileName);
               Map<String, String> guiConfigurationData = XmlParser.getXTrackerGuiConfigurationData(xTrackerGuiConfFile);

               if(guiConfigurationData.size() > 0) {

                   for(Map.Entry<PluginType, List<XTrackerPluginInformation>> entry : xTrackerPluginInformation.entrySet()) {

                       List<XTrackerPluginInformation> pluginInformationList = entry.getValue();
                       for(XTrackerPluginInformation pluginInfo : pluginInformationList) {

                           String xsdFileName = guiConfigurationData.get(pluginInfo.getPluginFileName());
                           pluginInfo.setXsdSchemaFileName(xsdFileName);

                           if(logger.isDebugEnabled()) {

                                logger.debug(pluginInfo);
                           }
                       }
                   }
               } else {

                   
               }

            } catch (FileNotFoundException ex) {

                logger.error(ex.getMessage());

            } catch (XmlFileValidationFailure ex) {

                logger.error(ex.getMessage());
            }
            
        } else {

            // If comboboxes not loaded, only allow user option to exit the screen and see
            // the error reported
            initGUIControls(true);
        }

        // Display some default information in the plugin display panels
        pluginParamsUIController.displayDefaultScreenAllForPlugins("", "<html><p>No Configuration Loaded</p><html>", 30);

     //   Vector<Component> guiComboBoxFocusTraversalPolicyOrder = new Vector<Component>(5);

    //    loadRawDataComboBox.setFocusCycleRoot(true);
        
    //    guiComboBoxFocusTraversalPolicyOrder.add(loadRawDataComboBox);
    //    guiComboBoxFocusTraversalPolicyOrder.add(spectralIdentComboBox);
  //      guiComboBoxFocusTraversalPolicyOrder.add(peakSelectionComboBox);
   //     guiComboBoxFocusTraversalPolicyOrder.add(quantificationComboBox);
   //     guiComboBoxFocusTraversalPolicyOrder.add(outputComboBox);
     //   guiComboBoxFocusTraversalPolicyOrder.add(saveConfigButton);
     //   guiComboBoxFocusTraversalPolicyOrder.add(runAppButton);

      //  guiComboBoxFocusTraversalPolicy = new XTrackerGuiFocusTraversalPolicy(guiComboBoxFocusTraversalPolicyOrder);
      //  setFocusTraversalPolicy(guiComboBoxFocusTraversalPolicy);

        // Set up frame closure policy

        // Set up the GUI exit policy
        WindowExitListener windowExitListner = new WindowExitListener();
        addWindowListener(windowExitListner);
    }

    /**
     *
     *
     */
    @Override
    public String toString() {

        return "XTrackerGuiUIFrame class";
    }

    /**
     *
     *
     */
    private XTrackerPluginInformation getXTrackerPluginInformation(PluginType pluginType, String pluginClassName) {

        XTrackerPluginInformation pluginInformation = null;

        List<XTrackerPluginInformation> pluginInformationForPluginType = xTrackerPluginInformation.get(pluginType);

        for(XTrackerPluginInformation pluginInfo : pluginInformationForPluginType) {

            if(pluginInfo.getPluginClassName().equals(pluginClassName)) {

                pluginInformation = pluginInfo;
            }
        }
        
        return pluginInformation;
    }

    /**
     *
     *
     */
    private String[] getPluginClassNameArrayByPluginType(PluginType pluginType) {

        List<XTrackerPluginInformation> pluginInformationForPluginType = xTrackerPluginInformation.get(pluginType);

        String[] pluginClassNameArrayForPluginType = new String[pluginInformationForPluginType.size() + 1];

        pluginClassNameArrayForPluginType[0] = "";
        int index = 1;
        for(XTrackerPluginInformation pluginInformation : pluginInformationForPluginType) {
            
            pluginClassNameArrayForPluginType[index] = pluginInformation.getPluginClassName();
            index++;
        }

        return pluginClassNameArrayForPluginType;
    }

    /**
     *
     *
     */
    private boolean initComboBoxData() {

        boolean comboBoxesOkay = true;

        try {
            // looks a bit ugly, but it'll be okay for now, sure theirs a fancy way
            // with generics but this works
            xTrackerPluginInformation = GuiUtils.getCompletePluginList(pluginDirectoryPath);

            loadRawDataComboBox.setModel(new DefaultComboBoxModel(getPluginClassNameArrayByPluginType(PluginType.RAWDATAPLUGIN)));
            spectralIdentComboBox.setModel(new DefaultComboBoxModel(getPluginClassNameArrayByPluginType(PluginType.IDENTDATAPLUGIN)));
            peakSelectionComboBox.setModel(new DefaultComboBoxModel(getPluginClassNameArrayByPluginType(PluginType.PEAKSELECTIONPLUGIN)));
            quantificationComboBox.setModel(new DefaultComboBoxModel(getPluginClassNameArrayByPluginType(PluginType.QUANTIFICATIONPLUGIN)));
            outputComboBox.setModel(new DefaultComboBoxModel(getPluginClassNameArrayByPluginType(PluginType.OUTPUTPLUGIN)));
            
        } catch (FileNotFoundException ex) {

            comboBoxesOkay = false;
            logger.error("No plugin information loaded - " + ex.getMessage());
        }

        return comboBoxesOkay;
    }

    /**
     *
     *
     */
    private void initGUIControls (boolean disableAll) {

        setTitle("");
        loadRawDataComboBox.setSelectedItem("");
        loadRawDataComboBox.setEnabled(false);
        spectralIdentComboBox.setSelectedItem("");
        spectralIdentComboBox.setEnabled(false);
        peakSelectionComboBox.setSelectedItem("");
        peakSelectionComboBox.setEnabled(false);
        quantificationComboBox.setSelectedItem("");
        quantificationComboBox.setEnabled(false);
        outputComboBox.setSelectedItem("");
        outputComboBox.setEnabled(false);

        closeMenuItem.setEnabled(false);

        newParamsFileButton.setEnabled(false);
        openParamsFileButton.setEnabled(false);
        saveParamsFileButton.setEnabled(false);

        runAppButton.setEnabled(false);

        if(disableAll) {

            newMenuItem.setEnabled(false);
            openConfigMenuItem.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pluginSelectionPanel = new javax.swing.JPanel();
        loadRawPanel = new javax.swing.JPanel();
        loadRawDataComboBox = new javax.swing.JComboBox();
        spectralIdentPanel = new javax.swing.JPanel();
        spectralIdentComboBox = new javax.swing.JComboBox();
        peakSelectionPanel = new javax.swing.JPanel();
        peakSelectionComboBox = new javax.swing.JComboBox();
        quantificationPanel = new javax.swing.JPanel();
        quantificationComboBox = new javax.swing.JComboBox();
        outputPanel = new javax.swing.JPanel();
        outputComboBox = new javax.swing.JComboBox();
        pluginParamsPanel = new javax.swing.JPanel();
        pluginParamsTabbedPane = new javax.swing.JTabbedPane();
        loadRawScrollPane = new javax.swing.JScrollPane();
        spectralIdentScrollPane = new javax.swing.JScrollPane();
        peakSelScrollPane = new javax.swing.JScrollPane();
        quantScrollPane = new javax.swing.JScrollPane();
        outputScrollPane = new javax.swing.JScrollPane();
        pluginParamsButtonPanel = new javax.swing.JPanel();
        openParamsFileButton = new javax.swing.JButton();
        saveParamsFileButton = new javax.swing.JButton();
        newParamsFileButton = new javax.swing.JButton();
        runAppPanel = new javax.swing.JPanel();
        runAppButton = new javax.swing.JButton();
        informationPanel = new javax.swing.JPanel();
        informationScrollPane = new javax.swing.JScrollPane();
        informationTextArea = new javax.swing.JTextArea();
        UIMenuBar = new javax.swing.JMenuBar();
        configMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openConfigMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        pluginSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plugin Configuration"));
        pluginSelectionPanel.setMaximumSize(new java.awt.Dimension(234, 400));
        pluginSelectionPanel.setMinimumSize(new java.awt.Dimension(234, 400));
        pluginSelectionPanel.setPreferredSize(new java.awt.Dimension(234, 400));

        loadRawPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Load Raw Data "));
        loadRawPanel.setMaximumSize(new java.awt.Dimension(202, 60));
        loadRawPanel.setMinimumSize(new java.awt.Dimension(202, 60));
        loadRawPanel.setPreferredSize(new java.awt.Dimension(202, 60));
        loadRawPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadRawPanelMouseClicked(evt);
            }
        });

        loadRawDataComboBox.setToolTipText("Select the plugin to load raw data information ");
        loadRawDataComboBox.setMaximumSize(new java.awt.Dimension(170, 20));
        loadRawDataComboBox.setMinimumSize(new java.awt.Dimension(170, 20));
        loadRawDataComboBox.setPreferredSize(new java.awt.Dimension(170, 20));
        loadRawDataComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadRawDataComboBoxMouseClicked(evt);
            }
        });
        loadRawDataComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                loadRawDataComboBoxItemStateChanged(evt);
            }
        });
        loadRawDataComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadRawDataComboBoxActionPerformed(evt);
            }
        });
        loadRawDataComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                loadRawDataComboBoxFocusGained(evt);
            }
        });

        javax.swing.GroupLayout loadRawPanelLayout = new javax.swing.GroupLayout(loadRawPanel);
        loadRawPanel.setLayout(loadRawPanelLayout);
        loadRawPanelLayout.setHorizontalGroup(
            loadRawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadRawPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loadRawDataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        loadRawPanelLayout.setVerticalGroup(
            loadRawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loadRawPanelLayout.createSequentialGroup()
                .addComponent(loadRawDataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        spectralIdentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectal Identitifications"));
        spectralIdentPanel.setMaximumSize(new java.awt.Dimension(202, 60));
        spectralIdentPanel.setMinimumSize(new java.awt.Dimension(202, 60));
        spectralIdentPanel.setPreferredSize(new java.awt.Dimension(202, 60));
        spectralIdentPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spectralIdentPanelMouseClicked(evt);
            }
        });

        spectralIdentComboBox.setToolTipText("Select the plugin to load protein/ peptide identifications");
        spectralIdentComboBox.setMaximumSize(new java.awt.Dimension(170, 20));
        spectralIdentComboBox.setMinimumSize(new java.awt.Dimension(170, 20));
        spectralIdentComboBox.setPreferredSize(new java.awt.Dimension(170, 20));
        spectralIdentComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spectralIdentComboBoxMouseClicked(evt);
            }
        });
        spectralIdentComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                spectralIdentComboBoxItemStateChanged(evt);
            }
        });
        spectralIdentComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spectralIdentComboBoxActionPerformed(evt);
            }
        });
        spectralIdentComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                spectralIdentComboBoxFocusGained(evt);
            }
        });

        javax.swing.GroupLayout spectralIdentPanelLayout = new javax.swing.GroupLayout(spectralIdentPanel);
        spectralIdentPanel.setLayout(spectralIdentPanelLayout);
        spectralIdentPanelLayout.setHorizontalGroup(
            spectralIdentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectralIdentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spectralIdentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        spectralIdentPanelLayout.setVerticalGroup(
            spectralIdentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spectralIdentPanelLayout.createSequentialGroup()
                .addComponent(spectralIdentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        peakSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peak Selection"));
        peakSelectionPanel.setMaximumSize(new java.awt.Dimension(202, 60));
        peakSelectionPanel.setMinimumSize(new java.awt.Dimension(202, 60));
        peakSelectionPanel.setPreferredSize(new java.awt.Dimension(202, 60));
        peakSelectionPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peakSelectionPanelMouseClicked(evt);
            }
        });

        peakSelectionComboBox.setToolTipText("Select the plugin to select peaks to quantitate in the raw data");
        peakSelectionComboBox.setMaximumSize(new java.awt.Dimension(170, 20));
        peakSelectionComboBox.setMinimumSize(new java.awt.Dimension(170, 20));
        peakSelectionComboBox.setPreferredSize(new java.awt.Dimension(170, 20));
        peakSelectionComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peakSelectionComboBoxMouseClicked(evt);
            }
        });
        peakSelectionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                peakSelectionComboBoxItemStateChanged(evt);
            }
        });
        peakSelectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peakSelectionComboBoxActionPerformed(evt);
            }
        });
        peakSelectionComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                peakSelectionComboBoxFocusGained(evt);
            }
        });

        javax.swing.GroupLayout peakSelectionPanelLayout = new javax.swing.GroupLayout(peakSelectionPanel);
        peakSelectionPanel.setLayout(peakSelectionPanelLayout);
        peakSelectionPanelLayout.setHorizontalGroup(
            peakSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peakSelectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peakSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        peakSelectionPanelLayout.setVerticalGroup(
            peakSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peakSelectionPanelLayout.createSequentialGroup()
                .addComponent(peakSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        quantificationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Quantification"));
        quantificationPanel.setMaximumSize(new java.awt.Dimension(202, 60));
        quantificationPanel.setMinimumSize(new java.awt.Dimension(202, 60));
        quantificationPanel.setPreferredSize(new java.awt.Dimension(202, 60));
        quantificationPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                quantificationPanelMouseClicked(evt);
            }
        });

        quantificationComboBox.setToolTipText("Select the plugin to quantify the amount of protein/ peptide");
        quantificationComboBox.setMaximumSize(new java.awt.Dimension(170, 20));
        quantificationComboBox.setMinimumSize(new java.awt.Dimension(170, 20));
        quantificationComboBox.setPreferredSize(new java.awt.Dimension(170, 20));
        quantificationComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                quantificationComboBoxMouseClicked(evt);
            }
        });
        quantificationComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                quantificationComboBoxItemStateChanged(evt);
            }
        });
        quantificationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantificationComboBoxActionPerformed(evt);
            }
        });
        quantificationComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                quantificationComboBoxFocusGained(evt);
            }
        });

        javax.swing.GroupLayout quantificationPanelLayout = new javax.swing.GroupLayout(quantificationPanel);
        quantificationPanel.setLayout(quantificationPanelLayout);
        quantificationPanelLayout.setHorizontalGroup(
            quantificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quantificationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(quantificationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        quantificationPanelLayout.setVerticalGroup(
            quantificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quantificationPanelLayout.createSequentialGroup()
                .addComponent(quantificationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));
        outputPanel.setMaximumSize(new java.awt.Dimension(202, 60));
        outputPanel.setMinimumSize(new java.awt.Dimension(202, 60));
        outputPanel.setPreferredSize(new java.awt.Dimension(202, 60));
        outputPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                outputPanelMouseClicked(evt);
            }
        });

        outputComboBox.setToolTipText("Select the plugin to display the results of quantification");
        outputComboBox.setMaximumSize(new java.awt.Dimension(170, 20));
        outputComboBox.setMinimumSize(new java.awt.Dimension(170, 20));
        outputComboBox.setPreferredSize(new java.awt.Dimension(170, 20));
        outputComboBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                outputComboBoxMouseClicked(evt);
            }
        });
        outputComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                outputComboBoxItemStateChanged(evt);
            }
        });
        outputComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputComboBoxActionPerformed(evt);
            }
        });
        outputComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                outputComboBoxFocusGained(evt);
            }
        });

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outputComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addComponent(outputComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pluginSelectionPanelLayout = new javax.swing.GroupLayout(pluginSelectionPanel);
        pluginSelectionPanel.setLayout(pluginSelectionPanelLayout);
        pluginSelectionPanelLayout.setHorizontalGroup(
            pluginSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginSelectionPanelLayout.createSequentialGroup()
                .addGroup(pluginSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pluginSelectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(outputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pluginSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(quantificationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pluginSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pluginSelectionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(peakSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pluginSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(spectralIdentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pluginSelectionPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(loadRawPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pluginSelectionPanelLayout.setVerticalGroup(
            pluginSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginSelectionPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(loadRawPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spectralIdentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peakSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(quantificationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(121, 121, 121))
        );

        pluginParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Plugin Parameters"));
        pluginParamsPanel.setMaximumSize(new java.awt.Dimension(720, 541));
        pluginParamsPanel.setMinimumSize(new java.awt.Dimension(720, 541));
        pluginParamsPanel.setPreferredSize(new java.awt.Dimension(720, 541));

        pluginParamsTabbedPane.setToolTipText("");
        pluginParamsTabbedPane.setMaximumSize(new java.awt.Dimension(700, 460));
        pluginParamsTabbedPane.setMinimumSize(new java.awt.Dimension(700, 460));
        pluginParamsTabbedPane.setPreferredSize(new java.awt.Dimension(700, 460));
        pluginParamsTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pluginParamsTabbedPaneStateChanged(evt);
            }
        });
        pluginParamsTabbedPane.addTab("Load Raw Data", loadRawScrollPane);
        pluginParamsTabbedPane.addTab("Spectral Identification", spectralIdentScrollPane);
        pluginParamsTabbedPane.addTab("Peak Selection", peakSelScrollPane);
        pluginParamsTabbedPane.addTab("Quantification", quantScrollPane);
        pluginParamsTabbedPane.addTab("Output", outputScrollPane);

        pluginParamsButtonPanel.setMaximumSize(new java.awt.Dimension(688, 23));
        pluginParamsButtonPanel.setMinimumSize(new java.awt.Dimension(688, 23));

        openParamsFileButton.setText("Open");
        openParamsFileButton.setToolTipText("Open an existing XML parameter file for the plugin selected");
        openParamsFileButton.setFocusable(false);
        openParamsFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openParamsFileButtonActionPerformed(evt);
            }
        });

        saveParamsFileButton.setText("Save");
        saveParamsFileButton.setToolTipText("Save edits to the parameter data showing in the selected tab ");
        saveParamsFileButton.setFocusable(false);
        saveParamsFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveParamsFileButtonActionPerformed(evt);
            }
        });

        newParamsFileButton.setText("New");
        newParamsFileButton.setToolTipText("Create a new XML parameter file for the plugin selected");
        newParamsFileButton.setFocusable(false);
        newParamsFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newParamsFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pluginParamsButtonPanelLayout = new javax.swing.GroupLayout(pluginParamsButtonPanel);
        pluginParamsButtonPanel.setLayout(pluginParamsButtonPanelLayout);
        pluginParamsButtonPanelLayout.setHorizontalGroup(
            pluginParamsButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginParamsButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newParamsFileButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openParamsFileButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 493, Short.MAX_VALUE)
                .addComponent(saveParamsFileButton)
                .addContainerGap())
        );
        pluginParamsButtonPanelLayout.setVerticalGroup(
            pluginParamsButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginParamsButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(saveParamsFileButton)
                .addComponent(openParamsFileButton)
                .addComponent(newParamsFileButton))
        );

        javax.swing.GroupLayout pluginParamsPanelLayout = new javax.swing.GroupLayout(pluginParamsPanel);
        pluginParamsPanel.setLayout(pluginParamsPanelLayout);
        pluginParamsPanelLayout.setHorizontalGroup(
            pluginParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pluginParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pluginParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pluginParamsTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 688, Short.MAX_VALUE)
                    .addComponent(pluginParamsButtonPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pluginParamsPanelLayout.setVerticalGroup(
            pluginParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pluginParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pluginParamsTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(9, 9, 9)
                .addComponent(pluginParamsButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        runAppPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Run Application"));
        runAppPanel.setMaximumSize(new java.awt.Dimension(234, 60));
        runAppPanel.setMinimumSize(new java.awt.Dimension(234, 60));
        runAppPanel.setPreferredSize(new java.awt.Dimension(234, 60));

        runAppButton.setMnemonic(KeyEvent.VK_R);
        runAppButton.setText("Run xTracker");
        runAppButton.setToolTipText("Save any edits and run the X-Tracker quantification pipeline");
        runAppButton.setFocusable(false);
        runAppButton.setMaximumSize(new java.awt.Dimension(150, 25));
        runAppButton.setMinimumSize(new java.awt.Dimension(150, 25));
        runAppButton.setPreferredSize(new java.awt.Dimension(150, 25));
        runAppButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runAppButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout runAppPanelLayout = new javax.swing.GroupLayout(runAppPanel);
        runAppPanel.setLayout(runAppPanelLayout);
        runAppPanelLayout.setHorizontalGroup(
            runAppPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runAppPanelLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(runAppButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        runAppPanelLayout.setVerticalGroup(
            runAppPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runAppPanelLayout.createSequentialGroup()
                .addComponent(runAppButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        informationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));
        informationPanel.setMaximumSize(new java.awt.Dimension(964, 143));
        informationPanel.setMinimumSize(new java.awt.Dimension(964, 143));

        informationTextArea.setColumns(20);
        informationTextArea.setFont(new java.awt.Font("Tahoma", 0, 14));
        informationTextArea.setRows(5);
        informationTextArea.setMaximumSize(new java.awt.Dimension(244, 89));
        informationTextArea.setMinimumSize(new java.awt.Dimension(244, 89));
        informationScrollPane.setViewportView(informationTextArea);

        javax.swing.GroupLayout informationPanelLayout = new javax.swing.GroupLayout(informationPanel);
        informationPanel.setLayout(informationPanelLayout);
        informationPanelLayout.setHorizontalGroup(
            informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, informationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(informationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 932, Short.MAX_VALUE)
                .addContainerGap())
        );
        informationPanelLayout.setVerticalGroup(
            informationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(informationPanelLayout.createSequentialGroup()
                .addComponent(informationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addContainerGap())
        );

        configMenu.setMnemonic(KeyEvent.VK_C);
        configMenu.setText("Configuration");
        configMenu.setFont(new java.awt.Font("Tahoma", 0, 12));

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setFont(new java.awt.Font("Tahoma", 0, 12));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        configMenu.add(newMenuItem);

        openConfigMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openConfigMenuItem.setFont(new java.awt.Font("Tahoma", 0, 12));
        openConfigMenuItem.setText("Open");
        openConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openConfigMenuItemActionPerformed(evt);
            }
        });
        configMenu.add(openConfigMenuItem);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setFont(new java.awt.Font("Tahoma", 0, 12));
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        configMenu.add(closeMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setFont(new java.awt.Font("Tahoma", 0, 12));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        configMenu.add(exitMenuItem);

        UIMenuBar.add(configMenu);

        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.setText("Help");
        helpMenu.setFont(new java.awt.Font("Tahoma", 0, 12));

        helpContentsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        helpContentsMenuItem.setFont(new java.awt.Font("Tahoma", 0, 12));
        helpContentsMenuItem.setText("Help Contents");
        helpContentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpContentsMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpContentsMenuItem);

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.SHIFT_MASK));
        aboutMenuItem.setFont(new java.awt.Font("Tahoma", 0, 12));
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        UIMenuBar.add(helpMenu);

        setJMenuBar(UIMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(informationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(runAppPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pluginSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pluginParamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pluginSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runAppPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pluginParamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(informationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     *
     *
     */
    private void openConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openConfigMenuItemActionPerformed

        // When we open a configuration, we opening an XML file, not the
        // configuration schema. We can find the schema via the XML file
        if (evt.getSource() instanceof JMenuItem) {

            boolean openConfigModel = true;

            XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
            if(configModelManager.checkXtrackerConfigModelForUnSavedChanges(currentModelName)) {
                    
                Object[] buttonOptions = {"Save", "Don't Save", "Cancel"};
                int retOption = JOptionPane.showOptionDialog(this,
                                                             "Configuration " + currentModelName + " has changes. Save to file?",
                                                             "Save Configuration to File",
                                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             buttonOptions,
                                                             buttonOptions[0]);
                    
                if(retOption == JOptionPane.YES_OPTION) {
                    // do this lot in its own thread?
                    configModelManager.saveXTrackerConfigModel(currentModelName);
                    configModelManager.closeXTrackerConfigModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        logger.info("Closing X-Tracker configuration " + currentModelName + " (Edits saved)");
                    }
                    
                    currentModelName = "";
                    
                } else if(retOption == JOptionPane.NO_OPTION) {

                    configModelManager.closeXTrackerConfigModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        logger.info("Closing X-Tracker configuration " + currentModelName + " (Edits not saved)");
                    }

                    currentModelName = "";

                } else if(retOption == JOptionPane.CANCEL_OPTION) {

                    openConfigModel = false;
                }
            } else {

                // no changes just go ahead and close down this config.
                // If one was open maybe could do all this within the openConfigModel block below but prefer it here
                if(currentModelName.equals("") == false) {

                    currentModelName = "";
                }
            }

            if(currentModelName.equals("") == true) {

                currRawDataPlugin = "";
                currSpecIdentDataPlugin = "";
                currPeakSelPlugin = "";
                currQuantPlugin = "";
                currOuputPlugin = "";

                setTitle("");
                pluginParamsUIController.displayDefaultScreenAllForPlugins("", "<html><p>No Configuration Loaded</p><html>", 30);

                closeMenuItem.setEnabled(false);

                loadRawDataComboBox.setSelectedItem("");
                loadRawDataComboBox.setEnabled(false);
                spectralIdentComboBox.setSelectedItem("");
                spectralIdentComboBox.setEnabled(false);
                peakSelectionComboBox.setSelectedItem("");
                peakSelectionComboBox.setEnabled(false);
                quantificationComboBox.setSelectedItem("");
                quantificationComboBox.setEnabled(false);
                outputComboBox.setSelectedItem("");
                outputComboBox.setEnabled(false);
                restoreBorderForPluginPanel(currentHighlightedPanel);

                newParamsFileButton.setEnabled(false);
                openParamsFileButton.setEnabled(false);
                saveParamsFileButton.setEnabled(false);

                runAppButton.setEnabled(false);
            }

            if(openConfigModel) {

                boolean problemConfiguration = false;

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Open X-Tracker Configuration");
                fileChooser.addChoosableFileFilter(xmlFileFilter);
                int returnVal = fileChooser.showOpenDialog(this);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    
                    File configurationFile = fileChooser.getSelectedFile();
                    try {

                        currentModelName = configModelManager.openConfigurationModel(fileDirectoryPath, configurationFile);
                        currentModelFilePath = configurationFile.getCanonicalPath();

                        setTitle(currentModelFilePath);

                        closeMenuItem.setEnabled(true);

                        XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);

                        if(logger.isInfoEnabled()) {

                            logger.info("Opening X-Tracker configuration " + currentModelName);
                        }

                        loadRawDataComboBox.setEnabled(true);
                        String loadRawDataJarFileName = currConfigModel.getPluginJarFileByPluginType(PluginType.RAWDATAPLUGIN);
                        String loadRawDataClassName = FileUtils.getFileNameWithoutExtension(loadRawDataJarFileName);
                        XTrackerPluginInformation currRawDataPluginInfo = getXTrackerPluginInformation(PluginType.RAWDATAPLUGIN,
                                                                                                       loadRawDataClassName);
                        if(currRawDataPluginInfo != null) {

                            File loadRawXsdFile = null;
                            String loadRawXsdFileName = currRawDataPluginInfo.getXsdSchemaFileName();

                            try {

                                if(loadRawXsdFileName != null) {

                                    loadRawXsdFile = FileUtils.getFile(fileDirectoryPath + loadRawXsdFileName);


                                    currConfigModel.createXTrackerParamFileModel(PluginType.RAWDATAPLUGIN,
                                                                                 fileDirectoryPath,
                                                                                 loadRawXsdFileName,
                                                                                 loadRawXsdFile);
                                }

                            } catch (FileNotFoundException ex) {

                                logger.error("Cannot find file " + ex.getMessage());
                            }

                            loadRawDataComboBox.setSelectedItem(loadRawDataClassName);
                            currRawDataPlugin = loadRawDataClassName;

                            if(logger.isInfoEnabled()) {

                                logger.info("X-Tracker configuration " + currentModelName +
                                            " plugin " + loadRawDataJarFileName + " set");
                            }

                        } else {

                            loadRawDataComboBox.setSelectedItem("");
                            currRawDataPlugin = "";

                            if(problemConfiguration == false) {

                                problemConfiguration = true;
                            }

                            logger.warn("Failed to find plugin " + loadRawDataJarFileName);
                        }

                        spectralIdentComboBox.setEnabled(true);
                        String spectralIdentJarFileName = currConfigModel.getPluginJarFileByPluginType(PluginType.IDENTDATAPLUGIN);
                        String spectralIdentClassName = FileUtils.getFileNameWithoutExtension(spectralIdentJarFileName);
                        XTrackerPluginInformation currSpectralIdentPluginInfo = getXTrackerPluginInformation(PluginType.IDENTDATAPLUGIN,
                                                                                                             spectralIdentClassName);
                        if(currSpectralIdentPluginInfo != null) {

                            File spectralIdentXsdFile = null;
                            String spectralIdentXsdFileName = currSpectralIdentPluginInfo.getXsdSchemaFileName();

                            try {

                                if(spectralIdentXsdFileName != null) {

                                    spectralIdentXsdFile = FileUtils.getFile(fileDirectoryPath + spectralIdentXsdFileName);

                                    currConfigModel.createXTrackerParamFileModel(PluginType.IDENTDATAPLUGIN,
                                                                             fileDirectoryPath,
                                                                             spectralIdentXsdFileName,
                                                                             spectralIdentXsdFile);
                                }

                            } catch (FileNotFoundException ex) {

                                logger.error("Cannot find file " + ex.getMessage());
                            }

                            spectralIdentComboBox.setSelectedItem(spectralIdentClassName);
                            currSpecIdentDataPlugin = spectralIdentClassName;

                            if(logger.isInfoEnabled()) {

                                logger.info("X-Tracker configuration " + currentModelName +
                                            " plugin " + spectralIdentJarFileName + " set");
                            }

                        } else {

                            spectralIdentComboBox.setSelectedItem("");
                            currSpecIdentDataPlugin = "";

                            if(problemConfiguration == false) {

                                problemConfiguration = true;
                            }

                            logger.warn("Failed to find plugin " + spectralIdentJarFileName);
                        }

                        peakSelectionComboBox.setEnabled(true);
                        String peakSelectionJarFileName = currConfigModel.getPluginJarFileByPluginType(PluginType.PEAKSELECTIONPLUGIN);
                        String peakSelectionClassName = FileUtils.getFileNameWithoutExtension(peakSelectionJarFileName);
                        XTrackerPluginInformation currPeakSelectionPluginInfo = getXTrackerPluginInformation(PluginType.PEAKSELECTIONPLUGIN,
                                                                                                             peakSelectionClassName);
                        if(currPeakSelectionPluginInfo != null) {

                            File peakSelectionXsdFile = null;
                            String peakSelectionXsdFileName = currPeakSelectionPluginInfo.getXsdSchemaFileName();
                            try {

                                if(peakSelectionXsdFileName != null) {

                                    peakSelectionXsdFile = FileUtils.getFile(fileDirectoryPath + peakSelectionXsdFileName);


                                    currConfigModel.createXTrackerParamFileModel(PluginType.PEAKSELECTIONPLUGIN,
                                                                                 fileDirectoryPath,
                                                                                 peakSelectionXsdFileName,
                                                                                 peakSelectionXsdFile);
                                }

                            } catch (FileNotFoundException ex) {

                                logger.error("Cannot find file " + ex.getMessage());
                            }

                            peakSelectionComboBox.setSelectedItem(peakSelectionClassName);
                            currPeakSelPlugin = peakSelectionClassName;

                            if(logger.isInfoEnabled()) {

                                logger.info("X-Tracker configuration " + currentModelName +
                                            " plugin " + peakSelectionJarFileName + " set");
                            }

                        } else {

                            peakSelectionComboBox.setSelectedItem("");
                            currPeakSelPlugin = "";

                            if(problemConfiguration == false) {

                                problemConfiguration = true;
                            }

                            logger.warn("Failed to find plugin " + peakSelectionJarFileName);
                        }

                        quantificationComboBox.setEnabled(true);
                        String quantificationJarFileName = currConfigModel.getPluginJarFileByPluginType(PluginType.QUANTIFICATIONPLUGIN);
                        String quantificationClassName = FileUtils.getFileNameWithoutExtension(quantificationJarFileName);
                        XTrackerPluginInformation currQuantificationPluginInfo = getXTrackerPluginInformation(PluginType.QUANTIFICATIONPLUGIN,
                                                                                                              quantificationClassName);
                        if(currQuantificationPluginInfo != null) {

                            File quantificationXsdFile = null;
                            String quantificationXsdFileName = currQuantificationPluginInfo.getXsdSchemaFileName();
                            try {

                                if(quantificationXsdFileName != null) {

                                    quantificationXsdFile = FileUtils.getFile(fileDirectoryPath + quantificationXsdFileName);

                                    currConfigModel.createXTrackerParamFileModel(PluginType.QUANTIFICATIONPLUGIN,
                                                                                 fileDirectoryPath,
                                                                                 quantificationXsdFileName,
                                                                                 quantificationXsdFile);
                                }

                            } catch (FileNotFoundException ex) {

                                logger.error("Cannot find file " + ex.getMessage());
                            }

                            quantificationComboBox.setSelectedItem(quantificationClassName);
                            currQuantPlugin = quantificationClassName;

                            if(logger.isInfoEnabled()) {

                                logger.info("X-Tracker configuration " + currentModelName +
                                            " plugin " + quantificationJarFileName + " set");
                            }

                        } else {

                            quantificationComboBox.setSelectedItem("");
                            currQuantPlugin = "";

                            if(problemConfiguration == false) {

                                problemConfiguration = true;
                            }

                            logger.warn("Failed to find plugin " + quantificationJarFileName);
                        }

                        outputComboBox.setEnabled(true);
                        String outputJarFileName = currConfigModel.getPluginJarFileByPluginType(PluginType.OUTPUTPLUGIN);
                        String outputClassName = FileUtils.getFileNameWithoutExtension(outputJarFileName);
                        XTrackerPluginInformation currOutputPluginInfo = getXTrackerPluginInformation(PluginType.OUTPUTPLUGIN,
                                                                                                      outputClassName);
                        if(currOutputPluginInfo != null) {

                            File outputXsdFile = null;
                            String outputXsdFileName = currOutputPluginInfo.getXsdSchemaFileName();
                            try {

                                if(outputXsdFileName != null) {

                                    outputXsdFile = FileUtils.getFile(fileDirectoryPath + outputXsdFileName);


                                    currConfigModel.createXTrackerParamFileModel(PluginType.OUTPUTPLUGIN,
                                                                                 fileDirectoryPath,
                                                                                 outputXsdFileName,
                                                                                 outputXsdFile);
                                }

                            } catch (FileNotFoundException ex) {

                                logger.error("Cannot find file " + ex.getMessage());
                            }

                            outputComboBox.setSelectedItem(outputClassName);
                            currOuputPlugin = outputClassName;

                            if(logger.isInfoEnabled()) {

                                logger.info("X-Tracker configuration " + currentModelName +
                                            " plugin " + outputJarFileName + " set");
                            }

                        } else {

                            outputComboBox.setSelectedItem("");
                            currOuputPlugin = "";

                            if(problemConfiguration == false) {

                                problemConfiguration = true;
                            }

                            logger.warn("Failed to find plugin " + outputJarFileName);
                        }

                        runAppButton.setEnabled(true);

                    } catch (FileNotFoundException ex) {

                        // dunno if this is a good idea but it does something at least
                        problemConfiguration = true;

                    } catch (IOException ex) {

                        // dunno if this is a good idea but it does something at least IOException & FileNotFound mmmm??
                        problemConfiguration = true;

                    } catch (XsdSchemaFileNotSet ex) {

                        // dunno if this is a good idea but it does something at least
                        problemConfiguration = true;
                        
                    } catch (XmlFileValidationFailure ex) {

                     /*   JOptionPane.showMessageDialog(this,
                                                      "Cannot open file " +
                                                      configurationFile.getName(),
                                                      "Configuration File is Invalid - Please check and be sure to open a X-Tracker configuration file",
                                                      JOptionPane.WARNING_MESSAGE); */
                        JOptionPane.showMessageDialog(this,
                                                      ex,
                                                      "Configuration File is Invalid",
                                                      JOptionPane.WARNING_MESSAGE);
                    }
                }

                if(problemConfiguration == true) {

                    JOptionPane.showMessageDialog(this,
                                                  "Problems were encountered opening configuration " + currentModelName,
                                                  "Problems Opening Configuration",
                                                  JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_openConfigMenuItemActionPerformed

    /**
     *
     *
     */
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed

        if (evt.getSource() instanceof JMenuItem) {

            boolean closeConfigModel = true;
            String editInfo = "";

            XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
            if(configModelManager.checkXtrackerConfigModelForUnSavedChanges(currentModelName)) {

                Object[] buttonOptions = {"Save", "Don't Save", "Cancel"};
                int retOption = JOptionPane.showOptionDialog(this,
                                                             "Configuration " + currentModelName + " has changes. Save to file?",
                                                             "Save Configuration to File And Close",
                                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             buttonOptions,
                                                             buttonOptions[0]);

                if(retOption == JOptionPane.YES_OPTION) {
                    
                    // do this lot in its own thread?
                    configModelManager.saveXTrackerConfigModel(currentModelName);
                    configModelManager.closeXTrackerConfigModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        editInfo = " (Edits saved)";
                    }

                } else if(retOption == JOptionPane.NO_OPTION) {

                    configModelManager.closeXTrackerConfigModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        editInfo = " (Edits not saved)";
                    }

                } else if(retOption == JOptionPane.CANCEL_OPTION) {

                    closeConfigModel = false;
                }
            }
            
            if(closeConfigModel) {

                if(logger.isInfoEnabled()) {

                    logger.info("Closing X-Tracker configuration " + currentModelName + editInfo);
                }

                currentModelName = "";
                currRawDataPlugin = "";
                currSpecIdentDataPlugin = "";
                currPeakSelPlugin = "";
                currQuantPlugin = "";
                currOuputPlugin = "";
                
                setTitle("");
                pluginParamsUIController.displayDefaultScreenAllForPlugins("", "<html><p>No Configuration Loaded</p><html>", 30);

                closeMenuItem.setEnabled(false);

                loadRawDataComboBox.setSelectedItem("");
                loadRawDataComboBox.setEnabled(false);
                spectralIdentComboBox.setSelectedItem("");
                spectralIdentComboBox.setEnabled(false);
                peakSelectionComboBox.setSelectedItem("");
                peakSelectionComboBox.setEnabled(false);
                quantificationComboBox.setSelectedItem("");
                quantificationComboBox.setEnabled(false);
                outputComboBox.setSelectedItem("");
                outputComboBox.setEnabled(false);
                restoreBorderForPluginPanel(currentHighlightedPanel);

                newParamsFileButton.setEnabled(false);
                openParamsFileButton.setEnabled(false);
                saveParamsFileButton.setEnabled(false);

                runAppButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_closeMenuItemActionPerformed

    /**
     *
     *
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        if (evt.getSource() instanceof JMenuItem) {

            if(exitXtrackerUI()) {

                System.exit(0);
            }
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     *
     *
     */
    private String getNewXTrackerConfigurationNameViaJOptionPane() {

        String inputString = new String("");
        boolean cancelPressed = false;
        boolean inputEntered = false;

        do {

            inputString = JOptionPane.showInputDialog(this,
                                                      "Enter name (no spaces or file extensions)",
                                                      "New xTracker Configuration Name",
                                                      JOptionPane.QUESTION_MESSAGE);

            if(inputString != null) {

                if(inputString.length() > 0) {

                    // Test inputString for white space or period '.', if split
                    // returns more than one string then the input is incorrect
                    // or so it is hoped, for now.
                    String inputStringSplit[] = inputString.split("[ .]");
                    if(inputStringSplit.length == 1) {

                        inputEntered = true;
                    }
                }
            } else {
                    
                // cancel pressed
                cancelPressed = true;
            }          
        } while(inputEntered == false && cancelPressed == false);

        return inputString;
    }

    /**
     *
     *
     */
    private File getNewXTrackerConfigurationFile() {

        File xTrackerConfigFile = null;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save new X-Tracker Configuration");
        fileChooser.addChoosableFileFilter(xmlFileFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        try {

            fileChooser.setCurrentDirectory(new File(new File(".").getCanonicalPath()));

        } catch (IOException ex) {

            logger.error("Cannot set current working directory " + ex.getMessage());
        }

        int returnVal = fileChooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();

            if(FileUtils.getFileNameExtension(file).equals("xml") == false) {

                try {

                    xTrackerConfigFile = new File(file.getCanonicalPath() + ".xml");

                } catch (IOException ex) {

                    logger.error("Cannot create new file with extension " + ex.getMessage());

                }
            } 
        }

        return xTrackerConfigFile;
    }

    /**
     *
     *
     */
    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed

        if (evt.getSource() instanceof JMenuItem) {

            boolean newConfigModel = true;

            XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
            if(configModelManager.checkXtrackerConfigModelForUnSavedChanges(currentModelName)) {

                Object[] buttonOptions = {"Save", "Don't Save", "Cancel"};
                int retOption = JOptionPane.showOptionDialog(this,
                                                             "Configuration " + currentModelName + " has changes. Save to file?",
                                                             "Save Configuration to File",
                                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null,
                                                             buttonOptions,
                                                             buttonOptions[0]);

                if(retOption == JOptionPane.YES_OPTION) {

                    // do this lot in its own thread?
                    configModelManager.saveXTrackerConfigModel(currentModelName);
                    configModelManager.closeXTrackerConfigModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        logger.info("Closing X-Tracker configuration " + currentModelName + " (Edits saved)");
                    }

                } else if(retOption == JOptionPane.NO_OPTION) {

                    configModelManager.closeXTrackerConfigModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        logger.info("Closing X-Tracker configuration " + currentModelName + " (Edits not saved)");
                    }

                } else if(retOption == JOptionPane.CANCEL_OPTION) {

                    newConfigModel = false;
                }
            }

            if(newConfigModel) {

              //  String newConfigName = getNewXTrackerConfigurationNameViaJOptionPane();
                File newConfigFile = getNewXTrackerConfigurationFile();

             //   if(newConfigName != null && newConfigName.equals(new String("")) == false) {
                if(newConfigFile != null) {

                    currentModelName = newConfigFile.getName();

                    try {
                        
                        currentModelFilePath = newConfigFile.getCanonicalPath();
                        
                    } catch (IOException ex) {

                        logger.error(ex.getMessage());
                    }

                    configModelManager.createNewConfigurationModel(currentModelName);

                    if(logger.isInfoEnabled()) {

                        logger.info("Creating X-Tracker configuration " + currentModelName);
                    }

                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    XTrackerConfigData currConfigModelConfigData = currConfigModel.getModelConfigData();

                    Document configDataDomDocument = XmlParser.createXmlConfigurationDomDocument(currConfigModelConfigData);
                    XmlParser.writeDomDocumentToXmlFile(configDataDomDocument, newConfigFile);
                    currConfigModelConfigData.setConfigDataDomDocument(configDataDomDocument);
                    currConfigModel.setConfigurationFile(newConfigFile);

                    setTitle(currentModelFilePath);

                    closeMenuItem.setEnabled(true);

                    loadRawDataComboBox.setEnabled(true);
                    loadRawDataComboBox.setSelectedItem("");
                    spectralIdentComboBox.setEnabled(true);
                    spectralIdentComboBox.setSelectedItem("");
                    peakSelectionComboBox.setEnabled(true);
                    peakSelectionComboBox.setSelectedItem("");
                    quantificationComboBox.setEnabled(true);
                    quantificationComboBox.setSelectedItem("");
                    outputComboBox.setEnabled(true);
                    outputComboBox.setSelectedItem("");

                  //  newParamsFileButton.setEnabled(true);
                  //  openParamsFileButton.setEnabled(true);
                    saveParamsFileButton.setEnabled(false);
                    runAppButton.setEnabled(false);
                }
            }    
        }
    }//GEN-LAST:event_newMenuItemActionPerformed

    /**
     *
     *
     */
    private void loadRawDataComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadRawDataComboBoxActionPerformed

        if (evt.getSource() instanceof JComboBox) {

            String actionCommand = evt.getActionCommand();

            String tempRawDataPlugin = (String)loadRawDataComboBox.getSelectedItem();

            if(tempRawDataPlugin.equals("") == false) {

                if((actionCommand.equals("IGNORE") || tempRawDataPlugin.equals(currRawDataPlugin) == false) && currentModelName.equals("") == false) {

                    currRawDataPlugin = tempRawDataPlugin;

                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    // can be null but only relevant for opeing a configuration where file names maybe spelt incorrectly
                    // or plugin specified does not exist
                    XTrackerPluginInformation currRawDataPluginInfo = getXTrackerPluginInformation(PluginType.RAWDATAPLUGIN, currRawDataPlugin);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.RAWDATAPLUGIN, currRawDataPluginInfo.getPluginFileName());

                    if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(currRawDataPluginInfo.getPluginFileName())) {

                        if(currRawDataPluginInfo.getXsdSchemaFileName() != null) {

                            XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(currRawDataPluginInfo.getXsdSchemaFileName());
                            String paramFile = paramFileModel.getParametersFileName();
                            // Make sure the comfig is set with the correct parameters if any
                            currConfigModel.setPluginParamFileByPluginType(PluginType.RAWDATAPLUGIN, paramFile);
                            
                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(currRawDataPluginInfo.getXsdSchemaFileName())) {

                                if(paramFile.equals("")) {

                                    // If its an unsaved parameter model
                                    paramFile = "Not set";
                                }

                                String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(currRawDataPluginInfo.getXsdSchemaFileName());
                                if(jarFile.equals(currRawDataPluginInfo.getPluginFileName()) == false) {

                                    PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(currRawDataPluginInfo.getXsdSchemaFileName());

                                    pluginParamsUIController.removeScreenForPlugin(PluginType.RAWDATAPLUGIN);
                                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                           currRawDataPluginInfo.getPluginFileName(),
                                                                                           "<html><p><center>Parameter file " + paramFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>is open for editing by plugin " + jarFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                           "</center></p>", 25);

                                    paramFileModel.addJarFileAssocation(currRawDataPluginInfo.getPluginFileName());
                                    newParamsFileButton.setEnabled(false);
                                    openParamsFileButton.setEnabled(false);
                                }

                            } else {

                                pluginParamsUIController.removeScreenForPlugin(PluginType.RAWDATAPLUGIN);
                                pluginParamsUIController.displayScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                currentModelName,
                                                                                currRawDataPluginInfo.getPluginFileName(),
                                                                                currRawDataPluginInfo.getPluginClassName(),
                                                                                currRawDataPluginInfo.getXsdSchemaFileName());

                                newParamsFileButton.setEnabled(false);
                                openParamsFileButton.setEnabled(true);
                            }

                        } else {
                            
                            currConfigModel.setPluginParamFileByPluginType(PluginType.RAWDATAPLUGIN, "");

                            pluginParamsUIController.removeScreenForPlugin(PluginType.RAWDATAPLUGIN);
                        /*    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currRawDataPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                   "",
                                                                                   currRawDataPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);
                        }
                        
                    } else {

                        String xsdFileDisplayed = pluginParamsUIController.getXsdFileForPluginTypeDisplayed(PluginType.RAWDATAPLUGIN);
                        String jarFileDisplayed = pluginParamsUIController.getJarFileForPluginTypeDisplayed(PluginType.RAWDATAPLUGIN);

                        currConfigModel.setPluginParamFileByPluginType(PluginType.RAWDATAPLUGIN, "");
                        pluginParamsUIController.removeScreenForPlugin(PluginType.RAWDATAPLUGIN);

                        if(currRawDataPluginInfo.getXsdSchemaFileName() == null) {

                        /*    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currRawDataPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                   "",
                                                                                   currRawDataPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);

                        } else {

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                                   "",
                                                                                   currRawDataPluginInfo.getHtmlInformation("", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(true);
                            openParamsFileButton.setEnabled(true);
                        }

                        if(jarFileDisplayed.equals("") == false) {

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(xsdFileDisplayed) == false) {

                                if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(jarFileDisplayed)) {

                                    spectralIdentComboBoxActionPerformed(new java.awt.event.ActionEvent(loadRawDataComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    peakSelectionComboBoxActionPerformed(new java.awt.event.ActionEvent(loadRawDataComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    quantificationComboBoxActionPerformed(new java.awt.event.ActionEvent(loadRawDataComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    outputComboBoxActionPerformed(new java.awt.event.ActionEvent(loadRawDataComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                }
                            }
                        }  
                    }

                    if(logger.isDebugEnabled()) {

                        logger.debug("XTrackerGuiUIFrame::loadRawDataComboBoxActionPerformed: Set to " +
                                      currConfigModel.getPluginJarFileByPluginType(PluginType.RAWDATAPLUGIN));
                    }

                    // check if the run button can be enabled - checking for presence
                    // of all five plugins, then checking for associated XML parameter
                    // file models - so long as plugins are selected and associated
                    // parameter file model are created then the run button is enabled
                    if(checkXTrackerConfigModelIsComplete()) {

                        runAppButton.setEnabled(true);
                        
                    }  else {

                        runAppButton.setEnabled(false);
                    }
                }
                
            } else {

                if(currentModelName.equals("") == false) {

                    currRawDataPlugin = "";
                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.RAWDATAPLUGIN, "");
                    currConfigModel.setPluginParamFileByPluginType(PluginType.RAWDATAPLUGIN, "");

                    pluginParamsUIController.removeScreenForPlugin(PluginType.RAWDATAPLUGIN);
                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.RAWDATAPLUGIN,
                                                                           "",
                                                                           "<html><p><center>Configuration '" +
                                                                           currentModelName +
                                                                           "' loaded</center></p><p><center>No plugin selected</center></p>", 30);

                    if(actionCommand.equals("IGNORE") == false) {

                        newParamsFileButton.setEnabled(false);
                        openParamsFileButton.setEnabled(false);
                        runAppButton.setEnabled(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_loadRawDataComboBoxActionPerformed

    /**
     *
     *
     */
    private void spectralIdentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spectralIdentComboBoxActionPerformed

        if (evt.getSource() instanceof JComboBox) {

            String actionCommand = evt.getActionCommand();

            String tempSpecIdentDataPlugin = (String)spectralIdentComboBox.getSelectedItem();

            if(tempSpecIdentDataPlugin.equals("") == false) {

                if((actionCommand.equals("IGNORE") || tempSpecIdentDataPlugin.equals(currSpecIdentDataPlugin) == false) && currentModelName.equals("") == false) {

                    currSpecIdentDataPlugin = tempSpecIdentDataPlugin;

                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    XTrackerPluginInformation currSpecIdentPluginInfo = getXTrackerPluginInformation(PluginType.IDENTDATAPLUGIN, currSpecIdentDataPlugin);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.IDENTDATAPLUGIN, currSpecIdentPluginInfo.getPluginFileName());
                    if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(currSpecIdentPluginInfo.getPluginFileName())) {

                        if(currSpecIdentPluginInfo.getXsdSchemaFileName() != null) {

                            XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(currSpecIdentPluginInfo.getXsdSchemaFileName());
                            String paramFile = paramFileModel.getParametersFileName();
                            currConfigModel.setPluginParamFileByPluginType(PluginType.IDENTDATAPLUGIN, paramFile);
                            
                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(currSpecIdentPluginInfo.getXsdSchemaFileName())) {
 
                                if(paramFile.equals("")) {

                                        paramFile = "Not set";
                                }
                                String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(currSpecIdentPluginInfo.getXsdSchemaFileName());
                                if(jarFile.equals(currSpecIdentPluginInfo.getPluginFileName()) == false) {

                                    PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(currSpecIdentPluginInfo.getXsdSchemaFileName());

                                    pluginParamsUIController.removeScreenForPlugin(PluginType.IDENTDATAPLUGIN);
                                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                           currSpecIdentPluginInfo.getPluginFileName(),
                                                                                           "<html><p><center>Parameter file " + paramFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>is open for editing by plugin " + jarFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                           "</center></p>", 25);

                                    paramFileModel.addJarFileAssocation(currSpecIdentPluginInfo.getPluginFileName());
                                    newParamsFileButton.setEnabled(false);
                                    openParamsFileButton.setEnabled(false);
                                }

                            } else {

                                pluginParamsUIController.displayScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                currentModelName,
                                                                                currSpecIdentPluginInfo.getPluginFileName(),
                                                                                currSpecIdentPluginInfo.getPluginClassName(),
                                                                                currSpecIdentPluginInfo.getXsdSchemaFileName());

                                newParamsFileButton.setEnabled(false);
                                openParamsFileButton.setEnabled(true);
                            }
                            
                        } else {

                            currConfigModel.setPluginParamFileByPluginType(PluginType.IDENTDATAPLUGIN, "");

                            pluginParamsUIController.removeScreenForPlugin(PluginType.IDENTDATAPLUGIN);
                         /*   pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currSpecIdentPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                   "",
                                                                                   currSpecIdentPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);
                        }
   
                    } else {

                        String xsdFileDisplayed = pluginParamsUIController.getXsdFileForPluginTypeDisplayed(PluginType.IDENTDATAPLUGIN);
                        String jarFileDisplayed = pluginParamsUIController.getJarFileForPluginTypeDisplayed(PluginType.IDENTDATAPLUGIN);

                        currConfigModel.setPluginParamFileByPluginType(PluginType.IDENTDATAPLUGIN, "");
                        pluginParamsUIController.removeScreenForPlugin(PluginType.IDENTDATAPLUGIN);

                        if(currSpecIdentPluginInfo.getXsdSchemaFileName() == null) {
                            
                            pluginParamsUIController.removeScreenForPlugin(PluginType.IDENTDATAPLUGIN);
                         /*   pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currSpecIdentPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                   "",
                                                                                   currSpecIdentPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);

                        } else {
                            
                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                                   "",
                                                                                   currSpecIdentPluginInfo.getHtmlInformation("", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(true);
                            openParamsFileButton.setEnabled(true);
                        }

                        if(jarFileDisplayed.equals("") == false) {

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(xsdFileDisplayed) == false) {

                                if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(jarFileDisplayed)) {

                                    loadRawDataComboBoxActionPerformed(new java.awt.event.ActionEvent(spectralIdentComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    peakSelectionComboBoxActionPerformed(new java.awt.event.ActionEvent(spectralIdentComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    quantificationComboBoxActionPerformed(new java.awt.event.ActionEvent(spectralIdentComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    outputComboBoxActionPerformed(new java.awt.event.ActionEvent(spectralIdentComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                }
                            }
                        }
                    }

                    if(logger.isDebugEnabled()) {

                        logger.debug("XTrackerGuiUIFrame::spectralIdentComboBoxActionPerformed: Set to " +
                                      currConfigModel.getPluginJarFileByPluginType(PluginType.IDENTDATAPLUGIN));
                    }

                    // check if the run button can be enabled - checking for presence
                    // of all five plugins, then checking for associated XML parameter
                    // file models - so long as plugins are selected and associated
                    // parameter file model are created then the run button is enabled
                    if(checkXTrackerConfigModelIsComplete()) {

                        runAppButton.setEnabled(true);

                    }  else {

                        runAppButton.setEnabled(false);
                    }
                }
                
            } else {

                if(currentModelName.equals("") == false) {

                    currSpecIdentDataPlugin = "";
                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.IDENTDATAPLUGIN, "");
                    currConfigModel.setPluginParamFileByPluginType(PluginType.IDENTDATAPLUGIN, "");
                    
                    pluginParamsUIController.removeScreenForPlugin(PluginType.IDENTDATAPLUGIN);
                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.IDENTDATAPLUGIN,
                                                                           "",
                                                                           "<html><p><center>Configuration '" +
                                                                           currentModelName +
                                                                           "' loaded</center></p><p><center>No plugin selected</center></p>", 30);

                    if(actionCommand.equals("IGNORE") == false) {

                        newParamsFileButton.setEnabled(false);
                        openParamsFileButton.setEnabled(false);
                        runAppButton.setEnabled(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_spectralIdentComboBoxActionPerformed

    /**
     *
     *
     */
    private void peakSelectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_peakSelectionComboBoxActionPerformed

        if (evt.getSource() instanceof JComboBox) {

            String actionCommand = evt.getActionCommand();

            String tempPeakSelPlugin = (String)peakSelectionComboBox.getSelectedItem();

            if(tempPeakSelPlugin.equals("") == false) {

                if((actionCommand.equals("IGNORE") || tempPeakSelPlugin.equals(currPeakSelPlugin) == false) && currentModelName.equals("") == false) {

                    currPeakSelPlugin = tempPeakSelPlugin;

                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    XTrackerPluginInformation currPeakSelPluginInfo = getXTrackerPluginInformation(PluginType.PEAKSELECTIONPLUGIN, currPeakSelPlugin);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.PEAKSELECTIONPLUGIN, currPeakSelPluginInfo.getPluginFileName());
                    if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(currPeakSelPluginInfo.getPluginFileName())) {

                        if(currPeakSelPluginInfo.getXsdSchemaFileName() != null) {

                            XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(currPeakSelPluginInfo.getXsdSchemaFileName());
                            String paramFile = paramFileModel.getParametersFileName();
                            currConfigModel.setPluginParamFileByPluginType(PluginType.PEAKSELECTIONPLUGIN, paramFile);

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(currPeakSelPluginInfo.getXsdSchemaFileName())) {
 
                                if(paramFile.equals("")) {

                                        paramFile = "Not set";
                                }
                                String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(currPeakSelPluginInfo.getXsdSchemaFileName());
                                if(jarFile.equals(currPeakSelPluginInfo.getPluginFileName()) == false) {

                                    PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(currPeakSelPluginInfo.getXsdSchemaFileName());

                                    pluginParamsUIController.removeScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN);
                                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                           currPeakSelPluginInfo.getPluginFileName(),
                                                                                           "<html><p><center>Parameter file " + paramFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>is open for editing by plugin " + jarFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                           "</center></p>", 25);

                                    paramFileModel.addJarFileAssocation(currPeakSelPluginInfo.getPluginFileName());
                                    newParamsFileButton.setEnabled(false);
                                    openParamsFileButton.setEnabled(false);
                                }

                            } else {

                                pluginParamsUIController.displayScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                currentModelName,
                                                                                currPeakSelPluginInfo.getPluginFileName(),
                                                                                currPeakSelPluginInfo.getPluginClassName(),
                                                                                currPeakSelPluginInfo.getXsdSchemaFileName());

                                newParamsFileButton.setEnabled(false);
                                openParamsFileButton.setEnabled(true);
                            }

                        } else {

                            currConfigModel.setPluginParamFileByPluginType(PluginType.PEAKSELECTIONPLUGIN, "");

                            pluginParamsUIController.removeScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN);
                        /*    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currPeakSelPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                   "",
                                                                                   currPeakSelPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);
                        }

                    } else {

                        String xsdFileDisplayed = pluginParamsUIController.getXsdFileForPluginTypeDisplayed(PluginType.PEAKSELECTIONPLUGIN);
                        String jarFileDisplayed = pluginParamsUIController.getJarFileForPluginTypeDisplayed(PluginType.PEAKSELECTIONPLUGIN);

                        currConfigModel.setPluginParamFileByPluginType(PluginType.PEAKSELECTIONPLUGIN, "");
                        pluginParamsUIController.removeScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN);

                        if(currPeakSelPluginInfo.getXsdSchemaFileName() == null) {

                         /*   pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currPeakSelPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                   "",
                                                                                   currPeakSelPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);

                        } else {

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                                   "",
                                                                                   currPeakSelPluginInfo.getHtmlInformation("", ""),
                                                                                   25);
                            
                            newParamsFileButton.setEnabled(true);
                            openParamsFileButton.setEnabled(true);
                        }
                        
                        if(jarFileDisplayed.equals("") == false) {

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(xsdFileDisplayed) == false) {

                                if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(jarFileDisplayed)) {

                                    loadRawDataComboBoxActionPerformed(new java.awt.event.ActionEvent(peakSelectionComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    spectralIdentComboBoxActionPerformed(new java.awt.event.ActionEvent(peakSelectionComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    quantificationComboBoxActionPerformed(new java.awt.event.ActionEvent(peakSelectionComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    outputComboBoxActionPerformed(new java.awt.event.ActionEvent(peakSelectionComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                }
                            }
                        }
                    }

                    if(logger.isDebugEnabled()) {

                        logger.debug("XTrackerGuiUIFrame::peakSelectionComboBoxActionPerformed: Set to " +
                                      currConfigModel.getPluginJarFileByPluginType(PluginType.PEAKSELECTIONPLUGIN));
                    }

                    // check if the run button can be enabled - checking for presence
                    // of all five plugins, then checking for associated XML parameter
                    // file models - so long as plugins are selected and associated
                    // parameter file model are created then the run button is enabled
                    if(checkXTrackerConfigModelIsComplete()) {

                        runAppButton.setEnabled(true);

                    }  else {

                        runAppButton.setEnabled(false);
                    }
                }

            } else {

                if(currentModelName.equals("") == false) {

                    currPeakSelPlugin = "";
                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.PEAKSELECTIONPLUGIN, "");
                    currConfigModel.setPluginParamFileByPluginType(PluginType.PEAKSELECTIONPLUGIN, "");
                    
                    pluginParamsUIController.removeScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN);
                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.PEAKSELECTIONPLUGIN,
                                                                           "",
                                                                           "<html><p><center>Configuration '" +
                                                                           currentModelName +
                                                                           "' loaded</center></p><p><center>No plugin selected</center></p>", 30);

                    if(actionCommand.equals("IGNORE") == false) {

                        newParamsFileButton.setEnabled(false);
                        openParamsFileButton.setEnabled(false);
                        runAppButton.setEnabled(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_peakSelectionComboBoxActionPerformed

    /**
     *
     *
     */
    private void quantificationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantificationComboBoxActionPerformed

        if (evt.getSource() instanceof JComboBox) {

            String actionCommand = evt.getActionCommand();

            String tempQuantPlugin = (String)quantificationComboBox.getSelectedItem();

            if(tempQuantPlugin.equals("") == false) {

                if((actionCommand.equals("IGNORE") || tempQuantPlugin.equals(currQuantPlugin) == false) && currentModelName.equals("") == false) {

                    currQuantPlugin = tempQuantPlugin;

                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    XTrackerPluginInformation currQuantPluginInfo = getXTrackerPluginInformation(PluginType.QUANTIFICATIONPLUGIN, currQuantPlugin);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.QUANTIFICATIONPLUGIN, currQuantPluginInfo.getPluginFileName());
                    if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(currQuantPluginInfo.getPluginFileName())) {
                
                        if(currQuantPluginInfo.getXsdSchemaFileName() != null) {

                            XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(currQuantPluginInfo.getXsdSchemaFileName());
                            String paramFile = paramFileModel.getParametersFileName();
                            currConfigModel.setPluginParamFileByPluginType(PluginType.QUANTIFICATIONPLUGIN, paramFile);

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(currQuantPluginInfo.getXsdSchemaFileName())) {

                                if(paramFile.equals("")) {

                                        paramFile = "Not set";
                                }
                                String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(currQuantPluginInfo.getXsdSchemaFileName());
                                if(jarFile.equals(currQuantPluginInfo.getPluginFileName()) == false) {

                                    PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(currQuantPluginInfo.getXsdSchemaFileName());

                                    pluginParamsUIController.removeScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN);
                                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                           currQuantPluginInfo.getPluginFileName(),
                                                                                           "<html><p><center>Parameter file " + paramFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>is open for editing by plugin " + jarFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                           "</center></p>", 25);

                                    paramFileModel.addJarFileAssocation(currQuantPluginInfo.getPluginFileName());
                                    newParamsFileButton.setEnabled(false);
                                    openParamsFileButton.setEnabled(false);
                                }

                            } else {

                                pluginParamsUIController.displayScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                currentModelName,
                                                                                currQuantPluginInfo.getPluginFileName(),
                                                                                currQuantPluginInfo.getPluginClassName(),
                                                                                currQuantPluginInfo.getXsdSchemaFileName());

                                newParamsFileButton.setEnabled(false);
                                openParamsFileButton.setEnabled(true);
                            }

                        } else {

                            currConfigModel.setPluginParamFileByPluginType(PluginType.QUANTIFICATIONPLUGIN, "");

                            pluginParamsUIController.removeScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN);
                       /*     pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currQuantPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                   "",
                                                                                   currQuantPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);
                        }

                    } else {

                        String xsdFileDisplayed = pluginParamsUIController.getXsdFileForPluginTypeDisplayed(PluginType.QUANTIFICATIONPLUGIN);
                        String jarFileDisplayed = pluginParamsUIController.getJarFileForPluginTypeDisplayed(PluginType.QUANTIFICATIONPLUGIN);

                        currConfigModel.setPluginParamFileByPluginType(PluginType.QUANTIFICATIONPLUGIN, "");
                        pluginParamsUIController.removeScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN);

                        if(currQuantPluginInfo.getXsdSchemaFileName() == null) {

                         /*   pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currQuantPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                   "",
                                                                                   currQuantPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);

                        } else {

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                                   "",
                                                                                   currQuantPluginInfo.getHtmlInformation("", ""),
                                                                                   25);
                            
                            newParamsFileButton.setEnabled(true);
                            openParamsFileButton.setEnabled(true);
                        }

                        if(jarFileDisplayed.equals("") == false) {

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(xsdFileDisplayed) == false) {

                                if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(jarFileDisplayed)) {

                                    loadRawDataComboBoxActionPerformed(new java.awt.event.ActionEvent(quantificationComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    spectralIdentComboBoxActionPerformed(new java.awt.event.ActionEvent(quantificationComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    peakSelectionComboBoxActionPerformed(new java.awt.event.ActionEvent(quantificationComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    outputComboBoxActionPerformed(new java.awt.event.ActionEvent(quantificationComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                }
                            }
                        }
                    }

                    if(logger.isDebugEnabled()) {

                        logger.debug("XTrackerGuiUIFrame::quantificationComboBoxActionPerformed: Set to " +
                                      currConfigModel.getPluginJarFileByPluginType(PluginType.QUANTIFICATIONPLUGIN));
                    }

                    // check if the run button can be enabled - checking for presence
                    // of all five plugins, then checking for associated XML parameter
                    // file models - so long as plugins are selected and associated
                    // parameter file model are created then the run button is enabled
                    if(checkXTrackerConfigModelIsComplete()) {

                        runAppButton.setEnabled(true);

                    }  else {

                        runAppButton.setEnabled(false);
                    }
                }

            } else {

                if(currentModelName.equals("") == false) {

                    currQuantPlugin = "";
                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.QUANTIFICATIONPLUGIN, "");
                    currConfigModel.setPluginParamFileByPluginType(PluginType.QUANTIFICATIONPLUGIN, "");
                    
                    pluginParamsUIController.removeScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN);
                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.QUANTIFICATIONPLUGIN,
                                                                           "",
                                                                           "<html><p><center>Configuration '" +
                                                                           currentModelName +
                                                                           "' loaded</center></p><p><center>No plugin selected</center></p>", 30);

                    if(actionCommand.equals("IGNORE") == false) {

                        newParamsFileButton.setEnabled(false);
                        openParamsFileButton.setEnabled(false);
                        runAppButton.setEnabled(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_quantificationComboBoxActionPerformed

    /**
     *
     *
     */
    private void outputComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputComboBoxActionPerformed

        if (evt.getSource() instanceof JComboBox) {

            String actionCommand = evt.getActionCommand();

            String tempOutputPlugin = (String)outputComboBox.getSelectedItem();

            if(tempOutputPlugin.equals("") == false) {

                if((actionCommand.equals("IGNORE") || tempOutputPlugin.equals(currOuputPlugin) == false) && currentModelName.equals("") == false) {

                    currOuputPlugin = tempOutputPlugin;

                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    XTrackerPluginInformation currOutputPluginInfo = getXTrackerPluginInformation(PluginType.OUTPUTPLUGIN, currOuputPlugin);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.OUTPUTPLUGIN, currOutputPluginInfo.getPluginFileName());
                    if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(currOutputPluginInfo.getPluginFileName())) {

                        if(currOutputPluginInfo.getXsdSchemaFileName() != null) {

                            XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(currOutputPluginInfo.getXsdSchemaFileName());
                            String paramFile = paramFileModel.getParametersFileName();
                            currConfigModel.setPluginParamFileByPluginType(PluginType.OUTPUTPLUGIN, paramFile);

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(currOutputPluginInfo.getXsdSchemaFileName())) {

                                if(paramFile.equals("")) {

                                        paramFile = "Not set";
                                }
                                String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(currOutputPluginInfo.getXsdSchemaFileName());
                                if(jarFile.equals(currOutputPluginInfo.getPluginFileName()) == false) {

                                    PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(currOutputPluginInfo.getXsdSchemaFileName());

                                    pluginParamsUIController.removeScreenForPlugin(PluginType.OUTPUTPLUGIN);
                                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                           currOutputPluginInfo.getPluginFileName(),
                                                                                           "<html><p><center>Parameter file " + paramFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>is open for editing by plugin " + jarFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                           "</center></p>", 25);

                                    paramFileModel.addJarFileAssocation(currOutputPluginInfo.getPluginFileName());
                                    newParamsFileButton.setEnabled(false);
                                    openParamsFileButton.setEnabled(false);
                                }

                            } else {

                                pluginParamsUIController.displayScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                currentModelName,
                                                                                currOutputPluginInfo.getPluginFileName(),
                                                                                currOutputPluginInfo.getPluginClassName(),
                                                                                currOutputPluginInfo.getXsdSchemaFileName());

                                newParamsFileButton.setEnabled(false);
                                openParamsFileButton.setEnabled(true);
                            }

                        } else {

                            currConfigModel.setPluginParamFileByPluginType(PluginType.OUTPUTPLUGIN, "");

                            pluginParamsUIController.removeScreenForPlugin(PluginType.OUTPUTPLUGIN);
                        /*    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currOutputPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                   "",
                                                                                   currOutputPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);
                        }

                    } else {

                        String xsdFileDisplayed = pluginParamsUIController.getXsdFileForPluginTypeDisplayed(PluginType.OUTPUTPLUGIN);
                        String jarFileDisplayed = pluginParamsUIController.getJarFileForPluginTypeDisplayed(PluginType.OUTPUTPLUGIN);

                        currConfigModel.setPluginParamFileByPluginType(PluginType.OUTPUTPLUGIN, "");
                        pluginParamsUIController.removeScreenForPlugin(PluginType.OUTPUTPLUGIN);

                        if(currOutputPluginInfo.getXsdSchemaFileName() == null) {

                         /*   pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                   "",
                                                                                   "<html><p><center>Selected Plugin " +
                                                                                   currOutputPluginInfo.getPluginFileName() +
                                                                                   "</center></p>" +
                                                                                   "<p><center>No parameters required</center></p>", 30); */

                             pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                   "",
                                                                                   currOutputPluginInfo.getHtmlInformation("<p><center>No parameters required</center></p>", ""),
                                                                                   25);

                            newParamsFileButton.setEnabled(false);
                            openParamsFileButton.setEnabled(false);

                        } else {

                            pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                                   "",
                                                                                   currOutputPluginInfo.getHtmlInformation("", ""),
                                                                                   25);
                            
                            newParamsFileButton.setEnabled(true);
                            openParamsFileButton.setEnabled(true);
                        }

                        if(jarFileDisplayed.equals("") == false) {

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(xsdFileDisplayed) == false) {

                                if(currConfigModel.doesParamFileModelExistWithJarFileAssoc(jarFileDisplayed)) {

                                    loadRawDataComboBoxActionPerformed(new java.awt.event.ActionEvent(outputComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    spectralIdentComboBoxActionPerformed(new java.awt.event.ActionEvent(outputComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    peakSelectionComboBoxActionPerformed(new java.awt.event.ActionEvent(outputComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                    quantificationComboBoxActionPerformed(new java.awt.event.ActionEvent(outputComboBox, ActionEvent.ACTION_PERFORMED, "IGNORE"));
                                }
                            }
                        }
                    }

                    if(logger.isDebugEnabled()) {

                        logger.debug("XTrackerGuiUIFrame::outputComboBoxActionPerformed: Set to " +
                                      currConfigModel.getPluginJarFileByPluginType(PluginType.OUTPUTPLUGIN));
                    }

                    // check if the run button can be enabled - checking for presence
                    // of all five plugins, then checking for associated XML parameter
                    // file models - so long as plugins are selected and associated
                    // parameter file model are created then the run button is enabled
                    if(checkXTrackerConfigModelIsComplete()) {

                        runAppButton.setEnabled(true);
                        
                    } else {

                        runAppButton.setEnabled(false);
                    }
                }
                
            } else {

                if(currentModelName.equals("") == false) {

                    currOuputPlugin = "";
                    XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                    XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                    currConfigModel.setPluginJarFileByPluginType(PluginType.OUTPUTPLUGIN, "");
                    currConfigModel.setPluginParamFileByPluginType(PluginType.OUTPUTPLUGIN, "");

                    pluginParamsUIController.removeScreenForPlugin(PluginType.OUTPUTPLUGIN);
                    pluginParamsUIController.displayDefaultScreenForPlugin(PluginType.OUTPUTPLUGIN,
                                                                           "",
                                                                           "<html><p><center>Configuration '" +
                                                                           currentModelName +
                                                                           "' loaded</center></p><p><center>No plugin selected</center></p>", 30);

                    if(actionCommand.equals("IGNORE") == false) {

                        newParamsFileButton.setEnabled(false);
                        openParamsFileButton.setEnabled(false);
                        runAppButton.setEnabled(false);
                    }
                }
            }
        }
    }//GEN-LAST:event_outputComboBoxActionPerformed

    /**
     *
     *
     */
    private boolean checkXTrackerConfigModelIsComplete() {

        boolean modelIsComplete = true;

        XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
        XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);

        modelIsComplete = configModelManager.checkXTrackerConfigModelHasAllPluginsSet(currentModelName);

        if(modelIsComplete == true) {

            for(PluginType type : PluginType.values()) {

                String jarFileName = "";
                if(type != PluginType.PLUGINTYPENOTDEFINED) {

                    jarFileName = currConfigModel.getPluginJarFileByPluginType(type);

                    if(jarFileName.equals("") == false) {

                        String classFileName = FileUtils.getFileNameWithoutExtension(jarFileName);
                        XTrackerPluginInformation pluginInfo = getXTrackerPluginInformation(type, classFileName);
                        // Making sure we have a XML parameter file for each plugin that requires one
                        String xsdSchemaFileName = pluginInfo.getXsdSchemaFileName();
                        if(xsdSchemaFileName != null) {

                            XTrackerParamFileModel model = currConfigModel.getParamFileModelByJarFileAssoc(jarFileName);
                            if(model == null) {

                                modelIsComplete = false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return  modelIsComplete;
    }

    private boolean validateXTrackerConfiguration() {

        boolean configIsValid = true;

        // Not very OO way of doing this validation but not working too well so trying it
        // this way
        XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
        XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);

        File currConfigModelFile = currConfigModel.getConfigurationFile();
        if(currConfigModelFile != null) {

            if(currConfigModel.checkXmlFileIsValid(fileDirectoryPath, currConfigModelFile) == false) {

                configIsValid = false;
                logger.warn("XSD Validation Failure: X-Tracker configuration " + currentModelName + " file " + currConfigModelFile.getName());
            }

            for(PluginType type : PluginType.values()) {

                String jarFileName = "";
                if(type != PluginType.PLUGINTYPENOTDEFINED) {

                    jarFileName = currConfigModel.getPluginJarFileByPluginType(type);

                    if(jarFileName.equals("") == false) {

                        XTrackerParamFileModel model = currConfigModel.getParamFileModelByJarFileAssoc(jarFileName);
                        if(model != null) {

                            File paramFile = model.getParameterFile();

                            if(currConfigModel.checkXmlFileIsValid(fileDirectoryPath, paramFile) == false) {

                                if(configIsValid == true) {

                                    configIsValid = false;
                                }

                                logger.warn("XSD Validation Failure: X-Tracker configuration " + currentModelName + " XML Parameter file " + paramFile.getName() + " falied validation");
                            }
                        } else {
                            
                            
                        }

                    } else {

                        if(configIsValid == true) {

                            configIsValid = false;
                        }
                    }
                }
            }
        }

        return configIsValid;
    }

    /**
     *
     *
     */
    private void runAppButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runAppButtonActionPerformed

        if (evt.getSource() instanceof JButton) {

            boolean runThePuppy = true;
            XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
            XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);

            File currConfigModelFile = currConfigModel.getConfigurationFile();
            if(currConfigModelFile != null) {
  
                if(configModelManager.checkXtrackerConfigModelForUnSavedChanges(currentModelName)) {

                    int retOption = JOptionPane.showOptionDialog(this,
                                                                 "Configuration " + currentModelName + " has changes. Save before running?",
                                                                 "Save and Run Configuration",
                                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null,
                                                                 null,
                                                                 null);

                    if(retOption == JOptionPane.YES_OPTION) {

                        configModelManager.saveXTrackerConfigModel(currentModelName);
                        runThePuppy = true;

                    } else if(retOption == JOptionPane.NO_OPTION) {

                        runThePuppy = true;

                    } else if(retOption == JOptionPane.CANCEL_OPTION) {

                        runThePuppy = false;
                    }
                }

                if(runThePuppy == true) {

                    if(validateXTrackerConfiguration()) {

                        try {

                            if(logger.isInfoEnabled()) {

                                logger.info("Running X-Tracker configuration " + currentModelName);
                            }

                            // get where the jre/ java exe is to be found, dependant on os type
                            String jre = GuiUtils.getJavaRuntime(GuiUtils.getOperatingSystemType()).toString();

                            List<String> processCommanddArray = new ArrayList<String>();
                            processCommanddArray.add(jre);
                            processCommanddArray.add("-jar");
                            processCommanddArray.add(fileDirectoryPath + "xTracker.jar");
                            processCommanddArray.add(fileDirectoryPath + currConfigModelFile.getName());
                            int processReturnValue = GuiUtils.runProcess(processCommanddArray);
                            if(processReturnValue != 0) {

                                logger.error("Problem running xTracker - exit code was " + processReturnValue);

                            } else {

                                if(logger.isInfoEnabled()) {

                                    logger.info("X-Tracker configuration ran okay " + currentModelName);
                                }
                            }

                        } catch (IOException ex) {

                            logger.error("Problem running xTracker " + ex.getMessage());

                        } catch(InterruptedException ex) {

                            logger.error("Problem running xTracker " + ex.getMessage());
                        }
                    } else {


                    }
                }

            } else {

                // Should'nt get here
                JOptionPane.showMessageDialog(this,
                                              "CANNOT RUN - no XML configuration file found for " + currentModelName,
                                              "Missing X-Tracker Configuration File",
                                              JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_runAppButtonActionPerformed

    /**
     *
     *
     */
    private void spectralIdentComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_spectralIdentComboBoxItemStateChanged
        
        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
              //  currentHighlightedPanel = PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
            }     
        }
    }//GEN-LAST:event_spectralIdentComboBoxItemStateChanged

    /**
     *
     *
     */
    private void loadRawDataComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_loadRawDataComboBoxItemStateChanged

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
              //  currentHighlightedPanel = PluginType.RAWDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_loadRawDataComboBoxItemStateChanged

    /**
     *
     *
     */
    private void peakSelectionComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_peakSelectionComboBoxItemStateChanged
        
        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
              //  currentHighlightedPanel = PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_peakSelectionComboBoxItemStateChanged

    /**
     *
     *
     */
    private void quantificationComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_quantificationComboBoxItemStateChanged
        
        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_quantificationComboBoxItemStateChanged

    /**
     *
     *
     */
    private void outputComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_outputComboBoxItemStateChanged
        
        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.OUTPUTPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_outputComboBoxItemStateChanged

    /**
     *
     *
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed

        JDialog aboutDialog = new XTrackerGuiAboutDialog(this, "About", true);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     *
     *
     */
    private void helpContentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpContentsMenuItemActionPerformed

        XTrackerGuiHelpDialog guiHelpDialog = new XTrackerGuiHelpDialog(this, "Help");
        guiHelpDialog.setTitle("Help");
        guiHelpDialog.setVisible(true);
    }//GEN-LAST:event_helpContentsMenuItemActionPerformed

    /**
     *
     *
     */
    private String getComboBoxSelectedItemByPluginType(PluginType pluginType) {

        String selectedItem = "";

        switch(pluginType) {

            case RAWDATAPLUGIN: {

                //selectedItem = (String)loadRawDataComboBox.getSelectedItem();
                selectedItem = currRawDataPlugin;
                break;
            }
            case IDENTDATAPLUGIN: {

                // selectedItem = (String)spectralIdentComboBox.getSelectedItem();
                selectedItem = currSpecIdentDataPlugin;
                break;
            }
            case PEAKSELECTIONPLUGIN: {

                //  selectedItem = (String)peakSelectionComboBox.getSelectedItem();
                selectedItem = currPeakSelPlugin;
                break;
            }
            case QUANTIFICATIONPLUGIN: {

                // selectedItem = (String)quantificationComboBox.getSelectedItem();
                selectedItem = currQuantPlugin;
                break;
            }
            case OUTPUTPLUGIN: {

                // selectedItem  = (String)outputComboBox.getSelectedItem();
                selectedItem = currOuputPlugin;
                break;
            }
            default: {

            }
        }

        return selectedItem;
    }

    

    /**
     *
     *
     */
    private void newParamsFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newParamsFileButtonActionPerformed

        if (evt.getSource() instanceof JButton) {
            
            // a model - new or an opened one - should already be loaded but just in case check
            if(currentModelName.equals("") == false) {
                
                XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);
                PluginType pluginTypeForCurrPluginParamsSelectedTab = pluginParamsUIController.getPluginTypeByTabPaneIndex(currentHighlightedPanel);
                String currSelectedItemForPluginType = getComboBoxSelectedItemByPluginType(pluginTypeForCurrPluginParamsSelectedTab);
                XTrackerPluginInformation currPluginInfo = getXTrackerPluginInformation(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                        currSelectedItemForPluginType);

                if(currPluginInfo != null) {

                    String xsdFileName = "";

                    try {

                        String tempXsdFileName = currPluginInfo.getXsdSchemaFileName();
                        if(tempXsdFileName != null) {

                            if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(tempXsdFileName)) {

                                String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(tempXsdFileName);
                                if(jarFile.equals(currPluginInfo.getPluginFileName()) == false) {

                                    PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(tempXsdFileName);

                                    pluginParamsUIController.removeScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab);
                                    pluginParamsUIController.displayDefaultScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                           currPluginInfo.getPluginFileName(),
                                                                                           "<html><p><center>Parameter entry screen for: " + currSelectedItemForPluginType +
                                                                                           "</center></p>" +
                                                                                           "<p><center>is open for editing by plugin " + jarFile +
                                                                                           "</center></p>" +
                                                                                           "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                           "</center></p>", 25);


                                    XTrackerParamFileModel model = currConfigModel.getParamFileModelByJarFileAssoc(jarFile);
                                    model.addJarFileAssocation(currPluginInfo.getPluginFileName());
                                    currConfigModel.setPluginJarFileByPluginType(pluginTypeForCurrPluginParamsSelectedTab, currPluginInfo.getPluginFileName());
                                    currConfigModel.setPluginParamFileByPluginType(pluginTypeForCurrPluginParamsSelectedTab, model.getParametersFileName());
                                    newParamsFileButton.setEnabled(false);
                                    openParamsFileButton.setEnabled(false);
                                    saveParamsFileButton.setEnabled(false);
                                }

                            } else {

                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setDialogTitle("Save new XML Parameter file");
                                fileChooser.addChoosableFileFilter(xmlFileFilter);
                                fileChooser.setAcceptAllFileFilterUsed(false);

                                try {

                                     fileChooser.setCurrentDirectory(new File(new File(".").getCanonicalPath()));

                                } catch (IOException ex) {

                                    logger.error("Cannot set current working directory " + ex.getMessage());
                                }

                                int returnVal = fileChooser.showSaveDialog(this);
                                if(returnVal == JFileChooser.APPROVE_OPTION) {

                                    File file = fileChooser.getSelectedFile();
                                    // maybe should ask JC for the FileFilter to be correct
                                    // but we want tosave xml so.
                                    //System.out.println(file.toURI());
                                    // check file extension has been added by user if not add one - could be problems here
                                    // like name. and such like
                                    if(FileUtils.getFileNameExtension(file).equals("xml") == false) {

                                        try {

                                            file = new File(file.getCanonicalPath() + ".xml");

                                        } catch (IOException ex) {

                                            logger.error("Cannot create new file with extension " + ex.getMessage());
                                        }
                                    }

                                    currConfigModel.setPluginJarFileByPluginType(pluginTypeForCurrPluginParamsSelectedTab, currSelectedItemForPluginType + ".jar");

                                    xsdFileName = tempXsdFileName;
                                    File xsdFile = FileUtils.getFile(fileDirectoryPath + xsdFileName);
                                    currConfigModel.createXTrackerParamFileModel(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                 fileDirectoryPath,
                                                                                 xsdFileName,
                                                                                 xsdFile);

                                    XTrackerParamFileModel model = currConfigModel.getParamFileModelByJarFileAssoc(currPluginInfo.getPluginFileName());

                                    // just made one but just in case
                                    if(model != null) {

                                        XTrackerXmlDocument xmlDocument = model.getParamFileXmlDocument();
                                        Document domDoc = xmlDocument.createDomDocument(xsdFileName);
                                        XmlParser.writeDomDocumentToXmlFile(domDoc, file);
                                        model.setParametersFileName(file.getName());
                                        model.setParameterFile(file);
                                        currConfigModel.setPluginParamFileByPluginType(pluginTypeForCurrPluginParamsSelectedTab, file.getName());

                                   //     File currConfigModelConfigurationFile = currConfigModel.getConfigurationFile();

                                        // have a saved config file but just in case
                                    /*    if(currConfigModelConfigurationFile != null) {

                                            XTrackerConfigData currConfigModelConfigData = currConfigModel.getModelConfigData();
                                            currConfigModelConfigData.saveConfigData();
                                            Document configDataDomDocument = currConfigModelConfigData.getConfigDataDomDocument();
                                            if(configDataDomDocument != null) {
                                                // should'nt be null but what to do if it is??
                                                XmlParser.writeDomDocumentToXmlFile(configDataDomDocument, currConfigModelConfigurationFile);
                                            }
                                        } */

                                        pluginParamsUIController.removeScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab);
                                        pluginParamsUIController.displayScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                        currentModelName,
                                                                                        currPluginInfo.getPluginFileName(),
                                                                                        currPluginInfo.getPluginClassName(),
                                                                                        xsdFileName);
                                        pluginParamsUIController.updateFileDescriptionInScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab, file.getName());

                                        newParamsFileButton.setEnabled(false);
                                        openParamsFileButton.setEnabled(true);
                                        saveParamsFileButton.setEnabled(false);

                                    }
                                }
                            }
                        }

                    } catch (FileNotFoundException ex) {

                        JOptionPane.showMessageDialog(this,
                                                      xsdFileName +
                                                      " " + ex.getMessage(),
                                                      "File Not Found",
                                                      JOptionPane.WARNING_MESSAGE);
                    }
                }

                // check if the run button can be enabled - checking for presence
                // of all five plugins, then checking for associated XML parameter
                // file models - so long as plugins are selected and associated
                // parameter file model are created then the run button is enabled
                if(checkXTrackerConfigModelIsComplete()) {

                    runAppButton.setEnabled(true);

                }  else {

                    runAppButton.setEnabled(false);
                }
            }
        }
    }//GEN-LAST:event_newParamsFileButtonActionPerformed

    /**
     *
     *
     */
    private void openParamsFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openParamsFileButtonActionPerformed

        if (evt.getSource() instanceof JButton) {

            // a model - new or an opened one - should already be loaded but just in case check
            if(currentModelName.equals("") == false) {

                XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                XTrackerConfigModel currConfigModel = configModelManager.getCurrentConfigurationModel(currentModelName);

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(xmlFileFilter);
                fileChooser.setAcceptAllFileFilterUsed(false);

                try {

                    fileChooser.setCurrentDirectory(new File(new File(".").getCanonicalPath()));

                } catch (IOException ex) {

                    logger.error("Cannot set current working directory " + ex.getMessage());
                }

                int returnVal = fileChooser.showOpenDialog(this);
                if(returnVal == JFileChooser.APPROVE_OPTION) {

                    File xmlParameterFile = fileChooser.getSelectedFile();

                    // Todo - check this. Some inefficiency here - calling methods
                    // to create files, dom documentments and then validate
                    // then calling some of the same functions again
                    if(currConfigModel.checkXmlFileIsValid(fileDirectoryPath, xmlParameterFile)) {

                        PluginType pluginTypeForCurrPluginParamsSelectedTab = pluginParamsUIController.getPluginTypeForCurrPluginParamsSelectedTab();
                        String currSelectedItemForPluginType = getComboBoxSelectedItemByPluginType(pluginTypeForCurrPluginParamsSelectedTab);
                        XTrackerPluginInformation currPluginInfo = getXTrackerPluginInformation(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                                currSelectedItemForPluginType);
                        // Not good doubling up calls, bit of a dogs dinner all around
                        // setting parameter files
                        String paramFileXsdSchemaLocationFileName = "";
                        Document document = XmlParser.getDomDocumentForXmlFile(xmlParameterFile);
                        try {

                            paramFileXsdSchemaLocationFileName = XmlParser.getXsdSchemaFileFromLocation(document);
                            
                        } catch (XsdSchemaFileNotSet ex) {
                            // Already checked for this in if clause
                        }

                        if(currPluginInfo.getXsdSchemaFileName().equals(paramFileXsdSchemaLocationFileName) == true) {
                        
                            currConfigModel.setPluginParamFileByPluginType(pluginTypeForCurrPluginParamsSelectedTab, xmlParameterFile.getName());

                            
                            try {

                                XTrackerParamFileModel paramFileModel = currConfigModel.getParamFileModelBySchemaFile(currPluginInfo.getXsdSchemaFileName());
                                currConfigModel.setPluginJarFileByPluginType(pluginTypeForCurrPluginParamsSelectedTab, currPluginInfo.getPluginFileName());

                                if(paramFileModel != null) {

                                    if(pluginParamsUIController.isParamFileModelForXsdFileDisplayed(currPluginInfo.getXsdSchemaFileName())) {

                                        String jarFile = pluginParamsUIController.getJarFileForXsdFileDisplayed(currPluginInfo.getXsdSchemaFileName());
                                        if(jarFile.equals(currPluginInfo.getPluginFileName()) == false) {

                                            PluginType pluginType = pluginParamsUIController.getPluginTypeForXsdFileDisplayed(currPluginInfo.getXsdSchemaFileName());

                                            pluginParamsUIController.removeScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab);
                                            pluginParamsUIController.displayDefaultScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                                   currPluginInfo.getPluginFileName(),
                                                                                                   "<html><p><center>Parameter file " + xmlParameterFile.getName() +
                                                                                                   "</center></p>" +
                                                                                                   "<p><center>is open for editing by plugin " + jarFile +
                                                                                                   "</center></p>" +
                                                                                                   "<p><center>Please select tab " + pluginType.getPluginTitle() +
                                                                                                   "</center></p>", 25);

                                            XTrackerParamFileModel model = currConfigModel.getParamFileModelByJarFileAssoc(jarFile);
                                            model.addJarFileAssocation(currPluginInfo.getPluginFileName());
                                            newParamsFileButton.setEnabled(false);
                                            openParamsFileButton.setEnabled(false);
                                        }

                                    } else {

                                        pluginParamsUIController.displayScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                        currentModelName,
                                                                                        currPluginInfo.getPluginFileName(),
                                                                                        currPluginInfo.getPluginClassName(),
                                                                                        currPluginInfo.getXsdSchemaFileName());
                                    }
                                    
                                } else {

                                    File xsdFile = FileUtils.getFile(fileDirectoryPath + paramFileXsdSchemaLocationFileName);
                                    currConfigModel.createXTrackerParamFileModel(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                 fileDirectoryPath,
                                                                                 paramFileXsdSchemaLocationFileName,
                                                                                 xsdFile);

                                    pluginParamsUIController.displayScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                    currentModelName,
                                                                                    currPluginInfo.getPluginFileName(),
                                                                                    currPluginInfo.getPluginClassName(),
                                                                                    paramFileXsdSchemaLocationFileName);
                                }


                            /*    pluginParamsUIController.displayScreenForPlugin(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                currentModelName,
                                                                                currPluginInfo.getPluginFileName(),
                                                                                currPluginInfo.getPluginClassName(),
                                                                                paramFileXsdSchemaLocationFileName);

                                switch(pluginTypeForCurrPluginParamsSelectedTab) {

                                    case RAWDATAPLUGIN: {

                                        break;
                                    }
                                    case IDENTDATAPLUGIN: {


                                        break;
                                    }
                                    case PEAKSELECTIONPLUGIN: {


                                        break;
                                    }
                                    case QUANTIFICATIONPLUGIN: {


                                        break;
                                    }
                                    case OUTPUTPLUGIN: {


                                        break;
                                    }
                                    default: {

                                    }
                                } */

                            } catch (FileNotFoundException ex) {

                                // should have already found xsd and parameter files
                            }
      
                        } else {

                            JOptionPane.showMessageDialog(this,
                                                          "Parameter file " +
                                                          xmlParameterFile.getName() +
                                                          " is not suitable for plugin " +
                                                          currPluginInfo.getPluginClassName(),
                                                          "Parameter File is Invalid",
                                                          JOptionPane.WARNING_MESSAGE);
                        }
                    
                    } else {

                        JOptionPane.showMessageDialog(this,
                                                      "Cannot open file " +
                                                      xmlParameterFile.getName(),
                                                      "Parameter File is Invalid",
                                                      JOptionPane.WARNING_MESSAGE);

                    }
                }

                // check if the run button can be enabled - checking for presence
                // of all five plugins, then checking for associated XML parameter
                // file models - so long as plugins are selected and associated
                // parameter file model are created then the run button is enabled
                if(checkXTrackerConfigModelIsComplete()) {

                    runAppButton.setEnabled(true);

                }  else {

                    runAppButton.setEnabled(false);
                }
            }
        }
    }//GEN-LAST:event_openParamsFileButtonActionPerformed

    /**
     *
     *
     */
    private void saveParamsFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveParamsFileButtonActionPerformed

        if (evt.getSource() instanceof JButton) {

            if(currentModelName.equals("") == false) {

                XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                PluginType pluginTypeForCurrPluginParamsSelectedTab = pluginParamsUIController.getPluginTypeForCurrPluginParamsSelectedTab();

                configModelManager.saveXTrackerConfigModelConfigurationData(currentModelName);
                configModelManager.saveXTrackerParamFileForPlugin(currentModelName, pluginTypeForCurrPluginParamsSelectedTab);

                pluginParamsUIController.resetParamFileNameToDefaultColour(pluginTypeForCurrPluginParamsSelectedTab);
                saveParamsFileButton.setEnabled(false);

            }
        }
    }//GEN-LAST:event_saveParamsFileButtonActionPerformed

    /**
     *
     *
     */
    private void pluginParamsTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pluginParamsTabbedPaneStateChanged

        if (evt.getSource() instanceof JTabbedPane) {

            if(currentModelName.equals("") == false) {

                XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();
                PluginType pluginTypeForCurrPluginParamsSelectedTab = pluginParamsUIController.getPluginTypeForCurrPluginParamsSelectedTab();
                if(pluginParamsUIController.isDefaultDisplayScreenSetForPlugin(pluginTypeForCurrPluginParamsSelectedTab) == false) {

                    newParamsFileButton.setEnabled(false);
                    openParamsFileButton.setEnabled(true);

                    if(configModelManager.checkXTrackerParamFileForUnsavedEditsForPlugin(currentModelName, pluginTypeForCurrPluginParamsSelectedTab)) {

                        saveParamsFileButton.setEnabled(true);

                    } else {

                        saveParamsFileButton.setEnabled(false);
                        pluginParamsUIController.resetParamFileNameToDefaultColour(pluginTypeForCurrPluginParamsSelectedTab);
                    }
                } else {

                    String currSelectedItemForPluginType = getComboBoxSelectedItemByPluginType(pluginTypeForCurrPluginParamsSelectedTab);
                    XTrackerPluginInformation currPluginInfo = getXTrackerPluginInformation(pluginTypeForCurrPluginParamsSelectedTab,
                                                                                            currSelectedItemForPluginType);

                    if(currPluginInfo != null && currPluginInfo.getXsdSchemaFileName() != null) {

                        newParamsFileButton.setEnabled(true);
                        openParamsFileButton.setEnabled(true);
                        saveParamsFileButton.setEnabled(false);
                        
                    } else {

                        newParamsFileButton.setEnabled(false);
                        openParamsFileButton.setEnabled(false);
                        saveParamsFileButton.setEnabled(false);
                    }
                }

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(pluginParamsTabbedPane.getSelectedIndex());
            }
        }
    }//GEN-LAST:event_pluginParamsTabbedPaneStateChanged

    private void loadRawPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadRawPanelMouseClicked

        if (evt.getSource() instanceof JPanel) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.RAWDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
            }  
        }
    }//GEN-LAST:event_loadRawPanelMouseClicked

    private void spectralIdentPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectralIdentPanelMouseClicked

        if (evt.getSource() instanceof JPanel) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_spectralIdentPanelMouseClicked

    private void peakSelectionPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peakSelectionPanelMouseClicked
        // TODO add your handling code here:
        if (evt.getSource() instanceof JPanel) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_peakSelectionPanelMouseClicked

    private void quantificationPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quantificationPanelMouseClicked
        // TODO add your handling code here:
        if (evt.getSource() instanceof JPanel) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
            //    currentHighlightedPanel = PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_quantificationPanelMouseClicked

    private void outputPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputPanelMouseClicked
        // TODO add your handling code here:
        if (evt.getSource() instanceof JPanel) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
              //  currentHighlightedPanel = PluginType.OUTPUTPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_outputPanelMouseClicked

    private void loadRawDataComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadRawDataComboBoxMouseClicked

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
              //  currentHighlightedPanel = PluginType.RAWDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_loadRawDataComboBoxMouseClicked

    private void spectralIdentComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spectralIdentComboBoxMouseClicked

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_spectralIdentComboBoxMouseClicked

    private void peakSelectionComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peakSelectionComboBoxMouseClicked

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_peakSelectionComboBoxMouseClicked

    private void quantificationComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quantificationComboBoxMouseClicked

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_quantificationComboBoxMouseClicked

    private void outputComboBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputComboBoxMouseClicked

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.OUTPUTPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_outputComboBoxMouseClicked

    private void loadRawDataComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loadRawDataComboBoxFocusGained

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.RAWDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.RAWDATAPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_loadRawDataComboBoxFocusGained

    private void spectralIdentComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_spectralIdentComboBoxFocusGained

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_spectralIdentComboBoxFocusGained

    private void peakSelectionComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_peakSelectionComboBoxFocusGained

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_peakSelectionComboBoxFocusGained

    private void quantificationComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_quantificationComboBoxFocusGained

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
              //  currentHighlightedPanel = PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex());
            }
        }
    }//GEN-LAST:event_quantificationComboBoxFocusGained

    private void outputComboBoxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputComboBoxFocusGained

        if (evt.getSource() instanceof JComboBox) {

            if(currentModelName.equals("") == false) {

                restoreBorderForPluginPanel(currentHighlightedPanel);
                setRedBorderForPluginPanel(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
             //   currentHighlightedPanel = PluginType.OUTPUTPLUGIN.getTabbedPaneIndex();

                pluginParamsUIController.setPluginParamsTabPaneIndex(PluginType.OUTPUTPLUGIN.getTabbedPaneIndex());
            }
            
        }
    }//GEN-LAST:event_outputComboBoxFocusGained

    /**
     *
     *
     */
    private void setRedBorderForPluginPanel(int panelIndex) {

        Border redBorder = new LineBorder(Color.red);

        switch(panelIndex) {

            case 0: {

                OverlayBorder.overlayBorder(loadRawPanel, redBorder);
                currentHighlightedPanel = PluginType.RAWDATAPLUGIN.getTabbedPaneIndex();
                break;
            }
            case 1: {

                OverlayBorder.overlayBorder(spectralIdentPanel, redBorder);
                currentHighlightedPanel = PluginType.IDENTDATAPLUGIN.getTabbedPaneIndex();
                break;
            }
            case 2: {

                OverlayBorder.overlayBorder(peakSelectionPanel, redBorder);
                currentHighlightedPanel = PluginType.PEAKSELECTIONPLUGIN.getTabbedPaneIndex();
                break;
            }
            case 3: {

                OverlayBorder.overlayBorder(quantificationPanel, redBorder);
                currentHighlightedPanel = PluginType.QUANTIFICATIONPLUGIN.getTabbedPaneIndex();
                break;
            }
            case 4: {

                OverlayBorder.overlayBorder(outputPanel, redBorder);
                currentHighlightedPanel = PluginType.OUTPUTPLUGIN.getTabbedPaneIndex();
                break;
            }
        }
    }

    /**
     *
     *
     */
    private void restoreBorderForPluginPanel(int panelIndex) {

        switch(panelIndex) {

            case 0: {

                OverlayBorder.restoreBorder(loadRawPanel);
                break;
            }
            case 1: {

                OverlayBorder.restoreBorder(spectralIdentPanel);
                break;
            }
            case 2: {

                OverlayBorder.restoreBorder(peakSelectionPanel);
                break;
            }
            case 3: {

                OverlayBorder.restoreBorder(quantificationPanel);
                break;
            }
            case 4: {

                OverlayBorder.restoreBorder(outputPanel);
                break;
            }
        }
    }
    
    /**
     *
     *
     */
    private boolean exitXtrackerUI() {

        boolean exitConfirmed = false;

        XTrackeConfigModelManager configModelManager = XTrackeConfigModelManager.createInstance();            
        if(configModelManager.checkXtrackerConfigModelForUnSavedChanges(currentModelName)) {

            int retOption = JOptionPane.showConfirmDialog(this,
                                                          "Save changes to " + currentModelName + " before exiting?",
                                                          "Exit xTrackerGui Application",
                                                          JOptionPane.YES_NO_CANCEL_OPTION,
                                                          JOptionPane.QUESTION_MESSAGE);

            if(retOption == JOptionPane.YES_OPTION) {

                configModelManager.saveXTrackerConfigModel(currentModelName);
                configModelManager.closeXTrackerConfigModel(currentModelName);
                exitConfirmed = true;
                
            } else if(retOption == JOptionPane.NO_OPTION) {

                configModelManager.closeXTrackerConfigModel(currentModelName);
                exitConfirmed = true;
            }
        } else {

            exitConfirmed = true;
        }

        return exitConfirmed;
    }

    /**
     *
     *
     */
/*    private class XTrackerGuiFocusTraversalPolicy extends FocusTraversalPolicy
    {
        Vector<Component> order;

        public XTrackerGuiFocusTraversalPolicy(Vector<Component> order) {

            this.order = new Vector<Component>(order.size());
            this.order.addAll(order);
        }
        @Override
        public Component getComponentAfter(Container focusCycleRoot,
                                           Component aComponent) {

            //  int idx = (order.indexOf(aComponent) + 1) % order.size();
            
            int idx = currentHighlightedPanel;
            if(aComponent instanceof JComboBox) {
            
                idx = (currentHighlightedPanel + 1) % order.size();
            }

            System.out.println("Curr HP " + currentHighlightedPanel + " idx " + idx + " Component " + aComponent);

            return order.get(idx);
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot,
                                            Component aComponent) {

          //  int idx = order.indexOf(aComponent) - 1;

            int idx = currentHighlightedPanel - 1;

            if (idx < 0) {

                idx = order.size() - 1;
            }

            return order.get(idx);
        }

        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {

            return order.get(currentHighlightedPanel);
        }

        @Override
        public Component getLastComponent(Container focusCycleRoot) {

            return order.lastElement();
        }

        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            
            return order.get(0);
        }
    } */

    /**
     *
     *
     */
    private class WindowExitListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent evt) {

            if(exitXtrackerUI()) {

                System.exit(0);
            }
        }
    }

    /**
     * 
     *
     */
    private static void createAndShowGUI() {
        
        XTrackerGuiUIFrame uiFrame = new XTrackerGuiUIFrame();

        URL imageURL = uiFrame.getClass().getResource("images/xtracker-icon.png");
        if (imageURL != null) {
            ImageIcon xTrackerIcon = new ImageIcon(imageURL);
            uiFrame.setIconImage(xTrackerIcon.getImage());
        }

        uiFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        uiFrame.pack();
        uiFrame.setLocationRelativeTo(null);

     /*   final SplashScreen splash = SplashScreen.getSplashScreen();

        if(splash != null) {

            Graphics2D graphics = splash.createGraphics();

            if (graphics != null) {

                try {

                    Thread.sleep(1500L);
                }
                catch(InterruptedException e) {

                }
            } else {

                System.out.println("graphics is null");
            }

            splash.close();

        } else {

            System.out.println("SplashScreen.getSplashScreen() returned null");   
        } */
        
        uiFrame.setVisible(true);
        
    }

    /**
     *
     * @param args the command line argument
     */
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar UIMenuBar;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenu configMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel informationPanel;
    private javax.swing.JScrollPane informationScrollPane;
    private javax.swing.JTextArea informationTextArea;
    private javax.swing.JComboBox loadRawDataComboBox;
    private javax.swing.JPanel loadRawPanel;
    private javax.swing.JScrollPane loadRawScrollPane;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JButton newParamsFileButton;
    private javax.swing.JMenuItem openConfigMenuItem;
    private javax.swing.JButton openParamsFileButton;
    private javax.swing.JComboBox outputComboBox;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JScrollPane peakSelScrollPane;
    private javax.swing.JComboBox peakSelectionComboBox;
    private javax.swing.JPanel peakSelectionPanel;
    private javax.swing.JPanel pluginParamsButtonPanel;
    private javax.swing.JPanel pluginParamsPanel;
    private javax.swing.JTabbedPane pluginParamsTabbedPane;
    private javax.swing.JPanel pluginSelectionPanel;
    private javax.swing.JScrollPane quantScrollPane;
    private javax.swing.JComboBox quantificationComboBox;
    private javax.swing.JPanel quantificationPanel;
    private javax.swing.JButton runAppButton;
    private javax.swing.JPanel runAppPanel;
    private javax.swing.JButton saveParamsFileButton;
    private javax.swing.JComboBox spectralIdentComboBox;
    private javax.swing.JPanel spectralIdentPanel;
    private javax.swing.JScrollPane spectralIdentScrollPane;
    // End of variables declaration//GEN-END:variables

}
