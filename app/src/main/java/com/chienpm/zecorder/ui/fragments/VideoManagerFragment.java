package com.chienpm.zecorder.ui.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.chienpm.zecorder.R;
import com.chienpm.zecorder.data.database.VideoDatabase;
import com.chienpm.zecorder.data.entities.Video;
import com.chienpm.zecorder.ui.adapters.VideoAdapter;
import com.chienpm.zecorder.ui.utils.MyUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoManagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoManagerFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "chienpm_log";
    private View mViewRoot;
    private Menu mMenu;
    ListView mListviewVideos;
    TextView mTvEmpty;
    private VideoAdapter mAdapter;

    // TODO: Rename and change types of parameters

    public VideoManagerFragment() {
        // Required empty public constructor
    }

    public static VideoManagerFragment newInstance(String param1, String param2) {
        VideoManagerFragment fragment = new VideoManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        mViewRoot = inflater.inflate(R.layout.fragment_video_manager, container, false);
        return mViewRoot;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.video_setting_menu, menu);
        mMenu= menu;
        final MenuItem item = menu.findItem(R.id.action_select_multiple);
        item.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = item.isChecked();
                item.setChecked(!isChecked);
                mAdapter.selectAll(!isChecked);
                mAdapter.showAllCheckboxes(true);
                Log.d(TAG, "onOptionsItemSelected: "+mAdapter.logSelection());
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateMenuItemsList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_sync:

                break;

            case R.id.action_rename:

                break;

            case R.id.action_share:

                break;

            case R.id.action_detail:

                break;

            case R.id.action_delete:
                requestConfirmDeletion();
                break;

            case R.id.action_cancel:
                mAdapter.showAllCheckboxes(false);
                mAdapter.selectAll(false);
                setMenuItemVisibility(R.id.action_select_multiple, false);
                break;

        }
        return false;
    }

    private void requestConfirmDeletion() {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Selected ?")
            .setMessage("Are you sure you want to delete this videos?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Continue with delete operation
                    mAdapter.deleteSelectedVideo();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMenu!=null)
            setMenuItemVisibility(R.id.action_select_multiple, false);
    }

    private void initViews() {
         mListviewVideos = (ListView) mViewRoot.findViewById(R.id.list_videos);
         mTvEmpty = mViewRoot.findViewById(R.id.tvEmpty);

         // Create textview to set empty state of listview
        mListviewVideos.setEmptyView(mTvEmpty);

        // Create a new {@link ArrayAdapter} of earthquakes: gắn cái datalist vào layout
        mAdapter = new VideoAdapter(
                getActivity(), new ArrayList<Video>());

        // Set the mAdapter on the {@link ListView}
        // so the list can be populated in the user interface
        mListviewVideos.setAdapter(mAdapter);

        handleListviewItemClickEvents();

        Log.d(TAG, "initLoader (activity create) called");
        getLoaderManager().initLoader(0, null, mLoadVideosCallback);

    }

    private void handleListviewItemClickEvents() {
        mListviewVideos.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    mAdapter.showAllCheckboxes(false);
            }
        });

        mListviewVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d("chienpm_log", "onCheckedChanged: "+mSelectedPositions.size());
                if(mAdapter.getSelectedPositionCount() == 0) {
                    Video video = mAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getLocalPath()));
                    intent.setDataAndType(Uri.parse(video.getLocalPath()), "video/mp4");
                    startActivity(intent);
                }
                else {
                    mAdapter.toggleSelectionAtPosition(position);
                }
                Log.d("chienpm_log", "selected: "+mAdapter.logSelection());
            }
        });

        mListviewVideos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.showAllCheckboxes(true);
                mAdapter.toggleSelectionAtPosition(position);
                setMenuItemVisibility(R.id.action_select_multiple, true);
                return false;
            }
        });
    }

    private void updateMenuItemsList() {
        int selectedMode = mAdapter.getSelectedMode();
        showMenuItems(false); //hide
        switch (selectedMode){

            case MyUtils.SELECTED_MODE_EMPTY:
                //hide all
                break;
            case MyUtils.SELECTED_MODE_ALL:
                setMenuItemVisibility(R.id.action_sync, true);
                setMenuItemVisibility(R.id.action_delete, true);
                setMenuItemVisibility(R.id.action_cancel, true);
                mMenu.findItem(R.id.action_sync).setTitle("Sync all");
                mMenu.findItem(R.id.action_delete).setTitle("Delete all");
                break;
            case MyUtils.SELECTED_MODE_MULTIPLE:
                setMenuItemVisibility(R.id.action_sync, true);
                setMenuItemVisibility(R.id.action_delete, true);
                setMenuItemVisibility(R.id.action_cancel, true);
                mMenu.findItem(R.id.action_sync).setTitle("Sync selected");
                mMenu.findItem(R.id.action_delete).setTitle("Delete selected");

                break;
            case MyUtils.SELECTED_MODE_SINGLE:
                showMenuItems(true);
                break;
        }
    }

    private void setMenuItemVisibility(int id, boolean value) {
        mMenu.findItem(id).setVisible(value);
    }

    private void showMenuItems(boolean value) {
        for(int i = 1; i < mMenu.size(); i++){
            mMenu.getItem(i).setVisible(value);
        }
    }


//    #Load data to list view
    private LoaderManager.LoaderCallbacks<ArrayList<Video>> mLoadVideosCallback = new LoaderManager.LoaderCallbacks<ArrayList<Video>>() {
        @NonNull
        @Override
        public Loader<ArrayList<Video>> onCreateLoader(int i, @Nullable Bundle bundle) {
            return new VideoAsyncTaskLoader(getContext());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<ArrayList<Video>> loader, ArrayList<Video> videos) {
            Log.d(TAG, "onLoadFinished: called");
            //clear the adapter of previous earthquake data
            mAdapter.clear();

            // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
            // data set. This will trigger the ListView to notifySettingChanged.
            if(videos != null && !videos.isEmpty()) {
                mAdapter.addAll(videos);
            }
            else{
                mTvEmpty.setText("No Video Recored");
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<ArrayList<Video>> loader) {
            Log.d(TAG, "onLoaderReset: called");
            mAdapter.clear();
        }
    };

    static class VideoAsyncTaskLoader extends AsyncTaskLoader<ArrayList<Video>>{
        WeakReference<Context> mWeakReference;
        public VideoAsyncTaskLoader(@NonNull Context context) {
            super(context);
            mWeakReference = new WeakReference<>(context);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Nullable
        @Override
        public ArrayList<Video> loadInBackground() {
            try {
                return (ArrayList<Video>) VideoDatabase.getInstance(mWeakReference.get()).getVideoDao().getAllVideo();
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "loadInBackground: "+e.getMessage());
                return null;
            }
        }

        @Override
        protected void onReset() {
            super.onReset();
        }
    }

}
