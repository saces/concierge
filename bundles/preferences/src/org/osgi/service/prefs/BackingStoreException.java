/*
 * @(#)BackingStoreException.java   1.3 01/07/18
 * $Header: /cvsroot/concierge/bundles/other/org/osgi/service/prefs/BackingStoreException.java,v 1.1 2005/09/07 21:06:15 rjan Exp $
 *
 * Open Services Gateway Initiative (OSGi) Confidential. 
 * 
 * (C) Copyright 1996-2001 Sun Microsystems, Inc. 
 * 
 * This source code is licensed to OSGi as MEMBER LICENSED MATERIALS 
 * under the terms of Section 3.2 of the OSGi MEMBER AGREEMENT.
 * 
 */

package org.osgi.service.prefs;

/**
 * Thrown to indicate that a preferences operation could not complete because
 * of a failure in the backing store, or a failure to contact the backing
 * store.
 *
 * @version $Revision: 1.1 $
 * @author Open Services Gateway Initiative
 */

public class BackingStoreException extends Exception {
    /**
     * Constructs a <tt>BackingStoreException</tt> with the specified detail message.
     *
     * @param s the detail message.
     */
    public BackingStoreException(String s) {
        super(s);
    }
}
