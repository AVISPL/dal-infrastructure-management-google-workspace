/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.common;

import com.avispl.symphony.dal.communicator.aggregator.dto.systemInfo.OrgUnit;

/**
 * OrgUnitEnum include different metrics for Google Workspace organizational units.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/19/2023
 * @since 1.0.0
 */
public enum OrgUnitEnum {
	DESCRIPTION("Description"),
	NAME("Name"),
	ORG_UNIT_ID("UnitID"),
	PARENT_ORG_UNIT("ParentName"),
	CHROME_OS_DEVICES_COUNT("ChromeOSDevicesCount"),
	;
	private final String name;

	/**
	 * Constructs a OrgUnitEnum enum constant with the specified name.
	 *
	 * @param name the name of the organizational unit metric
	 */
	OrgUnitEnum(String name) {
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

	/**
	 * Retrieves the value of the specified metric from the given OrgUnit object.
	 *
	 * @param orgUnit the OrgUnit object to retrieve the value from
	 * @param metric the metric to retrieve the value for
	 * @return the value of the specified metric from the OrgUnit object
	 */
	public static String getValueByProperty(OrgUnit orgUnit, OrgUnitEnum metric) {
		if (orgUnit == null) {
			orgUnit = new OrgUnit();
		}
		switch (metric) {
			case DESCRIPTION:
				return orgUnit.getDescription();
			case NAME:
				return orgUnit.getName();
			case ORG_UNIT_ID:
				return orgUnit.getOrgUnitId();
			case PARENT_ORG_UNIT:
				return orgUnit.getParentOrgUnitPath();
			default:
				return GoogleWorkspaceConstant.EMPTY;
		}
	}
}
