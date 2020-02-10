package com.jimlake.fyberofferwall;

import javax.annotation.Nullable;
import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import com.fyber.Fyber;
import com.fyber.ads.AdFormat;
import com.fyber.requesters.OfferWallRequester;
import com.fyber.requesters.RequestCallback;
import com.fyber.requesters.RequestError;
import com.fyber.user.User;

public class RNFyberOfferwallModule extends ReactContextBaseJavaModule  {
  private static final int OFFERWALL_REQUEST_CODE = 1234;
  private static final String TAG = "RNFyberOfferwall";
  private final ReactApplicationContext mReactContext;
  private Intent mOfferwallIntent = null;
  private boolean mIsRequesting = false;
  private Callback mOfferwallCallback = null;
  private Fyber.Settings mFyberSettings = null;

  public RNFyberOfferwallModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.mReactContext = reactContext;
    reactContext.addActivityEventListener(activityEventListener);
  }

  @Override
  public String getName() {
    return "RNFyberOfferwall";
  }

  private final ActivityEventListener activityEventListener = new ActivityEventListener() {
    @Override
    public void onActivityResult(final Activity activity,final int requestCode,final int resultCode,final Intent data) {
      if (requestCode == OFFERWALL_REQUEST_CODE) {
        final WritableNativeMap params = new WritableNativeMap();
        params.putInt("resultCode",resultCode);
        sendReactEvent("offerwall_closed",params);
        if (mOfferwallCallback != null) {
          mOfferwallCallback.invoke(resultCode);
          mOfferwallCallback = null;
        }
      } else {
        Log.d(TAG,"Other activity result:" + requestCode);
      }
    }
    @Override
    public void onNewIntent(final Intent intent) {}
  };

  private final RequestCallback requestCallback = new RequestCallback() {
    @Override
    public void onAdAvailable(Intent intent) {
      mOfferwallIntent = intent;
      sendReactEvent("offerwall_available",null);
    }
    @Override
    public void onAdNotAvailable(AdFormat adFormat) {
      mOfferwallIntent = null;
      sendReactEvent("offerwall_not_available",null);
    }
    @Override
    public void onRequestError(RequestError requestError) {
      mOfferwallIntent = null;
      sendReactEvent("offerwall_request_error",null);
    }
  };
  private void sendReactEvent(final String eventName,@Nullable WritableMap params) {
    if (params == null) {
      params = new WritableNativeMap();
      params.putString("name",eventName);
    }
    getReactApplicationContext()
      .getJSModule(RCTDeviceEventEmitter.class)
      .emit("FyberOfferwallEvent",params);
  }

  @ReactMethod
  public void init(final String appId,final String securityToken,final Callback callback) {
    final Activity activity = getCurrentActivity();
    if (activity != null) {
      mFyberSettings = Fyber.with(appId,activity).withSecurityToken(securityToken).start();
      callback.invoke((Object)null);
    } else {
      callback.invoke("no_activity");
    }
  }

  @ReactMethod
  public void setUserId(final String userId) {
    if (mFyberSettings != null) {
      try {
        mFyberSettings.updateUserId(userId);
      } catch (final Exception e) {
        Log.d(TAG,"setUserId threw");
      }
    }
  }
  @ReactMethod
  public void setGdprConsent(final Boolean gdprConsent) {
    User.setGdprConsent(gdprConsent,mReactContext);
  }

  @ReactMethod
  public void startRequest(final Boolean closeOnRedirect) {
    OfferWallRequester.create(requestCallback)
      .closeOnRedirect(closeOnRedirect)
      .request(mReactContext);
  }

  @ReactMethod
  public void showOfferwall(final Callback callback) {
    if (mOfferwallIntent != null) {
      mReactContext.startActivityForResult(mOfferwallIntent,OFFERWALL_REQUEST_CODE,null);
      mOfferwallCallback = callback;
    } else {
      callback.invoke("offerwall_not_available");
      mOfferwallCallback = null;
    }
  }
}
