package com.shomazzapp.vavilonWalls.View.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.shomazzapp.vavilonWalls.Presenter.WallsListPresenter;
import com.shomazzapp.vavilonWalls.Utils.FragmentRegulator;
import com.shomazzapp.vavilonWalls.Utils.NetworkHelper;
import com.shomazzapp.vavilonWalls.Utils.WallsLoader;
import com.shomazzapp.vavilonWalls.View.Adapters.SavedWallsViewAdapter;
import com.shomazzapp.vavilonWalls.View.Adapters.WallsViewAdapter;
import com.shomazzapp.walls.R;
import com.vk.sdk.api.model.VKApiPhoto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WallsListFragment extends Fragment implements WallsLoader, SwipeRefreshLayout.OnRefreshListener {

    public static String log = "wallslist";
    @BindView(R.id.walls_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_to_refresh_walls)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.network_lay_saved_walls)
    RelativeLayout networkLay;
    @BindView(R.id.tView)
    TextView textView;
    private int albumID;
    private boolean isForSavedWalls;
    private FragmentRegulator fragmentRegulator;
    private WallsViewAdapter wallsViewAdapter;
    private SavedWallsViewAdapter savedWallsViewAdapter;
    private Context context;
    private WallsListPresenter presenter;
    private View mainView;
    private RecyclerView.LayoutManager layoutManager;
    private HashSet<Integer> ids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mainView == null) {
            mainView = inflater.inflate(R.layout.fragment_walls_list, container, false);
            ButterKnife.bind(this, mainView);
            init();
        }
        if (!isForSavedWalls)
            loadAlbum(albumID, 0);
        else loadSavedWalls();
        return mainView;
    }

    @Override
    public boolean isNewCategory() {
        return presenter.isNewCategory();
    }

    @Override
    public ArrayList<VKApiPhoto> getWallsByCategory(int albumID, int offset) {
        return presenter.getWallsByCategory(albumID, offset, ids);
    }

    public void onNetworkChanged(boolean succes) {
        textView.setText(isForSavedWalls ? getResources().getString(R.string.no_saved_walls)
                : getResources().getString(R.string.no_connection));
        if (succes) {
            networkLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            networkLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    public void setFragmentRegulator(FragmentRegulator changer) {
        this.fragmentRegulator = changer;
    }

    public void loadAlbum(int albumID, int offset) {
        if (presenter != null)
            presenter.clearHidedWallsCount();
        presenter.loadWallByCategory(albumID, offset, ids);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fragmentRegulator != null) fragmentRegulator.hide();
        getActivity().findViewById(R.id.appodealBannerView).setBackgroundColor(
                getResources().getColor(R.color.app_overlay));
    }

    public void loadSavedWalls() {
        if (presenter != null)
            presenter.loadSavedWalls();
    }

    public void scrolToPosition(int position) {
        if (recyclerView != null)
            recyclerView.smoothScrollToPosition(position);
    }

    public void updateData(ArrayList<VKApiPhoto> walls) {
        if (NetworkHelper.isOnLine(this.context)) {
            wallsViewAdapter.updateData(walls);
            recyclerView.smoothScrollToPosition(0);
            onNetworkChanged(true);
        } else {
            onNetworkChanged(false);
            Toast.makeText(this.context, getResources().getString(R.string.error_network_msg),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void updateSavedWallsData(ArrayList<File> walls) {
        onNetworkChanged(true);
        if (walls.size() < 1) onNetworkChanged(false);
        savedWallsViewAdapter.updateData(walls);
        recyclerView.smoothScrollToPosition(0);
    }

    public void init() {
        layoutManager = new GridLayoutManager(context, 3);
        //  layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        presenter = new WallsListPresenter(this);
        if (!isForSavedWalls) {
            wallsViewAdapter = new WallsViewAdapter(context, fragmentRegulator, this);
            recyclerView.setAdapter(wallsViewAdapter);
        } else {
            savedWallsViewAdapter = new SavedWallsViewAdapter(context, fragmentRegulator, this);
            recyclerView.setAdapter(savedWallsViewAdapter);
        }
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.pink_color),
                getResources().getColor(R.color.blue_color));
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.app_overlay);
    }

    public void changeToInternetWalls() {
        setForSavedWalls(false);
        if (mainView != null) {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 3);
            // layoutManager.setAutoMeasureEnabled(true);
            recyclerView.setLayoutManager(layoutManager);
            presenter = new WallsListPresenter(this);
            wallsViewAdapter = new WallsViewAdapter(context, null, this);
            recyclerView.setAdapter(wallsViewAdapter);
        }
    }

    public void changeToSavedWalls() {
        setForSavedWalls(true);
        if (mainView != null) {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 3);
            //layoutManager.setAutoMeasureEnabled(true);
            recyclerView.setLayoutManager(layoutManager);
            presenter = new WallsListPresenter(this);
            savedWallsViewAdapter = new SavedWallsViewAdapter(context, fragmentRegulator, this);
            recyclerView.setAdapter(savedWallsViewAdapter);
        }
    }

    public void setForSavedWalls(boolean b) {
        this.isForSavedWalls = b;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isForSavedWalls = false;
        Glide.get(context).clearMemory();
    }

    @Override
    public void loadVKWallpaperFragment(ArrayList<VKApiPhoto> walls,
                                        int currentPosition) {
        fragmentRegulator.loadVKWallpaperFragment(walls, currentPosition, this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (wallsViewAdapter != null) wallsViewAdapter.setLoaded(false);
        transaction.replace(R.id.children_fragment_frame, fragmentRegulator.getWallpaperFragment()).commit();
    }

    @Override
    public void loadSavedWallpaperFragment(ArrayList<File> walls, int currentPosition) {
        fragmentRegulator.loadSavedWallpaperFragment(walls, currentPosition, this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.children_fragment_frame, fragmentRegulator.getWallpaperFragment()).commit();
    }

    @Override
    public void loadSavedWallpaperFragment(int currentPosition) {
        loadSavedWallpaperFragment(savedWallsViewAdapter.getWallpapers(), currentPosition);
    }

    @Override
    public void closeWallpaperFragment() {
        fragmentRegulator.closeWallpaperFragment();
        getChildFragmentManager().beginTransaction().remove(fragmentRegulator.getWallpaperFragment()).commit();
        for (int i = 0; i < getChildFragmentManager().getBackStackEntryCount(); ++i) {
            getChildFragmentManager().popBackStack();
        }
    }

    public void setIdsHashSet(HashSet<Integer> ids) {
        this.ids = ids;
    }

    public int getAlbumID() {
        return albumID;
    }

    public void setAlbumID(int id) {
        this.albumID = id;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isForSavedWalls) loadAlbum(albumID, 0);
                else loadSavedWalls();
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
                if (!isForSavedWalls)
                    wallsViewAdapter.setFullAlbumLoaded(false);
            }
        }, 200);
    }
}
