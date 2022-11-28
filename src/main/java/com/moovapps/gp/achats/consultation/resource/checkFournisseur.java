package com.moovapps.gp.achats.consultation.resource;

import com.axemble.vdoc.sdk.document.extensions.BaseResourceExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class checkFournisseur extends BaseResourceExtension {

    @Override
    public boolean onBeforeSave() {
        try {
            ILinkedResource iLinkedResource = getLinkedResource();
            IWorkflowInstance parentInstance = iLinkedResource.getParentInstance();
            IStorageResource Fournisseur = (IStorageResource) iLinkedResource.getValue("Fournisseur");
            if(parentInstance!=null){
                Collection<ILinkedResource> rbLinkedResources = (Collection<ILinkedResource> ) parentInstance.getLinkedResources(getLinkedResource().getDefinition().getName());
                if(rbLinkedResources!=null && !rbLinkedResources.isEmpty()){
                    ArrayList<ILinkedResource> filteredLinkedResource = (ArrayList<ILinkedResource>) rbLinkedResources.stream().filter(c -> c.getValue("Fournisseur").equals(Fournisseur)).collect(Collectors.toList());
                    if(filteredLinkedResource!=null && filteredLinkedResource.size()>1){
                        getResourceController().inform("Fournisseur" , "Ce fournisseur existe déjà! !");
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
