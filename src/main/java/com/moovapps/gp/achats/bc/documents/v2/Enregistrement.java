package com.moovapps.gp.achats.bc.documents.v2;

import com.aspose.cells.SaveFormat;
import com.aspose.pdf.Document;
import com.aspose.slides.exceptions.IOException;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.exceptions.SDKException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Logger;

public class Enregistrement extends BaseDocumentExtension {

    public boolean onAfterLoad() {
        IContext sysAdminContext = getDirectoryModule().getSysadminContext();
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("BordereauDePrix_BC_Tab");
            if (linkedResources == null || linkedResources.isEmpty()) {
                IWorkflowInstance workflowInstanceConsultation = getWorkflowInstance().getParentInstance();
                if (workflowInstanceConsultation != null)
                    MAJBordereauDePrix(workflowInstanceConsultation,sysAdminContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Envoyer"))
        {
            try
            {
//                IContext contextCreator = null;
//                Collection<IWorkflowInstance> collection = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Engagement");
//                for (IWorkflowInstance iWorkflowInstance : collection)
//                {
//                    contextCreator = getWorkflowModule().getContext((IUser) iWorkflowInstance.getValue(IProperty.System.CREATOR));
//                    ITaskInstance instance = iWorkflowInstance.getCurrentTaskInstance(contextCreator);
//                    ITask task = instance.getTask();
//                    IAction iAction = task.getAction("Envoyer");
//                    getWorkflowModule().end(contextCreator, instance, iAction, "");
//                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return super.onBeforeSubmit(action);
    }

    private void MAJBordereauDePrix(IWorkflowInstance workflowInstanceConsultation, IContext context) {
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)workflowInstanceConsultation.getValue("BordereauDePrix_Tab");
            ILinkedResource newLinkedResource = null;
            Collection<IProperty> properties = null;
            for (ILinkedResource linkedResource : linkedResources) {
                newLinkedResource = getWorkflowInstance().createLinkedResource("BordereauDePrix_BC_Tab");
                newLinkedResource.setValue("FamilleDArticles", linkedResource.getValue("FamilleDArticles"));
                newLinkedResource.setValue("SousFamilleDArticles", linkedResource.getValue("SousFamilleDArticles"));
                newLinkedResource.setValue("Article", linkedResource.getValue("Article"));
                newLinkedResource.setValue("Quantite", linkedResource.getValue("Quantite"));
                newLinkedResource.setValue("QuantiteLivree", linkedResource.getValue("QuantiteLivree"));
                newLinkedResource.setValue("ResteALivrer", linkedResource.getValue("ResteALivrer"));
                newLinkedResource.setValue("PrixUnitaire", linkedResource.getValue("PrixUnitaire"));
                newLinkedResource.setValue("TVA", linkedResource.getValue("TVA"));
                newLinkedResource.setValue("Commentaire", linkedResource.getValue("Commentaire"));

                newLinkedResource.save(context);
                getWorkflowInstance().addLinkedResource(newLinkedResource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
