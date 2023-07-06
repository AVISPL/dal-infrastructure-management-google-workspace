package com.avispl.symphony.dal.communicator.aggregator;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;

/**
 * GoogleWorkspaceCommunicator
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 6/19/2023
 * @since 1.0.0
 */
public class GoogleWorkspaceCommunicatorTest {
	private GoogleWorkspaceCommunicator googleWorkspaceCommunicator;

	private ExtendedStatistics extendedStatistic;

	@BeforeEach
	void setUp() throws Exception {
		googleWorkspaceCommunicator = new GoogleWorkspaceCommunicator();
		googleWorkspaceCommunicator.setHost("admin.googleapis.com");
		googleWorkspaceCommunicator.setLogin("***REMOVED***");
		googleWorkspaceCommunicator.setPassword("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");
		googleWorkspaceCommunicator.setPort(443);
		googleWorkspaceCommunicator.init();
		googleWorkspaceCommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		googleWorkspaceCommunicator.disconnect();
		googleWorkspaceCommunicator.destroy();
	}

	@Test
	void testGetAggregatorData() throws Exception {
		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Assert.assertEquals(7, statistics.size());
		Assert.assertEquals(1, advancedControllablePropertyList.size());
	}

	@Test
	void testGetAggregatorDataWithFiltering() throws Exception {
		googleWorkspaceCommunicator.setFilterOrgUnit("MOTF");
		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Assert.assertEquals(7, statistics.size());
		Assert.assertEquals(0, advancedControllablePropertyList.size());
	}

	@Test
	void tesControl() throws Exception {
		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();

		String property = "OrganizationalUnits#Name";
		String value = "MOTF";
		ControllableProperty controllableProperty = new ControllableProperty();
		controllableProperty.setProperty(property);
		controllableProperty.setValue(value);
		googleWorkspaceCommunicator.controlProperty(controllableProperty);

		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		statistics = extendedStatistic.getStatistics();
		advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Assertions.assertEquals(value, advancedControllablePropertyList.get(0).getValue());
	}
}
