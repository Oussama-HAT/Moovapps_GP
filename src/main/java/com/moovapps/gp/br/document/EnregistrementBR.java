package com.moovapps.gp.br.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import java.util.Collection;

public class EnregistrementBR extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;

    private ILinkedResource iLinkedResource = null;

    private IContext sysCreatorContext = null;

    public boolean onAfterLoad() {
        try {
            this.sysCreatorContext = getDirectoryModule().getContext(getWorkflowInstance().getCreatedBy());
            if (getWorkflowInstance().getParentInstance() != null) {
                IWorkflowInstance parenInstance = getWorkflowInstance().getParentInstance();
                Collection<ILinkedResource> BC_articles = (Collection<ILinkedResource>) parenInstance.getLinkedResources("BordereauDePrix_BC_Tab");
                Collection<ILinkedResource> BR_articles = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("ListeDesArtices_Reception_Tab");
                for (ILinkedResource iLinkedResource : BR_articles)
                    getWorkflowInstance().deleteLinkedResource(iLinkedResource);
                for (ILinkedResource bc_art : BC_articles) {
                    this.iLinkedResource = getWorkflowInstance().createLinkedResource("ListeDesArtices_Reception_Tab");
                    this.iLinkedResource.setValue("Article", bc_art.getValue("Article"));
                    this.iLinkedResource.setValue("Quantite", bc_art.getValue("Quantite"));
                    this.iLinkedResource.setValue("ResteALivrer", bc_art.getValue("ResteALivrer"));
                    this.iLinkedResource.setValue("PrixUnitaire", bc_art.getValue("PrixUnitaire"));
                    this.iLinkedResource.setValue("Stockable", ((IStorageResource)bc_art.getValue("Article")).getValue("GestionDeStock"));
                    this.iLinkedResource.save(this.sysCreatorContext);
                    getWorkflowInstance().addLinkedResource(this.iLinkedResource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}
