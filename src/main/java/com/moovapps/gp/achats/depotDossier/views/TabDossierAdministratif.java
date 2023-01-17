package com.moovapps.gp.achats.depotDossier.views;

import com.axemble.fc.file.domain.DBFile;
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
import com.axemble.vdp.ui.framework.widgets.CtlText;
import com.axemble.vdp.ui.framework.widgets.CtlTextArea;
import com.axemble.vdp.ui.framework.widgets.CtlTextBox;
import com.axemble.vdp.ui.framework.widgets.INamedWidget;
import com.axemble.vdp.ui.framework.widgets.list.Option;
import com.axemble.vdp.utils.parameters.TempUploadFile;
import com.moovapps.gp.services.DirectoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TabDossierAdministratif extends BaseViewExtension {
    protected static IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    protected IWorkflowModule workflowModule = null;

    protected ITaskInstance currentTaskInstance = null;

    protected String taskName = null;

    protected String tableName = null;

    protected Object valeur = null;

    public void onPrepareColumns(List list) {
        try {
            this.workflowModule = getWorkflowModule();
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            NamedContainer topButtonContainer = getView().getButtonsContainer();
            List<IWidget> buttonList = topButtonContainer.getWidgets();
            for (IWidget iWidget : buttonList) {
                INamedWidget iNamedWidget = (INamedWidget) iWidget;
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
            ILinkedResource linkedResource = (ILinkedResource) iViewItem.getResource();
            if (this.taskName != null)
                if (this.taskName.equals("OuvertureDuDossierAdministratif") || this.taskName.equals("DemandeDeLeveeDesReserves") || this.taskName.equals("Evaluation")) {
                    CtlAutocompleteList pieceFournie = ctlAutocompleteList("PieceFournie", linkedResource, (List<Option>) null);
                    viewModelItem.setValue("PieceFournie", pieceFournie);
                    CtlMultipleFileUpload piecesJointes = ctlMultipleFileUpload("PieceSJointeS", linkedResource);
                    viewModelItem.setValue("PieceSJointeS", piecesJointes);
                    if (this.taskName.equals("OuvertureDuDossierAdministratif") || this.taskName.equals("Evaluation")) {
                        CtlAutocompleteList conformiteDeLaPiece = ctlAutocompleteList("ConformiteDeLaPiece", linkedResource, (List<Option>) null);
                        conformiteDeLaPiece.setThrowEvents(true);
                        viewModelItem.setValue("ConformiteDeLaPiece", conformiteDeLaPiece);
                        CtlTextArea commentaire = ctlTextArea("Commentaire", linkedResource);
                        viewModelItem.setValue("Commentaire", commentaire);
                    }
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
            IUser user = (IUser) linkedResource.getValue(name);
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

    public CtlDate ctlDate(String name, ILinkedResource linkedResource) {
        CtlDate ctlDate = new CtlDate();
        ctlDate.setParam(linkedResource);
        ctlDate.setSysname(name);
        ctlDate.setDate((Date) linkedResource.getValue(name));
        ctlDate.addChangeListener(this.listener);
        return ctlDate;
    }

    public CtlCheckBox ctlCheckBox(String name, ILinkedResource linkedResource) {
        CtlCheckBox ctlCheckBox = new CtlCheckBox();
        ctlCheckBox.setParam(linkedResource);
        ctlCheckBox.setSysname(name);
        this.valeur = linkedResource.getValue(name);
        if (this.valeur != null)
            ctlCheckBox.setChecked(((Boolean) linkedResource.getValue(name)).booleanValue());
        ctlCheckBox.addChangeListener(this.listener);
        return ctlCheckBox;
    }

    public CtlMultipleFileUpload ctlMultipleFileUpload(String name, ILinkedResource linkedResource) {
        CtlMultipleFileUpload ctlMultipleFileUpload = new CtlMultipleFileUpload();
        ctlMultipleFileUpload.setParam(linkedResource);
        ctlMultipleFileUpload.setSysname(name);
        try {
            ArrayList<IAttachment> attachments = (ArrayList<IAttachment>) this.workflowModule.getAttachments((IResource) linkedResource, name);
            if (attachments != null && !attachments.isEmpty())
                for (Object attachment : attachments) {
                    try {
                        ctlMultipleFileUpload.add((DBFile) attachment);
                    } catch (Exception e) {
                        ctlMultipleFileUpload.add((TempUploadFile) attachment);
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
                CtlTextArea txtBox = (CtlTextArea) object;
                iLinkedResource = (ILinkedResource) txtBox.getParam();
                iLinkedResource.setValue(txtBox.getSysname(), txtBox.getLabel());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlText.class.equals(object.getClass())) {
                CtlText txtBox = (CtlText) object;
                iLinkedResource = (ILinkedResource) txtBox.getParam();
                iLinkedResource.setValue(txtBox.getSysname(), txtBox.getText());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber) object;
                iLinkedResource = (ILinkedResource) number.getParam();
                iLinkedResource.setValue(number.getSysname(), number.getNumberValue());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox) object;
                iLinkedResource = (ILinkedResource) checkBox.getParam();
                iLinkedResource.setValue(checkBox.getSysname(), checkBox.isChecked());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox) object;
                iLinkedResource = (ILinkedResource) box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList) object;
                iLinkedResource = (ILinkedResource) box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
                if (box.getSysname().equals("ConformiteDeLaPiece"))
                    if (TabDossierAdministratif.this.taskName.equals("OuvertureDuDossierAdministratif"))
                        TabDossierAdministratif.MAJDecisionCommission(TabDossierAdministratif.this.getWorkflowInstance());
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup) object;
                iLinkedResource = (ILinkedResource) radio.getParam();
                iLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate) object;
                iLinkedResource = (ILinkedResource) date.getParam();
                iLinkedResource.setValue(date.getSysname(), date.getDate());
                iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload) object;
                iLinkedResource = (ILinkedResource) multipleFileUpload.getParam();
                try {
                    iLinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        TabDossierAdministratif.this.workflowModule.addAttachment((IResource) iLinkedResource, multipleFileUpload.getSysname(), attachment);
                    iLinkedResource.save(TabDossierAdministratif.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            ILinkedResource iLinkedResource = TabDossierAdministratif.this.getWorkflowInstance().createLinkedResource("DA_Depot_Tab");
            TabDossierAdministratif.this.getWorkflowInstance().addLinkedResource(iLinkedResource);
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabDossierAdministratif.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument) item.getParam();
                TabDossierAdministratif.this.getWorkflowInstance().save(TabDossierAdministratif.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };

    public static void MAJDecisionCommission(IWorkflowInstance workflowInstanceDepot) {
        try {
            String conformitePiece = null, decision = "Retenu";
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("DA_Depot_Tab");
            for (ILinkedResource linkedResource : linkedResources) {
                conformitePiece = (String) linkedResource.getValue("ConformiteDeLaPiece");
                if (conformitePiece != null) {
                    if (conformitePiece.equals("Acceptée avec réserve") && decision.equals("Retenu")) {
                        decision = "Acceptée avec réserve";
                        continue;
                    }
                    if (conformitePiece.equals("Non conforme")) {
                        decision = "Rejeté";
                        break;
                    }
                }
            }
            linkedResources = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("DT_Depot_Tab");
            for (ILinkedResource linkedResource : linkedResources) {
                conformitePiece = (String) linkedResource.getValue("ConformiteDeLaPiece");
                if (conformitePiece != null) {
                    if (conformitePiece.equals("Acceptée avec réserve") && decision.equals("Retenu")) {
                        decision = "Acceptée avec réserve";
                        continue;
                    }
                    if (conformitePiece.equals("Non conforme")) {
                        decision = "Rejeté";
                        break;
                    }
                }
            }
            workflowInstanceDepot.setValue("DecisionCommissionDA", decision);
            workflowInstanceDepot.save(sysAdminContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
