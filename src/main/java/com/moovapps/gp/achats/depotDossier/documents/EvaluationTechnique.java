package com.moovapps.gp.achats.depotDossier.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import java.util.Collection;

public class EvaluationTechnique extends BaseDocumentExtension {
    IContext loggedOnUserContext = null;

    public boolean onAfterLoad() {
        try {
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            IWorkflowInstance parentInstance = getWorkflowInstance().getParentInstance();
            if (parentInstance != null) {
                getCriteresEvaluationTechnique(parentInstance, "CriteresNotationTech_AO_Tab", "ET_Depot_Tab");
                getBasesNotationTechnqiue(parentInstance, "BaseNotationTech_AO_Tab");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    private void getCriteresEvaluationTechnique(IWorkflowInstance parentInstance, String tableCriteresNTName, String tableName) {
        try {
            Collection<ILinkedResource> linkedResourcesEvalTechnique = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources(tableName);
            String lotDepot = (String)getWorkflowInstance().getValue("Lot");
            if (lotDepot != null && (linkedResourcesEvalTechnique == null || linkedResourcesEvalTechnique.isEmpty())) {
                Collection<ILinkedResource> linkedResourcesCriteres = (Collection<ILinkedResource>) parentInstance.getLinkedResources(tableCriteresNTName);
                ILinkedResource newLinkedResource = null;
                String lotPreparation = null;
                for (ILinkedResource linkedResourceCritere : linkedResourcesCriteres) {
                    lotPreparation = (String)linkedResourceCritere.getValue("Lot");
                    if (lotPreparation != null && lotPreparation.equals(lotDepot)) {
                        newLinkedResource = getWorkflowInstance().createLinkedResource(tableName);
                        newLinkedResource.setValue("Critere", linkedResourceCritere.getValue("Critere"));
                        newLinkedResource.setValue("Bareme", linkedResourceCritere.getValue("Bareme"));
                        newLinkedResource.save(this.loggedOnUserContext);
                        getWorkflowInstance().addLinkedResource(newLinkedResource);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getBasesNotationTechnqiue(IWorkflowInstance parentInstance, String tableBaseNTName) {
        try {
            String lotDepot = (String)getWorkflowInstance().getValue("Lot");
            if (lotDepot != null) {
                Collection<ILinkedResource> linkedResourcesBasesNotation = (Collection<ILinkedResource>) parentInstance.getLinkedResources(tableBaseNTName);
                String lotPreparation = null;
                for (ILinkedResource linkedResourceBaseNotation : linkedResourcesBasesNotation) {
                    lotPreparation = (String)linkedResourceBaseNotation.getValue("Lot");
                    if (lotPreparation != null && lotPreparation.equals(lotDepot)) {
                        getWorkflowInstance().setValue("BaseDeNotationTechnique", linkedResourceBaseNotation.getValue("BaseDeNotationTechnique"));
                        getWorkflowInstance().setValue("NoteEliminatoireTechnique", linkedResourceBaseNotation.getValue("NoteEliminatoire"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
