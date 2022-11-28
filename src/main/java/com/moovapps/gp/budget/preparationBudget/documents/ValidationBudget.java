package com.moovapps.gp.budget.preparationBudget.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import org.apache.ecs.html.Col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ValidationBudget extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected IContext respBudgetContext = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            String typeBudget = (String) getWorkflowInstance().getValue("TypeBudget");
            IStorageResource natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
            Collection<ILinkedResource> linkedResourcesPB = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("PB_Budget_Tab");
            MAJGenerationBudget(anneeBudgetaire, typeBudget, natureBudget, linkedResourcesPB);
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreure s'est produite! Merci de contacter votre administrateur.");
            return false;
        }
        return super.onBeforeSubmit(action);
    }

    private void MAJGenerationBudget(String anneeBudgetaire, String typeBudget, IStorageResource natureBudget, Collection<ILinkedResource> linkedResourcesPB) {
        IWorkflowInstance workflowInstanceGB = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint("AnneeBudgetaire", anneeBudgetaire);
            viewController.addEqualsConstraint("TypeBudget", typeBudget);
            viewController.addEqualsConstraint("NatureBudget", natureBudget);
            viewController.addNotInConstraint("DocumentState", Arrays.asList("Budget ouvert", "Budget ouvert (Nouvelle version en cours)", "Budget clôturé"));
            //viewController.addEqualsConstraint("DocumentState", "En cours");
            Collection<IWorkflowInstance> workflowInstancesGB = viewController.evaluate(WorkflowsService.getWorflowContainer("Budget", "GenerationDesBudgets"));
            if (workflowInstancesGB == null || workflowInstancesGB.isEmpty()) {
                IGroup iGroup = DirectoryService.getGroupe("ResponsableBudget");
                this.respBudgetContext = getWorkflowModule().getContext(iGroup.getAllMembers().iterator().next());
                workflowInstanceGB = getWorkflowModule().createWorkflowInstance(this.respBudgetContext, WorkflowsService.getWorflow("Budget", "GenerationDesBudgets_1.0"), null);
                workflowInstanceGB.setValue("AnneeBudgetaire", anneeBudgetaire);
                workflowInstanceGB.setValue("TypeBudget", typeBudget);
                workflowInstanceGB.setValue("NatureBudget", natureBudget);
                ILinkedResource linkedResourceGB = null;
                for (ILinkedResource linkedResourcePB : linkedResourcesPB) {
                    linkedResourceGB = workflowInstanceGB.createLinkedResource("RB_Budget_Tab");
                    linkedResourceGB.setValue("AnneeBudgetaire", anneeBudgetaire);
                    linkedResourceGB.setValue("TypeBudget", typeBudget);
                    linkedResourceGB.setValue("NatureBudget", natureBudget);
                    linkedResourceGB.setValue("RubriqueBudgetaire", linkedResourcePB.getValue("RubriqueBudgetaire"));
                    linkedResourceGB.setValue("CreditsOuvertsCE", linkedResourcePB.getValue("MontantDuBudgetCE"));
                    linkedResourceGB.setValue("CreditsOuvertsCP", linkedResourcePB.getValue("MontantDuBudgetCP"));
                    linkedResourceGB.save(this.respBudgetContext);
                    workflowInstanceGB.addLinkedResource(linkedResourceGB);
                }
                workflowInstanceGB.save(this.respBudgetContext);
            } else {
                workflowInstanceGB = workflowInstancesGB.iterator().next();
                Collection<ILinkedResource> linkedResourcesGB = (Collection<ILinkedResource>) workflowInstanceGB.getLinkedResources("RB_Budget_Tab");
                IStorageResource rubriqueBudgetairePB = null, rubriqueBudgetaireGB = null;
                Collection<IStorageResource> storageResourcesAxesGB = null, storageResourcesAxesPB = null;
                ArrayList<IStorageResource> axes = null;
                Double montantPB = null, montantGB = null;
                Boolean trouve = Boolean.valueOf(false);
                for (ILinkedResource linkedResourcePB : linkedResourcesPB) {
                    rubriqueBudgetairePB = (IStorageResource) linkedResourcePB.getValue("RubriqueBudgetaire");
                    //storageResourcesAxesPB = (Collection<IStorageResource>)linkedResourcePB.getValue("Axes");
                    montantPB = (Double) linkedResourcePB.getValue("MontantTotal");
                    trouve = Boolean.valueOf(false);
                    axes = new ArrayList<>();
                    for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                        rubriqueBudgetaireGB = (IStorageResource) linkedResourceGB.getValue("RubriqueBudgetaire");
                        storageResourcesAxesGB = (Collection<IStorageResource>) linkedResourceGB.getValue("Axes");
                        montantGB = (Double) linkedResourceGB.getValue("MontantTotal");
                        if (rubriqueBudgetairePB.getURI().equals(rubriqueBudgetaireGB.getURI())) {
                            montantGB = Double.valueOf(montantGB.doubleValue() + montantPB.doubleValue());
                            linkedResourceGB.setValue("MontantTotal", montantGB);
                            if (storageResourcesAxesGB != null && !storageResourcesAxesGB.isEmpty()) {
                                axes.addAll(storageResourcesAxesGB);
                                if (storageResourcesAxesPB != null && !storageResourcesAxesPB.isEmpty()) {
                                    axes.addAll(storageResourcesAxesPB);
                                    linkedResourceGB.setValue("Axes", axes);
                                }
                            } else {
                                axes.addAll(storageResourcesAxesPB);
                                linkedResourceGB.setValue("Axes", axes);
                            }
                            linkedResourceGB.save(this.respBudgetContext);
                            trouve = Boolean.valueOf(true);
                            break;
                        }
                    }
                    if (!trouve.booleanValue()) {
                        ILinkedResource linkedResourceGB = workflowInstanceGB.createLinkedResource("RB_Budget_Tab");
                        linkedResourceGB.setValue("AnneeBudgetaire", anneeBudgetaire);
                        linkedResourceGB.setValue("TypeBudget", typeBudget);
                        linkedResourceGB.setValue("NatureBudget", natureBudget);
                        linkedResourceGB.setValue("RubriqueBudgetaire", linkedResourcePB.getValue("RubriqueBudgetaire"));
                        linkedResourceGB.setValue("MontantTotal", montantPB);
                        linkedResourceGB.save(this.respBudgetContext);
                        workflowInstanceGB.addLinkedResource(linkedResourceGB);
                    }
                }
                workflowInstanceGB.save(this.respBudgetContext);
            }
            Double montantTotal = calculMontantTotal(workflowInstanceGB);
            workflowInstanceGB.setValue("MontantTotal", montantTotal);
            workflowInstanceGB.save(this.respBudgetContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double calculMontantTotal(IWorkflowInstance workflowInstanceGB) {
        Double montantTotal = Double.valueOf(0.0D);
        try {
            Collection<ILinkedResource> linkedResourcesGB = (Collection<ILinkedResource>) workflowInstanceGB.getLinkedResources("RB_Budget_Tab");
            Double montantGB = null;
            for (ILinkedResource linkedResourceGB : linkedResourcesGB) {
                montantGB = (Double) linkedResourceGB.getValue("MontantTotal");
                montantTotal = Double.valueOf(montantTotal.doubleValue() + montantGB.doubleValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return montantTotal;
    }
}
