package com.moovapps.gp.br.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;

import java.util.Collection;

public class CreationPaiement extends BaseDocumentExtension {
    private IContext loggedOnContext = null;

    @Override
    public boolean onBeforeSubmit(IAction action) {
        try {
            this.loggedOnContext = getWorkflowModule().getLoggedOnUserContext();
            double rap = 0.0D;
            if(action.getName().equals("EnvoyerPourPaiement")){
                if(getWorkflowInstance().getValue("BonAPayer").equals("Oui")){
                    IWorkflowInstance BCInstance = getWorkflowInstance().getParentInstance();
                    if(BCInstance!=null){
                        Collection<IWorkflowInstance> engagements = (Collection<IWorkflowInstance>) BCInstance.getLinkedWorkflowInstances("Engagement");
                        if(engagements!=null && !engagements.isEmpty()){
                            for(IWorkflowInstance engagementInstance : engagements) {
                                if(engagementInstance.getValue("DocumentState").equals("Engagement validé")){
                                    rap = engagementInstance.getValue("ResteAPayer")!=null ? ((Number)engagementInstance.getValue("ResteAPayer")).doubleValue() : 0.0D;
                                    if(rap == 0){
                                        getResourceController().alert("Action impossible : il ya aucun reste à payer");
                                        return false;
                                    }
                                    if(((Number)getWorkflowInstance().getValue("TotalFactureTTC")).doubleValue() > rap){
                                        getResourceController().alert("Le montant de paiement est supérieur a le reste a payé de l'engagement");
                                        return false;
                                    }
                                    IWorkflow iWorkflow = WorkflowsService.getWorflow("Budget", "Paiement_1.0");
                                    IWorkflowInstance paiementworkflowInstance = getWorkflowModule().createWorkflowInstance(this.loggedOnContext, iWorkflow, "");
                                    paiementworkflowInstance.setValue("AnneeBudgetaire", engagementInstance.getValue("AnneeBudgetaire"));
                                    paiementworkflowInstance.setValue("Fournisseur", engagementInstance.getValue("Fournisseur"));
                                    paiementworkflowInstance.setValue("NatureBudget", engagementInstance.getValue("NatureBudget"));
                                    paiementworkflowInstance.setValue("ENGAGEMENT_INSTANCE", engagementInstance);
                                    paiementworkflowInstance.setValue("MontantAPayer", getWorkflowInstance().getValue("TotalFactureTTC"));
                                    paiementworkflowInstance.save(this.loggedOnContext);
                                    getWorkflowInstance().addLinkedWorkflowInstance("Paiement" ,  paiementworkflowInstance);
                                    getWorkflowInstance().save(this.loggedOnContext);
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }
}
