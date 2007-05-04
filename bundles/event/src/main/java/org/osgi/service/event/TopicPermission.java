package org.osgi.service.event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Permission;
import java.security.PermissionCollection;

/**
 * topic permission.
 * 
 * @author OSGi.
 * 
 */
public final class TopicPermission extends Permission {

	/**
	 * the serial UID.
	 */
	static final long serialVersionUID = 0xaebcdbab3e0c1284L;

	/**
	 * publish.
	 */
	public static final String PUBLISH = "publish";

	/**
	 * subscribe.
	 */
	public static final String SUBSCRIBE = "subscribe";

	/**
	 * action publish.
	 */
	private static final int ACTION_PUBLISH = 1;

	/**
	 * action subscribe.
	 */
	private static final int ACTION_SUBSCRIBE = 2;

	/**
	 * action all.
	 */
	private static final int ACTION_ALL = 3;

	/**
	 * action none.
	 */
	private static final int ACTION_NONE = 0;

	/**
	 * action mask.
	 */
	private transient int action_mask;

	/**
	 * prefix.
	 */
	private transient String prefix;

	/**
	 * actions.
	 */
	private String actions;

	/**
	 * create new topic permission.
	 * 
	 * @param name
	 *            the name.
	 * @param actions
	 *            the actions.
	 */
	public TopicPermission(final String name, final String actions) {
		this(name, getMask(actions));
	}

	/**
	 * create new topic permission.
	 * 
	 * @param name
	 *            the name.
	 * @param mask
	 *            the mask.
	 */
	TopicPermission(final String name, final int mask) {
		super(name);
		action_mask = 0;
		actions = null;
		init(name, mask);
	}

	/**
	 * init the permission.
	 * 
	 * @param name
	 *            the name.
	 * @param mask
	 *            the mask.
	 */
	private void init(final String name, final int mask) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("invalid name");
		}
		if (name.equals("*")) {
			prefix = "";
		} else if (name.endsWith("/*")) {
			prefix = name.substring(0, name.length() - 1);
		} else {
			prefix = null;
		}
		if (mask == 0 || (mask & 3) != mask) {
			throw new IllegalArgumentException("invalid action string");
		} else {
			action_mask = mask;
			return;
		}
	}

	/**
	 * get the mask.
	 * 
	 * @param actions
	 *            the actions.
	 * @return the mask.
	 */
	private static int getMask(final String actions) {
		boolean seencomma = false;
		int mask = 0;
		if (actions == null) {
			return mask;
		}
		char a[] = actions.toCharArray();
		int i = a.length - 1;
		if (i < 0) {
			return mask;
		}
		int matchlen;
		for (; i != -1; i -= matchlen) {
			char c;
			while (i != -1
					&& ((c = a[i]) == ' ' || c == '\r' || c == '\n'
							|| c == '\f' || c == '\t')) {
				i--;
			}
			if (i >= 8 && (a[i - 8] == 's' || a[i - 8] == 'S')
					&& (a[i - 7] == 'u' || a[i - 7] == 'U')
					&& (a[i - 6] == 'b' || a[i - 6] == 'B')
					&& (a[i - 5] == 's' || a[i - 5] == 'S')
					&& (a[i - 4] == 'c' || a[i - 4] == 'C')
					&& (a[i - 3] == 'r' || a[i - 3] == 'R')
					&& (a[i - 2] == 'i' || a[i - 2] == 'I')
					&& (a[i - 1] == 'b' || a[i - 1] == 'B')
					&& (a[i] == 'e' || a[i] == 'E')) {
				matchlen = 9;
				mask |= 2;
			} else if (i >= 6 && (a[i - 6] == 'p' || a[i - 6] == 'P')
					&& (a[i - 5] == 'u' || a[i - 5] == 'U')
					&& (a[i - 4] == 'b' || a[i - 4] == 'B')
					&& (a[i - 3] == 'l' || a[i - 3] == 'L')
					&& (a[i - 2] == 'i' || a[i - 2] == 'I')
					&& (a[i - 1] == 's' || a[i - 1] == 'S')
					&& (a[i] == 'h' || a[i] == 'H')) {
				matchlen = 7;
				mask |= 1;
			} else {
				throw new IllegalArgumentException("invalid permission: "
						+ actions);
			}
			for (seencomma = false; i >= matchlen && !seencomma;) {
				switch (a[i - matchlen]) {
				default:
					throw new IllegalArgumentException("invalid permission: "
							+ actions);

				case 44: // ','
					seencomma = true;
				// fall through

				case 9: // '\t'
				case 10: // '\n'
				case 12: // '\f'
				case 13: // '\r'
				case 32: // ' '
					i--;
					break;
				}
			}

		}

		if (seencomma) {
			throw new IllegalArgumentException("invalid permission: " + actions);
		} else {
			return mask;
		}
	}

	/**
	 * check if the permission implies another permission.
	 * 
	 * @param p
	 *            the other permission.
	 * @return true or false.
	 */
	public boolean implies(final Permission p) {
		if (p instanceof TopicPermission) {
			TopicPermission target = (TopicPermission) p;
			if ((action_mask & target.action_mask) == target.action_mask) {
				if (prefix != null) {
					return target.getName().startsWith(prefix);
				} else {
					return target.getName().equals(getName());
				}
			}
		}
		return false;
	}

	/**
	 * get the actions.
	 * 
	 * @return the actions.
	 */
	public String getActions() {
		if (actions == null) {
			StringBuffer sb = new StringBuffer();
			boolean comma = false;
			if ((action_mask & 1) == 1) {
				sb.append("publish");
				comma = true;
			}
			if ((action_mask & 2) == 2) {
				if (comma) {
					sb.append(',');
				}
				sb.append("subscribe");
			}
			actions = sb.toString();
		}
		return actions;
	}

	/**
	 * get a new permission collection.
	 * 
	 * @return the permission collection.
	 */
	public PermissionCollection newPermissionCollection() {
		return new TopicPermissionCollection();
	}

	/**
	 * check if the permission equals another object.
	 * 
	 * @param obj
	 *            the other object.
	 * @return true or false.
	 */
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TopicPermission)) {
			return false;
		}
		TopicPermission p = (TopicPermission) obj;
		return action_mask == p.action_mask && getName().equals(p.getName());
	}

	/**
	 * get the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		return getName().hashCode() ^ getActions().hashCode();
	}

	/**
	 * get the mask.
	 * 
	 * @return the mask.
	 */
	int getMask() {
		return action_mask;
	}

	/**
	 * write the object.
	 * 
	 * @param s
	 *            the object output stream.
	 * @throws IOException
	 *             if something goes wrong.
	 */
	private synchronized void writeObject(final ObjectOutputStream s)
			throws IOException {
		if (actions == null) {
			getActions();
		}
		s.defaultWriteObject();
	}

	/**
	 * read the object.
	 * 
	 * @param s
	 *            the object input stream.
	 * @throws IOException
	 *             if something goes wrong.
	 * @throws ClassNotFoundException
	 *             if the class could not be found.
	 */
	private synchronized void readObject(final ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		init(getName(), getMask(actions));
	}
}
