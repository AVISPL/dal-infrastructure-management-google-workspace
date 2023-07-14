/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.dto.aggregatedInfo;

/**
 * Class representing a known network.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/20/2023
 * @since 1.0.0
 */
public class NetworkDTO {
	private String ipAddress;
	private String wanIpAddress;

	/**
	 * Retrieves {@link #ipAddress}
	 *
	 * @return value of {@link #ipAddress}
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets {@link #ipAddress} value
	 *
	 * @param ipAddress new value of {@link #ipAddress}
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Retrieves {@link #wanIpAddress}
	 *
	 * @return value of {@link #wanIpAddress}
	 */
	public String getWanIpAddress() {
		return wanIpAddress;
	}

	/**
	 * Sets {@link #wanIpAddress} value
	 *
	 * @param wanIpAddress new value of {@link #wanIpAddress}
	 */
	public void setWanIpAddress(String wanIpAddress) {
		this.wanIpAddress = wanIpAddress;
	}
}
