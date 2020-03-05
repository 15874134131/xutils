package com.android.xlwlibrary.audiorecorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.xlwlibrary.helper.XHelper;
import com.android.xlwlibrary.helper.XPcmToWav;
import com.android.xlwlibrary.helper.XThreadPoolHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2018/12/19.
 */

public class XAudioRecorder {
    private Context mContext;
    // 音频源：音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    // 音频通道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // 音频格式：PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区大小：缓冲区字节大小
    private int bufferSizeInBytes = 0;
    // 录音对象
    private AudioRecord audioRecord;
    // 录音状态
    private Status status=Status.STATUS_NO_READY;
    // 文件名
    private String fileName;
    // 录音文件集合，这是因为录音的时候 ，可能存在暂停的情况，
    // 但是用户点击暂停，然后重新开始的时候，其实是不同的两个音频文件，为了防止音频不混乱，同一时段的音频文件用list 保存
    private List<String> filesName = new ArrayList<>();
    private ThreadPoolExecutor threadPoolExecutor;
    private RecordStreamListener listener;
    private XHelper xHelper;

    public XAudioRecorder(Context context, ThreadPoolExecutor threadPoolExecutor, RecordStreamListener listener) {
        this.mContext=context;
        this.threadPoolExecutor=threadPoolExecutor;
        this.listener=listener;
        xHelper=XHelper.defaultXHelper();
    }

    /**
     * 创建录音对象
     */
    public void createAudio(String fileName, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        this.fileName = fileName;
    }

    /**
     * 创建默认的录音对象
     * @param fileName 文件名
     */

    public void createDefaultAudio(String fileName) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 16000,
                AUDIO_CHANNEL, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        this.fileName = fileName;
        status = Status.STATUS_READY;
        listener.setCallbackStatus(status);
    }

    //暂停
    public void onArPause(){
        if (status==Status.STATUS_START){
            //每次暂停其实都是结束当前回合录音的
            audioRecord.stop();
            status=Status.STATUS_PAUSE;
            listener.setCallbackStatus(status);
        }else {
            Toast.makeText(mContext,"录音未开始", Toast.LENGTH_SHORT).show();
        }
    }

    //继续
    public void onArContinue(){
        if (status==Status.STATUS_PAUSE){
            audioRecord.startRecording();
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    writeDataTOFile();
                    status=Status.STATUS_START;
                    listener.setCallbackStatus(status);
                }
            });
        }
    }

    /**
     * 开始录音
     */
    public void onStartRecord() {
        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            Toast.makeText(mContext,"录音尚未初始化,请检查是否禁止了录音权限~", Toast.LENGTH_SHORT).show();
        }
        if (status==Status.STATUS_READY){
            audioRecord.startRecording();
            XThreadPoolHelper.pool.execute(new Runnable() {
                @Override
                public void run() {
                    status=Status.STATUS_START;
                    listener.setCallbackStatus(status);
                    writeDataTOFile();
                }
            });
        }
    }

    /**
     * 停止录音
     */
    public void onStopRecord() {
        if (status == Status.STATUS_START || status == Status.STATUS_PAUSE) {
            audioRecord.stop();
            status = Status.STATUS_STOP;
            listener.setCallbackStatus(status);
            onRelease();
        }
    }

    /**
     * 取消录音
     */
    @Deprecated
    public void onCanel() {
        filesName.clear();
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        status = Status.STATUS_NO_READY;
        listener.setCallbackStatus(status);
    }

    /**
     * 释放资源
     */
    private void onRelease() {
        Log.d("XAudioRecorder","===onRelease===");
        //假如有暂停录音
        try {
            if (filesName.size() > 0) {
                List<String> filePaths = new ArrayList<>();
                for (String fileName : filesName) {
                    filePaths.add(getPcmFileAbsolutePath(fileName));
                }
                //清除
                filesName.clear();
                //将多个pcm文件转化为wav文件
                mergePCMFilesToWAVFile(filePaths);
            } else {
                //这里由于只要录音过filesName.size都会大于0,没录音时fileName为null
                //会报空指针 NullPointerException
                // 将单个pcm文件转化为wav文件
                makePCMFileToWAVFile();
            }
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        status = Status.STATUS_NO_READY;
        listener.setCallbackStatus(status);
    }

    /**
     * 将音频信息写入文件,
     */
    private void  writeDataTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        //输出流
        FileOutputStream fos = null;
        int readsize = 0;
        try {
            //文件名称
            String currentFileName = fileName;
            //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
            if (status == Status.STATUS_PAUSE) {
                currentFileName += filesName.size();
            }
            filesName.add(currentFileName);
            //生成pm 文件
            File file = new File(getPcmFileAbsolutePath(currentFileName));
            if (file.exists()) {
                file.delete();
            }
            // 建立一个可存取字节的文件
            fos = new FileOutputStream(file);
        } catch (IllegalStateException e) {
            Log.e("XAudioRecorder", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        } catch (FileNotFoundException e) {
            Log.e("XAudioRecorder", e.getMessage());
        }
        while (status == Status.STATUS_START) {
            //从硬件设备读取音频数据
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(audiodata);
                    if (listener != null) {
                        //返回 byte 数组，
                        listener.recordOfByte(audiodata, 0, audiodata.length);
                    }
                } catch (IOException e) {
                    Log.e("XAudioRecorder", e.getMessage());
                }
            }
        }
        try {
            if (fos != null) {
                fos.close();// 关闭输出流
            }
        } catch (IOException e) {
            Log.e("XAudioRecorder", e.getMessage());
        }
    }



    /**
     * 将pcm合并成wav
     *
     * @param filePaths
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (xHelper.xPcmToWav.mergePCMFilesToWAVFile(filePaths,getWavFileAbsolutePath(fileName+".wav"))) {
                        //操作成功
                    } else {
                        //操作失败
                        Log.e("XAudioRecorder", "mergePCMFilesToWAVFile fail");
                        throw new IllegalStateException("mergePCMFilesToWAVFile fail");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 将单个pcm文件转化为wav文件
     */
    private void makePCMFileToWAVFile() {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (xHelper.xPcmToWav.makePCMFileToWAVFile(getPcmFileAbsolutePath(fileName), getWavFileAbsolutePath(fileName), true)) {
                        //操作成功
                    } else {
                        //操作失败
                        Log.e("XAudioRecorder", "makePCMFileToWAVFile fail");
                        throw new IllegalStateException("makePCMFileToWAVFile fail");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //在指定路径下创建该文件，返回文件路径
    private String getPcmFileAbsolutePath(String filename){
        String path= Environment.getExternalStorageDirectory()+ File.separator+"ar"+ File.separator+"pm";
        String getPcmFileAbsolutePath="";
        File file2 = new File(path);
        if (!file2.mkdirs()) {//在目录下创建文件夹，如果父文件夹不存在则先创建父文件夹
            try {
                file2.createNewFile();//如果创建目录没有成功，就创建一个空白文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path + File.separator + filename);
        if (file.exists()) {
            getPcmFileAbsolutePath=file.getAbsolutePath();
        } else {
            try {
                boolean isCreated = file.createNewFile();
                if (isCreated) {
                    getPcmFileAbsolutePath=file.getAbsolutePath();
                } else {
                    boolean isCreated2=file.createNewFile();
                    if (isCreated2){
                        getPcmFileAbsolutePath=file.getAbsolutePath();
                    }else {
                        getPcmFileAbsolutePath="创建未成功";
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getPcmFileAbsolutePath;
    }

    private String getWavFileAbsolutePath(String filename) throws IOException {
        String wavPath= Environment.getExternalStorageDirectory()+ File.separator+"ar"+ File.separator+"wav";
        String AbsolutePath="";
        File wavfile= new File(wavPath);
        if (!wavfile.mkdirs()) {//在目录下创建文件夹，如果父文件夹不存在则先创建父文件夹
            try {
                wavfile.createNewFile();//如果创建目录没有成功，就创建一个空白文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(wavPath + File.separator + filename);
        if (file.exists()) {
            throw new IOException("文件已经存在");
        } else {
            try {
                boolean isCreated = file.createNewFile();
                if (isCreated) {
                    AbsolutePath=file.getAbsolutePath();
                } else {
                    boolean isCreated2=file.createNewFile();
                    if (isCreated2){
                        AbsolutePath=file.getAbsolutePath();
                    }else {
                        AbsolutePath="创建未成功";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return AbsolutePath;
    }
}
