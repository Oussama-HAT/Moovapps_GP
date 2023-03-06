package com.moovapps.gp.budget.utils;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.gp.helpers.DateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class BudgetUtils {

    public static void InitializeFields(IWorkflowInstance iWorkflowInstance)
    {
        if (iWorkflowInstance.getValue("AnneeBudgetaire") == null) {
            int todayYear = DateService.getYear(new Date());
            iWorkflowInstance.setValue("AnneeBudgetaire", String.valueOf(todayYear));
        }

        if (iWorkflowInstance.getParentInstance() != null) {
            if (iWorkflowInstance.getValue("MontantAImputer") == null && iWorkflowInstance.getParentInstance().getValue("TotalTTC") != null)
                iWorkflowInstance.setValue("MontantAImputer", iWorkflowInstance.getParentInstance().getValue("TotalTTC"));
        }
    }

    public static void InitializeRubriqueBudgetaireList(Collection<ILinkedResource> linkedResources , IWorkflowInstance iWorkflowInstance , IWorkflowModule iWorkflowModule)
    {
        if(linkedResources!=null && !linkedResources.isEmpty()){
            ArrayList<IOptionList.IOption> options = new ArrayList<>();
            for(ILinkedResource iLinkedResource : linkedResources){
                options.add(iWorkflowModule.createListOption((String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire"), (String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire")));
            }
            iWorkflowInstance.setList("RubriqueBudgetaire", options );
        }
        else{
            iWorkflowInstance.setList("RubriqueBudgetaire", null );
        }
    }

    public static boolean isDepenses(String typeBudget){
        return typeBudget.equals("Dépenses");
    }
}
