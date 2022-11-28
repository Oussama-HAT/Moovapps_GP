package com.moovapps.Equipment;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;

public class historiqueDesCompteurs extends BaseDocumentExtension {
    protected static final Logger LOG = Logger.getLogger(historiqueDesCompteurs.class);

    private static final long serialVersionUID = 1L;

    static final String NOM_PROJET = "MoovappsWorkplaceCSP";

    static final String NOM_RESERVOIR_DONNEES = "products";

    static final String NOM_TABLE = "HistoriqueReleveDeCompteur";

    static final int CATALOG_RESERVOIR = 4;

    public boolean onBeforeSubmit(IAction action) {
        try {
            if (action.getName().equals("Envoyer2")) {
                IWorkflowModule workflowModule = getWorkflowModule();
                IProjectModule projectModule = getProjectModule();
                IDirectoryModule directoryModule = getDirectoryModule();
                IContext context = workflowModule.getSysadminContext();
                IOrganization organization = directoryModule.getOrganization(context, "DefaultOrganization");
                IProject project = projectModule.getProject(context, "MoovappsWorkplaceCSP", organization);
                ICatalog catalogReferentiels = workflowModule.getCatalog(context, "products", 4, project);
                IResourceDefinition historiqueCompteur = workflowModule.getResourceDefinition(context, catalogReferentiels, "HistoriqueReleveDeCompteur");
                IStorageResource compteur = workflowModule.createStorageResource(context, historiqueCompteur, null);
                compteur.setValue("EQUIPEMENT", getWorkflowInstance().getValue("EQUIPEMENT"));
                compteur.setValue("DateReleve", getWorkflowInstance().getValue("DateReleveALAffectation"));
                compteur.setValue("ValeurCompteur", getWorkflowInstance().getValue("NouvelleValeurDuCompteur"));
                compteur.save(context);
            }
        } catch (DirectoryModuleException e) {
            e.printStackTrace();
        } catch (ProjectModuleException e) {
            e.printStackTrace();
        } catch (WorkflowModuleException e) {
            e.printStackTrace();
        }
        return true;
    }
}
