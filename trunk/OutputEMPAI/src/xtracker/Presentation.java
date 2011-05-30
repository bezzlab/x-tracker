
package xtracker;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/*
 * Class that create an ouput window for the spectral counting method.
 * The frame contains a list to select the file of which display the results,
 * and a table with the results.
 * This class use the a renderer and an editor class for the button,
 * in order to make a columns of buttons.
 * Each protein is a button that can open a window with the list of its peptide
 *
 * @author laurie
 */
public class Presentation extends JFrame implements ActionListener {

    /**
     * the xQuant structure with the quantitation results
     */
    private xQuant data;
    /**
     * The table where the results are presented
     */
    private JTable DataTable;
    /**
     * A conbo box to select a file
     */
    private JComboBox combo;
    /**
     * A label as a title of the output frame
     */
    private JLabel label;
    /**
     * A scrollpane where to put the table
     */
    private JScrollPane scroller;
    /**
     *  a model for the table => allow modifications
     */
    private DefaultTableModel model; 

    /**
     * Creates a frame presenting the results on the screen
     * @param input The xQuant data structure where the results are read
     */
    public Presentation(xQuant input) {
        this.data = input;

        // set the frame properties
        setSize(500, 500);
        show();
        setTitle("results");

        // create the columns for the table
        Vector columns = new Vector();
        Vector rows = new Vector();

        String id = "Prot id";
        String empai = "emPAI";
        String percent = "%mol EMPAI";

        columns.add(id);
        columns.add(empai);
        columns.add(percent);

        // read xQuant to create the rows of the table
        String[] files = new String[data.getQuantificationDataSize()];
        
        if (data.getQuantificationDataSize() > 0) {
        // put the files names in the list
            for (int d = 0; d < data.getQuantificationDataSize(); d++) {
                files[d] = data.getElementAtIndex(d).getFileName();
            }
 
            xQuantData quantData = data.getElementAtIndex(0);


            
            if (quantData.getQuantitativeDataSize() > 0) {

                double sumEMPAI = 0;
                String firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();
                // calculate the sum of emPAI for the percentage calculation
                sumEMPAI += quantData.getQuantitativeDataElemAt(0).getQuantityAt(0);

                for (int i = 1; i < quantData.getQuantitativeDataSize(); i++) {

                    if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {
                        sumEMPAI += quantData.getQuantitativeDataElemAt(i).getQuantityAt(0);
                        firstProt = quantData.getQuantitativeDataElemAt(i).getProteinId();
                    }
                }
                
                // fill the first row for the first protein
                
                firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();
                xQuantities quantity = quantData.getQuantitativeDataElemAt(0);
                Vector CurrentRow = new Vector();
                CurrentRow.addElement(quantity.getProteinId());
                double emPAI = quantity.getQuantityAt(0);

                BigDecimal bd = new BigDecimal(emPAI);
                BigDecimal bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
                emPAI = bd2.doubleValue();

                CurrentRow.addElement(emPAI);
                
                //calculate the percentage of emPAI
                double per = (emPAI / sumEMPAI) * 100;

                BigDecimal bdp = new BigDecimal(per);
                BigDecimal bd2p = bdp.setScale(3, BigDecimal.ROUND_DOWN);
                per = bd2p.doubleValue();

                CurrentRow.addElement(per);
                rows.addElement(CurrentRow);
                
                //create all the other rows
                for (int i = 0; i < quantData.getQuantitativeDataSize(); i++) {

                    //detect when it is not the same protein
                    if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {

                        quantity = quantData.getQuantitativeDataElemAt(i);
                        CurrentRow = new Vector();
                        CurrentRow.addElement(quantity.getProteinId());
                        emPAI = quantity.getQuantityAt(0);

                        bd = new BigDecimal(emPAI);
                        bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
                        emPAI = bd2.doubleValue();

                        CurrentRow.addElement(emPAI);

                        per = (emPAI / sumEMPAI) * 100;

                        bdp = new BigDecimal(per);
                        bd2p = bdp.setScale(3, BigDecimal.ROUND_DOWN);
                        per = bd2p.doubleValue();

                        CurrentRow.addElement(per);
                        rows.addElement(CurrentRow);

                        firstProt = quantData.getQuantitativeDataElemAt(i).getProteinId();
                    }
                }
            }


        }


        // Set the layout of the frame: gridbaglayout
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // create a label with its constraints
        label = new JLabel("Quantitative results (not normalised)");
        label.setForeground(Color.RED);
        label.setFont(new Font("Trebuchet MS", Font.BOLD, 14));
        c.weightx = 0.0;
        c.ipady = 10;// long element
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL; // take all the horizontal place
        c.anchor = GridBagConstraints.PAGE_START; //stay at the left side
        c.gridx = 0;
        c.gridy = 0;
        getContentPane().add(label, c);

        // create a combo box for the files and its constraints
        combo = new JComboBox(files);
        c.weightx = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL; // take all the horizontal place
        c.anchor = GridBagConstraints.PAGE_START; //stay at the left side
        c.gridx = 0;
        c.gridy = 1;
        getContentPane().add(combo, c);

        combo.addActionListener(this);

        // Create the table with the results
        model = new DefaultTableModel(rows, columns);
        DataTable = new JTable();
        DataTable.setModel(model);

        // make the first columns with buttons
        DataTable.getColumn("Prot id").setCellRenderer(new Renderer());
        DataTable.getColumn("Prot id").setCellEditor(new Editor(new JCheckBox(), data));

        scroller = new JScrollPane(DataTable);
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 20;      //make this component tall
        c.weightx = 0.5;
        c.weighty = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 2;
        getContentPane().add(scroller, c);


        validate();

    }

    /**
     * Event called when a action is made on the combo box.
     * Change the content of the JTable
     * @param ev an event
     */
    public void actionPerformed(ActionEvent ev) {

        // name of the selected file
        String file = combo.getSelectedItem().toString(); 
        
        // ****************************************
        // create a new table with info of new file
        // ****************************************
        
        
        Vector columns = new Vector();
        Vector rows = new Vector();

        String id = "Prot id";
        String empai = "emPAI";
        String percent = "%mol EMPAI";

        columns.add(id);
        columns.add(empai);
        columns.add(percent);

        if (data.getQuantificationDataSize() > 0) {
            xQuantData quantData = new xQuantData("file", 1); // fake xQuantData if not not initialised

            for (int d = 0; d < data.getQuantificationDataSize(); d++) {
                if (data.getElementAtIndex(d).getFileName().equalsIgnoreCase(file)) {
                   
                    quantData = data.getElementAtIndex(d);
                }

            }


            if (quantData.getQuantitativeDataSize() > 0) {
                // count sum of empai for percentages
                double sumEMPAI = 0;
                String firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();

                sumEMPAI += quantData.getQuantitativeDataElemAt(0).getQuantityAt(0);

                for (int i = 1; i < quantData.getQuantitativeDataSize(); i++) {

                    if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {
                        sumEMPAI += quantData.getQuantitativeDataElemAt(i).getQuantityAt(0);
                        firstProt = quantData.getQuantitativeDataElemAt(i).getProteinId();
                    }
                }
                
                // create first row with first protein
                firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();
                xQuantities quantity = quantData.getQuantitativeDataElemAt(0);
                Vector CurrentRow = new Vector();
                CurrentRow.addElement(quantity.getProteinId());
                double emPAI = quantity.getQuantityAt(0);

                BigDecimal bd = new BigDecimal(emPAI);
                BigDecimal bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
                emPAI = bd2.doubleValue();

                CurrentRow.addElement(emPAI);

                double per = (emPAI / sumEMPAI) * 100;

                BigDecimal bdp = new BigDecimal(per);
                BigDecimal bd2p = bdp.setScale(3, BigDecimal.ROUND_DOWN);
                per = bd2p.doubleValue();

                CurrentRow.addElement(per);
                rows.addElement(CurrentRow);

                // create all other rows
                for (int i = 0; i < quantData.getQuantitativeDataSize(); i++) {

                    if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {

                        quantity = quantData.getQuantitativeDataElemAt(i);
                        CurrentRow = new Vector();
                        CurrentRow.addElement(quantity.getProteinId());
                        emPAI = quantity.getQuantityAt(0);

                        bd = new BigDecimal(emPAI);
                        bd2 = bd.setScale(3, BigDecimal.ROUND_DOWN);
                        emPAI = bd2.doubleValue();

                        CurrentRow.addElement(emPAI);

                        per = (emPAI / sumEMPAI) * 100;

                        bdp = new BigDecimal(per);
                        bd2p = bdp.setScale(3, BigDecimal.ROUND_DOWN);
                        per = bd2p.doubleValue();

                        CurrentRow.addElement(per);
                        rows.addElement(CurrentRow);

                        firstProt = quantData.getQuantitativeDataElemAt(i).getProteinId();
                    }
                }
            }


        }
        
        // change the model of the table to display the changes
        model.setDataVector(rows, columns);
        model.fireTableDataChanged();
        // recall the classes to transform the first columns in buttons
        DataTable.getColumn("Prot id").setCellRenderer(new Renderer());
        DataTable.getColumn("Prot id").setCellEditor(new Editor(new JCheckBox(), data));

    }
}
