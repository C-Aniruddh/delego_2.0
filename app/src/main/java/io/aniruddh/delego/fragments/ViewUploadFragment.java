package io.aniruddh.delego.fragments;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.aniruddh.delego.Keys;
import io.aniruddh.delego.models.FileUpload;
import io.aniruddh.delego.tasker.NetworkClass;
import io.aniruddh.delego.R;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class ViewUploadFragment extends Fragment {

    private static final String TAG = ByCommitteeFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private List<FileUpload> delegateList;
    private StoreAdapter mAdapter;
    private String user_committee;

    TextView sessionName;
    String current_session;

    public ViewUploadFragment() {
        // Required empty public constructor
    }

    public static ViewUploadFragment newInstance(String param1, String param2) {
        ViewUploadFragment fragment = new ViewUploadFragment();
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
        String text = "Viewing uploads for " + user_committee + " :";
        sessionName.setText(text);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);


        fetchStoreItems();

        Toolbar toolbar = view.findViewById(R.id.toolbar);


        return view;
    }

    private void fetchStoreItems() {
        final String URL = Keys.SERVER + "viewuploads/" + user_committee ;
        JsonArrayRequest request = new JsonArrayRequest(URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getActivity(), "Couldn't fetch the delegate List! Please try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<FileUpload> items = new Gson().fromJson(response.toString(), new TypeToken<List<FileUpload>>() {
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
        private List<FileUpload> delegateList;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public TextView filename, country, name;
            public ImageView thumbnail;
            private ItemClickListener clickListener;
            public MyViewHolder(View view) {
                super(view);
                filename = view.findViewById(R.id.file_name);
                name = view.findViewById(R.id.delegate_committee_file);
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


        public StoreAdapter(Context context, List<FileUpload> delegateList) {
            this.context = context;
            this.delegateList = delegateList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.file_list_single, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final FileUpload delegate = delegateList.get(position);
            String text = "Uploaded by " + delegate.getName();
            holder.filename.setText(delegate.getFile());
            holder.name.setText(text);
            holder.country.setText(delegate.getCountry());

            holder.setClickListener(new ItemClickListener() {

                Uri url = Uri.parse(Keys.SERVER_CDN + "static/uploads/" + delegate.getFile());

                @Override
                public void onClick(View view, int position, boolean isLongClick) {

                    new MaterialStyledDialog.Builder(getActivity())
                            .setTitle("File")
                            .setDescription("What would you want to do with this file?")
                            .setHeaderColor(R.color.dialog_header)
                            .setStyle(Style.HEADER_WITH_TITLE)
                            .setNegativeText("Remove")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                String final_attendance = Keys.SERVER + "delfromuploads/" + delegate.getFile();
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    new HttpRequestTask(
                                            new HttpRequest(final_attendance, HttpRequest.GET),
                                            new HttpRequest.Handler() {

                                                @Override
                                                public void response(HttpResponse response) {
                                                    if (response.code == 200) {
                                                        String server_response = "File Removed";
                                                        fetchStoreItems();
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
                            .setNeutralText("Download")
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                    DownloadManager.Request r = new DownloadManager.Request(url);

                                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, delegate.getFile());

                                    r.allowScanningByMediaScanner();

                                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                                    DownloadManager dm = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                                    dm.enqueue(r);

                                    Snackbar.make(getView(), "Downloading...", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            })
                            //.setCancelable(true)
                            .setScrollable(true)
                            .show();

                }
            });
        }

        @Override
        public int getItemCount() {
            return delegateList.size();
        }

    }
}