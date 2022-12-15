package com.moovapps.gp.budget.generationBudget.resource;

import com.axemble.vdoc.sdk.document.extensions.BaseResourceExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.BigDecimalInputComponent;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.DoubleInputComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class checkRubriqueExist extends BaseResourceExtension {


    @Override
    public boolean onAfterLoad() {
        try {
            ILinkedResource iLinkedResource = getLinkedResource();
            iLinkedResource.getProtocolURI();
            BigDecimal totalEngagement = (BigDecimal) iLinkedResource.getValue("TotalDesEngagements");
            TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("CreditsOuvertsCP"));
            BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
            component.setNumberMin(totalEngagement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSave();
    }

    @Override
    public boolean onBeforeSave() {
        try {
            ILinkedResource iLinkedResource = getLinkedResource();
            IWorkflowInstance parentInstance = iLinkedResource.getParentInstance();
            IStorageResource rubriqueBudgetaireREF = (IStorageResource) iLinkedResource.getValue("RubriqueBudgetaire");
            if(parentInstance!=null){
                Collection<ILinkedResource> rbLinkedResources = (Collection<ILinkedResource> ) parentInstance.getLinkedResources("RB_Budget_Tab");
                if(rbLinkedResources!=null && !rbLinkedResources.isEmpty()){
                    ArrayList<ILinkedResource> filteredLinkedResource = (ArrayList<ILinkedResource>) rbLinkedResources.stream().filter(c -> c.getValue("RubriqueBudgetaire").equals(rubriqueBudgetaireREF)).collect(Collectors.toList());
                    if(filteredLinkedResource!=null && filteredLinkedResource.size()>1){
                        getResourceController().inform("RubriqueBudgetaire" , "Une rubrique portant le même code existe déjà !");
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