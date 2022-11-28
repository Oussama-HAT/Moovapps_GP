package com.moovapps.gp.budget.generationBudget.views;

import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.AbstractActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import com.moovapps.gp.budget.helpers.Const;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.List;

public class NouvelleVersionBudget extends BaseViewExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnContext = DirectoryService.getLoggedOnContext();

    public void onPrepareColumns(List list) {
        try {
            ViewModelColumn viewModelColumn = new ViewModelColumn("NouvelleVersion", "", 6);
            viewModelColumn.setWidth("1%");
            list.add(viewModelColumn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPrepareColumns(list);
    }

    public void onPrepareItem(ViewItem iViewItem) {
        try {
            getView().getName();
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            IWorkflowInstance workflowInstance = (IWorkflowInstance)iViewItem.getResource();
            String documentState = (String)workflowInstance.getValue("DocumentState");
            if (documentState.equals("Budget ouvert")) {
                CtlButton button = new CtlButton("NouvelleVersion", new CtlText("Nouvelle version"));
                button.setParam(workflowInstance);
                button.setStyle(2);
                button.addActionListener((AbstractActionListener)this.NouvelleVersion);
                viewModelItem.setValue("NouvelleVersion", button);
            } else if (documentState.equals("Budget ouvert (Nouvelle version en cours)")) {
                CtlText text = new CtlText(documentState);
                viewModelItem.setValue("NouvelleVersion", text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected ActionListener NouvelleVersion = new ActionListener() {
        private static final long serialVersionUID = 1L;

        public void onClick(ActionEvent paramActionEvent) {
            try {
                IWorkflowInstance workflowInstanceSource = (IWorkflowInstance)((CtlButton)paramActionEvent.getSource()).getParam();
                IWorkflowInstance workflowInstanceCible = WorkflowsService.duplicateWorkflowInstance(workflowInstanceSource, NouvelleVersionBudget.this.loggedOnContext);
                workflowInstanceCible.setValue("URIBudgetV1", workflowInstanceSource);
                workflowInstanceCible.setValue("VersionDuBudget", WorkflowsService.generateVersion(workflowInstanceSource));
                workflowInstanceCible.save(NouvelleVersionBudget.this.sysAdminContext);
                WorkflowsService.executeAction(workflowInstanceSource, NouvelleVersionBudget.this.loggedOnContext, Const.ACTION_GENERER_VERSION_GB, "");
                NouvelleVersionBudget.this.getView().refreshItems();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
