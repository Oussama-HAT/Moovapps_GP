package com.moovapps.gp.budget.rap.resource;

import com.axemble.vdoc.sdk.document.extensions.BaseResourceExtension;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.core.document.fields.DateField;
import com.axemble.vdp.ui.core.document.fields.TextBoxField;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.BigDecimalInputComponent;
import com.axemble.vdp.ui.framework.widgets.components.sys.forms.DoubleInputComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.moovapps.gp.budget.helpers.calculate.castToBigDecimal;

public class AnnulationRAP extends BaseResourceExtension {
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

        BigDecimal Max = castToBigDecimal(iWorkflowInstance.getValue("ResteAPayer"));
        ArrayList<ILinkedResource> linkedResourceCollection = (ArrayList<ILinkedResource>) getLinkedResource().getParentInstance().getLinkedResources(getLinkedResource().getDefinition().getName());
        if (linkedResourceCollection != null) {
            if (linkedResourceCollection.isEmpty()) {
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAnnule"));
                BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                component.setNumberMax(Max);
            } else {
                for (int i = 0; i < linkedResourceCollection.size(); i++) {
                    if (!linkedResourceCollection.get(i).getValue("id").equals(getLinkedResource().getValue("id"))) {
                        boolean flagged = (boolean) linkedResourceCollection.get(i).getValue("FLAG");
                        if(!flagged) {
                            BigDecimal montantannuler = linkedResourceCollection.get(i).getValue("MontantAnnule") != null ? castToBigDecimal(linkedResourceCollection.get(i).getValue("MontantAnnule")) : BigDecimal.ZERO;
                            Max = Max.subtract(montantannuler);
                            // Max -= (linkedResourceCollection.get(i).getValue("MontantAnnule") != null ? (Double) linkedResourceCollection.get(i).getValue("MontantAnnule") : 0);
                        }
                    }
                }
                TextBoxField field = ((TextBoxField) getDocument().getDefaultWidget("MontantAnnule"));
                BigDecimalInputComponent component = (BigDecimalInputComponent) field.getInputComponent();
                component.setNumberMax(Max);
            }
        }
        return super.onAfterLoad();
    }
}
