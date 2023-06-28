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

public final class Helper {
	private Helper() {
	}

	public static <T, V> List<T> combineListsFromMap(Map<V, List<T>> map) {

		if (map.isEmpty()) {
			return new ArrayList<>();
		}

		List<T> list = new ArrayList<>();
		for (List<T> value : map.values()) {
			list.addAll(value);
		}

		return list;
	}

	public static <T, V> Map<T, V> combineMapsFromMap(Map<T, Map<T, V>> map) {

		if (map.isEmpty()) {
			return new HashMap<>();
		}

		Map<T, V> allMaps = new HashMap<>();

		for (Map<T, V> value : map.values()) {
			allMaps.putAll(value);
		}

		return allMaps;
	}

	public static <T, V> Map<V, V> combineMapsFromMap2(Map<T, Map<V, V>> map) {

		if (map.isEmpty()) {
			return new HashMap<>();
		}

		Map<V, V> allMaps = new HashMap<>();

		for (Map<V, V> value : map.values()) {
			allMaps.putAll(value);
		}

		return allMaps;
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

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
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
