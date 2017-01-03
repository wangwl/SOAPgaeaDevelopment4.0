package org.bgi.flexlab.gaea.tools.recalibrator.table;

import org.bgi.flexlab.gaea.data.mapreduce.util.HdfsFilesReader;
import org.bgi.flexlab.gaea.tools.mapreduce.realigner.RecalibratorOptions;
import org.bgi.flexlab.gaea.tools.recalibrator.RecalibratorDatum;
import org.bgi.flexlab.gaea.tools.recalibrator.covariate.Covariate;
import org.bgi.flexlab.gaea.tools.recalibrator.covariate.CovariateUtil;
import org.bgi.flexlab.gaea.util.GaeaFilesReader;
import org.bgi.flexlab.gaea.util.NestedObjectArray;

import htsjdk.samtools.SAMFileHeader;

public class RecalibratorTableCombiner {
	private GaeaFilesReader reader = null;
	private RecalibratorTable tables = null;
	private Covariate[] covariates = null;

	public RecalibratorTableCombiner(RecalibratorOptions option, SAMFileHeader header) {
		covariates = CovariateUtil.initializeCovariates(option, header);
		tables = new RecalibratorTable(covariates, header.getReadGroups().size());
	}

	public void combineTable(String path) {
		reader = new HdfsFilesReader();
		reader.traversal(path);

		String line = null;
		if (reader.hasNext()) {
			line = reader.next();
			tableLineParser(line.split("\t"));
		}
		
		reader.clear();
	}

	private void tableLineParser(String[] array) {
		int[] keys = null;
		int length = 2;
		int index = Integer.parseInt(array[0]);
		if (index <= 2)
			length += index;
		else
			length = 4;
		keys = new int[length];

		int i;
		for (i = 0; i < length; i++)
			keys[i] = Integer.parseInt(array[i + 1]);

		RecalibratorDatum datum = new RecalibratorDatum(Long.parseLong(array[length + 1]),
				Long.parseLong(array[length + 2]), Double.parseDouble(array[length + 3]));
		
		updateTable(index,keys,datum);
	}
	
	public Covariate[] getCovariates(){
		return this.covariates;
	}

	private void updateTable(int index, int[] keys, RecalibratorDatum datum) {
		final NestedObjectArray<RecalibratorDatum> table = tables.getTable(index);
		final RecalibratorDatum currDatum = table.get(keys);
		if (currDatum == null)
			table.put(datum, keys[0], keys[1]);
		else {
			if (index == 0)
				currDatum.combine(datum);
			else
				currDatum.increment(datum);
		}
	}
	
	public RecalibratorTable getRecalibratorTable(){
		return this.tables;
	}
}