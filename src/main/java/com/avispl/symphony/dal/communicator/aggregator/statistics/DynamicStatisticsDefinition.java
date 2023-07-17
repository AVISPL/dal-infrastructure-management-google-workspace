/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.statistics;

/**
 * DynamicStatisticsDefinition is Enum representing dynamic statistics definitions.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 7/11/2023
 * @since 1.0.0
 */
public enum DynamicStatisticsDefinition {
	CORE_1("Core1(C)"),
	CORE_0("Core0(C)"),
	PACKAGE_ID_0("PackageId0(C)"),
	IWL_WIFI("IwlWifi_1(C)"),
	;

	private final String name;

	/**
	 * Constructs a DynamicStatisticsDefinition enum with the specified name.
	 *
	 * @param name The name of the dynamic statistic definition.
	 */
	DynamicStatisticsDefinition(final String name) {
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
