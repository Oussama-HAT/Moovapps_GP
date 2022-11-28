package com.moovapps.gp.achats.ao.views;

import com.axemble.fc.file.domain.DBFile;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
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
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
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
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TabDepotDeDossiers extends BaseViewExtension {
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
                if (this.taskName != null && this.taskName.equals("DepotDesDossiers")) {
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
            IStorageResource linkedIWorkflowInstance = (IStorageResource)iViewItem.getResource();
            if (this.taskName != null && this.taskName.equals("DepotDesDossiers")) {
                CtlAutocompleteList candidat = ctlAutocompleteList("Candidat", linkedIWorkflowInstance, (List<Option>)null);
                viewModelItem.setValue("Candidat", candidat);
                CtlAutocompleteList lot = ctlAutocompleteList("Lot", linkedIWorkflowInstance, getListLots());
                lot.setThrowEvents(true);
                viewModelItem.setValue("Lot", lot);
                CtlDate dateDeDepot = ctlDate("DateDeDepot", linkedIWorkflowInstance);
                viewModelItem.setValue("DateDeDepot", dateDeDepot);
                CtlAutocompleteList lieuDeDepot = ctlAutocompleteList("LieuDeDepot", linkedIWorkflowInstance, (List<Option>)null);
                viewModelItem.setValue("LieuDeDepot", lieuDeDepot);
                String lotString = (String)linkedIWorkflowInstance.getValue("Lot");
                String echantillonDemande = echantillonDemandeLot(lotString);
                if (echantillonDemande != null && echantillonDemande.equals("Oui")) {
                    CtlAutocompleteList echantillonDepose = ctlAutocompleteList("EchantillonDepose", linkedIWorkflowInstance, (List<Option>)null);
                    viewModelItem.setValue("EchantillonDepose", echantillonDepose);
                    CtlDate dateDeDepotDeLEchantillon = ctlDate("DateDeDepotDeLEchantillon", linkedIWorkflowInstance);
                    viewModelItem.setValue("DateDeDepotDeLEchantillon", dateDeDepotDeLEchantillon);
                }
                CtlTextArea commentaire = ctlTextArea("CommentaireDepotDossier", linkedIWorkflowInstance);
                viewModelItem.setValue("CommentaireDepotDossier", commentaire);
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
            ctlNumber.setLabel(String.valueOf(this.valeur).replace(".0", ""));
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

    public CtlDate ctlDate(String name, IStorageResource linkedResource) {
        CtlDate ctlDate = new CtlDate();
        ctlDate.setParam(linkedResource);
        ctlDate.setSysname(name);
        ctlDate.setDate((Date)linkedResource.getValue(name));
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
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
            } else if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber)object;
                iLinkedResource = (IStorageResource)number.getParam();
                iLinkedResource.setValue(number.getSysname(), number.getFloatValue());
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox)object;
                iLinkedResource = (IStorageResource)checkBox.getParam();
                iLinkedResource.setValue(checkBox.getSysname(), Boolean.valueOf(checkBox.isChecked()));
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox)object;
                iLinkedResource = (IStorageResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                iLinkedResource = (IStorageResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                if (box.getSysname().equals("Lot")) {
                    TabDepotDeDossiers.this.MAJInfosLot(box.getSelectedLabel(), iLinkedResource);
                } else if (box.getSysname().equals("Fournisseur")) {
                    IStorageResource storageResource = (IStorageResource)box.getSelectedKey();
                    if (storageResource != null) {
                        iLinkedResource.setValue("SecteurDActivite", storageResource.getValue("SecteurDActivite"));
                        iLinkedResource.setValue("SousSecteurDActivite", storageResource.getValue("SousSecteurDActivite"));
                    }
                }
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
                TabDepotDeDossiers.this.getView().refreshItems();
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup)object;
                iLinkedResource = (IStorageResource)radio.getParam();
                iLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate)object;
                iLinkedResource = (IStorageResource)date.getParam();
                iLinkedResource.setValue(date.getSysname(), date.getDate());
                iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload)object;
                iLinkedResource = (IStorageResource)multipleFileUpload.getParam();
                try {
                    iLinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        TabDepotDeDossiers.this.workflowModule.addAttachment((IResource)iLinkedResource, multipleFileUpload.getSysname(), attachment);
                    iLinkedResource.save(TabDepotDeDossiers.this.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            try {
                IWorkflowInstance linkedWorkflowInstance = TabDepotDeDossiers.this.getWorkflowModule().createWorkflowInstance(TabDepotDeDossiers.this.loggedOnUserContext, WorkflowsService.getWorflow("Achats", "DepotDesDossiers_1.0"), null);
                linkedWorkflowInstance.setValue("ReferenceDeLAO", TabDepotDeDossiers.this.getWorkflowInstance().getValue("sys_Reference"));
                linkedWorkflowInstance.setValue("EchantillonDepose", "Non");
                linkedWorkflowInstance = TabDepotDeDossiers.duplicateWorkflowInstance(TabDepotDeDossiers.this.getWorkflowModule(), TabDepotDeDossiers.this.loggedOnUserContext, TabDepotDeDossiers.this.getWorkflowInstance(), linkedWorkflowInstance);
                TabDepotDeDossiers.this.getWorkflowInstance().addLinkedWorkflowInstance("DepotDossiers_SP", linkedWorkflowInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabDepotDeDossiers.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument)item.getParam();
                TabDepotDeDossiers.this.getWorkflowInstance().save(TabDepotDeDossiers.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };

    public static IWorkflowInstance duplicateWorkflowInstance(IWorkflowModule workflowModule, IContext loggedOnUserContext, IWorkflowInstance workflowInstanceSource, IWorkflowInstance workflowInstanceCible) throws WorkflowModuleException {
        Collection<? extends IProperty> allFields = workflowInstanceSource.getDefinition().getProperties();
        for (IProperty iProperty : allFields) {
            if (!iProperty.getName().startsWith("sys_") && !iProperty.getName().endsWith("State")) {
                Collection<IAttachment> attachments;
                String str;
                switch ((str = iProperty.getDisplaySettings().getType()).hashCode()) {
                    case -1914417805:
                        if (!str.equals("file_multiple"))
                            break;
                        attachments = (Collection<IAttachment>) workflowModule.getAttachments((IResource)workflowInstanceSource, iProperty.getName());
                        if (attachments != null && !attachments.isEmpty())
                            for (IAttachment iattachment : attachments)
                                workflowModule.addAttachment((IResource)workflowInstanceCible, iProperty.getName(), iattachment);
                        continue;
                    case 998428800:
                        if (!str.equals("resourcetable"))
                            break;
                        createTableLines(workflowModule, loggedOnUserContext, workflowInstanceSource, workflowInstanceCible, iProperty);
                        continue;
                }
                workflowInstanceCible.setValue(iProperty.getName(), workflowInstanceSource.getValue(iProperty.getName()));
            }
        }
        workflowInstanceCible.save(loggedOnUserContext);
        return workflowInstanceCible;
    }

    public static void createTableLines(IWorkflowModule workflowModule, IContext loggedOnUserContext, IWorkflowInstance workflowInstanceSource, IWorkflowInstance workflowInstanceCible, IProperty iProperty) throws WorkflowModuleException {
        Collection<ILinkedResource> collectionLinkedResourcesSource = (Collection<ILinkedResource>) workflowInstanceSource.getLinkedResources(iProperty.getName());
        for (ILinkedResource linkedResourceSource : collectionLinkedResourcesSource) {
            ILinkedResource linkedResourceCible = workflowInstanceCible.createLinkedResource(iProperty.getName());
            Collection<? extends IProperty> allLinkedResourceFields = linkedResourceSource.getDefinition().getProperties();
            for (IProperty iPropertyLinkedResource : allLinkedResourceFields) {
                if (!iPropertyLinkedResource.getName().startsWith("sys_")) {
                    if (iPropertyLinkedResource.getDisplaySettings().getType().equals("file_multiple")) {
                        Collection<IAttachment> attachments = (Collection<IAttachment>) workflowModule.getAttachments((IResource)linkedResourceSource, iPropertyLinkedResource.getName());
                        if (attachments != null && !attachments.isEmpty())
                            for (IAttachment iattachment : attachments)
                                workflowModule.addAttachment((IResource)linkedResourceCible, iPropertyLinkedResource.getName(), iattachment);
                        continue;
                    }
                    linkedResourceCible.setValue(iPropertyLinkedResource.getName(), linkedResourceSource.getValue(iPropertyLinkedResource.getName()));
                }
            }
            workflowInstanceCible.addLinkedResource(linkedResourceCible);
        }
    }

    private List<Option> getListLots() {
        List<Option> options = new ArrayList<>();
        try {
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
            String code = null, intitule = null;
            for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                code = (String)linkedResourceLot.getValue("Code");
                intitule = (String)linkedResourceLot.getValue("Intitule");
                if (code != null && intitule != null)
                    options.add(new Option(String.valueOf(code) + " - " + intitule, String.valueOf(code) + " - " + intitule));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
    }

    private String echantillonDemandeLot(String lot) {
        try {
            String code = null, intitule = null;
            Collection<ILinkedResource> linkedResourcesModalites = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Modalites_AO_Tab");
            for (ILinkedResource linkedResourceModalite : linkedResourcesModalites) {
                code = (String)linkedResourceModalite.getValue("Code");
                intitule = (String)linkedResourceModalite.getValue("Intitule");
                if (lot != null && lot.equals(String.valueOf(code) + " - " + intitule))
                    return (String)linkedResourceModalite.getValue("Echantillon");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void MAJInfosLot(String lot, IStorageResource linkedIWorkflowInstance) {
        try {
            if (lot != null) {
                Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) linkedIWorkflowInstance.getLinkedResources("Lots_AO_Tab");
                String code = null, intitule = null;
                for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                    code = (String)linkedResourceLot.getValue("Code");
                    intitule = (String)linkedResourceLot.getValue("Intitule");
                    if (lot.equals(String.valueOf(code) + " - " + intitule)) {
                        linkedIWorkflowInstance.setValue("CautionProvisoire", linkedResourceLot.getValue("CautionProvisoire"));
                        linkedIWorkflowInstance.setValue("MontantEstimatifTTC", linkedResourceLot.getValue("MontantEstimatifTTC"));
                        linkedIWorkflowInstance.setValue("ModeDEvaluation", linkedResourceLot.getValue("ModeDEvaluation"));
                        linkedIWorkflowInstance.setValue("DePonderationTechnique", linkedResourceLot.getValue("DePonderationTechnique"));
                        linkedIWorkflowInstance.setValue("DePonderationFinanciere", linkedResourceLot.getValue("DePonderationFinanciere"));
                        break;
                    }
                }
            } else {
                linkedIWorkflowInstance.setValue("CautionProvisoire", null);
                linkedIWorkflowInstance.setValue("MontantEstimatifTTC", null);
                linkedIWorkflowInstance.setValue("ModeDEvaluation", null);
                linkedIWorkflowInstance.setValue("DePonderationTechnique", null);
                linkedIWorkflowInstance.setValue("DePonderationFinanciere", null);
            }
            linkedIWorkflowInstance.save(this.loggedOnUserContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
