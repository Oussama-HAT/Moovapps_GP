package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.utils.CollectionUtils;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class DiminutionEngagement extends BaseDocumentExtension {

    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private String anneeBudgetaire = null;

    private String RubriqueBudgetaire = null;

    private String typeBudget = "Dépenses";

    private IStorageResource rubriqueBudget = null;

    private IStorageResource natureBudget = null;

    private BigDecimal montantEngager = BigDecimal.ZERO;


    private BigDecimal rb_creditsOuvertsCP =BigDecimal.ZERO;

    private BigDecimal rb_disponible = BigDecimal.ZERO;

    private BigDecimal rb_totalAnnule =BigDecimal.ZERO;

    private BigDecimal totalmontantAnnule = BigDecimal.ZERO;

    private BigDecimal montantAnnuler = BigDecimal.ZERO;

    private BigDecimal resteAPayer = BigDecimal.ZERO;

    private BigDecimal resteAPayer_RB = BigDecimal.ZERO;

    private BigDecimal montantPaye =BigDecimal.ZERO;


    @Override
    public boolean onAfterLoad() {
        try{
            ITaskInstance taskInstance = getWorkflowInstance().getCurrentTaskInstance(sysAdminContext);
            if (taskInstance != null) {
                IAction action = getWorkflowModule().getAction(sysAdminContext, taskInstance.getTask(), "RAP");
                if(action!=null){
                    CtlButton ctlButton = getResourceController().getButton(action.getLabel(), 2);
                    if(ctlButton!=null){
                        BigDecimal rap = castToBigDecimal(getWorkflowInstance().getValue("ResteAPayer"));
                        if(rap.compareTo(BigDecimal.ZERO)>0)
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
                Collection<ILinkedResource> annulationlinkedResources = CollectionUtils.cast(getWorkflowInstance().getLinkedResources("CANCEL_Engagement") , ILinkedResource.class);
                this.totalmontantAnnule = getWorkflowInstance().getValue("MontantTotalAnnule") != null ? castToBigDecimal(getWorkflowInstance().getValue("MontantTotalAnnule")) : BigDecimal.ZERO;
                this.resteAPayer = castToBigDecimal(getWorkflowInstance().getValue("ResteAPayer"));
                this.montantEngager = castToBigDecimal(getWorkflowInstance().getValue("MontantAImputer"));
                this.montantPaye = getWorkflowInstance().getValue("MontantPaye") !=null ? castToBigDecimal(getWorkflowInstance().getValue("MontantPaye")): BigDecimal.ZERO;
                Collection<ILinkedResource> rubriquesLinkedResources = getRubriqueBudgetByCurrentBudget();
                if (rubriquesLinkedResources == null || rubriquesLinkedResources.isEmpty()) {
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                    return false;
                }
                if (annulationlinkedResources != null && !annulationlinkedResources.isEmpty()) {
                    for (ILinkedResource iLinkedResource : annulationlinkedResources) {
                        if (iLinkedResource.getValue("FLAG").equals(false)) {
                            this.montantAnnuler = this.montantAnnuler.add(castToBigDecimal(iLinkedResource.getValue("MontantAnnule")));
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
                    if (this.montantAnnuler.compareTo(this.resteAPayer) > 0) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RAP_LOWER"));
                        return false;
                    }
                    this.rb_creditsOuvertsCP = castToBigDecimal(rubResource.getValue("CreditsOuvertsCP"));
                    this.rb_totalAnnule = rubResource.getValue("TotalAnnulationDiminution") != null ? castToBigDecimal(rubResource.getValue("TotalAnnulationDiminution")) : BigDecimal.ZERO;
                    this.resteAPayer_RB = rubResource.getValue("RAP_CURRENT") != null ? castToBigDecimal(rubResource.getValue("RAP_CURRENT")) : BigDecimal.ZERO;
                    BigDecimal totalengagement_RB =  rubResource.getValue("TotalDesEngagements") != null ? castToBigDecimal(rubResource.getValue("TotalDesEngagements")) : BigDecimal.ZERO;
                    this.rb_totalAnnule = this.rb_totalAnnule.add(this.montantAnnuler);
                    this.rb_disponible = this.rb_creditsOuvertsCP.subtract(totalengagement_RB).add(rb_totalAnnule);

                    rubResource.setValue("TotalAnnulationDiminution", this.rb_totalAnnule);
                    rubResource.setValue("Disponible", this.rb_disponible);
                    rubResource.setValue("RAP_CURRENT", this.resteAPayer_RB.subtract(this.montantAnnuler));
                    rubResource.save(this.sysAdminContext);
                    rubResource.getParentInstance().save(this.sysAdminContext);
                }
                getWorkflowInstance().setValue("MontantTotalAnnule", this.totalmontantAnnule.add(this.montantAnnuler));
                getWorkflowInstance().setValue("ResteAPayer" ,this.montantEngager.subtract(this.totalmontantAnnule.add(this.montantAnnuler)).subtract(this.montantPaye));
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
