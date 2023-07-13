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
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;

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
		googleWorkspaceCommunicator.setPassword("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 1//04tDvzCfQx9IQCgYIARAAGAQSNwF-L9Ir-JA7TJ3U2D04WcmmyseCQWF6BRa60-vg_uqDkUk_k_wk9MTcTVmzC0zXKlyrpaeNf6Q");
		googleWorkspaceCommunicator.setPort(443);
		googleWorkspaceCommunicator.init();
		googleWorkspaceCommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		googleWorkspaceCommunicator.disconnect();
		googleWorkspaceCommunicator.destroy();
	}

	/**
	 * Unit test for the {@code getAggregatorData()} method.
	 * It asserts the size of the statistics map to be 7.
	 *
	 * @throws Exception if an exception occurs during the test.
	 */
	@Test
	void testGetAggregatorData() throws Exception {
		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Assert.assertEquals(7, statistics.size());
		Assert.assertEquals(1, advancedControllablePropertyList.size());
	}

	/**
	 * Unit test for the {@code getAggregatorDataAndFiltering()} method.
	 * It sets filter criteria for organization unit, current organization unit name, and serial number.
	 * Asserts the size of the statistics map to be 7.
	 *
	 * @throws Exception if an exception occurs during the test.
	 */
	@Test
	void testGetAggregatorDataAndFiltering() throws Exception {
		googleWorkspaceCommunicator.setFilterOrgUnit("AVI-SPL Labs");
		googleWorkspaceCommunicator.setFilterSerialNumber("48JR9FCNB03207P");
		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Assert.assertEquals(7, statistics.size());
		Assert.assertEquals(0, advancedControllablePropertyList.size());
	}

	/**
	 * Unit test for the {@code getAggregatorDataAndFailedFiltering()} method.
	 *
	 * This test verifies the behavior of the {@code getAggregatorDataAndFailedFiltering()} method in the GoogleWorkspaceCommunicator class.
	 * It sets the filter criteria for organization unit and serial number, which are intentionally set to fail the filtering.
	 * It asserts that the size of the statistics map is 2 and the size of the advancedControllablePropertyList is 0.
	 *
	 * @throws Exception if an exception occurs during the test.
	 */
	@Test
	void testGetAggregatorDataAndFailedFiltering() throws Exception {
		googleWorkspaceCommunicator.setFilterOrgUnit("AVI-SPL Labs");
		googleWorkspaceCommunicator.setFilterSerialNumber("AAAA");
		extendedStatistic = (ExtendedStatistics) googleWorkspaceCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		List<AdvancedControllableProperty> advancedControllablePropertyList = extendedStatistic.getControllableProperties();
		Assert.assertEquals(2, statistics.size());
		Assert.assertEquals(0, advancedControllablePropertyList.size());
	}

	/**
	 * Unit test for the {@code getMultipleStatistics()} method.
	 *
	 * This test verifies the behavior of the {@code getMultipleStatistics()} method in the GoogleWorkspaceCommunicator class.
	 * It sets the current organization unit name, filter organization unit, and filter serial number to empty strings.
	 * Finally, it retrieves the list of aggregated devices and asserts that the list size is 1 and the properties size is 27.
	 *
	 * @throws Exception if an exception occurs during the test.
	 */
	@Test
	void testGetMultipleStatistics() throws Exception {
		googleWorkspaceCommunicator.setCurrentOrgUnitName("");
		googleWorkspaceCommunicator.setFilterOrgUnit("");
		googleWorkspaceCommunicator.setFilterSerialNumber("");
		googleWorkspaceCommunicator.getMultipleStatistics();
		googleWorkspaceCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = googleWorkspaceCommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(1, aggregatedDeviceList.size());
		Assert.assertEquals(27, aggregatedDeviceList.get(0).getProperties().size());
	}

	/**
	 * Unit test for the {@code getMultipleStatistics()} method with historical properties.
	 *
	 * This test verifies the behavior of the {@code getMultipleStatistics()} method in the GoogleWorkspaceCommunicator class when historical properties are set.
	 * It sets the historical properties to "Core1(C),Core0(C),PackageId0(C),IwlWifi_1(C)".
	 * Finally, it retrieves the list of aggregated devices and asserts that the list size is 1, the properties size is 23, and the dynamic statistics size is 4.
	 *
	 * @throws Exception if an exception occurs during the test.
	 */
	@Test
	void testGetMultipleStatisticsWithHistorical() throws Exception {
		googleWorkspaceCommunicator.setHistoricalProperties("Core1(C),Core0(C),PackageId0(C),IwlWifi_1(C)");
		googleWorkspaceCommunicator.getMultipleStatistics();
		googleWorkspaceCommunicator.retrieveMultipleStatistics();
		Thread.sleep(30000);
		List<AggregatedDevice> aggregatedDeviceList = googleWorkspaceCommunicator.retrieveMultipleStatistics();
		Assert.assertEquals(1, aggregatedDeviceList.size());
		Assert.assertEquals(23, aggregatedDeviceList.get(0).getProperties().size());
		Assert.assertEquals(4, aggregatedDeviceList.get(0).getDynamicStatistics().size());
	}

	/**
	 * Unit test for controlling the change of organizational unit name.
	 *
	 * This test verifies the behavior of changing the organizational unit name using the {@code controlProperty()} method in the GoogleWorkspaceCommunicator class.
	 * It retrieves the extendedStatistic object from the first element of the list returned by {@code getMultipleStatistics()}.
	 * The statistics map and the advancedControllablePropertyList are obtained from the extendedStatistic object.
	 * Finally, it asserts that the value of the first advancedControllableProperty in the list is equal to the expected value.
	 *
	 * @throws Exception if an exception occurs during the test.
	 */
	@Test
	void tesControlChangeOrgUnitName() throws Exception {
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
