package rx.component;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by wei on 16-9-17.
 */
public class RxPermissions {

    public static final String TAG = "RxPermissions";

    static RxPermissions sSingleton;

    public static RxPermissions getInstance(Context ctx) {

        if (sSingleton == null) {
            sSingleton = new RxPermissions(ctx.getApplicationContext());
        }
        return sSingleton;
    }

    private Context mCtx;

    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();


    RxPermissions(Context ctx) {

        mCtx = ctx;
    }


    private void log(String message) {

        Log.d(TAG, message);
    }

    /**
     * 自动请求权限 和
     */
    public Observable.Transformer<Object, Boolean> ensure(final String... permissions) {

        return new Observable.Transformer<Object, Boolean>() {
            @Override
            public Observable<Boolean> call(Observable<Object> o) {

                return request(o, permissions)
                        // Transform Observable<Permission> to Observable<Boolean>
                        .buffer(permissions.length)
                        .flatMap(new Func1<List<Permission>, Observable<Boolean>>() {
                            @Override
                            public Observable<Boolean> call(List<Permission> permissions) {

                                if (permissions.isEmpty()) {
                                    // 发生屏幕方向转变
                                    // 在这种情况下我们不想传递空列表
                                    // 响应到subscriber, 只有在onComplete.
                                    return Observable.empty();
                                }
                                // return true,代表所有权限都授予
                                for (Permission p : permissions) {
                                    if (!p.granted) {
                                        return Observable.just(false);
                                    }
                                }
                                return Observable.just(true);
                            }
                        });
            }
        };
    }

    /**
     * 自动请求多个权限
     */
    public Observable.Transformer<Object, Permission> ensureEach(final String... permissions) {

        return new Observable.Transformer<Object, Permission>() {
            @Override
            public Observable<Permission> call(Observable<Object> o) {

                return request(o, permissions);
            }
        };
    }

    /**
     * 立即请求权限,调用必须在应用程序的初始化阶段
     */
    public Observable<Boolean> request(final String... permissions) {

        return Observable.just(null).compose(ensure(permissions));
    }

    /**
     * 立即请求权限,调用必须在应用程序的初始化阶段
     */
    public Observable<Permission> requestEach(final String... permissions) {

        return Observable.just(null).compose(ensureEach(permissions));
    }

    private Observable<Permission> request(final Observable<?> trigger,
                                           final String... permissions) {

        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
        return oneOf(trigger, pending(permissions))
                .flatMap(new Func1<Object, Observable<Permission>>() {
                    @Override
                    public Observable<Permission> call(Object o) {

                        return request_(permissions);
                    }
                });
    }

    private Observable<?> pending(final String... permissions) {

        for (String p : permissions) {
            if (!mSubjects.containsKey(p)) {
                return Observable.empty();
            }
        }
        return Observable.just(null);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {

        if (trigger == null) {
            return Observable.just(null);
        }
        return Observable.merge(trigger, pending);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Observable<Permission> request_(final String... permissions) {

        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();

        // 在多个权限的情况下,我们为每个创造一个可观测的
        // At the end, the observables are combined to have a unique response.
        for (String permission : permissions) {
            log("Requesting permission " + permission);
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                list.add(Observable.just(new Permission(permission, true)));
                continue;
            }

            if (isRevoked(permission)) {
                // Revoked by a policy, return a denied Permission object.
                list.add(Observable.just(new Permission(permission, false)));
                continue;
            }

            PublishSubject<Permission> subject = mSubjects.get(permission);
            // Create a new subject if not exists
            if (subject == null) {
                unrequestedPermissions.add(permission);
                subject = PublishSubject.create();
                mSubjects.put(permission, subject);
            }

            list.add(subject);
        }

        if (!unrequestedPermissions.isEmpty()) {
            startShadowActivity(unrequestedPermissions
                    .toArray(new String[unrequestedPermissions.size()]));
        }
        return Observable.concat(Observable.from(list));
    }

    /**
     * 调用活动: shouldShowRequestPermissionRationale 和 包装在一个可观测的返回值
     * <p>
     * 活动: shouldShowRequestPermissionRationale 返回适用于所有撤销权限
     * 你不应该调用这个方法,如果所有权限已授予
     */
    public Observable<Boolean> shouldShowRequestPermissionRationale(final Activity activity, final String... permissions) {

        if (!isHighApi()) {
            return Observable.just(false);
        }
        return Observable.just(shouldShowRequestPermissionRationale_(activity, permissions));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationale_(final Activity activity,
                                                          final String... permissions) {

        for (String p : permissions) {
            if (!isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false;
            }
        }
        return true;
    }

    void startShadowActivity(String[] permissions) {

        log("startShadowActivity " + TextUtils.join(", ", permissions));
        Intent intent = new Intent(mCtx, PermissionReqActivity.class);
        intent.putExtra("permissions", permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mCtx.startActivity(intent);
    }

    /**
     * 返回true,已经获得许可
     */
    public boolean isGranted(String permission) {

        return !isHighApi() || isGranted_(permission);
    }

    /**
     * 返回true,如果政策许可被撤销。
     */
    public boolean isRevoked(String permission) {

        return isHighApi() && isRevoked_(permission);
    }

    //如果是高版本api
    boolean isHighApi() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isGranted_(String permission) {

        return mCtx.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isRevoked_(String permission) {

        return mCtx.getPackageManager().isPermissionRevokedByPolicy(permission, mCtx.getPackageName());
    }

    void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        for (int i = 0, size = permissions.length; i < size; i++) {
            log("onRequestPermissionsResult  " + permissions[i]);
            // 查找相应的subject
            PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
            if (subject == null) {
                throw new IllegalStateException("RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
            }
            mSubjects.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            subject.onNext(new Permission(permissions[i], granted));
            subject.onCompleted();
        }
    }
}
