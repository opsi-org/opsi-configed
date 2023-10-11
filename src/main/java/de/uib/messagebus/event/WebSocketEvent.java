/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event;

public enum WebSocketEvent {
	GENERAL_EVENT("event"), GENERAL_ERROR("general_error"),

	CHANNEL_SUBSCRIPTION_EVENT("channel_subscription_event"),

	FILE_UPLOAD_RESULT("file_upload_result"),

	TERMINAL_OPEN_EVENT("terminal_open_event"), TERMINAL_CLOSE_EVENT("terminal_close_event"),
	TERMINAL_DATA_READ("terminal_data_read"), TERMINAL_RESIZE_REQUEST("terminal_resize_request"),
	TERMINAL_RESIZE_EVENT("terminal_resize_event"),

	HOST_CONNECTED("host_connected"), HOST_DISCONNECTED("host_disconnected"), HOST_CREATED("host_created"),
	HOST_DELETED("host_deleted"),

	PRODUCT_ON_CLIENT_CREATED("productOnClient_created"), PRODUCT_ON_CLIENT_UPDATED("productOnClient_updated"),
	PRODUCT_ON_CLIENT_DELETED("productOnClient_deleted");

	private final String displayName;

	WebSocketEvent(String displayName) {
		this.displayName = displayName;
	}

	public String asChannelEvent() {
		return GENERAL_EVENT.toString() + ":" + displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
