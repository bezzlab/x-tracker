package xtracker;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


public class myTable extends javax.swing.JFrame {

    /**
     * The constructor
     */

    public myTable(xQuant InputData, boolean normV){
    this.normVal=normV;
     data=InputData;
     model = new DefaultTableModel(){

    @Override
    public boolean isCellEditable(int row, int column)
 {
     return false;
 }
  };



    FileNames=new Vector<String>();
    String[] labels=data.getElementAtIndex(0).getAllLabels();
    int dataSize=InputData.getQuantificationDataSize();
        for(int dtaCnt=0;dtaCnt<dataSize;dtaCnt++){
            xQuantData myQntData=InputData.getElementAtIndex(dtaCnt);
            System.out.println("Results of dataset " + myQntData.getFileName());
            FileNames.addElement(myQntData.getFileName());

        }
         xQuantData myQntData=InputData.getElementAtIndex(0);
            //Remember that combinations of proteinId, peptideSeq are unique!
            int pepInd=myQntData.getQuantitativeDataSize();
            int condInd=myQntData.getLabelsSize();
            String outString="";
            quantifData=new Object[pepInd][2*condInd+2];

         
            
            System.out.print("\n");
            //If the control condition is invalid stop the execution else go on.
            

            for(int i=0;i < pepInd;i++){
                    xQuantities myXqnt= myQntData.getQuantitativeDataElemAt(i);
                    quantifData[i][0]=myXqnt.getProteinId();
                    quantifData[i][1]=myXqnt.getPeptideSeq();
                    outString=myXqnt.getProteinId() +" (" +myXqnt.getPeptideSeq() + ")";
                    //System.out.print(InputData.peptideIds[i] +" (" +InputData.peptideSeqs[j] + ")");
                    float sumVals=0f;
                    float tmpVals[] = new float[condInd];
                    float tmpValsErr[] = new float[condInd];
                    for(int k=0;k<condInd;k++){

                            float qntity = 0f;


                                qntity=myXqnt.getQuantityAt(k);
                                outString+="\t" + qntity;
                                tmpVals[k]=qntity;
                                tmpValsErr[k]=myXqnt.getQuantErrorAt(k);



                            sumVals+=qntity;
//                          System.out.print("\t" + InputData.getQuantity(InputData.peptideIds[i], InputData.peptideSeqs[j], InputData.conditions[k]));

                    }
                    if(sumVals>0){
                         for(int k=0;k<condInd;k++){
                             if(!normVal){
                                 sumVals=1;
                             }
                       
                         if(1000*tmpVals[k]/sumVals>Integer.MAX_VALUE){
                                 
                                double rounded=Math.round((1000*Float.valueOf(tmpVals[k]).doubleValue()/sumVals));
                                double roundedErr=Math.round(1000*tmpValsErr[k]);
                                quantifData[i][2*k+2]= Double.valueOf(rounded).toString();
                                quantifData[i][2*k+3]= Double.valueOf(roundedErr/1000).toString();
                                
                               }
                               else{
                                 
                               double rounded=Math.round(1000*tmpVals[k]/sumVals);
                               double roundedErr=Math.round(1000*tmpValsErr[k]);

                               quantifData[i][2*k+2]= String.valueOf(rounded/1000);
                               quantifData[i][2*k+3]= Double.valueOf(roundedErr/1000).toString();
                              
                               }
                         }
                        System.out.print(outString);
                        System.out.print("\n");
                    }else{
                        for(int k=0;k<condInd;k++){
                            quantifData[i][2*k+2]="NaN";
                            quantifData[i][2*k+3]="NaN";
                         }

                    }


            }
        

   // Add Panels
//    Container contentPane = getContentPane();
//   contentPane.add(titleLab=new JLabel());


  panel=new JPanel(new GridBagLayout());
  GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0.0;
        c.ipady = 10;// long element
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL; // take all the horizontal place
        c.anchor = GridBagConstraints.PAGE_START; //stay at the left side
        c.gridx = 0;
        c.gridy = 0;

  jCombo = new JComboBox();
  for(int g=0;g<FileNames.size();g++){
    jCombo.addItem(FileNames.elementAt(g));
  }
   titleLab =new JLabel();
   if(normVal){
       titleLab.setText("  Normalised Quantitation Results\n\n");
   }
   else{
          titleLab.setText("  Quantitation Results (not normalised)\n\n");
   }
   titleLab.setFont(new Font("SansSerif", Font.BOLD, 14));
   titleLab.setForeground(Color.RED);
   titleLab.setVisible(true);
panel.add(titleLab, c);
   //panel.add(titleLab,BorderLayout.NORTH);
   //panel.add(jCombo);
       c.weightx = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL; // take all the horizontal place
        c.anchor = GridBagConstraints.PAGE_START; //stay at the left side
        c.gridx = 0;
        c.gridy = 1;
   panel.add(jCombo, c);
  


  
    String[] columnNames = new String[2*labels.length+2];
    columnNames[0]= "Prot. ID";
    columnNames[1]= "Pep. Sequence";
    for(int h=0;h<labels.length;h++){
    columnNames[2*h+2]=labels[h];
    columnNames[2*h+3]=labels[h] + " Err.";
   }

ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource()==jCombo){
         
            refreshTable(data, jCombo.getSelectedIndex());

        }
      }
    };

jCombo.addActionListener(actionListener);




for(int jj=0;jj<columnNames.length;jj++){
    model.addColumn(columnNames[jj]);
}
for(int jj=0;jj<quantifData.length;jj++){
    model.addRow(quantifData[jj]);
    
}

table = new JTable(model);
table.setPreferredScrollableViewportSize(new Dimension(500, 580));
table.setShowHorizontalLines(true);
table.setShowVerticalLines(true);

 //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //Add the scroll pane to this panel.
  //      add(titleLab);
    //    add(scrollPane,-1);
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 20;      //make this component tall
        c.weightx = 0.5;
        c.weighty = 1.0;
        // c.gridwidth = 3;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 2;
    panel.add(scrollPane, c);
    //   panel.add(scrollPane,BorderLayout.SOUTH);
add(panel);

    }

    public void refreshTable(xQuant InputData,int index){
               String control_condition="Heavy";
               FileNames=new Vector<String>();

              



            xQuantData myQntData=InputData.getElementAtIndex(index);
            System.out.println("Results of dataset " + myQntData.getFileName());
            FileNames.addElement(myQntData.getFileName());
            //Remember that combinations of proteinId, peptideSeq are unique!
            int pepInd=myQntData.getQuantitativeDataSize();
            int condInd=myQntData.getLabelsSize();
            String outString="";
            quantifData=new Object[pepInd][2*condInd+2];

         

            for(int i=0;i < pepInd;i++){
                    xQuantities myXqnt= myQntData.getQuantitativeDataElemAt(i);
                    quantifData[i][0]=myXqnt.getProteinId();
                    quantifData[i][1]=myXqnt.getPeptideSeq();
                    outString=myXqnt.getProteinId() +" (" +myXqnt.getPeptideSeq() + ")";
                    //System.out.print(InputData.peptideIds[i] +" (" +InputData.peptideSeqs[j] + ")");
                    float sumVals=0f;
                    float tmpVals[] = new float[condInd];
                    float tmpValsErr[] = new float[condInd];
                    for(int k=0;k<condInd;k++){

                            float qntity = 0f;


                                qntity=myXqnt.getQuantityAt(k);
                                outString+="\t" + qntity;
                                tmpVals[k]=qntity;
                                tmpValsErr[k]=myXqnt.getQuantErrorAt(k);



                            sumVals+=qntity;
//                          System.out.print("\t" + InputData.getQuantity(InputData.peptideIds[i], InputData.peptideSeqs[j], InputData.conditions[k]));

                    }
                    if(sumVals>0){
                         for(int k=0;k<condInd;k++){
                             if(!normVal){
                                 sumVals=1;
                             }

                         if(1000*tmpVals[k]/sumVals>Integer.MAX_VALUE){

                                double rounded=Math.round((1000*Float.valueOf(tmpVals[k]).doubleValue()/sumVals));
                                double roundedErr=Math.round(1000*tmpValsErr[k]);
                                quantifData[i][2*k+2]= Double.valueOf(rounded).toString();
                                quantifData[i][2*k+3]= Double.valueOf(roundedErr/1000).toString();
                                
                               }
                               else{

                               double rounded=Math.round(1000*tmpVals[k]/sumVals);
                               double roundedErr=Math.round(1000*tmpValsErr[k]);

                               quantifData[i][2*k+2]= String.valueOf(rounded/1000);
                               quantifData[i][2*k+3]= Double.valueOf(roundedErr/1000).toString();
                            
                               }
                         }
                        System.out.print(outString);
                        System.out.print("\n");
                    }else{
                        for(int k=0;k<condInd;k++){
                            quantifData[i][2*k+2]="NaN";
                            quantifData[i][2*k+3]="NaN";
                         }

                    }


            }
      

model.setRowCount(0);
for(int jj=0;jj<quantifData.length;jj++){
  model.addRow(quantifData[jj]);
    //  for(int kkk=0;kkk<quantifData[jj].length;kkk++){
   //     model.setValueAt(quantifData[jj][kkk], jj, kkk);


     //   }
    }
    }
    public boolean normVal=false;
    Vector<String> FileNames;
    final JTable table;
    private JPanel panel;
	DefaultTableModel model;
	JLabel titleLab;
    private JComboBox jCombo;
    xQuant data;
    Object[][] quantifData;
}