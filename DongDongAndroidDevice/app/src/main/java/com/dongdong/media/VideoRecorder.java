package com.dongdong.media;

import java.io.IOException;
import java.io.InputStream;

import com.dongdong.AppConfig;
import com.dongdong.socket.beat.Packet;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.dongdong.socket.normal.DSPacket;
import com.dongdong.socket.normal.UdpClientSocket;

public class VideoRecorder implements SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener,
        Packet.DSMediaPacketCallback {
    protected static final int rtphl = 0;
    private static final int mVideoEncoder = MediaRecorder.VideoEncoder.H264;
    private static final String TAG = "VideoCamera";
    private LocalSocket receiver, sender;
    private LocalServerSocket lss;
    private MediaRecorder mMediaRecorder = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Thread t;
    private UdpClientSocket videoSocket = null;
    private Camera mCamera;

    /**
     * h264 packet info
     */
    private byte[] h264Buffer = new byte[1024 * 1024];
    private int h264Bufferoffset = 0;
    private int gop = -1;
    private int sequence = 0;
    private DSPacket packet = null;
    private int serverVideoPort = 45612;
    private byte[] h264head = {0, 0, 0, 1};
    private byte[] h264sps = {0x67, 0x42, 0x00, 0x1f, (byte) 0xe5, 0x40, 0x50,
            0x1e, (byte) 0xc8};
    private byte[] h264pps = {0x68, (byte) 0xce, 0x31, 0x12};
    public static final int MTU = 640 * 480 * 3 / 2;
    private byte[] buffer = new byte[MTU];
    private InputStream is;
    public boolean sendCode = false;
    SurfaceView mSurfaceView = null;

    public VideoRecorder(SurfaceView mSurfaceView) {

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        this.mSurfaceView = mSurfaceView;
        receiver = new LocalSocket();
        try {
            lss = new LocalServerSocket("VideoCamera");
            receiver.connect(new LocalSocketAddress("VideoCamera"));
            receiver.setReceiveBufferSize(500000);
            receiver.setSendBufferSize(500000);
            sender = lss.accept();
            sender.setReceiveBufferSize(500000);
            sender.setSendBufferSize(500000);
        } catch (IOException e) {
            return;
        }
        initPreview();
        try {
            if (videoSocket == null) {
                videoSocket = new UdpClientSocket();
            }
            if (packet == null) {
                packet = new DSPacket();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void stopVideoRecording() {
        Log.d(TAG, "stopVideoRecording");
        h264Bufferoffset = 0;
        mMediaRecorder.stop();

		/*
         * try { lss.close(); receiver.close(); sender.close(); } catch
		 * (IOException no_room) { no_room.printStackTrace(); }
		 */

    }

    public void startVideo() {
        sendCode = true;

    }

    public void stopVideo() {
        sendCode = false;

    }

    private boolean initMediaRecorder() {
        if (mSurfaceHolder == null)
            return false;
        mCamera.unlock();
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        else
            mMediaRecorder.reset();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(mVideoEncoder);
        mMediaRecorder.setVideoEncodingBitRate(800000 / 2);
        mMediaRecorder.setVideoFrameRate(5);
        mMediaRecorder.setVideoSize(640, 480);

        mMediaRecorder.setOutputFile(sender.getFileDescriptor());
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        try {
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            is = receiver.getInputStream();

        } catch (IOException exception) {
            return false;
        }
        return true;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {/*
																				 * Log
																				 * .
																				 * d
																				 * (
																				 * TAG
																				 * ,
																				 * "surfaceChanged"
																				 * )
																				 * ;
																				 * mSurfaceHolder
																				 * =
																				 * holder
																				 * ;
																				 * 
																				 * initMediaRecorder
																				 * (
																				 * )
																				 * ;
																				 * h264Bufferoffset
																				 * =
																				 * 0
																				 * ;
																				 * 
																				 * byte
																				 * buffer
																				 * [
																				 * ]
																				 * =
																				 * new
																				 * byte
																				 * [
																				 * 4
																				 * ]
																				 * ;
																				 * /
																				 * /
																				 * Skip
																				 * all
																				 * atoms
																				 * preceding
																				 * mdat
																				 * atom
																				 * while
																				 * (
																				 * true
																				 * )
																				 * {
																				 * try
																				 * {
																				 * while
																				 * (
																				 * is
																				 * .
																				 * read
																				 * (
																				 * )
																				 * !=
																				 * 'm'
																				 * )
																				 * ;
																				 * is
																				 * .
																				 * read
																				 * (
																				 * buffer
																				 * ,
																				 * 0
																				 * ,
																				 * 3
																				 * )
																				 * ;
																				 * }
																				 * catch
																				 * (
																				 * IOException
																				 * no_room
																				 * )
																				 * {
																				 * /
																				 * /
																				 * TODO
																				 * Auto
																				 * -
																				 * generated
																				 * catch
																				 * block
																				 * no_room
																				 * .
																				 * printStackTrace
																				 * (
																				 * )
																				 * ;
																				 * }
																				 * if
																				 * (
																				 * buffer
																				 * [
																				 * 0
																				 * ]
																				 * ==
																				 * 'd'
																				 * &&
																				 * buffer
																				 * [
																				 * 1
																				 * ]
																				 * ==
																				 * 'a'
																				 * &&
																				 * buffer
																				 * [
																				 * 2
																				 * ]
																				 * ==
																				 * 't'
																				 * )
																				 * break
																				 * ;
																				 * }
																				 * 
																				 * 
																				 * 
																				 * t
																				 * =
																				 * new
																				 * videoThread
																				 * (
																				 * )
																				 * ;
																				 * t
																				 * .
																				 * start
																				 * (
																				 * )
																				 * ;
																				 */
        mSurfaceView.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        // releaseCamera();
        // mSurfaceHolder = null;

    }

    // ////////////////////////////////////////////////////////////////////////////////////////

    private void writeH264SpsPps() {
        System.arraycopy(h264head, 0, h264Buffer, h264Bufferoffset,
                h264head.length);
        h264Bufferoffset += h264head.length;

        System.arraycopy(h264sps, 0, h264Buffer, h264Bufferoffset,
                h264sps.length);
        h264Bufferoffset += h264sps.length;

        System.arraycopy(h264head, 0, h264Buffer, h264Bufferoffset,
                h264head.length);
        h264Bufferoffset += h264head.length;

        System.arraycopy(h264pps, 0, h264Buffer, h264Bufferoffset,
                h264pps.length);
        h264Bufferoffset += h264pps.length;
    }

    /**
     * @param iskeyframe
     */
    private void writeH264Data(byte iskeyframe) throws IOException,
            InterruptedException {

        System.arraycopy(h264head, 0, h264Buffer, h264Bufferoffset,
                h264head.length);
        h264Bufferoffset += h264head.length;
        // Read NAL unit length (4 bytes)
        fill(rtphl, 4);
        int naluLength = buffer[rtphl + 3] & 0xFF
                | (buffer[rtphl + 2] & 0xFF) << 8
                | (buffer[rtphl + 1] & 0xFF) << 16
                | (buffer[rtphl] & 0xFF) << 24;
        // Read NAL unit header (1 byte)
        fill(rtphl, 1);
        // NAL unit type
        int type = buffer[rtphl] & 0x1F;
        int readLength = fill(rtphl + 1, naluLength - 1);
        System.arraycopy(buffer, 0, h264Buffer, h264Bufferoffset,
                readLength + 1);
        h264Bufferoffset += readLength + 1;
        if (sendCode) {
            packet.sendMediaRequest((DSPacket.DSMediaPacketCallback) VideoRecorder.this, 0, 0, sequence++,
                    iskeyframe, h264Buffer, h264Bufferoffset, false);
        }

        h264Bufferoffset = 0;

        // 为了节省IO流的开销，需要关闭
        // fos.close();

    }

    private int fill(int offset, int length) throws IOException {
        int sum = 0, len;
        while (sum < length) {
            len = is.read(buffer, offset + sum, length - sum);
            if (len < 0) {
                throw new IOException("End of stream");
            } else {
                sum += len;
            }
        }
        return sum;

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                Log.d(TAG, "MEDIA_RECORDER_INFO_UNKNOWN");
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                Log.d(TAG, "MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                Log.d(TAG, "MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED");
                break;
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            Log.d(TAG, "MEDIA_RECORDER_ERROR_UNKNOWN");

        }
    }

    private void initPreview() {
        mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters = mCamera.getParameters();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    }

    @Override
    public void onMediaPacket(byte[] mdpkt, int mdpktlength) {
        // TODO Auto-generated method stub
        try {
            videoSocket.send(AppConfig.SERVER_HOST_IP, serverVideoPort, mdpkt,
                    mdpktlength);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void releaseCamera() {
        if (null != mCamera) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private class videoThread extends Thread {
        @Override
        public synchronized void run() {

            while (true) {
                try {
                    byte iskeyframe = 0;
                    gop++;
                    if (gop == 15) {
                        gop = 0;
                    }
                    if (gop == 0) {
                        iskeyframe = 1;
                    }

                    if (iskeyframe == 1) {
                        writeH264SpsPps();
                    }
                    try {
                        writeH264Data(iskeyframe);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

}