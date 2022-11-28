package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IOperator;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;

import java.util.Collection;

public class AnnulationEngagement extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSave() {
        getWorkflowInstance().getHistory().addEvent("Annuler ma demande" , "Annulation", getWorkflowModule().getLoggedOnUser(),  getWorkflowModule().getLoggedOnUser() , (String) getWorkflowInstance().getValue("MotifDAnnulation"), null );
        IWorkflowInstance parentInstance = getWorkflowInstance().getParentInstance();

        if(parentInstance!=null){
            if(parentInstance.getWorkflow().getWorkflowContainer().getName().equals("BC")){
                ITaskInstance taskInstance = parentInstance.getCurrentTaskInstance(getWorkflowModule().getSysadminContext());
                if(taskInstance!=null){
                    Collection<IOperator> iOperators = (Collection<IOperator>) taskInstance.getOperators();
                    if(iOperators!=null && !iOperators.isEmpty()){
                        WorkflowsService.executeAction(parentInstance , getWorkflowModule().getContext(iOperators.iterator().next()) , "Retour" , "Engagement refusé");
                    }
                }
            }
        }
        return super.onBeforeSave();
    }
}
