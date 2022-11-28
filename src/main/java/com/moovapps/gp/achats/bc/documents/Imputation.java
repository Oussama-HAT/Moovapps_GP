package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;

public class Imputation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Valider"))
        {
            Number totalImpute = (Number) getWorkflowInstance().getValue("TotalImpute");
            Number totalTTC = (Number) getWorkflowInstance().getValue("TotalTTC");

            if(totalImpute!=null && totalTTC!=null)
            {
                if(totalImpute.intValue()<totalTTC.intValue()||totalImpute.intValue()>totalTTC.intValue())
                {
                    getResourceController().alert("Montant imputé doit être égal au total TTC");
                    return false;
                }
            }else if(totalImpute==null)
            {
                getResourceController().alert("Montant à imputer est null");
                return false;
            }
        }
        return super.onBeforeSubmit(action);
    }
}
