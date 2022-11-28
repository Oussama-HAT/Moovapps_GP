package com.moovapps.gp.achats.leveeReserves.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.gp.services.DirectoryService;
import com.moovapps.gp.services.WorkflowsService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DemandeLeveeReserves extends BaseDocumentExtension {
    protected IContext context = DirectoryService.getSysAdminContext();

    public boolean onAfterLoad() {
        try {
            String appelDOffre = (String)getWorkflowInstance().getValue("AppelDOffresEnCours");
            if (appelDOffre != null) {
                String[] tab = appelDOffre.split("\\/");
                getWorkflowInstance().setList("Lot", getListLots(tab[0], tab[1]));
            } else {
                getWorkflowInstance().setList("Lot", null);
                getWorkflowInstance().setValue("Lot", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            if (property.getName().equals("AppelDOffresEnCours")) {
                getWorkflowInstance().setValue("Lot", null);
                String appelDOffre = (String)getWorkflowInstance().getValue("AppelDOffresEnCours");
                if (appelDOffre != null) {
                    String[] tab = appelDOffre.split("\\/");
                    getWorkflowInstance().setList("Lot", getListLots(tab[0], tab[1]));
                } else {
                    getWorkflowInstance().setList("Lot", null);
                    getWorkflowInstance().setValue("Lot", null);
                }
            } else if (property.getName().equals("Lot") || property.getName().equals("Candidat")) {
                getWorkflowInstance().setValue("DA_Depot_Tab", null);
                getWorkflowInstance().setValue("DT_Depot_Tab", null);
                getWorkflowInstance().setValue("OT_Depot_Tab", null);
                String appelDOffre = (String)getWorkflowInstance().getValue("AppelDOffresEnCours");
                String candidat = (String)getWorkflowInstance().getValue("Candidat");
                String lot = (String)getWorkflowInstance().getValue("Lot");
                if (appelDOffre != null && candidat != null && lot != null) {
                    String[] tab = appelDOffre.split("\\/");
                    IWorkflowInstance workflowInstanceDepot = getWorkflowInstanceDepot(tab[0], lot, candidat);
                    if (workflowInstanceDepot != null) {
                        getReservesCandidat(workflowInstanceDepot);
                        getWorkflowInstance().setValue("CommissionUsers", workflowInstanceDepot.getValue("CommissionUsers"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    private IWorkflowInstance getWorkflowInstanceAO(String referenceAO, String objetAO) {
        try {
            IViewController viewController = getWorkflowModule().getViewController(this.context);
            viewController.addEqualsConstraint("ReferenceDeLAO", referenceAO);
            viewController.addEqualsConstraint("ObjetDeLAO", objetAO);
            Collection<IWorkflowInstance> workflowInstances = viewController.evaluate(WorkflowsService.getWorflowContainer("Achats", "AO"));
            if (workflowInstances != null && !workflowInstances.isEmpty())
                return workflowInstances.iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<IOptionList.IOption> getListLots(String referenceAO, String objetAO) {
        List<IOptionList.IOption> options = new ArrayList<>();
        try {
            IWorkflowInstance workflowInstanceAO = getWorkflowInstanceAO(referenceAO, objetAO);
            Collection<ILinkedResource> linkedResourcesLots = (Collection<ILinkedResource>) workflowInstanceAO.getLinkedResources("Lots_AO_Tab");
            String code = null, intitule = null;
            for (ILinkedResource linkedResourceLot : linkedResourcesLots) {
                code = (String)linkedResourceLot.getValue("Code");
                intitule = (String)linkedResourceLot.getValue("Intitule");
                if (code != null && intitule != null)
                    options.add(getWorkflowModule().createListOption(String.valueOf(code) + " - " + intitule, String.valueOf(code) + " - " + intitule));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return options;
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

    private IWorkflowInstance getReservesCandidat(IWorkflowInstance workflowInstanceDepot) {
        try {
            Collection<ILinkedResource> linkedResourcesDA = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("DA_Depot_Tab");
            String conformitePiece = null;
            for (ILinkedResource linkedResourceDA : linkedResourcesDA) {
                conformitePiece = (String)linkedResourceDA.getValue("ConformiteDeLaPiece");
                if (conformitePiece != null && conformitePiece.equals("Acceptée avec réserve"))
                        createLinkedResource(linkedResourceDA, "DA_Depot_Tab");
            }
            Collection<ILinkedResource> linkedResourcesDT = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("DT_Depot_Tab");
            for (ILinkedResource linkedResourceDT : linkedResourcesDT) {
                conformitePiece = (String)linkedResourceDT.getValue("ConformiteDeLaPiece");
                if (conformitePiece != null && conformitePiece.equals("Acceptée avec réserve"))
                        createLinkedResource(linkedResourceDT, "DT_Depot_Tab");
            }
            Collection<ILinkedResource> linkedResourcesOT = (Collection<ILinkedResource>) workflowInstanceDepot.getLinkedResources("OT_Depot_Tab");
            for (ILinkedResource linkedResourceOT : linkedResourcesOT) {
                conformitePiece = (String)linkedResourceOT.getValue("ConformiteDeLaPiece");
                if (conformitePiece != null && conformitePiece.equals("Acceptée avec réserve"))
                        createLinkedResource(linkedResourceOT, "OT_Depot_Tab");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createLinkedResource(ILinkedResource linkedResourceSource, String property) {
        try {
            ILinkedResource linkedResourceCible = getWorkflowInstance().createLinkedResource(property);
            linkedResourceCible.setValue("Piece", linkedResourceSource.getValue("Piece"));
            linkedResourceCible.setValue("PieceFournie", linkedResourceSource.getValue("PieceFournie"));
            Collection<IAttachment> attachments = (Collection<IAttachment>) getWorkflowModule().getAttachments((IResource)linkedResourceSource, "PieceSJointeS");
            if (attachments != null && !attachments.isEmpty())
                for (IAttachment iattachment : attachments)
                    getWorkflowModule().addAttachment((IResource)linkedResourceCible, "PieceSJointeS", iattachment);
            linkedResourceCible.setValue("ConformiteDeLaPiece", linkedResourceSource.getValue("ConformiteDeLaPiece"));
            linkedResourceCible.setValue("Commentaire", linkedResourceSource.getValue("Commentaire"));
            linkedResourceCible.save(this.context);
            getWorkflowInstance().addLinkedResource(linkedResourceCible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
