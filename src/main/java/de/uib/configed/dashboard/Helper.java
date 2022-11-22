package de.uib.configed.dashboard;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {
	public static <T, V> List<T> combineListsFromMap(Map<V, List<T>> map) {
		List<T> list = new ArrayList<>();

		if (map.isEmpty()) {
			return new ArrayList<T>();
		}

		for (List<T> value : map.values()) {
			list.addAll(value);
		}

		return list;
	}

	public static <T, V> Map<T, V> combineMapsFromMap(Map<T, Map<T, V>> map) {
		Map<T, V> allMaps = new HashMap<>();

		if (map.isEmpty()) {
			return new HashMap<>();
		}

		for (Map<T, V> value : map.values()) {
			allMaps.putAll(value);
		}

		return allMaps;
	}

	public static <T, V> Map<V, V> combineMapsFromMap2(Map<T, Map<V, V>> map) {
		Map<V, V> allMaps = new HashMap<>();

		if (map.isEmpty()) {
			return new HashMap<>();
		}

		for (Map<V, V> value : map.values()) {
			allMaps.putAll(value);
		}

		return allMaps;
	}

	public static <T, V> boolean listsInMapAreEmpty(Map<V, List<T>> map) {
		if (map.isEmpty()) {
			return true;
		}

		for (List<T> value : map.values()) {
			if (!value.isEmpty()) {
				return false;
			}
		}

		return true;
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

		return new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	}
}
