package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.budget.helpers.calculate;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.util.Collection;

public class DiminutionEngagement extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private String anneeBudgetaire = null;

    private String RubriqueBudgetaire = null;

    private String typeBudget = "Dépenses";

    private IStorageResource rubriqueBudget = null;

    private IStorageResource natureBudget = null;

    private double montantEngager = 0.0D;


    private double rb_creditsOuvertsCP = 0.0D;

    private double rb_disponible = 0.0D;

    private double rb_totalAnnule = 0.0D;

    private double totalmontantAnnule = 0.0D;

    private double montantAnnuler = 0.0D;

    private double resteAPayer = 0.0D;

    private double resteAPayer_RB = 0.0D;

    private double montantPaye = 0.0D;


    @Override
    public boolean onAfterLoad() {
        try{
            ITaskInstance taskInstance = getWorkflowInstance().getCurrentTaskInstance(sysAdminContext);
            if (taskInstance != null) {
                IAction action = getWorkflowModule().getAction(sysAdminContext, taskInstance.getTask(), "RAP");
                if(action!=null){
                    CtlButton ctlButton = getResourceController().getButton(action.getLabel(), 2);
                    if(ctlButton!=null){
                        ctlButton.setHidden(true);
                    }
                }
            }
        }
        catch (Exception e){
                e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {

        try {
            if (action.getName().equals("SolderDiminuer")) {
                this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
                this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                this.RubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
                Collection<ILinkedResource> annulationlinkedResources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("CANCEL_Engagement");
                this.totalmontantAnnule = getWorkflowInstance().getValue("MontantTotalAnnule") != null ? ((Number) getWorkflowInstance().getValue("MontantTotalAnnule")).doubleValue() : 0.0D;
                this.resteAPayer = ((Number) getWorkflowInstance().getValue("ResteAPayer")).doubleValue();
                this.montantEngager = ((Number) getWorkflowInstance().getValue("MontantAImputer")).doubleValue();
                this.montantPaye = getWorkflowInstance().getValue("MontantPaye") !=null ? ((Number) getWorkflowInstance().getValue("MontantPaye")).doubleValue() : 0.0D;
                Collection<ILinkedResource> rubriquesLinkedResources = getRubriqueBudgetByCurrentBudget();
                if (rubriquesLinkedResources == null || rubriquesLinkedResources.isEmpty()) {
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                    return false;
                }
                if (annulationlinkedResources != null && !annulationlinkedResources.isEmpty()) {
                    for (ILinkedResource iLinkedResource : annulationlinkedResources) {
                        if (iLinkedResource.getValue("FLAG").equals(false)) {
                            this.montantAnnuler += ((Number) iLinkedResource.getValue("MontantAnnule")).doubleValue();
                            iLinkedResource.setValue("FLAG", true);
                            iLinkedResource.save(sysAdminContext);
                        }
                    }
                    ILinkedResource rubResource = rubriquesLinkedResources.stream()
                            .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(this.RubriqueBudgetaire))
                            .findFirst()
                            .orElse(null);

                    if (rubResource == null) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                        return false;
                    }
                    if (this.montantAnnuler > this.resteAPayer) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RAP_LOWER"));
                        return false;
                    }
                    this.rb_creditsOuvertsCP = ((Number) rubResource.getValue("CreditsOuvertsCP")).doubleValue();
                    this.rb_totalAnnule = rubResource.getValue("TotalAnnulationDiminution") != null ? ((Number) rubResource.getValue("TotalAnnulationDiminution")).doubleValue() : 0.0D;
                    this.resteAPayer_RB = rubResource.getValue("RAP_CURRENT") != null ? ((Number) rubResource.getValue("RAP_CURRENT")).doubleValue() : 0.0D;
                    double totalengagement_RB =  rubResource.getValue("TotalDesEngagements") != null ? ((Number) rubResource.getValue("TotalDesEngagements")).doubleValue() : 0.0D;
                    this.rb_totalAnnule += this.montantAnnuler;
                    this.rb_disponible = this.rb_creditsOuvertsCP -totalengagement_RB +rb_totalAnnule;

                    rubResource.setValue("TotalAnnulationDiminution", this.rb_totalAnnule);
                    rubResource.setValue("Disponible", this.rb_disponible);
                    rubResource.setValue("RAP_CURRENT", this.resteAPayer_RB - this.montantAnnuler);
                    rubResource.save(this.sysAdminContext);
                    rubResource.getParentInstance().save(this.sysAdminContext);
                }
                getWorkflowInstance().setValue("MontantTotalAnnule", this.totalmontantAnnule + this.montantAnnuler);
                getWorkflowInstance().setValue("ResteAPayer" ,this.montantEngager - (this.totalmontantAnnule + this.montantAnnuler) - this.montantPaye);
                getWorkflowInstance().save(sysAdminContext);
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
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
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
