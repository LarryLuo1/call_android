package im.zego.call.ui;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.Utils;
import com.gyf.immersionbar.ImmersionBar;
import im.zego.call.ui.common.TipsDialog;
import im.zego.call.ui.common.TipsDialog.TipsMessageType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    protected T binding;
    private static final Handler tipsHandler = new Handler(Looper.getMainLooper());
    public static final int TIPS_TIME = 3 * 1000;
    protected TipsDialog tipsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            Class<T> clazz = (Class<T>) actualTypeArgument;
            Method method = clazz.getMethod("inflate", LayoutInflater.class);
            binding = (T) method.invoke(null, getLayoutInflater());
            View view = binding.getRoot();
            setContentView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImmersionBar.with(this)
            .statusBarColor(android.R.color.white)
            .fitsSystemWindows(true)
            .statusBarDarkFont(true)
            .init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tipsHandler.removeCallbacksAndMessages(null);
        if (tipsDialog != null) {
            tipsDialog.dismiss();
            tipsDialog = null;
        }
    }

    protected void showNormalTips(String message) {
        showTips(TipsMessageType.NORMAL, message, TIPS_TIME);
    }

    protected void showNormalTips(int res) {
        showTips(TipsMessageType.NORMAL, Utils.getApp().getString(res), TIPS_TIME);
    }

    protected void showWarnTips(String message) {
        showTips(TipsMessageType.WARN, message, TIPS_TIME);
    }

    protected void showWarnTips(int res) {
        showTips(TipsMessageType.WARN, Utils.getApp().getString(res), TIPS_TIME);
    }

    private void showTips(TipsMessageType type, String message, long time) {
        if (!AppUtils.isAppForeground() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tipsHandler.post(() -> Toast.makeText(Utils.getApp(), message, Toast.LENGTH_SHORT).show());
        }
        if (tipsDialog == null) {
            tipsDialog = new TipsDialog(this);
        }
        tipsHandler.post(() -> {
            tipsDialog.showColorTips(type, message);
            if (!tipsDialog.isShowing()) {
                tipsDialog.show();
            }
        });
        if (time != 0) {
            tipsHandler.postDelayed(() -> {
                hideTips();
            }, time);
        }
    }

    public void hideTips() {
        tipsDialog.dismiss();
    }
}
