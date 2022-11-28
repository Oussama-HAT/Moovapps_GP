package com.moovapps.gp.budget.preparationBudget.views;

import com.axemble.vdoc.sdk.impl.ProcessLinkedResource;
import com.axemble.vdoc.sdk.impl.ProcessStorageResource;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.widgets.CtlNumber;
import com.axemble.vdp.ui.framework.widgets.CtlRadioGroup;
import com.axemble.vdp.ui.framework.widgets.list.ListLayout;
import com.moovapps.gp.services.DirectoryService;

public class PreparationBudgetEditingViewExtension extends BaseViewExtension {
    private static final Logger LOG = Logger.getLogger(PreparationBudgetEditingViewExtension.class);
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            ProcessLinkedResource processLinkedResource = (ProcessLinkedResource) iViewItem.getResource();
            IStorageResource natureBudgetRef = (IStorageResource) processLinkedResource.getParentInstance().getValue("NatureBudget");
            if (natureBudgetRef != null && natureBudgetRef.getValue("sys_Title").equals("Investissement")) {
                CtlNumber ctlNumber = new CtlNumber();
                ctlNumber.setParam(processLinkedResource);
                ctlNumber.setSysname("MontantTotalBudgetCE");
                ctlNumber.updateFormat();
                CtlRadioGroup radioGroup = new CtlRadioGroup();
                radioGroup.setThrowEvents(true);
                radioGroup.setParam(processLinkedResource);
                radioGroup.setSysname("Participation");
                radioGroup.setEditable(true);
                radioGroup.showResetButton(false);
                ((ListLayout) radioGroup.getLayout()).verticalMode = true;
                ((ListLayout) radioGroup.getLayout()).nbOfColumns = 2;
                radioGroup.setMandatory(true);
                radioGroup.setList(processLinkedResource.getList("Participation"));
                radioGroup.setSelectedKey(processLinkedResource.getValue("Participation"));
                radioGroup.addChangeListener(this.listener);
                viewModelItem.setValue("Participation", radioGroup);
            }
        } catch (Exception e) {
            LOG.error("Error in class" + e.getClass() + "onPrepareItem method : " + e.getClass() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected ChangeListener listener = new ChangeListener() {
        public void onChange(ChangeEvent paramChangeEvent) {
            ProcessLinkedResource processLinkedResource = null;
            Object object = paramChangeEvent.getSource();
            try {
                if (CtlRadioGroup.class.equals(object.getClass())) {
                    CtlRadioGroup radio = (CtlRadioGroup) object;
                    processLinkedResource = (ProcessLinkedResource) radio.getParam();
                    processLinkedResource.setValue(radio.getSysname(), radio.getSelectedKey());
                    //processLinkedResource.save(PreparationBudgetEditingViewExtension.sysAdminContext);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            PreparationBudgetEditingViewExtension.this.getView().refreshItems();
        }
    };
}
