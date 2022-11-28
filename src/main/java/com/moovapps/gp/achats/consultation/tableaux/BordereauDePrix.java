package com.moovapps.gp.achats.consultation.tableaux;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

public class BordereauDePrix extends BaseDocumentExtension {
    public boolean onBeforeSave() {
        try {
            IStorageResource article = (IStorageResource)getWorkflowInstance().getValue("Article");
            if (article != null) {
                getWorkflowInstance().setValue("SousFamilleDArticles", article.getValue("SousFamilleDArticles"));
                getWorkflowInstance().setValue("FamilleDArticles", article.getValue("FamilleDArticles"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSave();
    }
}
