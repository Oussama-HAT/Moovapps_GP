package com.moovapps.gp.budget.paiement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

public class verifierBudget extends BaseDocumentExtension {

    private String anneeBudgetaire = null;
    private IStorageResource natureBudget = null;
    private IStorageResource fournisseur = null;
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    @Override
    public boolean onBeforeSave() {
        this.anneeBudgetaire = (String)getWorkflowInstance().getValue("AnneeBudgetaire");
        this.natureBudget = (IStorageResource)getWorkflowInstance().getValue("NatureBudget");
        if(getRubriqueBudgetByCurrentBudget()<=0){
            getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
            return false;
        }
        return super.onBeforeSave();
    }

    private int getRubriqueBudgetByCurrentBudget() {
        int count =0;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Dépenses");
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addEqualsConstraint("DocumentState", "Budget ouvert");
            count = viewController.evaluateSize(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            return count;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
