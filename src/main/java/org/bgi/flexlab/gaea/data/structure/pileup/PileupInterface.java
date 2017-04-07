package org.bgi.flexlab.gaea.data.structure.pileup;

import org.bgi.flexlab.gaea.data.structure.alignment.AlignmentsBasic;
import org.bgi.flexlab.gaea.tools.mapreduce.genotyper.GenotyperOptions;

import java.util.ArrayList;

/**
 * Created by zhangyong on 2016/12/26.
 */
public interface PileupInterface<T extends PileupReadInfo> {
    void calculateBaseInfo(GenotyperOptions options);

    void remove();

    void forwardPosition(int size);

    boolean isEmpty();

    void addReads(AlignmentsBasic read);

}
