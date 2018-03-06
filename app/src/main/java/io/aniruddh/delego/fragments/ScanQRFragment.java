package io.aniruddh.delego.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;
import io.aniruddh.delego.Keys;
import io.aniruddh.delego.MainActivity;
import io.aniruddh.delego.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScanQRFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScanQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanQRFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int flag = 0;


    private SurfaceView mySurfaceView;
    private QREader qrEader;

    private OnFragmentInteractionListener mListener;

    public ScanQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanQRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanQRFragment newInstance(String param1, String param2) {
        ScanQRFragment fragment = new ScanQRFragment();
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

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "Call");
        if(flag == 1){
            Intent restart = new  Intent(getActivity(), MainActivity.class);
            startActivity(restart);
        }
        qrEader.initAndStart(mySurfaceView);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Cleanup in onPause()
        // --------------------
        qrEader.releaseAndCleanup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_qr, container, false);

        mySurfaceView = (SurfaceView) view.findViewById(R.id.camera_view);
        qrEader = new QREader.Builder(getContext(), mySurfaceView, new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                Log.d("QREader", "Value : " + data);
                flag = 1;
                String process_url = Keys.SERVER + "user_details/" + data;
                Intent i = new Intent(getActivity(), UserDetails.class);
                i.putExtra("process_url", process_url);
                startActivity(i);
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(mySurfaceView.getHeight())
                .width(mySurfaceView.getWidth())
                .build();

        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onQRFragmentInteraction(uri);
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
        public void onQRFragmentInteraction(Uri string);
    }
}
