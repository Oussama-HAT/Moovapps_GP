package com.moovapps.gp.achats.ao.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class EvaluationTechnique extends BaseDocumentExtension {
    protected IContext sysAdmincontext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    public boolean onAfterLoad() {
        try {
            Collection<IWorkflowInstance> linkedWorkflowInstancesDepots = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
            for (IWorkflowInstance linkedWorkflowInstanceDepot : linkedWorkflowInstancesDepots)
                getBasesNotationTechnqiue(linkedWorkflowInstanceDepot, "BaseNotationTech_AO_Tab");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public boolean onBeforeSubmit(IAction action) {
        try {
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            if (action.getName().equals("Valider8")) {
                Collection<IWorkflowInstance> linkedWorkflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
                for (IWorkflowInstance linkedWorkflowInstance : linkedWorkflowInstances)
                    WorkflowsService.executeAction(linkedWorkflowInstance, this.loggedOnUserContext, "Valider3", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite. Merci de contacter votre administrateur !");
            return false;
        }
        return super.onBeforeSubmit(action);
    }

    private void getBasesNotationTechnqiue(IWorkflowInstance linkedWorkflowInstance, String tableBaseNTName) {
        try {
            String lotDepot = (String)linkedWorkflowInstance.getValue("Lot");
            if (lotDepot != null) {
                Collection<ILinkedResource> linkedResourcesBasesNotation = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources(tableBaseNTName);
                String lotPreparation = null;
                for (ILinkedResource linkedResourceBaseNotation : linkedResourcesBasesNotation) {
                    lotPreparation = (String)linkedResourceBaseNotation.getValue("Lot");
                    if (lotPreparation != null && lotPreparation.equals(lotDepot)) {
                        linkedWorkflowInstance.setValue("BaseDeNotationTechnique", linkedResourceBaseNotation.getValue("BaseDeNotationTechnique"));
                        linkedWorkflowInstance.setValue("NoteEliminatoireTechnique", linkedResourceBaseNotation.getValue("NoteEliminatoire"));
                        linkedWorkflowInstance.save(this.sysAdmincontext);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
