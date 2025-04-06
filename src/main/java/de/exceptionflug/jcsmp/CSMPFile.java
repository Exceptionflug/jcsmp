package de.exceptionflug.jcsmp;

import de.exceptionflug.jcsmp.header.DSPHeader;
import de.exceptionflug.jcsmp.vox.Vox;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.sound.sampled.*;
import java.io.*;

public class CSMPFile {

    private static final byte[] NIBBLE_TO_S8 = {0, 1, 2, 3, 4, 5, 6, 7, -8, -7, -6, -5, -4, -3, -2, -1};
    private static final int VOLUME = 11;
    private final Mode mode;
    private final File file;

    private CSMPFile(final Mode mode, final File file) {
        this.mode = mode;
        this.file = file;
    }

    public static CSMPFile readFile(final File file) throws IOException, LineUnavailableException {
        final byte[] fileContent = new FileInputStream(file).readAllBytes();
        ByteBuf buffer = Unpooled.wrappedBuffer(fileContent);
        final byte[] headerNibbleOne = new byte[4];
        buffer.readBytes(headerNibbleOne);
        String formatName = new String(headerNibbleOne);
        final CSMPFile out;
        if (formatName.equals("CSMP")) {
            System.out.println("File type is: CSMP (Donkey Kong Country: Returns / Metroid Prime 3)");
            out = new CSMPFile(Mode.DKCR, file);
        } else if (formatName.equals("RFRM")) {
            out = new CSMPFile(Mode.DKCTF, file);
            System.out.println("File type is: RFRM (Donkey Kong Country: Tropical Freeze)");
        } else {
            throw new UnsupportedEncodingException("Unknown format: " + formatName);
        }
        if (out.mode == Mode.DKCTF) {
            buffer.skipBytes(16); // Useless non-documented DKCTF specific header information
            byte[] headerNibbleName = new byte[4];
            buffer.readBytes(headerNibbleName);
            formatName = new String(headerNibbleName);
            if (formatName.equals("CSMP")) {
                buffer.skipBytes(8);
                int channelCount = 0;
                long fmtAlign = 0;
                while (true) {
                    headerNibbleName = new byte[4];
                    if (buffer.readableBytes() < 4) {
                        System.out.println("EOF reached as expected");
                        break;
                    }
                    buffer.readBytes(headerNibbleName);
                    final String sectionName = new String(headerNibbleName);
                    buffer.skipBytes(4);
                    try {
                        final long size = buffer.readUnsignedInt();
                        buffer.skipBytes(12); // Sometimes only 8????
                        System.out.println("Section name is: " + sectionName + " (size: "+size+")");
                        if (sectionName.equals("FMTA")) {
                            channelCount = buffer.readUnsignedByte();
                            fmtAlign = buffer.readUnsignedInt();
                            System.out.println("We have " + channelCount + " channels, fmtAlign = " + fmtAlign);
                            buffer.skipBytes((int) (size - 5));
                        } else if (sectionName.equals("DATA")) {
                            buffer.skipBytes((int) fmtAlign);
                            if (channelCount == 1) {
                                System.out.println("This is DSPADPCMCSMP mono! Lets see...");
                                final long offset = buffer.readerIndex() + (size - fmtAlign);
                                final ByteBuf buffer1 = buffer.copy(buffer.readerIndex(), (int) offset - 120); // -120 to cut some gibberish at the end
                                System.out.println("--- Channel 1 ---");
                                try (final FileOutputStream fos = new FileOutputStream("channel0.dsp")) {
                                    fos.write(buffer1.array());
                                    fos.flush();
                                }
                                final DSPHeader header1 = readDefaultDSPHeader(buffer1);
                                byte[] dspData1 = new byte[buffer1.readableBytes()];
                                buffer1.readBytes(dspData1);
                                byte[] intro1 = decodePlayable(dspData1, header1, 0, (int) header1.getLoopStart());
                                byte[] loop1 = decodePlayable(dspData1, header1, (int) header1.getLoopStart(), (int) header1.getLoopEnd());
                                System.out.println("");
                                System.out.println("Decoding done. Playing back PCM data...");
                                playBackMono(intro1, loop1, header1.getSampleRate());
                            } else if (channelCount == 2) {
                                System.out.println("So we have stereo DSPADPCMCSMP here... lets see...");
                                final long offset = buffer.readerIndex() + (size - fmtAlign) / 2;
                                final ByteBuf buffer1 = buffer.copy(buffer.readerIndex(), (int) offset - 120); // -120 to cut some gibberish at the end
                                System.out.println("--- Channel 1 ---");
                                final DSPHeader header1 = readDefaultDSPHeader(buffer1);
                                buffer.resetReaderIndex();
                                final ByteBuf buffer2 = buffer.copy((int) offset, (int) (buffer.readableBytes() - offset));
                                System.out.println("--- Channel 2 ---");
                                final DSPHeader header2 = readDefaultDSPHeader(buffer2);

                                byte[] dspData1 = new byte[buffer1.readableBytes()];
                                buffer1.readBytes(dspData1);
                                byte[] dspData2 = new byte[buffer2.readableBytes()];
                                buffer2.readBytes(dspData2);

                                byte[] intro1 = decodePlayable(dspData1, header1, 0, (int) header1.getLoopStart());
                                byte[] loop1 = decodePlayable(dspData1, header1, (int) header1.getLoopStart(), (int) header1.getLoopEnd());

                                byte[] intro2 = decodePlayable(dspData2, header2, 0, (int) header2.getLoopStart());
                                byte[] loop2 = decodePlayable(dspData2, header2, (int) header2.getLoopStart(), (int) header2.getLoopEnd());

                                System.out.println("");
                                System.out.println("Decoding done. Playing back PCM data...");

                                byte[] intro = combineData(intro1, intro2);
                                byte[] loopBody = combineData(loop1, loop2);
                                playBackStereo(intro, loopBody, header1.getSampleRate());

                                break;
                            } else {
                                if (channelCount == 4) {
                                    System.out.println("So we have two sets of stereo DSPADPCMCSMP channels here... lets see...");
                                    final long offset = buffer.readerIndex() + (size - fmtAlign) / 4;
                                    final ByteBuf buffer1 = buffer.copy(buffer.readerIndex(), (int) offset - 120); // -120 to cut some gibberish at the end
                                    System.out.println("--- Channel 1 ---");
                                    final DSPHeader header1 = readDefaultDSPHeader(buffer1);
                                    buffer.resetReaderIndex();
                                    final ByteBuf buffer2 = buffer.copy((int) offset, (int) (buffer.readableBytes() - (offset * 2)));
                                    try (final FileOutputStream fos = new FileOutputStream("channel0.dsp")) {
                                        fos.write(buffer1.array());
                                        fos.flush();
                                    }
                                    try (final FileOutputStream fos = new FileOutputStream("channel1.dsp")) {
                                        fos.write(buffer2.array());
                                        fos.flush();
                                    }
                                    System.out.println("--- Channel 2 ---");
                                    final DSPHeader header2 = readDefaultDSPHeader(buffer2);

                                    byte[] dspData1 = new byte[buffer1.readableBytes()];
                                    buffer1.readBytes(dspData1);
                                    byte[] dspData2 = new byte[buffer2.readableBytes()];
                                    buffer2.readBytes(dspData2);

                                    byte[] intro1 = decodePlayable(dspData1, header1, 0, (int) header1.getLoopStart());
                                    byte[] loop1 = decodePlayable(dspData1, header1, (int) header1.getLoopStart(), (int) header1.getLoopEnd());

                                    byte[] intro2 = decodePlayable(dspData2, header2, 0, (int) header2.getLoopStart());
                                    byte[] loop2 = decodePlayable(dspData2, header2, (int) header2.getLoopStart(), (int) header2.getLoopEnd());

                                    System.out.println("");
                                    System.out.println("Decoding done. Playing back PCM data...");

                                    byte[] intro = combineData(intro1, intro2);
                                    byte[] loopBody = combineData(loop1, loop2);
                                    playBackStereo(intro, loopBody, header1.getSampleRate());
                                    break;
                                }
                                throw new UnsupportedEncodingException("DSPADPCMCSMP with " + channelCount + " channels is not supported yet.");
                            }
                        } else {
                            if (size < 0) {
                                System.out.println("Strange size: " + size);
                                continue;
                            }
                            buffer.skipBytes((int) size);
                        }
                    } catch (final IllegalArgumentException e) {
                        System.out.println("EOF unexpected");
                        e.printStackTrace();
                        break;
                    }
                }
            } else {
                throw new UnsupportedEncodingException("RFRM container type cannot be " + formatName);
            }
        }
        return out;
    }

    private static byte[] decodePlayable(byte[] dspData, DSPHeader header, int start, int end) {
        final short[] decoded;
        if (header.getSampleRate() > 32000) {
            decoded = decodeVox(dspData, header);
        } else {
            decoded = decodeADPCM(dspData, header, start, end);
        }
        final byte[] bytes = new byte[decoded.length * 2];
        for (int i = 0; i < decoded.length; i++) {
            bytes[i * 2] = (byte) (decoded[i] & 0xFF);
            bytes[i * 2 + 1] = (byte) ((decoded[i] >> 8) & 0xFF);
        }
        return bytes;
    }

    private static short[] decodeVox(byte[] dspData, DSPHeader header) {
        System.out.println(header);
        int samplesCount = dspData.length * 2;  // 1 Byte = 2 Samples
        short[] pcmData = new short[samplesCount];

        int index = 0;
        int predictor = 0;
        int stepIndex = 0;

        int[] stepTable = {
                7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31,
                34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143,
                157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658,
                724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024,
                3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
                15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
        };

        int[] indexTable = { -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8 };

        for (byte b : dspData) {
            for (int i = 0; i < 2; i++) {
                int nibble = (i == 0) ? ((b >> 4) & 0x0F) : (b & 0x0F);
                int stepSize = stepTable[stepIndex];

                int diff = stepSize >> 3;
                if ((nibble & 1) != 0) diff += stepSize >> 2;
                if ((nibble & 2) != 0) diff += stepSize >> 1;
                if ((nibble & 4) != 0) diff += stepSize;

                if ((nibble & 8) != 0) {
                    predictor -= diff;
                } else {
                    predictor += diff;
                }

                predictor = Math.max(-32768, Math.min(32767, predictor));
                pcmData[index++] = (short) predictor;

                stepIndex += indexTable[nibble];
                stepIndex = Math.max(0, Math.min(stepTable.length - 1, stepIndex));
            }
        }

        return pcmData;
    }

    private static void playBackStereo(byte[] intro, byte[] loopBody, long sampleRate) {
        final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 1500);
        try {
            final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            // Play Intro
            line.write(intro, 0, intro.length);

            while (!Thread.interrupted()) {
                System.out.println("LOOP START");
                line.write(loopBody, 0, loopBody.length);
                System.out.println("LOOP END");
            }

            line.flush();

            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void playBackMono(byte[] intro, byte[] loopBody, long sampleRate) {
        final AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 16, 1, 2, 32000, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 1500);
        try {
            final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            // Play Intro
            line.write(intro, 0, intro.length);

            while (!Thread.interrupted()) {
                System.out.println("LOOP START");
                line.write(loopBody, 0, loopBody.length);
                System.out.println("LOOP END");
            }

            line.flush();

            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static byte[] combineData(byte[] channel1, byte[] channel2) {
        byte[] stereoBytes = new byte[channel1.length + channel2.length];

        for (int i = 0, j = 0; i < channel1.length; i += 2, j += 4) {
            // Channel 1
            stereoBytes[j] = channel1[i];
            stereoBytes[j + 1] = channel1[i + 1];

            // Channel 2
            stereoBytes[j + 2] = channel2[i];
            stereoBytes[j + 3] = channel2[i + 1];
        }
        return stereoBytes;
    }

    private static int nibbleidxToSampleidx(int nibbleidx) {
        int frame = nibbleidx / 16;
        int frameSamp = Math.max(2, nibbleidx % 16) - 2;
        return frame * 14 + frameSamp;
    }

    private static short[] decodeADPCM(byte[] src, DSPHeader dspHeader, int start, int end) {
        short hist1 = dspHeader.getHist1();
        short hist2 = dspHeader.getHist2();
        short[] dst = new short[nibbleidxToSampleidx(end) - nibbleidxToSampleidx(start)];
        int dstIndex = 0;
        int srcIndex = 0;
        int sampleIdx = 0;
        int startSampleIdx = nibbleidxToSampleidx(start);
        int endSampleIdx = nibbleidxToSampleidx(end);
        if (startSampleIdx == endSampleIdx) {
            return dst;
        }

        while (dstIndex < dspHeader.getSampleCount()) {
            if (srcIndex >= src.length) break;
            byte header = src[srcIndex++];

            int scale = 1 << (header & 0xF);
            int coefIndex = (header & 0xFF) >> 4;

            if (coefIndex >= dspHeader.getCoefficients().length) {
                throw new ArrayIndexOutOfBoundsException("Invalid coefIndex: " + coefIndex + " at srcIndex: " + srcIndex + " dstIndex: " + dstIndex + " / " + dst.length);
            }

            short coef1 = dspHeader.getCoefficients()[coefIndex][0];
            short coef2 = dspHeader.getCoefficients()[coefIndex][1];

            for (int b = 0; b < 7; b++) {
                if (srcIndex >= src.length) break;
                byte dataByte = src[srcIndex++];

                for (int s = 0; s < 2; s++) {
                    sampleIdx++;
                    byte adpcmNibble = (byte) ((s == 0) ? ((dataByte >> 4) & 0x0F) : (dataByte & 0x0F));

                    // Sign-Korrektur für 4-Bit ADPCM
                    if (adpcmNibble >= 8) adpcmNibble -= 16;

                    int sample = (((adpcmNibble * scale) << VOLUME) + 1024 + ((coef1 * hist1) + (coef2 * hist2))) >> 11;
                    sample = clamp(sample);

                    hist2 = hist1;
                    hist1 = (short) sample;
                    if (dstIndex >= dst.length) dstIndex = dst.length -1;
                    if (sampleIdx >= startSampleIdx) {
                        dst[dstIndex++] = (short) sample;
                    }

                    if (dstIndex >= dspHeader.getSampleCount() || sampleIdx >= endSampleIdx) break;
                }
            }
        }
        return dst;
    }

    private static short clamp(int val) {
        if (val < -32768) return -32768;
        if (val > 32767) return 32767;
        return (short) val;
    }

    private static DSPHeader readDefaultDSPHeader(final ByteBuf buffer) throws IOException {
        final long sampleCount = buffer.readUnsignedInt();
        final long nibbleCount = buffer.readUnsignedInt();
        final long sampleRate = buffer.readUnsignedInt();
        final boolean looped = buffer.readUnsignedShort() == 1;
        final int format = buffer.readUnsignedShort();
        if (format != 0) {
            throw new UnsupportedEncodingException("No other format then ADPCM allowed");
        }
        final long loopStart = buffer.readUnsignedInt();
        final long loopEnd = buffer.readUnsignedInt();
        final long zero = buffer.readUnsignedInt();

        final short[][] coefficients = new short[8][2];
        for (int i = 0; i < 8; i++) {
            coefficients[i][0] = buffer.readShort();
            coefficients[i][1] = buffer.readShort();
        }

        final int gain = buffer.readUnsignedShort();
        final int ps = buffer.readUnsignedShort();
        final short hist1 = buffer.readShort();
        final short hist2 = buffer.readShort();
        final int loopContextPS = buffer.readUnsignedShort();
        final int loopContextSampleHist1 = buffer.readShort();
        final int loopContextSampleHist2 = buffer.readShort();

        final int[] padding = new int[11];
        for (int i = 0; i < 11; i++) {
            padding[i] = buffer.readUnsignedShort();
        }
        System.out.println("Sample rate: " + sampleRate);
        System.out.println("Sample count: " + sampleCount);
        System.out.println("Loop start: " + loopStart);
        System.out.println("Loop end: " + loopEnd);
        return new DSPHeader(sampleCount, nibbleCount, sampleRate, looped, format, loopStart, loopEnd, zero, coefficients, gain, ps, hist1, hist2, loopContextPS, loopContextSampleHist1, loopContextSampleHist2, padding);
    }

}
