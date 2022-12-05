package com.moovapps.gp.helpers;

import com.axemble.commons.formulas.Formula;
import com.axemble.commons.formulas.FormulaObject;
import com.axemble.commons.utils.ObjectUtils;
import com.axemble.easysite.ui.runtime.ContextHandler;
import com.axemble.easysite.ui.servlets.EzsToken;
import com.axemble.vdoc.core.helpers.ConfigurationHelper;
import com.axemble.vdoc.core.helpers.FormulaFunctionHelper;
import com.axemble.vdoc.core.helpers.VariableFormulaHelper;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.IConnectionDefinition;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.runtime.InternalSiteExecutionContext;
import com.axemble.vdoc.sdk.utils.StringUtils;
import com.axemble.vdp.localization.interfaces.IFormatService;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.view.classes.ViewFormulaContext;
import com.axemble.vdp.workflow.classes.*;

import java.util.List;
import java.util.Map;

public class ExpressionUtils {
    public static final String evaluate(String expression, IContext context, IConnectionDefinition connectionDefinition, IResource resource, String language) {
        String contextPath = null;
        IFormatService userFormat = null;
        if (Navigator.getNavigator() != null) {
            userFormat = Navigator.getNavigator().getExecutionContext().getUserFormat();
            contextPath = Navigator.getNavigator().getExecutionContext().getRequest().getContextPath();
        }
        ConnectorsFormulaContext connectorsFormulaContext = new ConnectorsFormulaContext(context.getUser(), resource, Modules.getWorkflowModule().getConfiguration(), connectionDefinition, context, contextPath, language);
        return VariableFormulaHelper.evaluateString(expression, (FormulaContext)connectorsFormulaContext, userFormat, false);
    }

    public static final String evaluate(String expression, IContext context, IResource resource, String language) {
        return evaluate(expression, context, null, resource, language);
    }

    public static final String evaluateUrls(String text) {
        String baseURL, contextPath = "";
        if (Navigator.getNavigator() != null) {
            contextPath = Navigator.getNavigator().getExecutionContext().getRequest().getContextPath() + "/";
            baseURL = Navigator.getNavigator().getExecutionContext().getRequest().getScheme() + "://";
            baseURL = baseURL + Navigator.getNavigator().getExecutionContext().getRequest().getServerName();
            if (Navigator.getNavigator().getExecutionContext().getRequest().getServerPort() != 80 && Navigator.getNavigator().getExecutionContext().getRequest().getServerPort() != 443)
                baseURL = baseURL + ":" + Navigator.getNavigator().getExecutionContext().getRequest().getServerPort();
            baseURL = baseURL + contextPath;
        } else {
            baseURL = ConfigurationHelper.getConfiguration().getString("MAIL_BASE_URL");
            if (StringUtils.isNotEmpty(baseURL))
                if (baseURL.endsWith("/")) {
                    int beginIndexOfContextPath = baseURL.substring(0, baseURL.length() - 2).lastIndexOf("/");
                    if (beginIndexOfContextPath > 1)
                        contextPath = baseURL.substring(beginIndexOfContextPath);
                }
        }
        List<String> expressions = StringUtils.findNoRegEx(text, "<img", "/>");
        for (String expression : expressions) {
            if (StringUtils.isNotEmpty(contextPath))
                if (!expression.contains(baseURL)) {
                    String absoluteURL = StringUtils.replaceFirst(expression, contextPath, baseURL);
                    text = StringUtils.replaceAll(text, expression, absoluteURL);
                }
        }
        return text;
    }

    public static final Object getValue(String expression, IContext context, IResource resource) {
        ConnectorsFormulaContext connectorsFormulaContext = new ConnectorsFormulaContext(context.getUser(), resource, Modules.getWorkflowModule().getConfiguration(), null, context, null, context.getUser().getLanguage());
        return connectorsFormulaContext.getValue(expression);
    }

    public static final String evaluateExpression(String expression) {
        ContextHandler contextHandler = (ContextHandler)((InternalSiteExecutionContext) EzsToken.getSiteModule().getExecutionContext()).getInternalContextHandler();
        return contextHandler.evaluateExpressions(expression);
    }

    public static final <T> T evaluateFormulaWithEnglishLanguage(String expression, IContext context) {
        return evaluateFormulaWithEnglishLanguage(expression, context, (Map<String, Object>)null);
    }

    public static final <T> T evaluateFormula(String expression, IContext context) {
        return evaluateFormula(expression, context, (Map<String, Object>)null);
    }

    public static final <T> T evaluateFormulaWithEnglishLanguage(String expression, IContext context, Map<String, Object> parameters) {
        return evaluateFormula(expression, context, null, parameters, "en");
    }

    public static final <T> T evaluateFormula(String expression, IContext context, Map<String, Object> parameters) {
        return evaluateFormula(expression, context, null, parameters);
    }

    public static final <T> T evaluateFormula(String expression, IContext context, IResource resource) {
        return evaluateFormula(expression, context, resource, null);
    }

    public static final <T> T evaluateFormula(String expression, IContext context, IResource resource, Map<String, Object> parameters, String language) {
        ViewFormulaContext viewFormulaContext = null;
        Formula formula = new Formula(FormulaFunctionHelper.getSystemFormula(expression, language));
        if (resource instanceof IWorkflowInstance) {
            WorkflowInstanceFormulaContext workflowInstanceFormulaContext = new WorkflowInstanceFormulaContext(context.getUser(), (IWorkflowInstance)resource);
        } else if (resource instanceof com.axemble.vdoc.sdk.interfaces.IStorageResource) {
            ResourceFormulaContext resourceFormulaContext = new ResourceFormulaContext(context.getUser(), resource);
        } else if (resource instanceof com.axemble.vdoc.sdk.interfaces.ILinkedResource) {
            ResourceFormulaContext resourceFormulaContext = new ResourceFormulaContext(context.getUser(), resource);
        } else {
            viewFormulaContext = new ViewFormulaContext(context.getUser(), parameters);
        }
        Object result = formula.execute((FormulaObject)new FormulaResource((FormulaContext)viewFormulaContext));
        return (T) ObjectUtils.cast(result, result.getClass());
    }

    public static final <T> T evaluateFormula(String expression, IContext context, IResource resource, Map<String, Object> parameters) {
        return evaluateFormula(expression, context, resource, parameters, context.getLanguage());
    }
}
