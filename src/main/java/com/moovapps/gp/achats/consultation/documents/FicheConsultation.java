package com.moovapps.gp.achats.consultation.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import java.util.ArrayList;
import java.util.Collection;

public class FicheConsultation extends BaseDocumentExtension {
    protected IContext context = DirectoryService.getSysAdminContext();

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Envoyer")) {
                validationDemandesAchatDeReference();
                MAJCommission();
            } else if (action.getName().contains("Annuler")) {
                annulationDemandesAchatDeReference();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    private void MAJCommission() {
        try {
            ArrayList<IUser> membres = new ArrayList<>();
            IUser membre = null;
            String qualite = null;
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("Commission_Tab");
            for (ILinkedResource linkedResource : linkedResources) {
                membre = (IUser)linkedResource.getValue("NomPrenom");
                qualite = (String)linkedResource.getValue("Qualite");
                if (qualite != null && qualite.equals("Président"))
                        getWorkflowInstance().setValue("PresidentDeLaCommissionUser", membre);
                if (membre != null && !membres.contains(membre))
                    membres.add(membre);
            }
            getWorkflowInstance().setValue("CommissionUsers", membres);
            getWorkflowInstance().save(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validationDemandesAchatDeReference() {
        try {
            Collection<ProcessWorkflowInstance> uris = (Collection<ProcessWorkflowInstance>)getWorkflowInstance().getValue("URISDA");
            for (ProcessWorkflowInstance workflowInstanceDA : uris) {
                workflowInstanceDA.setValue("ReferenceDeLaConsultation", getWorkflowInstance().getValue("sys_Reference"));
                workflowInstanceDA.setValue("DocumentState", "Consultation en cours");
                workflowInstanceDA.save(this.context);
            }
        } catch (Exception e) {
            try {
                Collection<String> uris = (Collection<String>)getWorkflowInstance().getValue("URISDA");
                IWorkflowInstance workflowInstanceDA = null;
                for (String uri : uris) {
                    workflowInstanceDA = (IWorkflowInstance)getWorkflowModule().getElementByProtocolURI(uri);
                    workflowInstanceDA.setValue("ReferenceDeLaConsultation", getWorkflowInstance().getValue("sys_Reference"));
                    workflowInstanceDA.setValue("DocumentState", "Consultation en cours");
                    workflowInstanceDA.save(this.context);
                }
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
    }

    private void annulationDemandesAchatDeReference() {
        try {
            Collection<ProcessWorkflowInstance> uris = (Collection<ProcessWorkflowInstance>)getWorkflowInstance().getValue("URISDA");
            for (ProcessWorkflowInstance workflowInstanceDA : uris) {
                workflowInstanceDA.setValue("ReferenceDeLaConsultation", null);
                workflowInstanceDA.setValue("DocumentState", "Demande validée");
                workflowInstanceDA.save(this.context);
            }
            getWorkflowInstance().setValue("URISDA", null);
            getWorkflowInstance().save(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
