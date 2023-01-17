package com.moovapps.gp.achats.ao.tableaux;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import java.util.Collection;

public class TabLots extends BaseDocumentExtension {
    protected IContext context = DirectoryService.getSysAdminContext();

    public boolean onBeforeSave() {
        try {
            MAJBordereauDesPrixEtModalites(getWorkflowInstance().getParentInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSave();
    }

    private void MAJBordereauDesPrixEtModalites(IWorkflowInstance workflowInstance) {
        try {
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("Lots_AO_Tab");
            if (linkedResourcesLots == null || linkedResourcesLots.isEmpty()) {
                workflowInstance.setValue("BP_AO_Tab", null);
                workflowInstance.setValue("Modalites_AO_Tab", null);
            }
            Collection<ILinkedResource> linkedResourcesBPX = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("BP_AO_Tab");
            int cptLot = 1, cptBP = 1, cptModalites = 1;
            boolean trouve = false;
            ILinkedResource newLinkedResource = null;
            for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                trouve = false;
                if (linkedResourcesBPX != null && !linkedResourcesBPX.isEmpty()) {
                    cptBP = 1;
                    for (ILinkedResource linkedResourceBP : linkedResourcesBPX) {
                        if (cptLot == cptBP) {
                            MAJLigneTableau(linkedResourceBP, linkedResourceLot);
                            trouve = true;
                            break;
                        }
                        cptBP++;
                    }
                }
                if (!trouve) {
                    newLinkedResource = workflowInstance.createLinkedResource("BP_AO_Tab");
                    MAJLigneTableau(newLinkedResource, linkedResourceLot);
                    workflowInstance.addLinkedResource(newLinkedResource);
                }
                Collection<ILinkedResource> linkedResourcesModalites = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("Modalites_AO_Tab");
                trouve = false;
                if (linkedResourcesModalites != null && !linkedResourcesModalites.isEmpty()) {
                    cptModalites = 1;
                    for (ILinkedResource linkedResourceModalite : linkedResourcesModalites) {
                        if (cptLot == cptModalites) {
                            MAJLigneTableau(linkedResourceModalite, linkedResourceLot);
                            trouve = true;
                            break;
                        }
                        cptModalites++;
                    }
                }
                if (!trouve) {
                    newLinkedResource = workflowInstance.createLinkedResource("Modalites_AO_Tab");
                    MAJLigneTableau(newLinkedResource, linkedResourceLot);
                    workflowInstance.addLinkedResource(newLinkedResource);
                }
                cptLot++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void MAJLigneTableau(ILinkedResource linkedResourceDestination, ILinkedResource linkedResourceSource) {
        try {
            linkedResourceDestination.setValue("Code", linkedResourceSource.getValue("Code"));
            linkedResourceDestination.setValue("Intitule", linkedResourceSource.getValue("Intitule"));
            linkedResourceDestination.save(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
