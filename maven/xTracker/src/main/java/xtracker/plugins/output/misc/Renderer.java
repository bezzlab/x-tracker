

package xtracker.plugins.output.misc;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Class that directes the display of a pressed button in a Jtable
 * 
 * @author laurie Tonon for X-Tracker
 */

public class Renderer extends JButton implements TableCellRenderer {

  /**
   * Constructor
   */
  public Renderer() {
    setOpaque(false);
    setBackground(Color.WHITE);
    setBorderPainted(false);
  }
  
  /**
   * Method that change a button when it pressed
   * @param table the table where the button is
   * @param value The value of the button if any
   * @param isSelected boolean to know if the button is selected
   * @param hasFocus boolean to know if the button has the focus
   * @param row  row of the button in the table
   * @param column column of the button in the table
   * @return The component button
   */
  public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(Color.BLUE);
      setBackground(Color.BLUE);
    } else{
      setForeground(table.getForeground());
      setBackground(UIManager.getColor("Button.background"));
    }
    setText( (value ==null) ? "" : value.toString() );
    return this;
  }
}

