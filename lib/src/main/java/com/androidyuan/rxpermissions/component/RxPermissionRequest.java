package com.androidyuan.rxpermissions.component;

import rx.Observable;

/**
 * Created by wei on 16-9-22.
 */
public class RxPermissionRequest {

    Observable<Boolean> mBooleanObservable;

    public RxPermissionRequest(Observable<Boolean> observable) {
        mBooleanObservable = observable;
    }


    public void callback(OnPermissionsCallback callback) {
        mBooleanObservable.subscribe(callback);
    }

}
