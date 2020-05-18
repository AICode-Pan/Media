package com.explain.media.activity;

import android.app.Activity;
import android.content.Intent;

public class BaseActivity extends Activity {

    protected void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivityForResult(intent, 123);
    }
}
