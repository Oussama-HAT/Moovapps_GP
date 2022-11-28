package com.moovapps.gp.budget.imputation.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.components.listeners.ConfirmBoxListener;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

public class Creation extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        IResourceController iResourceController = getResourceController();
        NamedContainer namedContainer = iResourceController.getButtonContainer(IResourceController.BOTTOM_CONTAINER);
        List<IWidget> widgets = namedContainer.getWidgets();
        for (IWidget iWidget : widgets)
        {
            CtlButton button = (CtlButton) iWidget;

            if(!button.getName().equals(""))
            {
                    button.setHidden(true);
            }
        }
        return super.onAfterLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if(property.getName().equals("RubriqueBudgetaireNV"))
        {
            IStorageResource rebriqueBudget = (IStorageResource) getWorkflowInstance().getValue("RubriqueBudgetaireNV");
            String anneeBudget = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            IStorageResource budget = null;

            if(rebriqueBudget!=null)
            {
                budget = getBudget(rebriqueBudget,"rubrique",anneeBudget);
            }
            if(budget!=null)
            {
                getWorkflowInstance().setValue("Disponible",budget.getValue("Disponible"));
            }
        }
        if(property.getName().equals("ProgrammeDEmploi"))
        {
            IStorageResource programmeDEmploi = (IStorageResource) getWorkflowInstance().getValue("ProgrammeDEmploi");
            String anneeBudget = (String) getWorkflowInstance().getValue("AnneeBudgetaire");
            IStorageResource budget = null;

            if(programmeDEmploi!=null)
            {
                budget = getBudget(programmeDEmploi,"programme",anneeBudget);
            }
            if(budget!=null)
            {
                getWorkflowInstance().setValue("Disponible",budget.getValue("Disponible"));
            }

        }
        if(property.getName().equals("MontantAImputer"))
        {
            Number montantAImputer = (Number) getWorkflowInstance().getValue("MontantAImputer");
            Number disponible = (Number) getWorkflowInstance().getValue("Disponible");

            if(montantAImputer!=null && disponible!=null)
            {
                if(montantAImputer.intValue()>disponible.intValue())
                {
                    getResourceController().alert("Attention! Le montant à imputer est superieur au budget disponible");
                }
            }else if(disponible==null)
            {
                getResourceController().alert("Le budget rubrique introuvable!");
                //getWorkflowInstance().setValue("MontantAImputer",null);
            }
        }

        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {

        return super.onBeforeSubmit(action);
    }

    private IStorageResource getBudget(IStorageResource RubriqueOrProgramme, String type, String annee)
    {
        Collection<IStorageResource> collection = null;
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "ADMINISTRATIONGP", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext,"ReferentielsBudget", ICatalog.IType.STORAGE,project);
            IResourceDefinition w = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Budget");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("AnneeBudgetaire",annee);
            if(type.equals("rubrique"))
            {
                controller.addEqualsConstraint("RubriqueBudgetaire",RubriqueOrProgramme);
            }else if(type.equals("programme"))
            {
                controller.addEqualsConstraint("ProgrammeDEmploi",RubriqueOrProgramme);
            }
            collection = controller.evaluate(w);
            if(collection!=null)
            {
                return collection.iterator().next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
