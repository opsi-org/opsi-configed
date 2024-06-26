/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;
import de.uib.utils.logging.Logging;

//data source table productOnDepot
public class OpsiPackage implements Comparable<OpsiPackage> {
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

	// those which form the primary keys
	public static final List<String> SERVICE_KEYS = List.of(SERVICE_KEY_PRODUCT_ID0, SERVICE_KEY_PRODUCT_VERSION,
			SERVICE_KEY_PACKAGE_VERSION, SERVICE_KEY_PRODUCT_TYPE);

	public static final int TYPE_LOCALBOOT = 0;
	public static final int TYPE_NETBOOT = 1;

	protected String productId;
	private int productType;
	private String versionInfo;
	private String productVersion;
	private String packageVersion;

	private String representation;
	private String lockedText;

	public OpsiPackage(String productId, String productVersion, String packageVersion, String productType,
			boolean locked) {
		this.productId = productId;
		this.productVersion = productVersion;
		this.packageVersion = packageVersion;
		this.versionInfo = productVersion + ProductDataService.FOR_KEY + packageVersion;

		if (productType.equals(LOCALBOOT_PRODUCT_SERVER_STRING)) {
			this.productType = 0;
		} else if (productType.equals(NETBOOT_PRODUCT_SERVER_STRING)) {
			this.productType = 1;
		} else {
			this.productType = -1;
		}

		if (locked) {
			this.lockedText = IS_LOCKED_INFO;
		} else {
			this.lockedText = "";
		}

		Logging.debug(this.getClass(), "created : " + productId + ", " + productType + ", " + versionInfo);

		representation = buildRepresentation();
	}

	public OpsiPackage(Map<String, Object> m) {
		this("" + m.get(DB_KEY_PRODUCT_ID), "" + m.get(SERVICE_KEY_PRODUCT_VERSION),
				"" + m.get(SERVICE_KEY_PACKAGE_VERSION), "" + m.get(SERVICE_KEY_PRODUCT_TYPE),
				Boolean.TRUE.equals(m.get(SERVICE_KEY_LOCKED)));

		Logging.debug(this.getClass(), "built from " + m);
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
		return productVersion + ProductDataService.FOR_KEY + packageVersion;
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
		return productType == TYPE_LOCALBOOT;
	}

	public boolean isNetbootProduct() {
		return productType == TYPE_NETBOOT;
	}

	public static String giveProductType(int type) {
		return switch (type) {
		case TYPE_LOCALBOOT -> LOCALBOOT_PRODUCT_SERVER_STRING;
		case TYPE_NETBOOT -> NETBOOT_PRODUCT_SERVER_STRING;
		default -> "error";
		};
	}

	private String buildRepresentation() {
		return "{" + DB_KEY_PRODUCT_ID + ":\"" + productId + "\";" + SERVICE_KEY_PRODUCT_TYPE + ":\""
				+ giveProductType(productType) + "\";" + VERSION_INFO + ":\"" + versionInfo + "\"}";
	}

	@Override
	public String toString() {
		return representation;
	}

	// Interface Comparable
	@Override
	public int compareTo(OpsiPackage o) {
		return representation.compareTo(o.toString());
	}

	@Override
	public boolean equals(Object o) {
		return o == null || representation.equals(o.toString());
	}

	@Override
	public int hashCode() {
		return representation.hashCode();
	}
}
