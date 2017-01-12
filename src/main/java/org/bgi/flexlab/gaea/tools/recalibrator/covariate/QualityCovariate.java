package org.bgi.flexlab.gaea.tools.recalibrator.covariate;

import org.bgi.flexlab.gaea.data.structure.bam.GaeaSamRecord;
import org.bgi.flexlab.gaea.tools.mapreduce.realigner.RecalibratorOptions;
import org.bgi.flexlab.gaea.tools.recalibrator.ReadCovariates;
import org.bgi.flexlab.gaea.util.QualityUtils;

public class QualityCovariate implements RequiredCovariate {

	@Override
	public void initialize(RecalibratorOptions option) {}

	@Override
	public void recordValues(final GaeaSamRecord read, final ReadCovariates values) {
		final byte[] baseQualities = read.getBaseQualities();
		final byte[] baseInsertionQualities = read.getBaseInsertionQualities(true);
		final byte[] baseDeletionQualities = read.getBaseDeletionQualities(true);

		for (int i = 0; i < baseQualities.length; i++) {
			values.addCovariate(baseQualities[i], baseInsertionQualities[i], baseDeletionQualities[i], i);
		}
	}

	@Override
	public final Object getValue(final String str) {
		return Byte.parseByte(str);
	}

	@Override
	public String formatKey(final int key) {
		return String.format("%d", key);
	}

	@Override
	public int keyFromValue(final Object value) {
		return (value instanceof String) ? (int) Byte.parseByte((String) value) : (int) (Byte) value;
	}

	@Override
	public int maximumKeyValue() {
		return QualityUtils.MAXIMUM_USABLE_QUALITY_SCORE;
	}
}
