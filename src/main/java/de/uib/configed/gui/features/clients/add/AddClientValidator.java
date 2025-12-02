/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.List;
import java.util.regex.Pattern;

import de.uib.configed.gui.Configed;

final class AddClientValidator {
	static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");

	public interface RowValidation {
		/**
		 * Represents the possible outcomes of a validation step:
		 * <ul>
		 * <li><b>SUCCESS</b> – The row passed this validation step. Validation
		 * should continue with the next validator.</li>
		 * <li><b>DROP</b> – The row failed validation and must be rejected. The
		 * associated {@link AddClientEffect} contains the error information to
		 * be shown to the user.</li>
		 * <li><b>PAUSE</b> – The validation requires user confirmation before
		 * continuing (e.g. overwrite existing client, ignore naming violation).
		 * After this result, processing for this row is paused until the user
		 * responds.</li>
		 * </ul>
		 */
		enum ResultType {
			SUCCESS, DROP, PAUSE
		}

		/**
		 * Encapsulates the outcome of a validation step.
		 *
		 * @param type   The result type indicating how processing should
		 *               proceed.
		 * @param effect Optional effect describing feedback, warnings, or
		 *               questions to be presented to the UI layer. For
		 *               {@code SUCCESS} this may be {@code null}, while for
		 *               {@code DROP} and {@code PAUSE} it typically contains
		 *               error/warning details.
		 */
		record Result(ResultType type, AddClientEffect effect) {
		}

		/**
		 * Performs validation on the provided row and model state.
		 *
		 * @param row   The data row being validated.
		 * @param model The current AddClientModel containing accumulated state.
		 * @return A {@link Result} indicating whether validation succeeded,
		 *         failed, or requires user confirmation.
		 */
		Result validate(List<Object> row, AddClientModel model);
	}

	private AddClientValidator() {
	}

	static boolean isBoolean(String bool) {
		return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
				|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	static boolean areValuesValid(String hostname, String selectedDomain) {
		if (hostname == null || hostname.isEmpty()) {
			return false;
		}
		return selectedDomain != null && !selectedDomain.isEmpty();
	}

	static boolean requiresNetbiosConfirmation(String hostname) {
		return hostname.length() > 15 || NUMERIC_PATTERN.matcher(hostname).matches();
	}

	public static class BooleanValidator implements RowValidation {
		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			if (!isValidBooleans(row)) {
				return new Result(ResultType.DROP,
						new AddClientEffect.UIEffect.ShowErrorMessage(
								Configed.getResourceValue("NewClientDialog.nonBooleanValue.title"),
								Configed.getResourceValue("NewClientDialog.nonBooleanValue.message")));
			}
			return new Result(ResultType.SUCCESS, null);
		}

		private static boolean isValidBooleans(List<Object> client) {
			// Expecting shutdownInstall at index 10 and wan at index 11 as boolean strings
			if (client.size() <= 11) {
				return false;
			}
			return AddClientValidator.isBoolean((String) client.get(10))
					&& AddClientValidator.isBoolean((String) client.get(11));
		}
	}

	public static class HostnameDomainValidator implements RowValidation {
		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			String hostname = (String) row.get(0);
			String domain = (String) row.get(1);

			if (!AddClientValidator.areValuesValid(hostname, domain)) {
				return new Result(ResultType.DROP,
						new AddClientEffect.UIEffect.ShowErrorMessage(Configed.getResourceValue("error"),
								Configed.getResourceValue("NewClientDialog.hostnameRules")));
			}
			return new Result(ResultType.SUCCESS, null);
		}
	}

	public static class HostCollisionValidator implements RowValidation {

		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			String hostname = (String) row.get(0);
			String domain = (String) row.get(1);
			String opsiHostKey = hostname + "." + domain;

			if (!model.getHostnames().contains(opsiHostKey)) {
				return new Result(ResultType.SUCCESS, null);
			}

			if (model.getDepots().contains(opsiHostKey)) {
				return new Result(ResultType.DROP,
						new AddClientEffect.UIEffect.ShowErrorMessage(
								Configed.getResourceValue("NewClientDialog.OverwriteDepot.Title"),
								String.format(Configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"),
										opsiHostKey)));
			}

			return new Result(ResultType.PAUSE, new AddClientEffect.UIEffect.ShowOverwriteHostDialog(hostname));
		}
	}

	public static class NetbiosValidator implements RowValidation {

		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			String hostname = (String) row.get(0);

			if (!AddClientValidator.requiresNetbiosConfirmation(hostname)) {
				return new Result(ResultType.SUCCESS, null);
			}

			return new Result(ResultType.PAUSE, new AddClientEffect.UIEffect.ShowNetbiosConfirmDialog());
		}
	}

}
