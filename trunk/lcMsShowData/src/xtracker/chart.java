package xtracker;


import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.data.xy.*;

import java.util.Vector;

public class chart extends JFrame
{
        private XYSeries series;
	private XYDataset dataset;
	private JFreeChart jfc;

	public chart(Vector<Float> x,Vector<Float> y, String title)
	{
		series = new XYSeries(title);
                int xSize=x.size();
                int ySize=y.size();
                if(xSize!= ySize){
                    System.out.println("Error: X and Y datasets have different dimensions!");
                    System.exit(1);
                }
                for(int i=0; i<xSize;i++){
                    addElement(x.elementAt(i).floatValue(), y.elementAt(i).floatValue());                
                }
                dataset = new XYSeriesCollection(series);
        }
	
	public void addElement(float x,float y)
	{
		series.add(x, y);
                     
                
	}
	
	public void setChar(String title,String xTitle, String yTitle)
	{
                jfc = ChartFactory.createXYLineChart(title, xTitle, yTitle, dataset, PlotOrientation.VERTICAL , true, true, true);
                jfc.setTitle(title);
            }
	
	private JPanel createPanel()
	{
		return new ChartPanel(jfc);
	}
	
	public void Show(String title)
	{       
                JPanel myJPanel=createPanel();
                setTitle(title);
		setContentPane(myJPanel);
		setVisible(true);
	}
	
}
