/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.dto.aggregatedInfo;

/**
 * Class representing CPU temperature information.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/20/2023
 * @since 1.0.0
 */
public class CPUTemperature {
	private int temperature;
	private String label;

	/**
	 * Retrieves {@link #temperature}
	 *
	 * @return value of {@link #temperature}
	 */
	public int getTemperature() {
		return temperature;
	}

	/**
	 * Sets {@link #temperature} value
	 *
	 * @param temperature new value of {@link #temperature}
	 */
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	/**
	 * Retrieves {@link #label}
	 *
	 * @return value of {@link #label}
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets {@link #label} value
	 *
	 * @param label new value of {@link #label}
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
