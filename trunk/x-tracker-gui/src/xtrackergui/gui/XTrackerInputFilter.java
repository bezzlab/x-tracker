
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerInputFilter.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import xtrackergui.model.XTrackerXmlDocumentRow;
import xtrackergui.model.XTrackerXmlDocumentRowAttribute;

/**
 *
 *
 * @author andrew bullimore
 */
public class XTrackerInputFilter extends DocumentFilter {

    private boolean required = true;
    private XTrackerXmlDocumentRow row;
    private JPanel parentDisplayPanel;
    private String attributeName = "";
    private JLabel parentIconLabel;
    private ImageIcon inputValidIcon = null;
    private ImageIcon inputInvalidIcon = null;
    private Pattern pattern;
    private String patternString = null;

    public XTrackerInputFilter(boolean isRequired,
                               String patternToCheck) {

        required = isRequired;
        pattern = Pattern.compile(patternToCheck);
        patternString = patternToCheck;
    }

    public void setParerntDisplayPanel(JPanel parentPanel) {

        parentDisplayPanel = parentPanel;
    }

    public void setLabel(JLabel label) {

        parentIconLabel = label;
    }

    public void setInputValidIcon(ImageIcon valid) {

        inputValidIcon = valid;
    }

    public void setInputInvalidIcon(ImageIcon invalid) {

        inputInvalidIcon = invalid;
    }

    public void setXTrackerXmlDocumentRow(XTrackerXmlDocumentRow documentRow) {

        row = documentRow;
    }

    public void setAttributeName(String name) {

        attributeName = name;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {

        String testStr = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
        Matcher m = pattern.matcher(testStr);

        if(testStr.length() == 0) {

            parentIconLabel.setIcon(inputInvalidIcon);
            if(attributeName.equals("") == false) {

                XTrackerXmlDocumentRowAttribute rowAttribute = row.getAttribute(attributeName);
          //      rowAttribute.resetTagValue();
                rowAttribute.setAttributeValue(testStr);

            } else {

         //       row.resetTagValue();
                row.setTagValue(testStr);
            }
            
            if(parentDisplayPanel instanceof XTrackerPluginParamsDisplayPanel) {

                ((XTrackerPluginParamsDisplayPanel)parentDisplayPanel).updatePluginParamsGuiSaveButton();
            }

            return;
        }

        if(testStr.length() > 0 && m.matches()) {

            super.insertString(fb, offset, string, attr);
            parentIconLabel.setIcon(inputValidIcon);
            if(attributeName.equals("") == false) {

                XTrackerXmlDocumentRowAttribute rowAttribute = row.getAttribute(attributeName);
                rowAttribute.setAttributeValue(testStr);

            } else {

                row.setTagValue(testStr);
            }

        } else {

            super.insertString(fb, offset, string, attr);
            parentIconLabel.setIcon(inputInvalidIcon);
            if(attributeName.equals("") == false) {

                XTrackerXmlDocumentRowAttribute rowAttribute = row.getAttribute(attributeName);
          //      rowAttribute.resetTagValue();
                rowAttribute.setAttributeValue(testStr);

            } else {

          //      row.resetTagValue();
                row.setTagValue(testStr);
            }
        }

        if(parentDisplayPanel instanceof XTrackerPluginParamsDisplayPanel) {

            ((XTrackerPluginParamsDisplayPanel)parentDisplayPanel).updatePluginParamsGuiSaveButton();
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {

        String testStr = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
        if(length > 0) {
            
            super.remove(fb, offset, length);
        }
        parentIconLabel.setIcon(inputInvalidIcon);
        insertString(fb, offset, string, attr);
    }

  /*  public void remove(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {

        String newStr = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
        System.out.println("remove " + newStr);
        if (length > 0) super.remove(fb, offset, length);
        System.out.println("remove");
     //   insertString(fb, offset, string, attr);
    } */
}
