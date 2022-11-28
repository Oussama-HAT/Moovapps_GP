package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class DemandeDePrix extends BaseDocumentExtension {
    public boolean onBeforeSubmit(IAction action) {
        try {
            IContext loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            Collection<IWorkflowInstance> workflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Fournisseurs_SP");
            for (IWorkflowInstance workflowInstance : workflowInstances)
                WorkflowsService.executeAction(workflowInstance, loggedOnUserContext, "Envoyer", "");
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite ! Merci de contacter votre administrateur.");
            return false;
        }
        return super.onBeforeSubmit(action);
    }
}
