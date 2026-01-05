/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JComponent;

/**
 * Abstract base class implementing The Elm Architecture (TEA) pattern for Swing
 * UI components, supporting immutable state management, event-driven updates,
 * and side-effect handling.
 *
 * @param <M> The type representing the immutable UI state (Model).
 * @param <E> The type representing messages/events that trigger state updates.
 * @param <F> The type representing side-effects or commands resulting from
 *            updates.
 */
public abstract class AbstractTeaComponent<M, E, F> {
	/**
	 * The current immutable state of the component.
	 */
	protected M model;

	/**
	 * If true, model was set via constructor and should not be overwritten by
	 * {@link #initModel()}.
	 */
	private boolean modelProvided;

	/**
	 * Represents the result of an update operation in the application state.
	 * <p>
	 * This immutable data holder contains:
	 * <ul>
	 * <li>{@code model}: the updated state of the model after processing an
	 * input message.</li>
	 * <li>{@code effect}: an optional side-effect to be executed, if any.</li>
	 * </ul>
	 * <p>
	 * Instances of {@code UpdateResult} should be created using the provided
	 * static factory methods {@link #noEffect(Object)} or
	 * {@link #withEffect(Object, Object)} to explicitly indicate whether a
	 * side-effect is present. Direct construction via the canonical constructor
	 * is discouraged to maintain semantic clarity.
	 *
	 * @param <M> the type of the model
	 * @param <F> the type of the side-effect
	 */
	public record UpdateResult<M, F>(M model, Optional<F> effect) {
		/**
		 * Creates an {@code UpdateResult} representing an updated model with no
		 * side-effects.
		 *
		 * @param model the updated model state; must not be {@code null}
		 * @param <M>   the model type
		 * @param <F>   the effect type
		 * @return an {@code UpdateResult} with the given model and an empty
		 *         effect
		 * @throws IllegalArgumentException if {@code model} is {@code null}
		 */
		public static <M, F> UpdateResult<M, F> noEffect(M model) {
			if (model == null) {
				throw new IllegalArgumentException("Model must not be null");
			}
			return new UpdateResult<>(model, Optional.empty());
		}

		/**
		 * Creates an {@code UpdateResult} representing an updated model with a
		 * side-effect to execute.
		 *
		 * @param model  the updated model state; must not be {@code null}
		 * @param effect the side-effect to execute; must not be {@code null}
		 * @param <M>    the model type
		 * @param <F>    the effect type
		 * @return an {@code UpdateResult} with the given model and effect
		 *         present
		 * @throws IllegalArgumentException if {@code model} or {@code effect}
		 *                                  is {@code null}
		 */
		public static <M, F> UpdateResult<M, F> withEffect(M model, F effect) {
			if (model == null || effect == null) {
				throw new IllegalArgumentException("Model and effect must not be null");
			}
			return new UpdateResult<>(model, Optional.of(effect));
		}
	}

	/**
	 * Default constructor: model will be initialized via {@link #initModel()}.
	 */
	protected AbstractTeaComponent() {
		// model will be set in initUI()
	}

	/**
	 * Constructor accepting an initial model.
	 * 
	 * @param initialModel The model to use for this component.
	 */
	protected AbstractTeaComponent(M initialModel) {
		this.model = initialModel;
		this.modelProvided = true;
	}

	/**
	 * Initializes the model state when the component is first created.
	 *
	 * @return The initial model instance representing the UI state.
	 */
	protected abstract M initModel();

	/**
	 * Processes a message/event and produces a new model state along with an
	 * optional side-effect to execute.
	 *
	 * @param msg   The incoming message or event to process.
	 * @param model The current model state.
	 * @return An UpdateResult containing the new model and an optional effect.
	 */
	protected abstract UpdateResult<M, F> updateModel(E msg, M model);

	/**
	 * Generates the Swing UI component tree representing the current model
	 * state. This method should be pure and free of side-effects.
	 *
	 * @param model      The current UI state.
	 * @param dispatcher A callback function to dispatch messages/events.
	 * @return The root Swing component for this view.
	 */
	protected abstract JComponent renderView(M model, Consumer<E> dispatch);

	/**
	 * Handles side-effects triggered by the update method. Override to perform
	 * actions like I/O, showing dialogs, or other effects.
	 *
	 * @param effect The side-effect or command to handle.
	 */
	protected void handleEffect(F effect) {
		// Default: do nothing. Override in subclasses.
	}

	/**
	 * Entry point to create and initialize the UI component. This method
	 * initializes the model and generates the initial UI.
	 *
	 * @return The fully constructed root Swing component for this TEA
	 *         component.
	 */
	public final JComponent initUI() {
		if (!modelProvided) {
			this.model = initModel();
		}
		return renderView(model, this::dispatch);
	}

	/**
	 * Dispatches a message/event: updates the model, refreshes the UI, and
	 * handles any resulting side-effects.
	 *
	 * @param msg The message/event to process.
	 */
	public final void dispatch(E msg) {
		UpdateResult<M, F> result = updateModel(msg, model);
		this.model = result.model();
		refreshView();
		result.effect.ifPresent(this::handleEffect);
	}

	/**
	 * Refreshes the view after the model has been updated. Override to update
	 * or re-render UI components as needed. By default, this method does
	 * nothing.
	 */
	protected void refreshView() {
		// Optional: Re-render the view, if needed (revalidate, repaint)
	}
}
