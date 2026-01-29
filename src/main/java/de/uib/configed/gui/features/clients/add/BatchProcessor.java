/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.clients.add.AddClientValidator.RowValidation;

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
					// continue to next validator
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

			List<List<Object>> accepted = new ArrayList<>(model.getAcceptedRows());
			accepted.add(row);
			model = model.withAcceptedRows(accepted);
		}

		List<List<Object>> finalRows = new ArrayList<>(model.getAcceptedRows());
		model = model.toBuilder().acceptedRows(new ArrayList<>()).rowsToImport(new ArrayList<>())
				.pendingSingleRow(new ArrayList<>()).build();

		return UpdateResult.withEffect(model, new AddClientEffect.ServiceEffect.CreateClients(finalRows));
	}

	public UpdateResult<AddClientModel, AddClientEffect> processSingleRow(AddClientModel model, List<Object> row) {
		AddClientModel working = model;

		for (RowValidation validator : validators) {
			RowValidation.Result r = validator.validate(row, working);

			switch (r.type()) {
			case SUCCESS -> {
				// continue to next validator
			}
			case DROP -> {
				// Do NOT store pending row
				return UpdateResult.withEffect(working, r.effect());
			}
			case PAUSE -> {
				// Store the row as "pending"
				working = working.withPendingSingleRow(row);
				return UpdateResult.withEffect(working, r.effect());
			}
			}
		}

		return UpdateResult.withEffect(model, new AddClientEffect.ServiceEffect.CreateClients(List.of(row)));
	}
}
