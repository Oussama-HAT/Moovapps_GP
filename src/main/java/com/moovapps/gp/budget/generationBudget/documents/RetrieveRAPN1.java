package com.moovapps.gp.budget.generationBudget.documents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdp.utils.CollectionUtils;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.BudgetUtils.isDepenses;
import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class RetrieveRAPN1 extends BaseDocumentExtension {

    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();
    protected IContext loggedUserContext = DirectoryService.getLoggedOnContext();
    private String anneeBudgetaire = null;
    private String typeBudget = null;
    private IStorageResource natureBudget = null;

    @Override
    public boolean onBeforeSubmit(IAction action) {
        try {
            if(action.getName().equals(Const.ACTION_ENVOYER_VALIDATION_GB)){
                Collection<ILinkedResource> GB_linkedResources = CollectionUtils.cast(getWorkflowInstance().getLinkedResources("RB_Budget_Tab"), ILinkedResource.class);
                this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
                this.typeBudget = (String) getWorkflowInstance().getValue("TypeBudget");
                this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                String previousyear = String.valueOf(Integer.parseInt(this.anneeBudgetaire)-1);
                if(!isDepenses(this.typeBudget)){
                    return super.onBeforeSubmit(action);
                }
                IStorageResource rubriqueBudgetairePrevious = null;
                BigDecimal currentRAP, totalPaiement , totalEngagement , totalDiminution;
                if(GB_linkedResources==null || GB_linkedResources.isEmpty()){
                    getResourceController().alert("Introuvable !! ");
                    return false;
                }
                for (ILinkedResource linkedResourceGB: GB_linkedResources){
                    rubriqueBudgetairePrevious = getBudgetPreviousYear((IStorageResource) linkedResourceGB.getValue("RubriqueBudgetaire"), previousyear);
                    if(rubriqueBudgetairePrevious==null){
                        continue;
                    }
                    totalPaiement = castToBigDecimal(linkedResourceGB.getValue("TotalDesPaiements"));
                    totalEngagement = castToBigDecimal(linkedResourceGB.getValue("TotalDesEngagements"));
                    totalDiminution = castToBigDecimal(linkedResourceGB.getValue("TotalAnnulationDiminution"));
                    currentRAP =totalEngagement.subtract(totalDiminution).subtract(totalPaiement);
                    linkedResourceGB.setValue("RAPN1",castToBigDecimal(rubriqueBudgetairePrevious.getValue("RAP")));
                    linkedResourceGB.setValue("RAP_CURRENT",currentRAP.add(castToBigDecimal(rubriqueBudgetairePrevious.getValue("RAP"))));
                    linkedResourceGB.save(this.loggedUserContext);
                }
                getWorkflowInstance().save(this.loggedUserContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    public IStorageResource getBudgetPreviousYear(IStorageResource RubriqueBudgetaire, String PreviousYear) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            IViewController viewController = workflowModule.getViewController(DirectoryService.getSysAdminContext(), IResource.class);
            viewController.addEqualsConstraint("TypeBudget", this.typeBudget);
            viewController.addEqualsConstraint("NatureBudget", this.natureBudget);
            viewController.addEqualsConstraint("AnneeBudgetaire", PreviousYear);
            viewController.addEqualsConstraint("RubriqueBudgetaire", RubriqueBudgetaire);
            viewController.addGreaterConstraint("RAP", 0);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("ReferentielsBudget" , "Budget"));
            if(storageResources!=null && !storageResources.isEmpty()){
                return storageResources.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
