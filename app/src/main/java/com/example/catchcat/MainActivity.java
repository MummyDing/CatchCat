package com.example.catchcat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog.Builder;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,1,"About");
        menu.add(0,2,2,"Exit");
        return super.onCreateOptionsMenu(menu);
    }


    protected void exitDialog() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setMessage(R.string.exit_Notification);
        builder.setTitle(R.string.exit_title);
        builder.setPositiveButton(R.string.confirm_Btn, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });
        builder.setNegativeButton(R.string.cancel_Btn, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    protected void aboutDialog() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setMessage("AppName:CatchCat"+"Version: Release Version 1.0"+"\n"+"Author:MummyDing"+"\n"+"E-mail:MummyDing@outlook.com"+"\n"+"Blog:http://blog.csdn.net/mummyding");

        builder.setTitle(R.string.about_Title);

        builder.setPositiveButton(R.string.confirm_Btn, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case 1:
                aboutDialog();
                break;
            case 2:
                exitDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
