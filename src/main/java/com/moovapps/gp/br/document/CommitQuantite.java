package com.moovapps.gp.br.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import java.util.Collection;

public class CommitQuantite extends BaseDocumentExtension {
    private static final long serialVersionUID = 5601744160228878765L;

    private IContext sysCreatorContext = null;

    private Float quantiteLivree_br = Float.valueOf(0.0F);

    private Float quantiteLivree_bc = Float.valueOf(0.0F);

    private Float resteALivrer_bc = Float.valueOf(0.0F);

    private IStorageResource article_bc = null, article_br = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Envoyer")) {
                this.sysCreatorContext = getDirectoryModule().getContext(getWorkflowInstance().getCreatedBy());
                if (getWorkflowInstance().getParentInstance() != null) {
                    IWorkflowInstance parenInstance = getWorkflowInstance().getParentInstance();
                    Collection<ILinkedResource> BC_articles = (Collection<ILinkedResource>) parenInstance.getLinkedResources("BordereauDePrix_BC_Tab");
                    Collection<ILinkedResource> BR_articles = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("ListeDesArtices_Reception_Tab");
                    for (ILinkedResource br_art : BR_articles) {
                        this.article_br = (IStorageResource)br_art.getValue("Article");
                        this.quantiteLivree_br = (Float)br_art.getValue("QuantiteLivree");
                        for (ILinkedResource bc_art : BC_articles) {
                            this.article_bc = (IStorageResource)bc_art.getValue("Article");
                            if (this.article_bc.getValue("sys_Reference").equals(this.article_br.getValue("sys_Reference"))) {
                                this.quantiteLivree_bc = (Float)bc_art.getValue("QuantiteLivree");
                                this.resteALivrer_bc = (Float)bc_art.getValue("ResteALivrer");
                                this.quantiteLivree_bc = Float.valueOf(this.quantiteLivree_bc.floatValue() + this.quantiteLivree_br.floatValue());
                                bc_art.setValue("QuantiteLivree", this.quantiteLivree_bc);
                                bc_art.setValue("ResteALivrer", Float.valueOf(this.resteALivrer_bc.floatValue() - this.quantiteLivree_br.floatValue()));
                                bc_art.save(this.sysCreatorContext);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            getResourceController().alert("Une erreur est survenue, veuillez contacter l'administrateur");
            return false;
        }
        return super.onBeforeSubmit(action);
    }
}
