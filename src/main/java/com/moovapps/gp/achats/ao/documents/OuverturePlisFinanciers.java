package com.moovapps.gp.achats.ao.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class OuverturePlisFinanciers extends BaseDocumentExtension {
    protected IContext sysAdmincontext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            if (action.getName().equals("Valider9")) {
                Collection<IWorkflowInstance> workflowInstancesDepots = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
                String modeEvaluation = null, offreMoinsDisante = null, offreMieuxDisante = null, documentState = null;
                for (IWorkflowInstance workflowInstanceDepot : workflowInstancesDepots) {
                    documentState = (String)workflowInstanceDepot.getValue("DocumentState");
                    if (documentState.equals("Evaluation financière")) {
                            modeEvaluation = (String)workflowInstanceDepot.getValue("ModeDEvaluation");
                    if (modeEvaluation.equals("Moins disant")) {
                        offreMoinsDisante = (String)workflowInstanceDepot.getValue("OffreMoinsDisante");
                        if (offreMoinsDisante != null && offreMoinsDisante.equals("Oui")) {
                            workflowInstanceDepot.setValue("DecisionDeLaCommissionAdjudication", "Adjudicataire");
                        } else {
                            workflowInstanceDepot.setValue("DecisionDeLaCommissionAdjudication", "Non retenu");
                        }
                    } else if (modeEvaluation.equals("Mieux disant")) {
                        offreMieuxDisante = (String)workflowInstanceDepot.getValue("OffreMieuxDisante");
                        if (offreMieuxDisante != null && offreMieuxDisante.equals("Oui")) {
                            workflowInstanceDepot.setValue("DecisionDeLaCommissionAdjudication", "Adjudicataire");
                        } else {
                            workflowInstanceDepot.setValue("DecisionDeLaCommissionAdjudication", "Non retenu");
                        }
                    }
                    workflowInstanceDepot.save(this.sysAdmincontext);
                    WorkflowsService.executeAction(workflowInstanceDepot, this.loggedOnUserContext, "Valider5", "");
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        getResourceController().alert("Une erreur s'est produite. Merci de contacter votre administrateur !");
        return false;
    }
    return super.onBeforeSubmit(action);
}
}
