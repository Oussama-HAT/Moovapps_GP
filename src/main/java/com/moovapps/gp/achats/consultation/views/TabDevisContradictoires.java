package com.moovapps.gp.achats.consultation.views;

import com.axemble.fc.file.domain.DBFile;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IViewController;
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
import com.axemble.vdp.ui.framework.widgets.CtlCheckBox;
import com.axemble.vdp.ui.framework.widgets.CtlComboBox;
import com.axemble.vdp.ui.framework.widgets.CtlDate;
import com.axemble.vdp.ui.framework.widgets.CtlListView;
import com.axemble.vdp.ui.framework.widgets.CtlMultipleFileUpload;
import com.axemble.vdp.ui.framework.widgets.CtlNumber;
import com.axemble.vdp.ui.framework.widgets.CtlRadioGroup;
import com.axemble.vdp.ui.framework.widgets.CtlTextArea;
import com.axemble.vdp.ui.framework.widgets.CtlTextBox;
import com.axemble.vdp.ui.framework.widgets.INamedWidget;
import com.axemble.vdp.ui.framework.widgets.list.Option;
import com.axemble.vdp.utils.parameters.TempUploadFile;
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TabDevisContradictoires extends BaseViewExtension {
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
            if (this.taskName != null && this.taskName.equals("SaisieDesDevisContradictoires")) {
                CtlDate dateReceptionDevis = ctlDate("DateDeReceptionDuDevis", linkedIWorkflowInstance);
                viewModelItem.setValue("DateDeReceptionDuDevis", dateReceptionDevis);
                CtlNumber montantDevis = ctlNumber("MontantDuDevisHT", linkedIWorkflowInstance);
                viewModelItem.setValue("MontantDuDevisHT", montantDevis);
                CtlMultipleFileUpload devis = ctlMultipleFileUpload("Devis", linkedIWorkflowInstance);
                viewModelItem.setValue("Devis", devis);
                CtlMultipleFileUpload offreTechnique = ctlMultipleFileUpload("OffreTechniquePJ", linkedIWorkflowInstance);
                viewModelItem.setValue("OffreTechniquePJ", offreTechnique);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CtlRadioGroup ctlRadioGroup(String name, ILinkedResource linkedIWorkflowInstance) {
        CtlRadioGroup ctlRadio = new CtlRadioGroup();
        ctlRadio.setOptions((Collection<Option>) linkedIWorkflowInstance.getList(name));
        ctlRadio.setParam(linkedIWorkflowInstance);
        ctlRadio.setSelectedKey(linkedIWorkflowInstance.getValue(name));
        ctlRadio.setSysname(name);
        ctlRadio.showResetButton(false);
        ctlRadio.addChangeListener(this.listener);
        return ctlRadio;
    }

    public CtlNumber ctlNumber(String name, IStorageResource linkedIWorkflowInstance) {
        CtlNumber ctlNumber = new CtlNumber();
        ctlNumber.setParam(linkedIWorkflowInstance);
        ctlNumber.setSysname(name);
        this.valeur = linkedIWorkflowInstance.getValue(name);
        if (this.valeur != null)
            ctlNumber.setLabel(String.valueOf(this.valeur));
        ctlNumber.addChangeListener(this.listener);
        return ctlNumber;
    }

    public CtlAutocompleteList ctlAutocompleteList(String name, IStorageResource linkedIWorkflowInstance, List<Option> options) {
        CtlAutocompleteList ctlAutocompleteList = new CtlAutocompleteList();
        if (options != null) {
            ctlAutocompleteList.setOptions(options);
        } else {
            ctlAutocompleteList.setOptions((Collection<Option>) linkedIWorkflowInstance.getList(name));
        }
        ctlAutocompleteList.setParam(linkedIWorkflowInstance);
        ctlAutocompleteList.setSelectedKey(linkedIWorkflowInstance.getValue(name));
        ctlAutocompleteList.setSysname(name);
        ctlAutocompleteList.setThrowEvents(true);
        ctlAutocompleteList.addChangeListener(this.listener);
        return ctlAutocompleteList;
    }

    public CtlTextBox ctlTextBox(String name, IStorageResource linkedIWorkflowInstance) {
        CtlTextBox ctlTextBox = new CtlTextBox();
        ctlTextBox.setParam(linkedIWorkflowInstance);
        ctlTextBox.setSysname(name);
        this.valeur = linkedIWorkflowInstance.getValue(name);
        if (this.valeur != null)
            ctlTextBox.setLabel(String.valueOf(this.valeur));
        ctlTextBox.addChangeListener(this.listener);
        return ctlTextBox;
    }

    public CtlTextArea ctlTextArea(String name, IStorageResource linkedIWorkflowInstance) {
        CtlTextArea ctlTextArea = new CtlTextArea();
        ctlTextArea.setRows(1);
        ctlTextArea.setParam(linkedIWorkflowInstance);
        ctlTextArea.setSysname(name);
        this.valeur = linkedIWorkflowInstance.getValue(name);
        if (this.valeur != null)
            ctlTextArea.setLabel(String.valueOf(this.valeur));
        ctlTextArea.addChangeListener(this.listener);
        return ctlTextArea;
    }

    public CtlDate ctlDate(String name, IStorageResource linkedIWorkflowInstance) {
        CtlDate ctlDate = new CtlDate();
        ctlDate.setParam(linkedIWorkflowInstance);
        ctlDate.setSysname(name);
        ctlDate.setDate((Date)linkedIWorkflowInstance.getValue(name));
        ctlDate.addChangeListener(this.listener);
        return ctlDate;
    }

    public CtlCheckBox ctlCheckBox(String name, IStorageResource linkedIWorkflowInstance) {
        CtlCheckBox ctlCheckBox = new CtlCheckBox();
        ctlCheckBox.setParam(linkedIWorkflowInstance);
        ctlCheckBox.setSysname(name);
        this.valeur = linkedIWorkflowInstance.getValue(name);
        if (this.valeur != null)
            ctlCheckBox.setChecked(((Boolean)linkedIWorkflowInstance.getValue(name)).booleanValue());
        ctlCheckBox.addChangeListener(this.listener);
        return ctlCheckBox;
    }

    public CtlMultipleFileUpload ctlMultipleFileUpload(String name, IStorageResource linkedIWorkflowInstance) {
        CtlMultipleFileUpload ctlMultipleFileUpload = new CtlMultipleFileUpload();
        ctlMultipleFileUpload.setParam(linkedIWorkflowInstance);
        ctlMultipleFileUpload.setSysname(name);
        try {
            ArrayList<IAttachment> attachments = (ArrayList<IAttachment>)this.workflowModule.getAttachments((IResource)linkedIWorkflowInstance, name);
            if (attachments != null && !attachments.isEmpty())
                for (Object attachment : attachments) {
                    try {
                        ctlMultipleFileUpload.add((DBFile)attachment);
                    } catch (Exception e) {
                        ctlMultipleFileUpload.add((TempUploadFile)attachment);
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ctlMultipleFileUpload.addChangeListener(this.listener);
        return ctlMultipleFileUpload;
    }

    protected ChangeListener listener = new ChangeListener() {
        public void onChange(ChangeEvent paramChangeEvent) {
            IStorageResource iLinkedResource = null;
            Object object = paramChangeEvent.getSource();
            if (CtlTextArea.class.equals(object.getClass())) {
                CtlTextArea txtBox = (CtlTextArea)object;
                iLinkedResource = (IStorageResource)txtBox.getParam();
                iLinkedResource.setValue(txtBox.getSysname(), txtBox.getLabel());
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
            } else if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber)object;
                iLinkedResource = (IStorageResource)number.getParam();
                iLinkedResource.setValue(number.getSysname(), number.getNumberValue());
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox)object;
                iLinkedResource = (IStorageResource)checkBox.getParam();
                iLinkedResource.setValue(checkBox.getSysname(), checkBox.isChecked());
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox)object;
                iLinkedResource = (IStorageResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                iLinkedResource = (IStorageResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                if (box.getSysname().equals("Fournisseur")) {
                    IStorageResource storageResource = (IStorageResource)box.getSelectedKey();
                    if (storageResource != null) {
                        iLinkedResource.setValue("SecteurDActivite", storageResource.getValue("SecteurDActivite"));
                        iLinkedResource.setValue("SousSecteurDActivite", storageResource.getValue("SousSecteurDActivite"));
                    }
                }
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
                TabDevisContradictoires.this.getView().refreshItems();
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup)object;
                iLinkedResource = (IStorageResource)radio.getParam();
                iLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate)object;
                iLinkedResource = (IStorageResource)date.getParam();
                iLinkedResource.setValue(date.getSysname(), date.getDate());
                iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload)object;
                iLinkedResource = (IStorageResource)multipleFileUpload.getParam();
                try {
                    iLinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        TabDevisContradictoires.this.workflowModule.addAttachment((IResource)iLinkedResource, multipleFileUpload.getSysname(), attachment);
                    iLinkedResource.save(TabDevisContradictoires.this.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private List<Option> getSousSecteursActivite(IStorageResource secteurActivite) {
        List<Option> options = new ArrayList<>();
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext, IResource.class);
            viewController.addEqualsConstraint("SecteurDActivite", secteurActivite);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("RefAchats", "SousSecteursDActivite"));
            for (IStorageResource storageResource : storageResources)
                options.add(new Option(storageResource, (String)storageResource.getValue("sys_Title")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    private List<Option> getFournisseurs(IStorageResource secteurActivite, IStorageResource sousSecteurActivite) {
        List<Option> options = new ArrayList<>();
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext, IResource.class);
            if (secteurActivite != null)
                viewController.addEqualsConstraint("SecteurDActivite", secteurActivite);
            if (sousSecteurActivite != null)
                viewController.addEqualsConstraint("SousSecteurDActivite", sousSecteurActivite);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("RefAchats", "Fournisseurs"));
            for (IStorageResource storageResource : storageResources)
                options.add(new Option(storageResource, (String)storageResource.getValue("sys_Title")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            try {
                IWorkflowInstance linkedWorkflowInstance = TabDevisContradictoires.this.getWorkflowModule().createWorkflowInstance(TabDevisContradictoires.this.loggedOnUserContext, WorkflowsService.getWorflow("Achats", "Fournisseur_1.0"), null);
                TabDevisContradictoires.this.getWorkflowInstance().addLinkedWorkflowInstance("Fournisseurs_SP", linkedWorkflowInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabDevisContradictoires.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument)item.getParam();
                TabDevisContradictoires.this.getWorkflowInstance().save(TabDevisContradictoires.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };
}
