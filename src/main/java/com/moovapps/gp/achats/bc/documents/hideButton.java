package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.gp.services.DirectoryService;

public class hideButton extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    @Override
    public boolean onAfterLoad() {
        try{
            ITaskInstance taskInstance = getWorkflowInstance().getCurrentTaskInstance(sysAdminContext);
            if (taskInstance != null) {
                IAction action = getWorkflowModule().getAction(sysAdminContext, taskInstance.getTask(), "Valider");
                IAction action2 = getWorkflowModule().getAction(sysAdminContext, taskInstance.getTask(), "Retour");
                if(action!=null){
                    CtlButton ctlButton = getResourceController().getButton(action.getLabel(), 2);
                    if(ctlButton!=null){
                        ctlButton.setHidden(true);
                    }
                }
                if(action2!=null){
                    CtlButton ctlButton2 = getResourceController().getButton(action2.getLabel(), 2);
                    if(ctlButton2!=null){
                        ctlButton2.setHidden(true);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}
