package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;
import java.util.Date;

public class SaisieDesDevisContradictoires extends BaseDocumentExtension {
    public boolean onBeforeSubmit(IAction action) {
        try {
            IContext loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            Collection<IWorkflowInstance> workflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Fournisseurs_SP");
            Date dateReceptionDevis = null;
            for (IWorkflowInstance workflowInstance : workflowInstances) {
                dateReceptionDevis = (Date)workflowInstance.getValue("DateDeReceptionDuDevis");
                if (dateReceptionDevis != null) {
                    WorkflowsService.executeAction(workflowInstance, loggedOnUserContext, "ValiderLeDevis", "");
                    continue;
                }
                WorkflowsService.executeAction(workflowInstance, loggedOnUserContext, "DateLimiteDepassee", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite ! Merci de contacter votre administrateur.");
            return false;
        }
        return super.onBeforeSubmit(action);
    }
}
