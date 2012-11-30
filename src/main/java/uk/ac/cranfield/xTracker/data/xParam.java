package uk.ac.cranfield.xTracker.data;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab.MzTabParsingException;
import uk.ac.liv.jmzqml.model.mzqml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.Param;
import uk.ac.liv.jmzqml.model.mzqml.UserParam;
import uk.ac.liv.jmzqml.model.mzqml.AbstractParam;
import uk.ac.liv.jmzqml.model.mzqml.Cv;
import uk.ac.cranfield.xTracker.xTracker;
/**
 *
 * @author Jun Fan@cranfield
 */
public class xParam {
    Param param;
    public xParam(Param param){
        this.param = param;
    }
    
    public uk.ac.liv.jmzqml.model.mzqml.Param convertToQparam(){
        AbstractParam abstractParam;
        final uk.ac.ebi.jmzidml.model.mzidml.CvParam iCvParam = param.getCvParam();
        if (iCvParam != null) {
            abstractParam = new CvParam();
            abstractParam.setName(iCvParam.getName());
            //autoResolving set to true in line 175 for CvParam
            uk.ac.ebi.jmzidml.model.mzidml.Cv iCv= iCvParam.getCv();
            //cv is the library of CV terms, e.g. PSI-MS, UO
            Cv cv = xTracker.study.getCv(iCv.getId());
            if(cv==null){
                cv = new Cv();
                cv.setFullName(iCv.getFullName());
                cv.setId(iCv.getId());
                cv.setUri(iCv.getUri());
                cv.setVersion(iCv.getVersion());
                xTracker.study.addCv(cv.getId(), cv);
                xTracker.study.getMzQuantML().getCvList().getCv().add(cv);
            }
            ((CvParam) abstractParam).setCvRef(cv);
            ((CvParam) abstractParam).setAccession(iCvParam.getAccession());
            abstractParam.setValue(iCvParam.getValue());
            if(iCvParam.getUnitCvRef()!=null){
//            if(iCvParam.getUnitCv()!=null){
                ((CvParam) abstractParam).setUnitAccession(iCvParam.getUnitAccession());
                ((CvParam) abstractParam).setUnitName(iCvParam.getUnitName());
                ((CvParam) abstractParam).setUnitCvRef(iCvParam.getUnitCvRef());
            }
        } else {//userParam
            abstractParam = new UserParam();
            abstractParam.setValue(param.getUserParam().getValue());
            abstractParam.setName(param.getUserParam().getName());
            ((UserParam)abstractParam).setType(param.getUserParam().getType());
        }
        uk.ac.liv.jmzqml.model.mzqml.Param qparam = new uk.ac.liv.jmzqml.model.mzqml.Param();
        qparam.setParamGroup(abstractParam);
        return qparam;
    }
    
    public uk.ac.ebi.pride.jmztab.model.Param convertToTabParam(){
        uk.ac.ebi.pride.jmztab.model.Param tabParam = null;
        try {
            if (param.getCvParam() != null) {
                tabParam = new uk.ac.ebi.pride.jmztab.model.Param(param.getCvParam().getCv().getId(), param.getCvParam().getAccession(), param.getCvParam().getName(),param.getCvParam().getValue());
            }else{
                tabParam = new uk.ac.ebi.pride.jmztab.model.Param(param.getUserParam().getName(), param.getUserParam().getValue());
            }
        } catch (MzTabParsingException ex) {
            Logger.getLogger(xParam.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tabParam;
    }
}
