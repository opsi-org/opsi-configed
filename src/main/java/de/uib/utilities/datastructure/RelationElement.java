package de.uib.utilities.datastructure;

import java.util.HashMap;
import java.util.List;

import de.uib.utilities.logging.Logging;

//very similar to TableEntry
public class RelationElement<S, O> extends HashMap<S, O> {

	protected List<S> allowedAttributes;

	public RelationElement() {
		super();
	}

	public RelationElement(RelationElement<S, O> rowmap) {
		super(rowmap);
	}

	public void setAllowedAttributes(List<S> allowedAttributes) {
		this.allowedAttributes = allowedAttributes;
	}

	@Override
	public O get(Object key) {
		if (allowedAttributes != null && allowedAttributes.indexOf(key) < 0) {
			Logging.error(this, "key " + key + " not allowed");
			return null;
		} else {
			return super.get(key);
		}
	}

	public List<S> getAllowedAttributes() {
		return allowedAttributes;
	}

}
