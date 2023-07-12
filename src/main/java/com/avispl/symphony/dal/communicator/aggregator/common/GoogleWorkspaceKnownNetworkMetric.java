/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * GoogleWorkspaceKnownNetworkMetric contain different known network metrics for Google Workspace.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/20/2023
 * @since 1.0.0
 */
public enum GoogleWorkspaceKnownNetworkMetric {
	IP_ADDRESS("IPAddress"),
	WAN_IP_ADDRESS("WANIPAddress"),
	;
	private final String name;

	/**
	 * Constructs a GoogleWorkspaceKnownNetworkMetric enum constant with the specified name.
	 *
	 * @param name the name of the known network metric
	 */
	GoogleWorkspaceKnownNetworkMetric(String name) {
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

	/**
	 * Returns the GoogleWorkspaceKnownNetworkMetric enum constant associated with the specified name.
	 *
	 * @param name the name of the known network metric
	 * @return the GoogleWorkspaceKnownNetworkMetric enum constant with the specified name
	 * @throws IllegalStateException if the specified name is not supported
	 */
	public static GoogleWorkspaceKnownNetworkMetric getByName(String name) {
		Optional<GoogleWorkspaceKnownNetworkMetric> property = Arrays.stream(GoogleWorkspaceKnownNetworkMetric.values()).filter(group -> group.getName().equals(name)).findFirst();
		if (property.isPresent()) {
			return property.get();
		} else {
			throw new IllegalStateException(String.format("control group %s is not supported.", name));
		}
	}
}
