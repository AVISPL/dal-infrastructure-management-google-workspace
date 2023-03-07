/*
 * Copyright (c) 2022 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator;

import java.util.List;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;

/**
 * GoogleWorkspaceAggregatorDevice class provides during the monitoring and controlling process
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/6/2023
 * @since 1.0.0
 */
public class GoogleWorkspaceAggregatorDevice extends RestCommunicator implements Aggregator, Monitorable, Controller {
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		return null;
	}

	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {

	}

	@Override
	public void controlProperties(List<ControllableProperty> list) throws Exception {

	}

	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {

		return (List<AggregatedDevice>) new AggregatedDevice();
	}

	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
		return (List<AggregatedDevice>) new AggregatedDevice();
	}

	@Override
	protected void authenticate() throws Exception {

	}
}