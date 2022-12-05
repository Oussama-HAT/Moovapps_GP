package com.moovapps.gp.services;

import com.aspose.words.Bookmark;
import com.aspose.words.BookmarkCollection;
import com.aspose.words.BookmarkEnd;
import com.aspose.words.BookmarkStart;
import com.aspose.words.Cell;
import com.aspose.words.Document;
import com.aspose.words.DocumentBase;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.License;
import com.aspose.words.Node;
import com.aspose.words.Paragraph;
import com.aspose.words.Row;
import com.aspose.words.Run;
import com.aspose.words.Table;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.turbine.Turbine;

public class GenerateWordFile extends BaseDocumentExtension {
    private static final long serialVersionUID = 2913421132212918650L;
    protected static final String BOOKMARK_SEPARATOR_TABLEAU = "__";
    protected static final String BOOKMARK_SEPARATOR_RESERVOIR = "_";
    protected static final String VAR_START_TOKEN = "${";
    protected static final String VAR_END_TOKEN = "}";
    protected static final String BOOKMARK_TEMP_NAME = "bookmark_temp";

    public InputStream valorization(IWorkflowModule workflowModule, IResource iResource, IAttachment iAttachment) throws Exception {
        License license = new License();

        String licensePath = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.licence.path");

        if (StringUtils.isNotEmpty(licensePath)) {
            license.setLicense(Turbine.getRealPath(licensePath));
        }
        Document doc = new Document(iAttachment.getInputStream());
        DocumentBuilder builder = new DocumentBuilder(doc);
        bookmarkValorization(doc, builder, workflowModule, iResource);

        return getInputStream(doc);
    }

    public InputStream valorization(IWorkflowModule workflowModule, IResource iResource, IAttachment iAttachment , boolean includeHeader) throws Exception {
        License license = new License();

        String licensePath = workflowModule.getConfiguration().getStringProperty("com.vdoc.connector.aspose.licence.path");

        if (StringUtils.isNotEmpty(licensePath)) {
            license.setLicense(Turbine.getRealPath(licensePath));
        }
        Document doc = new Document(iAttachment.getInputStream());
        DocumentBuilder builder = new DocumentBuilder(doc);
        bookmarkValorization(doc, builder, workflowModule, iResource , includeHeader);

        return getInputStream(doc);
    }

    private void bookmarkValorization(Document doc, DocumentBuilder builder, IWorkflowModule workflowModule, IResource iResource , boolean includeHeader) throws Exception {
        Map<String, Collection<Bookmark>> bookmarkHeadersMap = new HashMap<>();

        BookmarkCollection bookmarkCollection = doc.getRange().getBookmarks();
        Iterator<Bookmark> bookmarkIterator = bookmarkCollection.iterator();

        while (bookmarkIterator.hasNext()) {

            Bookmark bookmark = bookmarkIterator.next();
            bookmark.setText("");

            if (bookmark.getName().contains("__")) {

                String tabSysName = bookmark.getName().split("__")[0];
                Collection<Bookmark> bookmarkHeaders = bookmarkHeadersMap.get(tabSysName);
                if (bookmarkHeaders == null) {
                    bookmarkHeaders = new ArrayList<>();
                }

                bookmarkHeaders.add(bookmark);
                bookmarkHeadersMap.put(tabSysName, bookmarkHeaders);
                continue;
            }

            Object resourceValue = iResource.getValue(bookmark.getName());

            if (resourceValue != null) {
                VDocValuesHelperForBookmarks.setType(resourceValue, bookmark, builder, workflowModule);
            }
        }

        buildTableRows(bookmarkHeadersMap, doc, builder, workflowModule, iResource , includeHeader);
    }

    private void bookmarkValorization(Document doc, DocumentBuilder builder, IWorkflowModule workflowModule, IResource iResource) throws Exception {
        Map<String, Collection<Bookmark>> bookmarkHeadersMap = new HashMap<>();

        BookmarkCollection bookmarkCollection = doc.getRange().getBookmarks();
        Iterator<Bookmark> bookmarkIterator = bookmarkCollection.iterator();

        while (bookmarkIterator.hasNext()) {

            Bookmark bookmark = bookmarkIterator.next();
            bookmark.setText("");

            if (bookmark.getName().contains("__")) {

                String tabSysName = bookmark.getName().split("__")[0];
                Collection<Bookmark> bookmarkHeaders = bookmarkHeadersMap.get(tabSysName);
                if (bookmarkHeaders == null) {
                    bookmarkHeaders = new ArrayList<>();
                }

                bookmarkHeaders.add(bookmark);
                bookmarkHeadersMap.put(tabSysName, bookmarkHeaders);
                continue;
            }

            Object resourceValue = iResource.getValue(bookmark.getName());

            if (resourceValue != null) {
                VDocValuesHelperForBookmarks.setType(resourceValue, bookmark, builder, workflowModule);
            }
        }

        buildTableRows(bookmarkHeadersMap, doc, builder, workflowModule, iResource);
    }

    public static InputStream getInputStream(Document document) throws Exception {
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        try {
            int saveFormat = document.getOriginalLoadFormat();
            bos = new ByteArrayOutputStream();
            document.save(bos, saveFormat);
            bis = new ByteArrayInputStream(bos.toByteArray());

            return bis;
        } finally {
            bos.flush();
            bos.close();
        }
    }

    private void buildTableRows(Map<String, Collection<Bookmark>> bookmarkHeadersMap, Document doc, DocumentBuilder builder, IWorkflowModule workflowModule, IResource iResource) throws Exception {
        for (Map.Entry<String, Collection<Bookmark>> entry : bookmarkHeadersMap.entrySet()) {
            Table table = null;

            Collection<Bookmark> bookmarkHeaders = entry.getValue();

            Cell parentCell = (Cell) ((Bookmark) bookmarkHeaders.iterator().next()).getBookmarkStart().getAncestor(7);
            if (parentCell != null) {

                table = parentCell.getParentRow().getParentTable();

                Collection<IResource> resources = (Collection<IResource>) iResource.getValue(entry.getKey());

                if (resources != null) {

                    for (IResource resource : resources) {

                        Row newRow = new Row((DocumentBase) doc);
                        newRow.getRowFormat().setAllowBreakAcrossPages(true);
                        table.appendChild((Node) newRow);

                        for (Bookmark bookmarkTableCellHeader : bookmarkHeaders) {

                            Cell cellHeader = (Cell) bookmarkTableCellHeader.getBookmarkStart().getAncestor(7);
                            String propertyName = bookmarkTableCellHeader.getName().split("__")[1];
                            Object resourceValue = resource.getValue(propertyName);

                            String label = resource.getDefinition().getProperty(propertyName).getLabel();
                            bookmarkTableCellHeader.setText(label);

                            Cell cell = new Cell((DocumentBase) doc);

                            cell.getCellFormat().setWidth(cellHeader.getCellFormat().getWidth());
                            newRow.appendChild((Node) cell);
                            cell.appendChild((Node) new Paragraph((DocumentBase) doc));

                            cell.getFirstParagraph().appendChild((Node) new BookmarkStart((DocumentBase) doc, "bookmark_temp"));
                            cell.getFirstParagraph().appendChild((Node) new Run((DocumentBase) doc, "bookmark_temp"));
                            cell.getFirstParagraph().appendChild((Node) new BookmarkEnd((DocumentBase) doc, "bookmark_temp"));

                            if (resourceValue instanceof Number) {

                                cell.getFirstParagraph().getParagraphFormat().setAlignment(2);
                            } else {

                                cell.getFirstParagraph().getParagraphFormat().setAlignment(0);
                            }

                            Bookmark bookmarkTemp = doc.getRange().getBookmarks().get("bookmark_temp");
                            VDocValuesHelperForBookmarks.setType(resourceValue, bookmarkTemp, builder, workflowModule);
                            bookmarkTemp.remove();
                        }
                    }
                }
            }
        }
    }

    private void buildTableRows(Map<String, Collection<Bookmark>> bookmarkHeadersMap, Document doc, DocumentBuilder builder, IWorkflowModule workflowModule, IResource iResource , boolean includeHeader) throws Exception {
        for (Map.Entry<String, Collection<Bookmark>> entry : bookmarkHeadersMap.entrySet()) {
            Table table = null;

            Collection<Bookmark> bookmarkHeaders = entry.getValue();

            Cell parentCell = (Cell) ((Bookmark) bookmarkHeaders.iterator().next()).getBookmarkStart().getAncestor(7);
            if (parentCell != null) {

                table = parentCell.getParentRow().getParentTable();

                Collection<IResource> resources = (Collection<IResource>) iResource.getValue(entry.getKey());

                if (resources != null) {

                    for (IResource resource : resources) {

                        Row newRow = new Row((DocumentBase) doc);
                        newRow.getRowFormat().setAllowBreakAcrossPages(true);
                        table.appendChild((Node) newRow);

                        for (Bookmark bookmarkTableCellHeader : bookmarkHeaders) {

                            Cell cellHeader = (Cell) bookmarkTableCellHeader.getBookmarkStart().getAncestor(7);
                            String propertyName = bookmarkTableCellHeader.getName().split("__")[1];
                            Object resourceValue = resource.getValue(propertyName);

                            String label = resource.getDefinition().getProperty(propertyName).getDescription();
                            bookmarkTableCellHeader.setText(label);

                            Cell cell = new Cell((DocumentBase) doc);

                            cell.getCellFormat().setWidth(cellHeader.getCellFormat().getWidth());
                            newRow.appendChild((Node) cell);
                            cell.appendChild((Node) new Paragraph((DocumentBase) doc));

                            cell.getFirstParagraph().appendChild((Node) new BookmarkStart((DocumentBase) doc, "bookmark_temp"));
                            cell.getFirstParagraph().appendChild((Node) new Run((DocumentBase) doc, "bookmark_temp"));
                            cell.getFirstParagraph().appendChild((Node) new BookmarkEnd((DocumentBase) doc, "bookmark_temp"));

                            if (resourceValue instanceof Number) {

                                cell.getFirstParagraph().getParagraphFormat().setAlignment(2);
                            } else {

                                cell.getFirstParagraph().getParagraphFormat().setAlignment(0);
                            }

                            Bookmark bookmarkTemp = doc.getRange().getBookmarks().get("bookmark_temp");
                            VDocValuesHelperForBookmarks.setType(resourceValue, bookmarkTemp, builder, workflowModule);
                            bookmarkTemp.remove();
                        }
                    }
                }
            }
        }
    }
}