/*
 * Sonatype Maven Shell (TM) Commercial Version.
 *
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/mvnsh/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

package com.sonatype.maven.shell.nexus.internal.marshal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sonatype.gshell.util.marshal.Marshaller;
import org.sonatype.gshell.util.marshal.MarshallerSupport;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.rest.model.ErrorMessage;
import org.sonatype.nexus.rest.model.ErrorReportRequest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.ErrorResponse;
import org.sonatype.nexus.rest.model.FeedListResource;
import org.sonatype.nexus.rest.model.FeedListResourceResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResourceResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;
import org.sonatype.nexus.rest.model.NFCRepositoryResource;
import org.sonatype.nexus.rest.model.NFCResource;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusListResource;
import org.sonatype.nexus.rest.model.RepositoryStatusListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.model.StatusConfigurationValidationResponse;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.rest.model.WastebasketResourceResponse;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.security.rest.model.ClientPermission;
import org.sonatype.security.rest.model.ExternalRoleMappingResource;
import org.sonatype.security.rest.model.ExternalRoleMappingResourceResponse;
import org.sonatype.security.rest.model.PlexusRoleListResourceResponse;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserResourceResponse;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.model.PrivilegeProperty;
import org.sonatype.security.rest.model.PrivilegeResource;
import org.sonatype.security.rest.model.PrivilegeResourceRequest;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.security.rest.model.PrivilegeTypePropertyResource;
import org.sonatype.security.rest.model.PrivilegeTypeResource;
import org.sonatype.security.rest.model.PrivilegeTypeResourceResponse;
import org.sonatype.security.rest.model.RoleListResourceResponse;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;
import org.sonatype.security.rest.model.UserChangePasswordRequest;
import org.sonatype.security.rest.model.UserForgotPasswordRequest;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;
import org.sonatype.security.rest.model.UserToRoleResource;
import org.sonatype.security.rest.model.UserToRoleResourceRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Support for {@link MarshallerFactory} implementations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class MarshallerFactorySupport
    implements MarshallerFactory
{
    private final XStream xs;

    public MarshallerFactorySupport() {
        this.xs = new XStream();
        configure(xs);
    }

    protected XStream configure(final XStream xs) {
        assert xs != null;

//        xs.registerConverter(new RepositoryBaseResourceConverter(xs.getMapper(), xs
//            .getReflectionProvider()), XStream.PRIORITY_VERY_HIGH);
//        xs.registerConverter(new RepositoryResourceResponseConverter(xs.getMapper(), xs
//            .getReflectionProvider()), XStream.PRIORITY_VERY_HIGH); // strips the class="class.name" attribute from data
//
//        xs.registerConverter(new ScheduledServiceBaseResourceConverter(xs.getMapper(), xs
//            .getReflectionProvider()), XStream.PRIORITY_VERY_HIGH);
//        xs.registerConverter(new ScheduledServicePropertyResourceConverter(xs.getMapper(), xs
//            .getReflectionProvider()), XStream.PRIORITY_VERY_HIGH);
//        xs.registerConverter(new ScheduledServiceResourceResponseConverter(xs.getMapper(), xs
//            .getReflectionProvider()), XStream.PRIORITY_VERY_HIGH); // strips the class="class.name" attribute from data

//        // Maven POM
//        xs.alias("project", Model.class);
//        // Maven model
//        xs.omitField(Model.class, "modelEncoding");
//        xs.omitField(ModelBase.class, "modelEncoding");
//        xs.omitField(Scm.class, "modelEncoding");

        // omitting modelEncoding
//        xs.omitField(ErrorResponse.class, "modelEncoding");
//        xs.omitField(ErrorMessage.class, "modelEncoding");
        xs.alias("nexus-error", ErrorResponse.class);
        xs.alias("error", ErrorMessage.class);
        xs.registerLocalConverter(ErrorResponse.class, "errors", new AliasingListConverter(ErrorMessage.class,
            "error"));

//        xs.omitField(ContentListResourceResponse.class, "modelEncoding");
//        xs.omitField(ContentListResource.class, "modelEncoding");
        xs.alias("content", ContentListResourceResponse.class);
        xs.alias("content-item", ContentListResource.class);
        xs.registerLocalConverter(ContentListResourceResponse.class, "data", new AliasingListConverter(
            ContentListResource.class, "content-item"));

//        xs.omitField(RepositoryResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryBaseResource.class, "modelEncoding");
//        xs.omitField(RepositoryResource.class, "modelEncoding");
//        xs.omitField(RepositoryProxyResource.class, "modelEncoding");
//        xs.omitField(RepositoryShadowResource.class, "modelEncoding");
//        xs.omitField(RepositoryResourceRemoteStorage.class, "modelEncoding");
        xs.alias("repository", RepositoryResourceResponse.class);

//        xs.omitField(RepositoryListResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryListResource.class, "modelEncoding");
        xs.alias("repositories", RepositoryListResourceResponse.class);
        // xstream.alias( "repositories-item", RepositoryListResource.class);
        xs.registerLocalConverter(RepositoryListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryListResource.class, "repositories-item"));

        xs.alias("repositoryTypes", NexusRepositoryTypeListResourceResponse.class);
        xs.registerLocalConverter(NexusRepositoryTypeListResourceResponse.class, "data",
            new AliasingListConverter(NexusRepositoryTypeListResource.class, "repositoryType"));

//        xs.omitField(RepositoryStatusResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryStatusResource.class, "modelEncoding");
        xs.alias("repository-status", RepositoryStatusResourceResponse.class);

//        xs.omitField(RepositoryStatusListResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryStatusListResource.class, "modelEncoding");
        xs.alias("repository-status-list", RepositoryStatusListResourceResponse.class);
        // xstream.alias( "repository-status-list-item", RepositoryStatusListResource.class);
        xs.registerLocalConverter(RepositoryStatusListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryStatusListResource.class, "repository-status-list-item"));

//        xs.omitField(RepositoryMetaResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryMetaResource.class, "modelEncoding");
        xs.alias("repository-meta-data", RepositoryMetaResourceResponse.class);

//        xs.omitField(RepositoryGroupListResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryGroupListResource.class, "modelEncoding");
        xs.alias("repo-group-list", RepositoryGroupListResourceResponse.class);
        // xstream.alias( "repo-group-list-item", RepositoryGroupListResource.class);
        // xstream.alias( "repo-group-member", RepositoryGroupMemberRepository.class);
        xs.registerLocalConverter(RepositoryGroupListResource.class, "repositories", new AliasingListConverter(
            RepositoryGroupMemberRepository.class, "repo-group-member"));
        xs.registerLocalConverter(RepositoryGroupResource.class, "repositories", new AliasingListConverter(
            RepositoryGroupMemberRepository.class, "repo-group-member"));
        xs.registerLocalConverter(RepositoryGroupListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryGroupListResource.class, "repo-group-list-item"));

//        xs.omitField(RepositoryGroupResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryGroupResource.class, "modelEncoding");
//        xs.omitField(RepositoryGroupMemberRepository.class, "modelEncoding");
        xs.alias("repo-group", RepositoryGroupResourceResponse.class);

//        xs.omitField(RepositoryRouteListResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryRouteListResource.class, "modelEncoding");
        xs.alias("repo-routes-list", RepositoryRouteListResourceResponse.class);
        // xstream.alias( "repo-routes-list-item", RepositoryRouteListResource.class);
        // xstream.alias( "repo-routes-member", RepositoryRouteMemberRepository.class);
        xs.registerLocalConverter(RepositoryRouteListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryRouteListResource.class, "repo-routes-list-item"));
        xs.registerLocalConverter(RepositoryRouteListResource.class, "repositories", new AliasingListConverter(
            RepositoryRouteMemberRepository.class, "repo-routes-member"));

//        xs.omitField(RepositoryRouteResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryRouteResource.class, "modelEncoding");
//        xs.omitField(RepositoryRouteMemberRepository.class, "modelEncoding");
        xs.alias("repo-route", RepositoryRouteResourceResponse.class);
        xs.registerLocalConverter(RepositoryRouteResource.class, "repositories", new AliasingListConverter(
            RepositoryRouteMemberRepository.class, "repository"));

//        xs.omitField(GlobalConfigurationListResourceResponse.class, "modelEncoding");
//        xs.omitField(GlobalConfigurationListResource.class, "modelEncoding");
        xs.alias("global-settings-list", GlobalConfigurationListResourceResponse.class);
        // xstream.alias( "global-settings-list-item", GlobalConfigurationListResource.class);
        xs.registerLocalConverter(GlobalConfigurationListResourceResponse.class, "data",
            new AliasingListConverter(GlobalConfigurationListResource.class, "global-settings-list-item"));

//        xs.omitField(GlobalConfigurationResourceResponse.class, "modelEncoding");
//        xs.omitField(GlobalConfigurationResource.class, "modelEncoding");
//        xs.omitField(RemoteConnectionSettings.class, "modelEncoding");
//        xs.omitField(RemoteHttpProxySettings.class, "modelEncoding");
//        xs.omitField(RestApiSettings.class, "modelEncoding");
//        xs.omitField(AuthenticationSettings.class, "modelEncoding");
//        xs.omitField(SmtpSettings.class, "modelEncoding");
//        xs.omitField(ErrorReportingSettings.class, "modelEncoding");
        xs.alias("global-settings", GlobalConfigurationResourceResponse.class);

//        xs.omitField(WastebasketResource.class, "modelEncoding");
//        xs.omitField(WastebasketResourceResponse.class, "modelEncoding");
        xs.alias("wastebasket", WastebasketResourceResponse.class);

//        xs.omitField(LogsListResourceResponse.class, "modelEncoding");
//        xs.omitField(LogsListResource.class, "modelEncoding");
        xs.alias("logs-list", LogsListResourceResponse.class);
        // xstream.alias( "logs-list-item", LogsListResource.class);
        xs.registerLocalConverter(LogsListResourceResponse.class, "data", new AliasingListConverter(
            LogsListResource.class, "logs-list-item"));

//        xs.omitField(ConfigurationsListResourceResponse.class, "modelEncoding");
//        xs.omitField(ConfigurationsListResource.class, "modelEncoding");
        xs.alias("configs-list", ConfigurationsListResourceResponse.class);
        // xstream.alias( "configs-list-tem", ConfigurationsListResource.class);
        xs.registerLocalConverter(ConfigurationsListResourceResponse.class, "data", new AliasingListConverter(
            ConfigurationsListResource.class, "configs-list-tem"));

//        xs.omitField(FeedListResourceResponse.class, "modelEncoding");
//        xs.omitField(FeedListResource.class, "modelEncoding");
        xs.alias("feeds-list", FeedListResourceResponse.class);
        // xstream.alias( "feeds-list-item", FeedListResource.class);
        xs.registerLocalConverter(FeedListResourceResponse.class, "data", new AliasingListConverter(
            FeedListResource.class, "feeds-list-item"));

//        xs.omitField(SearchResponse.class, "modelEncoding");
        xs.alias("search-results", SearchResponse.class);
        xs.registerLocalConverter(SearchResponse.class, "data", new AliasingListConverter(NexusArtifact.class,
            "artifact"));

//        xs.omitField(NexusResponse.class, "modelEncoding");
//        xs.omitField(NexusArtifact.class, "modelEncoding");
        xs.alias("artifact", NexusArtifact.class);

//        xs.omitField(AuthenticationLoginResourceResponse.class, "modelEncoding");
//        xs.omitField(AuthenticationLoginResource.class, "modelEncoding");
//        xs.omitField(AuthenticationClientPermissions.class, "modelEncoding");
//        xs.omitField(NexusAuthenticationClientPermissions.class, "modelEncoding");

        // Look at NexusAuthenticationLoginResourceConverter, we are only converting the clientPermissions field
        xs.alias("authentication-login", AuthenticationLoginResourceResponse.class);

        xs.registerLocalConverter(AuthenticationClientPermissions.class, "permissions",
            new AliasingListConverter(ClientPermission.class, "permission"));

//        xs.omitField(ClientPermission.class, "modelEncoding");

//        xs.omitField(StatusResource.class, "modelEncoding");
//        xs.omitField(StatusResourceResponse.class, "modelEncoding");
//        xs.omitField(StatusConfigurationValidationResponse.class, "modelEncoding");
        xs.alias("status", StatusResourceResponse.class);
        xs.registerLocalConverter(StatusConfigurationValidationResponse.class, "validationErrors",
            new AliasingListConverter(String.class, "error"));
        xs.registerLocalConverter(StatusConfigurationValidationResponse.class, "validationWarnings",
            new AliasingListConverter(String.class, "warning"));

//        xs.omitField(ScheduledServiceListResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceListResourceResponse.class, "modelEncoding");
//        xs.omitField(ScheduledServiceBaseResource.class, "modelEncoding");
//        xs.omitField(ScheduledServicePropertyResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceOnceResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceDailyResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceAdvancedResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceMonthlyResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceWeeklyResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceResourceResponse.class, "modelEncoding");
//        xs.omitField(ScheduledServiceTypeResourceResponse.class, "modelEncoding");
//        xs.omitField(ScheduledServiceTypeResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceTypePropertyResource.class, "modelEncoding");
//        xs.omitField(ScheduledServiceResourceStatus.class, "modelEncoding");
//        xs.omitField(ScheduledServiceResourceStatusResponse.class, "modelEncoding");
        xs.alias("schedules-list", ScheduledServiceListResourceResponse.class);
        // xstream.alias( "schedules-list-item", ScheduledServiceListResource.class );
        xs.alias("scheduled-task", ScheduledServiceResourceResponse.class);
        // xstream.alias( "scheduled-task-property", ScheduledServicePropertyResource.class );
        xs.alias("schedule-types", ScheduledServiceTypeResourceResponse.class);
        xs.alias("schedule-type", ScheduledServiceTypeResource.class);
        // xstream.alias( "schedule-type-property", ScheduledServiceTypePropertyResource.class );
        xs.registerLocalConverter(ScheduledServiceBaseResource.class, "properties", new AliasingListConverter(
            ScheduledServicePropertyResource.class, "scheduled-task-property"));
        xs.registerLocalConverter(ScheduledServiceWeeklyResource.class, "recurringDay",
            new AliasingListConverter(String.class, "day"));
        xs.registerLocalConverter(ScheduledServiceTypeResourceResponse.class, "data", new AliasingListConverter(
            ScheduledServiceTypeResource.class, "schedule-type"));
        xs.registerLocalConverter(ScheduledServiceTypeResource.class, "properties", new AliasingListConverter(
            ScheduledServiceTypePropertyResource.class, "scheduled-task-property"));
        xs.registerLocalConverter(ScheduledServiceListResourceResponse.class, "data", new AliasingListConverter(
            ScheduledServiceListResource.class, "schedules-list-item"));

//        xs.omitField(UserListResourceResponse.class, "modelEncoding");
//        xs.omitField(UserResourceRequest.class, "modelEncoding");
//        xs.omitField(UserResourceResponse.class, "modelEncoding");
//        xs.omitField(UserResource.class, "modelEncoding");
//        xs.omitField(UserForgotPasswordRequest.class, "modelEncoding");
//        xs.omitField(UserForgotPasswordResource.class, "modelEncoding");
//        xs.omitField(UserChangePasswordRequest.class, "modelEncoding");
//        xs.omitField(UserChangePasswordResource.class, "modelEncoding");
        xs.alias("users-list", UserListResourceResponse.class);
        // xstream.alias( "users-list-item", UserResource.class );
        xs.alias("user-request", UserResourceRequest.class);
        xs.alias("user-response", UserResourceResponse.class);
        xs.alias("user-forgotpw", UserForgotPasswordRequest.class);
        xs.alias("user-changepw", UserChangePasswordRequest.class);
        xs.registerLocalConverter(UserResource.class, "roles", new AliasingListConverter(String.class, "role"));
        xs.registerLocalConverter(UserListResourceResponse.class, "data", new AliasingListConverter(
            UserResource.class, "users-list-item"));

//        xs.omitField(RoleListResourceResponse.class, "modelEncoding");
//        xs.omitField(RoleResource.class, "modelEncoding");
//        xs.omitField(RoleResourceRequest.class, "modelEncoding");
//        xs.omitField(RoleResourceResponse.class, "modelEncoding");
        xs.alias("roles-list", RoleListResourceResponse.class);
        // xstream.alias( "roles-list-item", RoleResource.class );
        xs.alias("role-request", RoleResourceRequest.class);
        xs.alias("role-response", RoleResourceResponse.class);
        xs.registerLocalConverter(RoleListResourceResponse.class, "data", new AliasingListConverter(
            RoleResource.class, "roles-list-item"));
        xs.registerLocalConverter(RoleResource.class, "roles", new AliasingListConverter(String.class, "role"));
        xs.registerLocalConverter(RoleResource.class, "privileges", new AliasingListConverter(String.class,
            "privilege"));

//        xs.omitField(PrivilegeResourceRequest.class, "modelEncoding");
//        xs.omitField(PrivilegeResource.class, "modelEncoding");
//        xs.omitField(PrivilegeStatusResource.class, "modelEncoding");
//        xs.omitField(PrivilegeListResourceResponse.class, "modelEncoding");
//        xs.omitField(PrivilegeStatusResourceResponse.class, "modelEncoding");
//        xs.omitField(PrivilegeProperty.class, "modelEncoding");
//        xs.omitField(PrivilegeTypeResource.class, "modelEncoding");
//        xs.omitField(PrivilegeTypePropertyResource.class, "modelEncoding");
//        xs.omitField(PrivilegeTypeResourceResponse.class, "modelEncoding");
        xs.alias("privilege-request", PrivilegeResourceRequest.class);
        xs.alias("privilege-list-response", PrivilegeListResourceResponse.class);
        xs.alias("privilege-status-response", PrivilegeStatusResourceResponse.class);
        xs.alias("privilege-type-response", PrivilegeTypeResourceResponse.class);
        xs.aliasField("methods", PrivilegeResource.class, "method");
        xs.registerLocalConverter(PrivilegeListResourceResponse.class, "data", new AliasingListConverter(
            PrivilegeStatusResource.class, "privilege-item"));
        xs.registerLocalConverter(PrivilegeResource.class, "method", new AliasingListConverter(String.class,
            "method"));
        xs.registerLocalConverter(PrivilegeStatusResource.class, "properties", new AliasingListConverter(
            PrivilegeProperty.class, "privilege-property"));
        xs.registerLocalConverter(PrivilegeTypeResourceResponse.class, "data", new AliasingListConverter(
            PrivilegeTypeResource.class, "privilege-type"));
        xs.registerLocalConverter(PrivilegeTypeResource.class, "properties", new AliasingListConverter(
            PrivilegeTypePropertyResource.class, "privilege-type-property"));

//        xs.omitField(NFCResourceResponse.class, "modelEncoding");
//        xs.omitField(NFCResource.class, "modelEncoding");
//        xs.omitField(NFCRepositoryResource.class, "modelEncoding");
        xs.alias("nfc-info", NFCResourceResponse.class);
        // xstream.alias( "nfc-repo-info", NFCRepositoryResource.class );
        xs.registerLocalConverter(NFCResource.class, "nfcContents", new AliasingListConverter(
            NFCRepositoryResource.class, "nfc-repo-info"));
        xs.registerLocalConverter(NFCRepositoryResource.class, "nfcPaths", new AliasingListConverter(
            String.class, "path"));

//        xs.omitField(RepositoryTargetListResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryTargetListResource.class, "modelEncoding");
//        xs.omitField(RepositoryTargetResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryTargetResource.class, "modelEncoding");
        xs.alias("repo-targets-list", RepositoryTargetListResourceResponse.class);
        // xstream.alias( "repo-targets-list-item", RepositoryTargetListResource.class );
        xs.alias("repo-target", RepositoryTargetResourceResponse.class);
        xs.registerLocalConverter(RepositoryTargetResource.class, "patterns", new AliasingListConverter(
            String.class, "pattern"));
        xs.registerLocalConverter(RepositoryTargetListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryTargetListResource.class, "repo-targets-list-item"));

//        xs.omitField(RepositoryContentClassListResourceResponse.class, "modelEncoding");
//        xs.omitField(RepositoryContentClassListResource.class, "modelEncoding");
        xs.alias("repo-content-classes-list", RepositoryContentClassListResourceResponse.class);
        // xstream.alias( "repo-content-classes-list-item", RepositoryContentClassListResource.class );
        xs.registerLocalConverter(RepositoryContentClassListResourceResponse.class, "data",
            new AliasingListConverter(RepositoryContentClassListResource.class, "repo-content-classes-list-item"));

//        xs.omitField(PlexusComponentListResourceResponse.class, "modelEncoding");
//        xs.omitField(PlexusComponentListResource.class, "modelEncoding");
        xs.alias("components-list", PlexusComponentListResourceResponse.class);
        xs.alias("component", PlexusComponentListResource.class);
        xs.registerLocalConverter(PlexusComponentListResourceResponse.class, "data", new AliasingListConverter(
            PlexusComponentListResource.class, "component"));

//        xs.omitField(UserToRoleResourceRequest.class, "modelEncoding");
//        xs.omitField(UserToRoleResource.class, "modelEncoding");
        xs.alias("user-to-role", UserToRoleResourceRequest.class);
        xs.registerLocalConverter(UserToRoleResource.class, "roles", new AliasingListConverter(String.class,
            "role"));

//        xs.omitField(PlexusUserResourceResponse.class, "modelEncoding");
        xs.alias("plexus-user", PlexusUserResourceResponse.class);
//        xs.omitField(PlexusUserResource.class, "modelEncoding");
        xs.registerLocalConverter(PlexusUserResource.class, "roles", new AliasingListConverter(
            PlexusRoleResource.class, "plexus-role"));

//        xs.omitField(PlexusRoleResource.class, "modelEncoding");
        xs.alias("plexus-role", PlexusRoleResource.class);

//        xs.omitField(PlexusUserListResourceResponse.class, "modelEncoding");
        xs.alias("plexus-user-list", PlexusUserListResourceResponse.class);
        xs.registerLocalConverter(PlexusUserListResourceResponse.class, "data", new AliasingListConverter(
            PlexusUserResource.class, "plexus-user"));

//        xs.omitField(ExternalRoleMappingResourceResponse.class, "modelEncoding");
        xs.alias("external-role-mapping", ExternalRoleMappingResourceResponse.class);
        xs.registerLocalConverter(ExternalRoleMappingResourceResponse.class, "data", new AliasingListConverter(
            ExternalRoleMappingResource.class, "mapping"));
//        xs.omitField(ExternalRoleMappingResource.class, "modelEncoding");
        xs.alias("mapping", ExternalRoleMappingResource.class);
        xs.registerLocalConverter(ExternalRoleMappingResource.class, "mappedRoles", new AliasingListConverter(
            PlexusRoleResource.class, "plexus-role"));

//        xs.omitField(PlexusRoleListResourceResponse.class, "modelEncoding");
        xs.alias("plexus-roles", PlexusRoleListResourceResponse.class);
        xs.registerLocalConverter(PlexusRoleListResourceResponse.class, "data", new AliasingListConverter(
            PlexusRoleResource.class, "plexus-role"));

//        xs.omitField(PlexusUserSearchCriteriaResourceRequest.class, "modelEncoding");
        xs.alias("user-search", PlexusUserSearchCriteriaResourceRequest.class);
//        xs.omitField(PlexusUserSearchCriteriaResource.class, "modelEncoding");

//        xs.omitField(ErrorReportRequest.class, "modelEncoding");
//        xs.omitField(ErrorReportRequestDTO.class, "modelEncoding");
//        xs.omitField(ErrorReportResponse.class, "modelEncoding");
//        xs.omitField(ErrorReportResponseDTO.class, "modelEncoding");

        xs.alias("error-report-request", ErrorReportRequest.class);
        xs.alias("error-report-response", ErrorReportResponse.class);

//        xs.omitField(MirrorResourceListRequest.class, "modelEncoding");
//        xs.omitField(MirrorResourceListResponse.class, "modelEncoding");
//        xs.omitField(MirrorStatusResourceListResponse.class, "modelEncoding");
//        xs.omitField(MirrorResource.class, "modelEncoding");
//        xs.omitField(MirrorStatusResource.class, "modelEncoding");

        xs.alias("mirror-list-request", MirrorResourceListRequest.class);
        xs.alias("mirror-list-response", MirrorResourceListResponse.class);
        xs.alias("mirror-status-list-response", MirrorStatusResourceListResponse.class);

        xs.registerLocalConverter(MirrorResourceListRequest.class, "data", new AliasingListConverter(
            MirrorResource.class, "mirrorResource"));
        xs.registerLocalConverter(MirrorResourceListResponse.class, "data", new AliasingListConverter(
            MirrorResource.class, "mirrorResource"));
        xs.registerLocalConverter(MirrorStatusResourceListResponse.class, "data", new AliasingListConverter(
            MirrorStatusResource.class, "mirrorResource"));

        return xs;
    }

    public <T> Marshaller<T> create(final Class<T> type) {
        return new MarshallerSupport<T>(type) {
            @Override
            protected XStream createXStream() {
                return xs;
            }
        };
    }

    protected class AliasingListConverter
        implements Converter
    {
        private Class<?> type;

        private String alias;

        public AliasingListConverter(Class<?> type, String alias) {
            this.type = type;
            this.alias = alias;
        }

        @SuppressWarnings("unchecked")
        public boolean canConvert(Class type) {
            return List.class.isAssignableFrom(type);
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            List<?> list = (List<?>) source;
            for (Object elem : list) {
                if (!elem.getClass().isAssignableFrom(type)) {
                    throw new ConversionException("Found " + elem.getClass() + ", expected to find: " + this.type + " in List.");
                }

                ExtendedHierarchicalStreamWriterHelper.startNode(writer, alias, elem.getClass());
                context.convertAnother(elem);
                writer.endNode();
            }
        }

        @SuppressWarnings("unchecked")
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            List list = new ArrayList();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                list.add(context.convertAnother(list, type));
                reader.moveUp();
            }
            return list;
        }
    }
}