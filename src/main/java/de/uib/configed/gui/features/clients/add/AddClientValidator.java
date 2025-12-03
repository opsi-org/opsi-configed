/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.List;
import java.util.Objects;
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
		 * Represents the outcome of a single validation step performed by a
		 * {@link RowValidation} implementation.
		 * <p>
		 * A validation step can yield three possible results:
		 * <ul>
		 * <li><b>{@code SUCCESS}</b> – The row passed this validator.
		 * Processing should continue with the next validator.</li>
		 * <li><b>{@code DROP}</b> – The row failed validation and must be
		 * rejected. An associated {@link AddClientEffect} provides the error
		 * message or feedback to display to the user. No further validators
		 * will be executed for this row.</li>
		 * <li><b>{@code PAUSE}</b> – Validation requires user confirmation
		 * before processing can continue (e.g. host overwrite, NetBIOS
		 * warning). The associated {@link AddClientEffect} describes the UI
		 * prompt to present. Validation is suspended until the user
		 * responds.</li>
		 * </ul>
		 * <p>
		 * Instances of this record are created via the static factory methods:
		 * {@link #success()}, {@link #drop(AddClientEffect)}, and
		 * {@link #pause(AddClientEffect)}. These methods guarantee that each
		 * result type is constructed with the correct semantics (e.g.
		 * {@code DROP} and {@code PAUSE} always carry a non-null effect).
		 */
		record Result(ResultType type, AddClientEffect effect) {

			/**
			 * Creates a {@code SUCCESS} result indicating that the row passed
			 * this validation step and processing should continue with the next
			 * validator.
			 *
			 * @return a result of type {@link ResultType#SUCCESS}
			 */
			public static Result success() {
				return new Result(ResultType.SUCCESS, null);
			}

			/**
			 * Creates a {@code DROP} result indicating that validation failed
			 * and the row must be rejected. The provided effect contains
			 * details about the failure to be shown to the user.
			 *
			 * @param effect a non-null effect describing the validation error
			 * @return a result of type {@link ResultType#DROP}
			 * @throws NullPointerException if {@code effect} is {@code null}
			 */
			public static Result drop(AddClientEffect effect) {
				Objects.requireNonNull(effect, "DROP requires a non-null effect");
				return new Result(ResultType.DROP, effect);
			}

			/**
			 * Creates a {@code PAUSE} result indicating that validation
			 * requires user confirmation before processing can continue. The
			 * provided effect specifies the dialog or prompt to be shown.
			 *
			 * @param effect a non-null effect describing the user confirmation
			 *               required to proceed
			 * @return a result of type {@link ResultType#PAUSE}
			 * @throws NullPointerException if {@code effect} is {@code null}
			 */
			public static Result pause(AddClientEffect effect) {
				Objects.requireNonNull(effect, "PAUSE requires a non-null effect");
				return new Result(ResultType.PAUSE, effect);
			}
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

	protected static class BooleanValidator implements RowValidation {
		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			if (!isValidBooleans(row)) {
				return Result.drop(new AddClientEffect.UIEffect.ShowErrorMessage(
						Configed.getResourceValue("NewClientDialog.nonBooleanValue.title"),
						Configed.getResourceValue("NewClientDialog.nonBooleanValue.message")));
			}
			return Result.success();
		}

		private static boolean isValidBooleans(List<Object> client) {
			// Expecting shutdownInstall at index 10 and wan at index 11 as boolean strings
			if (client.size() <= 11) {
				return false;
			}
			return isBoolean((String) client.get(10)) && isBoolean((String) client.get(11));
		}

		private static boolean isBoolean(String bool) {
			return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
					|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
		}
	}

	protected static class HostnameDomainValidator implements RowValidation {
		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			String hostname = (String) row.get(0);
			String domain = (String) row.get(1);

			if (!areValuesValid(hostname, domain)) {
				return Result.drop(new AddClientEffect.UIEffect.ShowErrorMessage(Configed.getResourceValue("error"),
						Configed.getResourceValue("NewClientDialog.hostnameRules")));
			}
			return Result.success();
		}

		private static boolean areValuesValid(String hostname, String selectedDomain) {
			if (hostname == null || hostname.isEmpty()) {
				return false;
			}
			return selectedDomain != null && !selectedDomain.isEmpty();
		}
	}

	protected static class HostCollisionValidator implements RowValidation {
		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			String hostname = (String) row.get(0);
			String domain = (String) row.get(1);
			String opsiHostKey = hostname + "." + domain;

			if (!model.getHostnames().contains(opsiHostKey)) {
				return Result.success();
			}

			if (model.getDepots().contains(opsiHostKey)) {
				return Result.drop(new AddClientEffect.UIEffect.ShowErrorMessage(
						Configed.getResourceValue("NewClientDialog.OverwriteDepot.Title"), String.format(
								Configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"), opsiHostKey)));
			}

			return Result.pause(new AddClientEffect.UIEffect.ShowOverwriteHostDialog(hostname));
		}
	}

	protected static class NetbiosValidator implements RowValidation {
		@Override
		public Result validate(List<Object> row, AddClientModel model) {
			String hostname = (String) row.get(0);

			if (!requiresNetbiosConfirmation(hostname)) {
				return Result.success();
			}

			return Result.pause(new AddClientEffect.UIEffect.ShowNetbiosConfirmDialog());
		}

		private static boolean requiresNetbiosConfirmation(String hostname) {
			return hostname.length() > 15 || NUMERIC_PATTERN.matcher(hostname).matches();
		}
	}
}
