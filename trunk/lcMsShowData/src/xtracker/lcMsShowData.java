package xtracker;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.util.Vector;

import java.io.*;






import java.io.File;



public class lcMsShowData implements peakSelPlugin
{
    /**
     * Works from MS/MS data loaded into xLoad structure. 
     * It reads 
     * @param input
     * @return
     */
    public xPeaks start(xLoad input, String paramFile)
    {
        xPeaks ret = new xPeaks();

        int i=0;
        Vector<Float> RT= new Vector<Float>();
        Vector<Float> SIC = new Vector<Float>();
        Vector<Float> XIC = new Vector<Float>();
        Vector<Float> XIC_RT = new Vector<Float>();
        Vector<Float> TIC = new Vector<Float>();
        Vector<Float> Ms_mz = new Vector<Float>();
        Vector<Float> Ms_int = new Vector<Float>();

        Vector<Float> Ms_subset_mz = new Vector<Float>();
        Vector<Float> Ms_subset_int = new Vector<Float>();

        Vector<Float> smoothXIC = new Vector<Float>();

        Vector<Float> smooth7XIC = new Vector<Float>();

        float minMz=2000.0f;
        float maxMz=0f;
        this.loadParams(paramFile);
        //System.out.print("Parameters: " + SIC_mz + " " + MS_spec_RT + " " + XIC_initMz + " " + XIC_endMz);
        int dataSize=input.getDataSize();
        if(dataSize>1){
            System.out.println("Sorry at the moment only single files are supported!");
            System.exit(1);
        }
        for(i=0;i<dataSize;i++){
            xLoadData loadedData = input.getDataElemAt(i);
            System.out.println("Reading file " + loadedData.getFileName());
            int lcMsSize=loadedData.getLcMsDataSize();
            minMz=2000.0f;
            maxMz=0f;
            for(int j=0;j<lcMsSize;j++){
                xLcMsData myLcMsData=loadedData.getLcMsDataElemAt(j);
                float retTime=myLcMsData.getRetTime();
                RT.addElement(retTime);
                xSpectrum msSpec=myLcMsData.getSpectrum();
                float peakList[][]=msSpec.getSpectrum();
                float intSumTIC=0;
                float intSumXIC=0;
                float sicElem=0;
                 for(int k=0;k<peakList.length; k++){
                        float mzVal=peakList[k][0];
                        float intVal=peakList[k][1];
                        
                        if(minMz>mzVal){
                            minMz=mzVal;
                        }
                        if(maxMz<mzVal){
                            maxMz=mzVal;
                        }

                        //MS
                        if(retTime>= this.MS_spec_RT && retTime<= this.MS_spec_RT){
                            Ms_mz.addElement(mzVal);
                            Ms_int.addElement(intVal);
                        }
                        //Ms_subset
                        if(retTime>= this.Ms_subset_initRT && retTime<= this.Ms_subset_endRT && mzVal>= this.Ms_subset_initMz && mzVal<= this.Ms_subset_endMz){
                            Ms_subset_mz.addElement(mzVal);
                            Ms_subset_int.addElement(intVal);
                        }

                        //SIC considers 
                        if(mzVal==SIC_mz){
                            sicElem=intVal;
                        }
                        //XIC
                        if(retTime>=this.XIC_initRT && retTime<=this.XIC_endRT){
                            if((mzVal>=this.XIC_initMz)&&(mzVal<=this.XIC_endMz)){
                                intSumXIC+=intVal;
                                System.out.println("Values (rt,mz,int):" + retTime + ":" + mzVal + ":" + intVal);
                            }
                        }
                        //TIC
                        intSumTIC+=intVal;
                        
                }
                if(intSumXIC>0){
                    XIC.addElement(intSumXIC);
                    smoothXIC.addElement(intSumXIC);
                    smooth7XIC.addElement(intSumXIC);
                    XIC_RT.addElement(retTime);
                }
                TIC.addElement(intSumTIC);
                SIC.addElement(sicElem);    
            }
        
        }




        //triangular 3 points
        for(int p=1;p<XIC.size()-1;p++){
            float val= (XIC.elementAt(p-1)+ XIC.elementAt(p) + XIC.elementAt(p+1))/3;
            smoothXIC.setElementAt(val, p);
        }


         //triangular 7 points
        for(int p=3;p<XIC.size()-3;p++){
            float val= (XIC.elementAt(p-3)+2*XIC.elementAt(p-2)+3*XIC.elementAt(p-1)+ 4*XIC.elementAt(p) + 3*XIC.elementAt(p+1)+2*XIC.elementAt(p+2)+XIC.elementAt(p+3))/16;
            smooth7XIC.setElementAt(val, p);
        }

                chart j = new chart(XIC_RT,XIC,"Extracted Ion Chromatogram (XIC)");
		//j.setTitle("First chart...");
		j.setSize(640, 430);
		j.setChar("XIC mass range:" + this.XIC_initMz + "-" + this.XIC_endMz, "Retention Time (s) " + this.XIC_initRT + " - " + this.XIC_endRT, "Sum of Intensities");
		
                float myArea=computeArea(XIC_RT,XIC);
                
		j.Show("Extracted Ion Chromatogram (area: " + myArea + ")");
                
                chart j1 = new chart(RT,TIC,"Total Ion Chromatogram (TIC)");
		//j.setTitle("First chart...");
		j1.setSize(640, 430);
		j1.setChar("TIC", "Retention Time (s)", "Sum of Intensities");
		
		j1.Show("Total Ion Chromatogram");                
        
                 chart j2 = new chart(RT,SIC,"Single Ion Chromatogram (SIC)");
		//j.setTitle("First chart...");
		j2.setSize(640, 430);
		j2.setChar("SIC reference Mz:" + this.SIC_mz, "Retention Time (s)", "Intensity");
		
		j2.Show("Single Ion Chromatogram");
                
                chart j3 = new chart(Ms_mz,Ms_int,"MS1 Spectrum");
		//j.setTitle("First chart...");
		j3.setSize(640, 430);
		j3.setChar("MS spectrum at Ret. Time:" + this.MS_spec_RT, "M/Z", "Intensity");
		
		j3.Show("MS1 Spectrum");


                chart j4 = new chart(Ms_subset_mz,Ms_subset_int,"MS1 Sub-spectrum");
		//j.setTitle("First chart...");
		j4.setSize(640, 430);
		j4.setChar("in Ret. Times range (s) [" + this.Ms_subset_initRT + "," + this.Ms_subset_endRT +"]", "M/Z", "Intensity");

		j4.Show("MS1 Sub-Spectrum mz range ["+ this.Ms_subset_initMz + "," + this.Ms_subset_endMz +"]");

        System.out.println("Min mz:" + minMz);
        System.out.println("Max mz:" + maxMz);



        chart jj = new chart(XIC_RT,smoothXIC,"Extracted Ion Chromatogram (XIC) smoothed");
        jj.setSize(640, 430);
		jj.setChar("Smoothed XIC mass range:" + this.XIC_initMz + "-" + this.XIC_endMz, "Retention Time (s) " + this.XIC_initRT + " - " + this.XIC_endRT, "Sum of Intensities");

                myArea=computeArea(XIC_RT,smoothXIC);

		jj.Show("Extracted Ion Chromatogram (area: " + myArea + ")");


        chart jjj = new chart(XIC_RT,smooth7XIC,"Extracted Ion Chromatogram (XIC) smoothed 7pts");
        jjj.setSize(640, 430);
		jjj.setChar("Smoothed XIC mass range:" + this.XIC_initMz + "-" + this.XIC_endMz, "Retention Time (s) " + this.XIC_initRT + " - " + this.XIC_endRT, "Sum of Intensities");

                myArea=computeArea(XIC_RT,smooth7XIC);

		jjj.Show("Extracted Ion Chromatogram (area: " + myArea + ")");

     //   for(i=0;i<RT.size();i++){
    //        System.out.print(RT.elementAt(i) + " ");

       // }

       Vector<Float> x= new Vector<Float>();
       Vector<Float> y= new Vector<Float>();

       float a = Math.round(Double.valueOf(Math.random()*10).floatValue()*100)/100.0f;
       float b= Math.round(Double.valueOf(Math.random()*10).floatValue()*100)/100.0f;

       System.out.println("Line to estimate: y=" + a +"x + " +b );

       for(int ij=0; ij<12; ij++){
            float tmp=Double.valueOf(Math.random()*100).floatValue();
            float plus=Double.valueOf(Math.random()*100).floatValue();
            float minus=Double.valueOf(Math.random()*100).floatValue();
            float tmp1= a*tmp+b  +plus - minus;
            if(tmp>0 && tmp1>0){
            x.addElement(tmp);
            
            y.addElement(a*tmp+b  +plus - minus);
            }
            else{
                ij--;
            }
       }

       double [] vals= linearFit(x,y);

       System.out.println("X:");
       for(int ik=0;ik<x.size();ik++){
        System.out.println(x.elementAt(ik)) ;
       }
       System.out.println("Y:");
       for(int ik=0;ik<x.size();ik++){
        System.out.println(y.elementAt(ik)) ;
       }

       System.out.println("Estimated params: y=" + vals[0] +"x + " +vals[1] + " variance a:" + vals[2] + " variance b:" +vals[3]);
                
        /*
        
        System.out.println("---------- Data to plot ----------------");
        
        //Printing MS Spectrum
        System.out.print("MS_mz=[");
        for(i=0;i<Ms_mz.size();i++){
            System.out.print(Ms_mz.elementAt(i) + " ");
        
        }
        System.out.print("];");
        
        System.out.println("");
        
        System.out.print("MS_int=[");
        for(i=0;i<Ms_int.size();i++){
            System.out.print(Ms_int.elementAt(i) + " ");
        
        }
        System.out.print("];");
        System.out.println("");
        System.out.println("");
        
        //Printing TIC
        System.out.print("TIC_RT=[");
        for(i=0;i<RT.size();i++){
            System.out.print(RT.elementAt(i) + " ");
        
        }
        System.out.print("];");
        System.out.println("");
        
        System.out.print("TIC_SumInt=[");
        for(i=0;i<TIC.size();i++){
            System.out.print(TIC.elementAt(i) + " ");
        
        }
        System.out.print("];");
        
        
        System.out.println("");
        System.out.println("");
        
        //Printing SIC
        System.out.print("SIC_RT=[");
        for(i=0;i<RT.size();i++){
            System.out.print(RT.elementAt(i) + " ");
        
        }
        System.out.print("];");
        System.out.println("");
        
        System.out.print("SIC_SumInt=[");
        for(i=0;i<SIC.size();i++){
            System.out.print(SIC.elementAt(i) + " ");
        
        }
        System.out.print("];");
        
        
        System.out.println("");
        System.out.println("");
        
        //Printing XIC
        System.out.print("XIC_RT=[");
        for(i=0;i<RT.size();i++){
            System.out.print(RT.elementAt(i) + " ");
        
        }
        System.out.print("];");

        System.out.println("");
        
        System.out.print("XIC_SumInt=[");
        for(i=0;i<XIC.size();i++){
            System.out.print(XIC.elementAt(i) + " ");
        
        }
        System.out.print("];");
        System.out.println("");
   */
        
       System.out.println("Continue? (Y/N)");
        String cont="";
        while(cont.equals("")){
            BufferedReader keyb = new BufferedReader(new InputStreamReader(System.in));
             try{
                cont = keyb.readLine();
            }
            catch(Exception e){
            System.out.println("Error: Input problems!");
            System.exit(1);
        }        
                
        }
         return ret;
    
   }
    

    
        /**
     * Opens the dataFile xml file and loads mass shifts,the mz_tolerance (in Daltons) and the RT window size(in seconds).
     * @param dataFile
     * @return ret an array of float mass shifts.
     */
    
    public void loadParams(String dataFile){
       
        File file = new File(dataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try{ 
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node nodeLst = doc.getElementsByTagName("param").item(0);
           
            NodeList itemList=  nodeLst.getChildNodes();
            for(int i=0; i<itemList.getLength(); i++){
                Node item= itemList.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE){ 
                   if(item.getNodeName().equals("SIC_mz")){
                        SIC_mz=Float.valueOf(item.getTextContent()).floatValue();
                        
                   }
                   else if(item.getNodeName().equals("MS_spec_RT")){
                                        MS_spec_RT=Float.valueOf(item.getTextContent()).floatValue();
                          
                   }
                   else if(item.getNodeName().equals("XIC_initMz")){ 
                                   XIC_initMz=Float.valueOf(item.getTextContent()).floatValue();     
                   }
                   else if(item.getNodeName().equals("XIC_endMz")){
                            XIC_endMz=Float.valueOf(item.getTextContent()).floatValue();
                   
                   }                   
                   else if(item.getNodeName().equals("XIC_initRT")){ 
                            XIC_initRT=Float.valueOf(item.getTextContent()).floatValue();     
                   }
                   else if(item.getNodeName().equals("XIC_endRT")){
                            XIC_endRT=Float.valueOf(item.getTextContent()).floatValue();
                   
                   }
                   else if(item.getNodeName().equals("Ms_subset_initMZ")){
                            Ms_subset_initMz=Float.valueOf(item.getTextContent()).floatValue();
                   }
                   else if(item.getNodeName().equals("Ms_subset_endMZ")){
                            Ms_subset_endMz=Float.valueOf(item.getTextContent()).floatValue();

                   }
                   else if(item.getNodeName().equals("Ms_subset_initRT")){
                            Ms_subset_initRT=Float.valueOf(item.getTextContent()).floatValue();
                   }
                   else if(item.getNodeName().equals("Ms_subset_endRT")){
                            Ms_subset_endRT=Float.valueOf(item.getTextContent()).floatValue();

                   }

                }
            }
            
            
        }
        catch(Exception e){
            System.out.println("Exception while reading " + dataFile+ "\n" + e);
            System.exit(1);
        }
        

    }
    
    
    
    public boolean stop()
    {
        System.out.println(getName()+": stopping...");
        return true;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public String getType()
    {

        return type;
    }
    
        public String getDescription()
    {

        return description;
    }
        
        public float computeArea (Vector<Float> RT, Vector<Float> IC){
        
        float ret=0;
        float tmp_val=0;
        int size=RT.size();
        for(int i=0;i<size-1;i++){
                tmp_val=(IC.elementAt(i)+IC.elementAt(i+1))*(RT.elementAt(i+1)-RT.elementAt(i))/2;
                ret+=tmp_val;
        }
        
        return ret;
    }

    /**
     * Computes a linear fitting on input data x and y. No error is assumed on x nor y. It fits the curve Y=aX+b
     * to the coordinates in input x and y.
     * @param x xcoordinates to fit
     * @param y ycoordinates to fit
     * @return ret is an array of double arranged as follows ret[0]=a ret[1]=b ret[2]=sigma_a ret[2]=sigma_b (sigmas being variances in the estimation of a and b).
     */
    public double[] linearFit(Vector<Float> x, Vector<Float> y){
        double[] ret=new double[4];
        for(int i=0;i<4;i++){
            ret[i]=0;
        }
        int N=x.size();
        float sxx=0.0f;
        float sxy=0.0f;
        float sx=0;
        float sy=0;
        float sxoss=0f;
        float del=0;
        float s=N;
  



        if(y.size() != N){
            System.out.println("ERROR: linearFit. Vectors size does not match. x'size:" + N + " y's size:" +y.size());
            System.exit(1);
        }
        else{
            for(int i=0;i<N;i++){
                sx  += x.elementAt(i);
                sy  += y.elementAt(i);
                sxx += x.elementAt(i)*x.elementAt(i);
                sxy += x.elementAt(i)*y.elementAt(i);

            }
        del = s*sxx - sx*sx;

          // Intercept
        ret[1] =  (sxx*sy -sx*sxy)/del;
        // Slope
        ret[0] =  (s*sxy -sx*sy)/del;


        // Errors  (std dev) on the:
        // intercept
        ret[2] = Math.sqrt(sxx/del);
        // and slope
        ret[3] = Math.sqrt(s/del);

      /*  for (int i=0;i<n;i++) {
            t=x.elementAt(i)-sxoss;
            st2=t*t;
            b += t*y.elementAt(i);
        }
        b=b/st2;
        a=(sy-sx*b)/ss;
        siga=Math.sqrt((1.0+sx*sx/(ss*st2))/ss);
        sigb=Math.sqrt(1.0/st2);
        ret[0]=a;
        ret[1]=b;
        ret[2]=siga;
        ret[3]=sigb;
       */

        }
        return ret;
    }
        
    private float SIC_mz=-1f;
    private float MS_spec_RT=-1f;
    private float XIC_initMz=-1f;
    private float XIC_endMz=-1f;
    private float XIC_initRT=-1f;
    private float XIC_endRT=-1f;
    private float Ms_subset_initMz=-1f;
    private float Ms_subset_endMz=-1f;
    private float Ms_subset_initRT=-1f;
    private float Ms_subset_endRT=-1f;


    private final static String name = "Debug Plugin";
    private final static String version = "0.0.2";
    private final static String type = "PEAKSEL_plugin";
    private final static String description = "Reads in LC/MS data and outputs it in a matlab friendly format. It does not populate data structures therefore after this plugin xtracker will stop execution.";
}