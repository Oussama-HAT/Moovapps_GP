package com.moovapps.gp.br.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;

public class Affectation extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        Collection<ILinkedResource> listeArticles = (Collection<ILinkedResource>) getWorkflowInstance()
                                                    .getLinkedResources("ListeDesArtices_Reception_Tab");
        Collection<ILinkedResource> affectations = (Collection<ILinkedResource>) getWorkflowInstance()
                                                    .getLinkedResources("ListeDesAffectation");
        ILinkedResource affectation;
        Number qteArticle = 0;

        if(affectations.isEmpty())
        for (ILinkedResource article: listeArticles) {
            if(article.getValue("Stockable").equals("Immobilisation"))
            {
                qteArticle = (Number) article.getValue("QuantiteLivree")!=null?(Number) article.getValue("QuantiteLivree"):0;
                for (int i = 0; i < qteArticle.intValue(); i++) {

                    affectation = getWorkflowInstance().createLinkedResource("ListeDesAffectation");
                    if(getWorkflowInstance().getParentInstance()!=null)
                    {
                        affectation.setValue("NBC",getWorkflowInstance().getParentInstance().getValue("sys_Reference"));
                    }else
                    {
                        affectation.setValue("NBC",null);
                    }
                    affectation.setValue("NFacture",getWorkflowInstance().getValue("CodeFactureFournisseur"));
                    affectation.setValue("DesignationArticle",article.getValue("Article"));
                    affectation.setValue("Qte",1);
                    affectation.save(getDirectoryModule().getLoggedOnUserContext());

                    getWorkflowInstance().addLinkedResource(affectation);
                }
            }
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onAfterSubmit(IAction action) {
        if(action.getName().equals("EnvoyerPourValidation"))
        {
            Collection<ILinkedResource> listeAffectations = (Collection<ILinkedResource>) getWorkflowInstance()
                    .getLinkedResources("ListeDesAffectation");

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IContext context = getWorkflowModule().getLoggedOnUserContext();
            IStorageResource ficheImmobilisation = null;

            try {
                IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                IProject project = getProjectModule().getProject(sysContext, "ADMINISTRATIONGP", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RefAchats", ICatalog.IType.STORAGE, project);
                IResourceDefinition iResourceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "_8FicheImmobilisation");

                for (ILinkedResource affectaion: listeAffectations) {
                    if(affectaion.getValue("FicheImmobilisationURI")==null)
                    {
                        ficheImmobilisation = getWorkflowModule().createStorageResource(context, iResourceDefinition,null);
                    }else
                    {
                        ficheImmobilisation = (IStorageResource) getDirectoryModule().getElementByProtocolURI((String) affectaion.getValue("FicheImmobilisationURI"));
                    }
                    ficheImmobilisation.setValue("NBC",affectaion.getValue("NBC"));
                    ficheImmobilisation.setValue("NFacture",affectaion.getValue("NFacture"));
                    ficheImmobilisation.setValue("DesignationArticle",affectaion.getValue("DesignationArticle"));
                    ficheImmobilisation.setValue("NInventaire",affectaion.getValue("NInventaire"));
                    ficheImmobilisation.setValue("LieuDAffectation",affectaion.getValue("LieuDAffectation"));
                    ficheImmobilisation.setValue("AffectationParPersonnel",affectaion.getValue("AffectationParPersonnel"));
                    ficheImmobilisation.setValue("Observation",affectaion.getValue("Observation"));
                    ficheImmobilisation.setValue("Designation",affectaion.getValue("Designation"));
                    ficheImmobilisation.setValue("Auteur",affectaion.getValue("Auteur"));
                    ficheImmobilisation.setValue("Qte",affectaion.getValue("Qte"));
                    ficheImmobilisation.save(context);
                    if(affectaion.getValue("FicheImmobilisation")==null)
                    {
                        affectaion.setValue("FicheImmobilisation",ficheImmobilisation);
                        affectaion.setValue("FicheImmobilisationURI",ficheImmobilisation.getProtocolURI());
                        affectaion.save(context);
                    }
                }
                getWorkflowInstance().save(context);
            } catch (DirectoryModuleException | ProjectModuleException | WorkflowModuleException e) {
                e.printStackTrace();
            } catch (ModuleException e) {
                e.printStackTrace();
            }

        }
        return super.onAfterSubmit(action);
    }
}
