package io.aniruddh.delego.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.ason.Ason;
import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Form;
import com.afollestad.bridge.Request;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;

import org.w3c.dom.Text;

import io.aniruddh.delego.Keys;
import io.aniruddh.delego.R;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedbackFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedbackFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String user_id;

    RatingBar rating;
    RatingBar rating2;
    RatingBar rating3;
    RatingBar rating4;
    RatingBar rating5;

    TextView food;
    TextView logistics;
    TextView moderation;
    TextView debate;
    TextView accomodation;

    Button submit;

    private OnFragmentInteractionListener mListener;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedbackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedbackFragment newInstance(String param1, String param2) {
        FeedbackFragment fragment = new FeedbackFragment();
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
        user_id = prefs.getString("identifier", "default");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        rating = (RatingBar) view.findViewById(R.id.food_rating);
        rating2 = (RatingBar) view.findViewById(R.id.logistics_rating);
        rating3 = (RatingBar) view.findViewById(R.id.debate_rating);
        rating4 = (RatingBar) view.findViewById(R.id.moderation_rating);
        rating5 = (RatingBar) view.findViewById(R.id.accomodation_rating);

        food = (TextView) view.findViewById(R.id.food_fb_View);
        logistics = (TextView) view.findViewById(R.id.logistics_fb_View);
        debate = (TextView) view.findViewById(R.id.debate_fb_View);
        moderation = (TextView) view.findViewById(R.id.moderation_fb_View);
        accomodation = (TextView) view.findViewById(R.id.accomodation_fb_View);


        String process_url = Keys.SERVER + "checkfeedback/" + user_id;
        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            String status = ason.getString("status");
                            if (status.contentEquals("Not Set")){
                                food.setVisibility(View.INVISIBLE);
                                logistics.setVisibility(View.INVISIBLE);
                                debate.setVisibility(View.INVISIBLE);
                                moderation.setVisibility(View.INVISIBLE);
                                accomodation.setVisibility(View.INVISIBLE);
                            } else if(status.contentEquals("Already submitted")){
                                rating.setVisibility(View.INVISIBLE);
                                rating2.setVisibility(View.INVISIBLE);
                                rating3.setVisibility(View.INVISIBLE);
                                rating4.setVisibility(View.INVISIBLE);
                                rating5.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            food.setText(ai_response);
                            logistics.setText(ai_response);
                            debate.setText(ai_response);
                            moderation.setText(ai_response);
                            accomodation.setText(ai_response);
                        }
                    }
                }).execute();

        submit = (Button) view.findViewById(R.id.submitFeedback);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Keys.SERVER + "submitfeedback";

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                Form form = new Form()
                        .add("user_id", user_id)
                        .add("rating", String.valueOf(rating.getRating()))
                        .add("2-rating", String.valueOf(rating2.getRating()))
                        .add("3-rating", String.valueOf(rating3.getRating()))
                        .add("4-rating", String.valueOf(rating4.getRating()))
                        .add("5-rating", String.valueOf(rating5.getRating()));
                try {
                    Request request = Bridge
                            .post(url)
                            .body(form)
                            .request();
                } catch (BridgeException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFeedbackFragmentInteraction(uri);
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
        void onFeedbackFragmentInteraction(Uri uri);
    }
}
