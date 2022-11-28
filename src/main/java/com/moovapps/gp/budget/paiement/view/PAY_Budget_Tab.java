package com.moovapps.gp.budget.paiement.view;

import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.*;
import com.moovapps.gp.services.DirectoryService;

import java.util.Collection;
import java.util.List;

public class PAY_Budget_Tab extends BaseViewExtension {
    private static final long serialVersionUID = 1L;

    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    protected IContext loggedOnUserContext = null;

    private Double montantEngage = new Double(0.0D);

    private Double MontantPaye = new Double(0.0D);

    private Double montantAPayer = new Double(0.0D);

    private Double resteAPayer = new Double(0.0D);

    protected Object valeur = null;

    public void onPrepareColumns(List list) {
        try {
            this.sysAdminContext = getWorkflowModule().getSysadminContext();
            this.loggedOnUserContext = getWorkflowModule().getLoggedOnUserContext();
            NamedContainer topButtonContainer = getView().getButtonsContainer();
            List<IWidget> buttonList = topButtonContainer.getWidgets();
            for (IWidget iWidget : buttonList) {
                INamedWidget iNamedWidget = (INamedWidget)iWidget;
                if (!iNamedWidget.getName().equals("delete"))
                    iNamedWidget.setHidden(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPrepareColumns(list);
    }

    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            ILinkedResource ILinkedResource = (ILinkedResource)iViewItem.getResource();
            CtlNumber montantAPayer = ctlNumber("MontantAPayer", ILinkedResource);
            viewModelItem.setValue("MontantAPayer", montantAPayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CtlNumber ctlNumber(String name, ILinkedResource iLinkedResource) {
        CtlNumber ctlNumber = new CtlNumber();
        ctlNumber.setParam(iLinkedResource);
        ctlNumber.setSysname(name);
        ctlNumber.setThrowEvents(true);
        this.valeur = iLinkedResource.getValue(name);
        if (this.valeur != null)
            ctlNumber.setLabel(String.valueOf(this.valeur).replace(".0", ""));
        ctlNumber.addChangeListener(this.listener);
        return ctlNumber;
    }

    protected ChangeListener listener = new ChangeListener() {
        private static final long serialVersionUID = -8874731864069411182L;

        public void onChange(ChangeEvent paramChangeEvent) {
            ILinkedResource ILinkedResource = null;
            Object object = paramChangeEvent.getSource();
            if (CtlNumber.class.equals(object.getClass())) {
                CtlNumber number = (CtlNumber)object;
                ILinkedResource = (ILinkedResource)number.getParam();
                PAY_Budget_Tab.this.resteAPayer = (Double)ILinkedResource.getValue("ResteAPayer");
                if (number.getDoubleValue().doubleValue() > PAY_Budget_Tab.this.resteAPayer.doubleValue()) {
                    ILinkedResource.setValue(number.getSysname(), PAY_Budget_Tab.this.resteAPayer);
                } else {
                    ILinkedResource.setValue(number.getSysname(), number.getDoubleValue());
                }
                ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
                PAY_Budget_Tab.this.getView().refreshItems();
            } else if (CtlCheckBox.class.equals(object.getClass())) {
                CtlCheckBox checkBox = (CtlCheckBox)object;
                ILinkedResource = (ILinkedResource)checkBox.getParam();
                ILinkedResource.setValue(checkBox.getSysname(), Boolean.valueOf(checkBox.isChecked()));
                ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
            } else if (CtlComboBox.class.equals(object.getClass())) {
                CtlComboBox box = (CtlComboBox)object;
                ILinkedResource = (ILinkedResource)box.getParam();
                ILinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
            } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                CtlAutocompleteList box = (CtlAutocompleteList)object;
                ILinkedResource = (ILinkedResource)box.getParam();
                ILinkedResource.setValue(box.getSysname(), box.getSelectedKey());
                ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
                PAY_Budget_Tab.this.getView().refreshItems();
            } else if (CtlRadioGroup.class.equals(object.getClass())) {
                CtlRadioGroup radio = (CtlRadioGroup)object;
                ILinkedResource = (ILinkedResource)radio.getParam();
                ILinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
            } else if (CtlDate.class.equals(object.getClass())) {
                CtlDate date = (CtlDate)object;
                ILinkedResource = (ILinkedResource)date.getParam();
                ILinkedResource.setValue(date.getSysname(), date.getDate());
                ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
            } else if (CtlMultipleFileUpload.class.equals(object.getClass())) {
                CtlMultipleFileUpload multipleFileUpload = (CtlMultipleFileUpload)object;
                ILinkedResource = (ILinkedResource)multipleFileUpload.getParam();
                try {
                    ILinkedResource.setValue(multipleFileUpload.getSysname(), null);
                    Collection<IAttachment> documents = multipleFileUpload.getFiles();
                    for (IAttachment attachment : documents)
                        PAY_Budget_Tab.this.getWorkflowModule().addAttachment((IResource)ILinkedResource, multipleFileUpload.getSysname(), attachment);
                    ILinkedResource.save(PAY_Budget_Tab.this.sysAdminContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}

