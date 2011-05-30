package xtracker;

import java.util.*;

public class linearFitQuant implements quantPlugin
{
    public xQuant start(xPeaks inputData, String paramFile)
    {

        //Let's retrieve how many files have been processed
        int size=inputData.getSize();
        int i=-1;
        xCorrespondences myFileCorr = null;
        //The output structure
        xQuant ret = new xQuant();

        //Let's process the peaks belonging to every raw data file
        for(i=0;i<size;i++){
            myFileCorr=inputData.getElemAt(i);
            //The raw data file name
            String fileName=myFileCorr.getFileName();
            //Let's get the set of unique labels we want to quantitate on.
            String [] labelsTmp= myFileCorr.getUniqueLabelsOfLcMsCorr();
            int labTmpSize=labelsTmp.length;
            String [] labels=new String[labTmpSize*(labTmpSize-1)];
            int cnt=0;
            int labSize=labels.length;
         
            for(int lt=0;lt<labTmpSize;lt++){
                System.out.println(lt);
                for(int lt2=lt+1;lt2<labTmpSize;lt2++){
                 System.out.println(" - " +lt2);
                    System.out.println(labelsTmp[lt] + "/" + labelsTmp[lt2]);
                    labels[cnt]=labelsTmp[lt] + "/" + labelsTmp[lt2];
                    cnt++;
                    labels[cnt]=labelsTmp[lt2] + "/" + labelsTmp[lt];
                    cnt++;
                }

            }

            int corrSize=myFileCorr.getPeptideCorrespondenceDataSize();
            
            xQuantData myQuantData=new xQuantData(fileName,labSize);


           
            myQuantData.setAllLabels(labels);

            //Let's process any correspondence
            for(int ii=0;ii<corrSize;ii++){
                xCorrespondenceData myCdata=myFileCorr.getPeptideCorrespondenceDataElemtAt(ii);
                //Let's retrieve the proteinId and peptide sequence
                String protId=myCdata.getProteinId();
                String pepSeq=myCdata.getPeptideSeq();
                //Vectors for modifications associated to the peak of this peptide.
                Vector<String> modNames= new Vector<String>();
                Vector<Integer> modPos= new Vector<Integer>();

                int lcMsSize=myCdata.getLcMsCorrSize();
                int indx=-1;
                Vector rts[]= new Vector[labSize];
                Vector ics[]= new Vector[labSize];
                Vector mzs[]= new Vector[labSize];

                //Let's loop now through all lcMs peaks stored for this couple proteinId, peptideSeq.
                //System.out.println("LCMS size:" +lcMsSize);
                for(int jj=0;jj<lcMsSize;jj++){
                    xLcMsCorr myLcMsCorr=myCdata.getLcMsCorrElemAt(jj);
                    String lbl=myLcMsCorr.getLabel();

                    boolean searching=true;
                    for(int k=0;k<labTmpSize && searching;k++){

                        if(labelsTmp[k].equals(lbl)){
                            searching=false;
                            indx=k;
                          

                        }

                    }
                  if(!searching){
                     if(rts[indx]==null){
                        //Let's create the rt vector and add the rt value of the peak to the vector corresponding to lbl experimental condition
                        rts[indx]= new Vector<Float>();
                        rts[indx].addElement(Float.valueOf(myLcMsCorr.getRT()));

                  
                        //Let's create the intensity vector and add the intensity value of the peak to the vector corresponding to lbl experimental condition
                        ics[indx]= new Vector<Float>();
                        ics[indx].addElement(Float.valueOf(myLcMsCorr.getIntensity()));

                        //Let's create the mz vector and add the mz value of the peak to the vector corresponding to lbl experimental condition
                        mzs[indx]= new Vector<Float>();
                        mzs[indx].addElement(Float.valueOf(myLcMsCorr.getMz()));
                     
                     }
                     else{
                        //We have to keep the vectors ordered by increasing RT!
                        float rtVal=myLcMsCorr.getRT();
                        int my_index=Collections.binarySearch(rts[indx], rtVal);  //findIndex(rts[indx],rtVal);
                      
                        

                        if(my_index<0){
                            my_index=0-my_index-1;
                        }
                        

                        //Let's add the rt value of the peak to the vector corresponding to lbl experimental condition
                        rts[indx].add(my_index,Float.valueOf(myLcMsCorr.getRT()));
                        //Let's add the intensity value of the peak to the vector corresponding to lbl experimental condition
                        ics[indx].add(my_index,Float.valueOf(myLcMsCorr.getIntensity()));
                        //Let's add the mz value of the peak to the vector corresponding to lbl experimental condition
                        mzs[indx].add(my_index,Float.valueOf(myLcMsCorr.getMz()));

                  

                     }


                  }
                  else{
                    System.out.println("ERROR (computeLcMsArea): Label ("+lbl+") not found!");
                    System.exit(1);
                  }


                 //Adding modifications (if any)
                 for(int modCnt=0;modCnt<myLcMsCorr.getModificationSize();modCnt++){
                    int pos=myLcMsCorr.getModPositionAtIndex(modCnt);
                    String modNM=myLcMsCorr.getModificationNameAtIndex(modCnt);
                    int posWhere=modPos.indexOf(pos);
                    int modWhere=modNames.indexOf(modNM);
                    if(posWhere==-1 || modWhere==-1 || posWhere!=modWhere){
                        modPos.addElement(pos);
                        modNames.addElement(modNM);


                    }
                 }

                }
                //Let's create a quantities data structure
                xQuantities quantElem= new xQuantities(protId,pepSeq, labSize);
                Vector [] quantsByLabel= new Vector[labSize];
                for(int kk=0; kk<labTmpSize;kk++){
                    System.out.println(labelsTmp[kk] +":");
                    float qty=0f;
                    
                    Vector<series> quantFitted=this.computeArea(rts[kk], mzs[kk],ics[kk]);
/*                    float rtTmpv=0f;

                    for(int www=0;www<rts[kk].size();www++){
                        float v=Float.valueOf(rts[kk].elementAt(www).toString()).floatValue();
                        if(Float.compare(v, rtTmpv)!=0){
                            System.out.println(www+"]  RT:" +rts[kk].elementAt(www));
                            System.out.println(mzs[kk].elementAt(www) + " " + ics[kk].elementAt(www));
                            rtTmpv=v;
                        }
                        else{
                            System.out.println(mzs[kk].elementAt(www) + " " + ics[kk].elementAt(www));
                        }
                       

                    } */

                    //Let's add the quantity in the right place
//                    quantElem.addQuantity(kk, qty);

                    quantsByLabel[kk]=quantFitted;


               }
                Vector<Float> lab1Vec = new Vector<Float>();
                Vector<Float> lab2Vec = new Vector<Float>();
                int dataCounter=0;

                for(int lab1=0;lab1<labTmpSize;lab1++){
                    System.out.println(lab1 +": (" + pepSeq +")");
                    lab1Vec.removeAllElements();
                    int sz=quantsByLabel[lab1].size();
                    Vector<series> lab1series=quantsByLabel[lab1];
                    /*      for(int lll=0;lll<sz;lll++){
                        Vector<series> aaa= quantsByLabel[lab1];

                        lab1Vec.add(aaa.elementAt(lll).getIC());

                    }
                 */
                    for(int lab2=lab1+1;lab2<labTmpSize;lab2++){
                        int sz2=quantsByLabel[lab2].size();
                        lab2Vec.removeAllElements();
                        Vector<series> lab2series= quantsByLabel[lab2];
                        for(int gh=0;gh<lab1series.size();gh++){
                            float rt1=lab1series.elementAt(gh).getRT();
                            boolean found=false;
                            for(int lll=0;lll<sz2 && found==false;lll++){
                                if(lab2series.elementAt(lll).getRT()== rt1){
                                    lab1Vec.add(lab1series.elementAt(gh).getIC());
                                    lab2Vec.add(lab2series.elementAt(lll).getIC());
                                    found=true;
                                }
                            }
                        }
                        
//                        int mx=Math.min(lab1Vec.size(), lab2Vec.size());
//                        lab1Vec.setSize(mx);
//                        lab2Vec.setSize(mx);
   /*                     System.out.println("Lab 1: size " +lab1Vec.size());
                        for(int kl=0;kl<lab1Vec.size();kl++){
                            System.out.println(lab1Vec.elementAt(kl));

                        }
                        System.out.println("Lab 2: size " + lab2Vec.size());
                        for(int kl=0;kl<lab2Vec.size();kl++){
                            System.out.println(lab2Vec.elementAt(kl));

                        }*/

                      /*  double coeff=Math.random()*10;
                        double inter=Math.random()*1000;
                        Vector<Float> X= new Vector<Float>();
                        Vector<Float> Y= new Vector<Float>();

                        for(int luca=0;luca<120;luca++){
                            double x=Math.random();
                            double y=x*coeff + inter +Math.random() - Math.random();
                            X.add(Double.valueOf(x).floatValue());
                            Y.add(Double.valueOf(y).floatValue());


                        }
                        System.out.println("VALUES X:");
                                                for(int luca=0;luca<120;luca++){
                                                    System.out.println(X.elementAt(luca));
                                                }
                        System.out.println("VALUES Y:");
                                                for(int luca=0;luca<120;luca++){
                                                    System.out.println(Y.elementAt(luca));
                                                }

                        double [] vls=linearFit(X,Y);
                        System.out.println("Real values: y="+coeff+"x + "+inter);
                        System.out.println("Estimated par: y=" + vls[0] + "x + "+vls[1] + " variances: " + vls[2] + " " + vls[3]);
                        System.exit(1);
                        */
                        double [] vals= linearFit(lab1Vec,lab2Vec);
                        quantElem.addQuantity(dataCounter, Double.valueOf(vals[0]).floatValue());
                        quantElem.addQuantError(dataCounter, Double.valueOf(vals[2]).floatValue());
                        dataCounter++;
                        quantElem.addQuantity(dataCounter, 1/Double.valueOf(vals[0]).floatValue());
                        quantElem.addQuantError(dataCounter, Double.valueOf(vals[2]).floatValue());
                        dataCounter++;

                        System.out.println(labels[dataCounter-2] + ":" + vals[0] + " variance " + vals[2] + " b:" + vals[1] + " variance:" + vals[3]);

                        vals= linearFit(lab2Vec,lab1Vec);
                        System.out.println(labels[dataCounter-2] + ":" + vals[0] + " variance " + vals[2] + " b:" + vals[1] + " variance:" + vals[3]);
                  //      System.exit(1);
                    }
               


                }
                 //Let's add modifications to the quantities data structure.
                for(int kk=0;kk<modPos.size();kk++){
                    quantElem.addModification(modNames.elementAt(kk), modPos.elementAt(kk));
                }
                //Let's add it to the xQuantData structure
               myQuantData.addQuantitativeDataElem(quantElem);
               // System.exit(1);
                
               }
            //Let's add all the quantitative information from the raw data file to the xQuant structure
            ret.addQuantificationDataElem(myQuantData);


        }




        return ret;
    }

    /**
     * Computes the area of the peak Ion chromatogram specified. NOTE THAT it assumes
     * the vectors ordered by Retention Time ascendingly.
     * @param IC the vector of Intensities.
     * @param RT the vector of retention times.
     * @param MZ the vector of mz values.
     * @return an array as follows:<br>
     * ret[j][0] is the retention time, t, the quantitation is referred to<br>
     * ret[j][1] is the area of the MS subspectrum at retention time t
     */
    public Vector<series> computeArea (Vector<Float> RT, Vector<Float> MZ, Vector<Float> IC){
   
        Vector<series> ret = new Vector<series>();
        float tmp_val=0;
        int size=RT.size();

        float tmpRetVal[][] = new float[size][2];
        for(int j=0;j<size;j++){
            tmpRetVal[j][0]=-1f;
            tmpRetVal[j][1]=-1f;
        }

        Vector<Float> tmpIC = new Vector<Float>();
        Vector<Float> tmpMZ = new Vector<Float>();
        Vector valsByRT = new Vector();
        Vector<Float> rtV= new Vector<Float>();
        float currRT=RT.elementAt(0);
        int howMany=0;
        for(int j=0;j<size;j++){
            float rtTMP=RT.elementAt(j);
            float mzTMP=MZ.elementAt(j);
            float icTMP=IC.elementAt(j);
            if(currRT==rtTMP){
                if(tmpMZ.size()==0){
                    tmpMZ.add(MZ.elementAt(j));
                    tmpIC.add(IC.elementAt(j));

                }
                else{
                    int pos=Collections.binarySearch(tmpMZ, mzTMP);
                    if(pos<0){
                        pos=0-pos-1;

                    }
                    tmpMZ.add(pos, mzTMP);
                    tmpIC.add(pos, icTMP);
                }

            }
            else{
                //compute quantity and reinizialize arrays
                float qty=0;
               // System.out.println("Computing area on " + tmpMZ.size() + " peaks!");
                for(int ff=0;ff<tmpMZ.size()-1;ff++){
                        tmp_val=(tmpIC.elementAt(ff)+tmpIC.elementAt(ff+1))*(tmpMZ.elementAt(ff+1)-tmpMZ.elementAt(ff))/2;
                        qty+=tmp_val;

                   }

                   series vals = new series(currRT, qty);

                   ret.add(vals);
                   tmpIC.removeAllElements();
                   tmpMZ.removeAllElements();
                   tmpIC.addElement(icTMP);
                   tmpMZ.addElement(mzTMP);
                   currRT=rtTMP;
                   howMany++;

            }

        }

       
       

   //     System.out.println("RT size:" + RT.size() + " IC size:" +IC.size());

/*        float currRT=RT.elementAt(0);
        int howMany=0;
        
        for(int j=0;j<size;j++){

            float tmpRTval=RT.elementAt(j);
            float tmpICval=IC.elementAt(j);
            float tmpMZval=MZ.elementAt(j);

           // System.out.println(j+ ") RT[" + RT.elementAt(j) + "]=" + IC.elementAt(j));
            if(currRT==tmpRTval){
                //Values need to be ordered by MZ now
                int p=Collections.binarySearch(tmpMZ, tmpMZval);
                if(p<0){
                    p=0-p-1;

                }
                tmpIC.add(p, tmpICval);
                tmpMZ.add(p, tmpMZval);

            }

            else{
                   float qty=0f;
                   tmp_val=0;
                   for(int i=0;i<tmpIC.size()-1;i++){
                        tmp_val=(tmpIC.elementAt(i)+tmpIC.elementAt(i+1))*(tmpMZ.elementAt(i+1)-tmpMZ.elementAt(i))/2;
                        qty+=tmp_val;

                   }
                   tmpRetVal[j-1][0]=currRT;
                   tmpRetVal[j-1][1]=qty;
                   tmpIC.removeAllElements();
                   tmpMZ.removeAllElements();
                   tmpIC.addElement(tmpICval);
                   tmpMZ.addElement(tmpMZval);
                   currRT=tmpRTval;
                   howMany++;
            }

        }
        int j=0;
        boolean finished=false;
        ret= new float[howMany][2];
        while(j<howMany){
            ret[j][0]=tmpRetVal[j][0];
            ret[j][1]=tmpRetVal[j][1];
            j++;
        }
 */
        return ret;
    }


      public int findIndex(Vector<Float> vector, float target){
        int size=vector.size();
        int ret=-1;
        int index=0;
        int l_ind=0;
        int u_ind=size-1;
        float val=-1;
        boolean found=false;



        if(size==0){
            ret=0;
            found= true;
        }
        else{
            val=vector.elementAt(0);
            if(target<val){
                ret=1;
                found=true;
            }
            val=vector.elementAt(u_ind);
            if((target>val)&&(! found)){
                ret=u_ind+1;
                found=true;
            }
        }
        while(! found){
            index= Double.valueOf(Math.floor((u_ind-l_ind)/2) + l_ind).intValue();
            val=vector.elementAt(index);
            if(l_ind+1>=u_ind){
                found=true;
                ret=u_ind+1;
            }
            else{
                if(val>target){
                    u_ind=index;
                }
                else{
                    if(val<target){
                       l_ind=index;
                    }
                    else{
                        found=true;
                        ret=index;
                        while((vector.elementAt(ret-1)==target)&&(ret>0)){
                           ret--;
                        }

                    }

                }

            }

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


        System.out.println("FROM LINEAR FIT!!!!");


        if(y.size() != N){
            System.out.println("ERROR: linearFit. Vectors size does not match. x'size:" + N + " y's size:" +y.size());
            System.exit(1);
        }
        else{
        System.out.println("XY=[");
            for(int i=0;i<N;i++){
                        System.out.println(x.elementAt(i) + " " + y.elementAt(i));
                sx  += x.elementAt(i);
                sy  += y.elementAt(i);
                sxx += x.elementAt(i)*x.elementAt(i);
                sxy += x.elementAt(i)*y.elementAt(i);

            }
       System.out.println("]");
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

    private final static String name = "LinearFitQuant";
    private final static String version = "0.1";
    private final static String type = "QUANT_plugin";
    private final static String description = "This plugin reads in LC/MS peak correspondences and computes the area in the time domain. For every couple of labels it outputs the ratio Li/Lj. This means that given N labels we obtain N*(N-1) conditions.";
}