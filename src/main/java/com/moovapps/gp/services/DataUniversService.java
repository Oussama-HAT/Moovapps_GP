package com.moovapps.gp.services;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import java.util.Collection;

public class DataUniversService extends BaseDocumentExtension {
    public static final String projectName = "ADMINISTRATIONGP";

    public static IProject getProject() throws ProjectModuleException, DirectoryModuleException {
        IProjectModule projectModule = Modules.getProjectModule();
        try {
            return projectModule.getProject(DirectoryService.getSysAdminContext(), "ADMINISTRATIONGP", DirectoryService.getDefaultOrganization());
        } finally {
            Modules.releaseModule((IModule)projectModule);
        }
    }

    public static ICatalog getCatalog(String catalogName) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            return workflowModule.getCatalog(DirectoryService.getSysAdminContext(), catalogName, 4, getProject());
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
    }

    public static IResourceDefinition getResourceDefinition(String catalogName, String resourceDefinitionName) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            return workflowModule.getResourceDefinition(DirectoryService.getSysAdminContext(), getCatalog(catalogName), resourceDefinitionName);
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
    }

    public static IStorageResource getStorageResource(String titre, IResourceDefinition resourceDefinition) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            IViewController viewController = workflowModule.getViewController(DirectoryService.getSysAdminContext(), IResource.class);
            viewController.addEqualsConstraint("sys_Title", titre);
            Collection<IStorageResource> storageResources = viewController.evaluate(resourceDefinition);
            if (!storageResources.isEmpty())
                return storageResources.iterator().next();
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
        return null;
    }



}

