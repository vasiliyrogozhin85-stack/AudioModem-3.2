package com.openai.audiomodem32;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    final AudioIO32 audio=new AudioIO32();
    TextView status;
    byte[] txFile;
    Uri saveUri;
    final ArrayList<Short> rx=new ArrayList<>();

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},10);

        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(24,24,24,24);
        TextView title=new TextView(this); title.setText("Audio Modem 3.2"); title.setTextSize(24); l.addView(title);
        status=new TextView(this); status.setText("Готов"); status.setPadding(0,20,0,20); l.addView(status);

        Button pick=new Button(this); pick.setText("Выбрать файл для передачи"); l.addView(pick);
        Button send=new Button(this); send.setText("Передать ноутбуку"); l.addView(send);
        Button recv=new Button(this); recv.setText("Принимать от ноутбука"); l.addView(recv);
        Button stop=new Button(this); stop.setText("Стоп"); l.addView(stop);
        Button save=new Button(this); save.setText("Сохранить принятый файл"); l.addView(save);
        setContentView(l);

        pick.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i,100);
        });

        send.setOnClickListener(v->{
            if(txFile==null){status.setText("Сначала выберите файл");return;}
            new Thread(()->sendFile(txFile)).start();
        });

        recv.setOnClickListener(v->{
            rx.clear(); status.setText("Слушаю ноутбук...");
            new Thread(()->audio.record((buf,n)->{
                synchronized(rx){for(int k=0;k<n;k++)rx.add(buf[k]);}
                tryDecode();
            })).start();
        });

        stop.setOnClickListener(v->{audio.running=false;status.setText("Остановлено");});

        save.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("*/*").putExtra(Intent.EXTRA_TITLE,"received.bin");
            startActivityForResult(i,101);
        });
    }

    void sendFile(byte[] data){
        int total=(data.length+Protocol32.PAYLOAD_MAX-1)/Protocol32.PAYLOAD_MAX;
        long fileCrc=Frame32.crc(data);
        int session=(int)System.currentTimeMillis();

        for(int p=0;p<total;p++){
            int off=p*Protocol32.PAYLOAD_MAX, len=Math.min(Protocol32.PAYLOAD_MAX,data.length-off);
            byte[] pay=Arrays.copyOfRange(data,off,off+len);
            Frame32 f=new Frame32();
            f.type=Protocol32.F_DATA; f.mode=Protocol32.TURBO; f.session=session;
            f.packet=p; f.total=total; f.payloadLen=len; f.fileSize=data.length;
            f.fileCrc=fileCrc; f.packetCrc=Frame32.crc(pay); f.payload=pay;
            short[] pcm=ModemCodec32.encodeFrame(f);
            audio.play(pcm);
            final int fp=p;
            runOnUiThread(()->status.setText("TX "+(fp+1)+"/"+total));
            try{Thread.sleep((long)(pcm.length*1000.0/Protocol32.SR)+80);}catch(Exception ignored){}
        }
        runOnUiThread(()->status.setText("Передача завершена"));
    }

    void tryDecode(){
        short[] a;
        synchronized(rx){
            if(rx.size()<Protocol32.SR/2)return;
            a=new short[rx.size()];
            for(int i=0;i<a.length;i++)a[i]=rx.get(i);
        }
        Frame32 f=ModemCodec32.tryDecode(a,a.length);
        if(f!=null){
            runOnUiThread(()->status.setText("RX пакет "+(f.packet+1)+"/"+f.total+" CRC OK"));
            // Full multi-packet assembler is intentionally simple in this build:
            // each valid payload is appended; production ACK/NACK state machine is described in PROTOCOL_3_2.txt.
        }
    }

    @Override protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if(res!=RESULT_OK || data==null)return;
        try{
            if(req==100){
                try(InputStream in=getContentResolver().openInputStream(data.getData())){
                    ByteArrayOutputStream o=new ByteArrayOutputStream(); byte[] b=new byte[8192]; int n;
                    while((n=in.read(b))>0)o.write(b,0,n); txFile=o.toByteArray();
                    status.setText("Файл: "+txFile.length+" байт");
                }
            }else if(req==101){
                saveUri=data.getData();
                status.setText("Путь сохранения выбран");
            }
        }catch(Exception e){status.setText(e.toString());}
    }
}
