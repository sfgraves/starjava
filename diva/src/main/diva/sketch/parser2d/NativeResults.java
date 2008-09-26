/*
 * $Id: NativeResults.java,v 1.2 2001/07/22 22:01:50 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.sketch.parser2d;
import diva.sketch.recognition.TypedData;

/**
 * A class that encapsulates the parse results of a native rule so
 * that native rules do not have to deal with the implementation
 * details of the parser.  All we want back from the native rule
 * is the typed data result and some probability associated with
 * that result.
 *
 * @author  Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.2 $
 * @rating Red
 */
public class NativeResults {
    /**
     * The typed data that was generated by the rule.
     */
    public TypedData data;

    /**
     * The confidence associated with this result, between 0 and 100.
     */
    public double probability;

    /**
     * Construct a new result object that contains the given
     * data and probability.
     */
    public NativeResults(TypedData data, double probability) {
        this.data = data;
        this.probability = probability;
    }

    /**
     * Hide this constructor from the public.
     */
    private NativeResults() {}
}
