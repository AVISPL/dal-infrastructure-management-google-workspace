/*
 * Copyright (c) 2023 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.communicator.aggregator.dto.systemInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representing Org Unit.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 6/19/2023
 * @since 1.0.0
 */
public class OrgUnit {
	private String kind;
	@JsonProperty("etag")
	private String eTag;
	private String name;
	private String description;
	private String orgUnitPath;
	private String orgUnitId;
	private String parentOrgUnitPath;
	private String parentOrgUnitId;

	/**
	 * Constructs an OrgUnit object with the provided parameters.
	 *
	 * @param kind The kind of the organization unit.
	 * @param eTag The entity tag associated with the organization unit.
	 * @param name The name of the organization unit.
	 * @param description The description of the organization unit.
	 * @param orgUnitPath The path of the organization unit.
	 * @param orgUnitId The unique ID of the organization unit.
	 * @param parentOrgUnitPath The path of the parent organization unit.
	 * @param parentOrgUnitId The unique ID of the parent organization unit.
	 */
	public OrgUnit(String kind, String eTag, String name, String description, String orgUnitPath, String orgUnitId, String parentOrgUnitPath, String parentOrgUnitId) {
		this.kind = kind;
		this.eTag = eTag;
		this.name = name;
		this.description = description;
		this.orgUnitPath = orgUnitPath;
		this.orgUnitId = orgUnitId;
		this.parentOrgUnitPath = parentOrgUnitPath;
		this.parentOrgUnitId = parentOrgUnitId;
	}

	/**
	 * Default constructor for creating an empty OrgUnit object.
	 */
	public OrgUnit() {
	}

	/**
	 * Retrieves {@link #kind}
	 *
	 * @return value of {@link #kind}
	 */
	public String getKind() {
		return kind;
	}

	/**
	 * Sets {@link #kind} value
	 *
	 * @param kind new value of {@link #kind}
	 */
	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * Retrieves {@link #eTag}
	 *
	 * @return value of {@link #eTag}
	 */
	public String geteTag() {
		return eTag;
	}

	/**
	 * Sets {@link #eTag} value
	 *
	 * @param eTag new value of {@link #eTag}
	 */
	public void seteTag(String eTag) {
		this.eTag = eTag;
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
	 * Sets {@link #name} value
	 *
	 * @param name new value of {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #description}
	 *
	 * @return value of {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets {@link #description} value
	 *
	 * @param description new value of {@link #description}
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Retrieves {@link #orgUnitPath}
	 *
	 * @return value of {@link #orgUnitPath}
	 */
	public String getOrgUnitPath() {
		return orgUnitPath;
	}

	/**
	 * Sets {@link #orgUnitPath} value
	 *
	 * @param orgUnitPath new value of {@link #orgUnitPath}
	 */
	public void setOrgUnitPath(String orgUnitPath) {
		this.orgUnitPath = orgUnitPath;
	}

	/**
	 * Retrieves {@link #orgUnitId}
	 *
	 * @return value of {@link #orgUnitId}
	 */
	public String getOrgUnitId() {
		return orgUnitId;
	}

	/**
	 * Sets {@link #orgUnitId} value
	 *
	 * @param orgUnitId new value of {@link #orgUnitId}
	 */
	public void setOrgUnitId(String orgUnitId) {
		this.orgUnitId = orgUnitId;
	}

	/**
	 * Retrieves {@link #parentOrgUnitPath}
	 *
	 * @return value of {@link #parentOrgUnitPath}
	 */
	public String getParentOrgUnitPath() {
		return parentOrgUnitPath;
	}

	/**
	 * Sets {@link #parentOrgUnitPath} value
	 *
	 * @param parentOrgUnitPath new value of {@link #parentOrgUnitPath}
	 */
	public void setParentOrgUnitPath(String parentOrgUnitPath) {
		this.parentOrgUnitPath = parentOrgUnitPath;
	}

	/**
	 * Retrieves {@link #parentOrgUnitId}
	 *
	 * @return value of {@link #parentOrgUnitId}
	 */
	public String getParentOrgUnitId() {
		return parentOrgUnitId;
	}

	/**
	 * Sets {@link #parentOrgUnitId} value
	 *
	 * @param parentOrgUnitId new value of {@link #parentOrgUnitId}
	 */
	public void setParentOrgUnitId(String parentOrgUnitId) {
		this.parentOrgUnitId = parentOrgUnitId;
	}
}
