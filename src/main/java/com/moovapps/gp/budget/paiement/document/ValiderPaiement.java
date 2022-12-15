package com.moovapps.gp.budget.paiement.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.gp.budget.helpers.calculate;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;

import java.math.BigDecimal;
import java.util.Collection;

public class ValiderPaiement extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;

    private IContext sysAdminContext = DirectoryService.getSysAdminContext();

    private String typeBudget = "Dépenses";

    private BigDecimal montantPaiement = BigDecimal.ZERO;

    private BigDecimal montantPaye_Engagement = BigDecimal.ZERO;

    private BigDecimal montantPaye_RAP = BigDecimal.ZERO;

    private BigDecimal resteAPayer_Engagement = BigDecimal.ZERO;

    private BigDecimal resteAPayer_RAP = BigDecimal.ZERO;

    private BigDecimal payementN1_RB = BigDecimal.ZERO;

    private BigDecimal totalpaiement_RB = BigDecimal.ZERO;

    private BigDecimal payementRAPN1 = BigDecimal.ZERO;

    private BigDecimal resteAPayer_RB = BigDecimal.ZERO;

    private String RubriqueBudgetaire = null;

    private IWorkflowInstance engagementInstance = null;

    private IWorkflowInstance rapInstance = null;


    public boolean onBeforeSubmit(IAction action) {
        try {
            IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            this.engagementInstance = (IWorkflowInstance) getWorkflowInstance().getValue("ENGAGEMENT_INSTANCE");
            this.rapInstance = (IWorkflowInstance) getWorkflowInstance().getValue("RAP_INSTANCE");
            this.montantPaiement = (BigDecimal) getWorkflowInstance().getValue("MontantAPayer");
            IWorkflowInstance workflowInstance = null;
            Collection<ILinkedResource> linkedResources = null;
            if (action.getName().equals("Accepter")) {
                if(getWorkflowInstance().getValue("PaiementRAP").equals(false)){
                    // paiement ENGAGEMENT
                    workflowInstance = getBudgetOuverte(anneeBudgetaire, natureBudget);
                    if(workflowInstance ==null){
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                        return false;
                    }
                    linkedResources = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("RB_Budget_Tab");
                    if (this.engagementInstance != null) {
                        this.RubriqueBudgetaire = (String) this.engagementInstance.getValue("RubriqueBudgetaire");
                        this.montantPaye_Engagement = (BigDecimal) this.engagementInstance.getValue("MontantPaye");
                        this.resteAPayer_Engagement = (BigDecimal) this.engagementInstance.getValue("ResteAPayer");
                        if (this.RubriqueBudgetaire != null) {
                            ILinkedResource iLinkedResource = linkedResources.stream()
                                    .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(this.RubriqueBudgetaire))
                                    .findFirst()
                                    .orElse(null);
                            if(iLinkedResource==null){
                                getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                                return false;
                            }
                            if (this.resteAPayer_Engagement.compareTo(this.montantPaiement) < 0) {
                                getResourceController().alert("Le montant de paiement est supérieur a le reste a payé de l'engagement");
                                return false;
                            }
                                this.payementN1_RB = iLinkedResource.getValue("Paiement_N1") != null ? (BigDecimal) iLinkedResource.getValue("Paiement_N1") : BigDecimal.ZERO;
                                this.resteAPayer_RB = iLinkedResource.getValue("RAP_CURRENT") != null ? (BigDecimal) iLinkedResource.getValue("RAP_CURRENT") : BigDecimal.ZERO;
                                this.totalpaiement_RB = iLinkedResource.getValue("TotalDesPaiements") != null ? (BigDecimal) iLinkedResource.getValue("TotalDesPaiements"): BigDecimal.ZERO;
                                BigDecimal paiementn1 , totalpaiements , rap_current;
                                paiementn1 = this.payementN1_RB.add(this.montantPaiement);
                                totalpaiements = this.totalpaiement_RB.add(this.montantPaiement);
                                rap_current = this.resteAPayer_RB.subtract(this.montantPaiement);
                                iLinkedResource.setValue("Paiement_N1", paiementn1);
                                iLinkedResource.setValue("TotalDesPaiements", totalpaiements);
                                iLinkedResource.setValue("RAP_CURRENT", rap_current);
                                iLinkedResource.save(sysAdminContext);
                                iLinkedResource.getParentInstance().save(sysAdminContext);
                        }
                        this.engagementInstance.setValue("MontantPaye", this.montantPaye_Engagement.add(this.montantPaiement));
                        this.engagementInstance.setValue("ResteAPayer", this.resteAPayer_Engagement.subtract(this.montantPaiement));
                        this.engagementInstance.save(sysAdminContext);
                        String compte = (String) getWorkflowInstance().getValue("Compte");
                        IStorageResource compteRef = getCompte(compte);
                        IResourceDefinition iResourceDefinition = DataUniversService.getResourceDefinition("ReferentielsBudget", "CompteTresorerie");
                        if(compteRef == null){
                            compteRef = getWorkflowModule().createStorageResource(this.sysAdminContext, iResourceDefinition, null);
                        }
                        BigDecimal solde = compteRef.getValue("Solde") !=null ? (BigDecimal)compteRef.getValue("Solde") : BigDecimal.ZERO;
                        solde = solde.subtract(this.montantPaiement);
                        compteRef.setValue("sys_Title" , compte);
                        compteRef.setValue("Solde" , solde);
                        compteRef.save(this.sysAdminContext);
                        IResourceDefinition tresorieDefinition = DataUniversService.getResourceDefinition("ReferentielsBudget" , "Tresorie");
                        IStorageResource Tresorerie = getWorkflowModule().createStorageResource(this.sysAdminContext, tresorieDefinition, null);
                        Tresorerie.setValue("AnneeBudgetaire",anneeBudgetaire);
                        Tresorerie.setValue("Compte",compte);
                        Tresorerie.setValue("CompteTresorerie", compteRef);
                        Tresorerie.setValue("Date",getWorkflowInstance().getValue("DatePaiement"));
                        Tresorerie.setValue("Reference",getWorkflowInstance().getValue(IProperty.System.REFERENCE));
                        Tresorerie.setValue("Tiers",getWorkflowInstance().getValue("Fournisseur"));
                        Tresorerie.setValue("Type" , "Paiement");
                        Tresorerie.setValue("Montant",this.montantPaiement);
                        Tresorerie.save(this.sysAdminContext);
                    }
                }
                else{
                    workflowInstance = getBudgetOuverte(anneeBudgetaire, natureBudget);
                    linkedResources = workflowInstance != null ? (Collection<ILinkedResource>) workflowInstance.getLinkedResources("RB_Budget_Tab") : null;
                    if (linkedResources == null || linkedResources.isEmpty()) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_BUDGET_NOT_OPENED"));
                        return false;
                    }
                    if (this.rapInstance != null) {
                        this.RubriqueBudgetaire = (String) this.rapInstance.getValue("RubriqueBudgetaire");
                        this.montantPaye_RAP = this.rapInstance.getValue("MontantPaye") !=null ? (BigDecimal) this.rapInstance.getValue("MontantPaye"): BigDecimal.ZERO;
                        this.resteAPayer_RAP = (BigDecimal) this.rapInstance.getValue("ResteAPayer");
                        if(this.RubriqueBudgetaire==null){
                            getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                            return false;
                        }
                        ILinkedResource iLinkedResource = linkedResources.stream()
                                .filter(obj -> ((IStorageResource)obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(this.RubriqueBudgetaire))
                                .findFirst()
                                .orElse(null);
                        if(iLinkedResource==null){
                            getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                            return false;
                        }
                        if (this.resteAPayer_RAP.compareTo(this.montantPaiement) < 0) {
                            getResourceController().alert("Le montant de paiement est supérieur a le reste a payé");
                            return false;
                        }
                        this.payementRAPN1 = iLinkedResource.getValue("Paiement_RAP_N1") != null ? (BigDecimal) iLinkedResource.getValue("Paiement_RAP_N1") : BigDecimal.ZERO;
                        this.resteAPayer_RB = iLinkedResource.getValue("RAP_CURRENT") != null ? (BigDecimal) iLinkedResource.getValue("RAP_CURRENT") : BigDecimal.ZERO;
                        this.totalpaiement_RB = iLinkedResource.getValue("TotalDesPaiements") != null ? (BigDecimal) iLinkedResource.getValue("TotalDesPaiements") : BigDecimal.ZERO;
                        iLinkedResource.setValue("Paiement_RAP_N1", this.payementRAPN1.add(this.montantPaiement));
                        iLinkedResource.setValue("TotalDesPaiements", this.totalpaiement_RB.add(this.montantPaiement));
                        iLinkedResource.setValue("RAP_CURRENT", this.resteAPayer_RB.subtract(this.montantPaiement));
                        iLinkedResource.save(sysAdminContext);
                        iLinkedResource.getParentInstance().save(sysAdminContext);
                        this.rapInstance.setValue("MontantPaye", this.montantPaye_RAP.add(this.montantPaiement));
                        this.rapInstance.setValue("ResteAPayer", this.resteAPayer_RAP.subtract(this.montantPaiement));
                        this.rapInstance.save(sysAdminContext);
                        String compte = (String) getWorkflowInstance().getValue("Compte");
                        IStorageResource compteRef = getCompte(compte);
                        IResourceDefinition iResourceDefinition = DataUniversService.getResourceDefinition("ReferentielsBudget", "CompteTresorerie");
                        if(compteRef == null){
                            compteRef = getWorkflowModule().createStorageResource(this.sysAdminContext, iResourceDefinition, null);
                        }
                        BigDecimal solde = compteRef.getValue("Solde") !=null ? (BigDecimal)compteRef.getValue("Solde") : BigDecimal.ZERO;
                        solde = solde.subtract(this.montantPaiement);
                        //solde-=this.montantPaiement;
                        compteRef.setValue("sys_Title" , compte);
                        compteRef.setValue("Solde" , solde);
                        compteRef.save(this.sysAdminContext);

                        IResourceDefinition tresorieDefinition = DataUniversService.getResourceDefinition("ReferentielsBudget" , "Tresorie");
                        IStorageResource Tresorerie = getWorkflowModule().createStorageResource(this.sysAdminContext, tresorieDefinition, null);
                        Tresorerie.setValue("AnneeBudgetaire",anneeBudgetaire);
                        Tresorerie.setValue("Compte",compte);
                        Tresorerie.setValue("CompteTresorerie", compteRef);
                        Tresorerie.setValue("Date",getWorkflowInstance().getValue("DatePaiement"));
                        Tresorerie.setValue("Reference",getWorkflowInstance().getValue(IProperty.System.REFERENCE));
                        Tresorerie.setValue("Tiers",getWorkflowInstance().getValue("Fournisseur"));
                        Tresorerie.setValue("Type" , "Paiement");
                        Tresorerie.setValue("Montant",this.montantPaiement);
                        Tresorerie.save(this.sysAdminContext);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }


    private IWorkflowInstance getBudgetOuverte(String anneeBudgetaire, IStorageResource natureBudget) {
        try {
            this.sysAdminContext = getWorkflowModule().getSysadminContext();
            IWorkflowContainer workflowContainer = getWorkflowModule().getWorkflowContainer(this.sysAdminContext, getWorkflowInstance().getCatalog(), "GenerationDesBudgets");
            IViewController iViewController = getWorkflowModule().getViewController(this.sysAdminContext);
            iViewController.addEqualsConstraint("AnneeBudgetaire", anneeBudgetaire);
            iViewController.addEqualsConstraint("NatureBudget", natureBudget);
            iViewController.addEqualsConstraint("TypeBudget", this.typeBudget);
            iViewController.addEqualsConstraint("DocumentState", "Budget ouvert");
            Collection<IWorkflowInstance> workflowInstances = iViewController.evaluate(workflowContainer);
            if (workflowInstances != null && !workflowInstances.isEmpty())
                return workflowInstances.iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public IStorageResource getCompte(String compte){
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            IViewController viewController = workflowModule.getViewController(DirectoryService.getSysAdminContext(), IResource.class);
            viewController.addEqualsConstraint("sys_Title", compte);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("ReferentielsBudget" , "CompteTresorerie"));
            if(storageResources!=null && !storageResources.isEmpty()){
                return storageResources.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

