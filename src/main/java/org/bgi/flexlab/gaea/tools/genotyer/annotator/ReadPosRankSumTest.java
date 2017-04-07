package org.bgi.flexlab.gaea.tools.genotyer.annotator;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.bgi.flexlab.gaea.data.structure.alignment.AlignmentsBasic;
import org.bgi.flexlab.gaea.data.structure.pileup.Pileup;
import org.bgi.flexlab.gaea.data.structure.pileup.PileupReadInfo;
import org.bgi.flexlab.gaea.tools.genotyer.genotypeLikelihoodCalculator.PairHMMIndelErrorModel;
import org.bgi.flexlab.gaea.tools.genotyer.genotypeLikelihoodCalculator.PerReadAlleleLikelihoodMap;
import org.bgi.flexlab.gaea.tools.genotyer.annotator.interfaces.StandardAnnotation;
import org.bgi.flexlab.gaea.util.ReadUtils;
import org.bgi.flexlab.gaea.util.SystemConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * The u-based z-approximation from the Mann-Whitney Rank Sum Test for the distance from the end of the read for reads with the alternate allele; if the alternate allele is only seen near the ends of reads this is indicative of error).
 * Note that the read position rank sum test can not be calculated for sites without a mixture of reads showing both the reference and alternate alleles.
 */
public class ReadPosRankSumTest extends RankSumTest implements StandardAnnotation {

    public List<String> getKeyNames() {
        return Arrays.asList("ReadPosRankSum");
    }

    public List<VCFInfoHeaderLine> getDescriptions() {
        return Arrays.asList(new VCFInfoHeaderLine("ReadPosRankSum", 1, VCFHeaderLineType.Float, "Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias"));
    }

    protected void fillQualsFromPileup(final List<Allele> allAlleles,
                                       final int refLoc,
                                       final Pileup pileup,
                                       final PerReadAlleleLikelihoodMap alleleLikelihoodMap,
                                       final List<Double> refQuals, final List<Double> altQuals) {

        if (alleleLikelihoodMap == null) {
            // use old UG SNP-based version if we don't have per-read allele likelihoods
            for ( final PileupReadInfo p : pileup.getFilteredPileup() ) {
                if ( isUsableBase(p) ) {
                    int readPos = p.calcAlignmentByteArrayOffset(0, 0);

                    readPos = getFinalReadPosition(p.getReadInfo(),readPos);

                    if ( allAlleles.get(0).equals(Allele.create((byte) p.getBase(), true)) ) {
                        refQuals.add((double)readPos);
                    } else if ( allAlleles.contains(Allele.create((byte)p.getBase()))) {
                        altQuals.add((double)readPos);
                    }
                }
            }
            return;
        }
/**
 * not support for now
        for (Map.Entry<AlignmentsBasic,Map<Allele,Double>> el : alleleLikelihoodMap.getLikelihoodReadMap().entrySet()) {
            final AlignmentsBasic read = el.getKey();
            final int offset = ReadUtils.getReadCoordinateForReferenceCoordinate( read.getSoftStart(), read.getCigars(), refLoc, ReadUtils.ClippingTail.RIGHT_TAIL, true );
            if ( offset == ReadUtils.CLIPPING_GOAL_NOT_REACHED )
                continue;
            int readPos = .calcAlignmentByteArrayOffset( false, false, 0, 0 );
            final int numAlignedBases = AlignmentUtils.getNumAlignedBasesCountingSoftClips( read );
            if (readPos > numAlignedBases / 2)
                readPos = numAlignedBases - (readPos + 1);

//            int readPos = getOffsetFromClippedReadStart(el.getKey(), el.getKey().getOffset());
  //          readPos = getFinalReadPosition(el.getKey().getRead(),readPos);

            final Allele a = PerReadAlleleLikelihoodMap.getMostLikelyAllele(el.getValue());
            if (a.isNoCall())
                continue; // read is non-informative
            if (a.isReference())
                refQuals.add((double)readPos);
            else if (allAlleles.contains(a))
                altQuals.add((double)readPos);

        }
 */
    }

    int getFinalReadPosition(AlignmentsBasic read, int initialReadPosition) {
        final int numAlignedBases = getNumAlignedBases(read);

        int readPos = initialReadPosition;
        if (initialReadPosition > numAlignedBases / 2) {
            readPos = numAlignedBases - (initialReadPosition + 1);
        }
        return readPos;

    }
    int getNumClippedBasesAtStart(AlignmentsBasic read) {
        // compute total number of clipped bases (soft or hard clipped)
        // check for hard clips (never consider these bases):
        final int[] cigars = read.getCigars();
        final int first = cigars[0];
        int cigarOp = (first & SystemConfiguration.BAM_CIGAR_MASK);
        int cigarLength = first >> SystemConfiguration.BAM_CIGAR_SHIFT;

        int numStartClippedBases = 0;
        if (cigarOp == SystemConfiguration.BAM_CHARD_CLIP) {
            numStartClippedBases = cigarLength;
        }
        byte[] unclippedReadBases = read.getReadBases();
        byte[] unclippedReadQuals = read.getQualities();

        // Do a stricter base clipping than provided by CIGAR string, since this one may be too conservative,
        // and may leave a string of Q2 bases still hanging off the reads.
        for (int i = numStartClippedBases; i < unclippedReadBases.length; i++) {
            if (unclippedReadQuals[i] < PairHMMIndelErrorModel.BASE_QUAL_THRESHOLD)
                numStartClippedBases++;
            else
                break;

        }

        return numStartClippedBases;
    }

    int getNumAlignedBases(AlignmentsBasic read) {
        return read.getReadLength() - getNumClippedBasesAtStart(read) - getNumClippedBasesAtEnd(read);
    }

    int getNumClippedBasesAtEnd(AlignmentsBasic read) {
        // compute total number of clipped bases (soft or hard clipped)
        // check for hard clips (never consider these bases):
        final int[] cigars = read.getCigars();
        final int last = cigars[cigars.length - 1];
        int cigarOp = (last & SystemConfiguration.BAM_CIGAR_MASK);
        int cigarLength = last >> SystemConfiguration.BAM_CIGAR_SHIFT;

        int numEndClippedBases = 0;
        if (cigarOp == SystemConfiguration.BAM_CHARD_CLIP) {
            numEndClippedBases = cigarLength;
        }
        byte[] unclippedReadBases = read.getReadBases();
        byte[] unclippedReadQuals = read.getQualities();

        // Do a stricter base clipping than provided by CIGAR string, since this one may be too conservative,
        // and may leave a string of Q2 bases still hanging off the reads.
        for (int i = unclippedReadBases.length - numEndClippedBases - 1; i >= 0; i--) {
            if (unclippedReadQuals[i] < PairHMMIndelErrorModel.BASE_QUAL_THRESHOLD)
                numEndClippedBases++;
            else
                break;
        }


        return numEndClippedBases;
    }

    int getOffsetFromClippedReadStart(AlignmentsBasic read, int offset) {
        return offset - getNumClippedBasesAtStart(read);
    }
}
