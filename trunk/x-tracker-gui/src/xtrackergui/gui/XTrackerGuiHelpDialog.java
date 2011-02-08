
//
//    xTrackerGui
//
//    Package:    xtrackergui.gui
//    File:       XTrackerGuiHelpDialog.java
//    Date:       01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.awt.BorderLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import org.apache.log4j.Logger;

/**
 * Help information for xTracker GUI
 * 
 * @author andrew bullimore
 */
public class XTrackerGuiHelpDialog extends javax.swing.JFrame implements TreeSelectionListener {

    // Viewer for help pages - right side of display split pane
    private JEditorPane htmlViewerPane;
    // Tree view of help pages - left side of display split pane
    private JTree helpTocTree;

    // logger - any info logged is displayed in the main GUI textarea
    private Logger logger = Logger.getLogger(xtrackergui.gui.XTrackerGuiHelpDialog.class.getName());

    /**
     * Create help dialog
     * 
     * @param parentFrame The parent frame the dialog is invoked from
     * @param frameTitle The title of this dialog
     */
    public XTrackerGuiHelpDialog(XTrackerGuiUIFrame parentFrame, String frameTitle) {

        // create root node - display welome page
        DefaultMutableTreeNode helpTocRootNode = createNode("Welcome to xTrackerGui", "help/welcome.html");
        
        helpTocTree = new JTree(createHelpToc(helpTocRootNode));
        helpTocTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        helpTocTree.addTreeSelectionListener(this);

        htmlViewerPane = new JEditorPane();
        htmlViewerPane.setContentType("text/html");
        htmlViewerPane.setEditable(false);

        JScrollPane helpTocTreeViewer = new JScrollPane(helpTocTree);
        JScrollPane htmlViewerScrollPane = new JScrollPane(htmlViewerPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(150);

        splitPane.setLeftComponent(helpTocTreeViewer);
        
        JPanel helpRightPanel = new JPanel(new BorderLayout());
        helpRightPanel.add(BorderLayout.CENTER, htmlViewerScrollPane);
        splitPane.setRightComponent(helpRightPanel);

        add(splitPane);

        Object nodeInfo = helpTocRootNode.getUserObject();
        XTrackerHelpInformation helpInformation = (XTrackerHelpInformation)nodeInfo;
        displayURL(helpInformation.helpTextURL);
        
        setTitle(frameTitle);
        setPreferredSize(new Dimension(750, 500));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        // position centrally against paernt frame
        setLocationRelativeTo(parentFrame);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)helpTocTree.getLastSelectedPathComponent();

        if(node != null) {

            Object nodeInfo = node.getUserObject();
            XTrackerHelpInformation helpInformation = (XTrackerHelpInformation)nodeInfo;
            // display the help page
            displayURL(helpInformation.helpTextURL);
        }

        return;
    }

    /**
     * Holds help page title and resource file name for the page
     * 
     */
    private class XTrackerHelpInformation {

        private final URL helpTextURL;
        private final String helpTextNodeTitle;

        /**
         *
         * @param title Title of the help page
         * @param fileName name of help file to display
         */
        public XTrackerHelpInformation(String title, String fileName) {

            helpTextNodeTitle = title;
            helpTextURL = XTrackerGuiHelpDialog.class.getResource(fileName);

            if(helpTextURL == null) {
                
                logger.error("XTrackerHelpInformation::XTrackerHelpInformation: Problem Couldn't find help file: " + fileName);
            }
        }

        @Override
        public String toString() {
            
            return helpTextNodeTitle;
        }
    }

    /**
     * Sets the page of help to be displayed in the htmlViewerPane
     *
     * @param url URL of the help file to display
     */
    private void displayURL(URL url) {
        try {
            if (url != null) {

                htmlViewerPane.setPage(url);
                
            } else {

		htmlViewerPane.setText("File Not Found");
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

    /**
     * Create a node for a help file in the jTree display of help files
     *
     * @param title Title of the help page
     * @param urlPath URL of the help file to display
     */
    private DefaultMutableTreeNode createNode(String title, String urlPath) {

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new XTrackerHelpInformation(title, urlPath));

        return node;
    }

    /**
     * Create the table of contents for the help dialog
     *
     * @param rootNode The root node of the jTree display of help files - displays welcome page
     */
    private DefaultMutableTreeNode createHelpToc(DefaultMutableTreeNode rootNode) {

        DefaultMutableTreeNode helpCategoryNode = null;
       // DefaultMutableTreeNode helpInformationNode = null;

        helpCategoryNode = createNode("X-Tracker GUI Help", "help/xtrackerguihelp.html");
        rootNode.add(helpCategoryNode);
        helpCategoryNode = createNode("X-Tracker GUI Keyboard Shortcuts", "help/xtrackerguikeyboardshortcuts.html");
        rootNode.add(helpCategoryNode);
        
        return rootNode;
    }
}
