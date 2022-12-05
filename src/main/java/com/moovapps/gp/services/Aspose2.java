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
import com.axemble.vdoc.sdk.utils.StringUtils;
import com.axemble.vdp.ui.framework.foundation.Navigator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.moovapps.gp.helpers.ExpressionUtils;
import org.apache.commons.io.FileUtils;

public class Aspose2 extends BaseDocumentExtension {
    private static final long serialVersionUID = 1L;
    protected static final Logger log = Logger.getLogger(Aspose.class);


//    public void GenerationDuDocumentWord(IWorkflowModule workflowModule, IResource iResource, IResourceController resourceController, String cheminModeleWord, String champPJName, String titrePJOutput) {
//        ILibraryModule libraryModule = Modules.getLibraryModule();
//        IDirectoryModule directoryModule = Modules.getDirectoryModule();
//        try {
//            IContext context = libraryModule.getContextByLogin("sysadmin");
//            IOrganization organization = DirectoryService.getOrganization(directoryModule, workflowModule);
//            String libraryName = "Modeles";
//            String cheminTempFolder = "./Temp";
//            ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
//            if (library == null) {
//                resourceController.alert("Le chemin du fichier de modele n'existe pas dans l'espace documentaire... Veuillez contacter votre administrateur");
//            }
//            IFile file = libraryModule.getFileByPath(context, library, cheminModeleWord);
//            if (file == null) {
//                resourceController.alert("Le fichier de modele n'existe pas dans l'espace documentaire... Veuillez contacter votre administrateur");
//            }
//            GenerateWordFile generateWordFile = new GenerateWordFile();
//            IAttachment attachement = libraryModule.getAttachment(file, file.getName());
//            InputStream inputStream = generateWordFile.valorization(workflowModule, iResource, attachement);
//            Document document = new Document(inputStream);
//            document.save(cheminTempFolder + "\\" + titrePJOutput + ".docx", 40);
//            iResource.setValue(champPJName, null);
//            workflowModule.addAttachment((IResource) iResource, champPJName, new File(cheminTempFolder + "\\" + titrePJOutput + ".docx"));
//            iResource.save(champPJName);
//            File MyFile = new File(cheminTempFolder + "\\" + titrePJOutput +".docx");
//            MyFile.delete();
//        } catch (IOException | com.axemble.vdoc.sdk.exceptions.ModuleException | com.axemble.vdoc.sdk.exceptions.SDKException e) {
//
//            if (libraryModule.isTransactionActive()) {
//                libraryModule.rollbackTransaction();
//            }
//            String message = "Error in Aspose GenerationDuDocumentWord method : " + e.getClass() + " - " + e.getMessage();
//            Navigator.getNavigator().getRootNavigator().showAlertBox(message);
//        } catch (Exception e) {
//            String message = e.getMessage();
//            if (message == null) {
//                message = "";
//            }
//            e.printStackTrace();
//            log.error("Error in Aspose GenerationDuDocumentWord method : " + e.getClass() + " - " + message);
//        } finally {
//
//            if (!libraryModule.isTransactionActive()) {
//                Modules.releaseModule((IModule) libraryModule);
//            }
//            Modules.releaseModule((IModule) directoryModule);
//        }
//    }
//
//    public void GenerationDuDocumentPDF(IWorkflowModule workflowModule, IResource iResource, IResourceController resourceController, String cheminModeleWord, String champPJName, String titrePJOutput) {
//        ILibraryModule libraryModule = Modules.getLibraryModule();
//        IDirectoryModule directoryModule = Modules.getDirectoryModule();
//        try {
//            IContext context = libraryModule.getContextByLogin("sysadmin");
//            IOrganization organization = DirectoryService.getOrganization(directoryModule, workflowModule);
//            String libraryName = "Modeles";
//            String cheminTempFolder = "./Temp";
//            ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
//            if (library == null) {
//                resourceController.alert("Le chemin du fichier de modele n'existe pas dans l'espace documentaire... Veuillez contacter votre administrateur");
//            }
//            IFile file = libraryModule.getFileByPath(context, library, cheminModeleWord);
//            if (file == null) {
//                resourceController.alert("Le fichier de modele n'existe pas dans l'espace documentaire... Veuillez contacter votre administrateur");
//            }
//            GenerateWordFile generateWordFile = new GenerateWordFile();
//            IAttachment attachement = libraryModule.getAttachment(file, file.getName());
//            InputStream inputStream = generateWordFile.valorization(workflowModule, iResource, attachement);
//            Document document = new Document(inputStream);
//            document.save(cheminTempFolder + "\\" + titrePJOutput + ".pdf", 40);
//            iResource.setValue(champPJName, null);
//            workflowModule.addAttachment((IResource) iResource, champPJName, new File(cheminTempFolder + "\\" + titrePJOutput + ".pdf"));
//            iResource.save(champPJName);
//            File MyFile = new File(cheminTempFolder + "\\" + titrePJOutput +".pdf");
//            MyFile.delete();
//        } catch (IOException | com.axemble.vdoc.sdk.exceptions.ModuleException | com.axemble.vdoc.sdk.exceptions.SDKException e) {
//
//            if (libraryModule.isTransactionActive()) {
//                libraryModule.rollbackTransaction();
//            }
//            String message = "Error in Aspose GenerationDuDocumentWord method : " + e.getClass() + " - " + e.getMessage();
//            Navigator.getNavigator().getRootNavigator().showAlertBox(message);
//        } catch (Exception e) {
//            String message = e.getMessage();
//            if (message == null) {
//                message = "";
//            }
//            e.printStackTrace();
//            log.error("Error in Aspose GenerationDuDocumentWord method : " + e.getClass() + " - " + message);
//        } finally {
//
//            if (!libraryModule.isTransactionActive()) {
//                Modules.releaseModule((IModule) libraryModule);
//            }
//            Modules.releaseModule((IModule) directoryModule);
//        }
//    }

    public void GenerationDuDocumentWord(IWorkflowModule workflowModule, IResource iResource, IResourceController resourceController, String cheminModeleWord, String champPJName, String titrePJOutput) {
        ILibraryModule libraryModule = Modules.getLibraryModule();
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IOrganization organization = DirectoryService.getOrganization(directoryModule, workflowModule);
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
            document.save(cheminTempFolder + "\\" + getOutputFileName(context , titrePJOutput , iResource , attachement , "docx") , document.getOriginalLoadFormat());
            iResource.setValue(champPJName, null);
            workflowModule.addAttachment((IResource) iResource, champPJName, new File(cheminTempFolder + "\\" + getOutputFileName(context , titrePJOutput , iResource , attachement , "docx")));
            iResource.save(champPJName);
            File MyFile = new File(cheminTempFolder + "\\" + getOutputFileName(context , titrePJOutput , iResource , attachement , "docx"));
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

    public void GenerationDuDocumentPDF(IWorkflowModule workflowModule, IResource iResource, IResourceController resourceController, String cheminModeleWord, String champPJName, String titrePJOutput) {
        ILibraryModule libraryModule = Modules.getLibraryModule();
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IOrganization organization = DirectoryService.getOrganization(directoryModule, workflowModule);
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
            InputStream inputStream = generateWordFile.valorization(workflowModule, iResource, attachement , true);
            Document document = new Document(inputStream);
            document.save(cheminTempFolder + "\\" + getOutputFileName(context , titrePJOutput , iResource , attachement , "pdf"), 40);
            iResource.setValue(champPJName, null);
            workflowModule.addAttachment((IResource) iResource, champPJName, new File(cheminTempFolder + "\\" + getOutputFileName(context , titrePJOutput , iResource , attachement , "pdf")));
            iResource.save(champPJName);
            File MyFile = new File(cheminTempFolder + "\\" + getOutputFileName(context , titrePJOutput , iResource , attachement , "pdf"));
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

    private String getOutputFileName(IContext userContext, String titrePJOutput, IResource resource, IAttachment iAttachment, String replaceExtension) {
        String outputFileName = titrePJOutput;
        if (StringUtils.isEmpty(titrePJOutput)) {
            outputFileName = iAttachment.getName();
        } else {
            outputFileName = ExpressionUtils.evaluate(outputFileName, userContext, resource, null);
        }
        if (StringUtils.isNotEmpty(replaceExtension))
            if (outputFileName.contains(".")) {
                outputFileName = outputFileName.replace(outputFileName.subSequence(outputFileName.lastIndexOf("."), outputFileName.length()), "." + replaceExtension);
            } else {
                outputFileName = outputFileName + "." + replaceExtension;
            }
        return outputFileName;
    }
}