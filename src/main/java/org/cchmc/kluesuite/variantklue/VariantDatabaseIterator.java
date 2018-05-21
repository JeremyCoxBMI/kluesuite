package org.cchmc.kluesuite.variantklue;

import java.util.Iterator;

/**
 * Created by osboxes on 13/04/17.
 */
public interface VariantDatabaseIterator<E> extends Iterator<E> {

    public E peekPosition();

}
