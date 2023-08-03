/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

import java.util.Arrays;

/**
 * CPUTemperatureEnum include different CPU temperature metrics for Google devices.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/20/2023
 * @since 1.0.0
 */
public enum CPUTemperatureEnum {
	CORE_1("Core1(C)", "Core 1\n"),
	PACKAGE_ID_0("PackageId0(C)", "Package id 0\n"),
	CORE_0("Core0(C)", "Core 0\n"),
	IWL_WIFI_1("IwlWifi_1(C)", "iwlwifi_1\n"),
	;
	private final String name;
	private final String value;

	/**
	 * Constructs a CPUTemperatureEnum enum constant with the specified name and value.
	 *
	 * @param name the name of the temperature metric
	 * @param value the value of the temperature metric
	 */
	CPUTemperatureEnum(String name, String value) {
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
	 * Finds the name associated with a specific value in the CPUTemperatureEnum.
	 *
	 * @param value The value to search for.
	 * @return The name corresponding to the provided value, or null if no match is found.
	 */
	public static String findNameByValue(String value) {
		CPUTemperatureEnum matchedEnum = Arrays.stream(CPUTemperatureEnum.values())
				.filter(definition -> definition.getValue().equals(value))
				.findFirst()
				.orElse(null);

		return matchedEnum != null ? matchedEnum.getName() : null;
	}
}
