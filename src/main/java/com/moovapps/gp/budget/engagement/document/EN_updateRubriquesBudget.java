package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.budget.helpers.calculate;
import com.moovapps.gp.services.WorkflowsService;

import java.util.Collection;

public class EN_updateRubriquesBudget extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;

    private IContext sysAdminContext = null;

    private String anneeBudgetaire = null;

    private String RubriqueBudgetaire = null;

    private String typeBudget = "Dépenses";

    private IStorageResource sto_natureBudget = null;

    private double montantEngager = 0.0D;

    private double totalengagement_RB = 0.0D;

    private double totalpaiement_RB = 0.0D;

    private double rapLibere_RB = 0.0D;

    private double resteAPayer_RB = 0.0D;

    private double creditsouvertsCP = 0.0D;

    private double totalmontantAnnule = 0.0D;

    private double disponible = 0.0D;

    private double montantPaye = 0.0D;


    public boolean onBeforeSubmit(IAction action) {
        try {
            IWorkflowInstance parentInstance = getWorkflowInstance().getParentInstance();
            if (action.getName().equals("Accepter")) {
                this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
                this.sto_natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                this.RubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
                this.montantEngager = ((Number) getWorkflowInstance().getValue("MontantAImputer")).doubleValue();
                this.montantPaye = getWorkflowInstance().getValue("MontantPaye") !=null ? ((Number) getWorkflowInstance().getValue("MontantPaye")).doubleValue() : 0.0D;
                this.totalmontantAnnule = getWorkflowInstance().getValue("MontantTotalAnnule") != null ? ((Number) getWorkflowInstance().getValue("MontantTotalAnnule")).doubleValue() : 0.0D;
                Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget();
                if (linkedResources == null || linkedResources.isEmpty()) {
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                    return false;
                }
                ILinkedResource iLinkedResource = linkedResources.stream()
                        .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(this.RubriqueBudgetaire))
                        .findFirst()
                        .orElse(null);

                if(iLinkedResource==null){
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                    return false;
                }

                if (iLinkedResource.getValue("Disponible") != null && montantEngager > ((Number) iLinkedResource.getValue("Disponible")).doubleValue()) {
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_DISPO_LOWER"));
                    return false;
                }
                this.creditsouvertsCP = ((Number) iLinkedResource.getValue("CreditsOuvertsCP")).doubleValue();
                this.totalengagement_RB = iLinkedResource.getValue("TotalDesEngagements") != null ? ((Number) iLinkedResource.getValue("TotalDesEngagements")).doubleValue() : 0.0D;
                this.resteAPayer_RB = iLinkedResource.getValue("RAP_CURRENT") != null ? ((Number) iLinkedResource.getValue("RAP_CURRENT")).doubleValue() : 0.0D;
                double totalAnnule_RB =  iLinkedResource.getValue("TotalAnnulationDiminution") != null ? ((Number) iLinkedResource.getValue("TotalAnnulationDiminution")).doubleValue() : 0.0D;
                this.disponible = this.creditsouvertsCP -(this.totalengagement_RB + this.montantEngager) + totalAnnule_RB ;
                iLinkedResource.setValue("TotalDesEngagements", this.totalengagement_RB + this.montantEngager);
                iLinkedResource.setValue("Disponible", this.disponible);
                iLinkedResource.setValue("RAP_CURRENT", this.resteAPayer_RB + this.montantEngager);
                iLinkedResource.save(this.sysAdminContext);
                iLinkedResource.getParentInstance().save(this.sysAdminContext);
                getWorkflowInstance().setValue("ResteAPayer" ,this.montantEngager - this.totalmontantAnnule  - this.montantPaye);
                getWorkflowInstance().save("ResteAPayer");
                if(parentInstance!=null){
                    if(parentInstance.getWorkflow().getWorkflowContainer().getName().equals("BC")){
                        ITaskInstance taskInstance = parentInstance.getCurrentTaskInstance(this.sysAdminContext);
                        if(taskInstance!=null){
                            Collection<IOperator> iOperators = (Collection<IOperator>) taskInstance.getOperators();
                            if(iOperators!=null && !iOperators.isEmpty()){
                                IStorageResource rb = (IStorageResource) iLinkedResource.getValue("RubriqueBudgetaire");
                                if(rb!=null){
                                    parentInstance.setValue("RubriqueBudgetaire" , this.RubriqueBudgetaire);
                                    parentInstance.setValue("CodeRubrique" , rb.getValue("CodeRubrique"));
                                    parentInstance.setValue("Annee" , this.anneeBudgetaire);
                                    parentInstance.setValue("ArticleBudget" , rb.getValue("ArticleBudget")!=null ? ((IStorageResource)rb.getValue("ArticleBudget")).getValue("sys_Title") : null);
                                    parentInstance.setValue("Paragraphe" , rb.getValue("Paragraphe")!=null ? ((IStorageResource)rb.getValue("Paragraphe")).getValue("sys_Title") : null);
                                    parentInstance.save(getWorkflowModule().getContext(iOperators.iterator().next()));
                                }
                                WorkflowsService.executeAction(parentInstance , getWorkflowModule().getContext(iOperators.iterator().next()) , "Valider" , "Engagement validé");
                            }
                        }
                    }
                }
            }
            else if(action.getName().equals("Refuser")){
                if(parentInstance!=null){
                    if(parentInstance.getWorkflow().getWorkflowContainer().getName().equals("BC")){
                        ITaskInstance taskInstance = parentInstance.getCurrentTaskInstance(this.sysAdminContext);
                        if(taskInstance!=null){
                            Collection<IOperator> iOperators = (Collection<IOperator>) taskInstance.getOperators();
                            if(iOperators!=null && !iOperators.isEmpty()){
                                WorkflowsService.executeAction(parentInstance , getWorkflowModule().getContext(iOperators.iterator().next()) , "Retour" , "Engagement refusé");
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




    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget() {
        Collection<ILinkedResource> linkedResources = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), this.typeBudget);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.sto_natureBudget);
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

