/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

/**
 * GoogleWorkspaceAggregatedDeviceMetric include different aggregated device metrics for Google Workspace.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/20/2023
 * @since 1.0.0
 */
public enum GoogleWorkspaceAggregatedDeviceMetric {
	STATUS("Status"),
	SERIAL_NUMBER("SerialNumber"),
	CPU_MODEL("CPUModel"),
	ARCHITECTURE("Architecture"),
	MAC_ADDRESS("MACAddress"),
	CHROME_OS_VERSION("ChromeOSVersion"),
	PLATFORM_VERSION("PlatformVersion"),
	FIRMWARE_VERSION("FirmwareVersion"),
	TPM_FIRMWARE_VERSION("TPMFirmwareVersion"),
	BOOT_MODE("BootMode"),
	ENROLLMENT_TIME("EnrollmentTime"),
	AUTO_UPDATE_EXPIRATION("AutoUpdateExpiration"),
	ANNOTATED_USER("AnnotatedUser"),
	ORG_UNIT("OrgUnit"),
	MEMORY_TOTAL("MemoryTotal(GB)"),
	LAST_SYNC("LastSync"),
	MEMORY_FREE("MemoryFree(GB)"),
	CPU_UTILIZATION("CPUUtilization(%)"),
	RECENT_ACTIVITY("RecentActivity"),
	RECENT_USERS("RecentUsers"),
	IP_ADDRESS("IPAddress"),
	WAN_IP_ADDRESS("WANIPAddress"),
	VOLUME_LEVEL("VolumeLevel(%)"),
	;
	private final String name;

	/**
	 * Represents an aggregated metric in the Google Workspace.
	 * This class is used to store the name of the metric.
	 *
	 * @param name the name of the aggregated metric
	 */
	GoogleWorkspaceAggregatedDeviceMetric(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}
}
