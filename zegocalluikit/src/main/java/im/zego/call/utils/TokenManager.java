package im.zego.call.utils;

import com.blankj.utilcode.util.SPStaticUtils;

import java.util.Timer;
import java.util.TimerTask;

import im.zego.call.ZegoCallManager;
import im.zego.call.constant.Constants;
import im.zego.callsdk.callback.ZegoRequestCallback;
import im.zego.callsdk.model.ZegoUserInfo;

public class TokenManager {
    private static volatile TokenManager singleton = null;

    private TokenManager() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (needUpdateToken()) {
                    ZegoUserInfo userInfo = ZegoCallManager.getInstance().getLocalUserInfo();
                    if (userInfo != null) {
                        String userID = userInfo.userID;
                        ZegoCallManager.getInstance().getToken(userID, new ZegoRequestCallback() {
                            @Override
                            public void onResult(int errorCode, Object obj) {
                                if (errorCode == 0) {
                                    saveToken((String) obj, 24 * 3600 * 1000L);
                                } else {
                                }
                            }
                        });
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 60 * 1000L);
    }

    public static TokenManager getInstance() {
        if (singleton == null) {
            synchronized (TokenManager.class) {
                if (singleton == null) {
                    singleton = new TokenManager();
                }
            }
        }
        return singleton;
    }

    public TokenWrapper tokenWrapper;

    private void saveToken(String token, long effectiveTimeInSeconds) {
        long expiryTime = System.currentTimeMillis() + effectiveTimeInSeconds;

        SPStaticUtils.put(Constants.ZEGO_TOKEN_KEY, token);
        SPStaticUtils.put(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY, expiryTime);

        this.tokenWrapper = new TokenWrapper(token, expiryTime);
    }

    private boolean needUpdateToken() {
        if (tokenWrapper == null) return true;
        return System.currentTimeMillis() + 60 * 60 * 1000L > tokenWrapper.expiryTime;
    }

    private TokenWrapper getTokenFromDisk() {
        String token = SPStaticUtils.getString(Constants.ZEGO_TOKEN_KEY);
        long expiryTime = SPStaticUtils.getLong(Constants.ZEGO_TOKEN_EXPIRY_TIME_KEY);

        if (expiryTime < System.currentTimeMillis()) {
            return null;
        }

        return new TokenWrapper(token, expiryTime);
    }

    public static class TokenWrapper {
        public String token;
        public long expiryTime;

        TokenWrapper(String token, long expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }

        boolean isTokenValid() {
            return expiryTime > System.currentTimeMillis() + 10 * 60 * 1000L;
        }
    }
}
