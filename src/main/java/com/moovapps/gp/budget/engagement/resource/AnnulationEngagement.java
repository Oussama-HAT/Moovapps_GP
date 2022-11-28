package com.moovapps.gp.budget.engagement.resource;

import com.axemble.vdoc.sdk.document.extensions.BaseResourceExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.core.document.fields.DateField;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.DoubleInputComponent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AnnulationEngagement extends BaseResourceExtension {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public boolean onAfterLoad() {
        IWorkflowInstance iWorkflowInstance = getLinkedResource().getParentInstance();
        DateField Field = null;
        Date start = null;
        if (getLinkedResource().getValue("id") == null) {
            getLinkedResource().setValue("id", UUID.randomUUID().toString());
            getLinkedResource().save(getWorkflowModule().getLoggedOnUserContext());
        }
        if (iWorkflowInstance!=null && iWorkflowInstance.getValue("DateEngagement")!=null) {
            Field = (DateField) this.getDocument().getDefaultWidget("DateDiminution");
            start = (Date) iWorkflowInstance.getValue("DateEngagement");
            Field.setStartSelectionRange(DATE_FORMAT.format(start));
        }

        Double Max = ((Number) iWorkflowInstance.getValue("ResteAPayer")).doubleValue();
        ArrayList<ILinkedResource> linkedResourceCollection = (ArrayList<ILinkedResource>) getLinkedResource().getParentInstance().getLinkedResources(getLinkedResource().getDefinition().getName());
        if (linkedResourceCollection != null) {
            if (linkedResourceCollection.isEmpty()) {
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAnnule"));
                DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                component.setNumberMax(Max);
            } else {
                for (int i = 0; i < linkedResourceCollection.size(); i++) {
                    if (!linkedResourceCollection.get(i).getValue("id").equals(getLinkedResource().getValue("id"))) {
                        boolean flagged = (boolean) linkedResourceCollection.get(i).getValue("FLAG");
                        if(!flagged)
                        Max -= (linkedResourceCollection.get(i).getValue("MontantAnnule") != null ? (Double) linkedResourceCollection.get(i).getValue("MontantAnnule") : 0);
                    }
                }
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAnnule"));
                DoubleInputComponent component = (DoubleInputComponent) field.getInputComponent();
                component.setNumberMax(Max);
            }
        }
        return super.onAfterLoad();
    }
}
