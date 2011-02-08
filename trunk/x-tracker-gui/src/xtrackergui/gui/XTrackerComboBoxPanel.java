
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerComboBoxPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import xtrackergui.model.XTrackerXmlDocumentRow;
import xtrackergui.model.XTrackerXmlDocumentRowAttribute;

/**
 * A xTracker GUI component to display a list of choices in a JComboBox
 *
 * @author andrew bullimore
 */
public class XTrackerComboBoxPanel extends javax.swing.JPanel implements XTrackerGuiComponent {

    private boolean isRequired = false;
    private JPanel parentDisplayPanel = null;
    // the row in the XTrackerXmlDocument this display component 'works' for
    private XTrackerXmlDocumentRow xTrackerXmlDocumentRow;
    private String attributeName = "";
    private JLabel textDescriptionLabel;
    private JComboBox comboBoxEnumsField;
    private String comboBoxSelectedItem = null;
    private JLabel inputVerifiedLabel;
    private ImageIcon inputOkayIcon = null;
    private ImageIcon inputInCorrectIcon = null;
    private JButton removeButton = null;

    public XTrackerComboBoxPanel(XTrackerXmlDocumentRow row, String displayAttributeName, String descLabel, boolean required, List<String> xsdEnumerations) {

        this.setLayout(new MigLayout());

        xTrackerXmlDocumentRow = row;
        attributeName = displayAttributeName;

        isRequired = required;

        textDescriptionLabel = new JLabel();
        textDescriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        textDescriptionLabel.setText(descLabel);
        this.add(textDescriptionLabel, "width 200!");

        comboBoxEnumsField = new JComboBox();
        comboBoxEnumsField.setModel(new DefaultComboBoxModel(xsdEnumerations.toArray()));
        comboBoxEnumsField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {

                comboBoxSelectedItem = (String) comboBoxEnumsField.getSelectedItem();

                if(comboBoxSelectedItem != null && comboBoxSelectedItem.equals("") == false) {

                    inputVerifiedLabel.setIcon(inputOkayIcon);
                    if(attributeName.equals("") == false) {

                        XTrackerXmlDocumentRowAttribute attr = xTrackerXmlDocumentRow.getAttribute(attributeName);
                        attr.setAttributeValue((String) comboBoxEnumsField.getSelectedItem());

                    } else {

                        xTrackerXmlDocumentRow.setTagValue((String) comboBoxEnumsField.getSelectedItem());
                    }

                    if(isRequired == false) {

                        inputVerifiedLabel.setEnabled(true);
                    }
                } else {

                    inputVerifiedLabel.setIcon(inputInCorrectIcon);
                    inputVerifiedLabel.setEnabled(isRequired);
                    xTrackerXmlDocumentRow.setTagValue((String) comboBoxEnumsField.getSelectedItem());
                }

                if(parentDisplayPanel instanceof XTrackerPluginParamsDisplayPanel) {

                    ((XTrackerPluginParamsDisplayPanel)parentDisplayPanel).updatePluginParamsGuiSaveButton();
                }
            }
        });
        this.add(comboBoxEnumsField, "width 120!");

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
     * A xTracker GUI component to display a list of choices in a JComboBox
     *
     * @author andrew bullimore
     */
    public void setIsRequired(boolean required) {

        isRequired = required;
        inputVerifiedLabel.setEnabled(isRequired);
    }

    public boolean getIsRequired() {

        return isRequired;
    }

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

    @Override
    public void setDoNotAddValueToXml() {

        xTrackerXmlDocumentRow.setExcludeFromXmlDocument(true);
    }

    private void removeButtonActionPerformed() {

        this.setDoNotAddValueToXml();
        JComponent parent = (JComponent)this.getParent();
        parent.remove(this);
	parent.revalidate();
	parent.repaint();
    }

    @Override
    public JComponent getComponent() {

        return this;
    }

    @Override
    public void updateEnteredValue() {

        if(attributeName.equals("") == false) {

            XTrackerXmlDocumentRowAttribute attr = xTrackerXmlDocumentRow.getAttribute(attributeName);
            comboBoxEnumsField.setSelectedItem(attr.getAttributeValue());

        } else {

            comboBoxEnumsField.setSelectedItem(xTrackerXmlDocumentRow.getTagValue());
        }
    }

    @Override
    public void setXTrackerXmlDocumentRow(XTrackerXmlDocumentRow documentRow) {

        xTrackerXmlDocumentRow = documentRow;
    }

    @Override
    public void setXTrackerInputFilter(XTrackerInputFilter inputFilter) {
        
        // Not required
    }

    @Override
    public void setGuiComponentParentDisplayPanel(JPanel parentPanel) {
        
        parentDisplayPanel = parentPanel;
    }
}
