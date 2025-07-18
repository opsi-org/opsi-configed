package de.uib.configed.gui.healthcheck;

public sealed interface HealthCheckEffect permits HealthCheckEffect.SimpleEffect {
	enum SimpleEffect implements HealthCheckEffect {
		COPY, DOWNLOAD
	}
}