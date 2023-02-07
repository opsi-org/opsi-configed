/**
 *  OpsiPackage
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *    
 *  copyright:     Copyright (c) 2014
 *  organization: uib.de
 *  @author  R. Roeder 
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

//data source table productOnDepot
public class OpsiPackage implements Comparable {
	protected String productId;
	protected int productType;
	protected String versionInfo;
	protected String productVersion;
	protected String packageVersion;

	protected String representation;
	protected String lockedText;
	public static final String IS_LOCKED_INFO = "LOCKED";

	public static final String DB_KEY_PRODUCT_ID = "productId";
	public static final String SERVICE_KEY_PRODUCT_ID0 = "id";
	public static final String SERVICE_KEY_PRODUCT_VERSION = "productVersion";
	public static final String SERVICE_KEY_PACKAGE_VERSION = "packageVersion";
	public static final String SERVICE_KEY_PRODUCT_TYPE = "productType";
	public static final String SERVICE_KEY_LOCKED = "locked";
	public static final String VERSION_INFO = "versionInfo";

	public static final String LOCALBOOT_PRODUCT_SERVER_STRING = "LocalbootProduct";
	public static final String NETBOOT_PRODUCT_SERVER_STRING = "NetbootProduct";

	public static final List<String> SERVICE_KEYS; // those which form the primary keys
	static {
		SERVICE_KEYS = new ArrayList<>();
		SERVICE_KEYS.add(SERVICE_KEY_PRODUCT_ID0);
		SERVICE_KEYS.add(SERVICE_KEY_PRODUCT_VERSION);
		SERVICE_KEYS.add(SERVICE_KEY_PACKAGE_VERSION);
		SERVICE_KEYS.add(SERVICE_KEY_PRODUCT_TYPE);
	}

	public static final int TYPE_LOCALBOOT = 0;
	public static final int TYPE_NETBOOT = 1;

	public OpsiPackage(String productId, String productVersion, String packageVersion, String productType) {
		this(productId, productVersion, packageVersion, productType, false); // compatibility to usages without locked
																				// parameter
	}

	public OpsiPackage(String productId, String productVersion, String packageVersion, String productType,
			boolean locked) {
		this.productId = productId;
		this.productVersion = productVersion;
		this.packageVersion = packageVersion;
		this.versionInfo = productVersion + Globals.ProductPackageVersionSeparator.FOR_KEY + packageVersion;

		if (productType.equals(LOCALBOOT_PRODUCT_SERVER_STRING))
			this.productType = 0;
		else if (productType.equals(NETBOOT_PRODUCT_SERVER_STRING))
			this.productType = 1;
		else
			this.productType = -1;

		if (locked)
			this.lockedText = IS_LOCKED_INFO;
		else
			this.lockedText = "";

		Logging.debug(this, "created : " + productId + ", " + productType + ", " + versionInfo);

		representation = buildRepresentation();
	}

	public OpsiPackage(Map<String, Object> m) {
		this("" + m.get(DB_KEY_PRODUCT_ID), "" + m.get(SERVICE_KEY_PRODUCT_VERSION),
				"" + m.get(SERVICE_KEY_PACKAGE_VERSION), "" + m.get(SERVICE_KEY_PRODUCT_TYPE),
				Globals.interpretAsBoolean(m.get(SERVICE_KEY_LOCKED)));
		Logging.debug(this, "built from " + m);

	}

	public String getProductId() {
		return productId;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public String getPackageVersion() {
		return packageVersion;
	}

	public String getVersionInfo() {
		return versionInfo;
	}

	public String getLockedInfo() {
		return lockedText;
	}

	public static String produceVersionInfo(String productVersion, String packageVersion) {
		return productVersion + Globals.ProductPackageVersionSeparator.FOR_KEY + packageVersion;
	}

	public int getProductType() {
		return productType;
	}

	public List<Object> appendValues(List<Object> row) {

		row.add(giveProductType(getProductType()));
		row.add(getProductVersion());
		row.add(getPackageVersion());
		row.add(getLockedInfo());
		return row;
	}

	public boolean isLocalbootProduct() {
		return (productType == TYPE_LOCALBOOT);
	}

	public boolean isNetbootProduct() {
		return (productType == TYPE_NETBOOT);
	}

	public static String giveProductType(int type) {
		switch (type) {
		case TYPE_LOCALBOOT:
			return LOCALBOOT_PRODUCT_SERVER_STRING;

		case TYPE_NETBOOT:
			return NETBOOT_PRODUCT_SERVER_STRING;

		default:
			return "error";
		}
	}

	protected String buildRepresentation() {
		return "{" + DB_KEY_PRODUCT_ID + ":\"" + productId + "\";" + SERVICE_KEY_PRODUCT_TYPE + ":\""
				+ giveProductType(productType) + "\";" + VERSION_INFO + ":\"" + versionInfo + "\"}";
	}

	@Override
	public String toString() {
		return representation;
	}

	// Interface Comparable
	@Override
	public int compareTo(Object o) {
		return representation.compareTo(o.toString());
	}

	@Override
	public boolean equals(Object o) {
		return o == null || representation.equals(o.toString());
	}

}
