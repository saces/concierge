package org.osgi.service.event;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;

// Referenced classes of package org.osgi.service.event:
//            TopicPermission

/**
 * a topic permission collection.
 * 
 * @author OSGi.
 */
final class TopicPermissionCollection extends PermissionCollection {

	/**
	 * the serial UID.
	 */
	static final long serialVersionUID = 0xf7785518f07ab130L;

	/**
	 * the permissions.
	 */
	private Hashtable permissions;

	/**
	 * are all allowed ?
	 */
	private boolean all_allowed;

	/**
	 * create a new TPC.
	 * 
	 */
	public TopicPermissionCollection() {
		permissions = new Hashtable();
		all_allowed = false;
	}

	/**
	 * add a permission to the collection.
	 * 
	 * @param permission
	 *            the permission to add.
	 */
	public void add(final Permission permission) {
		if (!(permission instanceof TopicPermission)) {
			throw new IllegalArgumentException("invalid permission: "
					+ permission);
		}
		if (isReadOnly()) {
			throw new SecurityException(
					"attempt to add a Permission to a readonly PermissionCollection");
		}
		TopicPermission pp = (TopicPermission) permission;
		String name = pp.getName();
		TopicPermission existing = (TopicPermission) permissions.get(name);
		if (existing != null) {
			int oldMask = existing.getMask();
			int newMask = pp.getMask();
			if (oldMask != newMask) {
				permissions.put(name, new TopicPermission(name, oldMask
						| newMask));
			}
		} else {
			permissions.put(name, permission);
		}
		if (!all_allowed && name.equals("*")) {
			all_allowed = true;
		}
	}

	/**
	 * do the permission in the collection imply another permission ?
	 * 
	 * @param permission
	 *            the other permission.
	 * @return true or false.
	 */
	public boolean implies(final Permission permission) {
		if (!(permission instanceof TopicPermission)) {
			return false;
		}
		TopicPermission pp = (TopicPermission) permission;
		int desired = pp.getMask();
		int effective = 0;
		TopicPermission x;
		if (all_allowed) {
			x = (TopicPermission) permissions.get("*");
			if (x != null) {
				effective |= x.getMask();
				if ((effective & desired) == desired) {
					return true;
				}
			}
		}
		String name = pp.getName();
		x = (TopicPermission) permissions.get(name);
		if (x != null) {
			effective |= x.getMask();
			if ((effective & desired) == desired) {
				return true;
			}
		}
		int last;
		for (int offset = name.length() - 1; (last = name.lastIndexOf("/",
				offset)) != -1; offset = last - 1) {
			name = name.substring(0, last + 1) + "*";
			x = (TopicPermission) permissions.get(name);
			if (x != null) {
				effective |= x.getMask();
				if ((effective & desired) == desired) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * get the elements.
	 * 
	 * @return an enumeration over the elements.
	 */
	public Enumeration elements() {
		return permissions.elements();
	}
}
