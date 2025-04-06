package de.exceptionflug.jcsmp.header;

public class RASHeader {

    private final long channelCount1;
    private final long channelCount2;
    private final long sampleCount;
    private final long metaDataFlag;
    private final long sampleRate;
    private final long dataOffset;
    private final long dataSize;
    private final long blockSize;
    private final long blockCount;
    private final long startSample;
    private final long lastSampleOfLastBlock;
    private final long loopStartBlock;
    private final long loopStartSample;
    private final long loopEndBlock;
    private final long loopEndSample;

    public RASHeader(long channelCount1, long channelCount2, long sampleCount, long metaDataFlag, long sampleRate, long dataOffset, long dataSize, long blockSize, long blockCount, long startSample, long lastSampleOfLastBlock, long loopStartBlock, long loopStartSample, long loopEndBlock, long loopEndSample) {
        this.channelCount1 = channelCount1;
        this.channelCount2 = channelCount2;
        this.sampleCount = sampleCount;
        this.metaDataFlag = metaDataFlag;
        this.sampleRate = sampleRate;
        this.dataOffset = dataOffset;
        this.dataSize = dataSize;
        this.blockSize = blockSize;
        this.blockCount = blockCount;
        this.startSample = startSample;
        this.lastSampleOfLastBlock = lastSampleOfLastBlock;
        this.loopStartBlock = loopStartBlock;
        this.loopStartSample = loopStartSample;
        this.loopEndBlock = loopEndBlock;
        this.loopEndSample = loopEndSample;
    }

    public long getChannelCount1() {
        return channelCount1;
    }

    public long getChannelCount2() {
        return channelCount2;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public long getMetaDataFlag() {
        return metaDataFlag;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public long getBlockCount() {
        return blockCount;
    }

    public long getStartSample() {
        return startSample;
    }

    public long getLastSampleOfLastBlock() {
        return lastSampleOfLastBlock;
    }

    public long getLoopStartBlock() {
        return loopStartBlock;
    }

    public long getLoopStartSample() {
        return loopStartSample;
    }

    public long getLoopEndBlock() {
        return loopEndBlock;
    }

    public long getLoopEndSample() {
        return loopEndSample;
    }
}
