package com.moovapps.gp.achats.ao.tableaux;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TablCriteresNotationTechnique extends BaseDocumentExtension {
    public boolean onAfterLoad() {
        try {
            getWorkflowInstance().setList("Lot", getListLots());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    private List<IOptionList.IOption> getListLots() {
        List<IOptionList.IOption> options = new ArrayList<>();
        try {
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
            String evalTechnique = null, code = null, intitule = null;
            ArrayList<IOptionList.IOption> lots = new ArrayList<>();
            for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                evalTechnique = (String)linkedResourceLot.getValue("EvaluationTechnique");
                if (evalTechnique != null && evalTechnique.equals("Oui")) {
                    code = (String)linkedResourceLot.getValue("Code");
                    intitule = (String)linkedResourceLot.getValue("Intitule");
                    if (code != null && intitule != null)
                        lots.add(getWorkflowModule().createListOption(String.valueOf(code) + " - " + intitule, String.valueOf(code) + " - " + intitule));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }
}
