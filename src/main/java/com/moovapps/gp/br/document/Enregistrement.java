package com.moovapps.gp.br.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class Enregistrement extends BaseDocumentExtension {

    public boolean onAfterLoad() {
        try {
            IContext sysCreatorContext = getDirectoryModule().getContext(getWorkflowInstance().getCreatedBy());
            ILinkedResource iLinkedResource = null;

            if (getWorkflowInstance().getParentInstance() != null) {

                IWorkflowInstance parenInstance = getWorkflowInstance().getParentInstance();

                Collection<ILinkedResource> BC_articles = (Collection<ILinkedResource>) parenInstance.getLinkedResources("BordereauDePrix_BC_Tab");
                Collection<ILinkedResource> BR_articles = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("ListeDesArtices_Reception_Tab");

                BigDecimal quantite = null;
                BigDecimal prixU = null;
                BigDecimal tva = null;
                BigDecimal totalHT = null;
                BigDecimal totalTTC = null;
                //getWorkflowInstance().deleteLinkedResources(BR_articles);

                if(BR_articles!=null)
                if(BR_articles.size()==0)
                for (ILinkedResource bc_art : BC_articles) {
                    quantite = castToBigDecimal(bc_art.getValue("Quantite"));
                    prixU = castToBigDecimal(bc_art.getValue("PrixUnitaire"));
                    tva = castToBigDecimal(bc_art.getValue("TVA"));
                    totalHT = (prixU !=null && quantite !=null) ? prixU.multiply(quantite) : null;
                    //totalHT =  (prixU!=null && quantite!=null) ?prixU.floatValue()*quantite.intValue():null;*
                    totalTTC = tva!=null ? totalHT.add(totalHT.multiply(tva).divide(BigDecimal.valueOf(100))) : null;
                    //totalTTC = (tva!=null)? totalHT.floatValue()+(totalHT.floatValue()*tva.floatValue()/100):null;

                    iLinkedResource = getWorkflowInstance().createLinkedResource("ListeDesArtices_Reception_Tab");
                    iLinkedResource.setValue("Article", bc_art.getValue("Article"));
                    iLinkedResource.setValue("Quantite", quantite);
                    iLinkedResource.setValue("QuantiteLivree", quantite);
                    iLinkedResource.setValue("ResteALivrer", BigDecimal.ZERO);
                    iLinkedResource.setValue("PrixUnitaire", prixU);
                    iLinkedResource.setValue("TVA", tva);
                    iLinkedResource.setValue("PrixTotalHT",totalHT);
                    iLinkedResource.setValue("PrixTotalTTC",totalTTC);
                    iLinkedResource.setValue("Stockable", ((IStorageResource)bc_art.getValue("Article")).getValue("Type"));
                    iLinkedResource.save(sysCreatorContext);
                    getWorkflowInstance().addLinkedResource(iLinkedResource);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}