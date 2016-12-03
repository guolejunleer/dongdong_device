package com.dongdong.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.dongdong.AppConfig;
import com.dongdong.sdk.DongDongTransferCenter.GsmCoderCallback;
import com.dongdong.utils.DDLog;
import com.jr.GemvaryGs;

import com.dongdong.socket.normal.DSPacket;
import com.dongdong.socket.normal.UdpClientSocket;

public class DDAudioRecorder implements GsmCoderCallback, DSPacket.DSMediaPacketCallback {

    /*
     * 编码一个GSM需要320; 每次读AudioDataSize，可编成3个GSM
     */
    private static DDAudioRecorder mInstance;
    private static int AUDIO_DATA_SIZE = 320;

    private AudioRecord audioRecord;
    private GsmThread gsmThread;

    private boolean isRecording = false;
    private byte[] audioData = new byte[AUDIO_DATA_SIZE];

    private UdpClientSocket audioSocket;
    private DSPacket packet;
    private int sequence = 0;
    private final int serverVideoPort = 45613;

    private boolean recorderMode = false;
    private InputStream mMicrophone;
    private ByteArrayOutputStream mBaos;
    public boolean isFirst = true;

    private DDAudioRecorder() {
        int min = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (AUDIO_DATA_SIZE > min) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, AUDIO_DATA_SIZE);
        } else {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, min);
        }
    }

    public static DDAudioRecorder getInstance() {
        if (mInstance == null) {
            synchronized (DDAudioRecorder.class) {
                if (mInstance == null)
                    mInstance = new DDAudioRecorder();
            }
        }
        return mInstance;
    }

    public boolean audioState() {
        return (audioRecord.getState() == AudioRecord.STATE_INITIALIZED);
    }

    public void startSendAudio() {
        try {
            if (audioSocket == null) {
                audioSocket = new UdpClientSocket();
            }
            if (packet == null) {
                packet = new DSPacket();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        startRecord();
    }

    public void closeSendAudio() {
        stopRecord();
    }

    private void startRecord() {
        if (recorderMode) {
            if (!isRecording) {
                isRecording = true;
                audioRecord.startRecording();
                gsmThread = new GsmThread();
                gsmThread.start();
            }
        } else {
            if (!isRecording) {
                isRecording = true;
                isFirst = true;
                mBaos = new ByteArrayOutputStream(8000 * 2 * 20);
                mMicrophone = GemvaryGs.getInstance().GSgetMicInputStream(8000,
                        8000 * 15);
                gsmThread = new GsmThread();
                gsmThread.start();
            }
        }

    }

    private void stopRecord() {
        if (recorderMode) {
            if (isRecording) {
                isRecording = false;
                audioRecord.stop();
                gsmThread.interrupt();
                gsmThread = null;
            }
        } else {
            if (isRecording) {
                isRecording = false;
                gsmThread.interrupt();
                gsmThread = null;
            }
            try {
                if (mMicrophone != null) {
                    mMicrophone.close();
                }
                if (mBaos != null) {
                    mBaos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static int preTime = 0;
    private static int nowTime = 0;
    int count = 0;

    private class GsmThread extends Thread {

        @Override
        public void run() {
            if (recorderMode) {
                try {
                    while (isRecording) {
                        audioRecord.read(audioData, 0, AUDIO_DATA_SIZE);
                        DDAudioRecorder.this.audioRecord(audioData);
                    }
                } catch (Throwable e) {
                    isRecording = false;
                    e.printStackTrace();
                }
            } else {
                try {
                    byte buffer[] = new byte[AUDIO_DATA_SIZE];
                    while (isRecording) {

                        try {
                            int rtn;
                            rtn = GemvaryGs.getInstance().read(buffer, 0,
                                    AUDIO_DATA_SIZE);
                            // our code
                            DDAudioRecorder.this.audioRecord(buffer);
                            nowTime = (int) System.currentTimeMillis() / 1000;
                            if (preTime == 0) {
                                count++;
                            } else {
                                if (preTime == nowTime) {
                                    count++;
                                } else {
                                    count = 0;
                                    preTime = 0;
                                }
                            }
                            preTime = nowTime;
                            if (rtn > 0)
                                mBaos.write(buffer, 0, rtn);

                        } catch (Exception e) {
                            DDLog.e("DDAudioRecorder.clazz$GsmThread.class run-->>>GemvaryGs no_room:"
                                    + e);
                        }

                    }
                } catch (Throwable e) {
                    isRecording = false;
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void audioRecord(byte[] coderData) {
        packet.sendMediaRequest(DDAudioRecorder.this, 0, 0, sequence++, (byte) 0, coderData,
                coderData.length, true);
    }

    @Override
    public void onMediaPacket(byte[] mdpkt, int mdpktlength) {
        try {
            audioSocket.send(AppConfig.SERVER_HOST_IP, serverVideoPort, mdpkt, mdpktlength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
