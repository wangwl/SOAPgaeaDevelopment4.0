package org.bgi.flexlab.gaea.tools.baserecalibration.covariates;

import org.bgi.flexlab.gaea.data.structure.bam.GaeaSamRecord;
import org.bgi.flexlab.gaea.tools.baserecalibration.ReadCovariates;
import org.bgi.flexlab.gaea.tools.mapreduce.recalibrator.BaseRecalibrationOptions;

/**
 * The Covariate interface. A Covariate is a feature used in the recalibration that can be picked out of the read.
 * In general most error checking and adjustments to the data are done before the call to the covariates getValue methods in order to speed up the code.
 * This unfortunately muddies the code, but most of these corrections can be done per read while the covariates get called per base, resulting in a big speed up.
 */

public interface Covariate {

    /**
     * Initialize any member variables using the command-line arguments passed to the walker
     *
     * @param RAC the recalibration argument collection
     */
    public void initialize(final BaseRecalibrationOptions RAC);

    /**
     * Calculates covariate values for all positions in the read.
     *
     * @param read   the read to calculate the covariates on.
     * @param values the object to record the covariate values for every base in the read.
     */
    public void recordValues(final GaeaSamRecord read, final ReadCovariates values);

    /**
     * Used to get the covariate's value from input (Recalibration Report) file during on-the-fly recalibration
     *
     * @param str the key in string type (read from the csv)
     * @return the key in it's correct type.
     */
    public Object getValue(final String str);

    /**
     * Converts the internal representation of the key to String format for file output.
     *
     * @param key the long representation of the key
     * @return a string representation of the key
     */
    public String formatKey(final int key);

    /**
     * Converts an Object key into a long key using only the lowest numberOfBits() bits
     *
     * Only necessary for on-the-fly recalibration when you have the object, but need to store it in memory in long format. For counting covariates
     * the getValues method already returns all values in long format.
     *
     * @param value the object corresponding to the covariate
     * @return a long representation of the object
     */
    public int keyFromValue(final Object value);

    /**
     * Returns the maximum value possible for any key representing this covariate
     *
     * @return the maximum value possible for any key representing this covariate
     */
    public int maximumKeyValue();
}

