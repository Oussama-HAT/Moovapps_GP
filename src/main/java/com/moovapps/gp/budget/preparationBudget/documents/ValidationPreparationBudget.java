package com.moovapps.gp.budget.preparationBudget.documents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ValidationPreparationBudget extends BaseDocumentExtension {

    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected IContext respBudgetContext = null;

    private String anneeBudgetaire = null;

    private String typeBudget = null;

    private IStorageResource natureBudget = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            IWorkflowInstance workflowInstanceGB = null;
            IGroup iGroup = null;
            IUser respBudget = null;
            this.anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            this.typeBudget = (String) getWorkflowInstance().getValue("TypeBudget");
            this.natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            Collection<ILinkedResource> linkedResourcesPB = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("PB_Budget_Tab");
            Collection<IWorkflowInstance> workflowInstancesGB = getGeneratedBudgets();
            if (workflowInstancesGB == null || workflowInstancesGB.isEmpty()) {
                iGroup = DirectoryService.getGroupe("ResponsableBudget");
                if (iGroup == null) {
                    getResourceController().alert("Une erreure s'est produite! le groupe responsable budget n'est pas existe, Merci de contacter votre administrateur.");
                    return false;
                }
                if (iGroup.getAllMembers() == null || iGroup.getAllMembers().isEmpty()) {
                    getResourceController().alert("Une erreure s'est produite! aucun utilisateur existe dans le groupe responsable budget, Merci de contacter votre administrateur.");
                    return false;
                }
                respBudget = iGroup.getAllMembers().iterator().next();
                this.respBudgetContext = getWorkflowModule().getContext(respBudget);
                workflowInstanceGB = generateNewBudget(linkedResourcesPB);

            } else {
                workflowInstanceGB = updateGeneratedBudget(workflowInstancesGB.iterator().next() , linkedResourcesPB);
            }
            if(workflowInstanceGB!=null){
                if(!natureBudget.getValue("sys_Reference").equals("0002")){
                    Double CreditsOuvertsCETotal = calculMontantTotal(workflowInstanceGB , "CreditsOuvertsCE");
                    workflowInstanceGB.setValue("TotalDesCreditsOuvertsCE", CreditsOuvertsCETotal);
                }
                Double CreditsOuvertsCPTotal = calculMontantTotal(workflowInstanceGB , "CreditsOuvertsCP");
                workflowInstanceGB.setValue("TotalDesCreditsOuvertsCP", CreditsOuvertsCPTotal);
                workflowInstanceGB.save(this.respBudgetContext);
            }
            else{
                getResourceController().alert("Une erreure s'est produite! Merci de contacter votre administrateur.");
                return false;
            }
            //BudgetGenerate(linkedResourcesPB);
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreure s'est produite! Merci de contacter votre administrateur.");
            return false;
        }
        return super.onBeforeSubmit(action);
    }

    private Double calculMontantTotal(IWorkflowInstance workflowInstanceGB , String fieldtoCalculate) {
        Double montantTotal = 0.0D;
        try {
            Collection<ILinkedResource> linkedResourcesGB = (Collection<ILinkedResource>) workflowInstanceGB.getLinkedResources("RB_Budget_Tab");
            Double montant = null;
            for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                montant = linkedResourceGB.getValue(fieldtoCalculate)!=null ? (Double) linkedResourceGB.getValue(fieldtoCalculate) : Double.valueOf(0.0D);
                montantTotal += montant.doubleValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return montantTotal;
    }

    private IWorkflowInstance generateNewBudget(Collection<ILinkedResource> linkedResourcesPB) {
        if(checkBudgetExist()!=0){
            return null;
        }
        IWorkflowInstance workflowInstance = null;
        try {
            workflowInstance = getWorkflowModule().createWorkflowInstance(this.respBudgetContext, WorkflowsService.getWorflow("Budget", "GenerationDesBudgets_1.0"), null);
            workflowInstance.setValue("AnneeBudgetaire", this.anneeBudgetaire);
            workflowInstance.setValue("TypeBudget", this.typeBudget);
            workflowInstance.setValue("NatureBudget", this.natureBudget);
            ILinkedResource linkedResourceGB = null;
            IStorageResource previousBudget_STO = null;
            String previousyear = String.valueOf(Integer.valueOf(this.anneeBudgetaire)-1);
            for (ILinkedResource linkedResourcePB : linkedResourcesPB) {
                linkedResourceGB = workflowInstance.createLinkedResource("RB_Budget_Tab");
                previousBudget_STO = getBudgetPreviousYear((IStorageResource) linkedResourcePB.getValue("RubriqueBudgetaire"), previousyear);
                if(previousBudget_STO!=null && this.typeBudget.equals("Dépenses")){
                    linkedResourceGB.setValue("RAPN1",previousBudget_STO.getValue("RAP"));
                    linkedResourceGB.setValue("RAP_CURRENT",previousBudget_STO.getValue("RAP"));
                }
                linkedResourceGB.setValue("AnneeBudgetaire", this.anneeBudgetaire);
                linkedResourceGB.setValue("TypeBudget", this.typeBudget);
                linkedResourceGB.setValue("NatureBudget", this.natureBudget);
                linkedResourceGB.setValue("RubriqueBudgetaire", linkedResourcePB.getValue("RubriqueBudgetaire"));
                if(!this.natureBudget.getValue("sys_Reference").equals("0002")){
                    linkedResourceGB.setValue("CreditsOuvertsCE", linkedResourcePB.getValue("MontantDuBudgetCE"));
                }
                linkedResourceGB.setValue("CreditsOuvertsCP", linkedResourcePB.getValue("MontantDuBudgetCP"));
                linkedResourceGB.save(this.respBudgetContext);
                workflowInstance.addLinkedResource(linkedResourceGB);
            }
            workflowInstance.save(this.respBudgetContext);
            return workflowInstance;
        } catch (WorkflowModuleException e) {
            e.printStackTrace();
        } catch (ProjectModuleException e) {
            e.printStackTrace();
        } catch (DirectoryModuleException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IWorkflowInstance updateGeneratedBudget(IWorkflowInstance workflowInstanceGB , Collection<ILinkedResource> linkedResourcesPB)
    {
        boolean trouve = false;
        IWorkflowInstance workflowInstance = workflowInstanceGB;
        try {
            Collection<ILinkedResource> linkedResourcesGB = (Collection<ILinkedResource>) workflowInstanceGB.getLinkedResources("RB_Budget_Tab");
            IStorageResource rubriqueBudgetairePB = null, rubriqueBudgetaireGB = null , previousBudget_STO = null;
            Double montantDuBudgetCE = null, creditsOuvertsCE = null;
            Double montantDuBudgetCP = null, creditsOuvertsCP = null;
            String previousyear = String.valueOf(Integer.valueOf(this.anneeBudgetaire)-1);
            for (ILinkedResource linkedResourcePB : linkedResourcesPB) {
                rubriqueBudgetairePB = (IStorageResource) linkedResourcePB.getValue("RubriqueBudgetaire");
                montantDuBudgetCP = (Double) linkedResourcePB.getValue("MontantDuBudgetCP");
                montantDuBudgetCE = (Double) linkedResourcePB.getValue("MontantDuBudgetCE");
                trouve = false;
                for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                    rubriqueBudgetaireGB = (IStorageResource) linkedResourceGB.getValue("RubriqueBudgetaire");
                    creditsOuvertsCE = (Double) linkedResourceGB.getValue("CreditsOuvertsCE");
                    creditsOuvertsCP = (Double) linkedResourceGB.getValue("CreditsOuvertsCP");
                    if (rubriqueBudgetairePB.getProtocolURI().equals(rubriqueBudgetaireGB.getProtocolURI())) {
                        if(!natureBudget.getValue("sys_Reference").equals("0002")){
                            creditsOuvertsCE = creditsOuvertsCE.doubleValue() + montantDuBudgetCE.doubleValue();
                            linkedResourceGB.setValue("CreditsOuvertsCE", creditsOuvertsCE);
                        }
                        creditsOuvertsCP = creditsOuvertsCP.doubleValue() + montantDuBudgetCP.doubleValue();
                        linkedResourceGB.setValue("CreditsOuvertsCP", creditsOuvertsCP);
                        linkedResourceGB.save(this.respBudgetContext);
                        trouve = true;
                        break;
                    }
                }
                if (!trouve) {
                    ILinkedResource linkedResourceGB = workflowInstanceGB.createLinkedResource("RB_Budget_Tab");
                    linkedResourceGB.setValue("Protocol_URI_Original" , linkedResourceGB.getProtocolURI());
                    linkedResourceGB.setValue("AnneeBudgetaire", this.anneeBudgetaire);
                    linkedResourceGB.setValue("TypeBudget", this.typeBudget);
                    linkedResourceGB.setValue("NatureBudget", this.natureBudget);
                    linkedResourceGB.setValue("RubriqueBudgetaire", linkedResourcePB.getValue("RubriqueBudgetaire"));
                    if(!natureBudget.getValue("sys_Reference").equals("0002")){
                        linkedResourceGB.setValue("CreditsOuvertsCE", montantDuBudgetCE);
                    }
                    linkedResourceGB.setValue("CreditsOuvertsCP", montantDuBudgetCP);
                    previousBudget_STO = getBudgetPreviousYear((IStorageResource) linkedResourcePB.getValue("RubriqueBudgetaire"), previousyear);
                    if(previousBudget_STO!=null && this.typeBudget.equals("Dépenses")){
                        linkedResourceGB.setValue("RAPN1",previousBudget_STO.getValue("RAP"));
                        linkedResourceGB.setValue("RAP_CURRENT",previousBudget_STO.getValue("RAP"));
                    }
                    linkedResourceGB.save(this.respBudgetContext);
                    workflowInstanceGB.addLinkedResource(linkedResourceGB);
                }
            }
            workflowInstanceGB.save(this.respBudgetContext);
            return workflowInstance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Collection<IWorkflowInstance> getGeneratedBudgets() {
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), this.typeBudget);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addInConstraint("DocumentState", Arrays.asList("Demande à modifier", "En cours"));
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            return workflowInstances;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public  IStorageResource getBudgetPreviousYear(IStorageResource RubriqueBudgetaire, String PreviousYEAR) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            IViewController viewController = workflowModule.getViewController(DirectoryService.getSysAdminContext(), IResource.class);
            viewController.addEqualsConstraint("TypeBudget", this.typeBudget);
            viewController.addEqualsConstraint("NatureBudget", this.natureBudget);
            viewController.addEqualsConstraint("AnneeBudgetaire", PreviousYEAR);
            viewController.addEqualsConstraint("RubriqueBudgetaire", RubriqueBudgetaire);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("ReferentielsBudget" , "Budget"));
            if(storageResources!=null && !storageResources.isEmpty()){
                return storageResources.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int checkBudgetExist() {
        int count = 0;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), this.anneeBudgetaire);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), this.typeBudget);
            viewController.addEqualsConstraint(Const.Properties.NatureBudget.toString(), this.natureBudget);
            viewController.addNotInConstraint("DocumentState", Arrays.asList("Demande à modifier", "En cours" , "Budget ouvert (Nouvelle version en cours)" , "Budget rejeté"));
            count = viewController.evaluateSize(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            return count;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
