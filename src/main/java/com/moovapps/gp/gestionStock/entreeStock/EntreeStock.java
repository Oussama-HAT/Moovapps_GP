package com.moovapps.gp.gestionStock.entreeStock;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.math.BigDecimal;
import java.util.Collection;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class EntreeStock extends BaseDocumentExtension {

    @Override
    public boolean onAfterSubmit(IAction action) {
        if(action.getName().equals("Envoyer"))
        {
            IStorageResource articleRef = null;
            BigDecimal qteLivree = null;
            BigDecimal qteStock = null;
            Collection<ILinkedResource> listeArticlesReceptiones =
                    (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("ListeDesArticles");

            for (ILinkedResource article : listeArticlesReceptiones)
            {
                try{
                    IContext sysContext = getWorkflowModule().getSysadminContext();
                    IContext Context = getWorkflowModule().getLoggedOnUserContext();
                    IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(sysContext, "ADMINISTRATIONGP", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RefAchats", ICatalog.IType.STORAGE, project);
                    IResourceDefinition iResourceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "JournalDeStock");
                    IStorageResource jounalStock = getWorkflowModule().createStorageResource(sysContext, iResourceDefinition, null);
                    articleRef = (IStorageResource) article.getValue("Article");
                    qteLivree = castToBigDecimal(article.getValue("QuantLivree"));
                    qteStock = castToBigDecimal(articleRef.getValue("Qte"));
                    jounalStock.setValue("Article", article.getValue("Article"));
                    jounalStock.setValue("Date", getWorkflowInstance().getValue("DateDeReception"));
                    jounalStock.setValue("Magasin", article.getValue("Magasin"));
                    jounalStock.setValue("Prix", article.getValue("PrixUnitaire"));
                    jounalStock.setValue("QteStock", article.getValue("QuantLivree"));
                    jounalStock.setValue("Type", getWorkflowInstance().getValue("TypeDEntreeStock"));
                    jounalStock.setValue("Type2", getWorkflowInstance().getValue("TypeDeSortieStock"));
                    jounalStock.setValue("Categorie", getWorkflowInstance().getValue("Categorie"));

                    if(qteStock!=null)
                    {
                        if(getWorkflowInstance().getValue("Categorie").equals("Entrée"))
                        {
                            articleRef.setValue("Qte",qteStock.add(qteLivree));
                        }else
                        {
                            articleRef.setValue("Qte",qteStock.subtract(qteLivree));
                        }

                    }else
                    {
                        if(getWorkflowInstance().getValue("Categorie").equals("Entrée"))
                        {
                            articleRef.setValue("Qte",qteLivree);
                        }else
                        {
                            articleRef.setValue("Qte",qteLivree.negate());
                        }
                    }
                    articleRef.save(Context);
                    jounalStock.save(Context);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return super.onAfterSubmit(action);
    }
}
