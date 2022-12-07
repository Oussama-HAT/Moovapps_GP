package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.DoubleInputComponent;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.ResourceTableInputComponent;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.helpers.DateService;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class InitiationEngagement extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private double montantAImputer = 0.0D;

    private double disponible = 0.0D;

    public boolean onAfterLoad() {
        try {
            Collection<ILinkedResource> linkedResources = null;
            getResourceController().setThrowEvents("MontantAImputer", true);
            if (getWorkflowInstance().getValue("AnneeBudgetaire") == null) {
                int todayYear = DateService.getYear(new Date());
                getWorkflowInstance().setValue("AnneeBudgetaire", String.valueOf(todayYear));
            }
            if (getWorkflowInstance().getParentInstance() != null) {
                if (getWorkflowInstance().getValue("MontantAImputer") == null && getWorkflowInstance().getParentInstance().getValue("TotalTTC") != null)
                    getWorkflowInstance().setValue("MontantAImputer", getWorkflowInstance().getParentInstance().getValue("TotalTTC"));
            }
            IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            linkedResources = getRubriqueBudgetByCurrentBudget(anneeBudgetaire , natureBudget);
            if(linkedResources!=null && !linkedResources.isEmpty()){
                ArrayList<IOption> options = new ArrayList<IOption>();
                for(ILinkedResource iLinkedResource : linkedResources){
                    options.add(getWorkflowModule().createListOption((String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire"), (String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire")));
                }
                getWorkflowInstance().setList("RubriqueBudgetaire", options );
            }
            else{
                getWorkflowInstance().setList("RubriqueBudgetaire", null );
            }
            if(getWorkflowInstance().getValue("RubriqueBudgetaire")!=null && getWorkflowInstance().getValue("Disponible")!=null) {
                this.disponible = ((Number) getWorkflowInstance().getValue("Disponible")).doubleValue();
                BigDecimal bd = BigDecimal.valueOf(this.disponible);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                getWorkflowInstance().setValue("Disponible",bd.doubleValue());
                this.disponible = bd.doubleValue();
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAImputer"));
                DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                component.setNumberMax(this.disponible);
            }

            getDocument().getDefaultWidget("CANCEL_Engagement");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(anneeBudgetaire , natureBudget);
             if(property.getName().equals("AnneeBudgetaire") || property.getName().equals("NatureBudget")){
                getWorkflowInstance().setValue("RubriqueBudgetaire", null );
                if(linkedResources!=null && !linkedResources.isEmpty()){
                    ArrayList<IOption> options = new ArrayList<>();
                    for(ILinkedResource iLinkedResource : linkedResources){
                        options.add(getWorkflowModule().createListOption((String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire"), (String) ((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire")));
                    }
                    getWorkflowInstance().setList("RubriqueBudgetaire", options );
                }
                else{
                    getWorkflowInstance().setList("RubriqueBudgetaire", null );
                }

            }
            else if(property.getName().equals("RubriqueBudgetaire")){
                getWorkflowInstance().setValue("Disponible",null);
                //getWorkflowInstance().setValue("RubriqueBudgetaire_text",null);
                String rubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
                if(rubriqueBudgetaire!=null){
                    ILinkedResource iLinkedResource = linkedResources.stream()
                                                                    .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(rubriqueBudgetaire))
                                                                    .findFirst()
                                                                    .orElse(null);
                    if(iLinkedResource!=null){
                        BigDecimal bd = BigDecimal.valueOf(this.disponible);
                        bd = bd.setScale(2, RoundingMode.HALF_UP);
                        getWorkflowInstance().setValue("Disponible",bd.doubleValue());
                        this.disponible = bd.doubleValue();
                        this.disponible = iLinkedResource.getValue("Disponible")!=null ? ((Number) iLinkedResource.getValue("Disponible")).doubleValue() : ((Number) iLinkedResource.getValue("CreditsOuvertsCP")).doubleValue();
                        getWorkflowInstance().setValue("Disponible",this.disponible);
                        TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAImputer"));
                        DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                        component.setNumberMax(this.disponible);
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
        IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
        String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
        String RubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
        try {
            if(action.getName().equals("Envoyer")){
                if(!checkBudget(RubriqueBudgetaire , anneeBudgetaire, natureBudget)){
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    public boolean checkBudget(String protocolURI , String Annee , IStorageResource natureBudget){
        boolean ischecked = true;
        try {
            double montantEngager = ((Number) getWorkflowInstance().getValue("MontantAImputer")).doubleValue();
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(Annee , natureBudget);
            if(linkedResources==null || linkedResources.isEmpty()){
                getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                return false;
            }
            for(ILinkedResource iLinkedResource : linkedResources){
                if(((IStorageResource)iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(protocolURI)){
                    if(iLinkedResource.getValue("Disponible")!=null &&  montantEngager > ((Number) iLinkedResource.getValue("Disponible")).doubleValue()){
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_DISPO_LOWER"));
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

    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget(String Annee , IStorageResource natureBudget) {
        Collection<ILinkedResource> linkedResources = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), Annee);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Dépenses");
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), natureBudget);
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

