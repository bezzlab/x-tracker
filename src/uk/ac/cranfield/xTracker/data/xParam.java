package uk.ac.cranfield.xTracker.data;

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
        if (param.getCvParam() != null) {
            abstractParam = new CvParam();
            abstractParam.setName(param.getCvParam().getName());
            //autoResolving set to true in line 175 for CvParam
            uk.ac.ebi.jmzidml.model.mzidml.Cv iCv= param.getCvParam().getCv();
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
//            ((CvParam) abstractParam).setCvRef(param.getCvParam().getCv());
            ((CvParam) abstractParam).setAccession(param.getCvParam().getAccession());
            abstractParam.setValue(param.getCvParam().getValue());
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
}
