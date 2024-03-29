package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class Verification extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    @Override
    public boolean onBeforeSubmit(IAction action) {
        try{
            if(action.getName().equals("Refuser")){
                Collection<IWorkflowInstance> engagements = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Engagement");
                BigDecimal montantAnnuler = BigDecimal.ZERO;
                if(engagements!=null && !engagements.isEmpty()){
                    for(IWorkflowInstance engagementInstance : engagements){
                        if(engagementInstance.getValue("DocumentState").equals("Engagement validé")){
                            String rb = (String) engagementInstance.getValue("RubriqueBudgetaire");
                            BigDecimal montantpaye = engagementInstance.getValue("MontantPaye")!=null ? castToBigDecimal(engagementInstance.getValue("MontantPaye")) : BigDecimal.ZERO;
                            if(montantpaye.compareTo(BigDecimal.ZERO) > 0){
                                getResourceController().alert("Action impossible : Le bon de commande contient un engagemet payé");
                                return false;
                            }
                            Collection<ILinkedResource> annulationlinkedResources = (Collection<ILinkedResource>) engagementInstance.getLinkedResources("CANCEL_Engagement");
                            BigDecimal totalmontantAnnule = engagementInstance.getValue("MontantTotalAnnule") != null ? castToBigDecimal(engagementInstance.getValue("MontantTotalAnnule")): BigDecimal.ZERO;
                            BigDecimal resteAPayer = BigDecimal.ZERO;
                            Collection<ILinkedResource> rubriquesLinkedResources = getRubriqueBudgetByCurrentBudget((String)engagementInstance.getValue("AnneeBudgetaire") , (IStorageResource) engagementInstance.getValue("NatureBudget"));
                            if (rubriquesLinkedResources == null || rubriquesLinkedResources.isEmpty()) {
                                getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                                return false;
                            }
                            if (annulationlinkedResources != null && !annulationlinkedResources.isEmpty()) {
                                for (ILinkedResource iLinkedResource : annulationlinkedResources) {
                                    if (iLinkedResource.getValue("FLAG").equals(false)) {
                                        montantAnnuler = montantAnnuler.add(castToBigDecimal(iLinkedResource.getValue("MontantAnnule")));
                                    }
                                }
                                ILinkedResource rubResource = rubriquesLinkedResources.stream()
                                        .filter(obj -> ((IStorageResource) obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(rb))
                                        .findFirst()
                                        .orElse(null);

                                if (rubResource == null) {
                                    getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                                    return false;
                                }
                            }
                            BigDecimal montantBc =castToBigDecimal(engagementInstance.getValue("MontantAImputer"));
                            //resteAPayer = montantBc - (totalmontantAnnule+montantAnnuler) - montantpaye;
                            resteAPayer = montantBc.subtract(totalmontantAnnule.add(montantAnnuler)).subtract(montantpaye);
                            ILinkedResource iLinkedResource = engagementInstance.createLinkedResource("CANCEL_Engagement");
                            iLinkedResource.setValue("id" , UUID.randomUUID().toString());
                            iLinkedResource.setValue("DateDiminution" , new Date());
                            iLinkedResource.setValue("MontantAnnule" , resteAPayer);
                            iLinkedResource.setValue("Motif" , "Annulation de bon de commande");
                            iLinkedResource.save(this.sysAdminContext);
                            engagementInstance.addLinkedResource(iLinkedResource);
                            engagementInstance.save(this.sysAdminContext);

                            Collection<ITaskInstance> taskInstances = (Collection<ITaskInstance>) getWorkflowModule().getStartedTaskInstances(this.sysAdminContext,engagementInstance);
                            if(taskInstances!=null && !taskInstances.isEmpty()){
                                Collection<IOperator> iOperators = (Collection<IOperator>) taskInstances.iterator().next().getOperators();
                                if(iOperators!=null && !iOperators.isEmpty()){
                                    WorkflowsService.executeAction(engagementInstance , getWorkflowModule().getContext(iOperators.iterator().next()) , "SolderDiminuer" , "Annulation de bon de commande");
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

        private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget(String anneeBudgetaire, IStorageResource natureBudget) {
            Collection<ILinkedResource> linkedResources = null;
            try {
                IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
                viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), anneeBudgetaire);
                viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Dépenses");
                viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), natureBudget);
                viewController.addEqualsConstraint("DocumentState", "Budget ouvert");
                Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
                if (workflowInstances != null && !workflowInstances.isEmpty())
                    linkedResources = (Collection<ILinkedResource>) workflowInstances.iterator().next().getLinkedResources("RB_Budget_Tab");
                return linkedResources;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
}
