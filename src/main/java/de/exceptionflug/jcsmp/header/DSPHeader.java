package de.exceptionflug.jcsmp.header;

import java.util.Arrays;

public class DSPHeader {

    final long sampleCount;
    final long nibbleCount;
    final long sampleRate;
    final boolean looped;
    final int format;
    final long loopStart;
    final long loopEnd;
    final long zero;
    final short[][] coefficients;
    final int gain;
    final int ps;
    final short hist1;
    final short hist2;
    final int loopContextPS;
    final int loopContextSampleHist1;
    final int loopContextSampleHist2;
    final int[] padding;

    public DSPHeader(long sampleCount, long nibbleCount, long sampleRate, boolean looped, int format, long loopStart, long loopEnd, long zero, short[][] coefficients, int gain, int ps, short hist1, short hist2, int loopContextPS, int loopContextSampleHist1, int loopContextSampleHist2, int[] padding) {
        this.sampleCount = sampleCount;
        this.nibbleCount = nibbleCount;
        this.sampleRate = sampleRate;
        this.looped = looped;
        this.format = format;
        this.loopStart = loopStart;
        this.loopEnd = loopEnd;
        this.zero = zero;
        this.coefficients = coefficients;
        this.gain = gain;
        this.ps = ps;
        this.hist1 = hist1;
        this.hist2 = hist2;
        this.loopContextPS = loopContextPS;
        this.loopContextSampleHist1 = loopContextSampleHist1;
        this.loopContextSampleHist2 = loopContextSampleHist2;
        this.padding = padding;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public long getNibbleCount() {
        return nibbleCount;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public boolean isLooped() {
        return looped;
    }

    public int getFormat() {
        return format;
    }

    public long getLoopStart() {
        return loopStart;
    }

    public long getLoopEnd() {
        return loopEnd;
    }

    public long getZero() {
        return zero;
    }

    public short[][] getCoefficients() {
        return coefficients;
    }

    public int getGain() {
        return gain;
    }

    public int getPs() {
        return ps;
    }

    public short getHist1() {
        return hist1;
    }

    public short getHist2() {
        return hist2;
    }

    public int getLoopContextPS() {
        return loopContextPS;
    }

    public int getLoopContextSampleHist1() {
        return loopContextSampleHist1;
    }

    public int getLoopContextSampleHist2() {
        return loopContextSampleHist2;
    }

    public int[] getPadding() {
        return padding;
    }

    @Override
    public String toString() {
        return "DSPHeader{" +
                "sampleCount=" + sampleCount +
                ", nibbleCount=" + nibbleCount +
                ", sampleRate=" + sampleRate +
                ", looped=" + looped +
                ", format=" + format +
                ", loopStart=" + loopStart +
                ", loopEnd=" + loopEnd +
                ", zero=" + zero +
                ", coefficients=" + Arrays.toString(Arrays.stream(coefficients).map(Arrays::toString).toArray()) +
                ", gain=" + gain +
                ", ps=" + ps +
                ", hist1=" + hist1 +
                ", hist2=" + hist2 +
                ", loopContextPS=" + loopContextPS +
                ", loopContextSampleHist1=" + loopContextSampleHist1 +
                ", loopContextSampleHist2=" + loopContextSampleHist2 +
                ", padding=" + Arrays.toString(padding) +
                '}';
    }
}
