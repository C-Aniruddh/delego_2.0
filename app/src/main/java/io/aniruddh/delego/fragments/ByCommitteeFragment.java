package io.aniruddh.delego.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.afollestad.ason.Ason;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.bumptech.glide.Glide;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.aniruddh.delego.Keys;
import io.aniruddh.delego.models.CommitteeDelegate;
import io.aniruddh.delego.tasker.NetworkClass;
import io.aniruddh.delego.R;

import static android.content.Context.MODE_PRIVATE;

public class ByCommitteeFragment extends Fragment {

    private static final String TAG = ByCommitteeFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private List<CommitteeDelegate> delegateList;
    private StoreAdapter mAdapter;
    private String user_committee;

    TextView sessionName;
    String current_session;

    public ByCommitteeFragment() {
        // Required empty public constructor
    }

    public static ByCommitteeFragment newInstance(String param1, String param2) {
        ByCommitteeFragment fragment = new ByCommitteeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getContext().getSharedPreferences(Keys.PREFERENCES, MODE_PRIVATE);
        user_committee = prefs.getString("committee", "none");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_by_committee, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        delegateList = new ArrayList<>();
        mAdapter = new StoreAdapter(getActivity(), delegateList);
        sessionName = (TextView) view.findViewById(R.id.sessionName);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        String process_url = Keys.SERVER + "current_session";
        new HttpRequestTask(
                new HttpRequest(process_url, HttpRequest.GET),
                new HttpRequest.Handler() {

                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Ason ason = new Ason(response.body);
                            String status = ason.getString("session");
                            String current = "Current session : " + status;
                            current_session = status;
                            sessionName.setText(current);
                        } else {
                            String ai_response = "Can't reach the server at the moment. Please try again later.";
                            sessionName.setText(ai_response);
                        }
                    }
                }).execute();

        fetchStoreItems();

        Toolbar toolbar = view.findViewById(R.id.toolbar);


        return view;
    }

    private void fetchStoreItems() {
        final String URL = Keys.SERVER + "by_committee/" + user_committee ;
        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getActivity(), "Couldn't fetch the delegate List! Please try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<CommitteeDelegate> items = new Gson().fromJson(response.toString(), new TypeToken<List<CommitteeDelegate>>() {
                        }.getType());

                        delegateList.clear();
                        delegateList.addAll(items);

                        // refreshing recycler view
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        NetworkClass.getInstance().addToRequestQueue(request);
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }

    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {
        private Context context;
        private List<CommitteeDelegate> delegateList;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView name, country;
            public ImageView thumbnail;
            private ItemClickListener clickListener;
            public MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.delegate_name);
                country = view.findViewById(R.id.country_name);
                thumbnail = view.findViewById(R.id.thumbnail);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                clickListener.onClick(view, getPosition(), false);
            }

            public void setClickListener(ItemClickListener itemClickListener) {
                this.clickListener = itemClickListener;
            }
        }


        public StoreAdapter(Context context, List<CommitteeDelegate> delegateList) {
            this.context = context;
            this.delegateList = delegateList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.committee_delegate_single, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final CommitteeDelegate delegate = delegateList.get(position);
            holder.name.setText(delegate.getName());
            holder.country.setText(delegate.getCountry());

            Glide.with(context)
                    .load(delegate.getImage())
                    .into(holder.thumbnail);

            holder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    /*String name = ((TextView) recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.delegate_name)).getText().toString();
                    String delegate_identifier = delegate.getIdentifier();
                    String process_url = Keys.SERVER + "user_details/" + delegate_identifier;

                    Intent i = new Intent(getActivity(), UserDetails.class);
                    i.putExtra("process_url", process_url);
                    startActivity(i);
                    Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
                    Log.d("TAG", String.valueOf(delegateList.get(position)));*/

                    String delegate_identifier = delegate.getIdentifier();
                    makeAttendanceDialog(delegate_identifier);
                }
            });
        }

        @Override
        public int getItemCount() {
            return delegateList.size();
        }

        public void makeAttendanceDialog(final String user_id) {
            new MaterialStyledDialog.Builder(getActivity())
                    .setTitle("Set Attendance")
                    .setDescription("Is the delegate present and voting, present or absent?")
                    .setHeaderColor(R.color.dialog_header)
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setPositiveText("Present and Voting")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {

                        String final_attendance = Keys.SERVER + "set_attendance/" + user_id + "&Present%20And%20Voting&" + current_session;

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            new HttpRequestTask(
                                    new HttpRequest(final_attendance, HttpRequest.GET),
                                    new HttpRequest.Handler() {

                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                String server_response = "The delegate is Present and Voting";
                                                Snackbar.make(getView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();

                                            } else {
                                                String server_response = "Can't reach the server at the moment. Please try again later.";
                                                Snackbar.make(getView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }
                                    }).execute();
                        }
                    })
                    .setNegativeText("Present")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        String final_attendance = Keys.SERVER + "set_attendance/" + user_id + "&Present&" + current_session;

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            new HttpRequestTask(
                                    new HttpRequest(final_attendance, HttpRequest.GET),
                                    new HttpRequest.Handler() {

                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                String server_response = "The delegate has been marked Present";
                                                Snackbar.make(getView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();

                                            } else {
                                                String server_response = "Can't reach the server at the moment. Please try again later.";
                                                Snackbar.make(getView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }
                                    }).execute();
                        }
                    })
                    .setNeutralText("Absent")
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        String final_attendance = Keys.SERVER + "set_attendance/" + user_id +  "&Absent&" + current_session;

                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            new HttpRequestTask(
                                    new HttpRequest(final_attendance, HttpRequest.GET),
                                    new HttpRequest.Handler() {

                                        @Override
                                        public void response(HttpResponse response) {
                                            if (response.code == 200) {
                                                String server_response = "The delegate has been marked Absent";
                                                Snackbar.make(getView(), server_response, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();

                                            } else {
                                                String server_response = "Can't reach the server at the moment. Please try again later.";
                                                Snackbar.make(getView(), server_response, Snackbar.LENGTH_LONG)
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
    }
}