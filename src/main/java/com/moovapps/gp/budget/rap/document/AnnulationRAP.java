package com.moovapps.gp.budget.rap.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.budget.helpers.calculate;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.util.Collection;

public class AnnulationRAP extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private String anneeBudgetaire = null;

    private String RubriqueBudgetaire = null;

    private String typeBudget = "Dépenses";

    private IStorageResource rubriqueBudget = null;

    private IStorageResource natureBudget = null;


    private double rapLibere_RB = 0.0D;

    private double totalmontantAnnule = 0.0D;

    private double resteAPayer_RB = 0.0D;

    private double montantAnnuler = 0.0D;

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
                Collection<ILinkedResource> annulationlinkedResources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("CANCEL_RAP");
                this.totalmontantAnnule = getWorkflowInstance().getValue("MontantTotalAnnule") != null ? ((Number) getWorkflowInstance().getValue("MontantTotalAnnule")).doubleValue() : 0.0D;
                double rapN = getWorkflowInstance().getValue("ResteAPayerN1")!=null ? ((Number)getWorkflowInstance().getValue("ResteAPayerN1")).doubleValue() : 0.0D;
                Collection<ILinkedResource> rubriquesLinkedResources = getRubriqueBudgetByCurrentBudget();
                if (rubriquesLinkedResources == null || rubriquesLinkedResources.isEmpty()) {
                    getResourceController().alert("Action impossible : La rubrique budgétaire n'est pas existe , Merci de contacter votre administrateur !!");
                    return false;
                }
                if (annulationlinkedResources != null && !annulationlinkedResources.isEmpty()) {
                    for (ILinkedResource iLinkedResource : annulationlinkedResources) {
                        if (iLinkedResource.getValue("FLAG").equals(false)) {
                            montantAnnuler += ((Number) iLinkedResource.getValue("MontantAnnule")).doubleValue();
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
                    if (this.montantAnnuler > rapN) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RAPN_LOWER"));
                        return false;
                    }
                    this.rapLibere_RB = ((Number) rubResource.getValue("RAP_libere")).doubleValue();
                    this.resteAPayer_RB = rubResource.getValue("RAP_CURRENT") != null ? ((Number) rubResource.getValue("RAP_CURRENT")).doubleValue() : 0.0D;
                    this.totalmontantAnnule+=this.montantAnnuler;
                    rubResource.setValue("RAP_libere", this.rapLibere_RB + this.montantAnnuler);
                    rubResource.setValue("RAP_CURRENT", this.resteAPayer_RB - this.montantAnnuler);
                    rubResource.save(this.sysAdminContext);
                    rubResource.getParentInstance().save(this.sysAdminContext);

                }
                double rp = rapN - this.montantAnnuler;
                getWorkflowInstance().setValue("MontantTotalAnnule", this.totalmontantAnnule);
                getWorkflowInstance().setValue("ResteAPayer", rp);
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
                linkedResources = (Collection<ILinkedResource>) workflowInstances.iterator().next().getLinkedResources("RB_Budget_Tab");
            return linkedResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
