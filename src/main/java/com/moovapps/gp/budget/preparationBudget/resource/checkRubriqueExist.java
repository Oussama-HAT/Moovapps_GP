package com.moovapps.gp.budget.preparationBudget.resource;

import com.axemble.vdoc.sdk.document.extensions.BaseResourceExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class checkRubriqueExist extends BaseResourceExtension {

    @Override
    public boolean onBeforeSave() {
        try {
            ILinkedResource iLinkedResource = getLinkedResource();
            IWorkflowInstance parentInstance = iLinkedResource.getParentInstance();
            IStorageResource rubriqueBudgetaireREF = (IStorageResource) iLinkedResource.getValue("RubriqueBudgetaire");
            if(parentInstance!=null){
                Collection<ILinkedResource> rbLinkedResources = CollectionUtils.cast(parentInstance.getLinkedResources(getLinkedResource().getDefinition().getName()) , ILinkedResource.class);
                if(rbLinkedResources!=null && !rbLinkedResources.isEmpty()){
                    ArrayList<ILinkedResource> filteredLinkedResource = (ArrayList<ILinkedResource>) rbLinkedResources.stream().filter(c -> c.getValue("RubriqueBudgetaire").equals(rubriqueBudgetaireREF)).collect(Collectors.toList());
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
}
