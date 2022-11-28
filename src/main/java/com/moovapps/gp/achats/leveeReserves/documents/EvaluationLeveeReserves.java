package com.moovapps.gp.achats.leveeReserves.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.achats.depotDossier.views.TabDossierAdministratif;
import com.moovapps.gp.achats.depotDossier.views.TabOffreTechnique;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.Collection;

public class EvaluationLeveeReserves extends BaseDocumentExtension {
    protected IContext context = DirectoryService.getSysAdminContext();

    public boolean onBeforeSubmit(IAction action) {
        try {
            String appelDOffre = (String)getWorkflowInstance().getValue("AppelDOffresEnCours");
            String candidat = (String)getWorkflowInstance().getValue("Candidat");
            String lot = (String)getWorkflowInstance().getValue("Lot");
            String[] tab = appelDOffre.split("\\/");
            IWorkflowInstance workflowInstanceDepot = getWorkflowInstanceDepot(tab[0], lot, candidat);
            if (workflowInstanceDepot != null) {
                MAJLinkedResourceReserves(workflowInstanceDepot);
                TabDossierAdministratif.MAJDecisionCommission(workflowInstanceDepot);
                TabOffreTechnique.MAJDecisionCommission(workflowInstanceDepot);
            } else {
                getResourceController().alert("L'offre déposée est introuvable !");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onBeforeSubmit(action);
    }

    private IWorkflowInstance getWorkflowInstanceDepot(String referenceAO, String lot, String candidat) {
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.context);
            viewController.addEqualsConstraint("ReferenceDeLAO", referenceAO);
            viewController.addEqualsConstraint("Lot", lot);
            viewController.addEqualsConstraint("Candidat", candidat);
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Achats", "DepotDesDossiers"));
            if (workflowInstances != null && !workflowInstances.isEmpty())
                return workflowInstances.iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void MAJLinkedResourceReserves(IWorkflowInstance workflowInstanceDepot) {
        try {
            Collection<ILinkedResource> linkedResourcesCibles = null;
            Collection<IAttachment> attachments = null;
            Collection<ILinkedResource> linkedResourcesSources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("DA_Depot_Tab");
            for (ILinkedResource linkedResourceSource : linkedResourcesSources) {
                linkedResourcesCibles = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("DA_Depot_Tab");
                for (ILinkedResource linkedResourceCible : linkedResourcesCibles) {
                    if (linkedResourceSource.getValue("Piece").equals(linkedResourceCible.getValue("Piece"))) {
                        linkedResourceCible.setValue("PieceFournie", linkedResourceSource.getValue("PieceFournie"));
                        linkedResourceCible.setValue("ConformiteDeLaPiece", linkedResourceSource.getValue("ConformiteDeLaPiece"));
                        linkedResourceCible.setValue("PieceSJointeS", null);
                        attachments = (Collection<IAttachment>) getWorkflowModule().getAttachments((IResource)linkedResourceSource, "PieceSJointeS");
                        if (attachments != null && !attachments.isEmpty())
                            for (IAttachment iattachment : attachments)
                                getWorkflowModule().addAttachment((IResource)linkedResourceCible, "PieceSJointeS", iattachment);
                        linkedResourceCible.setValue("Commentaire", linkedResourceSource.getValue("Commentaire"));
                        linkedResourceCible.save(this.context);
                    }
                }
            }
            linkedResourcesSources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("DT_Depot_Tab");
            for (ILinkedResource linkedResourceSource : linkedResourcesSources) {
                linkedResourcesCibles = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("DT_Depot_Tab");
                for (ILinkedResource linkedResourceCible : linkedResourcesCibles) {
                    if (linkedResourceSource.getValue("Piece").equals(linkedResourceCible.getValue("Piece"))) {
                        linkedResourceCible.setValue("PieceFournie", linkedResourceSource.getValue("PieceFournie"));
                        linkedResourceCible.setValue("ConformiteDeLaPiece", linkedResourceSource.getValue("ConformiteDeLaPiece"));
                        linkedResourceCible.setValue("PieceSJointeS", null);
                        attachments = (Collection<IAttachment>) getWorkflowModule().getAttachments((IResource)linkedResourceSource, "PieceSJointeS");
                        if (attachments != null && !attachments.isEmpty())
                            for (IAttachment iattachment : attachments)
                                getWorkflowModule().addAttachment((IResource)linkedResourceCible, "PieceSJointeS", iattachment);
                        linkedResourceCible.setValue("Commentaire", linkedResourceSource.getValue("Commentaire"));
                        linkedResourceCible.save(this.context);
                    }
                }
            }
            linkedResourcesSources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("OT_Depot_Tab");
            for (ILinkedResource linkedResourceSource : linkedResourcesSources) {
                linkedResourcesCibles = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("OT_Depot_Tab");
                for (ILinkedResource linkedResourceCible : linkedResourcesCibles) {
                    if (linkedResourceSource.getValue("Piece").equals(linkedResourceCible.getValue("Piece"))) {
                        linkedResourceCible.setValue("PieceFournie", linkedResourceSource.getValue("PieceFournie"));
                        linkedResourceCible.setValue("ConformiteDeLaPiece", linkedResourceSource.getValue("ConformiteDeLaPiece"));
                        linkedResourceCible.setValue("PieceSJointeS", null);
                        attachments = (Collection<IAttachment>) getWorkflowModule().getAttachments((IResource)linkedResourceSource, "PieceSJointeS");
                        if (attachments != null && !attachments.isEmpty())
                            for (IAttachment iattachment : attachments)
                                getWorkflowModule().addAttachment((IResource)linkedResourceCible, "PieceSJointeS", iattachment);
                        linkedResourceCible.setValue("Commentaire", linkedResourceSource.getValue("Commentaire"));
                        linkedResourceCible.save(this.context);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

