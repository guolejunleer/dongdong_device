package com.dongdong.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.dongdong.AppConfig;

import java.io.IOException;

import com.dongdong.socket.normal.DSPacket;
import com.dongdong.socket.normal.UdpClientSocket;

public class AudioPlay {
    private AudioTrack mAudioTrack = null;
    private DSPacket packet = null;
    //byte[] audioData = new byte[320];
    private UdpClientSocket audioSocket = null;

    private int serverVideoPort = 45613;

    public AudioPlay() {
        if (packet == null) {
            packet = new DSPacket();
        }
        if (audioSocket == null) {
            try {
                audioSocket = new UdpClientSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, AudioTrack.getMinBufferSize(
                8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);
    }

    public void openSound() {
        mAudioTrack.play();
    }

    public void closeSound() {
        mAudioTrack.stop();
    }

    public void OnAudioData(int cmdflag, int sequence, byte[] mediadata) {
        mAudioTrack.write(mediadata, 0, mediadata.length);
        byte[] audioByte = packet.sendMediaResult(cmdflag, sequence);
        try {
            audioSocket.send(AppConfig.SERVER_HOST_IP, serverVideoPort, audioByte,
                    audioByte.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
