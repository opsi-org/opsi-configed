package de.uib.opsicommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class POJOReMapper {
	public static <T> T remap(Object obj, TypeReference<T> typeRef) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

		return mapper.convertValue(obj, typeRef);
	}

	public static boolean equalsNull(String ob) {
		return ob == null || "null".equalsIgnoreCase(ob);
	}

	public static Map<String, String> giveEmptyForNull(Map<String, Object> m) {
		HashMap<String, String> result = new HashMap<>();
		for (Entry<String, Object> entry : m.entrySet()) {
			if (entry.getValue() == null) {
				result.put(entry.getKey(), "");
			} else {
				result.put(entry.getKey(), "" + entry.getValue());
			}
		}

		return result;
	}
}
