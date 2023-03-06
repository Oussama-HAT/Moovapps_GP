package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;

import java.math.BigDecimal;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class Imputation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Valider"))
        {
            BigDecimal totalImpute = castToBigDecimal(getWorkflowInstance().getValue("TotalImpute"));
            BigDecimal totalTTC = castToBigDecimal(getWorkflowInstance().getValue("TotalTTC"));

            if(totalImpute!=null && totalTTC!=null)
            {
                if(totalImpute.compareTo(totalTTC) != 0)
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
