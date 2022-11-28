package com.moovapps.gp.achats.ao.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class Adjudication extends BaseDocumentExtension {
    protected IContext sysAdmincontext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Valider10")) {
                this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
                Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
                Collection<IWorkflowInstance> linkedWorkflowInstancesDepots = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
                String lotDepot = null, adjudicataire = null, lot = null;
                int adjudicataires = 0;
                for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                    adjudicataires = 0;
                    lot = linkedResourceLot.getValue("Code") + " - " + linkedResourceLot.getValue("Intitule");
                    for (IWorkflowInstance linkedWorkflowInstanceDepot : linkedWorkflowInstancesDepots) {
                        lotDepot = (String)linkedWorkflowInstanceDepot.getValue("Lot");
                        if (lot != null && lot.equals(lotDepot)) {
                            adjudicataire = (String)linkedWorkflowInstanceDepot.getValue("DecisionDeLaCommissionAdjudication");
                            if (adjudicataire != null && adjudicataire.equals("Adjudicataire"))
                                adjudicataires++;
                        }
                    }
                    if (adjudicataires != 1) {
                        getResourceController().alert("Vous devez sun seul fournisseur adjudicataire par lot !");
                        return false;
                    }
                }
                Collection<IWorkflowInstance> linkedWorkflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
                for (IWorkflowInstance linkedWorkflowInstance : linkedWorkflowInstances)
                    WorkflowsService.executeAction(linkedWorkflowInstance, this.loggedOnUserContext, "Valider", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite. Merci de contacter votre administrateur !");
            return false;
        }
        return super.onBeforeSubmit(action);
    }
}
