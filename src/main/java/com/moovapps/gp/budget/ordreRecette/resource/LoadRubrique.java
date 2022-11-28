package com.moovapps.gp.budget.ordreRecette.resource;

import com.axemble.vdoc.sdk.document.extensions.BaseResourceExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class LoadRubrique extends BaseResourceExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    @Override
    public boolean onAfterLoad() {
        IStorageResource natureBudget = (IStorageResource) getLinkedResource().getParentInstance().getValue("NatureBudget");
        String anneeBudgetaire = (String) getLinkedResource().getParentInstance().getValue("AnneeBudgetaire");
        Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(anneeBudgetaire , natureBudget);
        if(linkedResources!=null && !linkedResources.isEmpty()){
            ArrayList<IOptionList.IOption> options = new ArrayList<IOptionList.IOption>();
            for(ILinkedResource iLinkedResource : linkedResources){
                options.add(getWorkflowModule().createListOption((String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire"), (String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire")));
            }
            getLinkedResource().setList("RubriqueBudgetaire", options );
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSave() {
        try {
            ILinkedResource iLinkedResource = getLinkedResource();
            IWorkflowInstance parentInstance = iLinkedResource.getParentInstance();
            String rubriqueBudgetaire = (String) iLinkedResource.getValue("RubriqueBudgetaire");
            if(parentInstance!=null){
                Collection<ILinkedResource> rbLinkedResources = (Collection<ILinkedResource> ) parentInstance.getLinkedResources(getLinkedResource().getDefinition().getName());
                if(rbLinkedResources!=null && !rbLinkedResources.isEmpty()){
                    ArrayList<ILinkedResource> filteredLinkedResource = (ArrayList<ILinkedResource>) rbLinkedResources.stream().filter(c -> c.getValue("RubriqueBudgetaire").equals(rubriqueBudgetaire)).collect(Collectors.toList());
                    if(filteredLinkedResource!=null && filteredLinkedResource.size()>1){
                        getResourceController().inform("RubriqueBudgetaire" , "Une rubrique portant le même code existe déjà! !");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSave();
    }

    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget(String Annee , IStorageResource natureBudget) {
        Collection<ILinkedResource> linkedResources = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), Annee);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Recettes");
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), natureBudget);
            viewController.addEqualsConstraint("DocumentState", "Budget ouvert");
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            if(workflowInstances != null && !workflowInstances.isEmpty())
                linkedResources = (Collection<ILinkedResource>) workflowInstances.iterator().next().getLinkedResources("RB_Budget_Tab");
            return linkedResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
