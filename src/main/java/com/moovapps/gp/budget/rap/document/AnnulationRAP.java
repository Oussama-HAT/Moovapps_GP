package com.moovapps.gp.budget.rap.document;

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

public class AnnulationRAP extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private String anneeBudgetaire = null;

    private String RubriqueBudgetaire = null;

    private final String typeBudget = "Dépenses";

    private IStorageResource rubriqueBudget = null;

    private IStorageResource natureBudget = null;

    private BigDecimal rapLibere_RB = BigDecimal.ZERO;

    private BigDecimal totalmontantAnnule = BigDecimal.ZERO;

    private BigDecimal resteAPayer_RB = BigDecimal.ZERO;

    private BigDecimal montantAnnuler = BigDecimal.ZERO;

    @Override
    public boolean onAfterLoad() {
        try{
            ITaskInstance taskInstance = getWorkflowInstance().getCurrentTaskInstance(sysAdminContext);
            if (taskInstance != null) {
                IAction action = getWorkflowModule().getAction(sysAdminContext, taskInstance.getTask(), "Reporter");
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
                this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaireDestination");
                this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                this.RubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
                this.totalmontantAnnule = getWorkflowInstance().getValue("MontantTotalAnnule") != null ? castToBigDecimal(getWorkflowInstance().getValue("MontantTotalAnnule")): BigDecimal.ZERO;
                Collection<ILinkedResource> annulationlinkedResources = CollectionUtils.cast(getWorkflowInstance().getLinkedResources("CANCEL_RAP") , ILinkedResource.class);
                BigDecimal rapN = getWorkflowInstance().getValue("ResteAPayerN1")!=null ? castToBigDecimal(getWorkflowInstance().getValue("ResteAPayerN1")): BigDecimal.ZERO;
                Collection<ILinkedResource> rubriquesLinkedResources = getRubriqueBudgetByCurrentBudget();
                if (rubriquesLinkedResources == null || rubriquesLinkedResources.isEmpty()) {
                    getResourceController().alert("Action impossible : La rubrique budgétaire n'est pas existe , Merci de contacter votre administrateur !!");
                    return false;
                }
                if (annulationlinkedResources != null && !annulationlinkedResources.isEmpty()) {
                    for (ILinkedResource iLinkedResource : annulationlinkedResources) {
                        if (iLinkedResource.getValue("FLAG").equals(false)) {
                            this.montantAnnuler = this.montantAnnuler.add(castToBigDecimal(iLinkedResource.getValue("MontantAnnule")));
                            iLinkedResource.setValue("FLAG", true);
                            iLinkedResource.save(this.sysAdminContext);
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
                    if (this.montantAnnuler.compareTo(rapN) > 0) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RAPN_LOWER"));
                        return false;
                    }
                    this.rapLibere_RB = castToBigDecimal(rubResource.getValue("RAP_libere"));
                    this.resteAPayer_RB = rubResource.getValue("RAP_CURRENT") != null ? castToBigDecimal(rubResource.getValue("RAP_CURRENT")) : BigDecimal.ZERO;
                    this.totalmontantAnnule = this.totalmontantAnnule.add(this.montantAnnuler);
                    rubResource.setValue("RAP_libere", this.rapLibere_RB.add(this.montantAnnuler));
                    rubResource.setValue("RAP_CURRENT", this.resteAPayer_RB.subtract(this.montantAnnuler));
                    rubResource.save(this.sysAdminContext);
                    rubResource.getParentInstance().save(this.sysAdminContext);

                }
                getWorkflowInstance().setValue("MontantTotalAnnule", this.totalmontantAnnule);
                getWorkflowInstance().setValue("ResteAPayer", rapN.subtract(this.montantAnnuler));
                getWorkflowInstance().save(this.sysAdminContext);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);

    }

    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget() {
        Collection<ILinkedResource> linkedResources = null;
        Collection<IWorkflowInstance> workflowInstances =null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), this.typeBudget);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addEqualsConstraint("DocumentState", "Budget ouvert");
            workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            if (workflowInstances != null && !workflowInstances.isEmpty())
                linkedResources = CollectionUtils.cast(workflowInstances.iterator().next().getLinkedResources("RB_Budget_Tab"), ILinkedResource.class);
            return linkedResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
