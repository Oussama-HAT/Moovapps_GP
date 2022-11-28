package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;

import java.util.Collection;

public class CopyFournisseur extends BaseDocumentExtension {

    @Override
    public boolean onAfterSubmit(IAction action) {
        if(action.getName().equals("Valider2")){
            Collection<ILinkedResource> fournisseurLinkedResources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("DemandeDesPrix");
            ILinkedResource iLinkedResource = null;
            if(fournisseurLinkedResources!=null && !fournisseurLinkedResources.isEmpty()){
                for(ILinkedResource fournisseurResource :fournisseurLinkedResources) {
                    iLinkedResource = getWorkflowInstance().createLinkedResource("EvaluationConsultation");
                    iLinkedResource.setValue("Fournisseur",fournisseurResource.getValue("Fournisseur"));
                    iLinkedResource.save(getWorkflowModule().getSysadminContext());
                    getWorkflowInstance().addLinkedResource(iLinkedResource);
                }
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            }
        }
        return super.onAfterSubmit(action);
    }
}
