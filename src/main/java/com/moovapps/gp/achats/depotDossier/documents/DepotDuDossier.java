package com.moovapps.gp.achats.depotDossier.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DepotDuDossier extends BaseDocumentExtension {
    public boolean onAfterLoad() {
        try {
            getWorkflowInstance().setList("Lot", getListLots());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            if (property.getName().equals("Lot")) {
                String lot = (String)getWorkflowInstance().getValue("Lot");
                if (lot != null) {
                    Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
                    String code = null, intitule = null;
                    for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                        code = (String)linkedResourceLot.getValue("Code");
                        intitule = (String)linkedResourceLot.getValue("Intitule");
                        if (lot.equals(code + " - " + intitule)) {
                            getWorkflowInstance().setValue("CautionProvisoire", linkedResourceLot.getValue("CautionProvisoire"));
                            getWorkflowInstance().setValue("MontantEstimatifTTC", linkedResourceLot.getValue("MontantEstimatifTTC"));
                            getWorkflowInstance().setValue("ModeDEvaluation", linkedResourceLot.getValue("ModeDEvaluation"));
                            getWorkflowInstance().setValue("DePonderationTechnique", linkedResourceLot.getValue("DePonderationTechnique"));
                            getWorkflowInstance().setValue("DePonderationFinanciere", linkedResourceLot.getValue("DePonderationFinanciere"));
                            break;
                        }
                    }
                } else {
                    getWorkflowInstance().setValue("CautionProvisoire", null);
                    getWorkflowInstance().setValue("MontantEstimatifTTC", null);
                    getWorkflowInstance().setValue("ModeDEvaluation", null);
                    getWorkflowInstance().setValue("DePonderationTechnique", null);
                    getWorkflowInstance().setValue("DePonderationFinanciere", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    private List<IOptionList.IOption> getListLots() {
        List<IOptionList.IOption> options = new ArrayList<>();
        try {
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
            String code = null, intitule = null;
            for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                code = (String)linkedResourceLot.getValue("Code");
                intitule = (String)linkedResourceLot.getValue("Intitule");
                if (code != null && intitule != null)
                    options.add(getWorkflowModule().createListOption(String.valueOf(code) + " - " + intitule, String.valueOf(code) + " - " + intitule));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }
}
