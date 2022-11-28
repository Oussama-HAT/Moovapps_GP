package com.moovapps.gp.achats.depotDossier.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import java.util.Collection;

public class OuvertureDossierAdministratif extends BaseDocumentExtension {
    IContext loggedOnUserContext = null;

    public boolean onAfterLoad() {
        try {
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            IWorkflowInstance parentInstance = getWorkflowInstance().getParentInstance();
            if (parentInstance != null) {
                getPieces(parentInstance, "DossierAdministratif", "DA_Depot_Tab");
                getPieces(parentInstance, "DossierTechnique", "DT_Depot_Tab");
                getPieces(parentInstance, "OffreTechnique", "OT_Depot_Tab");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    private void getPieces(IWorkflowInstance parentInstance, String storageResourceName, String tableName) {
        try {
            Collection<ILinkedResource> linkedResourcesDA = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources(tableName);
            if (linkedResourcesDA == null || linkedResourcesDA.isEmpty()) {
                Collection<IStorageResource> storageResourcesDA = (Collection<IStorageResource>)parentInstance.getValue(storageResourceName);
                if (storageResourcesDA != null && !storageResourcesDA.isEmpty()) {
                    ILinkedResource newLinkedResource = null;
                    for (IStorageResource storageResourceDA : storageResourcesDA) {
                        newLinkedResource = getWorkflowInstance().createLinkedResource(tableName);
                        newLinkedResource.setValue("Piece", storageResourceDA.getValue("sys_Title"));
                        newLinkedResource.save(this.loggedOnUserContext);
                        getWorkflowInstance().addLinkedResource(newLinkedResource);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
