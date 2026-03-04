/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.util.List;

import javax.swing.JPopupMenu;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.ChangedDataManager;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.share.swing.PopupMenuTrait;

public class EditMapPanelHostProperties extends EditMapPanelX {
	private Runnable updateContent;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public EditMapPanelHostProperties(boolean keylistExtendible, boolean entryRemovable, Runnable updateContent) {
		super(keylistExtendible, entryRemovable);
		this.updateContent = updateContent;
	}

	@Override
	protected JPopupMenu createBasicPopup() {
		return new PopupMenuTrait(List.of(PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD),
				event -> updatePopupMenu(), table) {
			@Override
			public void action(int p) {
				super.action(p);
				if (p == PopupMenuTrait.POPUP_RELOAD) {
					ConfigedMain.getMainFrame().activateLoadingCursor();
					if (!CacheIdentifier.ALL_DATA.toString().equals(persistenceController.getTriggeredEvent())) {
						persistenceController.reloadData(ReloadEvent.DEPOT_PROPERTIES_DATA_RELOAD.toString());
					}

					updateContent.run();

					ConfigedMain.getMainFrame().deactivateLoadingCursor();
				}
				if (p == PopupMenuTrait.POPUP_SAVE) {
					ChangedDataManager.checkSaveAll(false);
				}
			}
		};
	}
}
