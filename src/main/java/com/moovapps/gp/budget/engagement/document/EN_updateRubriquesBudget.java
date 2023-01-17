package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.budget.helpers.calculate;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class EN_updateRubriquesBudget extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;

    private IContext sysAdminContext = null;

    private String anneeBudgetaire = null;

    private String RubriqueBudgetaire = null;

    private String typeBudget = "Dépenses";

    private IStorageResource sto_natureBudget = null;

    private BigDecimal montantEngager = BigDecimal.ZERO;

    private BigDecimal totalengagement_RB = BigDecimal.ZERO;

    private BigDecimal totalpaiement_RB = BigDecimal.ZERO;

    private BigDecimal rapLibere_RB = BigDecimal.ZERO;

    private BigDecimal resteAPayer_RB = BigDecimal.ZERO;

    private BigDecimal creditsouvertsCP = BigDecimal.ZERO;

    private BigDecimal totalmontantAnnule = BigDecimal.ZERO;

    private BigDecimal disponible = BigDecimal.ZERO;

    private BigDecimal montantPaye = BigDecimal.ZERO;


    public boolean onBeforeSubmit(IAction action) {
        try {
            IWorkflowInstance parentInstance = getWorkflowInstance().getParentInstance();
            if (action.getName().equals("Accepter")) {
                this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
                this.sto_natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                this.RubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
                this.montantEngager = castToBigDecimal(getWorkflowInstance().getValue("MontantAImputer"));
                this.montantPaye = getWorkflowInstance().getValue("MontantPaye") !=null ? castToBigDecimal(getWorkflowInstance().getValue("MontantPaye")) : BigDecimal.ZERO;
                this.totalmontantAnnule = getWorkflowInstance().getValue("MontantTotalAnnule") != null ? castToBigDecimal(getWorkflowInstance().getValue("MontantTotalAnnule")) : BigDecimal.ZERO;
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

                if (iLinkedResource.getValue("Disponible") != null && montantEngager.compareTo(castToBigDecimal(iLinkedResource.getValue("Disponible"))) > 0) {
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_DISPO_LOWER"));
                    return false;
                }
                this.creditsouvertsCP = castToBigDecimal(iLinkedResource.getValue("CreditsOuvertsCP"));
                this.totalengagement_RB = iLinkedResource.getValue("TotalDesEngagements") != null ? castToBigDecimal(iLinkedResource.getValue("TotalDesEngagements")) : BigDecimal.ZERO;
                this.resteAPayer_RB = iLinkedResource.getValue("RAP_CURRENT") != null ? castToBigDecimal(iLinkedResource.getValue("RAP_CURRENT")) : BigDecimal.ZERO;
                BigDecimal totalAnnule_RB =  iLinkedResource.getValue("TotalAnnulationDiminution") != null ? castToBigDecimal(iLinkedResource.getValue("TotalAnnulationDiminution")) : BigDecimal.ZERO;
                this.disponible = this.creditsouvertsCP.subtract(this.totalengagement_RB.add(this.montantEngager)).add(totalAnnule_RB);
                iLinkedResource.setValue("TotalDesEngagements", this.totalengagement_RB.add(this.montantEngager));
                iLinkedResource.setValue("Disponible", this.disponible);
                iLinkedResource.setValue("RAP_CURRENT", this.resteAPayer_RB.add(this.montantEngager));
                iLinkedResource.save(this.sysAdminContext);
                iLinkedResource.getParentInstance().save(this.sysAdminContext);
                getWorkflowInstance().setValue("ResteAPayer" ,this.montantEngager.subtract(this.totalmontantAnnule).subtract(this.montantPaye));
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

