/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.aggregator.parser.AggregatedDeviceProcessor;
import com.avispl.symphony.dal.aggregator.parser.PropertiesMapping;
import com.avispl.symphony.dal.aggregator.parser.PropertiesMappingParser;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.communicator.aggregator.common.AggregatedDeviceEnum;
import com.avispl.symphony.dal.communicator.aggregator.common.CPUTemperatureEnum;
import com.avispl.symphony.dal.communicator.aggregator.common.GoogleWorkspaceCommand;
import com.avispl.symphony.dal.communicator.aggregator.common.GoogleWorkspaceConstant;
import com.avispl.symphony.dal.communicator.aggregator.common.OrgUnitEnum;
import com.avispl.symphony.dal.communicator.aggregator.dto.aggregatedInfo.CPUTemperature;
import com.avispl.symphony.dal.communicator.aggregator.dto.aggregatedInfo.NetworkDTO;
import com.avispl.symphony.dal.communicator.aggregator.dto.systemInfo.OrgUnit;
import com.avispl.symphony.dal.communicator.aggregator.statistics.DynamicStatisticsDefinition;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * GoogleWorkspaceAggregatorDevice class provides during the monitoring and controlling process
 * Supported features are:
 * Monitoring Aggregator Device:
 * <ul>
 * <li> - ChromeOSDevicesCount</li>
 * <li> - OrganizationalUnitsCount</li>
 * <li> - OrganizationalUnits#ChromeOSDevicesCount</li>
 * <li> - OrganizationalUnits#Description</li>
 * <li> - OrganizationalUnits#Name</li>
 * <li> - OrganizationalUnits#ParentName</li>
 * <li> - OrganizationalUnits#UnitID</li>
 * </ul>
 * Monitoring Aggregated Device:
 * <ul>
 * <li> - AnnotatedUser</li>
 * <li> - Architecture</li>
 * <li> - AutoUpdateExpiration</li>
 * <li> - BootMode</li>
 * <li> - ChromeOSVersion</li>
 * <li> - CPUModel</li>
 * <li> - CPUUtilization(%)</li>
 * <li> - deviceId</li>
 * <li> - deviceModel</li>
 * <li> - deviceName</li>
 * <li> - deviceOnline</li>
 * <li> - EnrollmentTime</li>
 * <li> - FirmwareVersion</li>
 * <li> - IPAddress</li>
 * <li> - LastSync</li>
 * <li> - MACAddress</li>
 * <li> - MemoryFree(GB)</li>
 * <li> - MemoryTotal(GB)</li>
 * <li> - OrgUnit</li>
 * <li> - PlatformVersion</li>
 * <li> - RecentActivity</li>
 * <li> - RecentUsers</li>
 * <li> - SerialNumber</li>
 * <li> - Status</li>
 * <li> - TPMFirmwareVersion</li>
 * <li> - VolumeLevel(%)</li>
 * <li> - WANIPAddress</li>
 * <li> - CPUTemperature#Core0(C)</li>
 * <li> - CPUTemperature#Core1(C)</li>
 * <li> - CPUTemperature#IwlWifi_1(C)</li>
 * <li> - CPUTemperature#PackageId0(C)</li>
 * </ul>
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 3/6/2023
 * @since 1.0.0
 */
public class GoogleWorkspaceCommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {
	/**
	 * Process that is running constantly and triggers collecting data from Google Workspace API endpoints, based on the given timeouts and thresholds.
	 *
	 * @author Harry
	 * @since 1.0.0
	 */
	class GoogleWorkspaceDataLoader implements Runnable {
		private volatile boolean inProgress;
		private volatile boolean flag = false;

		public GoogleWorkspaceDataLoader() {
			inProgress = true;
		}

		@Override
		public void run() {
			loop:
			while (inProgress) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// Ignore for now
				}

				if (!inProgress) {
					break loop;
				}

				// next line will determine whether Poly Lens monitoring was paused
				updateAggregatorStatus();
				if (devicePaused) {
					continue loop;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Fetching other than Chrome OS device list");
				}
				long currentTimestamp = System.currentTimeMillis();
				if (!flag && nextDevicesCollectionIterationTimestamp <= currentTimestamp) {
					populateDeviceDetails();
					flag = true;
				}

				while (nextDevicesCollectionIterationTimestamp > System.currentTimeMillis()) {
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						//
					}
				}

				if (!inProgress) {
					break loop;
				}
				if (flag) {
					nextDevicesCollectionIterationTimestamp = System.currentTimeMillis() + 30000;
					flag = false;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Finished collecting devices statistics cycle at " + new Date());
				}
			}
			// Finished collecting
		}

		/**
		 * Triggers main loop to stop
		 */
		public void stop() {
			inProgress = false;
		}
	}

	/**
	 * Private variable representing the local extended statistics.
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * A private final ReentrantLock instance used to provide exclusive access to a shared resource
	 * that can be accessed by multiple threads concurrently. This lock allows multiple reentrant
	 * locks on the same shared resource by the same thread.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * A mapper for reading and writing JSON using Jackson library.
	 * ObjectMapper provides functionality for converting between Java objects and JSON.
	 * It can be used to serialize objects to JSON format, and deserialize JSON data to objects.
	 */
	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Indicates whether a device is considered as paused.
	 * True by default so if the system is rebooted and the actual value is lost -> the device won't start stats
	 * collection unless the {@link GoogleWorkspaceCommunicator#retrieveMultipleStatistics()} method is called which will change it
	 * to a correct value
	 */
	private volatile boolean devicePaused = true;

	/**
	 * We don't want the statistics to be collected constantly, because if there's not a big list of devices -
	 * new devices' statistics loop will be launched before the next monitoring iteration. To avoid that -
	 * this variable stores a timestamp which validates it, so when the devices' statistics is done collecting, variable
	 * is set to currentTime + 30s, at the same time, calling {@link #retrieveMultipleStatistics()} and updating the
	 * {@link #aggregatedDeviceList} resets it to the currentTime timestamp, which will re-activate data collection.
	 */
	private long nextDevicesCollectionIterationTimestamp;

	/**
	 * This parameter holds timestamp of when we need to stop performing API calls
	 * It used when device stop retrieving statistic. Updated each time of called #retrieveMultipleStatistics
	 */
	private volatile long validRetrieveStatisticsTimestamp;

	/**
	 * Aggregator inactivity timeout. If the {@link GoogleWorkspaceCommunicator#retrieveMultipleStatistics()}  method is not
	 * called during this period of time - device is considered to be paused, thus the Cloud API
	 * is not supposed to be called
	 */
	private static final long retrieveStatisticsTimeOut = 3 * 60 * 1000;

	/**
	 * Executor that runs all the async operations, that is posting and
	 */
	private ExecutorService executorService;

	/**
	 * A private field that represents an instance of the PolyLensDataLoader class, which is responsible for loading device data for PolyLens.
	 */
	private GoogleWorkspaceDataLoader deviceDataLoader;

	/**
	 * An instance of the AggregatedDeviceProcessor class used to process and aggregate device-related data.
	 */
	private AggregatedDeviceProcessor aggregatedDeviceProcessor;

	/**
	 * SSL certificate
	 */
	private SSLContext sslContext;

	/**
	 * Google Workspace API Token
	 */
	private String apiToken;

	/**
	 * customer ID used to perform commands related to orgUnit Chrome OS and telemetry.
	 */
	private String customerId;

	/**
	 * save time get token
	 */
	private Long tokenExpire;

	/**
	 * time the token expires
	 */
	private Long expiresIn = 3000L * 1000;

	/**
	 * save nextToken for next request
	 */
	private String nextTokenChromeOS = GoogleWorkspaceConstant.EMPTY;

	/**
	 * save nextToken for next request
	 */
	private String nextTokenTelemetry = GoogleWorkspaceConstant.EMPTY;

	/**
	 * List of aggregated device
	 */
	private List<AggregatedDevice> aggregatedDeviceList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * List of orgUnit
	 */
	private List<OrgUnit> orgUnitList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * contains information of aggregated devices
	 */
	private JsonNode aggregatedDeviceResponse;

	/**
	 * current orgUnit Name
	 */
	private String currentOrgUnitName;

	/**
	 * filter by Serial Number
	 */
	private String filterSerialNumber;

	/**
	 * filter by Org Unit
	 */
	private String filterOrgUnit;

	/**
	 * Configurable property for historical properties, comma separated values kept as set locally
	 */
	private Set<String> historicalProperties = new HashSet<>();

	/**
	 * Retrieves {@link #historicalProperties}
	 *
	 * @return value of {@link #historicalProperties}
	 */
	public String getHistoricalProperties() {
		return String.join(GoogleWorkspaceConstant.COMMA, this.historicalProperties);
	}

	/**
	 * Sets {@link #historicalProperties} value
	 *
	 * @param historicalProperties new value of {@link #historicalProperties}
	 */
	public void setHistoricalProperties(String historicalProperties) {
		this.historicalProperties.clear();
		Arrays.asList(historicalProperties.split(GoogleWorkspaceConstant.COMMA)).forEach(propertyName -> {
			this.historicalProperties.add(propertyName.trim());
		});
	}

	/**
	 * Sets {@link #currentOrgUnitName} value
	 *
	 * @param currentOrgUnitName new value of {@link #currentOrgUnitName}
	 */
	public void setCurrentOrgUnitName(String currentOrgUnitName) {
		this.currentOrgUnitName = currentOrgUnitName;
	}

	/**
	 * Retrieves {@link #filterSerialNumber}
	 *
	 * @return value of {@link #filterSerialNumber}
	 */
	public String getFilterSerialNumber() {
		return filterSerialNumber;
	}

	/**
	 * Sets {@link #filterSerialNumber} value
	 *
	 * @param filterSerialNumber new value of {@link #filterSerialNumber}
	 */
	public void setFilterSerialNumber(String filterSerialNumber) {
		this.filterSerialNumber = filterSerialNumber;
	}

	/**
	 * Retrieves {@link #filterOrgUnit}
	 *
	 * @return value of {@link #filterOrgUnit}
	 */
	public String getFilterOrgUnit() {
		return filterOrgUnit;
	}

	/**
	 * Sets {@link #filterOrgUnit} value
	 *
	 * @param filterOrgUnit new value of {@link #filterOrgUnit}
	 */
	public void setFilterOrgUnit(String filterOrgUnit) {
		this.filterOrgUnit = filterOrgUnit;
	}

	/**
	 * Update the status of the device.
	 * The device is considered as paused if did not receive any retrieveMultipleStatistics()
	 * calls during {@link GoogleWorkspaceCommunicator}
	 */
	private synchronized void updateAggregatorStatus() {
		devicePaused = validRetrieveStatisticsTimestamp < System.currentTimeMillis();
	}

	/**
	 * Uptime time stamp to valid one
	 */
	private synchronized void updateValidRetrieveStatisticsTimestamp() {
		validRetrieveStatisticsTimestamp = System.currentTimeMillis() + retrieveStatisticsTimeOut;
		updateAggregatorStatus();
	}

	/**
	 * Constructs a new instance of the GoogleWorkspaceCommunicator class.
	 * This constructor initializes the communicator with the necessary components and settings to interact with Google Workspace.
	 *
	 * @throws IOException if an I/O error occurs during the initialization process.
	 */
	public GoogleWorkspaceCommunicator() throws IOException {
		Map<String, PropertiesMapping> mapping = new PropertiesMappingParser().loadYML(GoogleWorkspaceConstant.MODEL_MAPPING_AGGREGATED_DEVICE, getClass());
		aggregatedDeviceProcessor = new AggregatedDeviceProcessor(mapping);
		this.setTrustAllCertificates(true);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * Check for available devices before retrieving the value
	 * ping latency information to Symphony
	 */
	@Override
	public int ping() throws Exception {
		if (isInitialized()) {
			long pingResultTotal = 0L;

			for (int i = 0; i < this.getPingAttempts(); i++) {
				long startTime = System.currentTimeMillis();

				try (Socket puSocketConnection = new Socket(this.host, this.getPort())) {
					puSocketConnection.setSoTimeout(this.getPingTimeout());
					if (puSocketConnection.isConnected()) {
						long pingResult = System.currentTimeMillis() - startTime;
						pingResultTotal += pingResult;
						if (this.logger.isTraceEnabled()) {
							this.logger.trace(String.format("PING OK: Attempt #%s to connect to %s on port %s succeeded in %s ms", i + 1, host, this.getPort(), pingResult));
						}
					} else {
						if (this.logger.isDebugEnabled()) {
							this.logger.debug(String.format("PING DISCONNECTED: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
						}
						return this.getPingTimeout();
					}
				} catch (SocketTimeoutException | ConnectException tex) {
					throw new RuntimeException("Socket connection timed out", tex);
				} catch (Exception e) {
					if (this.logger.isWarnEnabled()) {
						this.logger.warn(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
					}
					return this.getPingTimeout();
				}
			}
			return Math.max(1, Math.toIntExact(pingResultTotal / this.getPingAttempts()));
		} else {
			throw new IllegalStateException("Cannot use device class without calling init() first");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		reentrantLock.lock();
		try {
			if (!checkValidApiToken()) {
				throw new ResourceNotReachableException("API Token cannot be null or empty, please enter valid API token in the password and username field.");
			}
			Map<String, String> statistics = new HashMap<>();
			List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			retrieveCustomerId();
			if (StringUtils.isNotNullOrEmpty(customerId)) {
				retrieveSystemInfo();
				populateSystemData(statistics, advancedControllableProperties);
			} else {
				populateNoneData(statistics);
			}
			extendedStatistics.setStatistics(statistics);
			extendedStatistics.setControllableProperties(advancedControllableProperties);
			localExtendedStatistics = extendedStatistics;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		reentrantLock.lock();
		try {
			String value = String.valueOf(controllableProperty.getValue());
			String property = controllableProperty.getProperty();
			switch (property) {
				case "OrganizationalUnits#Name":
					currentOrgUnitName = value;
					break;
				default:
					logger.debug(String.format("Property name %s doesn't support", property));
			}
		} finally {
			reentrantLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
		if (CollectionUtils.isEmpty(controllableProperties)) {
			throw new IllegalArgumentException("ControllableProperties can not be null or empty");
		}
		for (ControllableProperty p : controllableProperties) {
			try {
				controlProperty(p);
			} catch (Exception e) {
				logger.error(String.format("Error when control property %s", p.getProperty()), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {
		if (!orgUnitList.isEmpty()) {
			if (checkValidApiToken()) {
				if (executorService == null) {
					executorService = Executors.newFixedThreadPool(1);
					executorService.submit(deviceDataLoader = new GoogleWorkspaceDataLoader());
				}
				nextDevicesCollectionIterationTimestamp = System.currentTimeMillis();
				updateValidRetrieveStatisticsTimestamp();
			}
			if (aggregatedDeviceList.isEmpty()) {
				return aggregatedDeviceList;
			}
			return cloneAndPopulateAggregatedDeviceList();
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
		return retrieveMultipleStatistics().stream().filter(aggregatedDevice -> list.contains(aggregatedDevice.getDeviceId())).collect(Collectors.toList());
	}

	@Override
	protected void authenticate() throws Exception {
		// Google Workspace only require API token for each request.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
		executorService = Executors.newFixedThreadPool(1);
		executorService.submit(deviceDataLoader = new GoogleWorkspaceDataLoader());

		// Create a trust manager that trusts all certificates
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		this.sslContext = SSLContext.getInstance(GoogleWorkspaceConstant.SSL);
		this.sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal destroy is called.");
		}
		if (deviceDataLoader != null) {
			deviceDataLoader.stop();
			deviceDataLoader = null;
		}

		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null && localExtendedStatistics.getControllableProperties() != null) {
			localExtendedStatistics.getStatistics().clear();
			localExtendedStatistics.getControllableProperties().clear();
		}
		orgUnitList.clear();
		nextDevicesCollectionIterationTimestamp = 0;
		aggregatedDeviceList.clear();
		super.internalDestroy();
	}

	/**
	 * {@inheritDoc}
	 * set Bearer Token into Header of Request
	 */
	@Override
	protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) {
		headers.setBearerAuth(apiToken);
		return headers;
	}

	/**
	 * Get customer id from command
	 */
	private void retrieveCustomerId() {
		try {
			customerId = GoogleWorkspaceConstant.EMPTY;
			JsonNode usersResponse = this.doGet(GoogleWorkspaceCommand.USER_COMMAND, JsonNode.class);
			if (usersResponse.has(GoogleWorkspaceConstant.USERS) && usersResponse.get(GoogleWorkspaceConstant.USERS).size() > 0) {
				customerId = usersResponse.get(GoogleWorkspaceConstant.USERS).get(0).get(GoogleWorkspaceConstant.CUSTOMER_ID).asText();
			}
		} catch (Exception e) {
			customerId = GoogleWorkspaceConstant.EMPTY;
			logger.error(String.format("Error when get customer id, %s", e));
		}
	}

	/**
	 * Get system information of GoogleWorkspace
	 */
	private void retrieveSystemInfo() {
		try {
			JsonNode orgUnitsResponse = this.doGet(GoogleWorkspaceCommand.ORG_UNIT_COMMAND.replace(GoogleWorkspaceConstant.PATH_VARIABLE_CUSTOMER_ID, customerId), JsonNode.class);
			orgUnitList.clear();
			orgUnitList = objectMapper.readValue(orgUnitsResponse.get(GoogleWorkspaceConstant.ORGANIZATION_UNIT).toString(), new TypeReference<List<OrgUnit>>() {
			});

			String chromeOSCommand = GoogleWorkspaceCommand.CHROME_OS_COMMAND.replace(GoogleWorkspaceConstant.PATH_VARIABLE_CUSTOMER_ID, customerId)
					.replace(GoogleWorkspaceConstant.PATH_VARIABLE_ORG_UNIT, getDefaultFilterValueForNullData(filterOrgUnit))
					.replace(GoogleWorkspaceConstant.PATH_VARIABLE_SERIAL_NUMBER, getDefaultFilterValueForNullData(filterSerialNumber));

			if (StringUtils.isNotNullOrEmpty(nextTokenChromeOS)) {
				chromeOSCommand = chromeOSCommand + GoogleWorkspaceConstant.NEXT_TOKEN_REQUEST_PARAM + nextTokenChromeOS;
			}
			JsonNode chromeOSResponse = this.doGet(chromeOSCommand, JsonNode.class);

			if (chromeOSResponse.has(GoogleWorkspaceConstant.CHROME_OS_DEVICE)) {
				aggregatedDeviceResponse = chromeOSResponse.get(GoogleWorkspaceConstant.CHROME_OS_DEVICE);

				nextTokenChromeOS = GoogleWorkspaceConstant.EMPTY;
				if (chromeOSResponse.has(GoogleWorkspaceConstant.NEXT_TOKEN)) {
					nextTokenChromeOS = chromeOSResponse.get(GoogleWorkspaceConstant.NEXT_TOKEN).asText();
				}
			} else {
				aggregatedDeviceResponse = objectMapper.createObjectNode();
			}
		} catch (Exception e) {
			aggregatedDeviceResponse = objectMapper.createObjectNode();
			orgUnitList.clear();
			logger.error(String.format("Error when get system information, %s", e));
		}
	}

	/**
	 * Populates the given statistics map with "none" values for specific keys.
	 *
	 * @param statistics the map to be populated with "none" values
	 */
	private void populateNoneData(Map<String, String> statistics) {
		statistics.put(GoogleWorkspaceConstant.CHROME_OS_DEVICES_COUNT, GoogleWorkspaceConstant.NONE);
		statistics.put(GoogleWorkspaceConstant.ORGANIZATIONAL_UNIT_COUNT, GoogleWorkspaceConstant.NONE);
	}

	/**
	 * Populates the given statistics map with system-related data.
	 *
	 * @param statistics the map to be populated with system data
	 * @param advancedControllableProperties the list of advanced controllable properties
	 */
	private void populateSystemData(Map<String, String> statistics, List<AdvancedControllableProperty> advancedControllableProperties) {
		long orgUnitCount = checkFilterOrgUnit();
		statistics.put(GoogleWorkspaceConstant.CHROME_OS_DEVICES_COUNT, String.valueOf(aggregatedDeviceResponse.size()));
		statistics.put(GoogleWorkspaceConstant.ORGANIZATIONAL_UNIT_COUNT, String.valueOf(orgUnitCount));
		if (orgUnitCount != 0) {
			OrgUnit orgUnit = getDefaultOrgUnit();
			String orgUnitGroup = GoogleWorkspaceConstant.ORGANIZATIONAL_UNITS_GROUP;
			for (OrgUnitEnum orgUnitMetric : OrgUnitEnum.values()) {
				String name = orgUnitMetric.getName();
				String value = OrgUnitEnum.getValueByProperty(orgUnit, orgUnitMetric);
				String propertyName = orgUnitGroup + name;
				switch (orgUnitMetric) {
					case NAME:
						if (StringUtils.isNotNullOrEmpty(value) && orgUnitCount > 1) {
							String[] orgUnitValues = orgUnitList.stream().map(OrgUnit::getName).toArray(String[]::new);
							addAdvanceControlProperties(advancedControllableProperties, statistics, createDropdown(propertyName, orgUnitValues, value));
						} else {
							advancedControllableProperties.removeIf(item -> item.getName().equalsIgnoreCase(propertyName));
							statistics.put(propertyName + GoogleWorkspaceConstant.SPACE, getDefaultValueForNullData(value));
						}
						break;
					case PARENT_ORG_UNIT:
						statistics.put(propertyName, getParentOrgUnitNameById(orgUnit.getParentOrgUnitId()));
						break;
					case CHROME_OS_DEVICES_COUNT:
						long count = 0;
						if (aggregatedDeviceResponse.isArray()) {
							count = IntStream.range(0, aggregatedDeviceResponse.size())
									.filter(i -> aggregatedDeviceResponse.get(i).has(GoogleWorkspaceConstant.ORG_UNIT_PATH) && aggregatedDeviceResponse.get(i).get(GoogleWorkspaceConstant.ORG_UNIT_PATH).asText()
											.equals(orgUnit.getOrgUnitPath()))
									.count();
						}
						statistics.put(propertyName, String.valueOf(count));
						break;
					default:
						statistics.put(propertyName, getDefaultValueForNullData(value));
				}
			}
		}
	}

	/**
	 * populate detail aggregated device
	 * add aggregated device into aggregated device list
	 */
	private void populateDeviceDetails() {
		try {
			String telemetryCommand = GoogleWorkspaceCommand.TELEMETRY_COMMAND.replace(GoogleWorkspaceConstant.PATH_VARIABLE_CUSTOMER_ID, customerId);
			if (StringUtils.isNotNullOrEmpty(nextTokenTelemetry)) {
				telemetryCommand = telemetryCommand + GoogleWorkspaceConstant.NEXT_TOKEN_REQUEST_PARAM + nextTokenTelemetry;
			}
			JsonNode telemetryResponse = doGet(telemetryCommand, JsonNode.class);

			nextTokenTelemetry = GoogleWorkspaceConstant.EMPTY;
			if (telemetryResponse.has(GoogleWorkspaceConstant.NEXT_TOKEN)) {
				nextTokenTelemetry = telemetryResponse.get(GoogleWorkspaceConstant.NEXT_TOKEN).asText();
			}

			for (JsonNode jsonNode : aggregatedDeviceResponse) {
				String id = jsonNode.get(GoogleWorkspaceConstant.DEVICE_ID).asText();
				ObjectNode objectNode = (ObjectNode) jsonNode;
				if (!telemetryResponse.isEmpty()) {
					JsonNode telemetryItem = getJsonNodeByDeviceId(telemetryResponse.get(GoogleWorkspaceConstant.DEVICES), id);
					if (telemetryItem != null && telemetryItem.has(GoogleWorkspaceConstant.AUDIO_STATUS_REPORT)) {
						objectNode.put(GoogleWorkspaceConstant.AUDIO_STATUS_REPORT, telemetryItem.get(GoogleWorkspaceConstant.AUDIO_STATUS_REPORT).get(0).get(GoogleWorkspaceConstant.OUTPUT_VOLUME).asText());
					}
				}

				JsonNode node = objectMapper.createArrayNode().add(objectNode);
				aggregatedDeviceList.removeIf(item -> item.getDeviceId().equals(id));
				aggregatedDeviceList.addAll(aggregatedDeviceProcessor.extractDevices(node));
			}
		} catch (Exception e) {
			logger.error("Error while populate aggregated device", e);
		}
	}

	/**
	 * Retrieves the name of a parent organizational unit based on the provided ID.
	 *
	 * @param id the ID of the parent organizational unit
	 * @return the name of the parent organizational unit, or "none" if an error occurs or the name is not found
	 */
	private String getParentOrgUnitNameById(String id) {
		try {
			JsonNode parentOrgUnitResponse = this.doGet(GoogleWorkspaceCommand.PARENT_ORG_UNIT_COMMAND.replace(GoogleWorkspaceConstant.PATH_VARIABLE_CUSTOMER_ID, customerId) + id, JsonNode.class);
			if (parentOrgUnitResponse.has(GoogleWorkspaceConstant.NAME)) {
				return parentOrgUnitResponse.get(GoogleWorkspaceConstant.NAME).asText();
			}
		} catch (Exception e) {
			logger.error("Error while retrieve Parent Org Unit", e);
			return GoogleWorkspaceConstant.NONE;
		}
		return GoogleWorkspaceConstant.NONE;
	}

	/**
	 * Clone an aggregated device list that based on aggregatedDeviceList variable
	 * populate monitoring and controlling for aggregated device
	 *
	 * @return List<AggregatedDevice> aggregated device list
	 */
	private List<AggregatedDevice> cloneAndPopulateAggregatedDeviceList() {
		List<AggregatedDevice> resultAggregatedDeviceList = new ArrayList<>();
		synchronized (aggregatedDeviceList) {
			for (AggregatedDevice aggregatedDevice : aggregatedDeviceList) {
				Map<String, String> mappingStatistic = aggregatedDevice.getProperties();
				Map<String, String> dynamics = new HashMap<>();
				Map<String, String> stats = new HashMap<>();
				aggregatedDevice.setDeviceName(aggregatedDevice.getDeviceModel() + " (" + aggregatedDevice.getDeviceName() + ")");
				if (aggregatedDevice.getDeviceModel().contains(GoogleWorkspaceConstant.CHROMEBOOK)) {
					aggregatedDevice.setDeviceModel(GoogleWorkspaceConstant.CHROMEBOOK);
				}
				aggregatedDevice.setDeviceOnline(true);
				if (!mappingStatistic.containsKey(GoogleWorkspaceConstant.VOLUME_LEVEL)) {
					aggregatedDevice.setDeviceOnline(false);
				}
				mapMonitoringProperty(mappingStatistic, stats);
				mapDynamicStatistic(mappingStatistic, stats, dynamics);

				aggregatedDevice.setProperties(stats);
				aggregatedDevice.setDynamicStatistics(dynamics);
				resultAggregatedDeviceList.add(aggregatedDevice);
			}
		}
		return resultAggregatedDeviceList;
	}

	/**
	 * Maps and transforms monitoring properties from the old statistics map to a new map format.
	 *
	 * @param mappingStatistic the old statistics map to be mapped and transformed
	 * @return a new map containing the mapped and transformed monitoring properties
	 */
	private void mapMonitoringProperty(Map<String, String> mappingStatistic, Map<String, String> stats) {
		String value;
		String name;
		NetworkDTO networkDTO;
		JsonNode jsonNodeValue;
		for (AggregatedDeviceEnum aggregatedDeviceMetric : AggregatedDeviceEnum.values()) {
			name = aggregatedDeviceMetric.getName();
			value = getDefaultValueForNullData(mappingStatistic.get(name));
			switch (aggregatedDeviceMetric) {
				case PLATFORM_VERSION:
					value = value.split(GoogleWorkspaceConstant.SPACE_REGEX)[0];
					stats.put(name, value);
					break;
				case AUTO_UPDATE_EXPIRATION:
					stats.put(name, convertMillisecondsToDate(value));
					break;
				case LAST_SYNC:
				case ENROLLMENT_TIME:
					stats.put(name, convertFormatDateTime(value));
					break;
				case MAC_ADDRESS:
					stats.put(name, formatMacAddress(value));
					break;
				case MEMORY_TOTAL:
					stats.put(name, convertByteToGB(value));
					break;
				case IP_ADDRESS:
					networkDTO = getKnowNetworkValue(value);
					if (networkDTO != null) {
						stats.put(name, networkDTO.getIpAddress());
					} else {
						stats.put(name, GoogleWorkspaceConstant.NONE);
					}
					break;
				case WAN_IP_ADDRESS:
					networkDTO = getKnowNetworkValue(value);
					if (networkDTO != null) {
						stats.put(name, networkDTO.getWanIpAddress());
					} else {
						stats.put(name, GoogleWorkspaceConstant.NONE);
					}
					break;
				case RECENT_USERS:
					jsonNodeValue = getJsonNodeValue(value);
					if (jsonNodeValue != null) {
						JsonNode lastObject = jsonNodeValue.get(jsonNodeValue.size() - 1);
						stats.put(name, lastObject.get(GoogleWorkspaceConstant.EMAIL).asText());
					} else {
						stats.put(name, GoogleWorkspaceConstant.NONE);
					}
					break;
				case RECENT_ACTIVITY:
					jsonNodeValue = getJsonNodeValue(value);
					if (jsonNodeValue != null) {
						JsonNode lastObject = jsonNodeValue.get(jsonNodeValue.size() - 1);
						String date = convertDateFormat(getDefaultValueForNullData(lastObject.get(GoogleWorkspaceConstant.DATE).asText()));
						String activeTime = convertMilliseconds(getDefaultValueForNullData(lastObject.get(GoogleWorkspaceConstant.ACTIVE_TIME).toString()));
						if (!GoogleWorkspaceConstant.NONE.equals(date) && !GoogleWorkspaceConstant.NONE.equals(activeTime)) {
							stats.put(name, activeTime + GoogleWorkspaceConstant.ON + date);
						} else {
							stats.put(name, GoogleWorkspaceConstant.NONE);
						}
					} else {
						stats.put(name, GoogleWorkspaceConstant.NONE);
					}
					break;
				case MEMORY_FREE:
					jsonNodeValue = getJsonNodeValue(value);
					if (jsonNodeValue != null) {
						JsonNode lastObject = jsonNodeValue.get(jsonNodeValue.size() - 1).get(GoogleWorkspaceConstant.SYSTEM_RAM_FREE_INFO);
						List<String> memoryFrees = convertJsonNodeToList(lastObject, new TypeReference<List<String>>() {
						});
						if (!memoryFrees.isEmpty()) {
							stats.put(name, convertByteToGB(memoryFrees.get(0)));
						} else {
							stats.put(name, GoogleWorkspaceConstant.NONE);
						}
					} else {
						stats.put(name, GoogleWorkspaceConstant.NONE);
					}
					break;
				case CPU_UTILIZATION:
					jsonNodeValue = getJsonNodeValue(value);
					if (jsonNodeValue != null) {
						JsonNode lastObject = jsonNodeValue.get(jsonNodeValue.size() - 2).get(GoogleWorkspaceConstant.CPU_UTILIZATION_PERCENTAGE_INFO);
						List<Integer> cpuUtilizationValues = convertJsonNodeToList(lastObject, new TypeReference<List<Integer>>() {
						});
						if (!cpuUtilizationValues.isEmpty()) {
							stats.put(name, String.valueOf(cpuUtilizationValues.get(0)));
						} else {
							stats.put(name, GoogleWorkspaceConstant.NONE);
						}
					} else {
						stats.put(name, GoogleWorkspaceConstant.NONE);
					}
					break;
				default:
					stats.put(name, value);
			}
		}
	}

	/**
	 * Maps dynamic statistics to the appropriate properties in the stats and dynamic maps.
	 *
	 * @param mappingStatistic The mapping of statistic names to their corresponding values.
	 * @param stats The stats map to populate with the mapped properties.
	 * @param dynamic The dynamic map to populate with the mapped properties.
	 */
	private void mapDynamicStatistic(Map<String, String> mappingStatistic, Map<String, String> stats, Map<String, String> dynamic) {
		String value;
		String name;
		String propertyName;
		JsonNode jsonNodeValue;
		for (DynamicStatisticsDefinition dynamicItem : DynamicStatisticsDefinition.values()) {
			name = dynamicItem.getName();
			value = getDefaultValueForNullData(mappingStatistic.get(name));
			propertyName = GoogleWorkspaceConstant.CPU_TEMPERATURE_GROUP + name;

			jsonNodeValue = getJsonNodeValue(value);
			if (jsonNodeValue != null) {
				JsonNode lastObject = jsonNodeValue.get(jsonNodeValue.size() - 1).get(GoogleWorkspaceConstant.CPU_TEMPERATURE_INFO);
				List<CPUTemperature> cpuTemperatures = convertJsonNodeToList(lastObject, new TypeReference<List<CPUTemperature>>() {
				});
				if (!cpuTemperatures.isEmpty()) {
					String label = CPUTemperatureEnum.getByName(name).getValue();
					int temperature = cpuTemperatures.stream().filter(cpuTemperature -> cpuTemperature.getLabel().equals(label)).map(CPUTemperature::getTemperature).findFirst()
							.orElse(0);

					boolean propertyListed = false;
					if (!historicalProperties.isEmpty()) {
						if (propertyName.contains(GoogleWorkspaceConstant.HASH)) {
							propertyListed = historicalProperties.contains(propertyName.split(GoogleWorkspaceConstant.HASH)[1]);
						} else {
							propertyListed = historicalProperties.contains(propertyName);
						}
					}
					if (propertyListed) {
						dynamic.put(propertyName, String.valueOf(temperature));
					} else {
						stats.put(propertyName, String.valueOf(temperature));
					}
				}
			} else {
				stats.put(name, GoogleWorkspaceConstant.NONE);
			}
		}
	}

	/**
	 * Parses a JSON string into a list of KnownNetwork objects and returns the last element.
	 *
	 * @param value The JSON string to be parsed.
	 * @return The last KnownNetwork object in the parsed list, or null if an exception occurs during parsing or the list is empty.
	 */
	private NetworkDTO getKnowNetworkValue(String value) {
		try {
			List<NetworkDTO> networkDTOS = objectMapper.readValue(value, new TypeReference<List<NetworkDTO>>() {
			});
			if (!networkDTOS.isEmpty()) {
				return networkDTOS.get(networkDTOS.size() - 1);
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	/**
	 * Parses a JSON string into a JsonNode object and returns it if it is a non-empty array.
	 *
	 * @param value The JSON string to be parsed.
	 * @return The parsed JsonNode object if it is a non-empty array, or null if an exception occurs during parsing or the array is empty.
	 */
	private JsonNode getJsonNodeValue(String value) {
		try {
			JsonNode root = objectMapper.readTree(value);
			if (root != null && root.isArray() && root.size() > 0) {
				return root;
			}
		} catch (Exception e) {
			logger.error("Error occurred while parsing JSON", e);
		}
		return null;
	}

	/**
	 * Converts a JsonNode object to a List of the specified type.
	 *
	 * @param <T> The type of objects in the resulting List.
	 * @param jsonNode The JsonNode object to be converted.
	 * @param typeReference The TypeReference specifying the target type for conversion.
	 * @return The converted List of objects, or an empty List if an exception occurs during conversion.
	 */
	private <T> List<T> convertJsonNodeToList(JsonNode jsonNode, TypeReference<List<T>> typeReference) {
		List<T> listValue = new ArrayList<>();
		try {
			return objectMapper.readValue(jsonNode.toString(), typeReference);
		} catch (Exception e) {
			logger.error("Error when convert json node to list", e);
			return listValue;
		}
	}

	/**
	 * Retrieves a JsonNode object from a collection based on the matching device ID.
	 *
	 * @param jsonNode the collection of JsonNode objects
	 * @param id the device ID to match
	 * @return the JsonNode object with the matching device ID, or null if not found
	 */
	private JsonNode getJsonNodeByDeviceId(JsonNode jsonNode, String id) {
		return StreamSupport.stream(jsonNode.spliterator(), false)
				.filter(item -> item.has(GoogleWorkspaceConstant.DEVICE_ID) && id.equals(item.get(GoogleWorkspaceConstant.DEVICE_ID).asText()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Retrieves the current organization unit name.
	 *
	 * @return The current organization unit name.
	 * If the filterOrgUnit is not null or empty, it returns the filterOrgUnit value.
	 * If the currentOrgUnitName is not null or empty, it returns the currentOrgUnitName value.
	 * Otherwise, it returns the name of the organization unit at index 4 in the orgUnitList.
	 */
	private String getCurrentOrgUnitName() {
		if (StringUtils.isNotNullOrEmpty(filterOrgUnit)) {
			return filterOrgUnit;
		}
		if (StringUtils.isNotNullOrEmpty(currentOrgUnitName)) {
			return currentOrgUnitName;
		}
		return orgUnitList.get(GoogleWorkspaceConstant.DEFAULT_ORG_UNIT_POSITION).getName();
	}

	/**
	 * Retrieves the default organizational unit based on the filterOrgUnit value or a fallback value.
	 *
	 * @return the default organizational unit
	 */
	private OrgUnit getDefaultOrgUnit() {
		return orgUnitList.stream().filter(orgUnit -> orgUnit.getName().equals(getCurrentOrgUnitName())).findFirst().orElse(new OrgUnit());
	}

	/**
	 * Checks the filterOrgUnit value and returns the count of matching organizational units.
	 *
	 * @return the count of matching organizational units
	 */
	private long checkFilterOrgUnit() {
		return StringUtils.isNullOrEmpty(filterOrgUnit) ? orgUnitList.size() :
				orgUnitList.stream()
						.filter(orgUnit -> orgUnit.getName().equals(filterOrgUnit))
						.count();
	}

	/**
	 * Retrieves a token using the provided client ID and client secret.
	 *
	 * @return the token string
	 */
	private String getToken() {
		String token = GoogleWorkspaceConstant.EMPTY;
		String[] credentials = this.getPassword().trim().split(GoogleWorkspaceConstant.SPACE_REGEX);
		if (credentials.length == 2) {
			String clientSecret = credentials[0].trim();
			String refreshToken = credentials[1].trim();
			if (credentials[0].contains(GoogleWorkspaceConstant.SLASH)) {
				refreshToken = credentials[0].trim();
				clientSecret = credentials[1].trim();
			}
			tokenExpire = System.currentTimeMillis();
			Map<String, String> params = new HashMap<>();
			params.put(GoogleWorkspaceConstant.CLIENT_ID, this.getLogin());
			params.put(GoogleWorkspaceConstant.CLIENT_SECRET, clientSecret);
			params.put(GoogleWorkspaceConstant.REFRESH_TOKEN, refreshToken);
			params.put(GoogleWorkspaceConstant.GRANT_TYPE, GoogleWorkspaceConstant.REFRESH_TOKEN);
			try {
				JsonNode response = doPost(GoogleWorkspaceConstant.OAUTH2_URL, params, JsonNode.class);
				if (response != null && response.has(GoogleWorkspaceConstant.ACCESS_TOKEN)) {
					token = response.get(GoogleWorkspaceConstant.ACCESS_TOKEN).asText();
				}
			} catch (Exception e) {
				throw new ResourceNotReachableException("Can't get token from client id and client secret", e);
			}
		}
		return token;
	}

	/**
	 * Check API token validation
	 * If the token expires, we send a request to get a new token
	 *
	 * @return boolean
	 */
	private boolean checkValidApiToken() {
		if (StringUtils.isNullOrEmpty(getLogin()) || StringUtils.isNullOrEmpty(getPassword())) {
			return false;
		}
		if (StringUtils.isNullOrEmpty(apiToken) || System.currentTimeMillis() - tokenExpire >= expiresIn) {
			apiToken = getToken();
		}
		return StringUtils.isNotNullOrEmpty(apiToken);
	}

	/**
	 * convert default date time to correct format date time
	 *
	 * @param dateTime default date time
	 * @return correct format date time
	 */
	private String convertFormatDateTime(String dateTime) {
		if (GoogleWorkspaceConstant.NONE.equals(dateTime)) {
			return dateTime;
		}
		String outputDateTime = GoogleWorkspaceConstant.NONE;
		SimpleDateFormat inputFormatter = new SimpleDateFormat(GoogleWorkspaceConstant.DEFAULT_FORMAT_DATETIME, Locale.US);
		inputFormatter.setTimeZone(TimeZone.getTimeZone(GoogleWorkspaceConstant.UTC));
		try {
			Date date = inputFormatter.parse(dateTime);

			SimpleDateFormat outputFormatter = new SimpleDateFormat(GoogleWorkspaceConstant.NEW_FORMAT_DATETIME, Locale.US);
			outputFormatter.setTimeZone(TimeZone.getTimeZone(GoogleWorkspaceConstant.UTC));
			outputDateTime = outputFormatter.format(date);
		} catch (Exception e) {
			logger.debug("Error when convert format datetime", e);
		}
		return outputDateTime;
	}

	/**
	 * Converts a value from milliseconds to a formatted date string.
	 *
	 * @param value the value in milliseconds
	 * @return the formatted date string in the format "MMM yyyy", or "none" if an error occurs
	 */
	private String convertMillisecondsToDate(String value) {
		if (GoogleWorkspaceConstant.NONE.equals(value)) {
			return value;
		}
		try {
			long milliseconds = Long.parseLong(value);
			Date date = new Date(milliseconds);
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
			return dateFormat.format(date);
		} catch (Exception e) {
			logger.debug("Error when convert milliseconds to datetime", e);
		}
		return GoogleWorkspaceConstant.NONE;
	}

	/**
	 * Formats a MAC address by adding colons between pairs of characters.
	 *
	 * @param input the input string to format as a MAC address
	 * @return the formatted MAC address string, or the original input if it is "none" or has an invalid length
	 */
	private String formatMacAddress(String input) {
		if (GoogleWorkspaceConstant.NONE.equals(input) || input.length() != GoogleWorkspaceConstant.MAC_ADDRESS_LENGTH) {
			return GoogleWorkspaceConstant.NONE;
		}
		StringBuilder formattedMacAddress = new StringBuilder();
		for (int i = 0; i < input.length(); i += 2) {
			formattedMacAddress.append(input.substring(i, i + 2).toUpperCase());
			if (i < input.length() - 2) {
				formattedMacAddress.append(GoogleWorkspaceConstant.COLON);
			}
		}
		return formattedMacAddress.toString();
	}

	/**
	 * Converts a value from bytes to gigabytes (GB).
	 *
	 * @param value the value in bytes
	 * @return the converted value in GB, or "none" if an error occurs
	 */
	private String convertByteToGB(String value) {
		if (GoogleWorkspaceConstant.NONE.equals(value)) {
			return value;
		}
		try {
			double bytes = Double.parseDouble(value);
			double gb = bytes / (1024 * 1024 * 1024);
			gb = Math.round(gb * 100.0) / 100.0;
			return String.valueOf(gb);
		} catch (Exception e) {
			logger.debug("Error when convert byte to GB", e);
		}
		return GoogleWorkspaceConstant.NONE;
	}

	/**
	 * Converts a value from milliseconds to a formatted time representation.
	 *
	 * @param value the value in milliseconds
	 * @return the formatted time representation, or "none" if an error occurs
	 */
	private String convertMilliseconds(String value) {
		if (GoogleWorkspaceConstant.NONE.equals(value)) {
			return value;
		}
		try {
			long milliseconds = Long.parseLong(value);
			long seconds = milliseconds / 1000;
			if (seconds < 60) {
				return seconds + GoogleWorkspaceConstant.SECOND;
			} else {
				long minutes = seconds / 60;
				if (minutes < 60) {
					return minutes + GoogleWorkspaceConstant.MINUTE;
				} else {
					long hours = minutes / 60;
					long remainingMinutes = minutes % 60;
					return hours + GoogleWorkspaceConstant.HOUR + remainingMinutes + GoogleWorkspaceConstant.MINUTE;
				}
			}
		} catch (Exception e) {
			logger.debug("Error when convert byte to GB", e);
		}
		return GoogleWorkspaceConstant.NONE;
	}

	/**
	 * Converts a date from one format to another format.
	 *
	 * @param inputDate the input date string to convert
	 * @return the converted date string, or "none" if an error occurs
	 */
	private String convertDateFormat(String inputDate) {
		if (GoogleWorkspaceConstant.NONE.equals(inputDate)) {
			return inputDate;
		}
		try {
			DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy");

			Date date = inputDateFormat.parse(inputDate);
			return outputDateFormat.format(date);
		} catch (Exception e) {
			logger.debug("Error when convert format datetime", e);
		}

		return GoogleWorkspaceConstant.NONE;
	}

	/**
	 * Returns a default filter value for null data.
	 *
	 * @param value the input value to check
	 * @return the original value if it is not null or empty, or an empty value constant if it is null or empty
	 */
	private String getDefaultFilterValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : GoogleWorkspaceConstant.EMPTY;
	}

	/**
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) ? value : GoogleWorkspaceConstant.NONE;
	}

	/**
	 * Add advancedControllableProperties if advancedControllableProperties different empty
	 *
	 * @param advancedControllableProperties advancedControllableProperties is the list that store all controllable properties
	 * @param stats store all statistics
	 * @param property the property is item advancedControllableProperties
	 * @return String response
	 * @throws IllegalStateException when exception occur
	 */
	private void addAdvanceControlProperties(List<AdvancedControllableProperty> advancedControllableProperties, Map<String, String> stats, AdvancedControllableProperty property) {
		if (property != null) {
			for (AdvancedControllableProperty controllableProperty : advancedControllableProperties) {
				if (controllableProperty.getName().equals(property.getName())) {
					advancedControllableProperties.remove(controllableProperty);
					break;
				}
			}
			stats.put(property.getName(), GoogleWorkspaceConstant.EMPTY);
			advancedControllableProperties.add(property);
		}
	}

	/***
	 * Create dropdown advanced controllable property
	 *
	 * @param name the name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty dropdown instance
	 */
	private AdvancedControllableProperty createDropdown(String name, String[] values, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(values);
		dropDown.setLabels(values);

		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}
}