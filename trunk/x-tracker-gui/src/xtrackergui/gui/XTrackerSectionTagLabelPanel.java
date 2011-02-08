
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerSectionTagLabelPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import xtrackergui.model.XTrackerXmlDocumentRow;

/**
 * GUI JPanel - displays the tag name of a section in a xml document
 *
 * @author andrew bullimore
 */
public class XTrackerSectionTagLabelPanel extends javax.swing.JPanel implements XTrackerGuiComponent {

    private JPanel parentDisplayPanel = null;
    // the row in the XTrackerXmlDocument this display component 'works' for
    private XTrackerXmlDocumentRow xTrackerXmlDocumentRow;
    private JLabel sectionTagDescriptionLabel;

    public XTrackerSectionTagLabelPanel(XTrackerXmlDocumentRow row, int headingStyle, String textDescLabel) {

        this.setLayout(new MigLayout());

        xTrackerXmlDocumentRow = row;

        sectionTagDescriptionLabel = new JLabel(textDescLabel);
        if(headingStyle == 1) {

            sectionTagDescriptionLabel.setFont(new Font("Tahoma", Font.PLAIN, 25));

        } else if(headingStyle == 2) {

            sectionTagDescriptionLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
   
        }

        add(sectionTagDescriptionLabel, "center");
    }


    @Override
    public JComponent getComponent() {

        return this;
    }

    @Override
    public void updateEnteredValue() {

        // Not reuired - though could maybe manipulate the label text using this
        // method
    }

    @Override
    public void setDoNotAddValueToXml() {
        
        xTrackerXmlDocumentRow.setExcludeFromXmlDocument(true);
    }

    @Override
    public void setXTrackerXmlDocumentRow(XTrackerXmlDocumentRow documentRow) {

        xTrackerXmlDocumentRow = documentRow;
    }

    @Override
    public void setXTrackerInputFilter(XTrackerInputFilter filter) {

        // Not required
    }

    @Override
    public void setGuiComponentParentDisplayPanel(JPanel parentPanel) {

        parentDisplayPanel = parentPanel;
    }
}
