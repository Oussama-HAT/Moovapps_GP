package com.moovapps.gp.achats.ao.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.ResourceTableInputComponent;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import java.util.ArrayList;
import java.util.Collection;

public class PreparationDossierAO extends BaseDocumentExtension {
    protected IContext context = DirectoryService.getSysAdminContext();

    public void onPropertyChanged(IProperty property) {
        try {
            if (property.getName().equals("Lots_AO_Tab"))
                MAJBordereauDesPrixEtModalites();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

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
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("SousCommissionTech_AO_Tab");
            for (ILinkedResource linkedResource : linkedResources) {
                membre = (IUser)linkedResource.getValue("NomPrenom");
                qualite = (String)linkedResource.getValue("Qualite");
                if (qualite != null && qualite.equals("Présient"))
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
            if (uris != null && !uris.isEmpty())
                for (ProcessWorkflowInstance workflowInstanceDA : uris) {
                    workflowInstanceDA.setValue("ReferenceDeLAO", getWorkflowInstance().getValue("sys_Reference"));
                    workflowInstanceDA.setValue("DocumentState", "AO en cours");
                    workflowInstanceDA.save(this.context);
                }
        } catch (Exception e) {
            try {
                Collection<String> uris = (Collection<String>)getWorkflowInstance().getValue("URISDA");
                if (uris != null && !uris.isEmpty()) {
                    IWorkflowInstance workflowInstanceDA = null;
                    for (String uri : uris) {
                        workflowInstanceDA = (IWorkflowInstance)getWorkflowModule().getElementByProtocolURI(uri);
                        workflowInstanceDA.setValue("ReferenceDeLAO", getWorkflowInstance().getValue("sys_Reference"));
                        workflowInstanceDA.setValue("DocumentState", "AO en cours");
                        workflowInstanceDA.save(this.context);
                    }
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
                workflowInstanceDA.setValue("ReferenceDeLAO", null);
                workflowInstanceDA.setValue("DocumentState", "Demande validée");
                        workflowInstanceDA.save(this.context);
            }
            getWorkflowInstance().setValue("URISDA", null);
            getWorkflowInstance().save(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void MAJBordereauDesPrixEtModalites() {
        try {
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
            if (linkedResourcesLots == null || linkedResourcesLots.isEmpty()) {
                getWorkflowInstance().setValue("BP_AO_Tab", null);
                getWorkflowInstance().setValue("Modalites_AO_Tab", null);
            }
            Collection<ILinkedResource> linkedResourcesBPX = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("BP_AO_Tab");
            if (linkedResourcesBPX != null && linkedResourcesLots.size() != linkedResourcesBPX.size()) {
                MAJLignesTableau(linkedResourcesBPX, linkedResourcesLots);
                ResourceTableInputComponent tableBP = (ResourceTableInputComponent)getResourceController().getDefaultWidget("BP_AO_Tab");
                tableBP.getView().refresh();
            }
            Collection<ILinkedResource> linkedResourcesModalites = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Modalites_AO_Tab");
            if (linkedResourcesModalites != null && linkedResourcesLots.size() != linkedResourcesModalites.size()) {
                MAJLignesTableau(linkedResourcesModalites, linkedResourcesLots);
                ResourceTableInputComponent tableModalites = (ResourceTableInputComponent)getResourceController().getDefaultWidget("Modalites_AO_Tab");
                tableModalites.getView().refresh();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void MAJLignesTableau(Collection<ILinkedResource> linkedResourcesDestination, Collection<ILinkedResource> linkedResourcesLots) {
        try {
            String codeSource = null, codeDestination = null;
            Boolean trouve = Boolean.valueOf(false);
            for (ILinkedResource linkedResourceDestination : linkedResourcesDestination) {
                codeDestination = (String)linkedResourceDestination.getValue("Code");
                trouve = Boolean.valueOf(false);
                for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                    codeSource = (String)linkedResourceLot.getValue("Code");
                    if (codeSource != null && codeDestination != null && codeSource.equals(codeDestination)) {
                        trouve = Boolean.valueOf(true);
                        break;
                    }
                }
                if (!trouve.booleanValue())
                    getWorkflowInstance().deleteLinkedResource(linkedResourceDestination);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
