package com.moovapps.gp.budget.paiement.document;


import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.BigDecimalInputComponent;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class InitiationPaiement extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;

    private IContext sysAdminContext = null;

    private String anneeBudgetaire = null;

    private IStorageResource natureBudget = null;

    private IWorkflowInstance EngagementInstance = null;

    private IWorkflowInstance RAPInstance = null;

    private BigDecimal resteApaye = BigDecimal.ZERO;

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
                    this.resteApaye = castToBigDecimal(this.EngagementInstance.getValue("ResteAPayer"));
                    TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                    BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                    component.setNumberMax(this.resteApaye);
                }
            }
            else{
                if(this.RAPInstance!=null && this.RAPInstance.getValue("RubriqueBudgetaire")!=null) {
                    this.resteApaye = castToBigDecimal(this.RAPInstance.getValue("ResteAPayer"));
                    TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                    BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
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
                            getWorkflowInstance().setValue("RubriqueBudgetaireNV" , iLinkedResource.getValue("RubriqueBudgetaire"));
                            this.resteApaye = castToBigDecimal(this.EngagementInstance.getValue("ResteAPayer"));
                            TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                            BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                            component.setNumberMax(this.resteApaye);
                        }
                        else{
                            getWorkflowInstance().setValue("RubriqueBudgetaireNV" , null);
                        }
                        getWorkflowInstance().save("RubriqueBudgetaireNV");
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
                            getWorkflowInstance().setValue("RubriqueBudgetaireNV" , iLinkedResource.getValue("RubriqueBudgetaire"));
                            this.resteApaye = castToBigDecimal(this.RAPInstance.getValue("ResteAPayer"));
                            TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAPayer"));
                            BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                            component.setNumberMax(this.resteApaye);
                        }
                        else{
                            getWorkflowInstance().setValue("RubriqueBudgetaireNV" , null);
                        }
                        getWorkflowInstance().save("RubriqueBudgetaireNV");
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
            BigDecimal MontantAPayer = castToBigDecimal(getWorkflowInstance().getValue("MontantAPayer"));
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget();
            if(linkedResources==null || linkedResources.isEmpty()){
                getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                return false;
            }
            BigDecimal RAP_CURRENT;
            for(ILinkedResource iLinkedResource : linkedResources){
                if(((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(RubriqueBudgetaire)){
                    RAP_CURRENT = castToBigDecimal(iLinkedResource.getValue("RAP_CURRENT"));
                    if(iLinkedResource.getValue("RAP_CURRENT")!=null && MontantAPayer.compareTo(RAP_CURRENT) > 0){
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

