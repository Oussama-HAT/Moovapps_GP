package com.moovapps.agent.auto.preventif;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IGroup;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.ITask;
import com.axemble.vdoc.sdk.interfaces.ITaskInstance;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowContainer;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.utils.Logger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

public class agentGenInterventionPreventive extends BaseAgent {
    protected void execute() {
        Logger LOG = Logger.getLogger(agentGenInterventionPreventive.class);
        Collection<? extends ILinkedResource> tableauDesArticles = null;
        Collection<? extends ILinkedResource> taches = null;
        ILinkedResource ilinkedResource = null;
        try {
            IContext context = getDirectoryModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "GestionParcAutomobile", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "ParcAutomobile", project);
            IWorkflowContainer workflowContainer = getWorkflowModule().getWorkflowContainer(context, catalog, "MaintenancePreventive");
            IWorkflow workflow = getWorkflowModule().getWorkflow(context, catalog, "MaintenancePreventive_1.0");
            Collection<? extends IWorkflowInstance> documents = getWorkflowModule().getWorkflowInstances(context, workflow);
            for (IWorkflowInstance document : documents) {
                if (document.getValue("DocumentState").equals("Applicable")) {
                    int Rappel = Math.round(((Float)document.getValue("FrequenceRappelJours")).floatValue());
                    int Frequence = Math.round(((Float)document.getValue("Frequence")).floatValue());
                    Date currentdate = new Date(System.currentTimeMillis());
                    Date dateDerniereIntervention = (Date)document.getValue("DateDerniereIntervention");
                    Calendar ddi = new GregorianCalendar();
                    ddi.setTime(dateDerniereIntervention);
                    ddi.add(5, Frequence);
                    Date dateProchaineIntervention = ddi.getTime();
                    long difference_In_Time = dateProchaineIntervention.getTime() - currentdate.getTime();
                    long difference_In_Days = difference_In_Time / 86400000L;
                    if (difference_In_Days <= Rappel) {
                        IWorkflowContainer destinationContainer = getWorkflowModule().getWorkflowContainer(context, catalog, "Entretien");
                        IWorkflow destinationWorkflow = getWorkflowModule().getWorkflow(context, catalog, "Entretien_1.0");
                        IGroup group = getDirectoryModule().getGroup(context, organization, "Responsable_Entretien");
                        IWorkflowInstance destinationDocument = getWorkflowModule().createWorkflowInstance(context, destinationWorkflow, null);
                        destinationDocument.setValue("EQUIPEMENT", document.getValue("EQUIPEMENT"));
                        destinationDocument.setValue("Gamme", document.getValue("Gamme"));
                        destinationDocument.setValue("DateDeGeneration", document.getValue("DateDerniereIntervention"));
                        tableauDesArticles = document.getLinkedResources("ListeDesArticles");
                        for (ILinkedResource linkedresource : tableauDesArticles) {
                            ilinkedResource = destinationDocument.createLinkedResource("ListeDesArticles");
                            ilinkedResource.setValue("Article", linkedresource.getValue("Article"));
                            ilinkedResource.setValue("Quantite", linkedresource.getValue("Quantite"));
                            ilinkedResource.setValue("Observation", linkedresource.getValue("Observation"));
                            ilinkedResource.save(context);
                            destinationDocument.addLinkedResource(ilinkedResource);
                        }
                        taches = document.getLinkedResources("Taches");
                        for (ILinkedResource linkedresource : taches) {
                            ilinkedResource = destinationDocument.createLinkedResource("Taches");
                            ilinkedResource.setValue("Designation", linkedresource.getValue("Designation"));
                            ilinkedResource.setValue("Cout", linkedresource.getValue("Cout"));
                            ilinkedResource.save(context);
                            destinationDocument.addLinkedResource(ilinkedResource);
                        }
                        destinationDocument.save(context);
                        ITask task = destinationDocument.getCurrentTaskInstance(context).getTask();
                        ITaskInstance taskinstance = destinationDocument.getCurrentTaskInstance(context);
                        IAction action = getWorkflowModule().getAction(context, task, "Generation");
                        getWorkflowModule().end(context, taskinstance, action, "Action système");
                        document.setValue("DateDerniereIntervention", dateProchaineIntervention);
                        document.save(context);
                        LOG.error("Hope");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
