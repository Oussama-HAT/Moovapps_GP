package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class DecisionFinale extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    public boolean onBeforeSubmit(IAction action) {
        try {
            IContext loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            Collection<IWorkflowInstance> workflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Fournisseurs_SP");
            String adjudicataire = null;
            IWorkflowInstance workflowInstanceAdjudicataire = null;
            int adjudicataires = 0;
            for (IWorkflowInstance workflowInstance : workflowInstances) {
                adjudicataire = (String)workflowInstance.getValue("Adjudicataire");
                if (adjudicataire != null && adjudicataire.equals("Oui")) {
                    adjudicataires++;
                    workflowInstanceAdjudicataire = workflowInstance;
                }
            }
            if (adjudicataires == 1 && workflowInstanceAdjudicataire != null) {
                getWorkflowInstance().setValue("SecteurDActivite", workflowInstanceAdjudicataire.getValue("SecteurDActivite"));
                getWorkflowInstance().setValue("SousSecteurDActivite", workflowInstanceAdjudicataire.getValue("SousSecteurDActivite"));
                getWorkflowInstance().setValue("Fournisseur", workflowInstanceAdjudicataire.getValue("Fournisseur"));
                getWorkflowInstance().save(this.sysAdminContext);
                for (IWorkflowInstance workflowInstance : workflowInstances)
                    WorkflowsService.executeAction(workflowInstance, loggedOnUserContext, "ValiderLaDecisionFinale", "");
            } else {
                getResourceController().alert("Vous devez sun seul fournisseur adjudicataire !");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }
}
