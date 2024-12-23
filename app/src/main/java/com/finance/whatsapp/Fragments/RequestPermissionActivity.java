// RequestPermissionActivity.java
package com.finance.whatsapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.finance.whatsapp.MainActivity;
import com.finance.whatsapp.R;
import com.finance.whatsapp.utils.FileUtils;
import com.finance.whatsapp.utils.RequestPermissionsHelper;

public class RequestPermissionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_permission);

        if (RequestPermissionsHelper.verifyPermissions(this)) {
            initializeAndProceed();
        } else {
            RequestPermissionsHelper.requestPermissions(this, this, PERMISSION_REQUEST_CODE);
        }

        findViewById(R.id.grant_permissions_button).setOnClickListener(v ->
                RequestPermissionsHelper.requestPermissions(this, this, PERMISSION_REQUEST_CODE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RequestPermissionsHelper.verifyPermissions(this)) {
            initializeAndProceed();
        }
    }

    private void initializeAndProceed() {
        FileUtils.initializeDirectories(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            RequestPermissionsHelper.handlePermissionsResult(this,
                    RequestPermissionsHelper.verifyPermissions(this),
                    this::initializeAndProceed);
        }
    }

}
