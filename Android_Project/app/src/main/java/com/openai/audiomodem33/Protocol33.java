package com.openai.audiomodem33;

public final class Protocol33 {
    private Protocol33() {}
    public static final int PAYLOAD_MAX = 1024;
    public static final int HEADER_BYTES = 40;
    public static final int FT_FILEINFO = 10;
    public static final int FT_DATA = 11;
    public static final int FT_ACK = 12;
    public static final int FT_NACK = 13;
    public static final int FT_END = 14;
    public static final int FT_CANCEL = 15;
}
