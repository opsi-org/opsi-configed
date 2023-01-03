package de.uib.utilities.datastructure;

import java.util.HashMap;
import java.util.List;

//very similar to TableEntry
public class RelationElement<S, O> extends HashMap<S, O> {

	protected List<S> allowedAttributes;
	protected List<S> minimalAttributes;

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
		assert allowedAttributes == null || allowedAttributes.indexOf(key) >= 0 : /*
																					 * if not, we get the error message:
																					 */ "key " + key + " not allowed";

		return super.get(key);
	}

	public List<S> getAllowedAttributes() {
		return allowedAttributes;
	}

}
