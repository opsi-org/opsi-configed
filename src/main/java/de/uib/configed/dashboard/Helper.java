/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import de.uib.configed.Configed;

public final class Helper {
	private Helper() {
	}

	public static <T, V> boolean mapsInMapAreEmpty(Map<T, Map<T, V>> map) {
		if (map.isEmpty()) {
			return true;
		}

		for (Map<T, V> value : map.values()) {
			if (!value.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public static <T> void fillMapOfListsForDepots(Map<String, List<T>> map, List<T> data, String depot) {
		if (map.containsKey(Configed.getResourceValue("Dashboard.selection.allDepots"))) {
			List<T> dataCombined = map.get(Configed.getResourceValue("Dashboard.selection.allDepots"));
			dataCombined.addAll(data);
			map.put(Configed.getResourceValue("Dashboard.selection.allDepots"), dataCombined);
		} else {
			map.put(Configed.getResourceValue("Dashboard.selection.allDepots"), new ArrayList<>(data));
		}
		map.put(depot, data);
	}

	public static <T, V> void fillMapOfMapsForDepots(Map<String, Map<T, V>> map, Map<T, V> data, String depot) {
		if (map.containsKey(Configed.getResourceValue("Dashboard.selection.allDepots"))) {
			Map<T, V> dataCombined = map.get(Configed.getResourceValue("Dashboard.selection.allDepots"));
			Optional<V> firstElement = data.values().stream().findFirst();
			if (firstElement.isPresent() && firstElement.get() instanceof Integer) {
				handleIntegerType(dataCombined, data);
			} else {
				dataCombined.putAll(data);
			}
			map.put(Configed.getResourceValue("Dashboard.selection.allDepots"), dataCombined);
		} else {
			map.put(Configed.getResourceValue("Dashboard.selection.allDepots"), new HashMap<>(data));
		}
		map.put(depot, data);
	}

	@SuppressWarnings("unchecked")
	private static <T, V> void handleIntegerType(Map<T, V> dataCombined, Map<T, V> data) {
		for (Entry<T, V> entry : data.entrySet()) {
			Integer oldValue = dataCombined.get(entry.getKey()) == null ? 0
					: (Integer) dataCombined.get(entry.getKey());
			Integer newValue = oldValue + (Integer) entry.getValue();
			dataCombined.put(entry.getKey(), (V) newValue);
		}
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage bufferedImage) {
			return bufferedImage;
		}

		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.drawImage(img, 0, 0, null);
		graphics2D.dispose();

		return bufferedImage;
	}

	public static Color adjustColorBrightness(Color color) {
		float[] colorHSB = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

		if (colorHSB[2] < 0.5F) {
			colorHSB[2] += 0.10F;
		} else {
			colorHSB[2] -= 0.10F;
		}

		return new Color(Color.HSBtoRGB(colorHSB[0], colorHSB[1], colorHSB[2]));
	}
}
