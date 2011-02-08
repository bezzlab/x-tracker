
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerFileChooserPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultCaret;
import net.miginfocom.swing.MigLayout;
import xtrackergui.model.XTrackerXmlDocumentRow;

/**
 * A file chooser xTracker GUI component
 *
 * @author andrew bullimore
 */
public class XTrackerFileChooserPanel extends javax.swing.JPanel implements XTrackerGuiComponent {

    private boolean isRequired = false;
    private JPanel parentDisplayPanel = null;
    // the row in the XTrackerXmlDocument this display component 'works' for
    private XTrackerXmlDocumentRow xTrackerXmlDocumentRow;
    private String attributeName = "";
    private XTrackerInputFilter inputDocumentFilter;
    private JLabel textDescriptionLabel;
    private JTextField textInputField;
    private JButton fileBrowseButton;
    private JButton clearTextInpurFieldButton;
    private JLabel inputVerifiedLabel;
    private ImageIcon inputOkayIcon = null;
    private ImageIcon inputInCorrectIcon = null;
    private JButton removeButton = null;
    private JPanel selfReference = this;
    private FileNameExtensionFilter xmlFileFilter = new FileNameExtensionFilter("XML file (*.xml)", "xml");

    /**
     * Create a file chooser xTracker GUI component
     *
     * @param row 
     * @param textDescLabel
     * @param required
     */
    public XTrackerFileChooserPanel(XTrackerXmlDocumentRow row, String displayAttributeName, String textDescLabel, boolean required) {

        this.setLayout(new MigLayout());

        xTrackerXmlDocumentRow = row;
        attributeName = displayAttributeName;

        isRequired = required;

        textDescriptionLabel = new JLabel();
        textDescriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        textDescriptionLabel.setText(textDescLabel);

        this.add(textDescriptionLabel, "width 200!");

        textInputField = new JTextField();
        textInputField.setEditable(false);
      //  textInputField.getDocument().addDocumentListener(new TextInputFieldDocumentListener());
        textInputField.setCaret(new DefaultCaret() {

            @Override
            public void focusGained(FocusEvent evt) {

                super.focusGained(evt);
                setVisible(true);
            }
        });
        this.add(textInputField, "width 200!");

        fileBrowseButton = new JButton("Browse");
        fileBrowseButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {

                if (evt.getSource() instanceof JButton) {

                    JFileChooser fileChooser = new JFileChooser();
                  //  fileChooser.addChoosableFileFilter(xmlFileFilter);
                    int returnVal = fileChooser.showOpenDialog(selfReference);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {

                        File file = fileChooser.getSelectedFile();
                        String filePath = file.getAbsolutePath();
                        textInputField.setText(filePath);
                    }
                }
            }
        });
        this.add(fileBrowseButton, "width 90!");

        clearTextInpurFieldButton = new JButton("Clear");
        clearTextInpurFieldButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {

                if (evt.getSource() instanceof JButton) {

                    textInputField.setText("");
                    if(parentDisplayPanel instanceof XTrackerPluginParamsDisplayPanel) {

                        ((XTrackerPluginParamsDisplayPanel)parentDisplayPanel).updatePluginParamsGuiSaveButton();
                    }
                }
            }
        });
        this.add(clearTextInpurFieldButton, "width 90!");

        {
            URL imageURL = getClass().getResource("images/excl-triangle-glow.png");
            if (imageURL != null) {

                inputInCorrectIcon = new ImageIcon(imageURL);
                inputVerifiedLabel = new JLabel(inputInCorrectIcon);
                this.add(inputVerifiedLabel, "width 60");
            }
        }

        {
            URL imageURL = getClass().getResource("images/tick-glow.png");
            if (imageURL != null) {

                inputOkayIcon = new ImageIcon(imageURL);
            }
        }
    }

    /**
     *
     * @param button
     */
    public void addButton(JButton button) {

        if(removeButton == null) {

            removeButton = button;
            removeButton.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {

                    removeButtonActionPerformed();
                }
            });

            this.add(removeButton, "width 90!");
            this.revalidate();
        }
    }

    /**
     * Specify that the data entered in this xTracker GUI component, if any, is not to appear in any xml file generated
     *
     */
    @Override
    public void setDoNotAddValueToXml() {

        xTrackerXmlDocumentRow.setExcludeFromXmlDocument(true);
    }

    /**
     *
     *
     */
    @Override
    public JComponent getComponent() {

        return this;
    }

    /**
     *
     *
     */
    @Override
    public void updateEnteredValue() {

        textInputField.setText(xTrackerXmlDocumentRow.getTagValue());
    }

    /**
     *
     *
     */
    private void removeButtonActionPerformed() {

        this.setDoNotAddValueToXml();
        JComponent parent = (JComponent)this.getParent();
        parent.remove(this);
	parent.revalidate();
	parent.repaint();
    }

    @Override
    public void setXTrackerXmlDocumentRow(XTrackerXmlDocumentRow documentRow) {
        
        xTrackerXmlDocumentRow = documentRow;
    }

    @Override
    public void setXTrackerInputFilter(XTrackerInputFilter inputFilter) {
        
        inputDocumentFilter = inputFilter;

        inputFilter.setLabel(inputVerifiedLabel);
        inputFilter.setInputValidIcon(inputOkayIcon);
        inputFilter.setInputInvalidIcon(inputInCorrectIcon);
        inputFilter.setXTrackerXmlDocumentRow(xTrackerXmlDocumentRow);
        inputFilter.setAttributeName(attributeName);

        AbstractDocument doc = (AbstractDocument) textInputField.getDocument();
        doc.setDocumentFilter(inputDocumentFilter);
    }

    @Override
    public void setGuiComponentParentDisplayPanel(JPanel parentPanel) {
        
        parentDisplayPanel = parentPanel;
        AbstractDocument doc = (AbstractDocument) textInputField.getDocument();
        ((XTrackerInputFilter)doc.getDocumentFilter()).setParerntDisplayPanel(parentDisplayPanel);
    }
}
