# RxPermissions
rxpermissions
Just want to let the authority to apply for more easy and convenient management
# use
```
//begin
RxPermissions.getInstance(MainActivity.this)
                        .request(Manifest.permission.CAMERA)
                        .callback(new OnPermissionsCallback() {
                            @Override
                            public void call(Boolean aBoolean) {
                                toast("" + aBoolean);
                            }
                        });
// end


// lambda structure
                        RxPermissions.getInstance(MainActivity.this)
                        .request(Manifest.permission.CAMERA)
                        .callback((Boolean aBoolean) -> toast("" + aBoolean) ); // show a success
```

## features

1. Easy to use (than the official plan is convenient, while allowing the child thread application, the callback is always in the main thread)

2. Efficient, can apply for multiple) at the same time
## note:
> - Need to be registered in mainfast  

```
<! - set transparent notitlebar -- -->
The < activity android: name = "rx.com ponent. PermissionReqActivity"
Android: theme = "@ android: style/theme. Translucent. NoTitleBar" / >
```

> - because the application process is to start a new transparent Activity, so it's easy to apply for the act of multiple trigger onPause, onResume.