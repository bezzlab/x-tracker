
//
//    xTrackerGui
//
//    Package: xtrackergui.gui
//    File: XTrackerDefaultPluginParamsDisplayPanel.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * General utility screen to display information to the user
 *
 * @author andrew bullimore
 */
public class XTrackerDefaultPluginParamsDisplayPanel extends javax.swing.JPanel {

    // label for displaying information to the user
    JLabel informationTextLabel;
    // this is a general screen - title it thus
    String title = "DefaultScreen";

    public XTrackerDefaultPluginParamsDisplayPanel(String screenTitle, String labelText, int fontSize) {

        setLayout(new BorderLayout());
     //   setLayout(new MigLayout("center"));

        if(screenTitle.equals("") == false) {

            title = screenTitle;
        }

        informationTextLabel = new JLabel(labelText);
        informationTextLabel.setFont(new Font("Tahoma", Font.PLAIN, fontSize));

        add(informationTextLabel);
        informationTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Set the preffered size of this JPanel
     *
     * @param size The preferred size of the JPanel
     */
    @Override
    public void setPreferredSize(Dimension size) {

        super.setPreferredSize(size);
        revalidate();
	repaint();
    }

    /**
     * Return the title of this screen
     *
     */
    public String getTitle() {

        return title;
    }

    /**
     * Update the text displayed by the JLabel
     *
     * @param labelText New text to display
     */
    public void updateInformationTextLabel(String labelText) {

        informationTextLabel.setText("");
        informationTextLabel.setText(labelText);
    }
}
