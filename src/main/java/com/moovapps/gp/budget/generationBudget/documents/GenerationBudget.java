package com.moovapps.gp.budget.generationBudget.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.BudgetUtils.isDepenses;
import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class GenerationBudget extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();
    protected IContext loggedOnContext = null;
    private String anneeBudgetaire = null;
    private String typeBudget = null;
    private IStorageResource natureBudget = null;

    public static ArrayList<String> systemButtonsList = new ArrayList<>(Arrays.asList(
            Const.ACTION_RETOUR_MODIFICATION2_GB,
            Const.ACTION_GENERER_VERSION_GB,
            Const.ACTION_BUDGET_REFUSER_GB,
            Const.ACTION_BUDGET_VALIDER_GB,
            Const.ACTION_CLOTURER_BUDGET_GB,
            Const.ACTION_REFUSER_GB));

    public boolean onAfterLoad() {
        try {
            ITaskInstance taskInstance = getWorkflowInstance().getCurrentTaskInstance(sysAdminContext);
            String documentState = (String) getWorkflowInstance().getValue("DocumentState");
            IWorkflowInstance workflowInstanceBudgetV1 = (IWorkflowInstance) getWorkflowInstance().getValue("URIBudgetV1");
            for (int i = 0; i < systemButtonsList.size(); i++) {
                if (taskInstance != null) {
                    IAction action = getWorkflowModule().getAction(sysAdminContext, taskInstance.getTask(), systemButtonsList.get(i));
                    if (action != null) {
                        CtlButton ctlButton = getResourceController().getButton(action.getLabel(), 2);
                        if (action.getName().equals(Const.ACTION_GENERER_VERSION_GB) && (documentState.equals("Prévisions validées") || documentState.equals("Budget ouvert (Nouvelle version en cours)"))) {
                            ctlButton.setHidden(true);
                            continue;
                        }
                        if (action.getName().equals(Const.ACTION_CLOTURER_BUDGET_GB) && documentState.equals("Prévisions validées")) {
                            ctlButton.setHidden(true);
                            continue;
                        }
                        if (action.getName().equals(Const.ACTION_RETOUR_MODIFICATION2_GB) && (documentState.equals("Budget ouvert (Nouvelle version en cours)") || documentState.equals("Budget ouvert"))) {
                            ctlButton.setHidden(true);
                            continue;
                        }
                        if (action.getName().equals(Const.ACTION_BUDGET_VALIDER_GB) && (documentState.equals("Budget ouvert (Nouvelle version en cours)") || documentState.equals("Budget ouvert"))) {
                            ctlButton.setHidden(true);
                            continue;
                        }
                        if (action.getName().equals(Const.ACTION_BUDGET_REFUSER_GB) && (documentState.equals("Budget ouvert (Nouvelle version en cours)") || documentState.equals("Budget ouvert") || workflowInstanceBudgetV1!=null)) {
                            ctlButton.setHidden(true);
                            continue;
                        }

                        if (action.getName().equals(Const.ACTION_REFUSER_GB) && workflowInstanceBudgetV1!=null) {
                            ctlButton.setHidden(true);
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public boolean onBeforeSubmit(IAction action) {
        try {
            this.loggedOnContext = getWorkflowModule().getLoggedOnUserContext();
            this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            this.typeBudget = (String) getWorkflowInstance().getValue("TypeBudget");
            if (action.getName().equals(Const.ACTION_BUDGET_VALIDER_GB)) {
                if(isDepenses(this.typeBudget)){
                    MAJMontantDisponible();
                }

            } else if (action.getName().equals(Const.ACTION_BUDGET_REFUSER_GB)) {
                IWorkflowInstance workflowInstanceBudgetV1 = (IWorkflowInstance) getWorkflowInstance().getValue("BudgetV1");
                if (workflowInstanceBudgetV1 != null) {
                    workflowInstanceBudgetV1.setValue("DocumentState", "Budget ouvert");
                    workflowInstanceBudgetV1.save(this.loggedOnContext);
                }
            } else if (action.getName().equals(Const.ACTION_GENERER_VERSION_GB)) {
                IWorkflowInstance workflowInstanceCible = WorkflowsService.duplicateWorkflowInstance(getWorkflowInstance(), this.loggedOnContext);
                workflowInstanceCible.setValue("URIBudgetV1", getWorkflowInstance());
                workflowInstanceCible.setValue("VersionDuBudget", WorkflowsService.generateVersion(getWorkflowInstance()));
                workflowInstanceCible.save(this.sysAdminContext);
                getResourceController().alert("Une nouvelle version du budget a été crée!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);

    }


    private BigDecimal MAJMontantDisponible() {
        BigDecimal montantTotal = BigDecimal.ZERO;
        try {
            Collection<ILinkedResource> linkedResourcesGB = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("RB_Budget_Tab");
            BigDecimal CreditsOuvertsCP =  BigDecimal.ZERO;
            BigDecimal TotalEngagement =  BigDecimal.ZERO;
            BigDecimal TotalAnnulation =  BigDecimal.ZERO;
            BigDecimal disponible =  BigDecimal.ZERO;
            for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                CreditsOuvertsCP = castToBigDecimal(linkedResourceGB.getValue("CreditsOuvertsCP"));
                TotalEngagement = castToBigDecimal(linkedResourceGB.getValue("TotalDesEngagements"));
                TotalAnnulation = castToBigDecimal(linkedResourceGB.getValue("TotalAnnulationDiminution"));
                disponible = CreditsOuvertsCP.subtract(TotalEngagement).add(TotalAnnulation);
                linkedResourceGB.setValue("Disponible", disponible);
                linkedResourceGB.save(this.sysAdminContext);
            }
            getWorkflowInstance().save(this.sysAdminContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return montantTotal;
    }
}
