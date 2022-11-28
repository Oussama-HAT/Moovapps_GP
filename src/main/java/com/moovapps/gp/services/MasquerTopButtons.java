package com.moovapps.gp.services;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.INamedWidget;
import java.util.List;

public class MasquerTopButtons extends BaseDocumentExtension {
    private static final long serialVersionUID = -6228898771536617988L;

    protected static final Logger log = Logger.getLogger(MasquerTopButtons.class);

    public boolean onAfterLoad() {
        try {
            NamedContainer topcontainer = getResourceController().getButtonContainer(1);
            List<IWidget> buttonList1 = topcontainer.getWidgets();
            for (IWidget iWidget1 : buttonList1) {
                INamedWidget iNamedWidget = (INamedWidget)iWidget1;
                if (!iNamedWidget.getName().equals("close") && !iNamedWidget.getName().equals("saveandclose") &&
                        !iNamedWidget.getName().equals("save") && !iNamedWidget.getName().equals("history"))
                    iNamedWidget.setHidden(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}

