package com.moovapps.gp.achats.bc.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class CreationEngagement extends BaseDocumentExtension {
    private IContext loggedOnContext = null;

    @Override
    public boolean onBeforeSubmit(IAction action) {
        try{
            if(action.getName().equals("Envoyer")){
                this.loggedOnContext = getWorkflowModule().getLoggedOnUserContext();
                IWorkflow iWorkflow = WorkflowsService.getWorflow("Budget", "Engagement_1.0");
                IWorkflowInstance engagementworkflowInstance = getWorkflowModule().createWorkflowInstance(this.loggedOnContext, iWorkflow, "");
                engagementworkflowInstance.setValue("ReferenceBCMarche", getWorkflowInstance().getValue("sys_Reference"));
                engagementworkflowInstance.setValue("Fournisseur", getWorkflowInstance().getValue("Fournisseur"));
                engagementworkflowInstance.setValue("ObjetEngagement", getWorkflowInstance().getValue("ObjetDuBC"));
                engagementworkflowInstance.setValue("MontantAImputer", castToBigDecimal(getWorkflowInstance().getValue("TotalTTC")));
                engagementworkflowInstance.save(this.loggedOnContext);
                getWorkflowInstance().addLinkedWorkflowInstance("Engagement" ,  engagementworkflowInstance);
                getWorkflowInstance().save(this.loggedOnContext);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }
}
