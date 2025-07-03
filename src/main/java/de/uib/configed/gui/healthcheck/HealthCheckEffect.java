package de.uib.configed.gui.healthcheck;

public sealed interface HealthCheckEffect
		permits HealthCheckEffect.None, HealthCheckEffect.Copy, HealthCheckEffect.Download {
	final class None implements HealthCheckEffect {}

	final class Copy implements HealthCheckEffect {}

	final class Download implements HealthCheckEffect {}
}