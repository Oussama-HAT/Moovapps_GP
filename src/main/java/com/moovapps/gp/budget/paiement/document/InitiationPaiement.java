package com.moovapps.gp.budget.paiement.document;


import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.DoubleInputComponent;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

public class InitiationPaiement extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;

    private IContext sysAdminContext = null;

    private String anneeBudgetaire = null;

    private IStorageResource natureBudget = null;

    private IWorkflowInstance EngagementInstance = null;

    private IWorkflowInstance RAPInstance = null;


    private double resteApaye = 0.0D;

    public boolean onAfterLoad() {
        try {
            this.sysAdminContext = getWorkflowModule().getSysadminContext();
            this.EngagementInstance= (IWorkflowInstance) getWorkflowInstance().getValue("ENGAGEMENT_INSTANCE");
            this.RAPInstance= (IWorkflowInstance) getWorkflowInstance().getValue("RAP_INSTANCE");

            if(getWorkflowInstance().getParentInstance()!=null){
                getResourceController().setEditable("ENGAGEMENT_INSTANCE" , false);
                getResourceController().setEditable("MontantAPayer" , false);
            }
            if(getWorkflowInstance().getValue("PaiementRAP").equals(false)) {
                if(this.EngagementInstance!=null && this.EngagementInstance.getValue("RubriqueBudgetaire")!=null) {
                    this.resteApaye = ((Number) this.EngagementInstance.getValue("ResteAPayer")).doubleValue();
                    TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                    DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                    BigDecimal bd = BigDecimal.valueOf(this.resteApaye);
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                    this.resteApaye = bd.doubleValue();
                    component.setNumberMax(this.resteApaye);
                }
            }
            else{
                if(this.RAPInstance!=null && this.RAPInstance.getValue("RubriqueBudgetaire")!=null) {
                    this.resteApaye = ((Number) this.RAPInstance.getValue("ResteAPayer")).doubleValue();
                    TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                    DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                    BigDecimal bd = BigDecimal.valueOf(this.resteApaye);
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                    this.resteApaye = bd.doubleValue();
                    component.setNumberMax(this.resteApaye);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget();
            if(property.getName().equals("ENGAGEMENT_INSTANCE")){
                this.EngagementInstance = (IWorkflowInstance) getWorkflowInstance().getValue("ENGAGEMENT_INSTANCE");
                if(this.EngagementInstance!=null) {
                    String rubriqueBudgetaire = (String) this.EngagementInstance.getValue("RubriqueBudgetaire");
                    if (rubriqueBudgetaire != null) {
                        ILinkedResource iLinkedResource = linkedResources.stream()
                                .filter(obj -> ((IStorageResource) obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(rubriqueBudgetaire))
                                .findFirst()
                                .orElse(null);
                        if (iLinkedResource != null) {
                            this.resteApaye = ((Number) this.EngagementInstance.getValue("ResteAPayer")).doubleValue();
                            TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                            DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                            component.setNumberMax(this.resteApaye);
                        }
                    }
                }
            }
            else if(property.getName().equals("RAP_INSTANCE")){
                this.RAPInstance= (IWorkflowInstance) getWorkflowInstance().getValue("RAP_INSTANCE");
                if(this.RAPInstance!=null) {
                    String rubriqueBudgetaire = (String) this.RAPInstance.getValue("RubriqueBudgetaire");
                    if (rubriqueBudgetaire != null) {
                        ILinkedResource iLinkedResource = linkedResources.stream()
                                .filter(obj -> ((IStorageResource) obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(rubriqueBudgetaire))
                                .findFirst()
                                .orElse(null);
                        if (iLinkedResource != null) {
                            this.resteApaye = ((Number) this.RAPInstance.getValue("ResteAPayer")).doubleValue();
                            TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                            DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                            component.setNumberMax(this.resteApaye);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        this.EngagementInstance= (IWorkflowInstance) getWorkflowInstance().getValue("ENGAGEMENT_INSTANCE");
        this.RAPInstance= (IWorkflowInstance) getWorkflowInstance().getValue("RAP_INSTANCE");
        this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
        this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
        String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
        String RubriqueBudgetaire = null;
        if(getWorkflowInstance().getValue("PaiementRAP").equals(false)) {
            RubriqueBudgetaire = (String) this.EngagementInstance.getValue("RubriqueBudgetaire");
        }
        else{
            RubriqueBudgetaire = (String) this.RAPInstance.getValue("RubriqueBudgetaire");
        }
        try {
            if(action.getName().equals("Envoyer")){
                if(!checkBudget(RubriqueBudgetaire)){
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    public boolean checkBudget(String RubriqueBudgetaire){
        boolean ischecked = true;
        try {
            double MontantAPayer = ((Number) getWorkflowInstance().getValue("MontantAPayer")).doubleValue();
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget();
            if(linkedResources==null || linkedResources.isEmpty()){
                getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                return false;
            }
            for(ILinkedResource iLinkedResource : linkedResources){
                if(((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(RubriqueBudgetaire)){
                    if(iLinkedResource.getValue("RAP_CURRENT")!=null && MontantAPayer > ((Number) iLinkedResource.getValue("RAP_CURRENT")).doubleValue()){
                        getResourceController().alert("Action impossible : Le montant de paiement est supérieur a le reste a payé !!");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            ischecked = false;
            e.printStackTrace();
        }
        return ischecked;
    }

    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget() {
        Collection<ILinkedResource> linkedResources = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Dépenses");
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addEqualsConstraint("DocumentState", "Budget ouvert");
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            if(workflowInstances != null && !workflowInstances.isEmpty())
                linkedResources = (Collection<ILinkedResource>) workflowInstances.iterator().next().getLinkedResources("RB_Budget_Tab");
            return linkedResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

