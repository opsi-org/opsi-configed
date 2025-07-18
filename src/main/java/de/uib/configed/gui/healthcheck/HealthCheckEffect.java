package de.uib.configed.gui.healthcheck;

public sealed interface HealthCheckEffect permits HealthCheckEffect.Copy, HealthCheckEffect.Download {

	final class Copy implements HealthCheckEffect {}

	final class Download implements HealthCheckEffect {}
}