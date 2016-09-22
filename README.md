# RxPermissions
rxpermissions

只是想要让权限申请管理更加简单方便

# 使用 

```
                //开始
                RxPermissions.getInstance(MainActivity.this)
                        .request(Manifest.permission.CAMERA)
                        .subscribe(new OnPermissionsCallback() {
                            @Override
                            public void call(Boolean aBoolean) {
                                toast(""+aBoolean);//显示成功与否
                            }
                        });
                //结束
                
                //lambda结构
                RxPermissions.getInstance(MainActivity.this)
                .request(Manifest.permission.CAMERA)
                .subscribe((Boolean aBoolean)-> toast(""+aBoolean) });//显示成功与否


```


# 特性

1.易用（比官方的方案方便,同时允许在子线程申请,callback始终在主线程）
2.高效（可以同时申请多个）

## 注意：

> - 使用中需要在mainfast中注册

```

        <!-- 设置透明 notitlebar -->
        <activity android:name="rx.component.PermissionReqActivity"
                  android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```
> - 申请过程因为是启动一个新的透明的Activity，所以很容易发生申请的act多次触发onPause,onResume的情况。



