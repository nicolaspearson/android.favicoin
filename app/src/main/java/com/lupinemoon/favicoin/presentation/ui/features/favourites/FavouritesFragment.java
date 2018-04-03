package com.lupinemoon.favicoin.presentation.ui.features.favourites;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lupinemoon.favicoin.BuildConfig;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.FragmentFavouritesBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPFragment;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.ui.features.favourites.adapters.FavCoinItemAdapter;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.widgets.OnBackPressedListener;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FavouritesFragment extends BaseVMPFragment<FavouritesContract.ViewModel, FavouritesContract.Presenter, FragmentFavouritesBinding> implements FavouritesContract.View {

    public static final String TAG = FavouritesFragment.class.getSimpleName();

    private FavCoinItemAdapter coinItemAdapter;

    private boolean loading = false;

    public static FavouritesFragment instance(AppCompatActivity activity, Bundle args) {
        FavouritesFragment favouritesFragment = (FavouritesFragment) activity.getSupportFragmentManager().findFragmentByTag(
                FavouritesFragment.TAG);

        if (favouritesFragment == null) {
            // Create the fragment
            favouritesFragment = new FavouritesFragment();
            favouritesFragment.setArguments(args);
        }

        return favouritesFragment;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.fragment_favourites;
    }

    @Override
    public int getToolbarTitle() {
        return R.string.toolbar_title_favourites;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public boolean monitorOfflineMode() {
        return true;
    }

    @Override
    public OnBackPressedListener getOnBackPressedListener() {
        // Not required, implemented in the landing activity
        return null;
    }

    @Nullable
    @Override
    public FavouritesViewModel createViewModel(@Nullable BaseViewModel.State savedViewModelState) {
        // Create the view model
        return new FavouritesViewModel(this, savedViewModelState);
    }

    @Nullable
    @Override
    public FavouritesPresenter createPresenter(@NonNull Bundle args) {
        // Create the presenter
        return new FavouritesPresenter(this, args);
    }

    @Nullable
    @Override
    public FragmentFavouritesBinding createBinding(
            @NonNull LayoutInflater inflater,
            int layoutId,
            @Nullable ViewGroup parent,
            boolean attachToParent) {
        return DataBindingUtil.inflate(inflater, getContentViewResource(), parent, false);
    }

    @Override
    public FragmentFavouritesBinding getBinding() {
        return super.getBinding();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Disable animations to avoid view flicker when we modify list items
        if (getBinding().favouritesRecyclerView.getItemAnimator() != null) {
            getBinding().favouritesRecyclerView.getItemAnimator().setAddDuration(0);
            getBinding().favouritesRecyclerView.getItemAnimator().setRemoveDuration(0);
            getBinding().favouritesRecyclerView.getItemAnimator().setMoveDuration(0);
            getBinding().favouritesRecyclerView.getItemAnimator().setChangeDuration(0);

            ((SimpleItemAnimator) getBinding().favouritesRecyclerView.getItemAnimator()).setSupportsChangeAnimations(
                    false);
        }

        getBinding().favouritesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (!loading && totalItemCount <= (lastVisibleItem + BuildConfig.COIN_PAGE_LOAD_THRESHOLD)) {
                    loading = true;
                    getPresenter().loadMore(totalItemCount);
                }
            }
        });

        getBinding().favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        getBinding().favouritesSwipeRefreshLayout.setColorSchemeResources(R.color.color_accent);
        getBinding().favouritesSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPresenter().fetchCoinItems(true, 0);
            }
        });

        // Set the view model variable
        getBinding().setViewModel((FavouritesViewModel) getViewModel());

        return getBinding().getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPresenter().fetchCoinItems(true, Constants.DRAWER_CLOSED_TIME_INTERVAL);
    }

    @Override
    public void showLoading() {
        loading = true;
        // We hide the recycler view, because initially we have no data
        // to display. All calls except fetchCoinItems() should be async
        // and performed in the background and therefore should not show
        // the popup loader.
        toggleNoNetworkView(true);
        if (coinItemAdapter == null) {
            getBinding().favouritesSwipeRefreshLayout.setVisibility(View.GONE);
            toggleEmptyView(true);
        }
        if (!getBinding().favouritesSwipeRefreshLayout.isRefreshing()) {
            togglePopupLoader(true);
        }
    }

    @Override
    public void hideLoading() {
        loading = false;
        getBinding().favouritesSwipeRefreshLayout.setVisibility(View.VISIBLE);
        if (!getBinding().favouritesSwipeRefreshLayout.isRefreshing()) {
            togglePopupLoader(false);
        } else {
            getBinding().favouritesSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showNetworkErrorLayout(final GenericCallback genericCallback) {
        getBinding().noNetworkLayout.noNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genericCallback.execute();
            }
        });
        toggleNoNetworkView(false);
    }

    private void toggleEmptyView(boolean hide) {
        getBinding().emptyLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    private void toggleNoNetworkView(boolean hide) {
        View noNetworkView = getBinding().noNetworkLayout.getRoot();
        noNetworkView.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    public List<CoinItem> getCoinItems() {
        if (coinItemAdapter != null) {
            return coinItemAdapter.getCoinItems();
        }
        return new ArrayList<>();
    }

    @Override
    public void setCoinItems(List<CoinItem> coinItems, boolean refresh) {
        toggleEmptyView(coinItems.size() > 0);
        toggleNoNetworkView(true);
        togglePopupLoader(false);
        Timber.d("setCoinItems: %s", coinItems);
        if (coinItemAdapter == null) {
            coinItemAdapter = new FavCoinItemAdapter(this, coinItems);
            getBinding().favouritesRecyclerView.setAdapter(coinItemAdapter);
            return;
        }

        if (!refresh && coinItemAdapter.getItemCount() > 0) {
            addCoinItems(coinItems);
        } else {
            coinItemAdapter.setCoinItems(coinItems);
        }
    }

    @Override
    public void addCoinItems(List<CoinItem> coinItems) {
        toggleEmptyView(coinItems.size() > 0);
        toggleNoNetworkView(true);
        Timber.d("addCoinItems: %s", coinItems);
        if (coinItemAdapter != null) {
            coinItemAdapter.addCoinItems(coinItems);
        } else {
            setCoinItems(coinItems, true);
        }
    }
}