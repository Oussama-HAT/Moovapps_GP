package com.axemble.vdp.ui.framework.widgets.components.sys.base;

import com.axemble.commons.filters.Filter;
import com.axemble.commons.filters.PropertyFilter;
import com.axemble.commons.filters.PropertyToPropertyFilter;
import com.axemble.commons.formulas.Formula;
import com.axemble.vdoc.core.helpers.ProtocolURIHelper;
import com.axemble.vdoc.core.utils.Introspector;
import com.axemble.vdoc.directory.domain.User;
import com.axemble.vdoc.project.domain.Project;
import com.axemble.vdoc.sdk.exceptions.RenderException;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.interfaces.ui.IWritable;
import com.axemble.vdp.VDPManagers;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.activity.domain.TaskOperators;
import com.axemble.vdp.catalog.domain.Catalog;
import com.axemble.vdp.resource.domain.Property;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.core.document.CoreField;
import com.axemble.vdp.ui.core.document.FormContextMap;
import com.axemble.vdp.ui.core.providers.ITableComponentSupport;
import com.axemble.vdp.ui.core.providers.IViewProvider;
import com.axemble.vdp.ui.core.providers.InputResourceTable;
import com.axemble.vdp.ui.core.providers.base.AbstractViewProvider;
import com.axemble.vdp.ui.framework.Constants;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.composites.CtlDynamicView;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.*;
import com.axemble.vdp.ui.framework.composites.xml.XMLSelector;
import com.axemble.vdp.ui.framework.document.AbstractDocument;
import com.axemble.vdp.ui.framework.document.AbstractField;
import com.axemble.vdp.ui.framework.document.fields.IAbstractField;
import com.axemble.vdp.ui.framework.foundation.NavigateContext;
import com.axemble.vdp.ui.framework.foundation.Widget;
import com.axemble.vdp.ui.framework.widgets.*;
import com.axemble.vdp.ui.framework.widgets.table.SubFieldDesc;
import com.axemble.vdp.ui.framework.writers.CtlDivWriter;
import com.axemble.vdp.ui.framework.writers.mobility.CtlMobileResourceTableViewWriter;
import com.axemble.vdp.ui.navigation.NavigationObject;
import com.axemble.vdp.utils.CollectionUtils;
import com.axemble.vdp.utils.StringUtils;
import com.axemble.vdp.utils.XMLUtil;
import com.axemble.vdp.view.classes.ViewHelper;
import com.axemble.vdp.workflow.domain.WorkflowInstance;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

public abstract  class BaseInputTableComponent extends BaseFieldComponent implements IViewProvider, ITableComponentSupport, InputResourceTable {
    private static final long serialVersionUID = 1L;
    protected static final int CREATE_FLAG = 1;
    protected static final int DELETE_FLAG = 2;
    protected int buttonsFlags = 0;
    protected CollectionViewModel model = null;
    protected CtlDynamicView view = null;
    protected boolean isSelectable = false;
    protected boolean isExportable = true;
    protected boolean filterable = true;
    protected boolean allowImages = true;
    protected boolean allowCreate = true;
    protected boolean allowRemove = true;
    protected boolean allowEdit = true;
    protected boolean allowRead = true;
    protected int minLines = 0;
    protected int maxLines = 2147483647;
    protected int elementsPerPage = 50;
    protected HashMap<CtlListView.Item, Object> itemButtons = new HashMap();
    protected ArrayList<AbstractDocument> documents = new ArrayList();
    protected ArrayList<SubFieldDesc> subfields = null;
    protected boolean columnsAsChanged = true;
    protected boolean mustRefreshPage = true;
    private boolean initialized = false;

    public BaseInputTableComponent() {
    }

    public CtlAbstractView getView() {
        return this.view;
    }

    protected void initParameters() {
        super.initParameters();
        this.setAllowCreate(this.getParameterBoolean("allowcreate", this.isAllowCreate(), this.isAllowCreate()));
        this.setAllowRemove(this.getParameterBoolean("allowremove", this.isAllowRemove(), this.isAllowRemove()));
        this.setAllowEdit(this.getParameterBoolean("allowedit", this.isAllowEdit(), this.isAllowEdit()));
        this.setAllowRead(this.getParameterBoolean("allowread", this.isAllowRead(), this.isAllowRead()));
        this.setMinLines(this.getParameterInt("linemin", this.getMinLines(), this.getMinLines()));
        this.setMaxLines(this.getParameterInt("linemax", this.getMaxLines(), this.getMaxLines()));
        this.setFilterable(this.getParameterBoolean("filterable", this.isFilterable(), this.isFilterable()));
        this.setSelectable(this.getParameterBoolean("selectable", this.isSelectable(), this.isSelectable()));
    }

    protected void initAdditionalParameters() {
        this.setElementsPerPage(this.getParameterInt("elementsperpage", this.getElementsPerPage(), this.getElementsPerPage()));
    }

    public void afterInit() {
        super.afterInit();
        this.setSelectable(this.isAllowRemove());
    }

    protected void initParameters(String viewName, CtlText viewlabel, Catalog catalog) {
        this.view = new CtlDynamicView(viewName, viewlabel);
        this.view.setProvider(this);
        this.view.setAutoHidePager(true);
        this.view.setPageable(true);
        this.view.setFilterable(this.isFilterable());
        this.view.setRowsPerPage(this.getElementsPerPage());
        this.subfields = new ArrayList();
        String language = this.getNavigator().getDefaultFormatService().getLanguage();
        Element additionalParameters = this.getAdditionalParametersNodesElement();

        for(Node child = additionalParameters.getFirstChild(); child != null; child = child.getNextSibling()) {
            this.addSubField(catalog, language, child);
        }

        List<Element> configuration = XMLUtil.selectChildNodes(additionalParameters, "configuration");
        if (!configuration.isEmpty()) {
            List<Element> subFieldsElements = XMLUtil.selectChildNodes((Node)configuration.iterator().next(), "subfield");
            Iterator var8 = subFieldsElements.iterator();

            while(var8.hasNext()) {
                Element subfield = (Element)var8.next();
                this.addSubField(catalog, language, subfield);
            }
        }

        Element definitionNode = XMLUtil.selectChildElement(additionalParameters, new String[]{"definition"});
        if (definitionNode != null) {
            this.model = new InputTableViewModel(ViewHelper.unmarshallView(definitionNode), catalog.getProject(), this.getView());
            int rowsPerPage = this.model.getDefaultRowsPerPage();
            if (rowsPerPage > 0) {
                this.view.setRowsPerPage(rowsPerPage, false);
            }
        }

        this.columnsAsChanged = true;
        this.mustRefreshPage = true;
    }

    private void addSubField(Catalog catalog, String language, Node child) {
        if (child.getNodeType() == 1 && "subfield".equals(child.getNodeName())) {
            SubFieldDesc subDesc = new SubFieldDesc((Element)child, catalog, language);
            this.subfields.add(subDesc);
        }

    }

    public void setDocuments(Collection<AbstractDocument> documents) {
        if (documents == null) {
            documents = new ArrayList();
        }

        if (this.documents.size() != 0 || ((Collection)documents).size() != 0) {
            boolean changed = true;
            Iterator i1;
            if (this.documents.size() == ((Collection)documents).size()) {
                changed = false;
                i1 = this.documents.iterator();
                Iterator i2 = ((Collection)documents).iterator();

                while(i1.hasNext()) {
                    if (i1.next() != i2.next()) {
                        changed = true;
                    }
                }
            }

            if (changed) {
                this.documents.clear();
                i1 = ((Collection)documents).iterator();

                while(i1.hasNext()) {
                    AbstractDocument doc = (AbstractDocument)i1.next();
                    if (doc != null) {
                        this.documents.add(doc);
                    }
                }

                this.stateChanged();
            }

        }
    }

    public void addDocument(AbstractDocument document) {
        if (!this.documents.contains(document)) {
            this.documents.add(document);
            this.stateChanged();
        }

    }

    public void removeDocument(AbstractDocument document) {
        if (this.documents.remove(document)) {
            this.stateChanged();
        }

    }

    public void removeDocuments(ArrayList documents) {
        boolean changed = false;

        for(int i = 0; i < documents.size(); ++i) {
            AbstractDocument document = (AbstractDocument)documents.get(i);
            if (this.documents.remove(document)) {
                changed = true;
            }
        }

        if (changed) {
            this.stateChanged();
        }

    }

    public boolean isForceParentId() {
        return true;
    }

    public boolean isFocus() {
        return false;
    }

    public void setEditable(boolean editable) {
        if (this.isEditable() != editable) {
            this.columnsAsChanged = true;
        }

        super.setEditable(editable);
    }

    protected void stateChanged() {
        this.mustRefreshPage = true;
        super.stateChanged();
    }

    public boolean isEmpty() {
        return this.documents.size() == 0;
    }

    public boolean canStillCreateChild(int numberOfChild) {
        return this.isEditable() && this.allowCreate && this.documents.size() + numberOfChild <= this.maxLines;
    }

    public int computeButtonsFlags() {
        int flags = 0;
        if (this.canStillCreateChild(1)) {
            flags |= 1;
        }

        if (this.isAllowRemove()) {
            flags |= 2;
        }

        return flags;
    }

    public boolean onNavigate(INavigateContext context) {
        return true;
    }

    public void onRefresh() {
    }

    public void init() {
    }

    public void getColumns() {
    }

    public void getItems() {
        this.mustRefreshPage = true;
    }

    public boolean validatePage(int page) {
        return true;
    }

    public void onPageChanged(int page) {
        this.mustRefreshPage = true;
    }

    public void containtAsChanged() {
        this.mustRefreshPage = true;
    }

    public Collection getDocuments() {
        return this.documents.size() == 0 ? null : this.documents;
    }

    public List getVisibleDocuments() {
        if (this.model == null) {
            return this.documents;
        } else {
            if (this.mustRefreshPage) {
                this.refreshPage((this.buttonsFlags & 2) != 0);
            }

            List ret = new ArrayList();
            Iterator iterator = this.model.getItems().iterator();

            while(true) {
                ViewModelItem item;
                do {
                    if (!iterator.hasNext()) {
                        return ret;
                    }

                    item = (ViewModelItem)iterator.next();
                } while(this.model.getFilterGroup() != null && !this.model.getFilterGroup().isObjectVisible(item));

                ret.add(item.getKey());
            }
        }
    }

    public int getDocumentCount() {
        return this.getVisibleDocuments().size();
    }

    public AbstractDocument getDocument(int index) {
        return (AbstractDocument)this.getVisibleDocuments().get(index);
    }

    public int getDocumentIndex(AbstractDocument document) {
        return this.getVisibleDocuments().indexOf(document);
    }

    public void onColumnClick(String columnName) {
        this.mustRefreshPage = true;
        this.getModel().setSortingOrder(this, columnName);
    }

    public boolean onDeleteEvent(ActionEvent event) {
        return true;
    }

    public boolean onActionEvent(ActionEvent event) {
        return true;
    }

    public boolean mustRefresh() {
        return false;
    }

    public abstract ActionListener getDeleteResourceListener();

    public abstract ActionListener getOpenResourceListener();

    public abstract ActionListener getCreateResourceListener();

    public abstract ActionListener getDeleteResourceListListener();

    public IWritable render() throws RenderException {
        if (this.isHidden()) {
            return null;
        } else {
            int newButtonsFlags = this.computeButtonsFlags();
            if (this.buttonsFlags != newButtonsFlags) {
                this.refreshButtons(newButtonsFlags);
            }

            if (this.columnsAsChanged) {
                this.refreshColumnsInternal();
            }

            if (this.mustRefreshPage) {
                this.refreshPage((newButtonsFlags & 2) != 0);
            }

            CtlDivWriter tw = new CtlDivWriter();
            this.renderInfoMessage();
            if (this.getNavigator().isMobile()) {
                CtlMobileResourceTableViewWriter ctlMobileTW = new CtlMobileResourceTableViewWriter();
                NavigateContext navigateContext = new NavigateContext("resource-table", "view");
                navigateContext.setParameter("Component", this);
                CtlNavigationButton buttonAffichage = new CtlNavigationButton("ResourceTableButton", new CtlText(this.getStaticString("LG_DISPLAY")), navigateContext);
                buttonAffichage.addActionListener(this.getNavigator().getCurrentNavigation().getNavigationActionListener());
                if ((newButtonsFlags & 1) != 0 && this.getDocument() != null && this.getDocument().getResource() != null) {
                    CtlButton createButton = new CtlButton("create", new CtlText(this.getStaticString("LG_CREATE")));
                    createButton.addActionListener(this.getCreateResourceListener());
                    ctlMobileTW.setCreateButton(createButton);
                }

                if (this.getDocumentCount() > 0) {
                    ctlMobileTW.setIndicator(String.valueOf(this.getDocumentCount()));
                }

                ctlMobileTW.setDisplayButton(buttonAffichage);
                if (this.includeErrorInfo) {
                    ctlMobileTW.add(this.renderErrorInfo());
                }

                return ctlMobileTW;
            } else {
                tw.addAttribute("data-vdp-sysname", this.getPropertyName());
                tw.addText(this.view);
                if (this.includeErrorInfo) {
                    tw.add(this.renderErrorInfo());
                }

                return tw;
            }
        }
    }

    protected abstract CoreDocument getDocument();

    public CtlListView.Item buildItem(Widget widget, Object key) {
        return this.buildItem(widget, key, (ViewModelItem)null);
    }

    public CtlListView.Item buildItem(Widget widget, Object key, ViewModelItem viewModelItem) {
        HashMap buttonMap = new HashMap();
        CtlListView listView = this.view.getListView();
        CtlListView.Item item = null;
        Iterator var9;
        NavigationObject navigationObject;
        String objectName;
        if (this.useMenu()) {
            CtlPopupMenuHandler popupMenu = new CtlPopupMenuHandler("actions");
            if (this.isSelectable()) {
                popupMenu.setCheckBox(new CtlCheckBox());
            }

            CtlPopupMenu.CtlPopupMenuItem actionItem;
            if (this.isAllowRead() && !viewModelItem.getButtonsToRemove().contains("properties")) {
                actionItem = popupMenu.getPopupMenu().addItem("properties", new CtlLocalizedText("LG_ACCESS2"));
                actionItem.addActionListener(this.getOpenResourceListener());
                actionItem.setParam(key);
            }

            if (this.isAllowRemove() && !viewModelItem.getButtonsToRemove().contains("delete")) {
                actionItem = popupMenu.getPopupMenu().addItem("delete", new CtlLocalizedText("LG_DELETE"));
                actionItem.addActionListener(this.getDeleteResourceListener());
                actionItem.setParam(key);
            }

            if (CollectionUtils.isNotEmpty(viewModelItem.getAdditionalActions())) {
                objectName = key == null ? null : key.toString();
                var9 = viewModelItem.getAdditionalActions().iterator();

                while(var9.hasNext()) {
                    navigationObject = (NavigationObject)var9.next();
                    AbstractViewProvider.createMenuAction(this.getView(), this.getNavigator().getCurrentNavigation(), popupMenu, navigationObject, objectName, key, viewModelItem);
                }
            }

            item = listView.createItem(popupMenu);
        } else {
            if (this.isSelectable()) {
                item = listView.createItem(new CtlCheckBox());
                item.setWidth("1%");
            }

            if (this.isAllowRemove() || this.isAllowRead()) {
                LinkedList buttons = new LinkedList();
                if (this.isAllowRemove() && !viewModelItem.getButtonsToRemove().contains("delete")) {
                    CtlImageButton buttonDelete = new CtlImageButton("delete");
                    buttonDelete.addActionListener(this.getDeleteResourceListener());
                    buttons.add(buttonDelete);
                    buttonMap.put("delete", buttonDelete);
                }

                if (this.isAllowRead() && !viewModelItem.getButtonsToRemove().contains("properties")) {
                    this.addReadButtons(buttons, (AbstractDocument)key);
                }

                Iterator buttonIterator = buttons.iterator();

                while(buttonIterator.hasNext()) {
                    CtlImageButton button = (CtlImageButton)buttonIterator.next();
                    buttonMap.put(button.getName(), button);
                }

                if (CollectionUtils.isNotEmpty(viewModelItem.getAdditionalActions())) {
                    objectName = key == null ? null : key.toString();
                    var9 = viewModelItem.getAdditionalActions().iterator();

                    while(var9.hasNext()) {
                        navigationObject = (NavigationObject)var9.next();
                        CtlNavigationImageButton button = AbstractViewProvider.createImageButton(this.getView(), this.getNavigator().getCurrentNavigation(), navigationObject, objectName, key, viewModelItem);
                        buttons.add(button);
                        buttonMap.put(button.getName(), button);
                    }
                }

                if (buttons.size() > 0) {
                    if (item != null) {
                        listView.createSubitem(buttons, item);
                    } else {
                        item = listView.createItem(buttons);
                    }
                }
            }
        }

        if (item != null) {
            listView.createSubitem(widget, item);
        } else {
            item = listView.createItem(widget);
        }

        this.itemButtons.put(item, buttonMap);
        item.setParam(key);
        return item;
    }

    public HashMap getButtons(CtlListView.Item item) {
        return (HashMap)this.itemButtons.get(item);
    }

    public void refreshColumn() {
        this.columnsAsChanged = true;
        if (this.getModel() instanceof AbstractCollectionViewModel) {
            ((AbstractCollectionViewModel)this.getModel()).clearColumns();
        }

    }

    private void refreshColumnsInternal() {
        this.columnsAsChanged = false;
        this.mustRefreshPage = true;
        this.view.getListView().clearColumns();
        CtlListView.Column column2;
        if (this.useMenu()) {
            column2 = null;
            if (this.isSelectable()) {
                column2 = this.view.getListView().createColumn("selectable", new CtlCheckBox());
            } else {
                column2 = this.view.getListView().createColumn("selectable", (CtlText)null);
            }

            column2.setWidth("1%");
            column2.setSystemColumn(true);
        } else {
            if (this.isSelectable()) {
                column2 = this.view.getListView().createColumn("selectable", new CtlCheckBox());
                column2.setWidth("1%");
                column2.setSystemColumn(true);
            }

            if (this.isSelectable || this.isAllowRead()) {
                column2 = this.view.getListView().createColumn("viewImages", new CtlText(""));
                column2.setWidth("1%");
                column2.setSystemColumn(true);
            }
        }

        this.getModel().fillListViewColumns(this);
    }

    protected boolean useMenu() {
        return this.getRootNavigator().getContainerFamily() == Constants.ContainerFamily.EASYSITE && (this.isAllowRemove() || this.isAllowRead());
    }

    protected void refreshButtons(int newButtonsFlags) {
        if ((this.buttonsFlags & 2) != (newButtonsFlags & 2)) {
            this.columnsAsChanged = true;
        }

        this.buttonsFlags = newButtonsFlags;
        this.view.removeAllButtons();
        if ((newButtonsFlags & 1) != 0) {
            this.view.createButton("create", new CtlLocalizedText("LG_CREATE"), false, this.getCreateResourceListener());
        }

        if ((newButtonsFlags & 2) != 0) {
            CtlButton bt = this.view.createButton("delete", new CtlLocalizedText("LG_DELETE"), false, this.getDeleteResourceListListener());
            bt.setStyle(2);
        }

    }

    public void refreshPage(boolean showDelete) {
        this.mustRefreshPage = false;
        this.getView().getListView().clearItems();
        this.itemButtons.clear();
        List<ViewModelItem> items = new ArrayList();

        ViewModelItem viewModelItem;
        label92:
        for(Iterator var3 = this.documents.iterator(); var3.hasNext(); items.add(viewModelItem)) {
            AbstractDocument doc = (AbstractDocument)var3.next();
            viewModelItem = new ViewModelItem(doc);
            if (this.getNavigator().isMobile()) {
                this.model.setLinkNavigateContext(new NavigateContext((String)null, "properties"));
            } else {
                viewModelItem.addButtonParameter("properties", (String)null, doc);
            }

            viewModelItem.addButtonParameter("delete", (String)null, doc);
            Iterator iterator = this.model.getAllColumns().values().iterator();

            while(true) {
                ViewModelColumn col;
                do {
                    if (!iterator.hasNext()) {
                        iterator = this.model.getFilterGroup().getAllFiltersRecursively((Class)null).iterator();

                        while(iterator.hasNext()) {
                            Filter filter = (Filter)iterator.next();
                            if (filter instanceof PropertyFilter) {
                                viewModelItem.setValue(((PropertyFilter)filter).getPropertyName(), this.getDocumentValue(((PropertyFilter)filter).getPropertyName(), doc));
                            } else if (filter instanceof PropertyToPropertyFilter) {
                                viewModelItem.setValue(((PropertyToPropertyFilter)filter).getPropertyName1(), this.getDocumentValue(((PropertyToPropertyFilter)filter).getPropertyName1(), doc));
                                viewModelItem.setValue(((PropertyToPropertyFilter)filter).getPropertyName2(), this.getDocumentValue(((PropertyToPropertyFilter)filter).getPropertyName2(), doc));
                            }
                        }

                        this.alterViewModelItem(viewModelItem, doc);
                        if (doc instanceof CoreDocument) {
                            ((FormContextMap)((CoreDocument)doc).getContext()).release();
                        }
                        continue label92;
                    }

                    col = (ViewModelColumn)iterator.next();
                    String pName = col.getName();
                    if (!pName.startsWith("_tmp")) {
                        ViewHelper.PropertyInfo pInfo = null;
                        if (doc instanceof CoreDocument) {
                            CoreDocument coreDoc = (CoreDocument)doc;
                            if (coreDoc.getResourceDefinition() != null) {
                                pInfo = new ViewHelper.PropertyInfo(coreDoc.getResourceDefinition().getStoragePropertiesMap(), pName);
                            }

                            if (coreDoc.getCatalog() != null && (pInfo == null || pInfo.property == null)) {
                                pInfo = new ViewHelper.PropertyInfo(coreDoc.getCatalog().getPropertiesMap(), pName);
                            }

                            if (pInfo != null && pInfo.property != null) {
                                pName = pInfo.property.getName();
                            }
                        }

                        Object value = this.getDocumentValue(pName, doc);
                        if (pInfo != null) {
                            value = VDPManagers.getResourceManager().getObjectValueRecursively(pInfo, (Serializable)value);
                        }

                        viewModelItem.setValue(col.getName(), value);
                    }
                } while(!StringUtils.isNotEmpty(col.getFormula()));

                Formula formula = new Formula(col.getFormula());
                Iterator var16 = formula.getDependencies().iterator();

                while(var16.hasNext()) {
                    String dependency = (String)var16.next();
                    if (!viewModelItem.getValues().containsKey(dependency)) {
                        Object value = this.getDocumentValue(dependency, doc);
                        viewModelItem.setValue(dependency, value);
                    }
                }
            }
        }

        this.model.setItems(items);
        this.model.fillListViewItems(this);
    }

    private SubFieldDesc getSubfieldDesc(String propertyName) {
        if (this.subfields != null && !this.subfields.isEmpty()) {
            Iterator subfieldIterator = this.subfields.iterator();

            SubFieldDesc subfield;
            do {
                if (!subfieldIterator.hasNext()) {
                    return null;
                }

                subfield = (SubFieldDesc)subfieldIterator.next();
            } while(!StringUtils.equals(subfield.getCompletePropertyName(), propertyName));

            return subfield;
        } else {
            return null;
        }
    }

    private WorkflowInstance findCoreDocumentWorkflowInstance(CoreDocument coreDoc) {
        if (coreDoc == null) {
            return null;
        } else {
            return coreDoc.getSourceDocumentToRead() != null ? coreDoc.getSourceDocumentToRead().getWorkflowInstance() : coreDoc.getWorkflowInstance();
        }
    }

    private Object getDocumentValue(String propertyName, AbstractDocument doc) {
        if (doc instanceof CoreDocument) {
            CoreDocument coreDoc = (CoreDocument)doc;
            if (propertyName.equals("sys_ModificationDate")) {
                return coreDoc.getResource() == null ? null : coreDoc.getResource().getModifiedDate();
            }

            if (propertyName.equals("sys_ResourceDefinition")) {
                return coreDoc.getResourceDefinition() == null ? null : coreDoc.getResourceDefinition().getName();
            }

            if (propertyName.equals("sys_Workflow")) {
                return coreDoc.getWorkflow() == null ? null : coreDoc.getWorkflow().getName();
            }

            if (propertyName.equals("sys_WorkflowContainer")) {
                return coreDoc.getWorkflow() == null ? null : coreDoc.getWorkflow().getWorkflowContainer().getName();
            }

            if (propertyName.equals("sys_Catalog")) {
                return coreDoc.getCatalog() == null ? null : coreDoc.getCatalog().getName();
            }

            if (propertyName.equals("sys_CurrentSteps")) {
                List<String> currentSteps = new ArrayList();
                Iterator var17 = this.findCoreDocumentWorkflowInstance(coreDoc).getTaskInstances(2).iterator();

                while(var17.hasNext()) {
                    TaskInstance ti = (TaskInstance)var17.next();
                    currentSteps.add(ti.getTask().getLabel(this.getLanguage()));
                }

                return currentSteps;
            }

            if (propertyName.equals("sys_CurrentActors")) {
                return this.getOperators(this.findCoreDocumentWorkflowInstance(coreDoc), -1);
            }

            if (propertyName.equals("sys_WaitingActors")) {
                return this.getOperators(this.findCoreDocumentWorkflowInstance(coreDoc), 1);
            }

            if (propertyName.equals("sys_PastActors")) {
                return this.getOperators(this.findCoreDocumentWorkflowInstance(coreDoc), 2);
            }

            if (propertyName.equals("sys_AllPastActors")) {
                List<User> pastActors = VDPManagers.getActivityManager().getPastActors(this.findCoreDocumentWorkflowInstance(coreDoc));
                Collections.sort(pastActors, new Comparator<User>() {
                    public int compare(User arg0, User arg1) {
                        return arg0.getLastName().compareToIgnoreCase(arg1.getLastName());
                    }
                });
                return pastActors;
            }
        }

        if (propertyName.equals("sys_Today")) {
            return new Timestamp(System.currentTimeMillis());
        } else if (propertyName.equals("sys_ConnectedUser")) {
            return this.getRootNavigator().getLoggedOnUser().getFullName();
        } else {
            AbstractField field = doc.getAbstractFieldByName(propertyName, true);
            if (field != null) {
                Widget customWidget = this.getCustomWidget(doc, field, this.getSubfieldDesc(propertyName));
                if (customWidget != null) {
                    return customWidget;
                } else {
                    Object value = field.getValue();
                    if (field instanceof CoreField) {
                        Property property = ((CoreField)field).getProperty();
                        if (property != null && property.getAdditionalType() == 1) {
                            if (value instanceof String) {
                                Object resourceObj = ProtocolURIHelper.getResource((String)value);
                                if (resourceObj != null) {
                                    value = ProtocolURIHelper.getResourceLabel(resourceObj, this.getNavigator().getDefaultFormatService().getLanguage());
                                }
                            } else if (value instanceof Collection) {
                                Collection<?> oldValue = (Collection)value;
                                value = new ArrayList();

                                Object elementValue;
                                for(Iterator iterator = oldValue.iterator(); iterator.hasNext(); CollectionUtils.cast(value, Object.class).add(elementValue)) {
                                    elementValue = iterator.next();
                                    if (elementValue instanceof String) {
                                        Object resourceObj = ProtocolURIHelper.getResource((String)elementValue);
                                        if (resourceObj != null) {
                                            elementValue = ProtocolURIHelper.getResourceLabel(resourceObj, this.getRootNavigator().getUserFormat().getLanguage());
                                        }
                                    }
                                }
                            }
                        } else if (property != null && property.getList() != null) {
                            value = property.getList().getValueLabelForObject(value, this.getLanguage());
                        }
                    }

                    return value;
                }
            } else {
                if (StringUtils.contains(propertyName, ".")) {
                    String propertyStart = StringUtils.split(propertyName, "\\.", 2)[0];
                    String propertyEnd = StringUtils.split(propertyName, "\\.", 2)[1];
                    field = doc.getAbstractFieldByName(propertyStart, true);
                    if (field != null && field.getValue() != null) {
                        return Introspector.getProperty(field.getValue(), propertyEnd, true, (Object[])null);
                    }
                }

                return null;
            }
        }
    }

    protected Widget getCustomWidget(AbstractDocument document, IAbstractField abstractField, SubFieldDesc subDesc) {
        return null;
    }

    private List<TaskOperators> getOperators(WorkflowInstance workflowInstance, int status) {
        Map<String, TaskOperators> usersActivitiesMap = new HashMap();
        Iterator var4 = workflowInstance.getTaskInstances(2).iterator();

        while(var4.hasNext()) {
            TaskInstance ti = (TaskInstance)var4.next();
            Collection<TaskOperators> operators = ti.getTaskOperators(status);
            Iterator var7 = operators.iterator();

            while(var7.hasNext()) {
                TaskOperators operator = (TaskOperators)var7.next();
                String mapKey = operator.getFulfiller().getId().toString() + "|" + operator.getAddressee().getId().toString();
                usersActivitiesMap.put(mapKey, operator);
            }
        }

        List<TaskOperators> values = new ArrayList(usersActivitiesMap.values());
        Collections.sort(values, new BaseInputTableComponent.OperatorComparator());
        return values;
    }

    protected void addReadButtons(LinkedList buttons, AbstractDocument document) {
        CtlImageButton buttonOpen = new CtlImageButton("properties");
        buttonOpen.setTitle(this.view.getStaticString("LG_ACCESS2"));
        buttonOpen.addActionListener(this.getOpenResourceListener());
        buttonOpen.setParam(document);
        buttons.add(buttonOpen);
    }

    protected void alterViewModelItem(ViewModelItem item, AbstractDocument document) {
    }

    public boolean onCreateChild(BaseInputTableComponent table) {
        return true;
    }

    public boolean onDeleteChild(BaseInputTableComponent table, AbstractDocument child) {
        return true;
    }

    public boolean onDeleteChildren(BaseInputTableComponent table, Collection children) {
        return true;
    }

    public boolean onOpenChild(BaseInputTableComponent table, AbstractDocument child) {
        return true;
    }

    public IViewModel getModel() {
        if (this.model == null) {
            this.model = new InputTableViewModel(this.subfields, (Project)null, this.getView());
        }

        return this.model;
    }

    public void onGroupLinkChange(Object key) {
    }

    public void onGroupLinkSecondChange(Object key) {
    }

    public void setModel(IViewModel model) {
        this.model = (CollectionViewModel)model;
    }

    public boolean onMoveEvent(ActionEvent event) {
        return true;
    }

    public void onRowsPerPageChanged(int nbRowsPerPage) {
    }

    public void onDisplayModeChange(int displayMode) {
    }

    public void onSorterChanged(String columnName, int order) {
        this.getItems();
    }

    public boolean isAllowImages() {
        return this.allowImages;
    }

    public void setAllowImages(boolean allowImages) {
        this.allowImages = allowImages;
    }

    public boolean isSelectable() {
        return this.isSelectable;
    }

    public void setSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    public boolean isExportable() {
        return this.isExportable;
    }

    public void setExportable(boolean isExportable) {
        this.isExportable = isExportable;
    }

    public boolean isFilterable() {
        return this.filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public void addAction(NavigationObject action) {
    }

    public void clearActions() {
    }

    public void setFilterFormElement(Element filterFormElement) {
    }

    public XMLSelector getParentSelector() {
        return null;
    }

    public int getMinLines() {
        return this.minLines;
    }

    public void setMinLines(int minLines) {
        this.minLines = minLines;
    }

    public int getMaxLines() {
        return this.maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public boolean isAllowCreate() {
        return this.allowCreate && this.isEditable();
    }

    public void setAllowCreate(boolean allowCreate) {
        this.allowCreate = allowCreate;
    }

    public boolean isAllowRemove() {
        return this.allowRemove && this.isEditable();
    }

    public void setAllowRemove(boolean allowRemove) {
        this.allowRemove = allowRemove;
    }

    public boolean isAllowEdit() {
        return this.allowEdit && this.isEditable();
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public boolean isAllowRead() {
        return this.allowRead;
    }

    public void setAllowRead(boolean allowRead) {
        this.allowRead = allowRead;
    }

    public int getElementsPerPage() {
        return this.elementsPerPage;
    }

    public void setElementsPerPage(int elementsPerPage) {
        this.elementsPerPage = elementsPerPage;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    protected class OperatorComparator implements Comparator<TaskOperators> {
        protected OperatorComparator() {
        }

        public int compare(TaskOperators o1, TaskOperators o2) {
            return o1.getFulfiller().getLastName().compareToIgnoreCase(o2.getFulfiller().getLastName());
        }
    }
}