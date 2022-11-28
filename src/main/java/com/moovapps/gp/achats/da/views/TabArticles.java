package com.moovapps.gp.achats.da.views;
import com.axemble.fc.file.domain.DBFile;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.AbstractActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlAutocompleteList;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
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
import com.moovapps.gp.services.DataUniversService;
import com.moovapps.gp.services.DirectoryService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
public class TabArticles extends BaseViewExtension {
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
            if (this.currentTaskInstance != null) {
                this.taskName = this.currentTaskInstance.getTask().getName();
                if (this.taskName != null && this.taskName.equals("DemandeDAchat")) {
                    CtlButton buttonAdd = new CtlButton("addElement", new CtlText("+"));
                    buttonAdd.addActionListener((AbstractActionListener)this.addLine);
                    topButtonContainer.add((IWidget)buttonAdd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPrepareColumns(list);
    }

    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            ILinkedResource linkedResource = (ILinkedResource)iViewItem.getResource();
            if (this.taskName != null && this.taskName.equals("DemandeDAchat")) {
                CtlAutocompleteList familleArticles = ctlAutocompleteList("FamilleDArticles", linkedResource, (List<Option>)null);
                viewModelItem.setValue("FamilleDArticles.sys_Title", familleArticles);
                IStorageResource storageResourceFamilleArticles = (IStorageResource)linkedResource.getValue("FamilleDArticles");
                CtlAutocompleteList sousFamilleArticles = ctlAutocompleteList("SousFamilleDArticles", linkedResource, getSousFamillesArticles(storageResourceFamilleArticles));
                viewModelItem.setValue("SousFamilleDArticles.sys_Title", sousFamilleArticles);
                IStorageResource storageResourceSousFamilleArticles = (IStorageResource)linkedResource.getValue("SousFamilleDArticles");
                CtlAutocompleteList article = ctlAutocompleteList("Article", linkedResource, getArticles(storageResourceFamilleArticles, storageResourceSousFamilleArticles));
                viewModelItem.setValue("Article.sys_Title", article);
                CtlNumber quantite = ctlNumber("Quantite", linkedResource);
                viewModelItem.setValue("Quantite", quantite);
                CtlTextArea commentaire = ctlTextArea("Commentaire", linkedResource);
                viewModelItem.setValue("Commentaire", commentaire);
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
        ctlRadio.setMandatory(true);
        ctlRadio.addChangeListener(this.listener);
        return ctlRadio;
    }

    public CtlNumber ctlNumber(String name, ILinkedResource linkedResource) {
        CtlNumber ctlNumber = new CtlNumber();
        ctlNumber.setParam(linkedResource);
        ctlNumber.setSysname(name);
        ctlNumber.setMandatory(true);
        this.valeur = linkedResource.getValue(name);
        if (this.valeur != null)
            ctlNumber.setLabel(String.valueOf(this.valeur).replace(".0", ""));
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
        ctlAutocompleteList.setSelectedKey(linkedResource.getValue(name));
        ctlAutocompleteList.setSysname(name);
        ctlAutocompleteList.setThrowEvents(true);
        ctlAutocompleteList.setMandatory(true);
        ctlAutocompleteList.addChangeListener(this.listener);
        return ctlAutocompleteList;
    }

    public CtlTextBox ctlTextBox(String name, ILinkedResource linkedResource) {
        CtlTextBox ctlTextBox = new CtlTextBox();
        ctlTextBox.setParam(linkedResource);
        ctlTextBox.setSysname(name);
        ctlTextBox.setMandatory(true);
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
        ctlTextArea.setMandatory(true);
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
                for (IAttachment attachment : attachments) {
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
                iLinkedResource.save(TabArticles.this.sysAdminContext);
            } else if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber)object;
                iLinkedResource = (ILinkedResource)number.getParam();
                iLinkedResource.setValue(number.getSysname(), number.getFloatValue());
                iLinkedResource.save(TabArticles.this.sysAdminContext);
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox)object;
                iLinkedResource = (ILinkedResource)checkBox.getParam();
                iLinkedResource.setValue(checkBox.getSysname(), Boolean.valueOf(checkBox.isChecked()));
                iLinkedResource.save(TabArticles.this.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox)object;
                iLinkedResource = (ILinkedResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabArticles.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                iLinkedResource = (ILinkedResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                if (box.getSysname().equals("Article")) {
                    IStorageResource storageResource = (IStorageResource)box.getSelectedKey();
                    if (storageResource != null) {
                        iLinkedResource.setValue("FamilleDArticles", storageResource.getValue("FamilleDArticles"));
                        iLinkedResource.setValue("SousFamilleDArticles", storageResource.getValue("SousFamilleDArticles"));
                    }
                }
                iLinkedResource.save(TabArticles.this.sysAdminContext);
                TabArticles.this.getView().refreshItems();
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup)object;
                iLinkedResource = (ILinkedResource)radio.getParam();
                iLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                iLinkedResource.save(TabArticles.this.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate)object;
                iLinkedResource = (ILinkedResource)date.getParam();
                iLinkedResource.setValue(date.getSysname(), date.getDate());
                iLinkedResource.save(TabArticles.this.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload)object;
                iLinkedResource = (ILinkedResource)multipleFileUpload.getParam();
                try {
                    iLinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        TabArticles.this.workflowModule.addAttachment((IResource)iLinkedResource, multipleFileUpload.getSysname(), attachment);
                    iLinkedResource.save(TabArticles.this.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private List<Option> getSousFamillesArticles(IStorageResource familleArticle) {
        List<Option> options = new ArrayList<>();
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext, IResource.class);
            viewController.addEqualsConstraint("FamilleDArticles", familleArticle);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("RefAchats", "SousFamillesDArticles"));
            for (IStorageResource storageResource : storageResources)
                options.add(new Option(storageResource, (String)storageResource.getValue("sys_Title")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    private List<Option> getArticles(IStorageResource familleArticle, IStorageResource sousFamilleArticle) {
        List<Option> options = new ArrayList<>();
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.sysAdminContext, IResource.class);
            if (familleArticle != null)
                viewController.addEqualsConstraint("FamilleDArticles", familleArticle);
            if (sousFamilleArticle != null)
                viewController.addEqualsConstraint("SousFamilleDArticles", sousFamilleArticle);
            Collection<IStorageResource> storageResources = viewController.evaluate(DataUniversService.getResourceDefinition("RefAchats", "Articles"));
            for (IStorageResource storageResource : storageResources)
                options.add(new Option(storageResource, (String)storageResource.getValue("sys_Title")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            ILinkedResource iLinkedResource = TabArticles.this.getWorkflowInstance().createLinkedResource("Articles_Tab");
            TabArticles.this.getWorkflowInstance().addLinkedResource(iLinkedResource);
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabArticles.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument)item.getParam();
                TabArticles.this.getWorkflowInstance().save(TabArticles.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };
}
