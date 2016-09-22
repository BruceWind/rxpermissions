package rxcomponent.wei.rxpermissions;

import android.Manifest;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.androidyuan.RxPermissions;
import com.androidyuan.component.OnPermissionsCallback;

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
                        .callback(new OnPermissionsCallback() {
                            @Override
                            public void call(Boolean aBoolean) {
                                toast("" + aBoolean);
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
