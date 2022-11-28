package com.moovapps.gp.marche.document;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import java.util.ArrayList;
import java.util.Collection;
public class EtablissementFicheMarche extends BaseDocumentExtension {
    private static final long serialVersionUID = 8760885001689442102L;

    private IWorkflowInstance iWorkflowInstancePERE = null;

    private ArrayList<IOptionList.IOption> lots = new ArrayList<>();

    private String nomDuLot = null;

    public boolean onAfterLoad() {
        try {
            getResourceController().setThrowEvents("Lot", true);
            if (getWorkflowInstance().getParentInstance() != null)
                setListDesLots();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            if (property.getName().equals("Lot") && getWorkflowInstance().getValue("Lot") != null) {
                setCandidat();
                setBordereauDePrix();
                setDelais();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    private void setListDesLots() {
        try {
            this.iWorkflowInstancePERE = getWorkflowInstance().getParentInstance();
            Collection<ILinkedResource> iLinkedResources = (Collection<ILinkedResource>) this.iWorkflowInstancePERE.getLinkedResources("Lots_AO_Tab");
            for (ILinkedResource iLinkedResource : iLinkedResources) {
                if (iLinkedResource.getValue("Code") != null && iLinkedResource.getValue("Intitule") != null) {
                    this.nomDuLot = iLinkedResource.getValue("Code") + " - " + iLinkedResource.getValue("Intitule");
                    this.lots.add(getWorkflowModule().createListOption(this.nomDuLot, this.nomDuLot));
                }
            }
            getWorkflowInstance().setList("Lot", this.lots);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCandidat() {
        try {
            this.nomDuLot = (String)getWorkflowInstance().getValue("Lot");
            Collection<IWorkflowInstance> iWorkflowInstances = (Collection<IWorkflowInstance>) this.iWorkflowInstancePERE.getLinkedWorkflowInstances("DepotDossiers_SP");
            for (IWorkflowInstance iWorkflowInstance : iWorkflowInstances) {
                if (this.nomDuLot.equals(iWorkflowInstance.getValue("Lot")) && iWorkflowInstance.getValue("DecisionDeLaCommissionAdjudication").equals("Adjudicataire")) {
                    getWorkflowInstance().setValue("Candidat", iWorkflowInstance.getValue("Candidat"));
                    getWorkflowInstance().setValue("TotalTTC", iWorkflowInstance.getValue("MontantDeLActeDEngagement"));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBordereauDePrix() {
        try {
            Collection<ILinkedResource> iLinkedResources = (Collection<ILinkedResource>) this.iWorkflowInstancePERE.getLinkedResources("BP_AO_Tab");
            for (ILinkedResource iLinkedResource : iLinkedResources) {
                if (this.nomDuLot.equals(iLinkedResource.getValue("Code") + " - " + iLinkedResource.getValue("Intitule"))) {
                    Collection<IAttachment> iAttachments = (Collection<IAttachment>) getWorkflowModule().getAttachments((IResource)iLinkedResource, "BordereauDesPrix");
                    getWorkflowInstance().setValue("BordereauDesPrix", iAttachments);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDelais() {
        try {
            Collection<ILinkedResource> iLinkedResources = (Collection<ILinkedResource>) this.iWorkflowInstancePERE.getLinkedResources("Modalites_AO_Tab");
            for (ILinkedResource iLinkedResource : iLinkedResources) {
                if (this.nomDuLot.equals(iLinkedResource.getValue("Code") + " - " + iLinkedResource.getValue("Intitule"))) {
                    if (iLinkedResource.getValue("DelaiDExecution") != null && iLinkedResource.getValue("Execution") != null) {
                        getWorkflowInstance().setValue("DelaiDExecutionGlobal", iLinkedResource.getValue("DelaiDExecution"));
                        getWorkflowInstance().setValue("Delai", iLinkedResource.getValue("Execution"));
                    }
                    if (iLinkedResource.getValue("DelaiDeGarantie") != null && iLinkedResource.getValue("Garantie") != null) {
                        getWorkflowInstance().setValue("DelaiDeGarantie", iLinkedResource.getValue("DelaiDeGarantie"));
                        getWorkflowInstance().setValue("DelaiGarantie", iLinkedResource.getValue("Garantie"));
                    }
                    if (iLinkedResource.getValue("RetenueDeGarantie") != null)
                        getWorkflowInstance().setValue("TauxRetenueDeGarantie", iLinkedResource.getValue("RetenueDeGarantie"));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
