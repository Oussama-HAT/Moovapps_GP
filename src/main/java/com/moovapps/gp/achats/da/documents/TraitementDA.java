package com.moovapps.gp.achats.da.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

public class TraitementDA extends BaseDocumentExtension {
    public boolean onAfterLoad() {
        try {
            ITaskInstance taskInstance = getWorkflowInstance().getCurrentTaskInstance(getWorkflowModule().getSysadminContext());
            if(taskInstance!=null){
                IAction action = getWorkflowModule().getAction(getWorkflowModule().getSysadminContext(), taskInstance.getTask(), "DATraitee");
                if(action!=null){
                    CtlButton button = getResourceController().getButton(action.getLabel(), 2);
                    if (button != null)
                        button.setHidden(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}
