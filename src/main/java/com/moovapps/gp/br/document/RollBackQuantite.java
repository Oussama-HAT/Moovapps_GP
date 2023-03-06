package com.moovapps.gp.br.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.utils.calculate.castToBigDecimal;

public class RollBackQuantite extends BaseDocumentExtension {
    private static final long serialVersionUID = 5601744160228878765L;

    private IContext sysCreatorContext = null;

    private BigDecimal quantiteLivree_br = BigDecimal.ZERO;

    private BigDecimal quantiteLivree_bc = BigDecimal.ZERO;

    private BigDecimal resteALivrer_bc = BigDecimal.ZERO;

    private IStorageResource article_bc = null, article_br = null;

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Refuser")) {
                this.sysCreatorContext = getDirectoryModule().getContext(getWorkflowInstance().getCreatedBy());
                if (getWorkflowInstance().getParentInstance() != null) {
                    IWorkflowInstance parenInstance = getWorkflowInstance().getParentInstance();
                    Collection<ILinkedResource> BC_articles = (Collection<ILinkedResource>) parenInstance.getLinkedResources("BordereauDePrix_BC_Tab");
                    Collection<ILinkedResource> BR_articles = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("ListeDesArtices_Reception_Tab");
                    for (ILinkedResource br_art : BR_articles) {
                        this.article_br = (IStorageResource)br_art.getValue("Article");
                        this.quantiteLivree_br = castToBigDecimal(br_art.getValue("QuantiteLivree"));
                        for (ILinkedResource bc_art : BC_articles) {
                            this.article_bc = (IStorageResource)bc_art.getValue("Article");
                            if (this.article_bc.getValue("sys_Reference").equals(this.article_br.getValue("sys_Reference"))) {
                                this.quantiteLivree_bc = castToBigDecimal(bc_art.getValue("QuantiteLivree"));
                                this.resteALivrer_bc = castToBigDecimal(bc_art.getValue("ResteALivrer"));
                                this.quantiteLivree_bc = this.quantiteLivree_bc.subtract(this.quantiteLivree_br);
                                bc_art.setValue("QuantiteLivree", this.quantiteLivree_bc);
                                bc_art.setValue("ResteALivrer", this.resteALivrer_bc.add(this.quantiteLivree_br));
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
