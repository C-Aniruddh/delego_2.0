package io.aniruddh.delego.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.ason.Ason;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import io.aniruddh.delego.Keys;
import io.aniruddh.delego.R;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FoodFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FoodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String user_type;
    String user_id;
    String full_name;
    String background;

    private OnFragmentInteractionListener mListener;

    public FoodFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FoodFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FoodFragment newInstance(String param1, String param2) {
        FoodFragment fragment = new FoodFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        SharedPreferences prefs = getActivity().getSharedPreferences(Keys.PREFERENCES, MODE_PRIVATE);
        user_type = prefs.getString("type", "user");
        user_id = prefs.getString("identifier", "default");
        full_name = prefs.getString("full_name", "Delegate Name");
        background = prefs.getString("background", "college");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_food, container, false);

        TextView greeting = (TextView) view.findViewById(R.id.greeting_prefs);
        String greet = "Hi " + full_name + "! You can set your food preferences here!";
        greeting.setText(greet);

        // String[] process_urls = new String[]{"getlunch", "getbreakfast", "getinformals", "getformals"};

        CardView informals = (CardView) view.findViewById(R.id.informals_viewgroup);

        if(background.contentEquals("school")){
            informals.setVisibility(View.INVISIBLE);
        }

        String process_url = Keys.SERVER + "getlunch/" + user_id;
        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            String status = ason.getString("status");
                            TextView textView = (TextView) view.findViewById(R.id.lunchView);
                            textView.setText(status);
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            TextView textView = (TextView) view.findViewById(R.id.lunchView);
                            textView.setText(ai_response);
                        }
                    }
                }).execute();

        process_url = Keys.SERVER + "getbreakfast/" + user_id;
        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            String status = ason.getString("status");
                            TextView textView = (TextView) view.findViewById(R.id.bfView);
                            textView.setText(status);
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            TextView textView = (TextView) view.findViewById(R.id.bfView);
                            textView.setText(ai_response);
                        }
                    }
                }).execute();

        process_url = Keys.SERVER + "getformals/" + user_id;
        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            String status = ason.getString("status");
                            TextView textView = (TextView) view.findViewById(R.id.formalsView);
                            textView.setText(status);
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            TextView textView = (TextView) view.findViewById(R.id.formalsView);
                            textView.setText(ai_response);
                        }
                    }
                }).execute();

        process_url = Keys.SERVER + "getinformals/" + user_id;
        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            String status = ason.getString("status");
                            TextView textView = (TextView) view.findViewById(R.id.informalView);
                            textView.setText(status);
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            TextView textView = (TextView) view.findViewById(R.id.informalView);
                            textView.setText(ai_response);
                        }
                    }
                }).execute();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFileFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFileFragmentInteraction(Uri uri);
    }


}
