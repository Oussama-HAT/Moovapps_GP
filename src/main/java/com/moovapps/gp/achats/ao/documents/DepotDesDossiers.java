package com.moovapps.gp.achats.ao.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class DepotDesDossiers extends BaseDocumentExtension {
    protected IContext loggedOnUserContext = null;

    protected Collection<ILinkedResource> linkedResourcesModalites = null;

    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Valider5")) {
                this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
                this.linkedResourcesModalites = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Modalites_AO_Tab");
                Collection<IWorkflowInstance> linkedWorkflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
                for (IWorkflowInstance linkedWorkflowInstance : linkedWorkflowInstances) {
                    MAJWorkflowInstanceDepot(linkedWorkflowInstance);
                    WorkflowsService.executeAction(linkedWorkflowInstance, this.loggedOnUserContext, "Valider26", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite. Merci de contacter votre administrateur !");
            return false;
        }
        return super.onBeforeSubmit(action);
    }

    private void MAJWorkflowInstanceDepot(IWorkflowInstance linkedWorkflowInstanceDepot) {
        try {
            String lot = (String)linkedWorkflowInstanceDepot.getValue("Lot");
            String code = null, intitule = null;
            for (ILinkedResource linkedResourceModalite : this.linkedResourcesModalites) {
                code = (String)linkedResourceModalite.getValue("Code");
                intitule = (String)linkedResourceModalite.getValue("Intitule");
                if (lot != null && lot.equals(String.valueOf(code) + " - " + intitule)) {
                    linkedWorkflowInstanceDepot.setValue("Echantillon", linkedResourceModalite.getValue("Echantillon"));
                    linkedWorkflowInstanceDepot.setValue("VisiteDesLieux", linkedResourceModalite.getValue("VisiteDesLieux"));
                    linkedWorkflowInstanceDepot.setValue("EvaluationTechnique", linkedResourceModalite.getValue("EvaluationTechnique"));
                    linkedWorkflowInstanceDepot.save(this.loggedOnUserContext);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
