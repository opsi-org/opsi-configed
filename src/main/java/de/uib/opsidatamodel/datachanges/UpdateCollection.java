package de.uib.opsidatamodel.datachanges;

import java.util.Collection;
import java.util.Iterator;

import de.uib.utilities.logging.Logging;

/**
*/
public class UpdateCollection implements UpdateCommand, CountedCollection {

	protected Collection<Object> implementor;

	// we delegate all Collection methods to this object extending only add()
	public UpdateCollection(Collection<Object> implementor) {
		this.implementor = implementor;
	}

	@Override
	public boolean addAll(Collection c) {
		boolean success = true;
		Iterator it = c.iterator();
		while (it.hasNext() && success) {
			Object ob = it.next();
			Logging.debug(this, "addAll, element of Collection: " + ob);
			if (!add(ob)) {
				success = false;
			}
		}
		return success;
	}

	@Override
	public void clear() {
		Logging.debug(this, "clear()");
		Iterator<Object> it = implementor.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			// a element of the collection is a collection, we do our best to clear recursively
			if (obj instanceof Collection) {
				Logging.debug(this, "by recursion, we will clear " + obj);
				((Collection) obj).clear();
			}

		}
		Logging.debug(this, "to clear elements of implementor " + implementor);
		implementor.clear();
		Logging.debug(this, "cleared: elements of implementor " + implementor.size());
	}

	public void clearElements()
	// *** perhaps we should instead implement a recursive empty which clears only
	// the implementors but does not remove the elements
	{

		Iterator<Object> it = implementor.iterator();
		while (it.hasNext()) {
			Object obj = it.next();

			// a element of the collection is a collection, we do our best to clear recursively
			if (obj instanceof UpdateCollection) {
				((UpdateCollection) obj).clearElements();
			}
		}
	}

	public void revert() {
		Logging.info(this, "revert()");

		Iterator<Object> it = implementor.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof UpdateCollection)
			// a element of the collection is a collection, we do our best to clear
			// recursively
			{

				((UpdateCollection) obj).revert();

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
	public boolean containsAll(Collection c) {
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
	public Iterator<Object> iterator() {
		return implementor.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return implementor.remove(o);
	}

	@Override
	public boolean removeAll(Collection c) {
		return implementor.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection c) {
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
	@SuppressWarnings("unused")
	public boolean add(Object obj) {
		Logging.debug(this, "###### UpdateCollection add Object  " + obj);
		if (!(obj instanceof UpdateCommand)) {
			Logging.error("Wrong element type, found" + obj.getClass().getName() + ", expected an UpdateCommand");

			return false;
		}

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
		if (size() == 0) {
			return 0;
		}

		int result = 0;

		Iterator<Object> it = implementor.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj != null) {
				// a element of the collection is a collection, we retrieve the size recursively
				if (obj instanceof CountedCollection) {
					result = result + ((CountedCollection) obj).accumulatedSize();
				} else {
					// we found an 'ordinary' element and add 1
					result++;
				}
			}
		}

		return result;
	}

	@Override
	public Object getController() {
		return null;
	}

	@Override
	public void setController(Object cont) {
		/* Not needed */}

	/**
	 * doCall calls doCall on all members. This will give a recursion for
	 * members being update collections themselves.
	 */
	@Override
	public void doCall() {
		Logging.debug(this, "doCall, element count: " + size());

		if (size() == 0) {
			return;
		}

		Iterator<Object> it = implementor.iterator();
		while (it.hasNext()) {
			UpdateCommand theCommand = ((UpdateCommand) it.next());

			if (theCommand != null) {
				theCommand.doCall();
			}
		}

	}

}
