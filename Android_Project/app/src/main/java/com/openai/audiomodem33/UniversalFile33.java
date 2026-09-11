package com.openai.audiomodem33;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public final class UniversalFile33 {
    public static final class Info {
        public final String name;
        public final int size, totalPackets;
        public final long crc;
        public Info(String n, int s, long c, int t){name=n;size=s;crc=c;totalPackets=t;}
    }

    public static Info inspect(File f) throws IOException {
        long len = f.length();
        if (len > Integer.MAX_VALUE) throw new IOException("3.3 supports files up to 2 GB");
        CRC32 crc = new CRC32();
        try(InputStream in = new BufferedInputStream(new FileInputStream(f))) {
            byte[] b = new byte[32768]; int n;
            while((n=in.read(b))>0) crc.update(b,0,n);
        }
        int total = len == 0 ? 0 : (int)((len + Protocol33.PAYLOAD_MAX - 1) / Protocol33.PAYLOAD_MAX);
        return new Info(f.getName(), (int)len, crc.getValue(), total);
    }

    public static byte[] makeInfoPayload(Info i) {
        byte[] name = i.name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer b = ByteBuffer.allocate(6 + name.length + 12).order(ByteOrder.LITTLE_ENDIAN);
        b.put((byte)'F').put((byte)'I').put((byte)'3').put((byte)'3');
        b.putShort((short)name.length).put(name).putInt(i.size).putInt((int)i.crc).putInt(i.totalPackets);
        return b.array();
    }

    public static byte[] readPacket(File f, int packetNo) throws IOException {
        long off = (long)packetNo * Protocol33.PAYLOAD_MAX;
        int take = (int)Math.min(Protocol33.PAYLOAD_MAX, f.length() - off);
        if (take <= 0) throw new EOFException();
        byte[] out = new byte[take];
        try(RandomAccessFile r = new RandomAccessFile(f,"r")) {
            r.seek(off); r.readFully(out);
        }
        return out;
    }

    public static void writePacket(File f, int packetNo, byte[] payload) throws IOException {
        try(RandomAccessFile r = new RandomAccessFile(f,"rw")) {
            r.seek((long)packetNo * Protocol33.PAYLOAD_MAX);
            r.write(payload);
        }
    }
}
