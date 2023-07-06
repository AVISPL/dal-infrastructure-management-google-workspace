/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

/**
 * GoogleWorkspaceCommand constant command URLs for Google Workspace operations.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/22/2023
 * @since 1.0.0
 */
public class GoogleWorkspaceCommand {
	public static final String USER_COMMAND = "/admin/directory/v1/users?domain=&maxResults=5";
	public static final String ORG_UNIT_COMMAND = "/admin/directory/v1/customer/{CustomerId}/orgunits";
	public static final String CHROME_OS_COMMAND = "/admin/directory/v1/customer/{CustomerId}/devices/chromeos?orgUnitPath=";
	public static final String PARENT_ORG_UNIT_COMMAND = "/admin/directory/v1/customer/{CustomerId}/orgunits/";
}
