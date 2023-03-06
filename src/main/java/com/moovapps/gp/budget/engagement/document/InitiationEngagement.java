package com.moovapps.gp.budget.engagement.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.BigDecimalInputComponent;
import com.axemble.vdp.utils.CollectionUtils;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.*;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;
import static com.moovapps.gp.budget.utils.BudgetUtils.*;

public class InitiationEngagement extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private BigDecimal montantAImputer = BigDecimal.ZERO;

    private BigDecimal disponible = BigDecimal.ZERO;

    public boolean onAfterLoad() {
        try {
            getResourceController().setThrowEvents("MontantAImputer", true);

            InitializeFields(getWorkflowInstance());

            IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");

            String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");

            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(anneeBudgetaire, natureBudget);

            InitializeRubriqueBudgetaireList(linkedResources, getWorkflowInstance(), getWorkflowModule());

            if (getWorkflowInstance().getValue("RubriqueBudgetaire") != null && getWorkflowInstance().getValue("Disponible") != null) {
                this.disponible = castToBigDecimal(getWorkflowInstance().getValue("Disponible"));
                getWorkflowInstance().setValue("Disponible", castToBigDecimal(getWorkflowInstance().getValue("Disponible")));
                getWorkflowInstance().setValue("CreditsOuvertsCP", castToBigDecimal(getWorkflowInstance().getValue("CreditsOuvertsCP")));
                getWorkflowInstance().setValue("CreditsOuvertsCE", castToBigDecimal(getWorkflowInstance().getValue("CreditsOuvertsCE")));
                getWorkflowInstance().setValue("TotalDesEngagements", castToBigDecimal(getWorkflowInstance().getValue("TotalDesEngagements")));
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAImputer"));
                BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                component.setNumberMax(this.disponible);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(anneeBudgetaire, natureBudget);
            if (property.getName().equals("AnneeBudgetaire") || property.getName().equals("NatureBudget")) {
                getWorkflowInstance().setValue("RubriqueBudgetaire", null);
                InitializeRubriqueBudgetaireList(linkedResources, getWorkflowInstance(), getWorkflowModule());
            } else if (property.getName().equals("RubriqueBudgetaire")) {
                getWorkflowInstance().setValue("Disponible", null);
                String rubriqueBudgetaire = (String) getWorkflowInstance().getValue("RubriqueBudgetaire");
                if (rubriqueBudgetaire == null) {
                    return;
                }
                ILinkedResource iLinkedResource = linkedResources.stream()
                        .filter(obj -> ((IStorageResource) obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(rubriqueBudgetaire))
                        .findFirst()
                        .orElse(null);
                if (iLinkedResource == null) {
                    getWorkflowInstance().setValue("RubriqueBudgetaireNV", null);
                    getWorkflowInstance().save("RubriqueBudgetaireNV");
                    return;
                }
                this.disponible = iLinkedResource.getValue("Disponible") != null ? castToBigDecimal(iLinkedResource.getValue("Disponible")) : castToBigDecimal(iLinkedResource.getValue("CreditsOuvertsCP"));
                getWorkflowInstance().setValue("CreditsOuvertsCP", castToBigDecimal(iLinkedResource.getValue("CreditsOuvertsCP")));
                getWorkflowInstance().setValue("CreditsOuvertsCE", castToBigDecimal(iLinkedResource.getValue("CreditsOuvertsCE")));
                getWorkflowInstance().setValue("TotalDesEngagements", castToBigDecimal(iLinkedResource.getValue("TotalDesEngagements")));
                getWorkflowInstance().setValue("Disponible", this.disponible);
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAImputer"));
                BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                component.setNumberMax(this.disponible);
                getWorkflowInstance().setValue("RubriqueBudgetaireNV", iLinkedResource.getValue("RubriqueBudgetaire"));
                getWorkflowInstance().save("RubriqueBudgetaireNV");
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
            if (action.getName().equals("Envoyer")) {
                if (!checkBudget(RubriqueBudgetaire, anneeBudgetaire, natureBudget)) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    public boolean checkBudget(String protocolURI, String Annee, IStorageResource natureBudget) {
        boolean ischecked = true;
        try {
            BigDecimal montantEngager = castToBigDecimal(getWorkflowInstance().getValue("MontantAImputer"));
            Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(Annee, natureBudget);
            if (linkedResources == null || linkedResources.isEmpty()) {
                getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                return false;
            }
            for (ILinkedResource iLinkedResource : linkedResources) {
                if (((IStorageResource) iLinkedResource.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(protocolURI)) {
                    BigDecimal Disponible = castToBigDecimal(iLinkedResource.getValue("Disponible"));
                    if (Disponible != null && montantEngager.compareTo(Disponible) > 0) {
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

    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget(String Annee, IStorageResource natureBudget) {
        Collection<ILinkedResource> linkedResources = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), Annee);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Dépenses");
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), natureBudget);
            viewController.addEqualsConstraint("DocumentState", "Budget ouvert");
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            if (workflowInstances != null && !workflowInstances.isEmpty())
                linkedResources = CollectionUtils.cast(workflowInstances.iterator().next().getLinkedResources("RB_Budget_Tab"),ILinkedResource.class);
            return linkedResources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

