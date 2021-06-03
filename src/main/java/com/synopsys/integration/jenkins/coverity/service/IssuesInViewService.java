/*
 * synopsys-coverity
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.jenkins.coverity.service;

import java.io.IOException;

import com.synopsys.integration.coverity.api.rest.View;
import com.synopsys.integration.coverity.api.rest.ViewContents;
import com.synopsys.integration.coverity.api.ws.configuration.CovRemoteServiceException_Exception;
import com.synopsys.integration.coverity.api.ws.configuration.ProjectDataObj;
import com.synopsys.integration.coverity.ws.ConfigurationServiceWrapper;
import com.synopsys.integration.coverity.ws.WebServiceFactory;
import com.synopsys.integration.coverity.ws.view.ViewReportWrapper;
import com.synopsys.integration.coverity.ws.view.ViewService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jenkins.extensions.JenkinsIntLogger;

import hudson.AbortException;

public class IssuesInViewService {
    private final JenkinsIntLogger logger;
    private final CoverityConfigService coverityConfigService;

    public IssuesInViewService(JenkinsIntLogger logger, CoverityConfigService coverityConfigService) {
        this.logger = logger;
        this.coverityConfigService = coverityConfigService;
    }

    public ViewReportWrapper getIssuesInView(String coverityInstanceUrl, String credentialsId, String projectName, String viewName) throws IOException, IntegrationException, CovRemoteServiceException_Exception {
        WebServiceFactory webServiceFactory = coverityConfigService.getWebServiceFactoryFromUrl(coverityInstanceUrl, credentialsId);
        ConfigurationServiceWrapper configurationServiceWrapper = webServiceFactory.createConfigurationServiceWrapper();
        logger.alwaysLog(String.format("Checking for issues in project \"%s\", view \"%s\".", projectName, viewName));
        ProjectDataObj project = configurationServiceWrapper.getProjectByExactName(projectName)
                                           .orElseThrow(() -> new AbortException("Coverity Issues could not be retrieved: No project with name " + projectName + " could be found. "
                                                                                     + "It either does not exist or the credentials configured in the Jenkins system configuration are insufficient to access it."));
        ViewService viewService = webServiceFactory.createViewService();
        View view = viewService.getViewByExactName(viewName)
                              .orElseThrow(() -> new AbortException("Coverity Issues could not be retrieved: No view with name " + viewName + " could be found. "
                                                                        + "It either does not exist or the credentials configured in the Jenkins system configuration are insufficient to access it."));

        ViewContents viewContents = viewService.getViewContents(project, view, 1, 0);
        String viewReportUrl = viewService.getProjectViewReportUrl(project, view);
        return new ViewReportWrapper(viewContents, viewReportUrl);
    }



}