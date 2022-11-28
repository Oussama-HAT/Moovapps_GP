package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class EvaluationDesOffres extends BaseDocumentExtension {
    public boolean onBeforeSubmit(IAction action) {
        try {
            IContext loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            Collection<IWorkflowInstance> workflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Fournisseurs_SP");
            Collection<ILinkedResource> linkedResources = null;
            IUser membre = null;
            String adjudicataire = null;
            Integer fournisseursAdjudicataires = Integer.valueOf(0);
            for (IWorkflowInstance workflowInstance : workflowInstances) {
                linkedResources = (Collection<ILinkedResource>)workflowInstance.getValue("EvaluationDeLOffre_Tab");
                if (linkedResources != null && !linkedResources.isEmpty())
                    for (ILinkedResource linkedResource : linkedResources) {
                        membre = (IUser)linkedResource.getValue("NomPrenom");
                        if (membre != null && membre.equals(loggedOnUserContext.getUser())) {
                            adjudicataire = (String)linkedResource.getValue("Adjudicataire");
                            if (adjudicataire != null && adjudicataire.equals("Oui"))
                                fournisseursAdjudicataires = Integer.valueOf(fournisseursAdjudicataires.intValue() + 1);
                            break;
                        }
                    }
            }
            if (fournisseursAdjudicataires.intValue() > 1) {
                getResourceController().alert("Vous ne pouvez pas splusieurs fournisseur adjudicataires !");
                return false;
            }
            for (IWorkflowInstance workflowInstance : workflowInstances)
                WorkflowsService.executeAction(workflowInstance, loggedOnUserContext, "ValiderLEvaluation", "");
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite ! Merci de contacter votre administrateur.");
            return false;
        }
        return super.onBeforeSubmit(action);
    }
}
