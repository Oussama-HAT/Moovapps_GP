package com.moovapps.gp.achats.ao.views;

import com.axemble.fc.file.domain.DBFile;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
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
import com.moovapps.gp.services.WorkflowsService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TabEvaluationOffresFinancieres extends BaseViewExtension {
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
            if (this.taskName != null && this.taskName.equals("OuvertureDesPlisFinanciers")) {
                CtlNumber montantHT = ctlNumber("MontantDeLActeDEngagementHT", linkedIWorkflowInstance);
                viewModelItem.setValue("MontantDeLActeDEngagementHT", montantHT);
                CtlNumber montantTVA = ctlNumber("MontantTVA", linkedIWorkflowInstance);
                viewModelItem.setValue("MontantTVA", montantTVA);
                CtlNumber montantTTC = ctlNumber("MontantDeLActeDEngagement", linkedIWorkflowInstance);
                montantTTC.setThrowEvents(true);
                viewModelItem.setValue("MontantDeLActeDEngagement", montantTTC);
                CtlMultipleFileUpload bordereauPrix = ctlMultipleFileUpload("BordereauDesPrix", linkedIWorkflowInstance);
                viewModelItem.setValue("BordereauDesPrix", bordereauPrix);
                CtlMultipleFileUpload detailEstimatif = ctlMultipleFileUpload("DetailEstimatif", linkedIWorkflowInstance);
                viewModelItem.setValue("DetailEstimatif", detailEstimatif);
                CtlTextArea commentaire = ctlTextArea("CommentaireEvalOffreFinanciere", linkedIWorkflowInstance);
                viewModelItem.setValue("CommentaireEvalOffreFinanciere", commentaire);
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
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
            } else if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber)object;
                iLinkedResource = (IStorageResource)number.getParam();
                iLinkedResource.setValue(number.getSysname(), number.getNumberValue());
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
                TabEvaluationOffresFinancieres.this.MAJOffreMoinsDisante();
                TabEvaluationOffresFinancieres.this.getView().refreshItems();
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox)object;
                iLinkedResource = (IStorageResource)checkBox.getParam();
                iLinkedResource.setValue(checkBox.getSysname(), checkBox.isChecked());
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox)object;
                iLinkedResource = (IStorageResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                iLinkedResource = (IStorageResource)box.getParam();
                iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
                TabEvaluationOffresFinancieres.this.getView().refreshItems();
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup)object;
                iLinkedResource = (IStorageResource)radio.getParam();
                iLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate)object;
                iLinkedResource = (IStorageResource)date.getParam();
                iLinkedResource.setValue(date.getSysname(), date.getDate());
                iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload)object;
                iLinkedResource = (IStorageResource)multipleFileUpload.getParam();
                try {
                    iLinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        TabEvaluationOffresFinancieres.this.workflowModule.addAttachment((IResource)iLinkedResource, multipleFileUpload.getSysname(), attachment);
                    iLinkedResource.save(TabEvaluationOffresFinancieres.this.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected ActionListener addLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            try {
                IWorkflowInstance linkedWorkflowInstance = TabEvaluationOffresFinancieres.this.getWorkflowModule().createWorkflowInstance(TabEvaluationOffresFinancieres.this.loggedOnUserContext, WorkflowsService.getWorflow("Achats", "DepotDesDossiers_1.0"), null);
                linkedWorkflowInstance.setValue("ReferenceDeLAO", TabEvaluationOffresFinancieres.this.getWorkflowInstance().getValue("sys_Reference"));
                TabEvaluationOffresFinancieres.this.getWorkflowInstance().addLinkedWorkflowInstance("DepotDossiers_SP", linkedWorkflowInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected ActionListener removeLine = new ActionListener() {
        public void onClick(ActionEvent paramActionEvent) {
            Iterator<CtlListView.Item> itr = TabEvaluationOffresFinancieres.this.getView().getListView().getSelectedItems().iterator();
            while (itr.hasNext()) {
                CtlListView.Item item = itr.next();
                CoreDocument coreDocument = (CoreDocument)item.getParam();
                TabEvaluationOffresFinancieres.this.getWorkflowInstance().save(TabEvaluationOffresFinancieres.this.getWorkflowModule().getLoggedOnUserContext());
            }
        }
    };

    private void MAJOffreMoinsDisante() {
        try {
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("Lots_AO_Tab");
            Collection<IWorkflowInstance> linkedWorkflowInstancesDepots = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("DepotDossiers_SP");
            String lot = null, lotDepot = null, documentState = null;
            Number montantActeEngagement = null, offreMoinsDisante = null;
            IWorkflowInstance workflowInstanceOffreMoinsDisante = null;
            for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                lot = linkedResourceLot.getValue("Code") + " - " + linkedResourceLot.getValue("Intitule");
                offreMoinsDisante = null;
                workflowInstanceOffreMoinsDisante = null;
                for (IWorkflowInstance linkedWorkflowInstanceDepot : linkedWorkflowInstancesDepots) {
                    documentState = (String)linkedWorkflowInstanceDepot.getValue("DocumentState");
                    if (!documentState.contains("rejeté")) {
                            lotDepot = (String)linkedWorkflowInstanceDepot.getValue("Lot");
                    if (lot != null && lotDepot != null && lot.equals(lotDepot)) {
                        montantActeEngagement = (Number)linkedWorkflowInstanceDepot.getValue("MontantDeLActeDEngagement");
                        if (montantActeEngagement != null)
                            if (offreMoinsDisante != null) {
                                if (montantActeEngagement.doubleValue() < offreMoinsDisante.doubleValue()) {
                                    offreMoinsDisante = montantActeEngagement;
                                    workflowInstanceOffreMoinsDisante = linkedWorkflowInstanceDepot;
                                }
                            } else {
                                offreMoinsDisante = montantActeEngagement;
                                workflowInstanceOffreMoinsDisante = linkedWorkflowInstanceDepot;
                            }
                        linkedWorkflowInstanceDepot.setValue("OffreMoinsDisante", "Non");
                        linkedWorkflowInstanceDepot.save(this.sysAdminContext);
                    }
                }
            }
            workflowInstanceOffreMoinsDisante.setValue("OffreMoinsDisante", "Oui");
            workflowInstanceOffreMoinsDisante.save(this.sysAdminContext);
            Float noteFinanciere = null, noteTechnique = null, noteFinale = null, offreMieuxDisante = Float.valueOf(0.0F);
            Float ponderationTechnique = null, ponderationFinanciere = null;
            String modeEvaluation = null;
            IWorkflowInstance workflowInstanceOffreMieuxDisante = null;
            for (IWorkflowInstance linkedWorkflowInstanceDepot : linkedWorkflowInstancesDepots) {
                documentState = (String)linkedWorkflowInstanceDepot.getValue("DocumentState");
                if (!documentState.contains("rejeté")) {
                        noteFinanciere = null;
                lotDepot = (String)linkedWorkflowInstanceDepot.getValue("Lot");
                if (lot != null && lotDepot != null && lot.equals(lotDepot)) {
                    montantActeEngagement = (Number)linkedWorkflowInstanceDepot.getValue("MontantDeLActeDEngagement");
                    if (montantActeEngagement != null)
                        noteFinanciere = Float.valueOf(100.0F * (float)(offreMoinsDisante.doubleValue() / montantActeEngagement.doubleValue()));
                    noteTechnique = (Float)linkedWorkflowInstanceDepot.getValue("NoteObtenueTechnique");
                    modeEvaluation = (String)linkedWorkflowInstanceDepot.getValue("ModeDEvaluation");
                    if (modeEvaluation.equals("Mieux disant")) {
                        ponderationTechnique = (Float)linkedWorkflowInstanceDepot.getValue("DePonderationTechnique");
                        ponderationFinanciere = (Float)linkedWorkflowInstanceDepot.getValue("DePonderationFinanciere");
                    } else {
                        ponderationTechnique = Float.valueOf(50.0F);
                        ponderationFinanciere = Float.valueOf(50.0F);
                    }
                    noteFinale = Float.valueOf(noteTechnique.floatValue() * ponderationTechnique.floatValue() * 0.01F + noteFinanciere.floatValue() * ponderationFinanciere.floatValue() * 0.01F);
                    if (noteFinale.floatValue() >= offreMieuxDisante.floatValue()) {
                        offreMieuxDisante = noteFinale;
                        workflowInstanceOffreMieuxDisante = linkedWorkflowInstanceDepot;
                    }
                    linkedWorkflowInstanceDepot.setValue("NoteFinanciereObtenue", noteFinanciere);
                    linkedWorkflowInstanceDepot.setValue("NoteFinaleObtenue", noteFinale);
                    linkedWorkflowInstanceDepot.setValue("OffreMieuxDisante", "Non");
                    linkedWorkflowInstanceDepot.save(this.sysAdminContext);
                }
            }
        }
        workflowInstanceOffreMieuxDisante.setValue("OffreMieuxDisante", "Oui");
        workflowInstanceOffreMieuxDisante.save(this.sysAdminContext);
    }
} catch (Exception e) {
        e.printStackTrace();
        }
        }
        }
