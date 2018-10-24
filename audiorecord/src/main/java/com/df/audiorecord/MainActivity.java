package com.df.audiorecord;

import android.Manifest;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.df.audiorecord.record.AudioRecordManager;
import com.df.audiorecord.record.OnRecordingListener;
import com.df.audiorecord.utils.ToastUitl;
import com.df.audiorecord.widgets.AudioRecordButton;
import com.df.audiorecord.widgets.AudioView;
import com.df.audiorecord.widgets.TimeAndVolumeView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static MainActivity mInstance;
    private AudioView mAudioView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInstance = this;
        mAudioView = findViewById(R.id.audio_view);
        mAudioView.bindActivity(mInstance);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //没有权限的情况下不响应
        mAudioView.setAudioEnable(checkPermission());
    }

    public static MainActivity getmInstance() {
        return mInstance;
    }

    /**
     * 录音与文件独写权限检测
     *
     * @return
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (AudioRecordManager.getInstance().checkAudioPermission(getApplicationContext())) {
                return true;
            } else {
                ToastUitl.showShort(getApplicationContext(), "请前往设置中开启录音权限");
                return false;
            }
        } else {
            final boolean[] record_permissiont = {false};
            final boolean[] write_permissiont = {false};
            RxPermissions permissions = new RxPermissions(this);
            permissions.requestEach(Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Permission>() {
                @Override
                public void accept(Permission permission) throws Exception {
                    if (permission.name == Manifest.permission.RECORD_AUDIO) {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            record_permissiont[0] = true;
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            record_permissiont[0] = false;

                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUitl.showShort(getApplicationContext(), "请前往设置中开启" + permission.name + "权限");
                            record_permissiont[0] = false;
                        }
                    } else {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            write_permissiont[0] = true;
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            write_permissiont[0] = false;
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUitl.showShort(getApplicationContext(), "请前往设置中开启" + permission.name + "权限");
                            write_permissiont[0] = false;
                        }
                    }
                }
            });
            return record_permissiont[0] && write_permissiont[0];
        }
    }


}
