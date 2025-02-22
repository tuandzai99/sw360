<%--
  ~ Copyright Siemens AG, 2013-2019. Part of the SW360 Portal Project.
  ~
  ~ This program and the accompanying materials are made
  ~ available under the terms of the Eclipse Public License 2.0
  ~ which is available at https://www.eclipse.org/legal/epl-2.0/
  ~
  ~ SPDX-License-Identifier: EPL-2.0
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.eclipse.sw360.datahandler.thrift.ModerationState" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.ComponentType" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.projects.ClearingRequest" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.DateRange" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.ClearingRequestState"%>
<%@ page import="org.eclipse.sw360.datahandler.thrift.ClearingRequestPriority"%>
<%@ page import="org.eclipse.sw360.datahandler.thrift.ClearingRequestType"%>
<%@ page import="com.liferay.portal.kernel.portlet.PortletURLFactoryUtil" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import ="java.util.Date" %>
<%@ page import ="java.text.SimpleDateFormat" %>

<%@ include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@ include file="/html/utils/includes/errorKeyToMessage.jspf"%>


<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="componentType" class="java.lang.String" scope="request"/>
<jsp:useBean id="requestingUser" class="java.lang.String" scope="request"/>
<jsp:useBean id="requestingUserDepartment" class="java.lang.String" scope="request"/>
<jsp:useBean id="moderators" class="java.lang.String" scope="request"/>
<jsp:useBean id="moderationState" class="java.lang.String" scope="request"/>

<portlet:resourceURL var="deleteModerationRequestAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DELETE_MODERATION_REQUEST%>'/>
</portlet:resourceURL>
<portlet:resourceURL var="loadProjectDetailsAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.LOAD_PROJECT_INFO%>'/>
</portlet:resourceURL>

<liferay-portlet:renderURL var="friendlyClearingURL" portletName="sw360_portlet_moderations">
    <portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.FRIENDLY_URL_PLACEHOLDER_PAGENAME%>"/>
    <portlet:param name="<%=PortalConstants.CLEARING_REQUEST_ID%>" value="<%=PortalConstants.FRIENDLY_URL_PLACEHOLDER_ID%>"/>
</liferay-portlet:renderURL>
<liferay-portlet:renderURL var="friendlyProjectURL" portletName="sw360_portlet_projects">
    <portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.FRIENDLY_URL_PLACEHOLDER_PAGENAME%>"/>
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="<%=PortalConstants.FRIENDLY_URL_PLACEHOLDER_ID%>"/>
</liferay-portlet:renderURL>
<portlet:resourceURL var="openModeraionRequestlisturl">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.LOAD_OPEN_MODERATION_REQUEST%>"/>
</portlet:resourceURL>
<portlet:resourceURL var="closedModeraionRequestlisturl">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.LOAD_CLOSED_MODERATION_REQUEST%>"/>
</portlet:resourceURL>
<portlet:actionURL var="applyFiltersURL" name="applyFilters">
</portlet:actionURL>
<jsp:useBean id="clearingRequests" type="java.util.List<org.eclipse.sw360.datahandler.thrift.projects.ClearingRequest>"
             scope="request"/>
<jsp:useBean id="closedClearingRequests" type="java.util.List<org.eclipse.sw360.datahandler.thrift.projects.ClearingRequest>"
             scope="request"/>
<jsp:useBean id="isClearingExpert" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="createdOn" class="java.util.Date"/>
<jsp:useBean id="modifiedOn" class="java.util.Date"/>
<jsp:useBean id="closedOn" class="java.util.Date"/>
<jsp:useBean id="babl" class="org.eclipse.sw360.portal.common.customfields.CustomField" scope="request"/>
<jsp:useBean id="exactMatchCheckBox" class="java.lang.String" scope="request"/>

<core_rt:set var="user" value="<%=themeDisplay.getUser()%>"/>

<div class="container" style="display: none;">
	<div class="row">
		<div class="col-3 sidebar">
            <div class="card-deck hidden" id="general_quick_filter">
                <%@ include file="/html/utils/includes/quickfilter.jspf" %>
            </div>
            <div id="requestTabs" class="list-group" data-initial-tab="${selectedTab}" role="tablist">
                <a class="list-group-item list-group-item-action <core_rt:if test="${selectedTab == 'tab-OpenMR'}">active</core_rt:if>" href="#tab-OpenMR" data-toggle="list" role="tab"><liferay-ui:message key="open.moderation.requests" /></a>
                <a class="list-group-item list-group-item-action <core_rt:if test="${selectedTab == 'tab-ClosedMR'}">active</core_rt:if>" href="#tab-ClosedMR" data-toggle="list" role="tab"><liferay-ui:message key="closed.moderation.requests" /></a>
                <a class="list-group-item list-group-item-action <core_rt:if test="${selectedTab == 'tab-OpenCR'}">active</core_rt:if>" href="#tab-OpenCR" data-toggle="list" role="tab"><liferay-ui:message key="open.clearing.requests" /></a>
                <a class="list-group-item list-group-item-action <core_rt:if test="${selectedTab == 'tab-ClosedCR'}">active</core_rt:if>" href="#tab-ClosedCR" data-toggle="list" role="tab"><liferay-ui:message key="closed.clearing.requests" /></a>
            </div>
            <div class="card-deck hidden" id="adv_search_mod_req">
                <div id="searchInput" class="card">
                    <div class="card-header">
                        <liferay-ui:message key="advanced.search" />
                    </div>
                    <div class="card-body">
                        <form action="<%=applyFiltersURL%>" method="post">
                            <div class="form-group">
                                <span class="d-flex align-items-center mb-2">
                                    <label class="mb-0 mr-auto" for="created_on"><liferay-ui:message key="date" /></label>
                                    <select class="form-control form-control-sm w-50" id="dateRange" name="<portlet:namespace/><%=PortalConstants.DATE_RANGE%>">
                                        <option value="<%=PortalConstants.NO_FILTER%>" class="textlabel stackedLabel"></option>
                                        <sw360:DisplayEnumOptions type="<%=DateRange.class%>" selectedName="${dateRange}" useStringValues="true"/>
                                    </select>
                                </span>
                                <input id="created_on" class="datepicker form-control form-control-sm" autocomplete="off"
                                    name="<portlet:namespace/><%=ModerationRequest._Fields.TIMESTAMP%>" <core_rt:if test="${empty timestamp}"> style="display: none;" </core_rt:if>
                                    type="text" pattern="\d{4}-\d{2}-\d{2}" value="<sw360:out value="${timestamp}"/>" />
                                <label id="toLabel" <core_rt:if test="${empty endDate}"> style="display: none;" </core_rt:if> ><liferay-ui:message key="to" /></label>
                                <input type="text" id="endDate" class="datepicker form-control form-control-sm ml-0" autocomplete="off"
                                    name="<portlet:namespace/><%=PortalConstants.END_DATE%>" <core_rt:if test="${empty endDate}"> style="display: none;" </core_rt:if>
                                    value="<sw360:out value="${endDate}"/>" pattern="\d{4}-\d{2}-\d{2}" />
                            </div>
                            <div class="form-group">
                                <label for="component_type"><liferay-ui:message key="type" /></label>
                                <select class="form-control form-control-sm" id="component_type" name="<portlet:namespace/><%=ModerationRequest._Fields.COMPONENT_TYPE%>">
                                    <option value="<%=PortalConstants.NO_FILTER%>" class="textlabel stackedLabel"></option>
                                    <sw360:DisplayEnumOptions type="<%=ComponentType.class%>" selectedName="${componentType}" useStringValues="true"/>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="document_name"><liferay-ui:message key="document.name" /></label>
                                <input type="text" class="form-control form-control-sm" name="<portlet:namespace/><%=ModerationRequest._Fields.DOCUMENT_NAME%>"
                                    value="<sw360:out value="${documentName}"/>" id="document_name">
                            </div>
                            <div class="form-group">
                                <label for="requesting_user"><liferay-ui:message key="requesting.user.email" /></label>
                                <input type="text" class="form-control form-control-sm" name="<portlet:namespace/><%=ModerationRequest._Fields.REQUESTING_USER%>"
                                    value="<sw360:out value="${requestingUser}"/>" id="requesting_user">
                            </div>
                            <div class="form-group">
                                <label for="requesting_user_department"><liferay-ui:message key="department" /></label>
                                <select class="form-control form-control-sm" id="requesting_user_department" name="<portlet:namespace/><%=ModerationRequest._Fields.REQUESTING_USER_DEPARTMENT%>">
                                    <option value=""/>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="moderators_search"><liferay-ui:message key="moderators" /></label>
                                <input type="text" class="form-control form-control-sm" name="<portlet:namespace/><%=ModerationRequest._Fields.MODERATORS%>"
                                    value="<sw360:out value="${moderators}"/>" id="moderators_search">
                            </div>
                            <div class="form-group">
                                <label for="moderation_state"><liferay-ui:message key="state" /></label>
                                <select class="form-control form-control-sm" id="moderation_state" name="<portlet:namespace/><%=ModerationRequest._Fields.MODERATION_STATE%>">
                                    <option value="<%=PortalConstants.NO_FILTER%>" class="textlabel stackedLabel"></option>
                                    <sw360:DisplayEnumOptions type="<%=ModerationState.class%>" selectedName="${moderationState}" useStringValues="true"/>
                                </select>
                            </div>
                            <div class="form-group">
                                <input class="form-check-input" type="checkbox"  value="On" <core_rt:if test="${exactMatchCheckBox != ''}"> checked="checked"</core_rt:if>
                                       name="<portlet:namespace/><%=PortalConstants.EXACT_MATCH_CHECKBOX%>" />
                                <label class="form-check-label" for="exactMatch"><liferay-ui:message key="exact.match" /></label>
                                <sup title="<liferay-ui:message key="the.search.result.will.display.elements.exactly.matching.the.input.equivalent.to.using.x.around.the.search.keyword" /> <liferay-ui:message key="applied.on.document.name.requesting.user.and.department" />" >
                                    <liferay-ui:icon icon="info-sign"/>
                                </sup>
                            </div>
                            <button type="submit" class="btn btn-primary btn-sm btn-block"><liferay-ui:message key="search" /></button>
                        </form>
                    </div>
                </div>
            </div>
            <div class="card-deck hidden" id="date-quickfilter">
                <div class="card">
                    <div class="card-header">
                        <liferay-ui:message key="advanced.filter" />
                    </div>
                <div class="card-body">
                <form>
                    <div class="form-group">
                        <label for="date_type"><liferay-ui:message key="select.date.type.and.range" />:</label>
                        <select class="form-control form-control-sm cr_filter" id="date_type">
                            <option value="" class="textlabel stackedLabel" ></option>
                            <option value="<%=ClearingRequest._Fields.TIMESTAMP%>" class="textlabel stackedLabel"><liferay-ui:message key="created.on" /></option>
                            <option value="<%=ClearingRequest._Fields.REQUESTED_CLEARING_DATE%>" class="textlabel stackedLabel"><liferay-ui:message key="preferred.clearing.date" /></option>
                            <option value="<%=ClearingRequest._Fields.AGREED_CLEARING_DATE%>" class="textlabel stackedLabel"><liferay-ui:message key="agreed.clearing.date" /></option>
                            <option value="<%=ClearingRequest._Fields.MODIFIED_ON%>" class="textlabel stackedLabel"><liferay-ui:message key="last.updated.on" /></option>
                            <option value="<%=ClearingRequest._Fields.TIMESTAMP_OF_DECISION%>" class="textlabel stackedLabel"><liferay-ui:message key="request.closed.on" /></option>
                        </select>
                    </div>
                    <div class="form-group">
                        <select class="form-control form-control-sm cr_filter" id="date_range" >
                            <option value="" class="textlabel stackedLabel" ></option>
                            <option value="0" class="textlabel stackedLabel"><liferay-ui:message key="today" /></option>
                            <option value="-30" class="textlabel stackedLabel"><liferay-ui:message key="last.30.days" /></option>
                            <option value="-7" class="textlabel stackedLabel"><liferay-ui:message key="last.7.days" /></option>
                            <option value="-15" class="textlabel stackedLabel"><liferay-ui:message key="last.15.days" /></option>
                            <option value="15" class="textlabel stackedLabel"><liferay-ui:message key="next.15.days" /></option>
                            <option value="7" class="textlabel stackedLabel"><liferay-ui:message key="next.7.days" /></option>
                            <option value="30" class="textlabel stackedLabel"><liferay-ui:message key="next.30.days" /></option>
                        </select>
                    </div>
                    <div class="form-group" id="cr_priority_div">
                        <label for="date_type"><liferay-ui:message key="priority" />:</label>
                        <select class="form-control form-control-sm cr_filter" id="cr_priority">
                            <option value="" class="textlabel stackedLabel" ></option>
                            <sw360:DisplayEnumOptions type="<%=ClearingRequestPriority.class%>" useStringValues="true"/>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="date_type"><liferay-ui:message key="ba-bl.slash.group" />:</label>
                        <select class="form-control form-control-sm cr_filter" id="ba_bl">
                            <option value=""></option>
                            <core_rt:if test="${babl.fieldType == 'DROPDOWN' and babl.fieldLabel == 'BA BL'}">
                            <option value="" class="textlabel stackedLabel" disabled="disabled">---- <liferay-ui:message key="business.area.line" /> ----</option>
                            <core_rt:forEach var="opt" items="${babl.options}">
                                <option value="${opt}">${opt}</option>
                            </core_rt:forEach>
                            </core_rt:if>
                            <option value="" class="textlabel stackedLabel" disabled="disabled">---- <liferay-ui:message key="group" /> ----</option>
                            <core_rt:forEach items="${organizations}" var="org">
                                <option value="<sw360:out value="${org.name}"/>"><sw360:out value="${org.name}"/></option>
                            </core_rt:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="date_type"><liferay-ui:message key="status" />:</label>
                        <select class="form-control form-control-sm cr_filter" id="cr_status">
                            <option value="" class="textlabel stackedLabel" ></option>
                            <sw360:DisplayEnumOptions type="<%=ClearingRequestState.class%>"/>
                        </select>
                    </div>
                    <div class="form-group" id="cr_type_div">
                        <label for="date_type"><liferay-ui:message key="clearing.type" />:</label>
                        <select class="form-control form-control-sm cr_filter" id="cr_type">
                            <option value="" class="textlabel stackedLabel" ></option>
                            <sw360:DisplayEnumOptions type="<%=ClearingRequestType.class%>" useStringValues="true"/>
                        </select>
                    </div>
                </form>
                </div>
                </div>
            </div>
		</div>
		<div class="col">
            <div class="row portlet-toolbar">
				<div class="col-auto">

                </div>
                 <div class="col portlet-title text-truncate" title="<liferay-ui:message key="moderations" /> (0/0)">
                    <liferay-ui:message key="moderations" /> (0/<span id="requestCounter">0</span>)
                </div>
            </div>

            <div class="row">
                <div class="col">
                    <div class="tab-content">
                        <div id="tab-OpenMR" class="tab-pane <core_rt:if test="${empty selectedTab}">active show</core_rt:if>">
                            <table id="moderationsTable" class="table table-bordered aligned-top">
			    </table>
			</div>
                        <div id="tab-ClosedMR" class="tab-pane">
                            <table id="closedModerationsTable" class="table table-bordered aligned-top">
                            </table>
                        </div>
                        <div id="tab-OpenCR" class="tab-pane <core_rt:if test="${selectedTab == 'tab-OpenCR'}">active show</core_rt:if>">
                            <table id="clearingRequestsTable" class="table table-bordered">
                            </table>
                        </div>
                        <div id="tab-ClosedCR" class="tab-pane <core_rt:if test="${selectedTab == 'tab-ClosedCR'}">active show</core_rt:if>">
                            <table id="closedClearingRequestsTable" class="table table-bordered">
                            </table>
                        </div>
                    </div>
                </div>
            </div>

		</div>
	</div>
</div>
<%@ include file="/html/utils/includes/pageSpinner.jspf" %>

<div class="dialogs auto-dialogs"></div>

<%--for javascript library loading --%>
<%@ include file="/html/utils/includes/requirejs.jspf" %>
<script>
AUI().use('liferay-portlet-url', function () {
    const buColIndex = 1, projectColIndex = 2, componentColIndex = 3, progressColIndex = 7, maxTextLength = 22;
    require(['jquery', 'bridges/datatables', 'modules/dialog', 'modules/validation', 'modules/listgroup', 'utils/includes/quickfilter', 'utils/render', 'bridges/jquery-ui', 'utils/link'], function($, datatables, dialog, validation, listgroup, quickfilter, render, jqui, linkToutil) {
        var moderationsDataTable,
            closedModerationsDataTable,
            clearingRequestsDataTable,
            closedClearingRequestsDataTable,
            closedModerationRequestsSize = 0,
            openedModerationRequestsSize = 0,
            requestingUserDepartments;

        listgroup.initialize('requestTabs', $('#requestTabs').data('initial-tab') || 'tab-OpenMR');

        moderationsDataTable = createModerationsTable("#moderationsTable", '<%=openModeraionRequestlisturl%>');
        closedModerationsDataTable = createModerationsTable("#closedModerationsTable", '<%=closedModeraionRequestlisturl%>');
        clearingRequestsDataTable = createClearingRequestsTable("#clearingRequestsTable", prepareClearingRequestsData());
        closedClearingRequestsDataTable = createClearingRequestsTable("#closedClearingRequestsTable", prepareClosedClearingRequestsData());

        $('#closedModerationsTable').on('click', 'svg.delete', function(event) {
            var data = $(event.currentTarget).data();
            deleteModerationRequest(data.moderationRequest, data.documentName);
        });

        // Event listener to the two range filtering inputs to redraw on input
        $('#date_type, #date_range, #cr_priority, #ba_bl, #cr_status, #cr_type').on('change', function(e) {
            filterChanged();
        });

        $('.datepicker').datepicker({changeMonth:true,changeYear:true,dateFormat: "yy-mm-dd", maxDate: new Date()}).change(dateChanged).on('changeDate', dateChanged);

        function dateChanged(ev) {
            let id = $(this).attr("id"),
                dt = $(this).val();
            if (id === "created_on") {
                $('#endDate').datepicker('option', 'minDate', dt);
            } else if (id === "endDate") {
                $('#created_on').datepicker('option', 'maxDate', dt ? dt : new Date());
            }
        }

        $('#dateRange').on('change', function (e) {
            let selected = $("#dateRange option:selected").text(),
                $datePkr = $(".datepicker"),
                $toLabel = $("#toLabel");

            if (!selected) {
                $datePkr.hide().val("");
                $toLabel.hide();
                return;
            }

            if (selected === 'Between') {
                $datePkr.show();
                $toLabel.show();
            } else {
                $("#created_on").show();
                $toLabel.hide();
                $("#endDate").hide().val("");
            }
        });

        function filterChanged() {
            let $priority = $('#cr_priority'),
                $babl = $('#ba_bl'),
                $status = $('#cr_status'),
                crPriority = $priority.find(":selected").text(),
                babl = $babl.find(":selected").val(),
                crStatus = $status.find(":selected").text(),
                crType = $('#cr_type').find(":selected").text(),
                tab = $('#requestTabs').find('a.active').attr('href');

            if (tab === '#tab-OpenCR') {
                clearingRequestsDataTable
                .column(1).search(babl)
                .column(4).search(crStatus)
                .column(5).search(crPriority)
                .column(14).search(crType)
                .draw();
            } else if (tab === '#tab-ClosedCR') {
                closedClearingRequestsDataTable
                .column(1).search(babl)
                .column(4).search(crStatus)
                .draw();
            }

            let $dateType = $("#date_type"),
                $dateRange = $('#date_range'),
                dateType = $dateType.find(":selected").val();
                if (dateType) {
                    $dateRange.show();
                    if ( dateType === "<%=ClearingRequest._Fields.TIMESTAMP%>" ||
                            dateType === "<%=ClearingRequest._Fields.MODIFIED_ON%>" ||
                            dateType === "<%=ClearingRequest._Fields.TIMESTAMP_OF_DECISION%>" ) {
                          //iterate through each option
                        $('#date_range option').each(function() {
                            if ($(this).attr("value") > 0) {
                                $(this).hide().prop("disabled", true);
                            }
                        });
                    } else {
                        $('#date_range option').each(function() {
                            if ($(this).attr("value") > 0) {
                                $(this).show().prop("disabled", false);
                            }
                        });
                    }
                } else {
                    $dateRange.val("").hide();
                }
            $.fn.dataTable.ext.search.push(
                    function( settings, data, dataIndex ) {
                        let today = new Date(),
                            dateType = $dateType.find(":selected").val(),
                            days = $dateRange.find(":selected").val(),
                            dateRange = new Date();
                        if (dateType && days) {
                            (days >= 0) ? dateRange.setDate(dateRange.getDate() + Math.abs(days)) : dateRange.setDate(dateRange.getDate() - Math.abs(days));
                            dateRange.setHours(0,0,0,0);
                        } else {
                            return true;
                        }
                        today.setHours(0,0,0,0);
                        let filterDate = new Date("1970-01-01"); // use data for the date column
                        if (dateType === "<%=ClearingRequest._Fields.TIMESTAMP%>" && data[9] && days <= 0) {
                            filterDate = new Date( data[9] );
                        } else if (dateType === "<%=ClearingRequest._Fields.REQUESTED_CLEARING_DATE%>" && data[10]) {
                            filterDate = new Date( data[10] );
                        } else if (dateType === "<%=ClearingRequest._Fields.AGREED_CLEARING_DATE%>" && data[11]) {
                            filterDate = new Date( data[11] );
                        } else if (dateType === "<%=ClearingRequest._Fields.MODIFIED_ON%>" && data[12] && days <= 0) {
                            filterDate = new Date( data[12] );
                        } else if (dateType === "<%=ClearingRequest._Fields.TIMESTAMP_OF_DECISION%>" && data[13] && days <= 0) {
                            filterDate = new Date( data[13] );
                        }
                        filterDate.setHours(0,0,0,0);

                        if ( ( !dateType && !days ) || ( dateType && !days ) ||
                             ( days > 0 && filterDate >= today && filterDate <= dateRange ) ||
                             ( days < 0 && filterDate <= today && filterDate >= dateRange ) ||
                             ( days == 0 && filterDate.getTime() == today.getTime() && filterDate.getTime() == dateRange.getTime() ) )
                        {
                            return true;
                        }
                        return false;
                    }
                );
            if ($('.list-group .list-group-item.active').attr('href') === "#tab-OpenCR") {
                clearingRequestsDataTable.draw();
            } else {
                closedClearingRequestsDataTable.draw();
            }
        }

        // catch ctrl+p and print dataTable
        $(document).on('keydown', function(e){
            if(e.ctrlKey && e.which === 80){
                e.preventDefault();
                moderationsDataTable.buttons('.custom-print-button').trigger();
            }
        });

        $(document).ready(function() {
            docReady();
        });

        function docReady() {
            let tab = $('#requestTabs').find('a.active').attr('href');
            $('#date_range').hide();
            changePortletToolBar(tab);
        }

        function changePortletToolBar(tab) {
            if (tab === '#tab-OpenCR' || tab === '#tab-ClosedCR') {
                let msg = '<liferay-ui:message key="clearing" /> (${clearingRequests.size()}/${closedClearingRequests.size()})';
                $('.portlet-title').attr('title', msg);
                $('.portlet-title').html(msg);
                $('#date-quickfilter').show();
                $('#general_quick_filter').show();
                $('#adv_search_mod_req').hide();
                $('.cr_filter').val("");
                if (tab === '#tab-OpenCR') {
                    $("#date_type option[value="+"<%=ClearingRequest._Fields.TIMESTAMP_OF_DECISION%>"+"]").hide().attr("disabled", "");
                    $("#cr_priority_div").show();
                    $("#cr_type_div").show();
                    $('#cr_status option').each(function() {
                        let val = $(this).attr("value");
                        if (val === "2" || val === "5") {
                            $(this).hide().prop("disabled", true);
                        } else {
                            $(this).show().prop("disabled", false);
                        }
                    });
                } else {
                    $("#date_type option[value="+"<%=ClearingRequest._Fields.TIMESTAMP_OF_DECISION%>"+"]").show().removeAttr("disabled");
                    $("#cr_priority_div").hide();
                    $("#cr_type_div").hide();
                    $('#cr_status option').each(function() {
                        let val = $(this).attr("value");
                        if (val === "" || val === "2" || val === "5") {
                            $(this).show().prop("disabled", false);
                        } else {
                            $(this).hide().prop("disabled", true);
                        }
                    });
                }
            } else {
                $('.portlet-title').attr('title', '<liferay-ui:message key="moderations" /> (' + openedModerationRequestsSize + '/' + closedModerationRequestsSize +')');
                $('.portlet-title').html('<liferay-ui:message key="moderations" /> (' + openedModerationRequestsSize + '/<span id="requestCounter">' + closedModerationRequestsSize +'</span>)');
                $('#date-quickfilter').hide();
                $('#general_quick_filter').hide();
                $('#adv_search_mod_req').show();
            }
        }

        $('a[data-toggle="list"]').on('shown.bs.tab', function (e) {
            changePortletToolBar(e.target.hash);
            filterChanged();
        })

        function createModerationsTable(tableId, url) {
            return datatables.create(tableId, {
                searching: true,
                bServerSide: true,
                sAjaxSource: url,
                columns: [
                    {title: "<liferay-ui:message key="date" />", data: "renderTimestamp", render: {display: render.renderTimestamp}, className: 'text-nowrap' },
                    {title: "<liferay-ui:message key="type" />", data: "componentType", className: 'text-nowrap'},
                    {title: "<liferay-ui:message key="document.name" />", width: "25%", render: {display: detailUrl}, data: "documentName"},
                    {title: "<liferay-ui:message key="requesting.user" />", width: "20%", data: "requestingUser"},
                    {title: "<liferay-ui:message key="department" />", width: "20%", data: "requestingUserDepartment"},
                    {title: "<liferay-ui:message key="moderators" />", width: "35%", data: "moderators", render: {display: renderModeratorsListExpandable}},
                    {title: "<liferay-ui:message key="state" />", data: "moderationState", className: 'text-nowrap'},
                    {title: "<liferay-ui:message key="actions" />", data:"isClearingAdmin", "defaultContent": "", render: {display: closedModAction}, className: 'one action'}
                ],
                language: {
                    url: "<liferay-ui:message key="datatables.lang" />",
                    loadingRecords: "<liferay-ui:message key="loading" />"
                },
                fnDrawCallback: function(settings){
                    openedModerationRequestsSize = settings.json.openModerationRequests;
                    closedModerationRequestsSize = settings.json.closedModerationRequests;
                    requestingUserDepartments = settings.json.requestingUserDepartments;
                    populateRequestingUsersDept(requestingUserDepartments.sort());
                    datatables.showPageContainer;
                    $(tableId + ' .TogglerModeratorsList').on('click', toggleModeratorsList );
                    docReady();
                },
                "order": [[ 0, "desc" ]],
            }, [0,1,2,3,4,5,6], [7]);
        }

        function populateRequestingUsersDept(requestingUserDepts) {
            $('#requesting_user_department').empty();
            $('#requesting_user_department').append("<option value=''/>");
            $.each(requestingUserDepts, function(i,dept) {
                var option = "<option value = '" + dept + "'>" + dept + "</option>";
                $(option).appendTo('#requesting_user_department');
            });
        }

        function closedModAction(isClearingAdmin, type, row) {
            let deleteIcon = '<div class="actions"><svg class="delete lexicon-icon" data-moderation-request="' + row.id + '" data-document-name="' + row.documentName +'"><title><liferay-ui:message key="delete" /></title><use href="/o/org.eclipse.sw360.liferay-theme/images/clay/icons.svg#trash"/></svg></div>';
            let successIcon = '<span class="badge badge-success">READY</span>';

            if(isClearingAdmin) {
                return deleteIcon;
            }
            else if(isClearingAdmin == false) {
                return successIcon;
            }
            return "";
        }

        function detailUrl(name, type, row) {
            let url = linkToutil.to('moderationRequest', 'edit', row.id);
            let viewUrl = $("<a></a>").attr("href",url).css("word-break","break-word").text(name);
            return viewUrl[0].outerHTML;
        }

        function prepareClearingRequestsData() {
            var result = [];
            <core_rt:forEach items="${clearingRequests}" var="request">
            <jsp:setProperty name="createdOn" property="time" value="${request.timestamp}"/>
            <core_rt:if test="${request.modifiedOn > 0}">
                <jsp:setProperty name="modifiedOn" property="time" value="${request.modifiedOn}"/>
            </core_rt:if>
                result.push({
                    "DT_RowId": "${request.id}",
                    "0": "${request.id}",
                    "1": "<liferay-ui:message key="not.loaded.yet" />",
                    "2": "<liferay-ui:message key="not.loaded.yet" />",
                    "3": "<liferay-ui:message key="not.loaded.yet" />",
                    "4": "<sw360:DisplayEnum value="${request.clearingState}"/>",
                    "5": "<sw360:DisplayEnum value="${request.priority}"/>",
                    "6": '<sw360:DisplayUserEmail email="${request.requestingUser}" />',
                    "7": "<liferay-ui:message key="not.loaded.yet" />",
                    "8": '<sw360:DisplayUserEmail email="${request.clearingTeam}" />',
                    "9": '<fmt:formatDate value="${createdOn}" pattern="yyyy-MM-dd"/>',
                    "10": '<sw360:out value="${request.requestedClearingDate}"/>',
                    "11": '<sw360:out value="${request.agreedClearingDate}"/>',
                    "12": '',
                    <core_rt:if test="${request.modifiedOn > 0}">
                        "12": '<fmt:formatDate value="${modifiedOn}" pattern="yyyy-MM-dd"/>',
                    </core_rt:if>
                    "13": '',
                    "14": "<sw360:DisplayEnum value="${request.clearingType}"/>",
                    "15": "${request.projectId}",
                });
            </core_rt:forEach>
            return result;
        }

        function prepareClosedClearingRequestsData() {
            var result = [];
            <core_rt:forEach items="${closedClearingRequests}" var="request">
            <jsp:setProperty name="createdOn" property="time" value="${request.timestamp}"/>
            <core_rt:if test="${request.modifiedOn > 0}">
                <jsp:setProperty name="modifiedOn" property="time" value="${request.modifiedOn}"/>
            </core_rt:if>
            <jsp:setProperty name="closedOn" property="time" value="${request.timestampOfDecision}"/>
                result.push({
                    "DT_RowId": "${request.id}",
                    "0": "${request.id}",
                    "1": "<liferay-ui:message key="not.loaded.yet" />",
                    "2": "<liferay-ui:message key="not.loaded.yet" />",
                    "3": "<liferay-ui:message key="not.loaded.yet" />",
                    "4": "<sw360:DisplayEnum value="${request.clearingState}"/>",
                    "5": "<sw360:DisplayEnum value="${request.priority}"/>",
                    "6": '<sw360:DisplayUserEmail email="${request.requestingUser}" />',
                    "7": "<liferay-ui:message key="not.loaded.yet" />",
                    "8": '<sw360:DisplayUserEmail email="${request.clearingTeam}" />',
                    "9": '<fmt:formatDate value="${createdOn}" pattern="yyyy-MM-dd"/>',
                    "10": '<sw360:out value="${request.requestedClearingDate}"/>',
                    "11": '<sw360:out value="${request.agreedClearingDate}"/>',
                    "12": '',
                    <core_rt:if test="${request.modifiedOn > 0}">
                        "12": '<fmt:formatDate value="${modifiedOn}" pattern="yyyy-MM-dd"/>',
                    </core_rt:if>
                    "13": '<fmt:formatDate value="${closedOn}" pattern="yyyy-MM-dd"/>',
                    "14": '<sw360:out value="${request.clearingType}"/>',
                    "15": "${request.projectId}",
                });
            </core_rt:forEach>
            return result;
        }

        function createClearingRequestsTable(tableId, tableData) {
            let hiddenCol = (tableId === '#clearingRequestsTable') ? [8, 12, 13] : [3, 5, 7, 12, 14];
            return datatables.create(tableId, {
                searching: true,
                deferRender: false, // do not change this value
                data: tableData,
                columns: [
                    {title: "<liferay-ui:message key="request.id" />", render: {display: renderClearingRequestUrl}, className: 'text-nowrap', width: "5%" },
                    {title: "<liferay-ui:message key="ba-bl.slash.group" />", className: 'text-nowrap', width: "7%" },
                    {title: "<liferay-ui:message key="project" />", width: "15%" },
                    {title: "<liferay-ui:message key="open.releases" />", width: "8%" },
                    {title: "<liferay-ui:message key="status" />", width: "8%" },
                    {title: "<liferay-ui:message key="priority" />", width: "7%" },
                    {title: "<liferay-ui:message key="requesting.user" />", className: 'text-nowrap', width: "10%" },
                    {title: "<liferay-ui:message key="clearing.progress" />", width: "15%" },
                    {title: "<liferay-ui:message key="clearing.team" />", className: 'text-nowrap', width: "15%" },
                    {title: "<liferay-ui:message key="created.on" />", className: 'text-nowrap', width: "8%" },
                    {title: "<liferay-ui:message key="preferred.clearing.date" />", width: "8%" },
                    {title: "<liferay-ui:message key="agreed.clearing.date" />", width: "7%" },
                    {title: "<liferay-ui:message key="modified.on" />", width: "7%" },
                    {title: "<liferay-ui:message key="request.closed.on" />", width: "7%" },
                    {title: "<liferay-ui:message key="clearing.type" />", width: "15%" },
                    {title: "<liferay-ui:message key="actions" />", render: {display: renderClearingRequestAction}, className: 'one action',  width: "5%" },
                ],
                language: {
                    emptyTable: "<liferay-ui:message key='no.clearing.request.found'/>"
                },
                columnDefs: [
                    {
                        targets: [0],
                        type: 'natural-nohtml'
                    },
                    {
                        targets: [5],
                        "createdCell": function (td, cellData, rowData, row, col) {
                            $(td).addClass('font-weight-bold');
                            if (rowData[5].includes("sw360-tt-ClearingRequestPriority-LOW")) {
                                $(td).addClass('text-success');
                            } else if (rowData[5].includes("sw360-tt-ClearingRequestPriority-MEDIUM")) {
                                $(td).addClass('text-primary');
                            } else if (rowData[5].includes("sw360-tt-ClearingRequestPriority-HIGH")) {
                                $(td).addClass('text-warning');
                            } else if (rowData[5].includes("sw360-tt-ClearingRequestPriority-CRITICAL")) {
                                $(td).addClass('text-danger');
                            }
                        }                     
                    },
                    {
                        "targets": hiddenCol,
                        "visible": false
                    }
                ],
                order: [[0, 'asc']],
                initComplete: function (oSettings) {
                    datatables.showPageContainer;
                    loadProjectDetails(tableId, tableData);
                }
            }, [0,1,2,3,4,5,6,8,9,10,11,12,13,14], [7,15]);
        }

        function renderClearingRequestUrl(tableData, type, row) {
            let portletURL = '<%=friendlyClearingURL%>';
            return render.linkTo(replaceFriendlyUrlParameter(portletURL.toString(), row.DT_RowId, '<%=PortalConstants.PAGENAME_DETAIL_CLEARING_REQUEST%>'), "", row.DT_RowId);
        }

        function renderLinkToProject(id, name) {
            if (id && name) {
                if (name.length > maxTextLength) {
                    name = name.substring(0, 20) + '...';
                }
                let requestPortletURL = '<%=friendlyProjectURL%>'.replace(/moderation/g, "projects");
                return render.linkTo(replaceFriendlyUrlParameter(requestPortletURL.toString(), id, '<%=PortalConstants.PAGENAME_DETAIL%>'), name);
            } else {
                return '<liferay-ui:message key="deleted.project" />';
            }
        }

        function renderClearingRequestAction(tableData, type, row) {
            let clearingTeam = extractEmailFromHTMLElement(row[8]),
                requestingUser = extractEmailFromHTMLElement(row[6]);
            if (row[15] && (clearingTeam === '${user.emailAddress}' || requestingUser === '${user.emailAddress}' || ${isClearingExpert})) {
                let portletURL = '<%=friendlyClearingURL%>';
                return render.linkTo(replaceFriendlyUrlParameter(portletURL.toString(), row.DT_RowId, '<%=PortalConstants.PAGENAME_EDIT_CLEARING_REQUEST%>'),
                        "",
                        '<div class="actions"><svg class="edit lexicon-icon"><title>Edit</title><use href="/o/org.eclipse.sw360.liferay-theme/images/clay/icons.svg#pencil"/></svg></div>'
                        );
            } else {
                return '';
            }
        }

        // helper functions
        function replaceFriendlyUrlParameter(portletUrl, id, page) {
            return portletUrl
                .replace('<%=PortalConstants.FRIENDLY_URL_PLACEHOLDER_PAGENAME%>', page)
                .replace('<%=PortalConstants.FRIENDLY_URL_PLACEHOLDER_ID%>', id);
        }

        function loadProjectDetails(tableId, tableData) {
            if (!tableData.length) {
                return;
            }
            let projectIds = [], crIds = [], $table = $(tableId), crTable = clearingRequestsDataTable,
                isOpenCrTable = tableId === '#clearingRequestsTable';
            tableData.forEach(myFunction);

            function myFunction(value, index, array) {
                let $buCell = $(tableId).find('tr#'+value.DT_RowId).find('td:eq('+buColIndex+')'),
                    $projCell = $(tableId).find('tr#'+value.DT_RowId).find('td:eq('+projectColIndex+')'),
                    $compCell = $(tableId).find('tr#'+value.DT_RowId).find('td:eq('+componentColIndex+')'),
                    $progressCell = $(tableId).find('tr#'+value.DT_RowId).find('td:eq('+progressColIndex+')');
                if (value[15]) {
                    projectIds.push(value[15]);
                    $buCell.html('<liferay-ui:message key="loading" />');
                    $projCell.html('<liferay-ui:message key="loading" />');
                    if (isOpenCrTable) {
                        $compCell.html('<liferay-ui:message key="loading" />');
                        $progressCell.html('<liferay-ui:message key="loading" />');
                    }
                    value[15] = "";
                } else {
                    crIds.push(value.DT_RowId);
                }
            }
            if (isOpenCrTable && projectIds.length > 25) {
                let i, j, temp, chunk = 25;
                setTimeout(function() {
                    for (i = 0, j = projectIds.length; i < j; i += chunk) {
                        temp = projectIds.slice(i, i + chunk);
                        loadProjectDetailsAjaxCall(tableId, tableData, temp, isOpenCrTable, crIds);
                    }
                }, 1000);
            } else {
                loadProjectDetailsAjaxCall(tableId, tableData, projectIds, isOpenCrTable, crIds);
            }
        }

        function loadProjectDetailsAjaxCall(tableId, tableData, projectIds, isOpenCrTable, crIds) {
            $.ajax({
                type: 'POST',
                url: '<%=loadProjectDetailsAjaxURL%>',
                cache: false,
                data: {
                    "<portlet:namespace/>projectIds": projectIds,
                    "<portlet:namespace/>isOpenCr": isOpenCrTable
                },
                success: function (response) {
                    function d(v) { return v == undefined ? 0 : v; }
                    function setProgress(totalCount, approvedCount, $pBar, pCell) {
                        if (approvedCount == 0) {
                            let progressText = "(0/"+totalCount+") "+"<liferay-ui:message key="none.of.the.directly.linked.releases.are.cleared" />";
                            $pBar.find('span').text("0%").removeClass('text-dark').addClass('text-danger');
                            $(pCell.node()).attr("title", progressText);
                        } else if (approvedCount === totalCount) {
                            let progressText = "("+totalCount+"/"+totalCount+") "+"<liferay-ui:message key="all.of.the.directly.linked.releases.are.cleared" />";
                            $pBar.find('span').text("100%").removeClass('text-dark').addClass('text-success');;
                            $(pCell.node()).attr("title", progressText);
                        } else {
                            let progressPercentage = ((approvedCount / totalCount) * 100).toFixed(0),
                                progressText = "("+ approvedCount +"/"+totalCount+") "+"<liferay-ui:message key="directly.linked.releases.are.cleared" />";
                            $pBar.find("span").text(progressPercentage + "%");
                            $(pCell.node()).attr("title", progressText);
                        }
                        return $pBar;
                    }

                    let table = isOpenCrTable ? clearingRequestsDataTable : closedClearingRequestsDataTable,
                        $progressBar = $('<div/>', {
                            'class': 'progress h-100 rounded-0',
                            'style': 'font-size: 100%;'
                        }),
                        $innerDiv = $('<div/>', {
                            'class': 'progress-bar progress-bar-striped',
                            'role': "progressbar",
                            'aria-valuenow': '0',
                            'aria-valuemin': '0',
                            'aria-valuemax': '100',
                            'style': 'width: 0%; overflow: visible;'
                        }),
                        $span = $('<span/>', {
                            'class': 'text-dark font-weight-bold'
                        });
                    $innerDiv.append('<span class="text-dark font-weight-bold"></span>');

                    for (let i = 0; i < response.length; i++) {
                        let crId = response[i].crId,
                            crIdCell = table.cell('#'+crId, 0),
                            buCell = table.cell('#'+crId, buColIndex),
                            projCell = table.cell('#'+crId, projectColIndex),
                            compCell = table.cell('#'+crId, componentColIndex),
                            progressCell = table.cell('#'+crId, progressColIndex),
                            projName = response[i].name;

                        buCell.data(response[i].bu);
                        projCell.data(renderLinkToProject(response[i].id, projName));
                        if (isOpenCrTable) {
                            let clearing = response[i].clearing,
                                totalCount = (!clearing) ? 0 : d(clearing.newRelease) + d(clearing.underClearing) + d(clearing.sentToClearingTool) + d(clearing.reportAvailable) + d(clearing.approved) + d(clearing.scanAvailable),
                                approvedCount = (!clearing) ? 0 : d(clearing.reportAvailable) + d(clearing.approved);
                            compCell.data(totalCount - approvedCount);
                            if (!totalCount || $(table.cell('#'+crId, 4).node()).find('span.sw360-tt-ClearingRequestState-NEW').text()) {
                                progressCell.data('<liferay-ui:message key="not.available" />');
                            } else {
                                progressCell.data(setProgress(totalCount, approvedCount, $innerDiv.clone(), progressCell)[0].outerHTML);
                            }
                        }
                        if (projName.length > maxTextLength) {
                            $(projCell.node()).attr("title", projName);
                        }
                    }
                    for (let i = 0; i < crIds.length; i++) {
                        let crId = crIds[i],
                            buCell = table.cell('#'+crId, buColIndex),
                            projCell = table.cell('#'+crId, projectColIndex),
                            compCell = table.cell('#'+crId, componentColIndex),
                            progressCell = table.cell('#'+crId, progressColIndex);
                        buCell.data('<liferay-ui:message key="not.available" />');
                        projCell.data('<liferay-ui:message key="deleted.project" />');
                        if (isOpenCrTable) {
                            compCell.data('<liferay-ui:message key="not.available" />');
                            progressCell.data('<liferay-ui:message key="not.available" />');
                        }
                    }
                    quickfilter.addTable(table);
                },
                error: function () {
                    for (var i = 0; i < tableData.length; i++) {
                        $table.find('tr#'+tableData[i].DT_RowId).find('td:eq('+buColIndex+')').html('<liferay-ui:message key="failed.to.load" />');
                        $table.find('tr#'+tableData[i].DT_RowId).find('td:eq('+projectColIndex+')').html('<liferay-ui:message key="failed.to.load" />');
                    }
                }
            });
        }

        function deleteModerationRequest(id, docName) {
            var $dialog;

            function deleteModerationRequestInternal(callback) {
                jQuery.ajax({
                    type: 'POST',
                    url: '<%=deleteModerationRequestAjaxURL%>',
                    cache: false,
                    data: {
                        <portlet:namespace/>moderationId: id
                    },
                    success: function (data) {
                        callback();

                        if (data.result == 'SUCCESS') {
                            closedModerationsDataTable.row('#' + id).remove().draw(false);
                            $('#requestCounter').text(parseInt($('#requestCounter').text()) - 1);
                            $('#requestCounter').parent().attr('title', $('#requestCounter').parent().text());
                            $dialog.close();
                        } else {
                            $dialog.alert('<liferay-ui:message key="i.could.not.delete.the.moderation.request" />');
                        }
                    },
                    error: function () {
                        callback();
                        $dialog.alert('<liferay-ui:message key="i.could.not.delete.the.moderation.request" />');
                    }
                });
            }

            $dialog = dialog.confirm(
                'danger',
                'question-circle',
                '<liferay-ui:message key="delete.moderation.request" />?',
                '<p><liferay-ui:message key="do.you.really.want.to.delete.the.moderation.request.x" /></p>',
                '<liferay-ui:message key="delete.moderation.request" />',
                {
                    name: docName,
                },
                function(submit, callback) {
                    deleteModerationRequestInternal(callback);
                }
            );
        }

        function stringToHtml(htmlText, trim) {
            if (typeof trim === 'number') {
                return htmlText = '<span title="'+htmlText+'">'+htmlText.substring(0, trim)+'...</span>';
            }
        }

            $('.datepicker').datepicker({
                minDate: new Date(),
                changeMonth: true,
                changeYear: true,
                dateFormat: "yy-mm-dd"
            });
    });

    function extractEmailFromHTMLElement(link) {
        return $(link).prop('href').substr('7');
    }

    function cutModeratorsList(moderators) {
        if (moderators) {
            var firstEmail = extractEmailFromHTMLElement(moderators.split(",")[0]);
            return  firstEmail.substring(0, 20) + "...";
        } else {
            return "";
        }
    }

    function renderModeratorsListExpandable(moderators) {
        var $container = $('<div/>', {
                style: 'display: flex;'
            }),
            $toggler = $('<div/>', {
                'class': 'TogglerModeratorsList',
                'style': 'margin-right: 0.25rem; cursor: pointer;'
            }),
            $togglerOn = $('<div/>', {
                'class': 'Toggler_on'
            }).html('&#x25BC'),
            $togglerOff = $('<div/>', {
                'class': 'Toggler_off'
            }).html('&#x25BA'),
            $collapsed = $('<div/>', {
                'class': 'ModeratorsListHidden'
            }).text(cutModeratorsList(moderators)),
            $expanded = $('<div/>', {
                'class': 'ModeratorsListShown'
            }).html(moderators);

        $togglerOn.hide();
        $expanded.hide();
        $toggler.append($togglerOff, $togglerOn);
        $container.append($toggler, $collapsed, $expanded);
        return $container[0].outerHTML;
    }

    function toggleModeratorsList() {
        var toggler_off = $(this).find('.Toggler_off');
        var toggler_on = $(this).find('.Toggler_on');
        var parent = $(this).parent();
        var ModeratorsListHidden = parent.find('.ModeratorsListHidden');
        var ModeratorsListShown = parent.find('.ModeratorsListShown');

        toggler_off.toggle();
        toggler_on.toggle();
        ModeratorsListHidden.toggle();
        ModeratorsListShown.toggle();
    }
    require(['jquery', 'utils/link'], function($, linkutil) {
        if (window.history.replaceState) {
            window.history.replaceState(null, document.title, linkutil.to('moderationRequest', 'list', ""));
        }
    });
});
</script>
