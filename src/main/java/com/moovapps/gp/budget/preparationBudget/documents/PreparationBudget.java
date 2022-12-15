package com.moovapps.gp.budget.preparationBudget.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class PreparationBudget extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    public void onPropertyChanged(IProperty property) {
        try {
            IWorkflowInstance iWorkflowInstance = getWorkflowInstance();
            String chargementRubriques = (String) iWorkflowInstance.getValue("ChargementDesRubriques");
            String typeBudget = (String) iWorkflowInstance.getValue(Const.Properties.TypeBudget.toString());
            IStorageResource natureBudget = (IStorageResource) iWorkflowInstance.getValue(Const.Properties.NatureBudget.toString());
            if (property.getName().equals("ChargementDesRubriques")) {
                iWorkflowInstance.deleteLinkedResources(iWorkflowInstance.getLinkedResources(Const.Properties.PB_Budget_Tab.toString()));
                if (chargementRubriques != null && chargementRubriques.equals("Depuis le référentiel")) {
                    MAJPreparationBudget(typeBudget, natureBudget);
                }
            }
            else if (property.getName().equals(Const.Properties.TypeBudget.toString()) || property.getName().equals(Const.Properties.NatureBudget.toString())) {
                iWorkflowInstance.deleteLinkedResources(iWorkflowInstance.getLinkedResources(Const.Properties.PB_Budget_Tab.toString()));
                if(chargementRubriques!=null && chargementRubriques.equals("Depuis le référentiel")){
                    MAJPreparationBudget(typeBudget, natureBudget);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {

        try {
            if(action.getName().equals(Const.ACTION_ENVOYER_VALIDATION_PB)){
                String Annee = (String) getWorkflowInstance().getValue(Const.Properties.AnneeBudgetaire.toString());
                String typeBudget = (String) getWorkflowInstance().getValue(Const.Properties.TypeBudget.toString());
                IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue(Const.Properties.NatureBudget.toString());
                Collection<ILinkedResource> rbLinkedResources = (Collection<ILinkedResource> ) getWorkflowInstance().getLinkedResources(Const.Properties.PB_Budget_Tab.toString());
                if(rbLinkedResources!=null && !rbLinkedResources.isEmpty()){
                    for(ILinkedResource iLinkedResource : rbLinkedResources){
                        if(iLinkedResource.getValue("MontantDuBudgetCP")==null || (!natureBudget.getValue("sys_Reference").equals("0002") && iLinkedResource.getValue("MontantDuBudgetCE")==null)){
                            getResourceController().alert("Action impossible: Le montant du budget CP / CE est obligatoire !");
                            return false;
                        }
                    }
                }
                boolean isExist = checkBudgetExist(Annee ,typeBudget , natureBudget);
                if(isExist){
                    getResourceController().alert("Action impossible: Une version du budget générée existe!");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);

    }

    private void MAJPreparationBudget(String typeBudget, IStorageResource natureBudget) {
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext, IResource.class);
            viewController.addEqualsConstraint("TypeBudget", typeBudget);
            viewController.addEqualsConstraint("NatureBudget", natureBudget);
            viewController.addEqualsConstraint("Actif", Boolean.TRUE);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("ReferentielsBudget", "RubriquesBudgetaires"));
            ILinkedResource newLinkedResourcePB = null;
            ArrayList<ILinkedResource> collectionLinkedResourcePB = new ArrayList<>();
            for (IStorageResource storageResource : storageResources) {
                newLinkedResourcePB = getWorkflowInstance().createLinkedResource(Const.Properties.PB_Budget_Tab.toString());
                newLinkedResourcePB.setValue("RubriqueBudgetaire", storageResource);
                newLinkedResourcePB.setValue("TypeBudget", typeBudget);
                newLinkedResourcePB.setValue("NatureBudget", natureBudget);
                newLinkedResourcePB.save(this.sysAdminContext);
                collectionLinkedResourcePB.add(newLinkedResourcePB);
            }
            getWorkflowInstance().addLinkedResources(collectionLinkedResourcePB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkBudgetExist(String Annee , String typeBudget , IStorageResource natureBudget) {
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), Annee);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), typeBudget);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), natureBudget);
            viewController.addNotInConstraint("DocumentState", Arrays.asList("Demande à modifier", "En cours" , "Budget ouvert (Nouvelle version en cours)" , "Budget rejeté"));
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            return (workflowInstances != null && !workflowInstances.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
