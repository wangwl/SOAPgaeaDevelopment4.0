/*******************************************************************************
 * Copyright (c) 2017, BGI-Shenzhen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.bgi.flexlab.gaea.tools.haplotypecaller.activeRegion;

import org.bgi.flexlab.gaea.data.structure.location.GenomeLocation;


public class ActivityProfileState {
	final private GenomeLocation loc;
	public double isActiveProb;
	public Type resultState;
	public Number resultValue;
	
	public enum Type {
		NONE,
		HIGH_QUALITY_SOFT_CLIPS
	}
	
	/**
     * Create a new ActivityProfileState at loc with probability of being active of isActiveProb
     *
     * @param loc the position of the result profile (for debugging purposes)
     * @param isActiveProb the probability of being active (between 0 and 1)
     */
    //@Requires({"loc != null", "isActiveProb >= 0.0 && isActiveProb <= 1.0"})
    public ActivityProfileState(final GenomeLocation loc, final double isActiveProb) {
        this(loc, isActiveProb, Type.NONE, null);
    }
    
    /**
     * Create a new ActivityProfileState at loc with probability of being active of isActiveProb that maintains some
     * information about the result state and value
     *
     * The only state value in use is HIGH_QUALITY_SOFT_CLIPS, and here the value is interpreted as the number
     * of bp affected by the soft clips.
     *
     * @param loc the position of the result profile (for debugging purposes)
     * @param isActiveProb the probability of being active (between 0 and 1)
     */
    //@Requires({"loc != null", "isActiveProb >= 0.0 && isActiveProb <= 1.0"})
    public ActivityProfileState(final GenomeLocation loc, final double isActiveProb, final Type resultState, final Number resultValue) {
        // make sure the location of that activity profile is 1
        if ( loc.size() != 1 )
            throw new IllegalArgumentException("Location for an ActivityProfileState must have to size 1 bp but saw " + loc);
        if ( resultValue != null && resultValue.doubleValue() < 0 )
            throw new IllegalArgumentException("Result value isn't null and its < 0, which is illegal: " + resultValue);

        this.loc = loc;
        this.isActiveProb = isActiveProb;
        this.resultState = resultState;
        this.resultValue = resultValue;
    }
    
    public String toString() {
        return "ActivityProfileState{" +
                "loc=" + loc +
                ", isActiveProb=" + isActiveProb +
                ", resultState=" + resultState +
                ", resultValue=" + resultValue +
                '}';
    }
}