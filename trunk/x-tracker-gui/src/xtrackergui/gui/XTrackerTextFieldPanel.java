
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerTextFieldPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.MigLayout;
import xtrackergui.model.XTrackerXmlDocumentRow;
import xtrackergui.model.XTrackerXmlDocumentRowAttribute;

/**
 * A xTracker GUI component to display/ input numerical or string based data
 * 
 * @author andrew bullimore
 */
public class XTrackerTextFieldPanel extends javax.swing.JPanel implements XTrackerGuiComponent {

    private JPanel parentDisplayPanel = null;
    // the row in the XTrackerXmlDocument this display component 'works' for
    private XTrackerXmlDocumentRow xTrackerXmlDocumentRow;
    private XTrackerInputFilter inputDocumentFilter;
    private String attributeName = "";
    private JLabel textDescriptionLabel;
    private JTextField textInputField;
    private JLabel inputVerifiedLabel;
    private ImageIcon inputOkayIcon = null;
    private ImageIcon inputInCorrectIcon = null;
    private JButton removeButton = null;
    
    /**
     * 
     *
     * @param textDescLabel
     */
    public XTrackerTextFieldPanel(XTrackerXmlDocumentRow row, String displayAttributeName, String textDescLabel) {

        this.setLayout(new MigLayout());

        xTrackerXmlDocumentRow = row;
        attributeName = displayAttributeName;

        textDescriptionLabel = new JLabel();
        textDescriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        textDescriptionLabel.setText(textDescLabel);

        this.add(textDescriptionLabel, "width 200!");

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
    public JComponent getComponent() {

        return this;
    }

    @Override
    public void updateEnteredValue() {

        if(attributeName.equals("") == false) {

            XTrackerXmlDocumentRowAttribute attr = xTrackerXmlDocumentRow.getAttribute(attributeName);
            textInputField.setText(attr.getAttributeValue());

        } else {

            textInputField.setText(xTrackerXmlDocumentRow.getTagValue());
        }
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

