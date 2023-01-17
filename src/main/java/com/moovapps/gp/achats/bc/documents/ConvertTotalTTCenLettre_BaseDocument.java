package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.moovapps.gp.helpers.ConvertirMontantEnLettre;

import java.math.BigDecimal;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class ConvertTotalTTCenLettre_BaseDocument extends BaseDocumentExtension {
    private static final long serialVersionUID = -1513666146210682764L;

    public boolean onAfterLoad() {
        try {
            getResourceController().setThrowEvents("TotalTTC", true);
            getResourceController().setThrowEvents("TotalFactureTTC", true);
            if (getWorkflowInstance().getValue("TotalTTC") != null)
                setMontantEnLettre();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            if (property.getName().equals("TotalTTC") || property.getName().equals("TotalFactureTTC"))
                setMontantEnLettre();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    private void setMontantEnLettre() {
        try {
            if (getWorkflowInstance().getValue("TotalTTC") != null) {
                BigDecimal montantBC =castToBigDecimal(getWorkflowInstance().getValue("TotalTTC"));
                getWorkflowInstance().setValue("TotalTTCEnLettre", ConvertirMontantEnLettre.begin(String.valueOf(montantBC)));
            } else {
                getWorkflowInstance().setValue("TotalTTCEnLettre", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
