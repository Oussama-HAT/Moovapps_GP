package com.moovapps.gp.budget.engagement.resource.definition;


import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.workflow.extensions.BaseResourceDefinitionExtension;

public class ControleDelete extends BaseResourceDefinitionExtension {

    @Override
    public boolean onStartRemoving() {
        try {
            ILinkedResource iLinkedResource = (ILinkedResource) getResource();
            if(!getWorkflowModule().getLoggedOnUser().isSysadmin()) {
                boolean flagged = (boolean) iLinkedResource.getValue("FLAG");
                if (flagged) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartRemoving();
    }
}
