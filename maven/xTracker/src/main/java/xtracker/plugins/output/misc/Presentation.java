
package xtracker.plugins.output.misc;

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
import xtracker.data.xQuant;
import xtracker.data.xQuantData;
import xtracker.data.xQuantities;

/**
 * Class that creates an ouput window for the spectral counting APEX method.
 * The frame contains a list to select the file of which display the results,
 * and a table with the results.
 * This class uses a renderer and an editor class for the button,
 * in order to make a columns of buttons.
 * Each protein is a button that can open a window with the list of its peptides.
 *
 * @author laurie Tonon for X-Tracker
 */


public class Presentation extends JFrame implements ActionListener {

    private xQuant data; // the xQuant structure with the quantitation results
    private JTable DataTable;
    private JComboBox combo;
    private JLabel label;
    private JScrollPane scroller;
    private DefaultTableModel model; // a model for the table => allow modifications 

    /**
     * Constructor that creates the frame and fills it with the quantitiative results from xQuant
     * @param input the xQuant data structure
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
        String APEXAbs = "APEX Score abs";
        String fper="False positive error";

        columns.add(id);
        columns.add(APEXAbs);
        columns.add(fper);

        // read xQuant to create the rows of the table
        String[] files = new String[data.getQuantificationDataSize()];
        
        if (data.getQuantificationDataSize() > 0) {
        // put the files names in the list
            for (int d = 0; d < data.getQuantificationDataSize(); d++) {
                files[d] = data.getElementAtIndex(d).getFileName();
            }
         
            xQuantData quantData = data.getElementAtIndex(0);


            
            if (quantData.getQuantitativeDataSize() > 0) {

                
                // fill the first row for the first protein
                
                String firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();
                xQuantities quantity = quantData.getQuantitativeDataElemAt(0);
                Vector CurrentRow = new Vector();
                CurrentRow.addElement(quantity.getProteinId());
                double APEX = quantity.getQuantityAt(0);

                BigDecimal bd;
                BigDecimal bd2;

                if(APEX != -1){
                     bd = new BigDecimal(APEX);
                     bd2 = bd.setScale(5, BigDecimal.ROUND_DOWN);
                    APEX= bd2.doubleValue();

                    CurrentRow.addElement(APEX);
                }
                else{
                    CurrentRow.addElement("Nan");
                }
                //calculate the percentage of emPAI
                double err = quantity.getQuantErrorAt(0);

                BigDecimal bdp;
                BigDecimal bd2p;

                if(err != -1){
                    bdp = new BigDecimal(err);
                    bd2p = bdp.setScale(5, BigDecimal.ROUND_DOWN);
                    err = bd2p.doubleValue();

                    CurrentRow.addElement(err);
                }
                else{
                    CurrentRow.addElement("Nan");
                }
                rows.addElement(CurrentRow);
                
                //create all the other rows
                for (int i = 0; i < quantData.getQuantitativeDataSize(); i++) {

                    //detect when it is not the same protein
                    if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {

                        quantity = quantData.getQuantitativeDataElemAt(i);
                        CurrentRow = new Vector();
                        CurrentRow.addElement(quantity.getProteinId());
                        APEX = quantity.getQuantityAt(0);

                        if(APEX != -1){
                            bd = new BigDecimal(APEX);
                            bd2 = bd.setScale(5, BigDecimal.ROUND_DOWN);
                            APEX = bd2.doubleValue();

                        CurrentRow.addElement(APEX);
                        }
                        else{
                            CurrentRow.addElement("Nan");
                         }

                        err = quantity.getQuantErrorAt(0);

                        if(err != -1){
                            bdp = new BigDecimal(err);
                            bd2p = bdp.setScale(5, BigDecimal.ROUND_DOWN);
                            err = bd2p.doubleValue();

                            CurrentRow.addElement(err);
                        }
                        else{
                            CurrentRow.addElement("Nan");
                        }
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
        label = new JLabel("APEX quantitative results");
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
        // c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 2;
        getContentPane().add(scroller, c);


        validate();

    }

    /**
     * Event called when a action is made on the combo box
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
        String APEXAbs = "APEX Score abs";
        String fper="False positive error";

        columns.add(id);
        columns.add(APEXAbs);
        columns.add(fper);

        if (data.getQuantificationDataSize() > 0) {
            xQuantData quantData = new xQuantData("file", 1); // fake xQuantData sinon  pas initialise

            for (int d = 0; d < data.getQuantificationDataSize(); d++) {
                if (data.getElementAtIndex(d).getFileName().equalsIgnoreCase(file)) {
                   
                    quantData = data.getElementAtIndex(d);
                }

            }


            if (quantData.getQuantitativeDataSize() > 0) {


                // create first row with first protein
                String firstProt = quantData.getQuantitativeDataElemAt(0).getProteinId();
                xQuantities quantity = quantData.getQuantitativeDataElemAt(0);
                Vector CurrentRow = new Vector();
                CurrentRow.addElement(quantity.getProteinId());
                double APEX = quantity.getQuantityAt(0);

                BigDecimal bd;
                BigDecimal bd2;

                if(APEX != -1){

                    bd = new BigDecimal(APEX);
                    bd2 = bd.setScale(5, BigDecimal.ROUND_DOWN);
                    APEX= bd2.doubleValue();

                    CurrentRow.addElement(APEX);
                }
                else{
                    CurrentRow.addElement("Nan");
                }
                double err = quantity.getQuantErrorAt(0);

                BigDecimal bdp;
                BigDecimal bd2p;

                if(err != -1){
                    bdp = new BigDecimal(err);
                    bd2p = bdp.setScale(5, BigDecimal.ROUND_DOWN);
                    err= bd2p.doubleValue();

                    CurrentRow.addElement(err);
                }
                else{
                    CurrentRow.addElement("Nan");
                }
                rows.addElement(CurrentRow);

                // create all other rows
                for (int i = 0; i < quantData.getQuantitativeDataSize(); i++) {

                    if (!(quantData.getQuantitativeDataElemAt(i).getProteinId().equalsIgnoreCase(firstProt))) {

                        quantity = quantData.getQuantitativeDataElemAt(i);
                        CurrentRow = new Vector();
                        CurrentRow.addElement(quantity.getProteinId());
                        APEX = quantity.getQuantityAt(0);

                        if(APEX != -1){
                            bd = new BigDecimal(APEX);
                            bd2 = bd.setScale(5, BigDecimal.ROUND_DOWN);
                            APEX = bd2.doubleValue();

                            CurrentRow.addElement(APEX);
                        }
                        else{
                            CurrentRow.addElement("Nan");
                        }
                        err = quantity.getQuantErrorAt(0);

                        if(err != -1){

                            bdp = new BigDecimal(err);
                            bd2p = bdp.setScale(5, BigDecimal.ROUND_DOWN);
                            err = bd2p.doubleValue();

                            CurrentRow.addElement(err);
                        }
                        else{
                            CurrentRow.addElement("Nan");
                        }

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
