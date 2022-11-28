package com.moovapps.Equipment;

import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.views.query.Definition;
import com.axemble.vdp.views.query.Field;

public class equipment360view extends BaseViewExtension {
    public static final String URI_PARAMETER_CLIENT = "e";

    private static final long serialVersionUID = -6516977433960169288L;

    private static final Logger LOG = Logger.getLogger(equipment360view.class);

    public boolean onPrepareView(Definition viewDefintion) {
        String eParameter = getSiteModule().getExecutionContext().getRequest().getParameter("e");
        Field field = new Field();
        field.setName("uri");
        field.setOperator("equals");
        field.setValue(eParameter);
        viewDefintion.getFilters().getFieldgroup().getFieldgroupOrField().add(field);
        return super.onPrepareView(viewDefintion);
    }

    public void onPrepareItem(ViewItem arg0) {}
}
