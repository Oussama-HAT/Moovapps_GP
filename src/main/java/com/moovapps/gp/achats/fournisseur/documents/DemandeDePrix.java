package com.moovapps.gp.achats.fournisseur.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

public class DemandeDePrix extends BaseDocumentExtension {
    public boolean onAfterLoad() {
        try {
            CtlButton button = getResourceController().getButton("Envoyer", 2);
            if (button != null)
                button.setHidden(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public boolean onBeforeSave() {
        try {
            IStorageResource fournisseur = (IStorageResource)getWorkflowInstance().getValue("Fournisseur");
            if (fournisseur != null) {
                getWorkflowInstance().setValue("SousSecteurDActivite", fournisseur.getValue("SousSecteurDActivite"));
                getWorkflowInstance().setValue("SecteurDActivite", fournisseur.getValue("SecteurDActivite"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSave();
    }
}
