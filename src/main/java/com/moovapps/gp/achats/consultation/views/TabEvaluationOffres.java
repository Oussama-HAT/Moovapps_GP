package com.moovapps.gp.achats.consultation.views;

import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlAutocompleteList;
import com.axemble.vdp.ui.framework.widgets.CtlListView;
import com.axemble.vdp.ui.framework.widgets.CtlTextArea;
import com.axemble.vdp.ui.framework.widgets.INamedWidget;
import com.axemble.vdp.ui.framework.widgets.list.Option;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TabEvaluationOffres extends BaseViewExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    protected IWorkflowModule workflowModule = null;

    protected ITaskInstance currentTaskInstance = null;

    protected String taskName = null;

    protected String tableName = null;

    protected Object valeur = null;

    public void onPrepareColumns(List list) {
        try {
            this.workflowModule = getWorkflowModule();
            this.sysAdminContext = getWorkflowModule().getSysadminContext();
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            NamedContainer topButtonContainer = getView().getButtonsContainer();
            List<IWidget> buttonList = topButtonContainer.getWidgets();
            for (IWidget iWidget : buttonList) {
                INamedWidget iNamedWidget = (INamedWidget)iWidget;
                if (!iNamedWidget.getName().equals("delete"))
                    iNamedWidget.setHidden(true);
            }
            this.currentTaskInstance = getWorkflowInstance().getCurrentTaskInstance(this.loggedOnUserContext);
            if (this.currentTaskInstance != null)
                this.taskName = this.currentTaskInstance.getTask().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPrepareColumns(list);
    }

    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            IStorageResource linkedIWorkflowInstance = (IStorageResource)iViewItem.getResource();
            if (this.taskName != null && this.taskName.equals("EvaluationDesOffres")) {
                CtlAutocompleteList adjudicataire = ctlAutocompleteList("Adjudicataire", linkedIWorkflowInstance, (List<Option>)null);
                viewModelItem.setValue("Adjudicataire", adjudicataire);
                CtlTextArea commentaire = ctlTextArea("CommentaireCommission", linkedIWorkflowInstance);
                viewModelItem.setValue("CommentaireCommission", commentaire);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CtlAutocompleteList ctlAutocompleteList(String name, IStorageResource linkedIWorkflowInstance, List<Option> options) {
        CtlAutocompleteList ctlAutocompleteList = new CtlAutocompleteList();
        ctlAutocompleteList.setThrowEvents(true);
        if (options != null) {
            ctlAutocompleteList.setOptions(options);
        } else {
            ctlAutocompleteList.setOptions((Collection<Option>) linkedIWorkflowInstance.getList(name));
        }
        ctlAutocompleteList.setParam(linkedIWorkflowInstance);
        ILinkedResource linkedResource = getLinkedResourceMembre(linkedIWorkflowInstance);
        if (linkedResource != null)
            ctlAutocompleteList.setSelectedKey(linkedResource.getValue(name));
        ctlAutocompleteList.setSysname(name);
        ctlAutocompleteList.addChangeListener(this.listener);
        return ctlAutocompleteList;
    }

    public CtlTextArea ctlTextArea(String name, IStorageResource linkedIWorkflowInstance) {
        CtlTextArea ctlTextArea = new CtlTextArea();
        ctlTextArea.setRows(1);
        ctlTextArea.setParam(linkedIWorkflowInstance);
        ctlTextArea.setSysname(name);
        ILinkedResource linkedResource = getLinkedResourceMembre(linkedIWorkflowInstance);
        if (linkedResource != null) {
            this.valeur = linkedResource.getValue("Commentaire");
            if (this.valeur != null)
                ctlTextArea.setLabel(String.valueOf(this.valeur));
        }
        ctlTextArea.setThrowEvents(true);
        ctlTextArea.addChangeListener(this.listener);
        return ctlTextArea;
    }

    protected ChangeListener listener = new ChangeListener() {
        public void onChange(ChangeEvent paramChangeEvent) {
            IStorageResource linkedIWorkflowInstance = null;
            Object object = paramChangeEvent.getSource();
            if (CtlTextArea.class.equals(object.getClass())) {
                CtlTextArea txtBox = (CtlTextArea)object;
                linkedIWorkflowInstance = (IStorageResource)txtBox.getParam();
                ILinkedResource linkedResource = TabEvaluationOffres.this.getLinkedResourceMembre(linkedIWorkflowInstance);
                if (linkedResource != null) {
                    linkedResource.setValue("Commentaire", txtBox.getLabel());
                    linkedResource.save(TabEvaluationOffres.this.sysAdminContext);
                }
                linkedIWorkflowInstance.save(TabEvaluationOffres.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                linkedIWorkflowInstance = (IStorageResource)box.getParam();
                TabEvaluationOffres.this.MAJLinkedResourceMembre(linkedIWorkflowInstance, box);
            }
        }
    };

    private ILinkedResource getLinkedResourceMembre(IStorageResource linkedIWorkflowInstance) {
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)linkedIWorkflowInstance.getValue("EvaluationDeLOffre_Tab");
            if (linkedResources != null && !linkedResources.isEmpty()) {
                IUser membre = null;
                for (ILinkedResource linkedResource : linkedResources) {
                    membre = (IUser)linkedResource.getValue("NomPrenom");
                    if (membre != null && membre.equals(this.loggedOnUserContext.getUser()))
                        return linkedResource;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void MAJLinkedResourceMembre(IStorageResource linkedIWorkflowInstance, CtlAutocompleteList box) {
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)linkedIWorkflowInstance.getValue("EvaluationDeLOffre_Tab");
            if (linkedResources != null && !linkedResources.isEmpty()) {
                IUser membre = null;
                for (ILinkedResource linkedResource : linkedResources) {
                    membre = (IUser)linkedResource.getValue("NomPrenom");
                    if (membre != null && membre.equals(this.loggedOnUserContext.getUser())) {
                        linkedResource.setValue(box.getSysname(), box.getSelectedKey());
                        if (box.getSelectedKey() != null) {
                            linkedResource.setValue("DateDeLEvaluation", new Date());
                        } else {
                            linkedResource.setValue("DateDeLEvaluation", null);
                        }
                        linkedResource.save(this.loggedOnUserContext);
                        break;
                    }
                }
                linkedIWorkflowInstance.save(this.loggedOnUserContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            try {
                IWorkflowInstance linkedWorkflowInstance = TabEvaluationOffres.this.getWorkflowModule().createWorkflowInstance(TabEvaluationOffres.this.loggedOnUserContext, WorkflowsService.getWorflow("Achats", "Fournisseur_1.0"), null);
                TabEvaluationOffres.this.getWorkflowInstance().addLinkedWorkflowInstance("Fournisseurs_SP", linkedWorkflowInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabEvaluationOffres.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument)item.getParam();
                TabEvaluationOffres.this.getWorkflowInstance().save(TabEvaluationOffres.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };
}
