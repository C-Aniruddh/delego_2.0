package io.aniruddh.delego.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.ason.Ason;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.klinker.android.sliding.SlidingActivity;

import org.w3c.dom.Text;

import io.aniruddh.delego.Keys;
import io.aniruddh.delego.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserDetails extends SlidingActivity {


    String user_type;
    String user_identifier;
    TextView attendance_status;

    @Override
    public void init(Bundle savedInstanceState) {
        final String[] user_name = new String[1];
        final String[] user_phone = new String[1];
        final String[] user_email = new String[1];

        Bundle b = getIntent().getExtras();
        String process_url = b.getString("process_url");

        SharedPreferences prefs = getSharedPreferences(Keys.PREFERENCES, MODE_PRIVATE);
        user_type = prefs.getString("type", "user");



        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SanFranciscoDisplay-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setContent(R.layout.activity_user_share);

        if (user_type.contentEquals("oc")){
            setFab(getResources().getColor(R.color.colorAccent), R.drawable.check, userArrival);
        }

        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            user_name[0] = ason.getString("Name");
                            user_identifier = ason.getString("identifier");
                            setTitle(user_name[0]);
                            String user_image = ason.getString("Image");
                            Log.d("TAG", user_image);
                            setUserImage(user_image);
                            String user_committee = ason.getString("Committee");
                            TextView committee = (TextView) findViewById(R.id.committeeView);
                            committee.setText(user_committee);
                            String user_rsvp = ason.getString("RSVP");
                            attendance_status = (TextView)findViewById(R.id.attendanceView);
                            attendance_status.setText(user_rsvp);
                            String user_type = ason.getString("Country");
                            TextView type = (TextView) findViewById(R.id.countryView);
                            type.setText(user_type);
                            String user_id = ason.getString("Numid");
                            TextView id = (TextView) findViewById(R.id.idView);
                            id.setText(user_id);
                            String user_role = ason.getString("Role");
                            TextView role = (TextView) findViewById(R.id.roleView);
                            role.setText(user_role);
                            user_phone[0] = ason.getString("Phone");
                            TextView phone = (TextView) findViewById(R.id.phoneView);
                            phone.setText(user_phone[0]);
                            user_email[0] = ason.getString("Email");
                            TextView email = (TextView) findViewById(R.id.emailView);
                            email.setText(user_email[0]);
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            TextView textView = (TextView) findViewById(R.id.idView);
                            textView.setText(ai_response);
                        }
                    }
                }).execute();

    }


    public void setUserImage(String url) {

        Glide.with(this)
                .load(url)
                .into((ImageView) findViewById(R.id.photo));

    }

    private View.OnClickListener userArrival = new View.OnClickListener() {
        public void onClick(View v) {
            // do something
            String process_url = Keys.SERVER + "user_arrival/" + user_identifier;
            new HttpRequestTask(
                    new HttpRequest(process_url, HttpRequest.GET),
                    new HttpRequest.Handler() {

                        @Override
                        public void response(HttpResponse response) {
                            if (response.code == 200) {
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Arrived", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                attendance_status.setText("Arrived");
                            } else {
                                String ai_response = "Can't reach the server at the moment. Please try again later.";
                                Snackbar.make(getWindow().getDecorView().getRootView(), ai_response, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    }).execute();

        }
    };


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    

}
