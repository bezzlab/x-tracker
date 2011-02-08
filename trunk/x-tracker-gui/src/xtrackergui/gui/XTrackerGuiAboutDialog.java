
//
//    xTrackerGui
//
//    Package:    xtrackergui.gui
//    File:       XTrackerGuiAboutDialog.java
//    Date:       01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

/**
 * Information about xTracker GUI and xTracker
 *
 * @author andrew bullimore
 */
public class XTrackerGuiAboutDialog extends javax.swing.JDialog {

    // reference to 'this' dialog
    private final JDialog selfReference = this;

    // logger - any info logged is displayed in the main GUI textarea
    private Logger logger = Logger.getLogger(xtrackergui.gui.XTrackerGuiAboutDialog.class.getName());

    /**
     * Create the About xTracker GUI dialog
     *
     * @param owner Parent frame of this dialog
     * @param title Title for the dialog
     * @param modal Set the dialog as modal if required
     */
    public XTrackerGuiAboutDialog(javax.swing.JFrame owner, final String title, final boolean modal) {

        super(owner, title, modal);

        setResizable(false);
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new MigLayout("fill, wrap 1, center"));

        URL imageURL = getClass().getResource("images/xtracker.png");
        if (imageURL != null) {
            ImageIcon xTrackerIcon = new ImageIcon(imageURL);
            aboutPanel.add(new JLabel(xTrackerIcon), "center");
        }

        // Information about xtracker GUI/ xTracker - displayed in label informationTextArea
        String information = "<html><p><b>X-Tracker: the Generic Tool for Quantitative Proteomics by Mass Spectrometry<b></p>" +
                             "<p><center>By Dr. Luca Bianco and Dr. Conrad Bessant</center></p>" +
                             "<p>Cranfield University, Building 63, Cranfield Health, MK43 0AL, United Kingdom</p>" +
                             "<p><center>Contributors</center>" +
                             "<center>Andrew Bullimore</center>" +
                             "<center>Laurie Tonon</center><br></p></html>";
        
        JLabel informationTextArea = new JLabel();
        informationTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        informationTextArea.setText(information);
        aboutPanel.add(informationTextArea, "center");

        // url for xTracker website - displayed in label urlLabel NB if Java 6 and above is installed
        // clicking this label with a mouse click should invoke the default web browser using java Desktop
        // and the xTracker website should display
        JLabel urlLabel = new JLabel("<html><font color=blue>Website: http://www.x-tracker.info</font></html>");
        urlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                
                if (Desktop.isDesktopSupported()) {

                    Desktop desktop = Desktop.getDesktop();
                    try {

                        desktop.browse(new URI("http://www.x-tracker.info"));
                        
                    } catch (Exception e) {

                        if(logger.isDebugEnabled()) {
                        
                            logger.debug("XTrackerGuiAboutDialog::XTrackerGuiAboutDialog: Problem - cannot find location http://www.x-tracker.info");
                        }
                    }
                } else {

                    if(logger.isDebugEnabled()) {

                        logger.debug("XTrackerGuiAboutDialog::XTrackerGuiAboutDialog: Java Desktop not supported (please install Java 6 or above)");
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent evt) {

                // change cursor when hovering over urlLabel
                Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
                setCursor(handCursor);
            }

             @Override
            public void mouseExited(MouseEvent evt) {

                // reset cursor to normal when moveing away from urlLabel
                Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                setCursor(defaultCursor);
            }
        });

        aboutPanel.add(urlLabel, "center");

        // email for Conrad Bessant - displayed in label emailLabel NB if Java 6 and above is installed
        // clicking this label with a mouse click should invoke the default e-mail client using java Desktop
        JLabel emailLabel = new JLabel("<html><font color=blue>Contact: c.bessant@cranfield.ac.uk</font></html>");
        emailLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                
                if (Desktop.isDesktopSupported()) {

                    Desktop desktop = Desktop.getDesktop();
                    try {

                         desktop.mail(new URI("mailto", "c.bessant@cranfield.ac.uk", null));

                    } catch (Exception e) {

                        if(logger.isDebugEnabled()) {
                            
                            logger.debug("XTrackerGuiAboutDialog: Problem - cannot mailto c.bessant@cranfield.ac.uk");
                        }
                    }
                } else {

                    if(logger.isDebugEnabled()) {
                    
                        logger.debug("XTrackerGuiAboutDialog: Java Desktop not supported");
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent evt) {

                // change cursor when hovering over emailLabel
                Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
                setCursor(handCursor);
            }

             @Override
            public void mouseExited(MouseEvent evt) {

                // reset cursor to normal when moveing away from emailLabel
                Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                setCursor(defaultCursor);
            }
        });

        aboutPanel.add(emailLabel, "center");

        JButton okButton = new JButton("Ok");
        aboutPanel.add(okButton, "center");

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                selfReference.dispose();
            }
        });

        getContentPane().add(aboutPanel);
        pack();
        setLocationRelativeTo(owner);
    }
}
