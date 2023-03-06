package com.moovapps.gp.budget.ordreRecette.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.gp.budget.utils.Const;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class ValiderOrdreRecette extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();
    private BigDecimal TotalRecette_RAP = BigDecimal.ZERO;
    private BigDecimal montantRecette = BigDecimal.ZERO;
    @Override
    public boolean onBeforeSubmit(IAction action) {
        try{
            if(action.getName().equals("Accepter")) {
                String anneeBudgetaire = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
                IStorageResource sto_natureBudget = (IStorageResource) getWorkflowInstance().getValue("NatureBudget");
                Collection<ILinkedResource> detailsLinkedResources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("OR_Budget_Tab");
                Collection<ILinkedResource> linkedResources = getRubriqueBudgetByCurrentBudget(anneeBudgetaire, sto_natureBudget);
                if (linkedResources == null && linkedResources.isEmpty()) {
                    getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                    return false;
                }
                for (ILinkedResource detail : detailsLinkedResources) {
                    ILinkedResource iLinkedResource = linkedResources.stream()
                            .filter(obj -> ((IStorageResource) obj.getValue("RubriqueBudgetaire")).getValue("RubriqueBudgetaire").equals(detail.getValue("RubriqueBudgetaire")))
                            .findFirst()
                            .orElse(null);

                    if (iLinkedResource == null) {
                        getResourceController().alert(getWorkflowModule().getStaticString("LG_RB_NOT_FOUND"));
                        return false;
                    }
                    this.montantRecette = castToBigDecimal(detail.getValue("MontantRecette"));
                    this.TotalRecette_RAP = iLinkedResource.getValue("TotalRecettesDeLAnnee") != null ? castToBigDecimal(iLinkedResource.getValue("TotalRecettesDeLAnnee")) : BigDecimal.ZERO;
                    iLinkedResource.setValue("TotalRecettesDeLAnnee", this.TotalRecette_RAP.add(this.montantRecette));
                    iLinkedResource.save(this.sysAdminContext);
                    iLinkedResource.getParentInstance().save(this.sysAdminContext);
                }
                BigDecimal montant = castToBigDecimal(getWorkflowInstance().getValue("MontantTotalRecette"));
                String compte = (String) getWorkflowInstance().getValue("Compte");
                IStorageResource compteRef = getCompte(compte);
                IResourceDefinition iResourceDefinition = DataUniversService.getResourceDefinition("ReferentielsBudget", "CompteTresorerie");
                if(compteRef == null){
                    compteRef = getWorkflowModule().createStorageResource(this.sysAdminContext, iResourceDefinition, null);
                }
                BigDecimal solde = compteRef.getValue("Solde") !=null ? castToBigDecimal(compteRef.getValue("Solde")) : BigDecimal.ZERO;
                solde = solde.add(montant);
                compteRef.setValue("sys_Title",compte);
                compteRef.setValue("Solde",solde);
                compteRef.save(this.sysAdminContext);
                IResourceDefinition tresorieDefinition = DataUniversService.getResourceDefinition("ReferentielsBudget", "Tresorie");
                IStorageResource Tresorerie = getWorkflowModule().createStorageResource(this.sysAdminContext, tresorieDefinition, null);
                Tresorerie.setValue("AnneeBudgetaire", anneeBudgetaire);
                Tresorerie.setValue("Compte", compte);
                Tresorerie.setValue("CompteTresorerie", compteRef);
                Tresorerie.setValue("Date", getWorkflowInstance().getValue("Date"));
                Tresorerie.setValue("Reference", getWorkflowInstance().getValue(IProperty.System.REFERENCE));
                Tresorerie.setValue("Tiers", getWorkflowInstance().getValue("Fournisseur"));
                Tresorerie.setValue("Type", "Recette");
                Tresorerie.setValue("Montant", castToBigDecimal(getWorkflowInstance().getValue("MontantTotalRecette")));
                Tresorerie.save(this.sysAdminContext);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }


    private Collection<ILinkedResource> getRubriqueBudgetByCurrentBudget(String Annee , IStorageResource natureBudget) {
        Collection<ILinkedResource> linkedResources = null;
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext);
            viewController.addEqualsConstraint(Const.Properties.AnneeBudgetaire.toString(), Annee);
            viewController.addEqualsConstraint(Const.Properties.TypeBudget.toString(), "Recettes");
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
