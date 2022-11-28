package com.moovapps.gp.services;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.exceptions.SDKException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import java.security.SecureRandom;

public class DirectoryService {
    private static Logger LOG = Logger.getLogger(DirectoryService.class);

    public static final String defaultOrganizationName = "DefaultOrganization";

    public static final String internalOrganizationName = "Internal";

    public static IContext getSysAdminContext() {
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            return directoryModule.getContextByLogin("sysadmin");
        } finally {
            Modules.releaseModule((IModule)directoryModule);
        }
    }

    public static IContext getContextByUser(IUser user) {
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            return directoryModule.getContext(user);
        } finally {
            Modules.releaseModule((IModule)directoryModule);
        }
    }

    public static IContext getLoggedOnContext() {
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            return directoryModule.getLoggedOnUserContext();
        } finally {
            Modules.releaseModule((IModule)directoryModule);
        }
    }

    public static IOrganization getDefaultOrganization() throws DirectoryModuleException {
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            return directoryModule.getOrganization(getSysAdminContext(), "DefaultOrganization");
        } finally {
            Modules.releaseModule((IModule)directoryModule);
        }
    }
    public static IOrganization getOrganization(IDirectoryModule directoryModule , IWorkflowModule workflowModule) throws ModuleException
    {
        IConfiguration configuration = workflowModule.getConfiguration();
        String orgaName = "DefaultOrganization";
        IOrganization organization = directoryModule.getOrganization(getSysAdminContext(), orgaName);

        if (organization == null)
        {
            String message = directoryModule.getStaticString("LG_VISIATIV_TIMESHEET_COLLABORATORS_GROUP_ORGA_NULL", orgaName);
            throw new SDKException(message);
        }
        return organization;
    }

    public static IOrganization getInternalOrganization() throws DirectoryModuleException {
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            return directoryModule.getOrganization(getSysAdminContext(), "Internal");
        } finally {
            Modules.releaseModule((IModule)directoryModule);
        }
    }

    public static IGroup getGroupe(String groupeName) throws DirectoryModuleException {
        IDirectoryModule directoryModule = Modules.getDirectoryModule();
        try {
            return directoryModule.getGroup(getSysAdminContext(), getInternalOrganization(), groupeName);
        } finally {
            Modules.releaseModule((IModule)directoryModule);
        }
    }

    public static String generatePassword() {
        String a = "0123456789ABCDEFGHIJKLMNOPQRSTUWYZ";
        SecureRandom rnd = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 3; i++)
            password.append("0123456789ABCDEFGHIJKLMNOPQRSTUWYZ".charAt(rnd.nextInt("0123456789ABCDEFGHIJKLMNOPQRSTUWYZ".length())));
        return password.toString();
    }
}
