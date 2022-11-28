package com.moovapps.gp.reception;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;

public class ValidationReseption extends BaseDocumentExtension {

    @Override
    public boolean onAfterSubmit(IAction action) {
        if(action.getName().equals("Accepter"))
        {
            Collection<ILinkedResource> listeArticles = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("ListeDesArtices_Reception_Tab");
            for (ILinkedResource article:  listeArticles) {
                addArticleToStock(article);
            }
        }
        return super.onAfterSubmit(action);
    }

    private void addArticleToStock(ILinkedResource article)
    {
        Number qteLivree = null;
        Number qteStock = null;
        IStorageResource articleRef = null;
        if(article.getValue("Stockable").equals("Géré en stock"))
        {
            try {
                IContext sysContext = getWorkflowModule().getSysadminContext();
                IContext Context = getWorkflowModule().getLoggedOnUserContext();
                IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                IProject project = getProjectModule().getProject(sysContext, "ADMINISTRATIONGP", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RefAchats", ICatalog.IType.STORAGE, project);
                IResourceDefinition iResourceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "JournalDeStock");
                IStorageResource jounalStock = getWorkflowModule().createStorageResource(Context, iResourceDefinition, null);

                articleRef = (IStorageResource) article.getValue("Article");
                qteLivree = (Number) article.getValue("QuantiteLivree");
                qteStock = (Number) articleRef.getValue("Qte");

                jounalStock.setValue("Article", article.getValue("Article"));
                jounalStock.setValue("Date", getWorkflowInstance().getValue("DateDeReception"));
                jounalStock.setValue("Magasin", article.getValue("Magasin"));
                jounalStock.setValue("Prix", article.getValue("PrixUnitaire"));
                jounalStock.setValue("QteStock", article.getValue("QuantiteLivree"));
                jounalStock.setValue("Type", "Réception");
                jounalStock.setValue("Categorie", "Entrée");

                if(qteStock!=null)
                {
                    articleRef.setValue("Qte",qteStock.intValue()+qteLivree.intValue());
                }else
                {
                    articleRef.setValue("Qte",qteLivree.intValue());
                }

                articleRef.save(Context);
                jounalStock.save(Context);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
