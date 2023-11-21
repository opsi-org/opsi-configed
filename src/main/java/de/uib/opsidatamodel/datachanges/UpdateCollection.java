/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.datachanges;

import java.util.Collection;
import java.util.Iterator;

import de.uib.utilities.logging.Logging;

/**
*/

public class UpdateCollection implements UpdateCommand, CountedCollection {
	protected Collection<UpdateCommand> implementor;

	// we delegate all Collection methods to this object extending only add()
	public UpdateCollection(Collection<UpdateCommand> implementor) {
		this.implementor = implementor;
	}

	@Override
	public boolean addAll(Collection<? extends UpdateCommand> c) {
		boolean success = true;
		Iterator<? extends UpdateCommand> it = c.iterator();
		while (it.hasNext() && success) {
			UpdateCommand updateCommand = it.next();
			Logging.debug(this, "addAll, element of Collection: " + updateCommand);
			if (!add(updateCommand)) {
				success = false;
			}
		}
		return success;
	}

	@Override
	public void clear() {
		Logging.debug(this, "clear()");
		Iterator<UpdateCommand> it = implementor.iterator();
		while (it.hasNext()) {
			UpdateCommand updateCommand = it.next();
			// a element of the collection is a collection, we do our best to clear recursively
			if (updateCommand instanceof Collection) {
				Logging.debug(this, "by recursion, we will clear " + updateCommand);
				((Collection<?>) updateCommand).clear();
			}
		}
		Logging.debug(this, "to clear elements of implementor " + implementor);
		implementor.clear();
		Logging.debug(this, "cleared: elements of implementor " + implementor.size());
	}

	public void clearElements() {
		// *** perhaps we should instead implement a recursive empty which clears only
		// the implementors but does not remove the elements

		Iterator<UpdateCommand> it = implementor.iterator();
		while (it.hasNext()) {
			UpdateCommand updateCommand = it.next();

			// a element of the collection is a collection, we do our best to clear recursively
			if (updateCommand instanceof UpdateCollection) {
				((UpdateCollection) updateCommand).clearElements();
			}
		}
	}

	public void revert() {
		Logging.info(this, "revert()");

		Iterator<UpdateCommand> it = implementor.iterator();
		while (it.hasNext()) {
			UpdateCommand updateCommand = it.next();
			if (updateCommand instanceof UpdateCollection) {
				// a element of the collection is a collection, we do our best to clear
				// recursively

				((UpdateCollection) updateCommand).revert();
			}
		}
	}

	public void cancel() {
		revert();
		clearElements();
	}

	@Override
	public boolean contains(Object o) {
		return implementor.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return implementor.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		return implementor.equals(o);
	}

	@Override
	public int hashCode() {
		return implementor.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return implementor.isEmpty();
	}

	@Override
	public Iterator<UpdateCommand> iterator() {
		return implementor.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return implementor.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return implementor.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return implementor.retainAll(c);
	}

	@Override
	public int size() {
		return implementor.size();
	}

	@Override
	public Object[] toArray() {
		return implementor.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return implementor.toArray(a);
	}

	@Override
	public boolean add(UpdateCommand obj) {
		Logging.debug(this, "###### UpdateCollection add Object  " + obj);

		if (obj == null) {
			return true;
		}

		return implementor.add(obj);
	}

	@Override
	public String toString() {
		return implementor.toString();
	}

	@Override
	public int accumulatedSize() {
		if (isEmpty()) {
			return 0;
		}

		int result = 0;

		Iterator<UpdateCommand> it = implementor.iterator();
		while (it.hasNext()) {
			UpdateCommand updateCommand = it.next();
			if (updateCommand != null) {
				// a element of the collection is a collection, we retrieve the size recursively
				if (updateCommand instanceof CountedCollection) {
					result = result + ((CountedCollection) updateCommand).accumulatedSize();
				} else {
					// we found an 'ordinary' element and add 1
					result++;
				}
			}
		}

		return result;
	}

	/**
	 * doCall calls doCall on all members. This will give a recursion for
	 * members being update collections themselves.
	 */
	@Override
	public void doCall() {
		Logging.debug(this, "doCall, element count: " + size());

		if (isEmpty()) {
			return;
		}

		Iterator<UpdateCommand> it = implementor.iterator();
		while (it.hasNext()) {
			UpdateCommand theCommand = it.next();

			if (theCommand != null) {
				theCommand.doCall();
			}
		}
	}
}
