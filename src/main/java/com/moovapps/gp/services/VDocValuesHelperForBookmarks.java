package com.moovapps.gp.services;

import com.aspose.words.Bookmark;
import com.aspose.words.DocumentBuilder;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IGroup;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.ILocalization;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.structs.Period;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.foundation.Navigator;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.imageio.ImageIO;

public class VDocValuesHelperForBookmarks {

    private static final Logger LOG = Logger.getLogger(VDocValuesHelperForBookmarks.class);
    private static final String SYS_TITLE = "sys_Title";

    public static void setType(Object resourceValue, Bookmark bookmark, DocumentBuilder builder, IWorkflowModule workflowModule) throws Exception {
        if (resourceValue == null) {
            bookmark.setText("");
        } else if (resourceValue instanceof Float || resourceValue instanceof Double) {
            setNumberType(resourceValue, bookmark, workflowModule);
        } else if (resourceValue instanceof Date) {
            setDateType((Date) resourceValue, bookmark, workflowModule);
        } else if (resourceValue instanceof Period) {
            setPeriodType((Period) resourceValue, bookmark, workflowModule);
        } else if (resourceValue instanceof IUser || resourceValue instanceof IOrganization || resourceValue instanceof ILocalization || resourceValue instanceof IGroup) {
            setDirectoryType(resourceValue, bookmark);
        } else if (resourceValue instanceof IStorageResource) {
            setStorageResourceType((IStorageResource) resourceValue, bookmark);
        } else if (resourceValue instanceof ILinkedResource) {
            setLinkedResourceType((ILinkedResource) resourceValue, bookmark);
        } else if (resourceValue instanceof IAttachment) {
            setAttachmentType((IAttachment) resourceValue, bookmark, builder);
        } else if (resourceValue instanceof Collection) {
            String separator = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.collection.separator");
            StringBuilder textBuilder = new StringBuilder();
            Collection<Object> values = (Collection<Object>) resourceValue;
            boolean isText = true;
            for (Object value : values) {
                setType(value, bookmark, builder, workflowModule);
                if (value instanceof IAttachment) {
                    isText = false;
                    builder.insertBreak(8);
                    continue;
                }
                textBuilder.append(bookmark.getText());
                textBuilder.append(separator);
                textBuilder.append(" ");
            }


            if (values.size() > 0 && isText) {
                bookmark.setText(textBuilder.substring(0, textBuilder.lastIndexOf(separator)));
            }
        } else {
            setStringType(resourceValue, bookmark);
        }
    }

    public static void setStringType(Object value, Bookmark bookmark) throws Exception {
        bookmark.setText(value.toString());
    }

    public static void setNumberType(Object numberValue, Bookmark bookmark, IWorkflowModule workflowModule) throws Exception {
        String format = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.number.format");
        DecimalFormat decimalFormat = new DecimalFormat(format);
        bookmark.setText(decimalFormat.format(numberValue));
    }

    public static void setDateType(Date date, Bookmark bookmark, IWorkflowModule workflowModule) throws Exception {
        String format = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.date.format");
        SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
        bookmark.setText(dateFormatter.format(date));
    }

    public static void setPeriodType(Period period, Bookmark bookmark, IWorkflowModule workflowModule) throws Exception {
        Date startDate = period.getStartDate();
        Date endDate = period.getEndDate();
        if (startDate != null && endDate != null) {
            String format = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.period.format");
            SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
            String formattedStartDate = dateFormatter.format(startDate);
            String formattedEndDate = dateFormatter.format(endDate);
            bookmark.setText("Du " + formattedStartDate + " Au " + formattedEndDate);
        }
    }

    public static void setDirectoryType(Object directoryValue, Bookmark bookmark) throws Exception {
        if (directoryValue instanceof IUser) {

            bookmark.setText(((IUser) directoryValue).getFullName());
        } else if (directoryValue instanceof IOrganization) {

            bookmark.setText(((IOrganization) directoryValue).getLabel());
        } else if (directoryValue instanceof ILocalization) {

            bookmark.setText(((ILocalization) directoryValue).getLabel());
        } else if (directoryValue instanceof IGroup) {

            bookmark.setText(((IGroup) directoryValue).getLabel());
        }
    }

    public static void setStorageResourceType(IStorageResource storageResource, Bookmark bookmark) throws Exception {
        bookmark.setText((String) storageResource.getValue("sys_Title"));
    }

    public static void setLinkedResourceType(ILinkedResource linkedResource, Bookmark bookmark) throws Exception {
        bookmark.setText(linkedResource.getDefinition().getName());
    }

    public static void setAttachmentType(IAttachment attachment, Bookmark bookmark, DocumentBuilder builder) throws Exception {
        InputStream inputStream = attachment.getInputStream();
        if (inputStream != null) {
            BufferedImage img = null;
            img = ImageIO.read(inputStream);
            if (img != null) {
                builder.moveToBookmark(bookmark.getName());
                builder.insertImage(img);

            } else {
                String baseUrl = Navigator.getNavigator().getExecutionContext().getRequest().getBaseUrl();
                builder.moveToBookmark(bookmark.getName());
                int previousStyleIdentifier = builder.getFont().getStyleIdentifier();
                builder.getFont().setStyleIdentifier(85);
                builder.insertHyperlink(attachment.getName(), String.valueOf(baseUrl) + attachment.getURI(), false);
                builder.getFont().setStyleIdentifier(previousStyleIdentifier);
            }
            inputStream.close();
        }
    }
}
