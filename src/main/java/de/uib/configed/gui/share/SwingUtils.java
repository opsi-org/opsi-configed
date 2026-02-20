/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;

/**
 * Utility methods for Swing component handling and event-related helpers.
 * <p>
 * This class provides reusable helpers for working with Swing components,
 * including key bindings, document listeners, EDT handling, and small UI
 * conveniences.
 * </p>
 * <p>
 * Methods here should not display dialogs directly. Dialog creation belongs in
 * {@link DialogUtils}.
 * </p>
 */
public final class SwingUtils {
	private static final Set<String> BLACKLISTED_KEYWORDS_PASSWORD = Set.of("netboot.linux-bootimage.cmdline.pwh");
	private static final Set<String> WHITELISTED_KEYWORDS_PASSWORD = Set.of("netboot.use_host_onetime_password");

	private static Parser markdownParser = Parser.builder()
			.extensions(Arrays.asList(AutolinkExtension.create(), TablesExtension.create())).build();
	private static HtmlRenderer renderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();

	private SwingUtils() {
	}

	public static String createTooltipForPropertyName(String propertyName, Map<String, Object> defaultsMap,
			Map<String, String> descriptionsMap, String additionalTooltipText) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder tooltip = new StringBuilder();

		if (defaultsMap != null && defaultsMap.get(propertyName) != null) {
			if (additionalTooltipText != null && !additionalTooltipText.isEmpty()) {
				tooltip.append("default (" + additionalTooltipText + "): ");
			} else {
				tooltip.append("default: ");
			}

			if (isKeyForSecretValue(propertyName)) {
				tooltip.append(Globals.STARRED_STRING);
			} else {
				tooltip.append(defaultsMap.get(propertyName));
			}
		}

		if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
			tooltip.append(parseMarkdown(descriptionsMap.get(propertyName)));
		}

		if (tooltip.length() > 200) {
			Logging.debug("tooltip length is ", tooltip.length());
			tooltip.insert(0, "<div style='width: 500px'>");
			tooltip.append("</div>");
		}

		return "<html>" + tooltip + "</html>";
	}

	public static boolean isKeyForSecretValue(String key) {
		String keyLowerCase = key.toLowerCase(Locale.ROOT);

		if (BLACKLISTED_KEYWORDS_PASSWORD.contains(keyLowerCase)) {
			return true;
		} else if (WHITELISTED_KEYWORDS_PASSWORD.contains(keyLowerCase)) {
			return false;
		} else {
			return keyLowerCase.indexOf("password") > -1 || keyLowerCase.indexOf("secret") > -1;
		}
	}

	public static String parseMarkdown(String markdown) {
		if (markdown == null) {
			return "";
		}

		Node document = markdownParser.parse(markdown);
		return renderer.render(document);
	}

	public static void addKeyBindingToJComponent(JComponent component, KeyStroke keyStroke, Runnable runnable,
			int condition) {
		Logging.info(keyStroke.toString(), " added to ", component.getClass().getSimpleName());
		component.getInputMap(condition).put(keyStroke, keyStroke.toString());
		component.getActionMap().put(keyStroke.toString(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Logging.debug(component.getClass().getSimpleName(), " ", keyStroke.toString(), " triggered");
				runnable.run();
			}
		});
	}

	public static void addKeyBindingToJComponent(JComponent component, KeyStroke keyStroke, Runnable runnable) {
		Logging.info(keyStroke.toString(), " added to ", component.getClass().getSimpleName());
		addKeyBindingToJComponent(component, keyStroke, runnable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	public static DocumentListener onDocumentChange(Runnable runnable) {
		return onDocumentChange(runnable, true);
	}

	public static DocumentListener onDocumentChangeWithoutRemoveUpdate(Runnable runnable) {
		return onDocumentChange(runnable, false);
	}

	private static DocumentListener onDocumentChange(Runnable runnable, boolean reactOnChangeUpdate) {
		return new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				runnable.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (reactOnChangeUpdate) {
					runnable.run();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				runnable.run();
			}
		};
	}

	public static JLabel createBoldLabel(String ressourceId) {
		JLabel label = new JLabel(Configed.getResourceValue(ressourceId));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}

	public static void runOnEventDispatchThread(Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(runnable);
		} else {
			runnable.run();
		}
	}

	public static <T> void runSwingWorker(Supplier<T> backgroundTask, Consumer<T> doneTask,
			Consumer<Exception> exceptionHandler) {

		Consumer<Exception> finalExceptionHandler = exceptionHandler != null ? exceptionHandler : ((Exception e) -> {
		});

		SwingWorker<T, Void> worker = new SwingWorker<>() {
			@Override
			protected T doInBackground() {
				return backgroundTask.get();
			}

			@Override
			protected void done() {
				try {
					T result = get();
					doneTask.accept(result);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					finalExceptionHandler.accept(e);
				} catch (ExecutionException e) {
					finalExceptionHandler.accept(e);
				}
			}
		};
		worker.execute();
	}
}
