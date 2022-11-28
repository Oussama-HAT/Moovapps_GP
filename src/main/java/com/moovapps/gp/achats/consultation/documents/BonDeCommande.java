package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class BonDeCommande extends BaseDocumentExtension {
    protected IContext context = DirectoryService.getSysAdminContext();

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Cloturer"))
                validationDemandesAchatDeReference();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    private void validationDemandesAchatDeReference() {
        IContext contextAcheteur = getWorkflowModule().getContext(getWorkflowInstance().getCreatedBy());
        try {
            Collection<ProcessWorkflowInstance> uris = (Collection<ProcessWorkflowInstance>)getWorkflowInstance().getValue("URISDA");
            for (ProcessWorkflowInstance workflowInstanceDA : uris)
                WorkflowsService.executeAction((IWorkflowInstance)workflowInstanceDA, contextAcheteur, "DATraitee", "");
        } catch (Exception e) {
            try {
                Collection<String> uris = (Collection<String>)getWorkflowInstance().getValue("URISDA");
                IWorkflowInstance workflowInstanceDA = null;
                for (String uri : uris) {
                    workflowInstanceDA = (IWorkflowInstance)getWorkflowModule().getElementByProtocolURI(uri);
                    WorkflowsService.executeAction(workflowInstanceDA, contextAcheteur, "DATraitee", "");
                }
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
    }
}
