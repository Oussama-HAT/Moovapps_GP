package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;

import java.util.Collection;

public class SaisieDevis extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("ValiderLEvaluation"))
        {
            boolean test = false;
            Collection<ILinkedResource> evalConsultation = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationConsultation");
            if(evalConsultation!= null && !evalConsultation.isEmpty())
                for (ILinkedResource iLinkedResource: evalConsultation)
                {
                    if(iLinkedResource.getValue("Adjudicataire")!=null && iLinkedResource.getValue("Adjudicataire").equals("Oui"))
                    {
                        if(test)
                        {
                            getResourceController().alert("Vous ne pouvez pas choisir plusieurs fournisseurs adjudicataires !");
                            return false;
                        }
                        else
                        {
                            test = true;
                        }
                    }
                }
        }
        return super.onBeforeSubmit(action);
    }

    @Override
    public boolean onAfterSubmit(IAction action) {
        Collection<ILinkedResource> evalConsultation = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationConsultation");
        if(evalConsultation!= null && !evalConsultation.isEmpty())
            for (ILinkedResource iLinkedResource: evalConsultation) {
                if(iLinkedResource.getValue("Adjudicataire").equals("Oui")){
                    getWorkflowInstance().setValue("Fournisseur" , iLinkedResource.getValue("Fournisseur"));
                    getWorkflowInstance().save("Fournisseur");
                    break;
                }
            }
        return super.onAfterSubmit(action);
    }
}
