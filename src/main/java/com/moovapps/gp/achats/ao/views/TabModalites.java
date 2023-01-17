package com.moovapps.gp.achats.ao.views;

import com.axemble.fc.file.domain.DBFile;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
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
import com.moovapps.gp.services.DirectoryService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TabModalites extends BaseViewExtension {
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
            ILinkedResource linkedResource = (ILinkedResource)iViewItem.getResource();
            if (this.taskName != null && this.taskName.equals("PreparationDuDossierDeLAO")) {
                CtlNumber delaiExecution = ctlNumber("DelaiDExecution", linkedResource);
                viewModelItem.setValue("DelaiDExecution", delaiExecution);
                CtlAutocompleteList execution = ctlAutocompleteList("Execution", linkedResource, (List<Option>)null);
                viewModelItem.setValue("Execution", execution);
                CtlNumber delaiGarantie = ctlNumber("DelaiDeGarantie", linkedResource);
                viewModelItem.setValue("DelaiDeGarantie", delaiGarantie);
                CtlAutocompleteList garantie = ctlAutocompleteList("Garantie", linkedResource, (List<Option>)null);
                viewModelItem.setValue("Garantie", garantie);
                CtlNumber retenueDeGarantie = ctlNumber("RetenueDeGarantie", linkedResource);
                viewModelItem.setValue("RetenueDeGarantie", retenueDeGarantie);
                CtlAutocompleteList echantillon = ctlAutocompleteList("Echantillon", linkedResource, (List<Option>)null);
                viewModelItem.setValue("Echantillon", echantillon);
                CtlAutocompleteList visiteDesLieux = ctlAutocompleteList("VisiteDesLieux", linkedResource, (List<Option>)null);
                viewModelItem.setValue("VisiteDesLieux", visiteDesLieux);
                CtlAutocompleteList evaluationTechnique = ctlAutocompleteList("EvaluationTechnique", linkedResource, (List<Option>)null);
                viewModelItem.setValue("EvaluationTechnique", evaluationTechnique);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CtlRadioGroup ctlRadioGroup(String name, ILinkedResource linkedResource) {
        CtlRadioGroup ctlRadio = new CtlRadioGroup();
        ctlRadio.setOptions((Collection<Option>) linkedResource.getList(name));
        ctlRadio.setParam(linkedResource);
        ctlRadio.setSelectedKey(linkedResource.getValue(name));
        ctlRadio.setSysname(name);
        ctlRadio.showResetButton(false);
        ctlRadio.addChangeListener(this.listener);
        return ctlRadio;
    }

    public CtlNumber ctlNumber(String name, ILinkedResource linkedResource) {
        CtlNumber ctlNumber = new CtlNumber();
        ctlNumber.setParam(linkedResource);
        ctlNumber.setSysname(name);
        this.valeur = linkedResource.getValue(name);
        if (this.valeur != null)
            ctlNumber.setLabel(String.valueOf(this.valeur));
        ctlNumber.addChangeListener(this.listener);
        return ctlNumber;
    }

    public CtlAutocompleteList ctlAutocompleteList(String name, ILinkedResource linkedResource, List<Option> options) {
        CtlAutocompleteList ctlAutocompleteList = new CtlAutocompleteList();
        if (options != null) {
            ctlAutocompleteList.setOptions(options);
        } else {
            ctlAutocompleteList.setOptions((Collection<Option>) linkedResource.getList(name));
        }
        ctlAutocompleteList.setParam(linkedResource);
        if (name.equals("NomPrenom")) {
            IUser user = (IUser)linkedResource.getValue(name);
            if (user != null)
                ctlAutocompleteList.setSelectedKey(user.getLogin());
        } else {
            ctlAutocompleteList.setSelectedKey(linkedResource.getValue(name));
        }
        ctlAutocompleteList.setSysname(name);
        ctlAutocompleteList.addChangeListener(this.listener);
        return ctlAutocompleteList;
    }

    public CtlTextBox ctlTextBox(String name, ILinkedResource linkedResource) {
        CtlTextBox ctlTextBox = new CtlTextBox();
        ctlTextBox.setParam(linkedResource);
        ctlTextBox.setSysname(name);
        this.valeur = linkedResource.getValue(name);
        if (this.valeur != null)
            ctlTextBox.setLabel(String.valueOf(this.valeur));
        ctlTextBox.addChangeListener(this.listener);
        return ctlTextBox;
    }

    public CtlTextArea ctlTextArea(String name, ILinkedResource linkedResource) {
        CtlTextArea ctlTextArea = new CtlTextArea();
        ctlTextArea.setRows(1);
        ctlTextArea.setParam(linkedResource);
        ctlTextArea.setSysname(name);
        this.valeur = linkedResource.getValue(name);
        if (this.valeur != null)
            ctlTextArea.setLabel(String.valueOf(this.valeur));
        ctlTextArea.addChangeListener(this.listener);
        return ctlTextArea;
    }

    public CtlCheckBox ctlCheckBox(String name, ILinkedResource linkedResource) {
        CtlCheckBox ctlCheckBox = new CtlCheckBox();
        ctlCheckBox.setParam(linkedResource);
        ctlCheckBox.setSysname(name);
        this.valeur = linkedResource.getValue(name);
        if (this.valeur != null)
            ctlCheckBox.setChecked(((Boolean)linkedResource.getValue(name)).booleanValue());
        ctlCheckBox.addChangeListener(this.listener);
        return ctlCheckBox;
    }

    public CtlMultipleFileUpload ctlMultipleFileUpload(String name, ILinkedResource linkedResource) {
        CtlMultipleFileUpload ctlMultipleFileUpload = new CtlMultipleFileUpload();
        ctlMultipleFileUpload.setParam(linkedResource);
        ctlMultipleFileUpload.setSysname(name);
        try {
            ArrayList<IAttachment> attachments = (ArrayList<IAttachment>)this.workflowModule.getAttachments((IResource)linkedResource, name);
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
            ILinkedResource iLinkedResource = null;
            Object object = paramChangeEvent.getSource();
            if (CtlTextArea.class.equals(object.getClass())) {
                CtlTextArea txtBox = (CtlTextArea)object;
                iLinkedResource = (ILinkedResource)txtBox.getParam();
                iLinkedResource.setValue(txtBox.getSysname(), txtBox.getLabel());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlTextBox.class.equals(object.getClass())) {
                CtlTextBox txtBox = (CtlTextBox)object;
                iLinkedResource = (ILinkedResource)txtBox.getParam();
                iLinkedResource.setValue(txtBox.getSysname(), txtBox.getLabel());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber)object;
                iLinkedResource = (ILinkedResource)number.getParam();
                iLinkedResource.setValue(number.getSysname(), number.getNumberValue());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox)object;
                iLinkedResource = (ILinkedResource)checkBox.getParam();
                iLinkedResource.setValue(checkBox.getSysname(), checkBox.isChecked());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox)object;
                iLinkedResource = (ILinkedResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                iLinkedResource = (ILinkedResource)box.getParam();
                if (box.getSysname().equals("NomPrenom")) {
                    try {
                        iLinkedResource.setValue(box.getSysname(), TabModalites.this.workflowModule.getUserByLogin((String)box.getSelectedKey()));
                    } catch (WorkflowModuleException e) {
                        e.printStackTrace();
                    }
                } else {
                    iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                }
                iLinkedResource.save(TabModalites.this.sysAdminContext);
                if (box.getSysname().equals("EvaluationTechnique") || box.getSysname().equals("VisiteDesLieux") || box.getSysname().equals("Echantillon")) {
                    Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>) TabModalites.this.getWorkflowInstance().getLinkedResources("Modalites_AO_Tab");
                    if (linkedResources != null && !linkedResources.isEmpty()) {
                        String valeur = null;
                        String trouve = "Non";
                        for (ILinkedResource linkedResource : linkedResources) {
                            valeur = (String)linkedResource.getValue(box.getSysname());
                            if (valeur != null && valeur.equals("Oui")) {
                                trouve = "Oui";
                                break;
                            }
                        }
                        TabModalites.this.getWorkflowInstance().setValue(box.getSysname(), trouve);
                        TabModalites.this.getWorkflowInstance().save(box.getSysname());
                    }
                }
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup)object;
                iLinkedResource = (ILinkedResource)radio.getParam();
                iLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate)object;
                iLinkedResource = (ILinkedResource)date.getParam();
                iLinkedResource.setValue(date.getSysname(), date.getDate());
                iLinkedResource.save(TabModalites.this.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload)object;
                iLinkedResource = (ILinkedResource)multipleFileUpload.getParam();
                try {
                    iLinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        TabModalites.this.workflowModule.addAttachment((IResource)iLinkedResource, multipleFileUpload.getSysname(), attachment);
                    iLinkedResource.save(TabModalites.this.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            ILinkedResource iLinkedResource = TabModalites.this.getWorkflowInstance().createLinkedResource("Modalites_AO_Tab");
            TabModalites.this.getWorkflowInstance().addLinkedResource(iLinkedResource);
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabModalites.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument)item.getParam();
                TabModalites.this.getWorkflowInstance().save(TabModalites.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };
}
