/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.clients.add.AddClientValidator.RowValidation;
import de.uib.configed.share.logging.Logging;

public class BatchProcessor {

	private final List<RowValidation> validators;

	public BatchProcessor(List<RowValidation> validators) {
		this.validators = validators;
	}

	public UpdateResult<AddClientModel, AddClientEffect> process(AddClientModel model) {

		List<List<Object>> toImport = new ArrayList<>(model.getRowsToImport());

		while (!toImport.isEmpty()) {
			List<Object> row = toImport.remove(0);

			for (RowValidation validator : validators) {
				var result = validator.validate(row, model);

				switch (result.type()) {

				case SUCCESS -> {
					// continue with next validator
					continue;
				}

				case DROP -> {
					// discard row, show effect
					model = model.withRowsToImport(toImport);
					return UpdateResult.withEffect(model, result.effect());
				}

				case PAUSE -> {
					// keep row aside, pause import
					model = model.withRowsToImport(toImport).withPendingSingleRow(row);
					return UpdateResult.withEffect(model, result.effect());
				}
				}
			}

			// Row passed all validators
			List<List<Object>> accepted = new ArrayList<>(model.getAcceptedRows());
			accepted.add(row);
			model = model.withAcceptedRows(accepted);
		}

		// Finished
		List<List<Object>> finalRows = new ArrayList<>(model.getAcceptedRows());
		model = model.withAcceptedRows(new ArrayList<>()).withRowsToImport(new ArrayList<>())
				.withPendingSingleRow(new ArrayList<>());

		Logging.devel(this, "rows ", finalRows);
		return UpdateResult.withEffect(model, new AddClientEffect.ServiceEffect.CreateMultipleClients(finalRows));
	}
}
