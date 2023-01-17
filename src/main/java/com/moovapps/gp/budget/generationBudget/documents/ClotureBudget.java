package com.moovapps.gp.budget.generationBudget.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class ClotureBudget extends BaseDocumentExtension {
    private IContext sysAdminContext = DirectoryService.getSysAdminContext();
    private IContext loggedOnContext = null;
    private String anneeBudgetaire = null;
    private String typeBudget = null;
    private IStorageResource natureBudget = null;

    private BigDecimal resteApayerEngagement = BigDecimal.ZERO;
    private BigDecimal resteApayerRAP = BigDecimal.ZERO;
    private BigDecimal disponible = BigDecimal.ZERO;
    private BigDecimal montantEngager = BigDecimal.ZERO;
    private BigDecimal montantPaye = BigDecimal.ZERO;

    public boolean onBeforeSubmit(IAction action) {
        try {
            this.loggedOnContext = getWorkflowModule().getLoggedOnUserContext();
            this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            this.typeBudget = (String) getWorkflowInstance().getValue("TypeBudget");
            IWorkflow iWorkflow = WorkflowsService.getWorflow("Budget", "ResteAPayer_1.0");
            if (action.getName().equals(Const.ACTION_CLOTURER_BUDGET_GB)) {
                if (this.typeBudget.equals("Dépenses")) {
                    Collection<IWorkflowInstance> engagementsInstances = getEngagementValideWithRAP();
                    Collection<IWorkflowInstance> rapWorkflowInstances = getRAP();
                    Collection<ILinkedResource> RB_linkedResources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("RB_Budget_Tab");
                    if (engagementsInstances != null && !engagementsInstances.isEmpty()) {
                        for (IWorkflowInstance engagementworkflowInstance : engagementsInstances) {
                            this.resteApayerEngagement = castToBigDecimal(engagementworkflowInstance.getValue("ResteAPayer"));
                                Collection<IWorkflowInstance> rapInstances = (Collection<IWorkflowInstance>) engagementworkflowInstance.getLinkedWorkflowInstances("FicheRAP");
                                ILinkedResource iLinkedResource = RB_linkedResources.stream()
                                        .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(engagementworkflowInstance.getValue("RubriqueBudgetaire")))
                                        .findFirst()
                                        .orElse(null);
                                if (iLinkedResource == null) {
                                    getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                                    return false;
                                }

                                if (rapInstances == null || rapInstances.isEmpty()) {
                                    this.montantEngager = castToBigDecimal(engagementworkflowInstance.getValue("MontantAImputer"));
                                    this.montantPaye = castToBigDecimal(engagementworkflowInstance.getValue("MontantPaye"));
                                    this.disponible = castToBigDecimal(iLinkedResource.getValue("Disponible"));
                                    IWorkflowInstance RAPInstance = getWorkflowModule().createWorkflowInstance(this.loggedOnContext, iWorkflow, "");
                                    int annee = Integer.parseInt((String) engagementworkflowInstance.getValue("AnneeBudgetaire")) + 1;
                                    RAPInstance.setValue("ReferenceEngagement", engagementworkflowInstance.getValue("sys_Reference"));
                                    RAPInstance.setValue("ReferenceBCMarche", engagementworkflowInstance.getValue("ReferenceBCMarche"));
                                    RAPInstance.setValue("Fournisseur", engagementworkflowInstance.getValue("Fournisseur"));
                                    RAPInstance.setValue("AnneeBudgetaireSource", engagementworkflowInstance.getValue("AnneeBudgetaire"));
                                    RAPInstance.setValue("AnneeBudgetaireDestination", String.valueOf(annee));
                                    RAPInstance.setValue("NatureBudget", engagementworkflowInstance.getValue("NatureBudget"));
                                    RAPInstance.setValue("DateEngagement", engagementworkflowInstance.getValue("DateEngagement"));
                                    RAPInstance.setValue("ObjetEngagement", engagementworkflowInstance.getValue("ObjetEngagement"));
                                    RAPInstance.setValue("RubriqueBudgetaire", engagementworkflowInstance.getValue("RubriqueBudgetaire"));
                                    RAPInstance.setValue("Disponible", this.disponible);
                                    RAPInstance.setValue("MontantAImputer", this.montantEngager);
                                    RAPInstance.setValue("CumulDesPaiementsN1", this.montantPaye);
                                    RAPInstance.setValue("ResteAPayerN1", this.resteApayerEngagement);
                                    RAPInstance.setValue("MontantAPayer", 0);
                                    RAPInstance.setValue("ResteAPayer", this.resteApayerEngagement);
                                    RAPInstance.save(this.loggedOnContext);
                                    engagementworkflowInstance.addLinkedWorkflowInstance("FicheRAP", RAPInstance);
                                    engagementworkflowInstance.save(this.sysAdminContext);
                                    WorkflowsService.executeAction(engagementworkflowInstance , this.loggedOnContext , "RAP" , "AUTO");
                                }
                        }
                    }
                    if(rapWorkflowInstances != null && !rapWorkflowInstances.isEmpty()){
                        for (IWorkflowInstance iWorkflowInstance : rapWorkflowInstances) {
                            if(((Number)iWorkflowInstance.getValue("ResteAPayer")).doubleValue() !=0){
                                    ILinkedResource iLinkedResource = RB_linkedResources.stream()
                                            .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(iWorkflowInstance.getValue("RubriqueBudgetaire")))
                                            .findFirst()
                                            .orElse(null);
                                    if (iLinkedResource == null) {
                                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                                        return false;
                                    }
                                this.disponible = castToBigDecimal(iLinkedResource.getValue("Disponible"));
                                this.resteApayerRAP = castToBigDecimal(iWorkflowInstance.getValue("ResteAPayer"));
                                this.montantEngager = castToBigDecimal(iWorkflowInstance.getValue("MontantAImputer"));
                                this.montantPaye = castToBigDecimal(iWorkflowInstance.getValue("MontantAPayer"));
                                BigDecimal cumulePaiementN1 = castToBigDecimal(iWorkflowInstance.getValue("CumulDesPaiementsN1"));
                                IWorkflowInstance RAPInstance = getWorkflowModule().createWorkflowInstance(this.loggedOnContext, iWorkflow, "");
                                int annee = Integer.parseInt((String) iWorkflowInstance.getValue("AnneeBudgetaireDestination")) + 1;
                                RAPInstance.setValue("ReferenceEngagement", iWorkflowInstance.getValue("ReferenceEngagement"));
                                RAPInstance.setValue("ReferenceBCMarche", iWorkflowInstance.getValue("ReferenceBCMarche"));
                                RAPInstance.setValue("Fournisseur", iWorkflowInstance.getValue("Fournisseur"));
                                RAPInstance.setValue("AnneeBudgetaireSource", iWorkflowInstance.getValue("AnneeBudgetaireSource"));
                                RAPInstance.setValue("AnneeBudgetaireDestination", String.valueOf(annee));
                                RAPInstance.setValue("NatureBudget", iWorkflowInstance.getValue("NatureBudget"));
                                RAPInstance.setValue("DateEngagement", iWorkflowInstance.getValue("DateEngagement"));
                                RAPInstance.setValue("ObjetEngagement", iWorkflowInstance.getValue("ObjetEngagement"));
                                RAPInstance.setValue("RubriqueBudgetaire", iWorkflowInstance.getValue("RubriqueBudgetaire"));
                                RAPInstance.setValue("Disponible", this.disponible);
                                RAPInstance.setValue("MontantAImputer", this.montantEngager);
                                RAPInstance.setValue("CumulDesPaiementsN1", cumulePaiementN1.add(this.montantPaye));
                                RAPInstance.setValue("ResteAPayerN1", this.resteApayerRAP);
                                RAPInstance.setValue("MontantAPayer", 0);
                                RAPInstance.setValue("ResteAPayer", this.resteApayerRAP);
                                RAPInstance.save(this.loggedOnContext);
                                //RAPInstance.setValue("sys_Reference" , "RAP-"+String.valueOf(annee) + "-"+getWorkflowInstance().getValue("sys_Reference_chrono"));
                                //RAPInstance.save("sys_Reference");
                                iWorkflowInstance.getParentInstance().addLinkedWorkflowInstance("FicheRAP", RAPInstance);
                                iWorkflowInstance.getParentInstance().save(this.sysAdminContext);
                            }
                            WorkflowsService.executeAction(iWorkflowInstance , this.loggedOnContext , "Reporter" , "AUTO");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }


    @Override
    public boolean onAfterSubmit(IAction action) {
        if (action.getName().equals(Const.ACTION_CLOTURER_BUDGET_GB)) {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IResourceDefinition iResourceDefinition = null;
            IStorageResource budget = null;
            try {
                IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                IProject project = getProjectModule().getProject(sysContext, "ADMINISTRATIONGP", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "ReferentielsBudget", ICatalog.IType.STORAGE, project);
                iResourceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Budget");

            } catch (DirectoryModuleException | WorkflowModuleException | ProjectModuleException e) {
                e.printStackTrace();
            }
            Collection<ILinkedResource> budgets = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("RB_Budget_Tab");
            for (ILinkedResource iLinkedResource : budgets) {
                try {
                    budget = getWorkflowModule().createStorageResource(sysContext, iResourceDefinition, null);
                } catch (WorkflowModuleException e) {
                    e.printStackTrace();
                }
                if(budget!=null){
                    budget.setValue("TypeBudget", iLinkedResource.getValue("TypeBudget"));
                    budget.setValue("NatureBudget", iLinkedResource.getValue("NatureBudget"));
                    budget.setValue("AnneeBudgetaire", iLinkedResource.getValue("AnneeBudgetaire"));
                    budget.setValue("RubriqueBudgetaire", iLinkedResource.getValue("RubriqueBudgetaire"));
                    budget.setValue("ProgrammeDEmploi", iLinkedResource.getValue("ProgrammeDEmploi"));
                    budget.setValue("CreditsOuvertsCE", castToBigDecimal(iLinkedResource.getValue("CreditsOuvertsCE")));
                    budget.setValue("CreditsOuvertsCP", castToBigDecimal(iLinkedResource.getValue("CreditsOuvertsCP")));
                    budget.setValue("RAP", castToBigDecimal(iLinkedResource.getValue("RAP_CURRENT")));
                    budget.setValue("TotalDesEngagements", castToBigDecimal(iLinkedResource.getValue("TotalDesEngagements")));
                    budget.setValue("TotalDesPaiements", castToBigDecimal(iLinkedResource.getValue("TotalDesPaiements")));
                    budget.setValue("Disponible", castToBigDecimal(iLinkedResource.getValue("Disponible")));
                    budget.save(getDirectoryModule().getLoggedOnUserContext());
                }
            }
        }
        return super.onAfterSubmit(action);
    }

    private Collection<IWorkflowInstance> getEngagementValideWithRAP() {
        Collection<IWorkflowInstance> workflowInstances = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addEqualsConstraint("DocumentState", "Engagement validé");
            viewController.addGreaterConstraint("ResteAPayer", 0);
            workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "Engagement"));
            return workflowInstances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Collection<IWorkflowInstance> getRAP() {
        Collection<IWorkflowInstance> workflowInstances = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint("AnneeBudgetaireDestination", this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addEqualsConstraint("DocumentState", "En cours");
            viewController.addGreaterConstraint("ResteAPayer", 0);
            workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "ResteAPayer"));
            return workflowInstances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
