package im.zego.call.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.StringUtils;
import com.permissionx.guolindev.PermissionX;
import im.zego.call.R;
import java.util.Objects;

/**
 * Created by rocket_wang on 2021/12/10.
 */
public class PermissionHelper {

    public static void requestCameraAndAudio(FragmentActivity activity,
        @Nullable IPermissionCallback permissionCallback) {
        PermissionX.init(activity)
            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .onExplainRequestReason((scope, deniedList) -> scope
                .showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"))
            .onForwardToSettings((scope, deniedList) -> {
                if (deniedList.size() > 0) {
                    if (Objects.equals(Manifest.permission.CAMERA, deniedList.get(0))) {
                        new AlertDialog.Builder(activity)
                            .setTitle(StringUtils.getString(R.string.dialog_login_page_title_cannot_use_camera))
                            .setMessage(StringUtils.getString(R.string.dialog_login_page_massage_cannot_use_camera))
                            .setNegativeButton(StringUtils.getString(R.string.dialog_login_page_cancel),
                                (dialog, which) -> {
                                    dialog.dismiss();
                                })
                            .setPositiveButton(StringUtils.getString(R.string.dialog_login_page_go_to_settings),
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    forwardToSettings(activity);
                                })
                            .create()
                            .show();
                    } else {
                        new AlertDialog.Builder(activity)
                            .setTitle(StringUtils.getString(R.string.dialog_login_page_mic_cant_open))
                            .setMessage(StringUtils.getString(R.string.dialog_login_page_mic_permission))
                            .setNegativeButton(StringUtils.getString(R.string.dialog_login_page_cancel),
                                (dialog, which) -> {
                                    dialog.dismiss();
                                })
                            .setPositiveButton(StringUtils.getString(R.string.dialog_login_page_go_to_settings),
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    forwardToSettings(activity);
                                })
                            .create()
                            .show();
                    }
                }
            })
            .request((allGranted, grantedList, deniedList) -> {
                if (permissionCallback != null) {
                    permissionCallback.onRequestCallback(allGranted);
                }
            });
    }

    private static void forwardToSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static boolean checkFloatWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PermissionUtils.isGrantedDrawOverlays();
        } else {
            return true;
        }
    }

    public interface IPermissionCallback {

        void onRequestCallback(boolean isAllGranted);
    }
}