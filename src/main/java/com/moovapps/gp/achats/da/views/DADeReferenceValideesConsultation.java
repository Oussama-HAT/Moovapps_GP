package com.moovapps.gp.achats.da.views;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.AbstractActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlCheckBox;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class DADeReferenceValideesConsultation extends BaseViewExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected HashMap<String, Object> uiComponentCollection;

    protected Vector<Object> uiAllComponentCollection = new Vector();

    public void onPrepareColumns(List viewModelColumns) {
        try {
            NamedContainer namedContainer = getView().getButtonsContainer();
            CtlButton button = new CtlButton("ValiderSelection", new CtlText("Valider la sélection"));
                    button.addActionListener((AbstractActionListener)this.ValiderSelection);
            namedContainer.add((IWidget)button);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPrepareColumns(viewModelColumns);
    }

    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            ProcessWorkflowInstance workflowInstance = (ProcessWorkflowInstance)iViewItem.getResource();
            CtlCheckBox ctlCheckBox = ctlCheckBox("Check", workflowInstance);
            this.uiComponentCollection = new HashMap<>();
            this.uiComponentCollection.put("resource", workflowInstance);
            this.uiComponentCollection.put("Check", ctlCheckBox);
            this.uiAllComponentCollection.add(workflowInstance);
            this.uiAllComponentCollection.add(ctlCheckBox);
            viewModelItem.setValue("Check", ctlCheckBox);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CtlCheckBox ctlCheckBox(String name, ProcessWorkflowInstance workflowInstance) {
        CtlCheckBox ctlCheckBox = new CtlCheckBox();
        ctlCheckBox.setParam(workflowInstance);
        ctlCheckBox.setSysname(name);
        ctlCheckBox.setChecked(((Boolean)workflowInstance.getValue("Check")).booleanValue());
        ctlCheckBox.setEditable(true);
        ctlCheckBox.addChangeListener(this.listener);
        return ctlCheckBox;
    }

    protected ChangeListener listener = new ChangeListener() {
        public void onChange(ChangeEvent paramChangeEvent) {
            ProcessWorkflowInstance workflowInstance = null;
            Object object = paramChangeEvent.getSource();
            if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox ctlCheckBox = (CtlCheckBox)object;
                workflowInstance = (ProcessWorkflowInstance)ctlCheckBox.getParam();
                workflowInstance.setValue(ctlCheckBox.getSysname(), Boolean.valueOf(ctlCheckBox.isChecked()));
                workflowInstance.save(DADeReferenceValideesConsultation.this.sysAdminContext);
            }
        }
    };

    ActionListener ValiderSelection = new ActionListener() {
        public void onClick(ActionEvent arg0) {
            try {
                DADeReferenceValideesConsultation.this.getWorkflowInstance().setValue("BordereauDePrix_Tab", null);
                ProcessWorkflowInstance workflowInstanceDA = null;
                DADeReferenceValideesConsultation.this.getWorkflowInstance().setValue("URISDA", null);
                Boolean check = null;
                Collection<String> uris = new ArrayList<>();
                for (Object object : DADeReferenceValideesConsultation.this.uiAllComponentCollection) {
                    if (ProcessWorkflowInstance.class.equals(object.getClass())) {
                        workflowInstanceDA = (ProcessWorkflowInstance)object;
                        check = (Boolean)workflowInstanceDA.getValue("Check");
                        if (check != null && check.booleanValue()) {
                            uris.add(workflowInstanceDA.getProtocolURI());
                            DADeReferenceValideesConsultation.this.MAJBordereauDePrix((IWorkflowInstance)workflowInstanceDA);
                            workflowInstanceDA.setValue("ReferenceDeLaConsultation", DADeReferenceValideesConsultation.this.getWorkflowInstance().getValue("sys_Reference"));
                        } else {
                            workflowInstanceDA.setValue("ReferenceDeLaConsultation", null);
                        }
                        workflowInstanceDA.save(DADeReferenceValideesConsultation.this.sysAdminContext);
                    }
                }
                DADeReferenceValideesConsultation.this.getWorkflowInstance().setValue("URISDA", uris);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void MAJBordereauDePrix(IWorkflowInstance workflowInstanceDA) {
        try {
            Collection<ILinkedResource> linkedResourcesArticlesDA = (Collection<ILinkedResource>)workflowInstanceDA.getValue("Articles_Tab");
            Collection<ILinkedResource> linkedResourcesArticlesBDP = (Collection<ILinkedResource>)getWorkflowInstance().getValue("BordereauDePrix_Tab");
            ILinkedResource newLinkedResource = null;
            Collection<IProperty> properties = null;
            IStorageResource articleDA = null, articleBDP = null;
            BigDecimal quantiteDA = null, quantiteBDP = null;
            String commentaireDA = null, commentaireBDP = null;
            Collection<String> urisDA = new ArrayList<>();
            Collection<String> referencesDA = new ArrayList<>();
            String uriDA = workflowInstanceDA.getProtocolURI();
            String referenceDA = (String)workflowInstanceDA.getValue("sys_Reference");
            boolean existe = false;
            for (ILinkedResource linkedResourceArticleDA : linkedResourcesArticlesDA) {
                existe = false;
                urisDA = new ArrayList<>();
                referencesDA = new ArrayList<>();
                articleDA = (IStorageResource)linkedResourceArticleDA.getValue("Article");
                if (linkedResourcesArticlesBDP != null && !linkedResourcesArticlesBDP.isEmpty())
                    for (ILinkedResource linkedResourceArticleBDP : linkedResourcesArticlesBDP) {
                        articleBDP = (IStorageResource)linkedResourceArticleBDP.getValue("Article");
                        if (articleDA != null && (articleBDP != null && articleDA.equals(articleBDP))) {
                            quantiteDA = castToBigDecimal(linkedResourceArticleDA.getValue("Quantite"));
                            quantiteBDP = castToBigDecimal(linkedResourceArticleBDP.getValue("Quantite"));
                            if (quantiteDA != null && quantiteBDP != null) {
                                linkedResourceArticleBDP.setValue("Quantite", quantiteBDP.add(quantiteDA));
                                urisDA = (ArrayList)linkedResourceArticleBDP.getValue("URISDA");
                                if (urisDA != null && !urisDA.contains(uriDA)) {
                                    urisDA.add(uriDA);
                                    linkedResourceArticleBDP.setValue("URISDA", urisDA);
                                    referencesDA = (ArrayList)linkedResourceArticleBDP.getValue("ReferencesDA");
                                    if (referencesDA != null && !referencesDA.contains(referenceDA)) {
                                        referencesDA.add(referenceDA);
                                        linkedResourceArticleBDP.setValue("ReferencesDA", referencesDA);
                                    }
                                }
                                commentaireDA = (String)linkedResourceArticleDA.getValue("Commentaire");
                                commentaireBDP = (String)linkedResourceArticleBDP.getValue("Commentaire");
                                if (commentaireDA != null && commentaireBDP != null) {
                                    linkedResourceArticleBDP.setValue("Commentaire", String.valueOf(commentaireBDP) + "\n" + commentaireDA);
                                } else if (commentaireBDP == null) {
                                    linkedResourceArticleBDP.setValue("Commentaire", commentaireDA);
                                }
                                linkedResourceArticleBDP.save(this.sysAdminContext);
                            }
                            existe = true;
                            break;
                        }
                    }
                if (!existe) {
                    properties = (Collection<IProperty>) linkedResourceArticleDA.getDefinition().getProperties();
                    newLinkedResource = getWorkflowInstance().createLinkedResource("BordereauDePrix_Tab");
                    for (IProperty property : properties) {
                        if (!property.getName().startsWith("sys_"))
                            newLinkedResource.setValue(property.getName(), linkedResourceArticleDA.getValue(property.getName()));
                    }
                    urisDA.add(uriDA);
                    newLinkedResource.setValue("URISDA", urisDA);
                    referencesDA.add(referenceDA);
                    newLinkedResource.setValue("ReferencesDA", referencesDA);
                    newLinkedResource.save(this.sysAdminContext);
                    getWorkflowInstance().addLinkedResource(newLinkedResource);
                }
            }
            getWorkflowInstance().save(this.sysAdminContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
