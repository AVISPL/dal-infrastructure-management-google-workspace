/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

import java.util.Arrays;
import java.util.Optional;

/**
 * GoogleCPUTemperatureMetric include different CPU temperature metrics for Google devices.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/20/2023
 * @since 1.0.0
 */
public enum GoogleCPUTemperatureMetric {
	CORE_1("Core1(C)", "Core 1\n"),
	PACKAGE_ID_0("PackageId0(C)", "Package id 0\n"),
	CORE_0("Core0(C)", "Core 0\n"),
	IWL_WIFI_1("IwlWifi_1(C)", "iwlwifi_1\n"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructs a GoogleCPUTemperatureMetric enum constant with the specified name and value.
	 *
	 * @param name the name of the temperature metric
	 * @param value the value of the temperature metric
	 */
	GoogleCPUTemperatureMetric(String name, String value) {
		this.name = name;
		this.value = value;
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
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the GoogleCPUTemperatureMetric enum constant associated with the specified name.
	 *
	 * @param name the name of the temperature metric
	 * @return the GoogleCPUTemperatureMetric enum constant with the specified name
	 * @throws IllegalStateException if the specified name is not supported
	 */
	public static GoogleCPUTemperatureMetric getByName(String name) {
		Optional<GoogleCPUTemperatureMetric> property = Arrays.stream(GoogleCPUTemperatureMetric.values()).filter(group -> group.getName().equals(name)).findFirst();
		if (property.isPresent()) {
			return property.get();
		} else {
			throw new IllegalStateException(String.format("control group %s is not supported.", name));
		}
	}
}
