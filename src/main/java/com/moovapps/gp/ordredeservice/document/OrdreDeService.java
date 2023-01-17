package com.moovapps.gp.ordredeservice.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowContainer;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

import java.util.Collection;

public class OrdreDeService extends BaseDocumentExtension {
    private static final long serialVersionUID = -3309182896981840228L;

    public boolean onAfterLoad() {
        try {
            NamedContainer containerBOTTOM = getResourceController().getButtonContainer(2);
            Collection<IWidget> widgetsBOTTOM = containerBOTTOM.getWidgets();
            for (IWidget widget : widgetsBOTTOM) {
                CtlButton ctlButton = (CtlButton) widget;
                if (ctlButton.getName().equals("Clôturer"))
                    ctlButton.setHidden(true);
            }
            NamedContainer containerTOP = getResourceController().getButtonContainer(1);
            Collection<IWidget> widgetsTOP = containerTOP.getWidgets();
            for (IWidget widget : widgetsTOP) {
                CtlButton ctlButton = (CtlButton) widget;
                if (!ctlButton.getName().equals("abort"))
                    ctlButton.setHidden(true);
            }
            if (getWorkflowInstance().getParentInstance() == null) {
                getResourceController().setEditable("ReferenceDuMarche_OS", true);
            } else {
                getResourceController().setEditable("ReferenceDuMarche_OS", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Valider"))
                if (getWorkflowInstance().getParentInstance() == null)
                    if (!((Boolean) getWorkflowInstance().getValue("ParenteInstance_MARCHE")).booleanValue()) {
                        String referenceDuMarche = (String) getWorkflowInstance().getValue("ReferenceDuMarche_OS");
                        IWorkflowInstance marche = getMarcheFromReference(referenceDuMarche);
                        marche.addLinkedWorkflowInstance("OrdresDeService", getWorkflowInstance());
                        marche.save(getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().setValue("ParenteInstance_MARCHE", Boolean.TRUE);
                    }
        } catch (Exception e) {
            getResourceController().alert("Un problèùe est survenu, veuillez contacter votre administrateur");
            e.printStackTrace();
            return false;
        }
        return super.onBeforeSubmit(action);
    }

    private IWorkflowInstance getMarcheFromReference(String referenceDuMarche) {
        try {
            IWorkflowContainer iWorkflowContainer = getWorkflowModule().getWorkflowContainer(getWorkflowModule().getSysadminContext(), getWorkflowInstance().getWorkflow().getCatalog(), "marche");
            IViewController iViewController = getWorkflowModule().getViewController(getWorkflowModule().getSysadminContext());
            iViewController.addEqualsConstraint("sys_Reference", referenceDuMarche);
            return (IWorkflowInstance) iViewController.evaluate(iWorkflowContainer).iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

