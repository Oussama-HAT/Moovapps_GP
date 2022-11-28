package com.moovapps.gp.services;


import com.aspose.words.Document;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

public class Aspose extends BaseDocumentExtension {

    public void GenerationDuDocumentPDF (IWorkflowModule workflowModule , IWorkflowInstance workflowInstance , IResourceController resourceController,String cheminFileCenter, String champPJName, String titrePJOutput)
    {
        ILibraryModule libraryModule = Modules.getLibraryModule();
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try
        {
            //IConfiguration configuration = workflowModule.getConfiguration();
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IOrganization organization = DirectoryService.getDefaultOrganization();
            //----------------------------------------- Param�tres ------------------------------------

            String libraryName = "Modeles";
            String cheminTempFolder = "./Temp";

            ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
            if(library == null)
            {
                resourceController.alert("l'espace documentaire n'existe pas... Vieullez contacter votre administrateur"+context.getUser().getFullName());
            }else
            {
                    Collection<IFolder> folders = library.getFolders(context);

                IFile file = libraryModule.getFileByPath(context, library, cheminFileCenter);
                if (file == null)
                {
                    resourceController.alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
                }
                else
                {
                    IAttachment attachement = libraryModule.getAttachment(file, file.getName());
                    byte[] byteContent = attachement.getContent();
                    FileUtils.writeByteArrayToFile(new File(cheminTempFolder +"\\" + attachement.getName()),byteContent);
                    File fileToStore = new File(cheminTempFolder +"\\" + attachement.getName());

                    InputStream inputStream = new FileInputStream(fileToStore);
                    Document document = new Document(inputStream);
                    document.save(cheminTempFolder+ "\\" + titrePJOutput + ".pdf", 40);
                    workflowInstance.setValue(champPJName, null);
                    workflowModule.addAttachment(workflowInstance, champPJName, new File(cheminTempFolder+ "\\" + titrePJOutput + ".pdf"));
                    //workflowInstance.save(champPJName);

                    File MyFile = new File(String.valueOf(cheminTempFolder) + "\\" + titrePJOutput + ".pdf");
                    MyFile.delete();
                    fileToStore.delete();
                    fileToStore.deleteOnExit();
                }
            }
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message == null)
            {
                message = "";
            }
            e.printStackTrace();
            LOGGER.error("Error in Aspose GenerationDuDocumentPDF method : " + e.getClass() + " - " + message);
        }finally {

            if (!libraryModule.isTransactionActive())
              {
               Modules.releaseModule((IModule)libraryModule);
              }

             Modules.releaseModule((IModule)directoryModule);
           }

        //Script moovapps processus:
        //var jsObject = new Packages.com.moovapps.gp.services.Aspose();
        //jsObject.genererPVReunionAdhocWord(iWorkflowModule, iWorkflowInstance, iResourceController,"cheminFileCenter","champPJName","titrePJOutput");
    }
    public void GenerationDuDocumentPDF2 (IWorkflowModule workflowModule , ILinkedResource workflowInstance , IResourceController resourceController,String cheminFileCenter, String champPJName, String titrePJOutput)
    {
        ILibraryModule libraryModule = Modules.getLibraryModule();
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try
        {
            //IConfiguration configuration = workflowModule.getConfiguration();
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IOrganization organization = DirectoryService.getDefaultOrganization();
            //----------------------------------------- Param�tres ------------------------------------

            String libraryName = "Modeles";
            String cheminTempFolder = "./Temp";

            ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
            if(library == null)
            {
                resourceController.alert("l'espace documentaire n'existe pas... Veuillez contacter votre administrateur");
            }else
            {
                Collection<IFolder> folders = library.getFolders(context);

                IFile file = libraryModule.getFileByPath(context, library, cheminFileCenter);
                if (file == null)
                {
                    resourceController.alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
                }
                else
                {
                    IAttachment attachement = libraryModule.getAttachment(file, file.getName());
                    byte[] byteContent = attachement.getContent();
                    FileUtils.writeByteArrayToFile(new File(cheminTempFolder +"\\" + attachement.getName()),byteContent);
                    File fileToStore = new File(cheminTempFolder +"\\" + attachement.getName());

                    InputStream inputStream = new FileInputStream(fileToStore);
                    Document document = new Document(inputStream);
                    document.save(cheminTempFolder+ "\\" + titrePJOutput + ".pdf", 40);
                    workflowInstance.setValue(champPJName, null);
                    workflowModule.addAttachment(workflowInstance, champPJName, new File(cheminTempFolder+ "\\" + titrePJOutput + ".pdf"));
                    //workflowInstance.save(champPJName);

                    File MyFile = new File(String.valueOf(cheminTempFolder) + "\\" + titrePJOutput + ".pdf");
                    MyFile.delete();
                    fileToStore.delete();
                    fileToStore.deleteOnExit();
                }
            }
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message == null)
            {
                message = "";
            }
            e.printStackTrace();
            LOGGER.error("Error in Aspose GenerationDuDocumentPDF method : " + e.getClass() + " - " + message);
        }finally {

            if (!libraryModule.isTransactionActive())
            {
                Modules.releaseModule((IModule)libraryModule);
            }

            Modules.releaseModule((IModule)directoryModule);
        }

        //Script moovapps processus:
        //var jsObject = new Packages.com.moovapps.gp.services.Aspose();
        //jsObject.genererPVReunionAdhocWord(iWorkflowModule, iWorkflowInstance, iResourceController,"cheminFileCenter","champPJName","titrePJOutput");
    }
}
