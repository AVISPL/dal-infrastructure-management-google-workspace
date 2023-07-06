/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.communicator.aggregator.common.GoogleWorkspaceCommand;
import com.avispl.symphony.dal.communicator.aggregator.common.GoogleWorkspaceConstant;
import com.avispl.symphony.dal.communicator.aggregator.common.GoogleWorkspaceOrgUnitMetric;
import com.avispl.symphony.dal.communicator.aggregator.dto.OrgUnit;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * GoogleWorkspaceAggregatorDevice class provides during the monitoring and controlling process
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 3/6/2023
 * @since 1.0.0
 */
public class GoogleWorkspaceCommunicator extends RestCommunicator implements Aggregator, Monitorable, Controller {
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
	ObjectMapper objectMapper = new ObjectMapper();

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
	 * List of orgUnit
	 */
	private List<OrgUnit> orgUnitList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * contains information of aggregated devices
	 */
	JsonNode aggregatedDeviceResponse;

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
	 * Sets {@link #currentOrgUnitName} value
	 *
	 * @param currentOrgUnitName new value of {@link #currentOrgUnitName}
	 */
	public void setCurrentOrgUnitName(String currentOrgUnitName) {
		this.currentOrgUnitName = currentOrgUnitName;
	}

	/**
	 * Retrieves {@link #reentrantLock}
	 *
	 * @return value of {@link #reentrantLock}
	 */
	public ReentrantLock getReentrantLock() {
		return reentrantLock;
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
	 * Constructs a new instance of the GoogleWorkspaceCommunicator class.
	 * This constructor initializes the communicator with the necessary components and settings to interact with Google Workspace.
	 *
	 * @throws IOException if an I/O error occurs during the initialization process.
	 */
	public GoogleWorkspaceCommunicator() throws IOException {
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
					if (this.logger.isDebugEnabled()) {
						this.logger.error(String.format("PING TIMEOUT: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
					}
					throw new SocketTimeoutException("Connection timed out");
				} catch (Exception e) {
					if (this.logger.isDebugEnabled()) {
						this.logger.error(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
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
		this.sslContext = SSLContext.getInstance("SSL");
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
		localExtendedStatistics.getStatistics().clear();
		localExtendedStatistics.getControllableProperties().clear();
		orgUnitList.clear();
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

			JsonNode chromeOSResponse = this.doGet(
					GoogleWorkspaceCommand.CHROME_OS_COMMAND.replace(GoogleWorkspaceConstant.PATH_VARIABLE_CUSTOMER_ID, customerId) + getDefaultFilterValueForNullData(filterOrgUnit), JsonNode.class);
			if (chromeOSResponse.has(GoogleWorkspaceConstant.CHROME_OS_DEVICE)) {
				aggregatedDeviceResponse = chromeOSResponse.get(GoogleWorkspaceConstant.CHROME_OS_DEVICE);
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
			String orgUnitGroup = "OrganizationalUnits#";
			for (GoogleWorkspaceOrgUnitMetric orgUnitMetric : GoogleWorkspaceOrgUnitMetric.values()) {
				String name = orgUnitMetric.getName();
				String value = GoogleWorkspaceOrgUnitMetric.getValueByProperty(orgUnit, orgUnitMetric);
				String propertyName = orgUnitGroup + name;
				switch (orgUnitMetric) {
					case NAME:

						if (StringUtils.isNotNullOrEmpty(value) && orgUnitCount > 1) {
							String[] orgUnitValues = orgUnitList.stream().map(OrgUnit::getName).toArray(String[]::new);
							addAdvanceControlProperties(advancedControllableProperties, statistics, createDropdown(propertyName, orgUnitValues, value));
						} else {
							advancedControllableProperties.removeIf(item -> item.getName().equalsIgnoreCase(propertyName));
							statistics.put(propertyName + " ", getDefaultValueForNullData(value));
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
	 * Retrieves the name of a parent organizational unit based on the provided ID.
	 *
	 * @param id the ID of the parent organizational unit
	 * @return the name of the parent organizational unit, or "none" if an error occurs or the name is not found
	 */
	private String getParentOrgUnitNameById(String id) {
		try {
			JsonNode parentOrgUnitResponse = this.doGet(GoogleWorkspaceCommand.PARENT_ORG_UNIT_COMMAND.replace(GoogleWorkspaceConstant.PATH_VARIABLE_CUSTOMER_ID, customerId) + id, JsonNode.class);
			if (parentOrgUnitResponse.has("name")) {
				return parentOrgUnitResponse.get("name").asText();
			}
		} catch (Exception e) {
			logger.error("Error while retrieve Parent Org Unit", e);
			return GoogleWorkspaceConstant.NONE;
		}
		return GoogleWorkspaceConstant.NONE;
	}

	private String getCurrentOrgUnitName() {
		if (StringUtils.isNotNullOrEmpty(filterOrgUnit)) {
			return filterOrgUnit;
		} else if (StringUtils.isNotNullOrEmpty(currentOrgUnitName)) {
			return currentOrgUnitName;
		} else {
			return orgUnitList.get(4).getName();
		}
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
		if (StringUtils.isNullOrEmpty(filterOrgUnit)) {
			return orgUnitList.size();
		}
		return orgUnitList.stream()
				.filter(orgUnit -> orgUnit.getName().equals(filterOrgUnit))
				.count();
	}

	/**
	 * Retrieves a token using the provided client ID and client secret.
	 *
	 * @return the token string
	 */
	private String getToken() {
		String token;
		tokenExpire = System.currentTimeMillis();
		Map<String, String> params = new HashMap<>();
		params.put("client_id", this.getLogin());
		params.put("client_secret", this.getPassword());
		params.put("refresh_token", "1//04tDvzCfQx9IQCgYIARAAGAQSNwF-L9Ir-JA7TJ3U2D04WcmmyseCQWF6BRa60-vg_uqDkUk_k_wk9MTcTVmzC0zXKlyrpaeNf6Q");
		params.put("grant_type", "refresh_token");
		try {
			JsonNode response = doPost("https://oauth2.googleapis.com/token", params, JsonNode.class);
			token = response.get("access_token").asText();
		} catch (Exception e) {
			throw new ResourceNotReachableException("Can't get token from client id and client secret", e);
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
		return true;
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