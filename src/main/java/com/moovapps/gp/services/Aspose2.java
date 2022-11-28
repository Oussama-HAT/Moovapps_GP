package com.moovapps.gp.services;

import com.aspose.words.Document;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IConfiguration;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IFile;
import com.axemble.vdoc.sdk.interfaces.ILibrary;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.foundation.Navigator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

public class Aspose2 extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;
    protected static final Logger log = Logger.getLogger(Aspose.class);

    public void GenerationDuDocumentWord(IWorkflowModule workflowModule, IResource iResource, IResourceController resourceController, String cheminModeleWord, String champPJName, String titrePJOutput) {
        ILibraryModule libraryModule = Modules.getLibraryModule();
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IOrganization organization = DirectoryService.getOrganization(directoryModule, workflowModule);
            IConfiguration configuration = workflowModule.getConfiguration();
            //String libraryName = configuration.getStringProperty("com.moovapps....").trim();
            //String cheminTempFolder = configuration.getStringProperty("com.moovapps....").trim();

            String libraryName = "Modeles";
            String cheminTempFolder = "./Temp";
            ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
            if (library == null) {
                resourceController.alert("Le chemin du fichier de modele n'existe pas dans l'espace documentaire... Veuillez contacter votre administrateur");
            }
            IFile file = libraryModule.getFileByPath(context, library, cheminModeleWord);
            if (file == null) {
                resourceController.alert("Le fichier de modele n'existe pas dans l'espace documentaire... Veuillez contacter votre administrateur");
            }
            GenerateWordFile generateWordFile = new GenerateWordFile();
            IAttachment attachement = libraryModule.getAttachment(file, file.getName());
            InputStream inputStream = generateWordFile.valorization(workflowModule, iResource, attachement);
            Document document = new Document(inputStream);
            document.save(String.valueOf(cheminTempFolder) + "\\" + titrePJOutput + ".pdf", 40);
            iResource.setValue(champPJName, null);
            workflowModule.addAttachment((IResource) iResource, champPJName, new File(String.valueOf(cheminTempFolder) + "\\" + titrePJOutput + ".pdf"));
            iResource.save(champPJName);
            File MyFile = new File(cheminTempFolder + "\\" + titrePJOutput +".pdf");
            MyFile.delete();
        } catch (IOException | com.axemble.vdoc.sdk.exceptions.ModuleException | com.axemble.vdoc.sdk.exceptions.SDKException e) {

            if (libraryModule.isTransactionActive()) {
                libraryModule.rollbackTransaction();
            }
            String message = "Error in Aspose GenerationDuDocumentWord method : " + e.getClass() + " - " + e.getMessage();
            Navigator.getNavigator().getRootNavigator().showAlertBox(message);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null) {
                message = "";
            }
            e.printStackTrace();
            log.error("Error in Aspose GenerationDuDocumentWord method : " + e.getClass() + " - " + message);
        } finally {

            if (!libraryModule.isTransactionActive()) {
                Modules.releaseModule((IModule) libraryModule);
            }
            Modules.releaseModule((IModule) directoryModule);
        }
    }

    public void GenerationDuDocumentPDF(IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IResourceController resourceController, String champPJName, String titrePJOutput) {
        ILibraryModule libraryModule = Modules.getLibraryModule();
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            IConfiguration configuration = workflowModule.getConfiguration();
            //String cheminTempFolder = configuration.getStringProperty("com.moovapps....").trim();
            String cheminTempFolder = "./Temp";
            Collection<IAttachment> attachments = (Collection<IAttachment>) workflowInstance.getValue("PVWordDeLaReunion");
            if (attachments != null && !attachments.isEmpty()) {
                byte[] byteContent = ((IAttachment) attachments.iterator().next()).getContent();
                FileUtils.writeByteArrayToFile(new File(String.valueOf(cheminTempFolder) + "\\" + ((IAttachment) attachments.iterator().next()).getName()), byteContent);
                File fileToStore = new File(String.valueOf(cheminTempFolder) + "\\" + ((IAttachment) attachments.iterator().next()).getName());
                InputStream inputStream = new FileInputStream(fileToStore);
                Document document = new Document(inputStream);
                document.save(String.valueOf(cheminTempFolder) + "\\" + titrePJOutput + ".pdf", 40);
                workflowInstance.setValue(champPJName, null);
                workflowModule.addAttachment((IResource) workflowInstance, champPJName, new File(String.valueOf(cheminTempFolder) + "\\" + titrePJOutput + ".pdf"));
                workflowInstance.save(champPJName);
                File MyFile = new File(cheminTempFolder + "\\" + titrePJOutput + ".pdf");
                MyFile.delete();
                fileToStore.delete();
            } else {
                resourceController.alert("ERROR");
            }

        } catch (IOException | com.axemble.vdoc.sdk.exceptions.ModuleException | com.axemble.vdoc.sdk.exceptions.SDKException e) {
            if (libraryModule.isTransactionActive()) {
                libraryModule.rollbackTransaction();
            }
            String message = "Error in Aspose GenerationDuDocumentPDF method : " + e.getClass() + " - " + e.getMessage();
            Navigator.getNavigator().getRootNavigator().showAlertBox(message);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null) {
                message = "";
            }
            e.printStackTrace();
            log.error("Error in Aspose GenerationDuDocumentPDF method : " + e.getClass() + " - " + message);
        } finally {
            if (!libraryModule.isTransactionActive()) {
                Modules.releaseModule((IModule) libraryModule);
            }
            Modules.releaseModule((IModule) directoryModule);
        }
    }
}