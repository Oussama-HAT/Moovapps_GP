package com.moovapps.gp.budget.preparationBudget.documents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdp.utils.CollectionUtils;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

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
                    BigDecimal CreditsOuvertsCETotal = calculMontantTotal(workflowInstanceGB , "CreditsOuvertsCE");
                    workflowInstanceGB.setValue("TotalDesCreditsOuvertsCE", CreditsOuvertsCETotal);
                }
                BigDecimal CreditsOuvertsCPTotal = calculMontantTotal(workflowInstanceGB , "CreditsOuvertsCP");
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

    private BigDecimal calculMontantTotal(IWorkflowInstance workflowInstanceGB , String fieldtoCalculate) {
        BigDecimal montantTotal = BigDecimal.ZERO;
        try {
            Collection<ILinkedResource> linkedResourcesGB = (Collection<ILinkedResource>) workflowInstanceGB.getLinkedResources("RB_Budget_Tab");
            BigDecimal montant = null;
            for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                montant = linkedResourceGB.getValue(fieldtoCalculate)!=null ? castToBigDecimal(linkedResourceGB.getValue(fieldtoCalculate)) : BigDecimal.ZERO;
                montantTotal = montantTotal.add(montant);
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
            String previousyear = String.valueOf(Integer.parseInt(this.anneeBudgetaire)-1);
            for (ILinkedResource linkedResourcePB : linkedResourcesPB) {
                linkedResourceGB = workflowInstance.createLinkedResource("RB_Budget_Tab");
                linkedResourceGB.setValue("AnneeBudgetaire", this.anneeBudgetaire);
                linkedResourceGB.setValue("TypeBudget", this.typeBudget);
                linkedResourceGB.setValue("NatureBudget", this.natureBudget);
                linkedResourceGB.setValue("RubriqueBudgetaire", linkedResourcePB.getValue("RubriqueBudgetaire"));
                if(!this.natureBudget.getValue("sys_Reference").equals("0002")){
                    linkedResourceGB.setValue("CreditsOuvertsCE", castToBigDecimal(linkedResourcePB.getValue("MontantDuBudgetCE")));
                }
                linkedResourceGB.setValue("CreditsOuvertsCP", castToBigDecimal(linkedResourcePB.getValue("MontantDuBudgetCP")));
                linkedResourceGB.save(this.respBudgetContext);
                workflowInstance.addLinkedResource(linkedResourceGB);
            }
            workflowInstance.save(this.respBudgetContext);
            return workflowInstance;
        } catch (WorkflowModuleException | ProjectModuleException | DirectoryModuleException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IWorkflowInstance updateGeneratedBudget(IWorkflowInstance workflowInstanceGB , Collection<ILinkedResource> linkedResourcesPB) {
        boolean trouve = false;
        IWorkflowInstance workflowInstance = workflowInstanceGB;
        try {
            Collection<ILinkedResource> linkedResourcesGB = CollectionUtils.cast(workflowInstanceGB.getLinkedResources("RB_Budget_Tab"),ILinkedResource.class);
            IStorageResource rubriqueBudgetairePB = null, rubriqueBudgetaireGB = null ;
            BigDecimal montantDuBudgetCE = null, creditsOuvertsCE = null;
            BigDecimal montantDuBudgetCP = null, creditsOuvertsCP = null;
            String previousyear = String.valueOf(Integer.parseInt(this.anneeBudgetaire)-1);
            for (ILinkedResource linkedResourcePB : linkedResourcesPB) {
                rubriqueBudgetairePB = (IStorageResource) linkedResourcePB.getValue("RubriqueBudgetaire");
                montantDuBudgetCP =  castToBigDecimal(linkedResourcePB.getValue("MontantDuBudgetCP"));
                montantDuBudgetCE = castToBigDecimal(linkedResourcePB.getValue("MontantDuBudgetCE"));
                trouve = false;
                for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                    rubriqueBudgetaireGB = (IStorageResource) linkedResourceGB.getValue("RubriqueBudgetaire");
                    creditsOuvertsCE = castToBigDecimal(linkedResourceGB.getValue("CreditsOuvertsCE"));
                    creditsOuvertsCP = castToBigDecimal(linkedResourceGB.getValue("CreditsOuvertsCP"));
                    if (rubriqueBudgetairePB.getProtocolURI().equals(rubriqueBudgetaireGB.getProtocolURI())) {
                        if(!natureBudget.getValue("sys_Reference").equals("0002")){
                            creditsOuvertsCE = creditsOuvertsCE.add(montantDuBudgetCE);
                            linkedResourceGB.setValue("CreditsOuvertsCE", new BigDecimal(creditsOuvertsCE.toString()));
                        }
                        creditsOuvertsCP = creditsOuvertsCP.add(montantDuBudgetCP);
                        linkedResourceGB.setValue("CreditsOuvertsCP", new BigDecimal(creditsOuvertsCP.toString()));
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
