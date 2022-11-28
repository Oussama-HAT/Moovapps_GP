package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import java.util.Collection;

public class Enregistrement extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    String champs = null;

    public boolean onAfterLoad() {
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("BordereauDePrix_BC_Tab");
            if (linkedResources == null || linkedResources.isEmpty()) {
                IWorkflowInstance workflowInstanceConsultation = getWorkflowInstance().getParentInstance();
                if (workflowInstanceConsultation != null)
                    MAJBordereauDePrix(workflowInstanceConsultation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    private void MAJBordereauDePrix(IWorkflowInstance workflowInstanceConsultation) {
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)workflowInstanceConsultation.getValue("BordereauDePrix_Tab");
            ILinkedResource newLinkedResource = null;
            Collection<IProperty> properties = null;
            for (ILinkedResource linkedResource : linkedResources) {
                properties = (Collection<IProperty>) linkedResource.getDefinition().getProperties();
                newLinkedResource = getWorkflowInstance().createLinkedResource("BordereauDePrix_BC_Tab");
                for (IProperty property : properties) {
                    if (!property.getName().startsWith("sys_") && !property.getName().contains("URI"))
                        newLinkedResource.setValue(property.getName(), linkedResource.getValue(property.getName()));
                }
                newLinkedResource.save(this.sysAdminContext);
                getWorkflowInstance().addLinkedResource(newLinkedResource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
