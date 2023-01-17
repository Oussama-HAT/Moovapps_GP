package com.moovapps.gp.achats.da.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class ValidationDirecteur extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        getWorkflowInstance().setValue("TotalBudgetDisponible",getBudgetDispo());
        return super.onAfterLoad();
    }

    private BigDecimal getBudgetDispo()
    {
        BigDecimal budgetDispo = BigDecimal.ZERO;
        IContext sysContext = getWorkflowModule().getSysadminContext();
        IOrganization organization;
        IProject project;
        ICatalog catalog;
        IResourceDefinition iResourceDefinition;
        Collection<IStorageResource> budgets;
        try {
            organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            project = getProjectModule().getProject(sysContext, "ADMINISTRATIONGP", organization);
            catalog = getWorkflowModule().getCatalog(sysContext, "ReferentielsBudget", ICatalog.IType.STORAGE, project);
            iResourceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Budget");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            //controller.addEqualsConstraint("", "");
            budgets = controller.evaluate(iResourceDefinition);

            if(budgets!=null)
                for (IStorageResource budget: budgets)
                {
                    if(budget.getValue("Disponible")!=null)
                    {
                        budgetDispo = budgetDispo.add(castToBigDecimal(budget.getValue("Disponible")));
                    }
                }

        } catch (DirectoryModuleException | ProjectModuleException | WorkflowModuleException e) {
            e.printStackTrace();
        }


        return budgetDispo;
    }
}
