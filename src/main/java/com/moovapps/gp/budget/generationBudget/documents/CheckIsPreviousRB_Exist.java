package com.moovapps.gp.budget.generationBudget.documents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdp.utils.CollectionUtils;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;

import java.util.Collection;
import java.util.stream.Collectors;

public class CheckIsPreviousRB_Exist extends BaseDocumentExtension {

    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();
    private String anneeBudgetaire = null;
    private String typeBudget = null;
    private IStorageResource natureBudget = null;

    @Override
    public boolean onBeforeSubmit(IAction action) {
        try {
            if(action.getName().equals(Const.ACTION_ACCEPTER_GB)){
                Collection<ILinkedResource> GB_linkedResources = CollectionUtils.cast(getWorkflowInstance().getLinkedResources("RB_Budget_Tab"), ILinkedResource.class);
                this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
                this.typeBudget = (String) getWorkflowInstance().getValue("TypeBudget");
                this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                String previousyear = String.valueOf(Integer.parseInt(this.anneeBudgetaire)-1);
                Collection<IStorageResource> iStorageResources =getBudgetPreviousYear(previousyear);
                if(GB_linkedResources!=null && !GB_linkedResources.isEmpty()){
                    Collection<IStorageResource> iStorageResourceList = GB_linkedResources.stream().map(obj -> (IStorageResource)obj.getValue("RubriqueBudgetaire")).collect(Collectors.toList());
                    IStorageResource rubriqueBudgetaire= null;
                    for (ILinkedResource linkedResource: GB_linkedResources){
                        rubriqueBudgetaire = (IStorageResource) linkedResource.getValue("RubriqueBudgetaire");
                        if(rubriqueBudgetaire==null){
                            getResourceController().alert(getWorkflowModule().getStaticString("LG_RBN1_NOT_FOUND"));
                            return false;
                        }
                        if(iStorageResourceList!=null && !iStorageResourceList.isEmpty() && iStorageResources !=null && !iStorageResources.isEmpty() &&
                                !iStorageResourceList.containsAll(iStorageResources)){
                            getResourceController().alert(getWorkflowModule().getStaticString("LG_RBN1_NOT_FOUND"));
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    public Collection<IStorageResource> getBudgetPreviousYear(String PreviousYEAR){
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            IViewController viewController = workflowModule.getViewController(DirectoryService.getSysAdminContext(), IResource.class);
            viewController.addEqualsConstraint("TypeBudget", this.typeBudget);
            viewController.addEqualsConstraint("NatureBudget", this.natureBudget);
            viewController.addEqualsConstraint("AnneeBudgetaire", PreviousYEAR);
            viewController.addGreaterConstraint("RAP", 0);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("ReferentielsBudget" , "Budget"));
            if(storageResources!=null && !storageResources.isEmpty()){
                return storageResources.stream().map(obj -> (IStorageResource)obj.getValue("RubriqueBudgetaire")).collect(Collectors.toList());
            }
            return storageResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
