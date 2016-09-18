package rxcomponent.wei.rxpermissions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.hardware.Camera;
import android.view.View;
import android.widget.Toast;
import rx.android.schedulers.AndroidSchedulers;
import rx.component.RxPermissions;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RxPermissionsSample";

    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.enableCamera).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //开始
                RxPermissions.getInstance(MainActivity.this)
                        .request(Manifest.permission.CAMERA)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {

                            @Override
                            public void call(Boolean aBoolean) {//权限回调
                                toast(aBoolean + "");
                            }
                        });
                //结束

            }
        });

    }

    @Override
    protected void onStop() {

        super.onStop();
        releaseCamera();
    }

    private void toast(String str) {

        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void releaseCamera() {

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
