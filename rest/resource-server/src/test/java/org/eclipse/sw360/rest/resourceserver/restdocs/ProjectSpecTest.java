/*
 * Copyright Siemens AG, 2017-2018. Part of the SW360 Portal Project.
 *
  * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.rest.resourceserver.restdocs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.thrift.CycloneDxComponentType;
import org.eclipse.sw360.datahandler.thrift.MainlineState;
import org.eclipse.sw360.datahandler.thrift.ProjectReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.RequestSummary;
import org.eclipse.sw360.datahandler.thrift.Source;
import org.eclipse.sw360.datahandler.thrift.Visibility;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentUsage;
import org.eclipse.sw360.datahandler.thrift.attachments.CheckStatus;
import org.eclipse.sw360.datahandler.thrift.attachments.LicenseInfoUsage;
import org.eclipse.sw360.datahandler.thrift.attachments.UsageData;
import org.eclipse.sw360.datahandler.thrift.components.ClearingState;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.ComponentType;
import org.eclipse.sw360.datahandler.thrift.components.ECCStatus;
import org.eclipse.sw360.datahandler.thrift.components.EccInformation;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoFile;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.OutputFormatInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.OutputFormatVariant;
import org.eclipse.sw360.datahandler.thrift.packages.Package;
import org.eclipse.sw360.datahandler.thrift.packages.PackageManager;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectClearingState;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectProjectRelationship;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectRelationship;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectState;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectType;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.ProjectVulnerabilityRating;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.ReleaseVulnerabilityRelation;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityCheckStatus;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityDTO;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityRatingForProject;
import org.eclipse.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import org.eclipse.sw360.rest.resourceserver.Sw360ResourceServer;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.attachment.Sw360AttachmentService;
import org.eclipse.sw360.rest.resourceserver.licenseinfo.Sw360LicenseInfoService;
import org.eclipse.sw360.rest.resourceserver.packages.SW360PackageService;
import org.eclipse.sw360.rest.resourceserver.project.Sw360ProjectService;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
import org.eclipse.sw360.rest.resourceserver.report.SW360ReportService;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.eclipse.sw360.rest.resourceserver.vulnerability.Sw360VulnerabilityService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.sw360.datahandler.thrift.MainlineState.MAINLINE;
import static org.eclipse.sw360.datahandler.thrift.MainlineState.OPEN;
import static org.eclipse.sw360.datahandler.thrift.ReleaseRelationship.CONTAINED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectSpecTest extends TestRestDocsSpecBase {

    @Value("${sw360.test-user-id}")
    private String testUserId;

    @Value("${sw360.test-user-password}")
    private String testUserPassword;

    @MockBean
    private Sw360UserService userServiceMock;

    @MockBean
    private SW360PackageService packageServiceMock;

    @MockBean
    private Sw360ProjectService projectServiceMock;

    @MockBean
    private Sw360ReleaseService releaseServiceMock;

    @MockBean
    private Sw360AttachmentService attachmentServiceMock;

    @MockBean
    private Sw360LicenseInfoService licenseInfoMockService;

    @MockBean
    private Sw360VulnerabilityService vulnerabilityMockService;

    @MockBean
    private SW360ReportService sw360ReportServiceMock;

    private Project project;
    private Set<Project> projectList = new HashSet<>();
    private Attachment attachment;

    @Before
    public void before() throws TException, IOException {
        Set<Attachment> attachmentList = new HashSet<>();
        List<EntityModel<Attachment>> attachmentResources = new ArrayList<>();
        attachment = new Attachment("1231231254", "spring-core-4.3.4.RELEASE.jar");
        attachment.setSha1("da373e491d3863477568896089ee9457bc316783");
        attachmentList.add(attachment);
        attachmentResources.add(EntityModel.of(attachment));

        Set<Attachment> setOfAttachment = new HashSet<Attachment>();
        Attachment att1 = new Attachment("1234", "test.zip").setAttachmentType(AttachmentType.SOURCE)
                .setCreatedBy("user@sw360.org").setSha1("da373e491d312365483589ee9457bc316783").setCreatedOn("2021-04-27")
                .setCreatedTeam("DEPARTMENT");
        Attachment att2 = att1.deepCopy().setAttachmentType(AttachmentType.BINARY).setCreatedComment("Created Comment")
                .setCheckStatus(CheckStatus.ACCEPTED).setCheckedComment("Checked Comment").setCheckedOn("2021-04-27")
                .setCheckedBy("admin@sw360.org").setCheckedTeam("DEPARTMENT1");

        given(this.attachmentServiceMock.getAttachmentContent(any())).willReturn(new AttachmentContent().setId("1231231254").setFilename("spring-core-4.3.4.RELEASE.jar").setContentType("binary"));
        given(this.attachmentServiceMock.getResourcesFromList(any())).willReturn(CollectionModel.of(attachmentResources));
        given(this.attachmentServiceMock.uploadAttachment(any(), any(), any())).willReturn(attachment);
        given(this.attachmentServiceMock.updateAttachment(any(), any(), any(), any())).willReturn(att2);
        Mockito.doNothing().when(projectServiceMock).copyLinkedObligationsForClonedProject(any(), any(),
                any());

        Map<String, ProjectReleaseRelationship> linkedReleases = new HashMap<>();
        Map<String, ProjectProjectRelationship> linkedProjects = new HashMap<>();
        ProjectReleaseRelationship projectReleaseRelationship = new ProjectReleaseRelationship(CONTAINED, MAINLINE)
                .setComment("Test Comment").setCreatedOn("2020-08-05").setCreatedBy("admin@sw360.org");
        ProjectReleaseRelationship projectReleaseRelationshipResponseBody = projectReleaseRelationship.deepCopy()
                .setComment("Test Comment").setMainlineState(MainlineState.SPECIFIC)
                .setReleaseRelation(ReleaseRelationship.STANDALONE);
        Map<String, String> externalIds = new HashMap<>();
        externalIds.put("portal-id", "13319-XX3");
        externalIds.put("project-ext", "515432");
        externalIds.put("ws-project-token", "[\"490389ac-0269-4719-9cbf-fb5e299c8415\",\"3892f1db-4361-4e83-a89d-d28a262d65b9\"]");

        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("OSPO-Comment", "Some Comment");

        setOfAttachment.add(att1);
        Project projectForAtt = new Project();
        projectForAtt.setAttachments(setOfAttachment);
        projectForAtt.setId("98745");
        projectForAtt.setName("Test Project");
        projectForAtt.setProjectType(ProjectType.PRODUCT);
        projectForAtt.setVersion("1");
        projectForAtt.setCreatedOn("2021-04-27");
        projectForAtt.setCreatedBy("admin@sw360.org");

        Set<String> licenseIds = new HashSet<>();
        licenseIds.add("MIT");
        licenseIds.add("GPL");

        Package package1 = new Package("angular-sanitize", "1.8.2", "pkg:npm/angular-sanitize@1.8.2", CycloneDxComponentType.FRAMEWORK)
                .setId("122357345")
                .setCreatedBy("admin@sw360.org")
                .setCreatedOn("2023-01-02")
                .setVcs("git+https://github.com/angular/angular.js.git")
                .setHomepageUrl("http://angularjs.org")
                .setLicenseIds(licenseIds)
                .setReleaseId("12345678")
                .setPackageManager(PackageManager.NPM)
                .setDescription("Sanitizes an html string by stripping all potentially dangerous tokens.");

        Package package2 = new Package()
                .setId("875689754")
                .setName("applicationinsights-web")
                .setVersion("2.5.11")
                .setCreatedBy("user@sw360.org")
                .setCreatedOn("2023-02-02")
                .setPurl("pkg:npm/@microsoft/applicationinsights-web@2.5.11")
                .setPackageManager(PackageManager.NPM)
                .setPackageType(CycloneDxComponentType.LIBRARY)
                .setVcs("git+https://github.com/microsoft/ApplicationInsights-JS.git")
                .setHomepageUrl("https://github.com/microsoft/ApplicationInsights-JS#readme")
                .setDescription("Application Insights is an extension of Azure Monitor and provides application performance monitoring (APM) features");

        given(this.packageServiceMock.getPackageForUserById(eq(package1.getId()))).willReturn(package1);
        given(this.packageServiceMock.getPackageForUserById(eq(package2.getId()))).willReturn(package2);

        Set<String> linkedPackages = new HashSet<>();
        linkedPackages.add(package1.getId());
        linkedPackages.add(package2.getId());

        List<Project> projectListByName = new ArrayList<>();
        Set<Project> usedByProjectList = new HashSet<>();
        project = new Project();
        project.setId("376576");
        project.setName("Emerald Web");
        project.setProjectType(ProjectType.PRODUCT);
        project.setVersion("1.0.2");
        project.setDescription("Emerald Web provides a suite of components for Critical Infrastructures.");
        project.setDomain("Hardware");
        project.setCreatedOn("2016-12-15");
        project.setCreatedBy("admin@sw360.org");
        project.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        project.setBusinessUnit("sw360 AR");
        project.setExternalIds(Collections.singletonMap("mainline-id-project", "515432"));
        project.setOwnerAccountingUnit("4822");
        project.setOwnerCountry("DE");
        project.setDeliveryStart("2018-05-01");
        project.setOwnerGroup("AA BB 123 GHV2-DE");
        project.setTag("project test tag 1");
        project.setPreevaluationDeadline("2018-07-17");
        project.setSystemTestStart("2017-01-01");
        project.setSystemTestEnd("2018-03-01");
        project.setObligationsText("Lorem Ipsum");
        project.setClearingSummary("Lorem Ipsum");
        project.setSpecialRisksOSS("Lorem Ipsum");
        project.setGeneralRisks3rdParty("Lorem Ipsum");
        project.setSpecialRisks3rdParty("Lorem Ipsum");
        project.setLicenseInfoHeaderText("Lorem Ipsum");
        project.setDeliveryChannels("Lorem Ipsum");
        project.setVendor(new Vendor());
        project.setRemarksAdditionalRequirements("Lorem Ipsum");
        ReleaseClearingStateSummary clearingCount = new ReleaseClearingStateSummary();
        clearingCount.newRelease = 2;
        clearingCount.sentToClearingTool = 1;
        clearingCount.underClearing = 0;
        clearingCount.reportAvailable = 0;
        clearingCount.scanAvailable = 0;
        clearingCount.internalUseScanAvailable = 1;
        clearingCount.approved = 2;
        project.setReleaseClearingStateSummary(clearingCount);
        linkedReleases.put("3765276512", projectReleaseRelationship);
        project.setReleaseIdToUsage(linkedReleases);
        linkedProjects.put("376570", new ProjectProjectRelationship(ProjectRelationship.CONTAINED).setEnableSvm(true));
        project.setLinkedProjects(linkedProjects);
        project.setPackageIds(linkedPackages);
        project.setAttachments(attachmentList);
        project.setSecurityResponsibles(new HashSet<>(Arrays.asList("securityresponsible1@sw360.org", "securityresponsible2@sw360.org")));
        project.setProjectResponsible("projectresponsible@sw360.org");
        project.setExternalIds(externalIds);
        project.setAdditionalData(additionalData);
        project.setPhaseOutSince("2020-06-24");
        project.setClearingRequestId("CR-1");
        Map<String, String> externalURLs1 = new HashMap<>();
        externalURLs1.put("homepage", "http://test_wiki_url.com");
        externalURLs1.put("wiki", "http://test_wiki_url.com");
        project.setExternalUrls(externalURLs1);
        project.setReleaseRelationNetwork("[{\"comment\":\"\",\"releaseLink\":[],\"createBy\":\"admin@sw360.org\",\"createOn\":\"2022-08-15\",\"mainlineState\":\"OPEN\",\"releaseId\":\"3765276512\",\"releaseRelationship\":\"CONTAINED\"}]");

        projectListByName.add(project);
        projectList.add(project);
        usedByProjectList.add(project);


        Map<String, String> externalIds2 = new HashMap<>();
        externalIds2.put("project-ext", "7657");

        Project project2 = new Project();
        project2.setId("376570");
        project2.setName("Orange Web");
        project2.setVersion("2.0.1");
        project2.setProjectType(ProjectType.PRODUCT);
        project2.setDescription("Orange Web provides a suite of components for documentation.");
        project.setDomain("Hardware");
        project2.setCreatedOn("2016-12-17");
        project2.setCreatedBy("john@sw360.org");
        project2.setBusinessUnit("sw360 EX DF");
        project2.setOwnerAccountingUnit("5661");
        project2.setOwnerCountry("FR");
        project2.setDeliveryStart("2018-05-01");
        project2.setOwnerGroup("SIM-KA12");
        project2.setTag("project test tag 2");
        project2.setPreevaluationDeadline("2018-07-17");
        project2.setSystemTestStart("2017-01-01");
        project2.setSystemTestEnd("2018-03-01");
        project2.setObligationsText("Lorem Ipsum");
        project2.setClearingSummary("Lorem Ipsum");
        project2.setSpecialRisksOSS("Lorem Ipsum");
        project2.setGeneralRisks3rdParty("Lorem Ipsum");
        project2.setSpecialRisks3rdParty("Lorem Ipsum");
        project2.setDeliveryChannels("Lorem Ipsum");
        project2.setRemarksAdditionalRequirements("Lorem Ipsum");
        project2.setLicenseInfoHeaderText("Lorem Ipsum");
        project2.setVendor(new Vendor());
        project2.setSecurityResponsibles(new HashSet<>(Arrays.asList("securityresponsible1@sw360.org", "securityresponsible2@sw360.org")));
        project2.setProjectResponsible("projectresponsible@sw360.org");
        Map<String, String> projExtKeys = new HashMap<String, String>();
        projExtKeys.put("mainline-id-project", "7657");
        projExtKeys.put("portal-id", "13319-XX3");
        project2.setExternalIds(projExtKeys);
        linkedReleases = new HashMap<>();
        linkedReleases.put("5578999", projectReleaseRelationship);
        project2.setReleaseIdToUsage(linkedReleases);
        project2.setExternalIds(externalIds2);
        project2.setPackageIds(linkedPackages);
        Map<String, String> externalURLs = new HashMap<>();
        externalURLs.put("homepage", "http://test_wiki_url.com");
        externalURLs.put("wiki", "http://test_wiki_url.com");
        project2.setExternalUrls(externalURLs);
        project2.setPhaseOutSince("2020-06-02");
        project2.setClearingTeam("Unknown");
        project2.setContributors(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        project2.setClearingRequestId("CR-2");

        projectList.add(project2);

        Map<String, ProjectReleaseRelationship> linkedReleases2 = new HashMap<>();
        Map<String, ProjectProjectRelationship> linkedProjects2 = new HashMap<>();
        Map<String, ProjectProjectRelationship> linkedProjects3 = new HashMap<>();
        Project project4 = new Project();
        project4.setId("12345");
        project4.setName("dummy");
        project4.setVersion("2.0.1");
        project4.setProjectType(ProjectType.PRODUCT);
        project4.setState(ProjectState.ACTIVE);
        project4.setClearingState(ProjectClearingState.OPEN);
        linkedProjects2.put("123456", new ProjectProjectRelationship(ProjectRelationship.CONTAINED).setEnableSvm(true));
        project4.setLinkedProjects(linkedProjects2);
        project4.setSecurityResponsibles(new HashSet<>(Arrays.asList("securityresponsible1@sw360.org", "securityresponsible2@sw360.org")));

        Project project5 = new Project();
        project5.setId("123456");
        project5.setName("dummy2");
        project5.setVersion("2.0.1");
        project5.setProjectType(ProjectType.PRODUCT);
        project5.setState(ProjectState.ACTIVE);
        project5.setClearingState(ProjectClearingState.OPEN);
        linkedReleases2.put("37652765121", projectReleaseRelationship);
        project5.setReleaseIdToUsage(linkedReleases2);
        linkedProjects3.put("1234567", new ProjectProjectRelationship(ProjectRelationship.CONTAINED).setEnableSvm(true));
        project5.setLinkedProjects(linkedProjects3);
        project5.setSecurityResponsibles(new HashSet<>(Arrays.asList("securityresponsible1@sw360.org", "securityresponsible2@sw360.org")));

        Project project6 = new Project();
        project6.setId("1234567");
        project6.setName("dummy3");
        project6.setVersion("3.0.1");
        project6.setProjectType(ProjectType.PRODUCT);
        project6.setState(ProjectState.ACTIVE);
        project6.setClearingState(ProjectClearingState.OPEN);
        project6.setSecurityResponsibles(new HashSet<>(Arrays.asList("securityresponsible1@sw360.org", "securityresponsible2@sw360.org")));

        Release release5 = new Release();
        release5.setId("37652765121");
        release5.setName("Angular 2.3.1");
        release5.setCpeid("cpe:/a:Google:Angular:2.3.1:");
        release5.setReleaseDate("2016-12-17");
        release5.setVersion("2.3.1");
        release5.setCreatedOn("2016-12-28");

        Set<String> releaseIds = new HashSet<>(Collections.singletonList("3765276512"));
        Set<String> releaseIdsTransitive = new HashSet<>(Arrays.asList("3765276512", "5578999"));

        Attachment SPDXAttachment = new Attachment("3331231254", "bom.spdx.rdf");
        SPDXAttachment.setSha1("df903e491d3863477568896089ee9457bc316183");
        SPDXAttachment.setAttachmentType(AttachmentType.SBOM);
        Set<Attachment> spdxSet = new HashSet<>();
        spdxSet.add(SPDXAttachment);

        Attachment CycloneDXAttachment = new Attachment("3331111231254", "sampleBOM.xml");
        CycloneDXAttachment.setSha1("df3e491d3863477568896089ee9457bc316183");
        CycloneDXAttachment.setAttachmentType(AttachmentType.SBOM);
        Set<Attachment> cyclonedxSet = new HashSet<>();
        cyclonedxSet.add(CycloneDXAttachment);

        Project SPDXProject = new Project();
        SPDXProject.setId("333655");
        SPDXProject.setName("Green Web");
        SPDXProject.setVersion("1.0.1");
        SPDXProject.setCreatedOn("2022-11-13");
        SPDXProject.setBusinessUnit("sw360 BA");
        SPDXProject.setState(ProjectState.ACTIVE);
        SPDXProject.setClearingState(ProjectClearingState.OPEN);
        SPDXProject.setProjectType(ProjectType.PRODUCT);
        SPDXProject.setCreatedBy("admin@sw360.org");
        SPDXProject.setAttachments(spdxSet);

        Project cycloneDXProject = new Project();
        cycloneDXProject.setId("3336565435");
        cycloneDXProject.setName("Azure Web");
        cycloneDXProject.setVersion("1.0.2");
        cycloneDXProject.setCreatedOn("2022-11-13");
        cycloneDXProject.setBusinessUnit("sw360 BA");
        cycloneDXProject.setState(ProjectState.ACTIVE);
        cycloneDXProject.setClearingState(ProjectClearingState.OPEN);
        cycloneDXProject.setProjectType(ProjectType.PRODUCT);
        cycloneDXProject.setCreatedBy("admin@sw360.org");
        cycloneDXProject.setAttachments(cyclonedxSet);
        cycloneDXProject.setPackageIds(linkedPackages);

        RequestSummary requestSummaryForSPDX = new RequestSummary();
        requestSummaryForSPDX.setMessage(SPDXProject.getId());
        requestSummaryForSPDX.setRequestStatus(RequestStatus.SUCCESS);

        RequestSummary requestSummaryForCycloneDX = new RequestSummary();
        requestSummaryForCycloneDX.setMessage("{\"projectId\":\"" + cycloneDXProject.getId() + "\"}");

        given(this.projectServiceMock.importSPDX(any(),any())).willReturn(requestSummaryForSPDX);
        given(this.projectServiceMock.importCycloneDX(any(),any(),any())).willReturn(requestSummaryForCycloneDX);
        given(this.sw360ReportServiceMock.getProjectBuffer(any(),anyBoolean())).willReturn(ByteBuffer.allocate(10000));
        given(this.projectServiceMock.getProjectsForUser(any(), any())).willReturn(projectList);
        given(this.projectServiceMock.getProjectForUserById(eq(project.getId()), any())).willReturn(project);
        given(this.projectServiceMock.getProjectForUserById(eq(project2.getId()), any())).willReturn(project2);
        given(this.projectServiceMock.getProjectForUserById(eq(project4.getId()), any())).willReturn(project4);
        given(this.projectServiceMock.getProjectForUserById(eq(project5.getId()), any())).willReturn(project5);
        given(this.projectServiceMock.getProjectForUserById(eq(project6.getId()), any())).willReturn(project6);
        given(this.projectServiceMock.getProjectForUserById(eq(projectForAtt.getId()), any())).willReturn(projectForAtt);
        given(this.projectServiceMock.getProjectForUserById(eq(SPDXProject.getId()), any())).willReturn(SPDXProject);
        given(this.projectServiceMock.getProjectForUserById(eq(cycloneDXProject.getId()), any())).willReturn(cycloneDXProject);
        given(this.projectServiceMock.searchLinkingProjects(eq(project.getId()), any())).willReturn(usedByProjectList);
        given(this.projectServiceMock.searchProjectByName(eq(project.getName()), any())).willReturn(projectListByName);
        given(this.projectServiceMock.searchProjectByTag(any(), any())).willReturn(new ArrayList<Project>(projectList));
        given(this.projectServiceMock.searchProjectByType(any(), any())).willReturn(new ArrayList<Project>(projectList));
        given(this.projectServiceMock.searchProjectByGroup(any(), any())).willReturn(new ArrayList<Project>(projectList));
        given(this.projectServiceMock.refineSearch(any(), any())).willReturn(projectListByName);
        given(this.projectServiceMock.getReleaseIds(eq(project.getId()), any(), eq(false))).willReturn(releaseIds);
        given(this.projectServiceMock.getReleaseIds(eq(project.getId()), any(), eq(true))).willReturn(releaseIdsTransitive);
        given(this.projectServiceMock.deleteProject(eq(project.getId()), any())).willReturn(RequestStatus.SUCCESS);
        given(this.projectServiceMock.updateProjectReleaseRelationship(any(), any(), any())).willReturn(projectReleaseRelationshipResponseBody);
        given(this.projectServiceMock.getClearingInfo(eq(project), any())).willReturn(project);
        given(this.projectServiceMock.convertToEmbeddedWithExternalIds(eq(project))).willReturn(
                new Project("Emerald Web")
                        .setVersion("1.0.2")
                        .setId("376576")
                        .setDescription("Emerald Web provides a suite of components for Critical Infrastructures.")
                        .setExternalIds(Collections.singletonMap("mainline-id-project", "515432"))
                        .setProjectType(ProjectType.PRODUCT));
        given(this.projectServiceMock.convertToEmbeddedWithExternalIds(eq(project2))).willReturn(
                new Project("Orange Web")
                        .setVersion("2.0.1")
                        .setId("376570")
                        .setDescription("Orange Web provides a suite of components for documentation.")
                        .setExternalIds(projExtKeys)
                        .setProjectType(ProjectType.PRODUCT)
                        .setVendor((new  Vendor("Test", "Test short", "http://testvendoraddress.com").setId("987567468"))));
        when(this.projectServiceMock.createProject(any(), any())).then(invocation ->
                new Project("Test Project")
                        .setId("1234567890")
                        .setDescription("This is the description of my Test Project")
                        .setProjectType(ProjectType.PRODUCT)
                        .setVersion("1.0")
                        .setCreatedBy("admin@sw360.org")
                        .setPhaseOutSince("2020-06-25")
                        .setState(ProjectState.ACTIVE)
                        .setCreatedOn(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .setVendor((new  Vendor("Test", "Test short", "http://testvendoraddress.com").setId("987567468"))));
        given(this.projectServiceMock.getMyProjects(any(), any())).willReturn(new ArrayList<>(projectList));
        given(this.projectServiceMock.getWithFilledClearingStatus(
                eq(new ArrayList<>(projectList)), eq(ImmutableMap.<String, Boolean>builder()
                        .put(ProjectClearingState.OPEN.toString(), true)
                        .put(ProjectClearingState.CLOSED.toString(), true)
                        .put(ProjectClearingState.IN_PROGRESS.toString(), true)
                        .build()))).willReturn(new ArrayList<>(projectList));

        Release release = new Release();
        release.setId("3765276512");
        release.setName("Angular 2.3.0");
        release.setCpeid("cpe:/a:Google:Angular:2.3.0:");
        release.setReleaseDate("2016-12-07");
        release.setVersion("2.3.0");
        release.setCreatedOn("2016-12-18");
        Component compo = new Component();
        compo.setComponentType(ComponentType.OSS);
        release.setComponentType(compo.getComponentType());
        EccInformation eccInformation = new EccInformation();
        eccInformation.setEccStatus(ECCStatus.APPROVED);
        release.setEccInformation(eccInformation);
        release.setCreatedBy("admin@sw360.org");
        release.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        release.setComponentId("12356115");
        release.setClearingState(ClearingState.APPROVED);
        release.setExternalIds(Collections.singletonMap("mainline-id-component", "1432"));
        release.setPackageIds(linkedPackages);
        release.setMainlineState(MainlineState.MAINLINE);
        release.setMainLicenseIds(new HashSet<>(Arrays.asList("GPL-2.0-or-later", "Apache-2.0")));
        release.setOtherLicenseIds(new HashSet<>(Arrays.asList("LGPL-2.0")));
        release.setAttachments(setOfAttachment);

        Release release2 = new Release();
        release2.setId("5578999");
        release2.setName("Spring 1.4.0");
        release2.setCpeid("cpe:/a:Spring:1.4.0:");
        release2.setReleaseDate("2017-05-06");
        release2.setVersion("1.4.0");
        release2.setCreatedOn("2017-11-19");
        eccInformation.setEccStatus(ECCStatus.APPROVED);
        release2.setEccInformation(eccInformation);
        release2.setCreatedBy("admin@sw360.org");
        release2.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        release2.setComponentId("12356115");
        release2.setClearingState(ClearingState.APPROVED);
        release2.setExternalIds(Collections.singletonMap("mainline-id-component", "1771"));
        release2.setComponentType(ComponentType.COTS);
        release2.setMainlineState(MainlineState.MAINLINE);
        release2.setMainLicenseIds(new HashSet<>(Arrays.asList("Apache-2.0")));
        release2.setOtherLicenseIds(new HashSet<>(Arrays.asList("GPL-2.0")));
        release2.setAttachments(setOfAttachment);

        Release rel = new Release();
        Map<String, String> releaseExternalIds = new HashMap<>();
        releaseExternalIds.put("mainline-id-component", "1432");
        releaseExternalIds.put("ws-component-id", "[\"2365\",\"5487923\"]");

        rel.setId("3765276512");
        rel.setName("Spring Core 4.3.4");
        rel.setCpeid("cpe:/a:pivotal:spring-core:4.3.4:");
        rel.setReleaseDate("2016-12-07");
        rel.setVersion("4.3.4");
        rel.setCreatedOn("2016-12-18");
        rel.setCreatedBy("admin@sw360.org");
        rel.setSourceCodeDownloadurl("http://www.google.com");
        rel.setBinaryDownloadurl("http://www.google.com/binaries");
        rel.setComponentId("17653524");
        rel.setComponentType(ComponentType.OSS);
        rel.setClearingState(ClearingState.APPROVED);
        rel.setExternalIds(releaseExternalIds);
        rel.setAdditionalData(Collections.singletonMap("Key", "Value"));
        rel.setLanguages(new HashSet<>(Arrays.asList("C++", "Java")));
        rel.setMainLicenseIds(new HashSet<>(Arrays.asList("GPL-2.0-or-later", "Apache-2.0")));
        rel.setOperatingSystems(ImmutableSet.of("Windows", "Linux"));
        rel.setSoftwarePlatforms(new HashSet<>(Arrays.asList("Java SE", ".NET")));
        rel.setMainlineState(MainlineState.MAINLINE);
        rel.setVendor(new Vendor("TV", "Test Vendor", "http://testvendor.com"));
        rel.setPackageIds(linkedPackages);

        given(this.releaseServiceMock.getReleaseForUserById(eq(release.getId()), any())).willReturn(release);
        given(this.releaseServiceMock.getReleaseForUserById(eq(release2.getId()), any())).willReturn(release2);

        given(this.userServiceMock.getUserByEmailOrExternalId("admin@sw360.org")).willReturn(
                new User("admin@sw360.org", "sw360").setId("123456789"));
        given(this.userServiceMock.getUserByEmail("admin@sw360.org")).willReturn(
                new User("admin@sw360.org", "sw360").setId("123456789"));
        given(this.userServiceMock.getUserByEmail("jane@sw360.org")).willReturn(
                new User("jane@sw360.org", "sw360").setId("209582812"));
        OutputFormatInfo outputFormatInfo = new OutputFormatInfo();
        outputFormatInfo.setFileExtension("html");
        given(this.licenseInfoMockService.getOutputFormatInfoForGeneratorClass(any()))
                .willReturn(outputFormatInfo);
        LicenseInfoFile licenseInfoFile = new LicenseInfoFile();
        licenseInfoFile.setGeneratedOutput(new byte[0]);
        given(this.licenseInfoMockService.getLicenseInfoFile(any(), any(), any(), any(),
                any(),any(), any())).willReturn(licenseInfoFile);

        Source ownerSrc1 = Source.releaseId("9988776655");
        Source usedBySrc = Source.projectId(project.getId());
        LicenseInfoUsage licenseInfoUsage1 = new LicenseInfoUsage(new HashSet<>());
        licenseInfoUsage1.setProjectPath("11223344:22334455");
        licenseInfoUsage1.setExcludedLicenseIds(Sets.newHashSet("22334455", "9988776655"));
        licenseInfoUsage1.setIncludeConcludedLicense(false);
        UsageData usageData1 = new UsageData();
        usageData1.setLicenseInfo(licenseInfoUsage1);
        AttachmentUsage attachmentUsage1 = new AttachmentUsage(ownerSrc1, "aa1122334455bbcc", usedBySrc);
        attachmentUsage1.setUsageData(usageData1);
        attachmentUsage1.setId("11223344889977");

        Source ownerSrc2 = Source.releaseId("5566778899");
        LicenseInfoUsage licenseInfoUsage2 = new LicenseInfoUsage(new HashSet<>());
        licenseInfoUsage2.setProjectPath("11223344:22334455:445566778899");
        licenseInfoUsage2.setExcludedLicenseIds(Sets.newHashSet("44553322", "5566778899"));
        licenseInfoUsage2.setIncludeConcludedLicense(true);
        UsageData usageData2 = new UsageData();
        usageData2.setLicenseInfo(licenseInfoUsage2);
        AttachmentUsage attachmentUsage2 = new AttachmentUsage(ownerSrc2, "aa1122334455ee", usedBySrc);
        attachmentUsage2.setUsageData(usageData2);
        attachmentUsage2.setId("8899776655");

        List<AttachmentUsage> attachmentUsageList = new ArrayList<>();
        attachmentUsageList.add(attachmentUsage1);
        attachmentUsageList.add(attachmentUsage2);
        given(this.attachmentServiceMock.getAllAttachmentUsage(any())).willReturn(attachmentUsageList);
        List<VulnerabilityDTO> vulDtos = new ArrayList<VulnerabilityDTO>();
        VulnerabilityDTO vulDto = new VulnerabilityDTO();
        vulDto.setComment("Lorem Ipsum");
        vulDto.setExternalId("12345");
        vulDto.setProjectRelevance("IRRELEVANT");
        vulDto.setIntReleaseId("21055");
        vulDto.setIntReleaseName("Angular 2.3.0");
        vulDto.setAction("Update to Fixed Version");
        vulDto.setPriority("2 - major");
        vulDto.setTitle("title");
        ReleaseVulnerabilityRelation relation = new ReleaseVulnerabilityRelation();
        relation.setReleaseId("3765276512");
        relation.setVulnerabilityId("1333333333");
        relation.setMatchedBy("matchedBy");
        relation.setUsedNeedle("usedNeedle");
        vulDto.setReleaseVulnerabilityRelation(relation);
        vulDtos.add(vulDto);
        VulnerabilityDTO vulDto1 = new VulnerabilityDTO();
        vulDto1.setComment("Lorem Ipsum");
        vulDto1.setExternalId("23105");
        vulDto1.setProjectRelevance("APPLICABLE");
        vulDto1.setIntReleaseId("21055");
        vulDto1.setIntReleaseName("Angular 2.3.0");
        vulDto1.setAction("Update to Fixed Version");
        vulDto1.setPriority("1 - critical");
        vulDtos.add(vulDto1);
        given(this.vulnerabilityMockService.getVulnerabilitiesByProjectId(any(), any())).willReturn(vulDtos);
        VulnerabilityCheckStatus vulnCheckStatus = new VulnerabilityCheckStatus();
        vulnCheckStatus.setCheckedBy("admin@sw360.org");
        vulnCheckStatus.setCheckedOn(SW360Utils.getCreatedOn());
        vulnCheckStatus.setVulnerabilityRating(VulnerabilityRatingForProject.IRRELEVANT);
        vulnCheckStatus.setComment("Lorem Ipsum");
        vulnCheckStatus.setProjectAction("Lorem Ipsum");

        VulnerabilityCheckStatus vulnCheckStatus1 = new VulnerabilityCheckStatus();
        vulnCheckStatus1.setCheckedBy("admin@sw360.org");
        vulnCheckStatus1.setCheckedOn(SW360Utils.getCreatedOn());
        vulnCheckStatus1.setVulnerabilityRating(VulnerabilityRatingForProject.APPLICABLE);
        vulnCheckStatus1.setComment("Lorem Ipsum");
        vulnCheckStatus1.setProjectAction("Lorem Ipsum");

        List<VulnerabilityCheckStatus> vulCheckStatuses = new ArrayList<VulnerabilityCheckStatus>();
        vulCheckStatuses.add(vulnCheckStatus);
        vulCheckStatuses.add(vulnCheckStatus1);

        Map<String, List<VulnerabilityCheckStatus>> releaseIdToStatus = new HashMap<String, List<VulnerabilityCheckStatus>>();
        releaseIdToStatus.put("21055", vulCheckStatuses);

        ProjectVulnerabilityRating projVulnRating = new ProjectVulnerabilityRating();
        projVulnRating.setProjectId(project.getId());
        Map<String, Map<String, List<VulnerabilityCheckStatus>>> vulnerabilityIdToReleaseIdToStatus = new HashMap<String, Map<String, List<VulnerabilityCheckStatus>>>();
        vulnerabilityIdToReleaseIdToStatus.put("12345", releaseIdToStatus);
        vulnerabilityIdToReleaseIdToStatus.put("23105", releaseIdToStatus);
        projVulnRating.setVulnerabilityIdToReleaseIdToStatus(vulnerabilityIdToReleaseIdToStatus);
        List<ProjectVulnerabilityRating> projVulnRatings = new ArrayList<ProjectVulnerabilityRating>();
        projVulnRatings.add(projVulnRating);
        given(this.vulnerabilityMockService.getProjectVulnerabilityRatingByProjectId(any(), any())).willReturn(projVulnRatings);

        Map<String, VulnerabilityRatingForProject> relIdToprojVUlRating = new HashMap<String, VulnerabilityRatingForProject>();
        relIdToprojVUlRating.put("21055", VulnerabilityRatingForProject.IRRELEVANT);

        Map<String, VulnerabilityRatingForProject> relIdToprojVUlRating1 = new HashMap<String, VulnerabilityRatingForProject>();
        relIdToprojVUlRating1.put("21055", VulnerabilityRatingForProject.APPLICABLE);

        Map<String, Map<String, VulnerabilityRatingForProject>> vulIdToRelIdToRatings = new HashMap<String, Map<String, VulnerabilityRatingForProject>>();
        vulIdToRelIdToRatings.put("12345", relIdToprojVUlRating);
        vulIdToRelIdToRatings.put("23105", relIdToprojVUlRating1);

        given(this.vulnerabilityMockService.fillVulnerabilityMetadata(any(), any())).willReturn(vulIdToRelIdToRatings);
        given(this.vulnerabilityMockService.updateProjectVulnerabilityRating(any(), any())).willReturn(RequestStatus.SUCCESS);
        given(this.projectServiceMock.getReleasesFromProjectIds(any(), anyBoolean(), any(), any())).willReturn(Set.of(rel));
    }

    @Test
    public void should_document_get_projects() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_usedbyresource_for_project() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/usedBy/" + project.getId())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]businessUnit").description("The business unit this project belongs to"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_attachment_usage_for_project() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/attachmentUsage")
                .header("Authorization", "Bearer " + accessToken).accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(responseFields(
                        subsectionWithPath("releaseIdToUsage").description("The relationship between linked releases of the project"),
                        subsectionWithPath("linkedProjects").description("The linked projects"),
                        subsectionWithPath("_embedded.sw360:release").description("An array of linked releases"),
                        subsectionWithPath("_embedded.sw360:attachmentUsages").description("An array of project's attachment usages"))));
    }

    @Test
    public void should_document_get_projects_with_all_details() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("allDetails", "true")
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("allDetails").description("Flag to get projects with all details. Possible values are `<true|false>`"),
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]createdOn").description("The date the project was created"),
                                subsectionWithPath("_embedded.sw360:projects.[]description").description("The project description"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects.[]domain").description("The domain, possible values are:"  + Sw360ResourceServer.DOMAIN.toString()).optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                subsectionWithPath("_embedded.sw360:projects.[]businessUnit").description("The business unit this project belongs to"),
                                subsectionWithPath("_embedded.sw360:projects.[]externalIds").description("When projects are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                                subsectionWithPath("_embedded.sw360:projects.[]additionalData").description("A place to store additional data used by external tools").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]ownerAccountingUnit").description("The owner accounting unit of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]ownerGroup").description("The owner group of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]ownerCountry").description("The owner country of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]obligationsText").description("The obligations text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]clearingSummary").description("The clearing summary text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]specialRisksOSS").description("The special risks OSS text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]generalRisks3rdParty").description("The general risks 3rd party text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]specialRisks3rdParty").description("The special risks 3rd party text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]deliveryChannels").description("The sales and delivery channels text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]remarksAdditionalRequirements").description("The remark additional requirements text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]tag").description("The project tag"),
                                subsectionWithPath("_embedded.sw360:projects.[]deliveryStart").description("The project delivery start date"),
                                subsectionWithPath("_embedded.sw360:projects.[]preevaluationDeadline").description("The project preevaluation deadline"),
                                subsectionWithPath("_embedded.sw360:projects.[]systemTestStart").description("Date of the project system begin phase"),
                                subsectionWithPath("_embedded.sw360:projects.[]systemTestEnd").description("Date of the project system end phase"),
                                subsectionWithPath("_embedded.sw360:projects.[]linkedProjects").description("The relationship between linked projects of the project").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]linkedReleases").description("The relationship between linked releases of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]securityResponsibles").description("An array of users responsible for security of the project."),
                                subsectionWithPath("_embedded.sw360:projects.[]projectResponsible").description("A user who is responsible for the project."),
                                subsectionWithPath("_embedded.sw360:projects.[]enableSvm").description("Security vulnerability monitoring flag"),
                                subsectionWithPath("_embedded.sw360:projects.[]enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                subsectionWithPath("_embedded.sw360:projects[]considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                subsectionWithPath("_embedded.sw360:projects.[]state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                subsectionWithPath("_embedded.sw360:projects.[]phaseOutSince").description("The project phase-out date"),
                                subsectionWithPath("_embedded.sw360:projects.[]clearingRequestId").description("Clearing Request id associated with project."),
                                subsectionWithPath("_embedded.sw360:projects.[]_links").description("Self <<resources-index-links,Links>> to Project resource"),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.createdBy").description("The user who created this project"),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.clearingTeam").description("The clearingTeam of the project").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.homepage").description("The homepage url of the project").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.wiki").description("The wiki url of the project").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]licenseInfoHeaderText").description("The licenseInfoHeaderText text of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]externalUrls").description("A place to store additional data used by external tools").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.sw360:moderators").description("An array of all project moderators with email").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.sw360:contributors").description("An array of all project contributors with email").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]_embedded.sw360:attachments").description("An array of all project attachments").optional(),
                                subsectionWithPath("_embedded.sw360:projects.[]vendor").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_project() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("self").description("The <<resources-projects,Projects resource>>")
                        ),
                        responseFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("version").description("The project version"),
                                fieldWithPath("createdOn").description("The date the project was created"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                fieldWithPath("domain").description("The domain, possible values are:"  + Sw360ResourceServer.DOMAIN.toString()),
                                fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                fieldWithPath("businessUnit").description("The business unit this project belongs to"),
                                subsectionWithPath("externalIds").description("When projects are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                                subsectionWithPath("additionalData").description("A place to store additional data used by external tools"),
                                fieldWithPath("ownerAccountingUnit").description("The owner accounting unit of the project"),
                                fieldWithPath("ownerGroup").description("The owner group of the project"),
                                fieldWithPath("ownerCountry").description("The owner country of the project"),
                                fieldWithPath("obligationsText").description("The obligations text of the project"),
                                fieldWithPath("clearingSummary").description("The clearing summary text of the project"),
                                fieldWithPath("specialRisksOSS").description("The special risks OSS text of the project"),
                                fieldWithPath("generalRisks3rdParty").description("The general risks 3rd party text of the project"),
                                fieldWithPath("specialRisks3rdParty").description("The special risks 3rd party text of the project"),
                                fieldWithPath("deliveryChannels").description("The sales and delivery channels text of the project"),
                                fieldWithPath("remarksAdditionalRequirements").description("The remark additional requirements text of the project"),
                                fieldWithPath("tag").description("The project tag"),
                                fieldWithPath("deliveryStart").description("The project delivery start date"),
                                fieldWithPath("preevaluationDeadline").description("The project preevaluation deadline"),
                                fieldWithPath("systemTestStart").description("Date of the project system begin phase"),
                                fieldWithPath("systemTestEnd").description("Date of the project system end phase"),
                                subsectionWithPath("linkedProjects").description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                subsectionWithPath("linkedReleases").description("The relationship between linked releases of the project"),
                                fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                fieldWithPath("projectResponsible").description("A user who is responsible for the project."),
                                fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                fieldWithPath("licenseInfoHeaderText").description("The licenseInfoHeaderText text of the project"),
                                subsectionWithPath("externalUrls").description("A place to store additional data used by external URLs"),
                                fieldWithPath("clearingRequestId").description("Clearing Request id associated with project."),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                subsectionWithPath("_embedded.createdBy").description("The user who created this project"),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_embedded.sw360:releases").description("An array of <<resources-releases, Releases resources>>"),
                                subsectionWithPath("_embedded.sw360:packages").description("An array of linked <<resources-packages, Packages resources>>"),
                                subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                subsectionWithPath("_embedded.sw360:moderators").description("An array of all project moderators with email and link to their <<resources-user-get,User resource>>"),
                                subsectionWithPath("_embedded.sw360:attachments").description("An array of all project attachments and link to their <<resources-attachment-get,Attachment resource>>")
                        )));
    }

    @Test
    public void should_document_get_projects_by_type() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("type", project.getProjectType().toString())
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("type").description("Project types = `{CUSTOMER, INTERNAL, PRODUCT, SERVICE, INNER_SOURCE}`"),
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects.[]visibility")
                                        .description("The visibility of the project, possible values are: "
                                                + Arrays.asList(Visibility.values())),
                                subsectionWithPath("_embedded.sw360:projects")
                                        .description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links")
                                        .description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_projects_by_group() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("group", project.getBusinessUnit())
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("group").description("The project group"),
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects[]visibility").description("The visibility of the project, possible values are: " + Arrays.asList(Visibility.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_projects_by_tag() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("tag", project.getTag())
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("tag").description("The project tag"),
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects[]visibility").description("The visibility of the project, possible values are: " + Arrays.asList(Visibility.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_projects_by_name() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", project.getName())
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("name").description("The name of the project"),
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects.[]visibility").description("The visibility of the project, possible values are: "+ Arrays.asList(Visibility.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_projects_by_lucene_search() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", project.getName())
                .param("type", project.getProjectType().name())
                .param("group", project.getBusinessUnit())
                .param("tag", project.getTag())
                .param("luceneSearch", "true")
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("name").description("The name of the project"),
                                parameterWithName("type").description("The type of the project"),
                                parameterWithName("group").description("The group of the project"),
                                parameterWithName("tag").description("The tag of the project"),
                                parameterWithName("luceneSearch").description("List project by lucene search"),
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects per page"),
                                parameterWithName("sort").description("Defines order of the projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects.[]visibility").description("The visibility of the project, possible values are: "+ Arrays.asList(Visibility.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_projects_by_externalIds() throws Exception {
        Map<String, Set<String>> externalIdsQuery = new HashMap<>();
        externalIdsQuery.put("portal-id", new HashSet<>(Collections.singletonList("13319-XX3")));
        externalIdsQuery.put("project-ext", new HashSet<>(Arrays.asList("515432", "7657")));
        given(this.projectServiceMock.searchByExternalIds(eq(externalIdsQuery), any())).willReturn((new HashSet<>(projectList)));

        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/searchByExternalIds?project-ext=515432&project-ext=7657&portal-id=13319-XX3")
                .contentType(MediaTypes.HAL_JSON)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]externalIds").description("External Ids of the project. Return as 'Single String' when single value, or 'Array of String' when multi-values"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_license_clearing() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/licenseClearing")
                .header("Authorization", "Bearer " + accessToken)
                .param("transitive", "true")
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("transitive").description("Get the transitive releases"),
                                parameterWithName("page").description("Page of releases"),
                                parameterWithName("page_entries").description("Amount of releases page"),
                                parameterWithName("sort").description("Defines order of the releases")
                        ),
                        responseFields(
                                fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                subsectionWithPath("linkedReleases").description("The relationship between linked releases of the project"),
                                subsectionWithPath("linkedProjects").description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                subsectionWithPath("_embedded.sw360:release").description("An array of linked releases"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_linked_projects() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/linkedProjects")
                .header("Authorization", "Bearer " + accessToken)
                .param("transitive", "false")
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects page"),
                                parameterWithName("sort").description("Defines order of the projects"),
                                parameterWithName("transitive").description("Get the transitive projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_linked_projects_transitive() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + "12345" + "/linkedProjects?transitive=true")
                .header("Authorization", "Bearer " + accessToken)
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("page").description("Page of projects"),
                                parameterWithName("page_entries").description("Amount of projects page"),
                                parameterWithName("sort").description("Defines order of the projects"),
                                parameterWithName("transitive").description("Get the transitive projects")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of projects per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing projects"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_project_releases() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/releases")
                .header("Authorization", "Bearer " + accessToken)
                .param("transitive", "false")
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("transitive").description("Get the transitive releases"),
                                parameterWithName("page").description("Page of releases"),
                                parameterWithName("page_entries").description("Amount of releases page"),
                                parameterWithName("sort").description("Defines order of the releases")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:releases").description("An array of <<resources-releases, Releases resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of releases per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing releases"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_project_vulnerabilities() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/vulnerabilities")
                .header("Authorization", "Bearer " + accessToken)
                .param("priority", "1 - critical")
                .param("priority", "2 - major")
                .param("projectRelevance", "IRRELEVANT")
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "externalId,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("priority").description("The priority of vulnerability. For example: `1 - critical`, `2 - major`"),
                                parameterWithName("projectRelevance").description("The relevance of project of the vulnerability, possible values are: " + Arrays.asList(VulnerabilityRatingForProject.values())),
                                parameterWithName("page").description("Page of vulnerabilities"),
                                parameterWithName("page_entries").description("Amount of vulnerability page"),
                                parameterWithName("sort").description("Defines order of the vulnerability. It can be done based on " + Arrays.asList(VulnerabilityDTO._Fields.EXTERNAL_ID.getFieldName(), VulnerabilityDTO._Fields.TITLE.getFieldName(), VulnerabilityDTO._Fields.PRIORITY.getFieldName(), VulnerabilityDTO._Fields.PROJECT_RELEVANCE.getFieldName()))
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation"),
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]priority").description("The priority of vulnerability"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]projectAction").description("The action of vulnerability"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]projectRelevance").description("The relevance of project of the vulnerability, possible values are: " + Arrays.asList(VulnerabilityRatingForProject.values())),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]comment").description("Any message to added while updating project vulnerabilities"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]intReleaseId").description("The release id"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]intReleaseName").description("The release name"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]title").description("The title"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes").description("An array of <<resources-vulnerabilities, Vulnerability resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of vulnerability per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing vulnerability"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_project_vulnerabilities_by_externalid() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/vulnerabilities")
                .header("Authorization", "Bearer " + accessToken)
                .param("releaseId", "21055")
                .param("externalId", "12345")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("releaseId").description("The release Id of vulnerability."),
                                parameterWithName("externalId").description("The external Id of vulnerability.")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]priority").description("The priority of vulnerability"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]projectAction").description("The action of vulnerability"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]projectRelevance").description("The relevance of project of the vulnerability, possible values are: " + Arrays.asList(VulnerabilityRatingForProject.values())),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]comment").description("Any message to added while updating project vulnerabilities"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]intReleaseId").description("The release id"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]intReleaseName").description("The release name"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes").description("An array of <<resources-vulnerabilities, Vulnerability resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_update_project_vulnerabilities() throws Exception {
        Map<String, String> vulDtoMap = new HashMap<>();
        vulDtoMap.put("externalId", "12345");
        vulDtoMap.put("projectRelevance", "IRRELEVANT");
        vulDtoMap.put("comment", "Lorem Ipsum");
        vulDtoMap.put("intReleaseId", "21055");
        vulDtoMap.put("projectAction", "Lorem Ipsum");
        List<Map<String, String>> vulDtoMaps = new ArrayList<Map<String, String>>();
        vulDtoMaps.add(vulDtoMap);
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(patch("/api/projects/" + project.getId() + "/vulnerabilities")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(this.objectMapper.writeValueAsString(vulDtoMaps))
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]projectRelevance").description("The relevance of project of the vulnerability, possible values are: " + Arrays.asList(VulnerabilityRatingForProject.values())),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]intReleaseId").description("The release id"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]comment").description("Any message to add while updating project vulnerabilities"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes.[]projectAction").description("The action of vulnerability"),
                                subsectionWithPath("_embedded.sw360:vulnerabilityDTOes").description("An array of <<resources-vulnerabilities, Vulnerability resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_project_releases_transitive() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/releases?transitive=true")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:releases").description("An array of <<resources-releases, Releases resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_project_releases_ecc_information() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/releases/ecc?transitive=false")
                .header("Authorization", "Bearer " + accessToken)
                .param("page", "0")
                .param("page_entries", "5")
                .param("sort", "name,desc")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("transitive").description("Get the transitive releases"),
                                parameterWithName("page").description("Page of releases"),
                                parameterWithName("page_entries").description("Amount of releases per page"),
                                parameterWithName("sort").description("Defines order of the releases")
                        ),
                        links(
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("last").description("Link to last page"),
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:releases").description("An array of <<resources-releases, Releases resources>>"),
                                subsectionWithPath("_embedded.sw360:releases.[].eccInformation.eccStatus").description("The ECC information status value"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("page").description("Additional paging information"),
                                fieldWithPath("page.size").description("Number of releases per page"),
                                fieldWithPath("page.totalElements").description("Total number of all existing releases"),
                                fieldWithPath("page.totalPages").description("Total number of pages"),
                                fieldWithPath("page.number").description("Number of the current page")
                        )));
    }

    @Test
    public void should_document_get_project_attachment_info() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/attachments")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        responseFields(
                                subsectionWithPath("_embedded.sw360:attachments").description("An array of <<resources-attachment, Attachments resources>>"),
                                subsectionWithPath("_embedded.sw360:attachments.[]filename").description("The attachment filename"),
                                subsectionWithPath("_embedded.sw360:attachments.[]sha1").description("The attachment sha1 value"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_update_project_attachment_info() throws Exception {
        Attachment updateAttachment = new Attachment().setAttachmentType(AttachmentType.BINARY)
                .setCreatedComment("Created Comment").setCheckStatus(CheckStatus.ACCEPTED)
                .setCheckedComment("Checked Comment");
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc
                .perform(patch("/api/projects/98745/attachment/1234").contentType(MediaTypes.HAL_JSON)
                        .content(this.objectMapper.writeValueAsString(updateAttachment))
                        .header("Authorization", "Bearer " + accessToken).accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                requestFields(
                        fieldWithPath("attachmentType").description("The type of Attachment. Possible Values are: "+Arrays.asList(AttachmentType.values())),
                        fieldWithPath("createdComment").description("The upload Comment of Attachment"),
                        fieldWithPath("checkStatus").description("The checkStatus of Attachment. Possible Values are: "+Arrays.asList(CheckStatus.values())),
                        fieldWithPath("checkedComment").description("The checked Comment of Attachment")),
                responseFields(
                        fieldWithPath("filename").description("The attachment filename"),
                        fieldWithPath("sha1").description("The attachment sha1 value"),
                        fieldWithPath("attachmentType").description("The type of attachment. Possible Values are: "+Arrays.asList(AttachmentType.values())),
                        fieldWithPath("createdBy").description("The email of user who uploaded the attachment"),
                        fieldWithPath("createdTeam").description("The department of user who uploaded the attachment"),
                        fieldWithPath("createdOn").description("The date when attachment was uploaded"),
                        fieldWithPath("createdComment").description("The upload Comment of attachment"),
                        fieldWithPath("checkStatus").description("The checkStatus of attachment. Possible Values are: "+Arrays.asList(CheckStatus.values())),
                        fieldWithPath("checkedComment").description("The checked comment of attachment"),
                        fieldWithPath("checkedBy").description("The email of user who checked the attachment"),
                        fieldWithPath("checkedTeam").description("The department of user who checked the attachment"),
                        fieldWithPath("checkedOn").description("The date when attachment was checked"),
                        subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                )));
    }

    @Test
    public void should_document_get_project_attachment() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId() + "/attachments/" + attachment.getAttachmentContentId())
                .header("Authorization", "Bearer " + accessToken)
                .accept("application/*"))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document());
    }

    @Test
    public void should_document_create_project() throws Exception {
        Map<String, Object> project = new HashMap<>();
        project.put("name", "Test Project");
        project.put("version", "1.0");
        project.put("visibility", "PRIVATE");
        project.put("description", "This is the description of my Test Project");
        project.put("projectType", ProjectType.PRODUCT.toString());
        Map<String, ProjectReleaseRelationship> releaseIdToUsage = new HashMap<>();
        releaseIdToUsage.put("3765276512", new ProjectReleaseRelationship(CONTAINED, OPEN));
        project.put("linkedReleases", releaseIdToUsage);
        Map<String, ProjectProjectRelationship> linkedProjects = new HashMap<>();
        linkedProjects.put("376576", new ProjectProjectRelationship(ProjectRelationship.CONTAINED).setEnableSvm(true));
        project.put("linkedProjects", linkedProjects);
        project.put("leadArchitect", "lead@sw360.org");
        project.put("moderators", new HashSet<>(Arrays.asList("moderator1@sw360.org", "moderator2@sw360.org")));
        project.put("contributors", new HashSet<>(Arrays.asList("contributor1@sw360.org", "contributor2@sw360.org")));
        project.put("state", ProjectState.ACTIVE.toString());
        project.put("phaseOutSince", "2020-06-24");

        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(post("/api/projects")
                .contentType(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(project))
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("_embedded.createdBy.email", Matchers.is("admin@sw360.org")))
                .andDo(this.documentationHandler.document(
                        requestFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("version").description("The version of the new project"),
                                fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("linkedReleases").description("The relationship between linked releases of the project"),
                                subsectionWithPath("linkedProjects").description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                fieldWithPath("leadArchitect").description("The lead architect of the project"),
                                fieldWithPath("contributors").description("An array of contributors to the project"),
                                fieldWithPath("moderators").description("An array of moderators"),
                                fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                fieldWithPath("phaseOutSince").description("The project phase-out date")
                        ),
                        responseFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("version").description("The project version"),
                                fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                fieldWithPath("createdOn").description("The date the project was created"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                subsectionWithPath("_embedded.createdBy").description("The user who created this project")
                        )));
    }
    @Test
    public void should_document_create_duplicate_project() throws Exception {
        Map<String, Object> projectReqs = new HashMap<>();
        projectReqs.put("name", "Test Project");
        projectReqs.put("version", "1.0");
        projectReqs.put("visibility", "PRIVATE");
        projectReqs.put("description", "This is the description of my Test Project");
        projectReqs.put("projectType", ProjectType.PRODUCT.toString());
        Map<String, ProjectReleaseRelationship> releaseIdToUsage = new HashMap<>();
        releaseIdToUsage.put("3765276512", new ProjectReleaseRelationship(CONTAINED, OPEN));
        projectReqs.put("linkedReleases", releaseIdToUsage);
        Map<String, ProjectProjectRelationship> linkedProjects = new HashMap<>();
        linkedProjects.put("376576", new ProjectProjectRelationship(ProjectRelationship.CONTAINED).setEnableSvm(true));
        projectReqs.put("linkedProjects", linkedProjects);
        projectReqs.put("leadArchitect", "lead@sw360.org");
        projectReqs.put("moderators", new HashSet<>(Arrays.asList("moderator1@sw360.org", "moderator2@sw360.org")));
        projectReqs.put("contributors", new HashSet<>(Arrays.asList("contributor1@sw360.org", "contributor2@sw360.org")));
        projectReqs.put("state", ProjectState.ACTIVE.toString());
        projectReqs.put("phaseOutSince", "2020-06-24");

        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(post("/api/projects/duplicate/" + project.getId())
                .contentType(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(projectReqs))
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("_embedded.createdBy.email", Matchers.is("admin@sw360.org")))
                .andDo(this.documentationHandler.document(
                        requestFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("version").description("The version of the new project"),
                                fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("linkedReleases").description("The relationship between linked releases of the project"),
                                subsectionWithPath("linkedProjects").description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                fieldWithPath("leadArchitect").description("The lead architect of the project"),
                                fieldWithPath("contributors").description("An array of contributors to the project"),
                                fieldWithPath("moderators").description("An array of moderators"),
                                fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                fieldWithPath("phaseOutSince").description("The project phase-out date")
                        ),
                        responseFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("version").description("The project version"),
                                fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                fieldWithPath("createdOn").description("The date the project was created"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                subsectionWithPath("_embedded.createdBy").description("The user who created this project")
                        )));
    }

    @Test
    public void should_document_update_project() throws Exception {
        Project updateProject = new Project();
        updateProject.setName("updated project");
        updateProject.setDescription("Project description updated");
        updateProject.setVersion("1.0");
        updateProject.setState(ProjectState.PHASE_OUT);
        updateProject.setPhaseOutSince("2020-06-24");
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc
                .perform(patch("/api/projects/376576").contentType(MediaTypes.HAL_JSON)
                        .content(this.objectMapper.writeValueAsString(updateProject))
                        .header("Authorization", "Bearer " + accessToken).accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                links(linkWithRel("self").description("The <<resources-projects,Projects resource>>")),
                requestFields(
                        fieldWithPath("name").description("The name of the project"),
                        fieldWithPath("type").description("project"),
                        fieldWithPath("version").description("The version of the new project"),
                        fieldWithPath("visibility").description("The project visibility, possible values are: "
                                + Arrays.asList(Visibility.values())),
                        fieldWithPath("description").description("The project description"),
                        fieldWithPath("projectType").description("The project type, possible values are: "
                                + Arrays.asList(ProjectType.values())),
                        fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                        fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                        fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                        fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                        fieldWithPath("phaseOutSince").description("The project phase-out date"),
                        fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag.")),
                responseFields(fieldWithPath("name").description("The name of the project"),
                        fieldWithPath("version").description("The project version"),
                        fieldWithPath("createdOn").description("The date the project was created"),
                        fieldWithPath("description").description("The project description"),
                        fieldWithPath("domain").description("The domain, possible values are:"  + Sw360ResourceServer.DOMAIN.toString()),
                        fieldWithPath("projectType").description("The project type, possible values are: "
                                + Arrays.asList(ProjectType.values())),
                        fieldWithPath("visibility").description("The project visibility, possible values are: "
                                + Arrays.asList(Visibility.values())),
                        fieldWithPath("businessUnit").description("The business unit this project belongs to"),
                        subsectionWithPath("externalIds").description(
                                "When projects are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                        subsectionWithPath("additionalData").description("A place to store additional data used by external tools"),
                        fieldWithPath("ownerAccountingUnit")
                                .description("The owner accounting unit of the project"),
                        fieldWithPath("ownerGroup").description("The owner group of the project"),
                        fieldWithPath("ownerCountry").description("The owner country of the project"),
                        fieldWithPath("obligationsText").description("The obligations text of the project"),
                        fieldWithPath("clearingSummary")
                                .description("The clearing summary text of the project"),
                        fieldWithPath("specialRisksOSS")
                                .description("The special risks OSS text of the project"),
                        fieldWithPath("generalRisks3rdParty")
                                .description("The general risks 3rd party text of the project"),
                        fieldWithPath("specialRisks3rdParty")
                                .description("The special risks 3rd party text of the project"),
                        fieldWithPath("deliveryChannels")
                                .description("The sales and delivery channels text of the project"),
                        fieldWithPath("remarksAdditionalRequirements")
                                .description("The remark additional requirements text of the project"),
                        fieldWithPath("tag").description("The project tag"),
                        fieldWithPath("deliveryStart").description("The project delivery start date"),
                        fieldWithPath("preevaluationDeadline")
                                .description("The project preevaluation deadline"),
                        fieldWithPath("systemTestStart").description("Date of the project system begin phase"),
                        fieldWithPath("systemTestEnd").description("Date of the project system end phase"),
                        fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                        fieldWithPath("phaseOutSince").description("The project phase-out date"),
                        subsectionWithPath("linkedProjects")
                                .description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                        subsectionWithPath("linkedReleases")
                                .description("The relationship between linked releases of the project"),
                        fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                        fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                        fieldWithPath("clearingRequestId").description("Clearing Request id associated with project."),
                        fieldWithPath("projectResponsible").description("A user who is responsible for the project."),
                        subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                        subsectionWithPath("_embedded.createdBy").description("The user who created this project"),
                        fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                        fieldWithPath("licenseInfoHeaderText").description("The licenseInfoHeaderText text of the project"),
                        subsectionWithPath("externalUrls").description("A place to store additional data used by external URLs"),
                        fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                        fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                        subsectionWithPath("_embedded.sw360:moderators").description("An array of moderators"),
                        subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                        subsectionWithPath("_embedded.sw360:releases").description("An array of <<resources-releases, Releases resources>>"),
                        subsectionWithPath("_embedded.sw360:packages").description("An array of linked <<resources-packages, Packages resources>>"),
                        subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                        subsectionWithPath("_embedded.sw360:attachments").description("An array of all project attachments and link to their <<resources-attachment-get,Attachment resource>>"))));
    }

    @Test
    public void should_document_upload_attachment_to_project() throws Exception {
        testAttachmentUpload("/api/projects/", project.getId());
    }

    @Test
    public void should_document_link_releases() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/projects/" + project.getId() + "/releases");
        add_patch_releases(requestBuilder);
    }

    @Test
    public void should_document_link_releases_with_project_release_relation() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/projects/" + project.getId() + "/releases");
        add_patch_releases_with_project_release_relation(requestBuilder);
    }

    @Test
    public void should_document_patch_releases() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = patch("/api/projects/" + project.getId() + "/releases");
        add_patch_releases(requestBuilder);
    }

    @Test
    public void should_document_patch_releases_with_project_release_relation() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = patch("/api/projects/" + project.getId() + "/releases");
        add_patch_releases_with_project_release_relation(requestBuilder);
    }

    @Test
    public void should_document_link_packages() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = patch("/api/projects/" + project.getId() + "/link/packages");
        link_unlink_packages(requestBuilder);
    }

    @Test
    public void should_document_unlink_packages() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = patch("/api/projects/" + project.getId() + "/unlink/packages");
        link_unlink_packages(requestBuilder);
    }

    @Test
    public void should_document_update_project_release_relationship() throws Exception {
        ProjectReleaseRelationship updateProjectReleaseRelationship = new ProjectReleaseRelationship()
                .setComment("Test Comment").setMainlineState(MainlineState.SPECIFIC)
                .setReleaseRelation(ReleaseRelationship.STANDALONE);
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc
                .perform(patch("/api/projects/376576/release/3765276512").contentType(MediaTypes.HAL_JSON)
                        .content(this.objectMapper.writeValueAsString(updateProjectReleaseRelationship))
                        .header("Authorization", "Bearer " + accessToken).accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                requestFields(
                        fieldWithPath("releaseRelation").description("The relation of linked release. Possible Values are: "+Arrays.asList(ReleaseRelationship.values())),
                        fieldWithPath("mainlineState").description("The mainlineState of linked release. Possible Values are: "+Arrays.asList(MainlineState.values())),
                        fieldWithPath("comment").description("The Comment for linked release")),
                responseFields(
                        fieldWithPath("releaseRelation").description("The relation of linked release. Possible Values are: "+Arrays.asList(ReleaseRelationship.values())),
                        fieldWithPath("mainlineState").description("The mainlineState of linked release. Possible Values are: "+Arrays.asList(MainlineState.values())),
                        fieldWithPath("comment").description("The Comment for linked release"),
                        fieldWithPath("createdOn").description("The date when release was linked to project"),
                        fieldWithPath("createdBy").description("The email of user who linked release to project")
                )));
    }

    @Test
    public void should_document_get_download_license_info() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(get("/api/projects/" + project.getId()+ "/licenseinfo?generatorClassName=XhtmlGenerator&variant=DISCLOSURE&externalIds=portal-id,main-project-id")
                .header("Authorization", "Bearer " + accessToken)
                .accept("application/xhtml+xml"))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler
                        .document(requestParameters(
                                parameterWithName("generatorClassName")
                                        .description("All possible values for output generator class names are "
                                                + Arrays.asList("DocxGenerator", "XhtmlGenerator", "TextGenerator")),
                                parameterWithName("variant").description("All the possible values for variants are "
                                        + Arrays.asList(OutputFormatVariant.values())),
                                parameterWithName("externalIds").description("The external Ids of the project")
                                )));
    }

    @Test
    public void should_document_get_projects_releases() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(get("/api/projects/releases")
                 .header("Authorization", "Bearer " + accessToken)
                 .param("clearingState", ClearingState.APPROVED.toString())
                 .param("transitive", "false")
                 .param("page", "0")
                 .param("page_entries", "5")
                 .param("sort", "name,desc")
                 .content(this.objectMapper.writeValueAsString(List.of("376576","376570")))
                 .contentType(MediaTypes.HAL_JSON)
                 .accept(MediaTypes.HAL_JSON))
                 .andExpect(status().isOk())
                 .andDo(this.documentationHandler.document(
                             links(
                                     linkWithRel("curies").description("Curies are used for online documentation"),
                                     linkWithRel("first").description("Link to first page"),
                                     linkWithRel("last").description("Link to last page")
                                     ),
                             requestParameters(
                                     parameterWithName("transitive").description("Get the transitive releases"),
                                     parameterWithName("clearingState").description("The clearing state of the release. Possible values are: "+Arrays.asList(ClearingState.values())),
                                     parameterWithName("page").description("Page of releases"),
                                     parameterWithName("page_entries").description("Amount of releases page"),
                                     parameterWithName("sort").description("Defines order of the releases")
                                     ),
                             responseFields(
                                     subsectionWithPath("_embedded.sw360:releases.[]componentType").description("The component type, possible values are: " + Arrays.asList(ComponentType.values())),
                                     subsectionWithPath("_embedded.sw360:releases.[]name").description("The name of the release, optional"),
                                     subsectionWithPath("_embedded.sw360:releases.[]version").description("The version of the release"),
                                     subsectionWithPath("_embedded.sw360:releases.[]createdBy").description("Email of the release creator"),
                                     subsectionWithPath("_embedded.sw360:releases.[]componentId").description("The component id"),
                                     subsectionWithPath("_embedded.sw360:releases.[]packageIds").description("The component id"),
                                     subsectionWithPath("_embedded.sw360:releases.[]cpeid").description("CpeId of the release"),
                                     subsectionWithPath("_embedded.sw360:releases.[]clearingState").description("The clearing of the release, possible values are " + Arrays.asList(ClearingState.values())),
                                     subsectionWithPath("_embedded.sw360:releases.[]releaseDate").description("The date of this release"),
                                     subsectionWithPath("_embedded.sw360:releases.[]createdOn").description("The creation date of the internal sw360 release"),
                                     subsectionWithPath("_embedded.sw360:releases.[]mainlineState").description("the mainline state of the release, possible values are: " + Arrays.asList(MainlineState.values())),
                                     subsectionWithPath("_embedded.sw360:releases.[]sourceCodeDownloadurl").description("the source code download url of the release"),
                                     subsectionWithPath("_embedded.sw360:releases.[]binaryDownloadurl").description("the binary download url of the release"),
                                     subsectionWithPath("_embedded.sw360:releases.[]externalIds").description("When releases are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                                     subsectionWithPath("_embedded.sw360:releases.[]additionalData").description("A place to store additional data used by external tools"),
                                     subsectionWithPath("_embedded.sw360:releases.[]languages").description("The language of the component"),
                                     subsectionWithPath("_embedded.sw360:releases.[]mainLicenseIds").description("An array of all main licenses"),
                                     subsectionWithPath("_embedded.sw360:releases.[]operatingSystems").description("The OS on which the release operates"),
                                     subsectionWithPath("_embedded.sw360:releases.[]softwarePlatforms").description("The software platforms of the component"),
                                     subsectionWithPath("_embedded.sw360:releases.[]vendor").description("The Id of the vendor"),
                                     subsectionWithPath("_embedded.sw360:releases.[]_links").description("<<resources-release-get,Release>> to release resource"),
                                     subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                     fieldWithPath("page").description("Additional paging information"),
                                     fieldWithPath("page.size").description("Number of releases per page"),
                                     fieldWithPath("page.totalElements").description("Total number of all existing releases"),
                                     fieldWithPath("page.totalPages").description("Total number of pages"),
                                     fieldWithPath("page.number").description("Number of the current page")
                                     )
                             ));
    }

    @Test
    public void should_document_delete_project() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(delete("/api/projects/" + project.getId())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
    }

    private void add_patch_releases(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        List<String> releaseIds = Arrays.asList("3765276512", "5578999", "3765276513");

        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(requestBuilder.contentType(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(releaseIds))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isCreated());
    }

    private void link_unlink_packages(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        Set<String> packageIds = new HashSet<>();

        packageIds.add("9876746589");
        packageIds.add("4444444467");
        packageIds.add("5555555576");

        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(requestBuilder.contentType(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(packageIds))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isCreated());
    }

    private void add_patch_releases_with_project_release_relation(MockHttpServletRequestBuilder requestBuilder)
            throws Exception {
        ProjectReleaseRelationship projectReleaseRelationship1 = new ProjectReleaseRelationship(
                ReleaseRelationship.REFERRED, MAINLINE);
        ProjectReleaseRelationship projectReleaseRelationship2 = new ProjectReleaseRelationship(
                ReleaseRelationship.STANDALONE, MainlineState.SPECIFIC).setComment("Test Comment 2");

        ImmutableMap<String, ProjectReleaseRelationship> releaseIdToUsage = ImmutableMap
                .<String, ProjectReleaseRelationship>builder().put("12345", projectReleaseRelationship1)
                .put("54321", projectReleaseRelationship2).build();
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(requestBuilder.contentType(MediaTypes.HAL_JSON)
                .content(this.objectMapper.writeValueAsString(releaseIdToUsage))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isCreated());
    }

    @Test
    public void should_document_get_my_projects() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/myprojects")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("createdBy", "true")
                        .param("moderator", "true")
                        .param("contributor", "true")
                        .param("projectOwner", "true")
                        .param("leadArchitect", "true")
                        .param("projectResponsible", "true")
                        .param("securityResponsible", "true")
                        .param("stateOpen", "true")
                        .param("stateClosed", "true")
                        .param("stateInProgress", "true")
                        .param("allDetails", "true")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("createdBy").description("Projects with current user as creator. Possible values are `<true|false>`"),
                                parameterWithName("moderator").description("Projects with current user as moderator. Possible values are `<true|false>`"),
                                parameterWithName("contributor").description("Projects with current user as contributor. Possible values are `<true|false>`"),
                                parameterWithName("projectOwner").description("Projects with current user as owner. Possible values are `<true|false>`"),
                                parameterWithName("leadArchitect").description("Projects with current user as lead architect. Possible values are `<true|false>`"),
                                parameterWithName("projectResponsible").description("Projects with current user as project responsible. Possible values are `<true|false>`"),
                                parameterWithName("securityResponsible").description("Projects with current user as security responsible. Possible values are `<true|false>`"),
                                parameterWithName("stateOpen").description("Projects with state as open. Possible values are `<true|false>`"),
                                parameterWithName("stateClosed").description("Projects with state as closed. Possible values are `<true|false>`"),
                                parameterWithName("stateInProgress").description("Projects with state as in progress. Possible values are `<true|false>`"),
                                parameterWithName("allDetails").description("Flag to get projects with all details. Possible values are `<true|false>`")
                        ),
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                subsectionWithPath("_embedded.sw360:projects.[]name").description("The name of the project"),
                                subsectionWithPath("_embedded.sw360:projects.[]version").description("The project version"),
                                subsectionWithPath("_embedded.sw360:projects.[]projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                subsectionWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_import_spdx() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","file=@/bom.spdx.rdf".getBytes());
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/projects/import/SBOM")
                .content(file.getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("type", "SPDX");
        this.mockMvc.perform(builder).andExpect(status().isOk()).andDo(this.documentationHandler.document());
    }

    @Test
    public void should_document_get_project_count() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(get("/api/projects/projectcount")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                       responseFields(
                               fieldWithPath("status").description("status of the API. Possible values are `<success|failure>`").optional(),
                               fieldWithPath("count").description("Count of projects for a user.").optional()
                       )));
    }

    @Test
    public void should_document_get_license_clearing_information() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        this.mockMvc.perform(get("/api/projects/" + project.getId()+ "/licenseClearingCount")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                       responseFields(
                               fieldWithPath("Release Count").description("Total count of releases of a project including sub-projects releases"),
                               fieldWithPath("Approved Count").description("Approved license clearing state releases")
                       )));
    }

    @Test
    public void should_document_create_summary_administration() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/projects/" + project.getId()+ "/summaryAdministration")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        responseFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("version").description("The project version"),
                                fieldWithPath("createdOn").description("The date the project was created"),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                fieldWithPath("domain").description("The domain, possible values are:"  + Sw360ResourceServer.DOMAIN.toString()),
                                fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                subsectionWithPath("externalIds").description("When projects are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                                subsectionWithPath("additionalData").description("A place to store additional data used by external tools"),
                                fieldWithPath("ownerAccountingUnit").description("The owner accounting unit of the project"),
                                fieldWithPath("ownerGroup").description("The owner group of the project"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("ownerCountry").description("The owner country of the project"),
                                fieldWithPath("obligationsText").description("The obligations text of the project"),
                                fieldWithPath("clearingSummary").description("The clearing summary text of the project"),
                                fieldWithPath("specialRisksOSS").description("The special risks OSS text of the project"),
                                fieldWithPath("generalRisks3rdParty").description("The general risks 3rd party text of the project"),
                                fieldWithPath("specialRisks3rdParty").description("The special risks 3rd party text of the project"),
                                fieldWithPath("deliveryChannels").description("The sales and delivery channels text of the project"),
                                fieldWithPath("remarksAdditionalRequirements").description("The remark additional requirements text of the project"),
                                fieldWithPath("tag").description("The project tag"),
                                fieldWithPath("businessUnit").description("The business unit this project belongs to"),
                                fieldWithPath("deliveryStart").description("The project delivery start date"),
                                fieldWithPath("preevaluationDeadline").description("The project preevaluation deadline"),
                                fieldWithPath("systemTestStart").description("Date of the project system begin phase"),
                                fieldWithPath("systemTestEnd").description("Date of the project system end phase"),
                                fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                fieldWithPath("clearingRequestId").description("Clearing Request id associated with project."),
                                subsectionWithPath("externalUrls").description("A place to store additional data used by external URLs"),
                                subsectionWithPath("_embedded.createdBy").description("The user who created this project"),
                                subsectionWithPath("_embedded.projectResponsible").description("The project responsible displayed").type(JsonFieldType.OBJECT).optional(),
                                subsectionWithPath("_embedded.securityResponsibles").description("An array of project securityResponsible will get displayed").type(JsonFieldType.ARRAY).optional(),
                                subsectionWithPath("_embedded.projectOwner").description("The project owner").type(JsonFieldType.OBJECT).optional(),
                                subsectionWithPath("_embedded.modifiedBy").description("The user who modified the project").type(JsonFieldType.OBJECT).optional(),
                                subsectionWithPath("_embedded.leadArchitect").description("The user who leadArchitect of this project").type(JsonFieldType.OBJECT).optional(),
                                subsectionWithPath("_embedded.sw360:moderators").description("An array of moderators"),
                                subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"))
                        		));
    }

    @Test
    public void should_document_get_project_report() throws Exception{
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("withlinkedreleases", "true")
                        .param("mimetype", "xlsx")
                        .param("mailrequest", "true")
                        .param("module", "projects")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("withlinkedreleases").description("Projects with linked releases. Possible values are `<true|false>`"),
                                parameterWithName("mimetype").description("Projects download format. Possible values are `<xls|xlsx>`"),
                                parameterWithName("mailrequest").description("Downloading project report requirted mail link. Possible values are `<true|false>`"),
                                parameterWithName("module").description("module represent the project or component. Possible values are `<components|projects>`")
                        ),responseFields(
                                subsectionWithPath("response").description("The response message displayed").optional()
                                )
                        ));
    }

    @Test
    public void should_document_get_project_report_without_mail_req() throws Exception{
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        mockMvc.perform(get("/api/reports")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("withlinkedreleases", "true")
                        .param("mimetype", "xlsx")
                        .param("mailrequest", "false")
                        .param("module", "projects")
                        .accept("application/xhtml+xml"))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestParameters(
                                parameterWithName("withlinkedreleases").description("Projects with linked releases. Possible values are `<true|false>`"),
                                parameterWithName("mimetype").description("Projects download format. Possible values are `<xls|xlsx>`"),
                                parameterWithName("mailrequest").description("Downloading project report requirted mail link. Possible values are `<true|false>`"),
                                parameterWithName("module").description("module represent the project or component. Possible values are `<components|projects>`")
                        )));
    }

    @Test
    public void should_document_import_cyclonedx() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","file=@/sampleBOM.xml".getBytes());
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/projects/import/SBOM")
                .content(file.getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("type", "CycloneDX");
        this.mockMvc.perform(builder).andExpect(status().isOk()).andDo(this.documentationHandler.document());
    }

    @Test
    public void should_document_import_cyclonedx_on_project() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file","file=@/sampleBOM.xml".getBytes());
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/projects/"+project.getId()+"/import/SBOM")
                .content(file.getBytes())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + accessToken);
        this.mockMvc.perform(builder).andExpect(status().isOk()).andDo(this.documentationHandler.document());
    }

    @Test
    public void should_document_get_project_with_dependencies_network() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        if (!SW360Constants.ENABLE_FLEXIBLE_PROJECT_RELEASE_RELATIONSHIP) {
            mockMvc.perform(get("/api/projects/network/" + project.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .accept(MediaTypes.HAL_JSON))
                    .andExpect(status().isInternalServerError());
        } else {
            mockMvc.perform(get("/api/projects/network/" + project.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .accept(MediaTypes.HAL_JSON))
                    .andExpect(status().isOk())
                    .andDo(this.documentationHandler.document(
                            links(
                                    linkWithRel("self").description("The <<resources-projects,Projects resource>>")
                            ),
                            responseFields(
                                    fieldWithPath("name").description("The name of the project"),
                                    fieldWithPath("version").description("The project version"),
                                    fieldWithPath("createdOn").description("The date the project was created"),
                                    fieldWithPath("description").description("The project description"),
                                    fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                    fieldWithPath("domain").description("The domain, possible values are:"  + Sw360ResourceServer.DOMAIN.toString()),
                                    fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                    fieldWithPath("businessUnit").description("The business unit this project belongs to"),
                                    subsectionWithPath("externalIds").description("When projects are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                                    subsectionWithPath("additionalData").description("A place to store additional data used by external tools"),
                                    fieldWithPath("ownerAccountingUnit").description("The owner accounting unit of the project"),
                                    fieldWithPath("ownerGroup").description("The owner group of the project"),
                                    fieldWithPath("ownerCountry").description("The owner country of the project"),
                                    fieldWithPath("obligationsText").description("The obligations text of the project"),
                                    fieldWithPath("clearingSummary").description("The clearing summary text of the project"),
                                    fieldWithPath("specialRisksOSS").description("The special risks OSS text of the project"),
                                    fieldWithPath("generalRisks3rdParty").description("The general risks 3rd party text of the project"),
                                    fieldWithPath("specialRisks3rdParty").description("The special risks 3rd party text of the project"),
                                    fieldWithPath("deliveryChannels").description("The sales and delivery channels text of the project"),
                                    fieldWithPath("remarksAdditionalRequirements").description("The remark additional requirements text of the project"),
                                    fieldWithPath("tag").description("The project tag"),
                                    fieldWithPath("deliveryStart").description("The project delivery start date"),
                                    fieldWithPath("preevaluationDeadline").description("The project preevaluation deadline"),
                                    fieldWithPath("systemTestStart").description("Date of the project system begin phase"),
                                    fieldWithPath("systemTestEnd").description("Date of the project system end phase"),
                                    subsectionWithPath("linkedProjects").description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                    fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                    fieldWithPath("projectResponsible").description("A user who is responsible for the project."),
                                    fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                    fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                    fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                    fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                    fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                    fieldWithPath("clearingRequestId").description("Clearing Request id associated with project."),
                                    fieldWithPath("licenseInfoHeaderText").description("License info header text."),
                                    subsectionWithPath("dependencyNetwork").description("Dependency network of project with release."),
                                    subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                    subsectionWithPath("_embedded.createdBy").description("The user who created this project"),
                                    subsectionWithPath("_embedded.sw360:projectDTOs").description("An array of <<resources-projects, Projects resources>>"),
                                    subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                    subsectionWithPath("_embedded.sw360:moderators").description("An array of all project moderators with email and link to their <<resources-user-get,User resource>>"),
                                    subsectionWithPath("_embedded.sw360:attachments").description("An array of all project attachments and link to their <<resources-attachment-get,Attachment resource>>")
                            )));
        }
    }

    @Test
    public void should_document_create_project_with_network() throws Exception {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        when(this.projectServiceMock.createProject(any(), any())).
                thenReturn(
                        new Project("Test Project")
                                .setId("1234567890")
                                .setDescription("This is the description of my Test Project")
                                .setProjectType(ProjectType.PRODUCT)
                                .setVersion("1.0")
                                .setCreatedBy("admin@sw360.org")
                                .setPhaseOutSince("2020-06-25")
                                .setState(ProjectState.ACTIVE)
                                .setReleaseRelationNetwork("[{\"comment\":\"Test Comment\",\"releaseLink\":[],\"createBy\":\"admin@sw360.org\",\"createOn\":\"" + currentDate + "\",\"mainlineState\":\"OPEN\",\"releaseId\":\"3765276512\",\"releaseRelationship\":\"CONTAINED\"}]")
                                .setVendor((new Vendor("Test", "Test short", "http://testvendoraddress.com").setId("987567468")))
                                .setCreatedOn(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));

        Map<String, Object> project = new HashMap<>();
        project.put("name", "Test Project");
        project.put("version", "1.0");
        project.put("visibility", "PRIVATE");
        project.put("description", "This is the description of my Test Project");
        project.put("projectType", ProjectType.PRODUCT.toString());
        List<Map<String, Object>> dependencyNetwork = new ArrayList<>();
        Map<String, Object> releaseNode = new HashMap<>();
        releaseNode.put("releaseId", "3765276512");
        releaseNode.put("comment", "Test Comment");
        releaseNode.put("releaseRelationship", "CONTAINED");
        releaseNode.put("mainlineState", "MAINLINE");
        releaseNode.put("createBy", "admin@sw360.org");
        dependencyNetwork.add(releaseNode);
        project.put("dependencyNetwork", dependencyNetwork);
        Map<String, ProjectProjectRelationship> linkedProjects = new HashMap<>();
        linkedProjects.put("376576", new ProjectProjectRelationship(ProjectRelationship.CONTAINED).setEnableSvm(true));
        project.put("linkedProjects", linkedProjects);
        project.put("leadArchitect", "lead@sw360.org");
        project.put("moderators", new HashSet<>(Arrays.asList("moderator1@sw360.org", "moderator2@sw360.org")));
        project.put("contributors", new HashSet<>(Arrays.asList("contributor1@sw360.org", "contributor2@sw360.org")));
        project.put("state", ProjectState.ACTIVE.toString());
        project.put("phaseOutSince", "2020-06-24");

        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        if (!SW360Constants.ENABLE_FLEXIBLE_PROJECT_RELEASE_RELATIONSHIP) {
            this.mockMvc.perform(post("/api/projects/network/")
                            .contentType(MediaTypes.HAL_JSON)
                            .content(this.objectMapper.writeValueAsString(project))
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isInternalServerError());
        } else {
            this.mockMvc.perform(post("/api/projects/network/")
                            .contentType(MediaTypes.HAL_JSON)
                            .content(this.objectMapper.writeValueAsString(project))
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("_embedded.createdBy.email", Matchers.is("admin@sw360.org")))
                    .andDo(this.documentationHandler.document(
                            requestFields(
                                    fieldWithPath("name").description("The name of the project"),
                                    fieldWithPath("description").description("The project description"),
                                    fieldWithPath("version").description("The version of the new project"),
                                    fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                    fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                    subsectionWithPath("linkedProjects").description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                    fieldWithPath("leadArchitect").description("The lead architect of the project"),
                                    fieldWithPath("contributors").description("An array of contributors to the project"),
                                    fieldWithPath("moderators").description("An array of moderators"),
                                    fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                    fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                    subsectionWithPath("dependencyNetwork").description("Dependency network")
                            ),
                            responseFields(
                                    fieldWithPath("name").description("The name of the project"),
                                    fieldWithPath("version").description("The project version"),
                                    fieldWithPath("visibility").description("The project visibility, possible values are: " + Arrays.asList(Visibility.values())),
                                    fieldWithPath("createdOn").description("The date the project was created"),
                                    fieldWithPath("description").description("The project description"),
                                    fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                    fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                    fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                    fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                    fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                    fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                    fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                    subsectionWithPath("dependencyNetwork").description("Dependency network"),
                                    subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                    subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                    subsectionWithPath("_embedded.createdBy").description("The user who created this project")
                            )));
        }
    }

    @Test
    public void should_document_update_project_with_network() throws Exception {
        Project updateProject = new Project();
        updateProject.setName("updated project");
        updateProject.setDescription("Project description updated");
        updateProject.setVersion("1.0");
        updateProject.setState(ProjectState.PHASE_OUT);
        updateProject.setPhaseOutSince("2020-06-24");
        String accessToken = TestHelper.getAccessToken(mockMvc, testUserId, testUserPassword);
        if (!SW360Constants.ENABLE_FLEXIBLE_PROJECT_RELEASE_RELATIONSHIP) {
            this.mockMvc
                    .perform(patch("/api/projects/network/376576").contentType(MediaTypes.HAL_JSON)
                            .content(this.objectMapper.writeValueAsString(updateProject))
                            .header("Authorization", "Bearer " + accessToken).accept(MediaTypes.HAL_JSON))
                    .andExpect(status().isInternalServerError());
        } else {
            this.mockMvc
                    .perform(patch("/api/projects/network/376576").contentType(MediaTypes.HAL_JSON)
                            .content(this.objectMapper.writeValueAsString(updateProject))
                            .header("Authorization", "Bearer " + accessToken).accept(MediaTypes.HAL_JSON))
                    .andExpect(status().isOk())
                    .andDo(this.documentationHandler.document(
                            links(linkWithRel("self").description("The <<resources-projects,Projects resource>>")),
                            requestFields(
                                    fieldWithPath("name").description("The name of the project"),
                                    fieldWithPath("type").description("project"),
                                    fieldWithPath("version").description("The version of the new project"),
                                    fieldWithPath("visibility").description("The project visibility, possible values are: "
                                            + Arrays.asList(Visibility.values())),
                                    fieldWithPath("description").description("The project description"),
                                    fieldWithPath("projectType").description("The project type, possible values are: "
                                            + Arrays.asList(ProjectType.values())),
                                    fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                    fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                    fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                    fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                    fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                    fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag.")),
                            responseFields(fieldWithPath("name").description("The name of the project"),
                                    fieldWithPath("version").description("The project version"),
                                    fieldWithPath("createdOn").description("The date the project was created"),
                                    fieldWithPath("description").description("The project description"),
                                    fieldWithPath("domain").description("The domain, possible values are:" + Sw360ResourceServer.DOMAIN.toString()),
                                    fieldWithPath("projectType").description("The project type, possible values are: "
                                            + Arrays.asList(ProjectType.values())),
                                    fieldWithPath("visibility").description("The project visibility, possible values are: "
                                            + Arrays.asList(Visibility.values())),
                                    fieldWithPath("businessUnit").description("The business unit this project belongs to"),
                                    subsectionWithPath("externalIds").description(
                                            "When projects are imported from other tools, the external ids can be stored here. Store as 'Single String' when single value, or 'Array of String' when multi-values"),
                                    subsectionWithPath("additionalData").description("A place to store additional data used by external tools"),
                                    fieldWithPath("ownerAccountingUnit")
                                            .description("The owner accounting unit of the project"),
                                    fieldWithPath("ownerGroup").description("The owner group of the project"),
                                    fieldWithPath("ownerCountry").description("The owner country of the project"),
                                    fieldWithPath("obligationsText").description("The obligations text of the project"),
                                    fieldWithPath("clearingSummary")
                                            .description("The clearing summary text of the project"),
                                    fieldWithPath("specialRisksOSS")
                                            .description("The special risks OSS text of the project"),
                                    fieldWithPath("generalRisks3rdParty")
                                            .description("The general risks 3rd party text of the project"),
                                    fieldWithPath("specialRisks3rdParty")
                                            .description("The special risks 3rd party text of the project"),
                                    fieldWithPath("deliveryChannels")
                                            .description("The sales and delivery channels text of the project"),
                                    fieldWithPath("remarksAdditionalRequirements")
                                            .description("The remark additional requirements text of the project"),
                                    fieldWithPath("tag").description("The project tag"),
                                    fieldWithPath("deliveryStart").description("The project delivery start date"),
                                    fieldWithPath("preevaluationDeadline")
                                            .description("The project preevaluation deadline"),
                                    fieldWithPath("systemTestStart").description("Date of the project system begin phase"),
                                    fieldWithPath("systemTestEnd").description("Date of the project system end phase"),
                                    fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                    fieldWithPath("phaseOutSince").description("The project phase-out date"),
                                    subsectionWithPath("linkedProjects")
                                            .description("The `linked project id` - metadata of linked projects (`enableSvm` - whether linked projects will be part of SVM, `projectRelationship` - relationship between linked project and the project. Possible values: " + Arrays.asList(ProjectRelationship.values())),
                                    fieldWithPath("securityResponsibles").description("An array of users responsible for security of the project."),
                                    fieldWithPath("state").description("The project active status, possible values are: " + Arrays.asList(ProjectState.values())),
                                    fieldWithPath("clearingRequestId").description("Clearing Request id associated with project."),
                                    fieldWithPath("projectResponsible").description("A user who is responsible for the project."),
                                    fieldWithPath("licenseInfoHeaderText").description("License info header text."),
                                    subsectionWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                    subsectionWithPath("_embedded.createdBy").description("The user who created this project"),
                                    fieldWithPath("enableSvm").description("Security vulnerability monitoring flag"),
                                    fieldWithPath("considerReleasesFromExternalList").description("Consider list of releases from existing external list"),
                                    fieldWithPath("enableVulnerabilitiesDisplay").description("Displaying vulnerabilities flag."),
                                    subsectionWithPath("dependencyNetwork").description("Dependency network"),
                                    subsectionWithPath("_embedded.sw360:moderators").description("An array of moderators"),
                                    subsectionWithPath("_embedded.sw360:projectDTOs").description("An array of <<resources-projects, Projects resources>>"),
                                    subsectionWithPath("_embedded.sw360:vendors").description("An array of all component vendors with full name and link to their <<resources-vendor-get,Vendor resource>>"),
                                    subsectionWithPath("_embedded.sw360:attachments").description("An array of all project attachments and link to their <<resources-attachment-get,Attachment resource>>"))));
        }
    }
}
