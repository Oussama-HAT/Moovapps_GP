package com.moovapps.gp.achats.fournisseur.documents;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.gp.services.DirectoryService;
import java.util.Collection;

public class EvaluationOffre extends BaseDocumentExtension {
    protected IContext sysAdminContext = DirectoryService.getSysAdminContext();

    public boolean onAfterLoad() {
        try {
            MAJCommissionEvaluation();
            masquerBottomButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }

    public void onPropertyChanged(IProperty property) {
        try {
            if (property.getName().equals("EvaluationDeLOffre_Tab"))
                masquerBottomButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPropertyChanged(property);
    }

    private void MAJMonEvaluation() {
        try {
            getResourceController().showBodyBlock("MonEvaluaion", false);
            getResourceController().showBodyBlock("DecisionFinale", false);
            IUser loggedOnUser = getWorkflowModule().getLoggedOnUser();
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationDeLOffre_Tab");
            IUser membre = null;
            String qualite = null;
            for (ILinkedResource linkedResource : linkedResources) {
                membre = (IUser)linkedResource.getValue("NomPrenom");
                qualite = (String)linkedResource.getValue("Qualite");
                if (membre != null && membre.equals(loggedOnUser)) {
                    getWorkflowInstance().setValue("DateDeLEvaluationMembre", linkedResource.getValue("DateDeLEvaluation"));
                    getWorkflowInstance().setValue("AdjudicataireMembre", linkedResource.getValue("Adjudicataire"));
                    getWorkflowInstance().setValue("CommentaireMembre", linkedResource.getValue("Commentaire"));
                    getResourceController().showBodyBlock("MonEvaluaion", true);
                    if (qualite != null && qualite.equals("Président"))
                            getResourceController().showBodyBlock("DecisionFinale", true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void MAJCommissionEvaluation() {
        try {
            Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("EvaluationDeLOffre_Tab");
            if (linkedResources == null || linkedResources.isEmpty()) {
                linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("Commission_Tab");
                ILinkedResource newLinkedResource = null;
                Collection<IProperty> properties = null;
                for (ILinkedResource linkedResource : linkedResources) {
                    properties = (Collection<IProperty>) linkedResource.getDefinition().getProperties();
                    newLinkedResource = getWorkflowInstance().createLinkedResource("EvaluationDeLOffre_Tab");
                    for (IProperty property : properties) {
                        if (!property.getName().startsWith("sys_") && !property.getName().equals("Commentaire"))
                            newLinkedResource.setValue(property.getName(), linkedResource.getValue(property.getName()));
                    }
                    newLinkedResource.save(this.sysAdminContext);
                    getWorkflowInstance().addLinkedResource(newLinkedResource);
                }
            }
            getWorkflowInstance().save(this.sysAdminContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void masquerBottomButton() {
        try {
            CtlButton button = getResourceController().getButton("Valider l'évaluation", 2);
            if (button != null) {
                IUser loggedOnUser = getWorkflowModule().getLoggedOnUser();
                IUser membre = null;
                String qualite = null, adjudicataire = null;
                Boolean hideAction = Boolean.valueOf(true);
                Collection<ILinkedResource> linkedResources = (Collection<ILinkedResource>)getWorkflowInstance().getValue("EvaluationDeLOffre_Tab");
                for (ILinkedResource linkedResource : linkedResources) {
                    membre = (IUser)linkedResource.getValue("NomPrenom");
                    qualite = (String)linkedResource.getValue("Qualite");
                    adjudicataire = (String)linkedResource.getValue("Adjudicataire");
                    if (membre != null && qualite != null && membre.equals(loggedOnUser) && qualite.equals("Président")) {
                            hideAction = Boolean.valueOf(false);
                    break;
                }
            }
            button.setHidden(hideAction.booleanValue());
            if (hideAction.booleanValue()) {
                getResourceController().showBodyBlock("DecisionFinale", false);
            } else {
                getResourceController().showBodyBlock("DecisionFinale", true);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
