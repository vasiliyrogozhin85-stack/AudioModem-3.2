package com.openai.audiomodem32;

public final class Protocol32 {
    public static final int SR=48000, USEFUL=80, CP=16, SYM=96;
    public static final int TOTAL_CARRIERS=16, DATA_CARRIERS=14, BYTES_PER_SYM=7;
    public static final int PAYLOAD_MAX=1024, HEADER_BYTES=36;
    public static final int PREAMBLE=8, SYNC=4;
    public static final int F_TRAIN=1,F_CAPS=2,F_DATA=3,F_ACK=4,F_NACK=5,F_END=6,F_CANCEL=7;
    public static final int SAFE=0,NORMAL=1,FAST=2,TURBO=3;

    public static double freq(int c){ return 1800.0+c*600.0; }
    public static boolean pilot(int c){ return c==3 || c==12; }
    public static int dataCarrier(int d){ return d<=2?d:(d<=10?d+1:d+2); }

    public static int chooseMode(double snr,double per){
        if(per>0.12) return SAFE;
        if(snr>=28 && per<0.02) return TURBO;
        if(snr>=21 && per<0.05) return FAST;
        if(snr>=14 && per<0.08) return NORMAL;
        return SAFE;
    }
}
