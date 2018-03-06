package io.aniruddh.delego;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.ason.Ason;
import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Callback;
import com.afollestad.bridge.MultipartForm;
import com.afollestad.bridge.Pipe;
import com.afollestad.bridge.ProgressCallback;
import com.afollestad.bridge.Request;
import com.afollestad.bridge.Response;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import io.aniruddh.delego.fragments.AllDelegatesFragment;
import io.aniruddh.delego.fragments.ByCommitteeFragment;
import io.aniruddh.delego.fragments.FeedbackFragment;
import io.aniruddh.delego.fragments.FileUploadFragment;
import io.aniruddh.delego.fragments.FoodFragment;
import io.aniruddh.delego.fragments.ScanQRFragment;
import io.aniruddh.delego.fragments.SearchViewFragment;
import io.aniruddh.delego.fragments.SpeakerListFragment;
import io.aniruddh.delego.fragments.UserDetails;
import io.aniruddh.delego.fragments.ViewUploadFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements ScanQRFragment.OnFragmentInteractionListener, FoodFragment.OnFragmentInteractionListener, FileUploadFragment.OnFragmentInteractionListener, FeedbackFragment.OnFragmentInteractionListener{

    String user_type = null;
    String user_committee = null;
    String user_id = null;
    private Toolbar toolbar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (user_type.contentEquals("user")){
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_preferences:
                        toolbar.setTitle(R.string.preferences);
                        fragment = new FoodFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_scan_qr:
                        toolbar.setTitle(R.string.scan_qr);
                        fragment = new ScanQRFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_file_upload_user:
                        toolbar.setTitle(R.string.file_upload);
                        fragment = new FileUploadFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_feedback_user:
                        toolbar.setTitle(R.string.feedback);
                        fragment = new FeedbackFragment();
                        loadFragment(fragment);
                        return true;
                }
            } else if (user_type.contentEquals("oc")){
                final Fragment[] fragment = new Fragment[1];
                switch (item.getItemId()) {
                    case R.id.navigation_all_delegates:
                        toolbar.setTitle(R.string.all_delegates);
                        fragment[0] = new AllDelegatesFragment();
                        loadFragment(fragment[0]);
                        return true;
                    case R.id.navigation_scan_qr:
                        fragment[0] = new ScanQRFragment();
                        loadFragment(fragment[0]);
                        toolbar.setTitle(R.string.scan_qr);
                        return true;
                    case R.id.navigation_search:
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Enter search query")
                                .inputRangeRes(2, 20, R.color.colorAccent)
                                .input(null, null, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        // Do something
                                        Keys.SEARCH = input.toString();
                                        fragment[0] = new SearchViewFragment();
                                        dialog.dismiss();
                                        loadFragment(fragment[0]);
                                    }
                                }).show();
                }
            } else if (user_type.contentEquals("rapporteur")){
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_my_committee:
                        toolbar.setTitle(user_committee);
                        fragment = new ByCommitteeFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_uploads:
                        toolbar.setTitle(R.string.view_uploads);
                        fragment = new ViewUploadFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_speaker_list:
                        toolbar.setTitle(R.string.speaker_list);
                        fragment = new SpeakerListFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_contact_exchange_rap:
                        toolbar.setTitle(R.string.contact_exchange);
                        fragment = new ScanQRFragment();
                        loadFragment(fragment);
                        return true;
                }
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SanFranciscoDisplay-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences(Keys.PREFERENCES, MODE_PRIVATE);
        user_type = prefs.getString("type", "user");
        user_committee = prefs.getString("committee", "My Committee");
        user_id = prefs.getString("identifier", "default");
        // Attaching the layout to the toolbar object
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Fragment fragment;
        if(user_type.contentEquals("user")){
            navigation.inflateMenu(R.menu.navigation_user);
            fragment = new FoodFragment();
            loadFragment(fragment);
        } else if (user_type.contentEquals("oc")){
            navigation.inflateMenu(R.menu.navigation_oc);
            fragment = new AllDelegatesFragment();
            loadFragment(fragment);
        } else if (user_type.contentEquals("rapporteur")){
            navigation.inflateMenu(R.menu.navigation_rap);
            fragment = new ByCommitteeFragment();
            loadFragment(fragment);
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.navigation_add_to_speakers);
        if (user_type.contentEquals("oc")){
            item.setVisible(false);
        } else if (user_type.contentEquals("rapporteur")){
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.navigation_logout) {
            SharedPreferences.Editor editor = getSharedPreferences(Keys.PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean("logged_status", false);
            editor.apply();
            finish();
            return true;
        } else if (id == R.id.navigation_add_to_speakers){
            new MaterialStyledDialog.Builder(this)
                    .setTitle("Add to speakers")
                    .setDescription("Are you sure you want to be added in the speakers list?")
                    .setHeaderColor(R.color.dialog_header)
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setNegativeText("Yes")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        String final_attendance = Keys.SERVER + "addtospeakers/" + user_id + "&" + user_committee;
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            new HttpRequestTask(
                                    new HttpRequest(final_attendance, HttpRequest.GET),
                                    new HttpRequest.Handler() {

                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                String server_response = "You are in the speakers list now";
                                                Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            } else {
                                                String server_response = "Can't reach the server at the moment. Please try again later.";
                                                Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }
                                    }).execute();
                        }
                    })
                    .setNeutralText("No")
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Cancelled", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    })
                    //.setCancelable(true)
                    .setScrollable(true)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onQRFragmentInteraction(Uri string) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void formalsDialog(View v){
        new MaterialStyledDialog.Builder(this)
                .setTitle("Formal Socials")
                .setDescription("Are you attending the Formal Socials?")
                .setHeaderColor(R.color.dialog_header)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setNegativeText("Attending")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "formals/" + user_id + "&" + "Attending";
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.formalsView);
                                            attendance.setText("Attending");

                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                .setNeutralText("Not Attending")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "formals/" + user_id + "&" + "NotAttending";

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are not attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.formalsView);
                                            attendance.setText("Not Attending");
                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                //.setCancelable(true)
                .setScrollable(true)
                .show();

    }

    public void informalsDialog(View v){
        new MaterialStyledDialog.Builder(this)
                .setTitle("Informal Socials")
                .setDescription("Are you attending the Informal Socials?")
                .setHeaderColor(R.color.dialog_header)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setNegativeText("Attending")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "informals/" + user_id + "&" + "Attending";
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.formalsView);
                                            attendance.setText("Attending");

                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                .setNeutralText("Not Attending")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "informals/" + user_id + "&" + "NotAttending";

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are not attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.informalView);
                                            attendance.setText("Not Attending");
                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                //.setCancelable(true)
                .setScrollable(true)
                .show();

    }

    public void bfDialog(View v){
        new MaterialStyledDialog.Builder(this)
                .setTitle("Breakfast")
                .setDescription("Do you plan on having breakfast?")
                .setHeaderColor(R.color.dialog_header)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setNegativeText("Yes")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "breakfast/" + user_id + "&" + "Yes";
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.bfView);
                                            attendance.setText("Attending");

                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                .setNeutralText("No")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "breakfast/" + user_id + "&" + "No";

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are not attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.bfView);
                                            attendance.setText("Not Attending");
                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                //.setCancelable(true)
                .setScrollable(true)
                .show();

    }

    public void lunchDialog(View v){
        new MaterialStyledDialog.Builder(this)
                .setTitle("Lunch")
                .setDescription("Do you plan on having lunch?")
                .setHeaderColor(R.color.dialog_header)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setNegativeText("Yes")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "lunch/" + user_id + "&" + "Yes";
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                            TextView attendance = (TextView) findViewById(R.id.lunchView);
                                            attendance.setText("Attending");

                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                .setNeutralText("No")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    String final_attendance = Keys.SERVER + "lunch/" + user_id + "&" + "No";

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new HttpRequestTask(
                                new HttpRequest(final_attendance, HttpRequest.GET),
                                new HttpRequest.Handler() {

                                    @Override
                                    public void response(HttpResponse response) {
                                        if (response.code == 200) {
                                            String server_response = "You are not attending";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();

                                            TextView attendance = (TextView) findViewById(R.id.lunchView);
                                            attendance.setText("Not Attending");
                                        } else {
                                            String server_response = "Can't reach the server at the moment. Please try again later.";
                                            Snackbar.make(getWindow().getDecorView().getRootView(), server_response, Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                }).execute();
                    }
                })
                //.setCancelable(true)
                .setScrollable(true)
                .show();

    }

    public void fuDialog(View v){
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file

            final File file = new File(filePath);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            MultipartForm form = null;

            try {
                form = new MultipartForm()
                        .add("file", String.valueOf((new File(file.getName()))), Pipe.forFile(new File(file.getAbsolutePath())))
                        .add("identifier", user_id);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean showMinMax = false;
            final MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .title("Processing")
                    .content("Please wait while we upload and process your file")
                    .progress(false, 100, showMinMax)
                    .show();
            Bridge
                    .post(Keys.SERVER + "fileupload")
                    .cancellable(false)
                    .body(form)
                    .uploadProgress(new ProgressCallback() {
                        @Override
                        public void progress(Request request, int current, int total, int percent) {
                            dialog.setProgress(percent);
                        }
                    })
                    .request(new Callback() {
                        @Override
                        public void response(Request request, Response response, BridgeException e) {
                            // Use response
                            dialog.dismiss();
                            MaterialDialog dialog2 = new MaterialDialog.Builder(MainActivity.this)
                                    .title("File Upload")
                                    .content("The file you sent has been successfully uploaded and can now be seen by the EB.")
                                    .positiveText("Okay")
                                    .show();
                        }
                    })
                    .response();
        }
    }


    @Override
    public void onFileFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFeedbackFragmentInteraction(Uri uri) {

    }
}
