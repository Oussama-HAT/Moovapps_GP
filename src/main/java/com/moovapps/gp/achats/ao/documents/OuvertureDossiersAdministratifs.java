package com.moovapps.gp.achats.ao.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class OuvertureDossiersAdministratifs extends BaseDocumentExtension {
    protected IContext sysAdmincontext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Valider6")) {
                this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
                Collection<IWorkflowInstance> linkedWorkflowInstances = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
                for (IWorkflowInstance linkedWorkflowInstance : linkedWorkflowInstances)
                    WorkflowsService.executeAction(linkedWorkflowInstance, this.loggedOnUserContext, "Valider2", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur s'est produite. Merci de contacter votre administrateur !");
            return false;
        }
        return super.onBeforeSubmit(action);
    }
}
