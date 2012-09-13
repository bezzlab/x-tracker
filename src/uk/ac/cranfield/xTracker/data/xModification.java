package uk.ac.cranfield.xTracker.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.Modification;
import uk.ac.ebi.jmzidml.model.mzidml.Param;
import uk.ac.cranfield.xTracker.utils.UnimodParser;
import uk.ac.cranfield.xTracker.xTracker;
/**
 *
 * @author Jun Fan@cranfield
 */
public class xModification{
    public static final int N_TERM_LOCATION = 0;
    public static final int C_TERM_LOCATION = -1;
    public final int NOT_DEFINED = -1000;
    private String name;
    private double massshift = 0;
//    private boolean isVariable;
    private int position = NOT_DEFINED;
    private ArrayList<xParam> params;
    private boolean fromMzIdentML;
    
    public xModification(Modification mod) {
        if(mod.getMonoisotopicMassDelta()!=0) {
            massshift = mod.getMonoisotopicMassDelta();
        }else if(mod.getAvgMassDelta()!=0){
            massshift = mod.getAvgMassDelta();
        }
        if(mod.getLocation()!=null) position = mod.getLocation();
        params = new ArrayList<xParam>();
        List<CvParam> cvParams = mod.getCvParam();
        for(CvParam cvParam:cvParams){
            Param param = new Param();
            param.setParam(cvParam);
            params.add(new xParam(param));
            if(cvParam.getAccession().indexOf("UNIMOD")>-1){
                name = cvParam.getName();
            }
        }
        fromMzIdentML = true;
    }
    
    public xModification(String nameIn, double massshiftIn, int positionIn) {
        name = nameIn;
        massshift = massshiftIn;
        position = positionIn;
        params = new ArrayList<xParam>();
        fromMzIdentML = false;
    }
    
    public uk.ac.liv.jmzqml.model.mzqml.Modification convertToQmodification(){
        uk.ac.liv.jmzqml.model.mzqml.Modification qmod = new uk.ac.liv.jmzqml.model.mzqml.Modification();
        if (massshift !=0) qmod.setMonoisotopicMassDelta(massshift);
        if (position != NOT_DEFINED) qmod.setLocation(position);
        if(fromMzIdentML){
            for(xParam param:params){
                uk.ac.liv.jmzqml.model.mzqml.AbstractParam qParam = param.convertToQparam().getParamGroup();
                if (qParam instanceof uk.ac.liv.jmzqml.model.mzqml.CvParam) {
                    qmod.getCvParam().add((uk.ac.liv.jmzqml.model.mzqml.CvParam)qParam);
                }
            }
        }else{
            //the modification name in mascot is something like iTraq (N-term)
            String accession = UnimodParser.getUnimodID(name);
            if(accession==null){
                Pattern pattern = Pattern.compile("^(\\w+)\\s*\\(");
                Matcher m = pattern.matcher(name);
                if(m.find()){
                    name = m.group(1);
                    accession = UnimodParser.getUnimodID(name);
                }
            }
            if(accession!=null){
                uk.ac.liv.jmzqml.model.mzqml.CvParam qcv = new uk.ac.liv.jmzqml.model.mzqml.CvParam();
                qcv.setAccession(accession);
                qcv.setCvRef(xTracker.study.getCv("UNIMOD"));
                qcv.setName(name);
                qmod.getCvParam().add(qcv);
            }
        }
        return qmod;
    }
            
    public String getID(){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("-");
        sb.append(position);
        return sb.toString();
    }

//    public boolean isVariable() {
//        return isVariable;
//    }

    public double getMassShift() {
        return massshift;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.massshift) ^ (Double.doubleToLongBits(this.massshift) >>> 32));
        hash = 17 * hash + this.position;
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof xModification)) return false;
        xModification mod = (xModification)obj;
        if(this.getMassShift()==mod.getMassShift()
                && this.getPosition() == mod.getPosition()
                && this.getName().equals(mod.getName())) return true;
        return false;
    }
}
