package com.openai.audiomodem32;

import android.media.*;

public final class AudioIO32 {
    public volatile boolean running;

    public void play(short[] pcm){
        int min=AudioTrack.getMinBufferSize(Protocol32.SR,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack t=new AudioTrack(AudioManager.STREAM_MUSIC,Protocol32.SR,AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,Math.max(min,pcm.length*2),AudioTrack.MODE_STATIC);
        t.write(pcm,0,pcm.length); t.play();
    }

    public void record(Chunk sink){
        int min=AudioRecord.getMinBufferSize(Protocol32.SR,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        int size=Math.max(min,8192);
        AudioRecord r;
        try{
            r=new AudioRecord(MediaRecorder.AudioSource.UNPROCESSED,Protocol32.SR,AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,size);
        }catch(Exception e){
            r=new AudioRecord(MediaRecorder.AudioSource.MIC,Protocol32.SR,AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,size);
        }
        short[] b=new short[size/2];
        running=true; r.startRecording();
        try{
            while(running){
                int n=r.read(b,0,b.length);
                if(n>0)sink.accept(b,n);
            }
        }finally{
            try{r.stop();}catch(Exception ignored){}
            r.release(); running=false;
        }
    }

    public interface Chunk{ void accept(short[] b,int n); }
}
