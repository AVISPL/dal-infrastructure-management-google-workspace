/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

/**
 * GoogleWorkspaceConstant constants related to Google Workspace.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/19/2023
 * @since 1.0.0
 */
public class GoogleWorkspaceConstant {
	public static final String NONE = "None";
	public static final String EMPTY = "";
	public static final String USERS = "users";
	public static final String NAME = "name";
	public static final String CUSTOMER_ID = "customerId";
	public static final String ORGANIZATION_UNIT = "organizationUnits";
	public static final String CHROME_OS_DEVICE = "chromeosdevices";
	public static final String ORG_UNIT_PATH = "orgUnitPath";
	public static final String DEVICE_ID = "deviceId";
	public static final String DEVICES = "devices";
	public static final String LAST_SYNC = "LastSync";
	public static final String CPU_TEMPERATURE_INFO = "cpuTemperatureInfo";
	public static final String SYSTEM_RAM_FREE_INFO = "systemRamFreeInfo";
	public static final String CPU_UTILIZATION_PERCENTAGE_INFO = "cpuUtilizationPercentageInfo";
	public static final String EMAIL = "email";
	public static final String DATE = "date";
	public static final String ACTIVE_TIME = "activeTime";
	public static final String ON = "on ";
	public static final String SSL = "SSL";
	public static final String AUDIO_STATUS_REPORT = "audioStatusReport";
	public static final String OUTPUT_VOLUME = "outputVolume";
	public static final String CHROME_OS_DEVICES_COUNT = "ChromeOSDevicesCount";
	public static final String ORGANIZATIONAL_UNIT_COUNT = "OrganizationalUnitsCount";
	public static final String CHROMEBOOK = "Chromebook";
	public static final String NEXT_TOKEN = "nextPageToken";
	public static final String VOLUME_LEVEL = "VolumeLevel(%)";
	public static final String DEFAULT_FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final String NEW_FORMAT_DATETIME = "MMM dd, yyyy, h:mm a";
	public static final String UTC = "UTC";
	public static final String MODEL_MAPPING_AGGREGATED_DEVICE = "googleworkspace/model-mapping.yml";
	public static final String OAUTH2_URL = "https://oauth2.googleapis.com/token";
	public static final String PATH_VARIABLE_CUSTOMER_ID = "{CustomerId}";
	public static final String PATH_VARIABLE_ORG_UNIT = "{orgUnit}";
	public static final String PATH_VARIABLE_SERIAL_NUMBER = "{serialNumber}";
	public static final String CPU_TEMPERATURE_GROUP = "CPUTemperature#";
	public static final String ORGANIZATIONAL_UNITS_GROUP = "OrganizationalUnits#";
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String GRANT_TYPE = "grant_type";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String NEXT_TOKEN_REQUEST_PARAM = "&pageToken=";
	public static final String SPACE_REGEX = "\\s+";
	public static final String COLON = ":";
	public static final String COMMA = ",";
	public static final String HASH = "#";
	public static final String SLASH = "//";
	public static final String SPACE = " ";
	public static final String SECOND = " sec ";
	public static final String MINUTE = " min ";
	public static final String HOUR = " h ";
	public static final int MAC_ADDRESS_LENGTH = 12;
	public static final int DEFAULT_ORG_UNIT_POSITION = 0;
	public static final int MIN_VOLUME_LEVEL = 0;
	public static final String REGEX_SERIAL_NUMBER = "^[a-zA-Z0-9]+$";
	public static final String CPU_TEMPERATURE = "CPU_Temperature";
}
