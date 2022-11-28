package com.moovapps.gp.budget.helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class copyWorkflow extends BaseDocumentExtension {


    public static IWorkflowInstance duplicateWorkflowInstance(IWorkflowInstance workflowInstanceSource, IContext context) throws WorkflowModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        IWorkflowInstance workflowInstanceCible = null;
        try {
            workflowInstanceCible = workflowModule.createWorkflowInstance(context, workflowInstanceSource.getWorkflow(), null);
            Collection<? extends IProperty> allFields = workflowInstanceSource.getDefinition().getProperties();
            for (IProperty iProperty : allFields) {
                if (!iProperty.getName().startsWith("sys_") && !iProperty.getName().contains("State") && !iProperty.getName().equals("URIBudgetV1") && !iProperty.getName().contains("Commentaire")) {
                    Collection<IAttachment> attachments;
                    String str;
                    switch ((str = iProperty.getDisplaySettings().getType()).hashCode()) {
                        case -1914417805:
                            if (!str.equals("file_multiple"))
                                break;
                            attachments = (Collection<IAttachment>) workflowModule.getAttachments((IResource)workflowInstanceSource, iProperty.getName());
                            for (IAttachment iattachment : attachments)
                                workflowModule.addAttachment((IResource)workflowInstanceCible, iProperty.getName(), iattachment);
                            continue;
                        case 998428800:
                            if (!str.equals("resourcetable"))
                                break;
                            createTableSousLines(workflowInstanceSource, workflowInstanceCible, iProperty);
                            continue;
                    }
                    workflowInstanceCible.setValue(iProperty.getName(), workflowInstanceSource.getValue(iProperty.getName()));
                }
            }
            workflowInstanceCible.save(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workflowInstanceCible;
    }


    public void SaveLinkedResource(ILinkedResource iLinkedResourceInput , IResource iResourceOutput , Map<String,String> Params)
    {
        try
        {
            for (Map.Entry<String, String> entry : Params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if(key.startsWith("_"))
                {
                    String newKey = key.replaceFirst("_", "");
                    if(iLinkedResourceInput.getDefinition().getProperty(newKey).getDisplaySettings().getType().equals("file_multiple"))
                    {
                        Collection<IAttachment> attachments = (Collection<IAttachment>)iResourceOutput.getValue(value);
                        if (attachments != null && !attachments.isEmpty())
                            for (IAttachment iattachment : attachments){
                                Modules.getWorkflowModule().addAttachment((IResource)iLinkedResourceInput, newKey, iattachment);
                            }
                    }
                    else{
                        Method method = this.getClass().getMethod(key,IResource.class , String.class);
                        Object obj = method.invoke(this,iResourceOutput, value); //
                        iLinkedResourceInput.setValue(newKey, obj);
                    }
                }
                else{
                    iLinkedResourceInput.setValue(key, iResourceOutput.getValue(value));
                }
            }
            iLinkedResourceInput.save(Modules.getWorkflowModule().getLoggedOnUserContext());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void createTableSousLines(IWorkflowInstance workflowInstanceSource, IWorkflowInstance workflowInstanceCible, IProperty iProperty) throws WorkflowModuleException {
        IWorkflowModule workflowModule = Modules.getWorkflowModule();
        try {
            Collection<ILinkedResource> collectionLinkedResourcesSource = (Collection<ILinkedResource>) workflowInstanceSource.getLinkedResources(iProperty.getName());
            for (ILinkedResource linkedResourceSource : collectionLinkedResourcesSource) {
                ILinkedResource linkedResourceCible = workflowInstanceCible.createLinkedResource(iProperty.getName());
                Collection<? extends IProperty> allLinkedResourceFields = linkedResourceSource.getDefinition().getProperties();
                for (IProperty iPropertyLinkedResource : allLinkedResourceFields) {
                    if (!iPropertyLinkedResource.getName().startsWith("sys_")) {
                        if (iPropertyLinkedResource.getDisplaySettings().getType().equals("file_multiple")) {
                            Collection<IAttachment> attachments = (Collection<IAttachment>) workflowModule.getAttachments((IResource)workflowInstanceSource, iPropertyLinkedResource.getName());
                            for (IAttachment iattachment : attachments)
                                workflowModule.addAttachment((IResource)workflowInstanceCible, iPropertyLinkedResource.getName(), iattachment);
                            continue;
                        }
                        linkedResourceCible.setValue(iPropertyLinkedResource.getName(), linkedResourceSource.getValue(iPropertyLinkedResource.getName()));
                    }
                }
                workflowInstanceCible.addLinkedResource(linkedResourceCible);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
