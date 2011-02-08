
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerComboBoxAndTextFieldPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.MigLayout;
import xtrackergui.model.XTrackerXmlDocumentRow;

/**
 *
 * @author andrew bullimore
 */
public class XTrackerComboBoxAndTextFieldPanel extends javax.swing.JPanel implements XTrackerGuiComponent {

    private boolean isRequired = false;
    // the row in the XTrackerXmlDocument this display component 'works' for
    private XTrackerXmlDocumentRow xTrackerXmlDocumentRow;
    private String attributeName = "";
    private XTrackerInputFilter inputDocumentFilter;
    private JLabel textDescriptionLabel;
    private JComboBox comboBoxEnumsField;
    private String comboBoxSelectedItem = null;
    private JTextField textInputField;
    private JLabel inputVerifiedLabel;
    private ImageIcon inputOkayIcon = null;
    private ImageIcon inputInCorrectIcon = null;
    private JButton removeButton = null;

    /**
     *
     *
     */
    public XTrackerComboBoxAndTextFieldPanel(XTrackerXmlDocumentRow row, String displayAttributeName, String comboBoxDescLabel) {

        this.setLayout(new MigLayout());

        xTrackerXmlDocumentRow = row;
        attributeName = displayAttributeName;

    //    isRequired = ;

        textDescriptionLabel = new JLabel();
        textDescriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        textDescriptionLabel.setText(comboBoxDescLabel);
        this.add(textDescriptionLabel, "width 200!");

        comboBoxEnumsField = new JComboBox();
     //   comboBoxEnumsField.setModel(new DefaultComboBoxModel(xsdEnumerations.toArray()));
        comboBoxEnumsField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {

                comboBoxSelectedItem = (String) comboBoxEnumsField.getSelectedItem();

                if(comboBoxSelectedItem != null && comboBoxSelectedItem.equals("") == false) {

                    inputVerifiedLabel.setIcon(inputOkayIcon);
                    if(isRequired == false) {

                        inputVerifiedLabel.setEnabled(true);
                    }

                } else {

                        inputVerifiedLabel.setIcon(inputInCorrectIcon);
                        inputVerifiedLabel.setEnabled(isRequired);
                }
            }
        });
        this.add(comboBoxEnumsField, "width 120!");

        textInputField = new JTextField();
        this.add(textInputField, "width 120!");

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

        inputVerifiedLabel.setEnabled(isRequired);
    }

    /**
     *
     *
     */
    public void setIsRequired(boolean required) {

        isRequired = required;
        inputVerifiedLabel.setEnabled(isRequired);
    }

    /**
     *
     *
     */
    public boolean getIsRequired() {

        return isRequired;
    }

    /**
     *
     *
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
     *
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
    private void removeButtonActionPerformed() {

        this.setDoNotAddValueToXml();
        JComponent parent = (JComponent)this.getParent();
        parent.remove(this);
	parent.revalidate();
	parent.repaint();
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

        comboBoxEnumsField.setSelectedItem(xTrackerXmlDocumentRow.getTagValue());
        textInputField.setText(xTrackerXmlDocumentRow.getTagValue());
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
