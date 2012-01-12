
package xtracker.plugins.output.misc;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import xtracker.data.xQuant;
import xtracker.data.xQuantities;

/**
 * Class that is used to transform a column of a JTable in buttons.
 * It manages the display of the button as well as its actions.
 * When a button is pressed, a frame is open with a list of peptides
 * from the selected protein, retrieved into xQuant
 * @author laurie Tonon for X-Tracker
 */

public class Editor extends DefaultCellEditor {

    protected JButton button;
    private String label;
    private boolean isPushed;
    private xQuant data; // the xQuant data with all the quantification results, to retrieve the peptides

    /**
     * Constructor
     * @param checkBox a checkbox to respect the heritage
     * @param input an xQuant structure
     */
    public Editor(JCheckBox checkBox, xQuant input) {
        super(checkBox);
        data = input;

        //create the button and add its listener
        button = new JButton();
        button.setOpaque(false);
        button.setBackground(Color.WHITE);
        button.setBorderPainted(true);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                showFrame(button.getText());

            }
        });
    }

    /**
     * Method that is used to change the button display
     * @param table the Jtable where the button is
     * @param value the value of the button if any
     * @param isSelected a boolean to know if the button is selected
     * @param row the row of the button in the table
     * @param column the column of the button in the table
     * @return The component button
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(Color.BLUE);
            button.setBackground(Color.BLUE);
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        button.setBackground(Color.WHITE);
        button.setBorderPainted(true);
        isPushed = true;
        return button;
    }

    /**
     * Method to get the value of the selected cell. Used to create the button
     * @return the value of the selected cell of a JTable
     */
    public Object getCellEditorValue() {

        return new String(label);
    }

    /**
     * Event called when a button is pressed; Open a frame with the list of peptides
     * @param text the label of the button (i.e: the id of a protein)
     */
    public void showFrame(String text) {
        // create a new frame
        final JFrame frame = new JFrame();

        // set frame parameters
        frame.setSize(400, 350);
        frame.setBackground(Color.white);
        frame.setTitle("list of peptides");

        // set a gridbaglayout to the frame
        frame.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // create labels to explain how to have the modifications
        
        JLabel color = new JLabel("Peptides with modifications appear in blue.");
        color.setForeground(Color.red);
        color.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        c.weightx = 0.0;
        c.ipady = 10;
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(color, c);

        
        JLabel tip = new JLabel("Put the mouse on a peptide sequence to see modifications");
        tip.setForeground(Color.red);
        tip.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        c.weightx = 0.0;
        c.ipady = 10;
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(tip, c);

        // get the list of peptides for the selected protein
        ArrayList<String[]> pept = listPeptides(text);

        // create a panel with a gridbaglayout to contain the peptides sequences
        
        
        JPanel panel=new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints d = new GridBagConstraints();

        


        // add a label for each peptide in the panel
        for (int r = 0; r < pept.size(); r++) {

            JLabel lab = new JLabel(pept.get(r)[0]);
            String modif = pept.get(r)[1];
            lab.setToolTipText(modif);
            if (!(modif.equalsIgnoreCase("no modifications"))) {
                lab.setForeground(Color.BLUE);
            }
            d.weightx = 0.0;
            d.fill = GridBagConstraints.HORIZONTAL;
            d.anchor = GridBagConstraints.PAGE_START;
            d.gridx = 0;
            d.gridy = r;
            
            panel.add(lab,d);


        }

        // create a panel with scroll bars
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createTitledBorder("peptides of protein " + text));
        scroll.setSize(350, 250);
        c.fill = GridBagConstraints.BOTH;
        c.anchor=GridBagConstraints.CENTER;
        c.ipady = 20;      //make this component tall
        c.ipadx=20;
        c.weightx = 0.5;
        c.weighty = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 2;
        frame.getContentPane().add(scroll, c);

        // create a close button with its constraints
        JButton but = new JButton("close");
        but.setForeground(Color.red);
        but.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
        });

        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipady =0;     
        c.ipadx=0;
        c.insets = new Insets(0, 0, 5, 5);
        c.fill = GridBagConstraints.FIRST_LINE_END;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.gridx = 0;
        c.gridy = 3;
        frame.getContentPane().add(but, c);



        // set the frame visible
        frame.setVisible(true);



    }

    /**
     * Method that counts the number of peptide for a protein
     * @param protein The id of the protein
     * @return An array with two dimensions, with for each peptide its sequence and its modifications
     */
    public ArrayList<String[]> listPeptides(String protein) {
        
        ArrayList<String[]> listPept = new ArrayList<String[]>();


        // read xQuant
        if (data.getQuantificationDataSize() > 0) {
            for (int i = 0; i < data.getQuantificationDataSize(); i++) {

                if (data.getElementAtIndex(i).getQuantitativeDataSize() > 0) {

                    // create an array with the sequence and modifs of the peptides
                    
                    for (int j = 0; j < data.getElementAtIndex(i).getQuantitativeDataSize(); j++) {

                        xQuantities quant = data.getElementAtIndex(i).getQuantitativeDataElemAt(j);

                        // We find the protein of interest
                        if (quant.getProteinId().equalsIgnoreCase(protein)) {

                            // get the sequence
                            String sequence = quant.getPeptideSeq();
                            String modif = "";

                            // get the modifications
                            for (int m = 0; m < quant.getModificationSize(); m++) {
                                modif += quant.getModificationAtIndex(m) + " position n°: " + quant.getModPositionAtIndex(m);
                                modif += "; ";
                            }

                            String[] list = new String[2];
                            list[0] = sequence;
                            
                            if (modif.equalsIgnoreCase("")) {
                                list[1] = "no modifications";
                            } else {
                                list[1] = modif;
                            }

                            listPept.add(list);
                        }
                    }
                }

            }
        }


        return listPept;
    }
}

