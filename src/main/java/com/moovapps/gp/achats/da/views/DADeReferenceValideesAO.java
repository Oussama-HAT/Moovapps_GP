package com.moovapps.gp.achats.da.views;
import com.axemble.vdoc.sdk.interfaces.IContext;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
public class DADeReferenceValideesAO extends BaseViewExtension {
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
                workflowInstance.save(DADeReferenceValideesAO.this.sysAdminContext);
            }
        }
    };

    ActionListener ValiderSelection = new ActionListener() {
        public void onClick(ActionEvent arg0) {
            try {
                ProcessWorkflowInstance workflowInstanceDA = null;
                DADeReferenceValideesAO.this.getWorkflowInstance().setValue("URISDA", null);
                Boolean check = null;
                Collection<String> uris = new ArrayList<>();
                for (Object object : DADeReferenceValideesAO.this.uiAllComponentCollection) {
                    if (ProcessWorkflowInstance.class.equals(object.getClass())) {
                        workflowInstanceDA = (ProcessWorkflowInstance)object;
                        check = (Boolean)workflowInstanceDA.getValue("Check");
                        if (check != null && check.booleanValue()) {
                            uris.add(workflowInstanceDA.getProtocolURI());
                            workflowInstanceDA.setValue("ReferenceDeLAO", DADeReferenceValideesAO.this.getWorkflowInstance().getValue("sys_Reference"));
                        } else {
                            workflowInstanceDA.setValue("ReferenceDeLAO", null);
                        }
                        workflowInstanceDA.save(DADeReferenceValideesAO.this.sysAdminContext);
                    }
                }
                DADeReferenceValideesAO.this.getWorkflowInstance().setValue("URISDA", uris);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

