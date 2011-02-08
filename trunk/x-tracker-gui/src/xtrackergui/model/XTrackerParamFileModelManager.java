
//
//    xTrackerGui
//
//    Package: xtrackergui.model
//    File: XTrackerParamFileModelManager.java
//    Date: 01/08/2010
//    Author: Andrew Bullimore
//

package xtrackergui.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrew bullimore
 */
public class XTrackerParamFileModelManager {

    private List<XTrackerParamFileModel> paramFileModels;

    /**
     *
     *
     */
    public XTrackerParamFileModelManager() {

        paramFileModels = new ArrayList<XTrackerParamFileModel>();
    }

    @Override
    public String toString() {
        
        return new String("XTrackerParaFileModelManager has " + paramFileModels.size() +
                          " parameter file models");
    }

    /**
     *
     *
     */
    public void addParamFileModel(XTrackerParamFileModel paramFileModel) {

        paramFileModels.add(paramFileModel);
    }

    /**
     *
     *
     */
    public XTrackerParamFileModel getParamFileModelBySchemaFile(String xsdSchemaFileName) {

        XTrackerParamFileModel paramFileModel = null;
        
        for(XTrackerParamFileModel model : paramFileModels) {
            
            if(model.getXsdSchemaFileName().equals(xsdSchemaFileName) == true) {

                paramFileModel = model;
                break;
            }
        }

        return paramFileModel;
    }

    /**
     *
     *
     */
    public XTrackerParamFileModel getParamFileModelByJarFileAssoc(String jarFileName) {

        XTrackerParamFileModel paramFileModel = null;

        for(XTrackerParamFileModel model : paramFileModels) {

            if(model.isJarFileAssociated(jarFileName)) {

                paramFileModel = model;
                break;
            }
        }

        return paramFileModel;
    }

    /**
     *
     *
     */
    public boolean doesParamFileModelExistForSchema(String xsdSchemaFileName) {

        boolean modelExists = false;

        for(XTrackerParamFileModel model : paramFileModels) {

            if(model.getXsdSchemaFileName().equals(xsdSchemaFileName)) {

                modelExists = true;
                break;
            }
        }
        
        return modelExists;
    }

    /**
     *
     *
     */
    public boolean doesParamFileModelExistWithJarFileAssoc(String jarFileName) {

        boolean jarFileAssociated = false;

        for(XTrackerParamFileModel model : paramFileModels) {

            if(model.isJarFileAssociated(jarFileName)) {

                jarFileAssociated = true;
                break;
            }
        }

        return jarFileAssociated;
    }

    /**
     *
     *
     */
    public void removeParamFileModel(String xsdSchemaFileName) {

        XTrackerParamFileModel model = getParamFileModelBySchemaFile(xsdSchemaFileName);

        if(model != null) {
        
            paramFileModels.remove(model);
        }
    }

    /**
     *
     *
     */
    public void removeAllParamFileModels() {

        paramFileModels.clear();
    }
}
