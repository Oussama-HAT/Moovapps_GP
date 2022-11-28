package com.moovapps.gp.services;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

public class MasquerBottomButtons extends BaseDocumentExtension {
    private static final long serialVersionUID = -6228898771536617988L;

    protected static final Logger log = Logger.getLogger(MasquerBottomButtons.class);

    public boolean onAfterLoad() {
        try {
            CtlButton button = getResourceController().getButton("Valider", 2);
            if (button != null)
                button.setHidden(true);
            button = getResourceController().getButton("AO Infructieux", 2);
            if (button != null)
                button.setHidden(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}

