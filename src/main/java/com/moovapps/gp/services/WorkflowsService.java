package com.moovapps.gp.services;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowContainer;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import java.util.Collection;

public class WorkflowsService extends BaseDocumentExtension {
    public static final String projectName = "GP";

    public static IProject getProject() throws ProjectModuleException, DirectoryModuleException {
        IProjectModule projectModule = Modules.getProjectModule();
        try {
            return projectModule.getProject(DirectoryService.getSysAdminContext(), "GP", DirectoryService.getDefaultOrganization());
        } finally {
            Modules.releaseModule((IModule)projectModule);
        }
    }

    public static ICatalog getCatalog(String catalogName) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            return workflowModule.getCatalog(DirectoryService.getSysAdminContext(), catalogName, getProject());
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
    }

    public static IWorkflowContainer getWorflowContainer(String catalogName, String workflowContainerName) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            return workflowModule.getWorkflowContainer(DirectoryService.getSysAdminContext(), getCatalog(catalogName), workflowContainerName);
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
    }

    public static IWorkflow getWorflow(String catalogName, String workflowName) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            return workflowModule.getWorkflow(DirectoryService.getSysAdminContext(), getCatalog(catalogName), workflowName);
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
    }

    public static IWorkflowInstance getWorflowInstance(IWorkflowContainer workflowContainer, String id) throws WorkflowModuleException, ProjectModuleException, DirectoryModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            IViewController viewController = workflowModule.getViewController(DirectoryService.getSysAdminContext());
            viewController.addEqualsConstraint("sys_Reference", id);
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(workflowContainer);
            if (workflowInstances != null && !workflowInstances.isEmpty())
                return workflowInstances.iterator().next();
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
        return null;
    }

    public static void executeAction(IWorkflowInstance workflowInstance, IContext contextIntervenant, String actionName, String commentaire) {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            ITaskInstance taskInstance = workflowInstance.getCurrentTaskInstance(contextIntervenant);
            if (taskInstance != null) {
                IAction actionAExecuter = taskInstance.getTask().getAction(actionName);
                if (actionAExecuter != null) {
                    workflowModule.end(contextIntervenant, taskInstance, actionAExecuter, commentaire);
                    workflowInstance.save(contextIntervenant);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Modules.releaseModule((IModule)workflowModule);
        }
    }

    public static IWorkflowInstance duplicateWorkflowInstance(IWorkflowInstance workflowInstanceSource, IContext context) throws WorkflowModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        IWorkflowInstance workflowInstanceCible = null;
        try {
            workflowInstanceCible = workflowModule.createWorkflowInstance(context, workflowInstanceSource.getWorkflow(), null);
            Collection<? extends IProperty> allFields = workflowInstanceSource.getDefinition().getProperties();
            int i = 1;
            for (IProperty iProperty : allFields) {
                System.out.println(i+ "    ---------  "+iProperty.getName());
                if (!iProperty.getName().startsWith("sys_") && !iProperty.getName().contains("State") && !iProperty.getName().equals("URIBudgetV1") && !iProperty.getName().contains("Commentaire")) {
                    Collection<IAttachment> attachments;
                    String str = iProperty.getDisplaySettings().getType();
                    if(str.equals("file_multiple")){
                        attachments = (Collection<IAttachment>) workflowModule.getAttachments((IResource)workflowInstanceSource, iProperty.getName());
                        if(attachments!=null && !attachments.isEmpty()) {
                            for (IAttachment iattachment : attachments) {
                                workflowModule.addAttachment((IResource) workflowInstanceCible, iProperty.getName(), iattachment);
                            }
                        }
                        continue;
                    }
                    else if(str.equals("resourcetable")){
                        createTableSousLines(workflowInstanceSource, workflowInstanceCible, iProperty);
                        continue;
                    }
                    workflowInstanceCible.setValue(iProperty.getName(), workflowInstanceSource.getValue(iProperty.getName()));
                }
                i++;
            }
            workflowInstanceCible.save(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowInstanceCible;
    }

    public static void createTableSousLines(IWorkflowInstance workflowInstanceSource, IWorkflowInstance workflowInstanceCible, IProperty iProperty) throws WorkflowModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            Collection<ILinkedResource> collectionLinkedResourcesSource = (Collection<ILinkedResource>) workflowInstanceSource.getLinkedResources(iProperty.getName());
            if(collectionLinkedResourcesSource!=null && !collectionLinkedResourcesSource.isEmpty()){
                for (ILinkedResource linkedResourceSource : collectionLinkedResourcesSource) {
                    ILinkedResource linkedResourceCible = workflowInstanceCible.createLinkedResource(iProperty.getName());
                    Collection<? extends IProperty> allLinkedResourceFields = linkedResourceSource.getDefinition().getProperties();
                    int i=0;
                    for (IProperty iPropertyLinkedResource : allLinkedResourceFields) {
                        System.out.println(i+ "  linkedResourceCible ---------  "+iPropertyLinkedResource.getName());
                        if (!iPropertyLinkedResource.getName().startsWith("sys_")) {
                            if (iPropertyLinkedResource.getDisplaySettings().getType().equals("file_multiple")) {
                                Collection<IAttachment> attachments = (Collection<IAttachment>) workflowModule.getAttachments((IResource)workflowInstanceSource, iPropertyLinkedResource.getName());
                                for (IAttachment iattachment : attachments)
                                    workflowModule.addAttachment((IResource)workflowInstanceCible, iPropertyLinkedResource.getName(), iattachment);
                                continue;
                            }
                            linkedResourceCible.setValue(iPropertyLinkedResource.getName(), linkedResourceSource.getValue(iPropertyLinkedResource.getName()));
                        }
                        i++;
                    }
                    workflowInstanceCible.addLinkedResource(linkedResourceCible);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateVersion(IWorkflowInstance workflowInstanceSource) {
        try {
            String versionSource = (String)workflowInstanceSource.getValue("VersionDuBudget");
            if (versionSource != null) {
                Integer version = Integer.valueOf(versionSource);
                version = Integer.valueOf(version.intValue() + 1);
                if (version.intValue() < 10)
                    return String.valueOf("0" + version);
                return String.valueOf(version);
            }
            return String.valueOf("01");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

