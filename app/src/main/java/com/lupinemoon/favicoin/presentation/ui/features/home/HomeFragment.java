package com.lupinemoon.favicoin.presentation.ui.features.home;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.lupinemoon.favicoin.databinding.FragmentHomeBinding;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPFragment;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.ui.features.home.adapters.CoinItemAdapter;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.OnBackPressedListener;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class HomeFragment extends BaseVMPFragment<HomeContract.ViewModel, HomeContract.Presenter, FragmentHomeBinding> implements HomeContract.View {

    public static final String TAG = HomeFragment.class.getSimpleName();

    private CoinItemAdapter coinItemAdapter;

    private boolean loading = false;

    public static HomeFragment instance(AppCompatActivity activity, Bundle args) {
        HomeFragment homeFragment = (HomeFragment) activity.getSupportFragmentManager().findFragmentByTag(
                HomeFragment.TAG);

        if (homeFragment == null) {
            // Create the fragment
            homeFragment = new HomeFragment();
            homeFragment.setArguments(args);
        }

        return homeFragment;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.fragment_home;
    }

    @Override
    public int getToolbarTitle() {
        return R.string.toolbar_title_home;
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
    public HomeViewModel createViewModel(@Nullable BaseViewModel.State savedViewModelState) {
        // Create the view model
        return new HomeViewModel(this, savedViewModelState);
    }

    @Nullable
    @Override
    public HomePresenter createPresenter(@NonNull Bundle args) {
        // Create the presenter
        return new HomePresenter(this, args);
    }

    @Nullable
    @Override
    public FragmentHomeBinding createBinding(
            @NonNull LayoutInflater inflater,
            int layoutId,
            @Nullable ViewGroup parent,
            boolean attachToParent) {
        return DataBindingUtil.inflate(inflater, getContentViewResource(), parent, false);
    }

    @Override
    public FragmentHomeBinding getBinding() {
        return super.getBinding();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Disable animations to avoid view flicker when we modify list items
        if (getBinding().homeRecyclerView.getItemAnimator() != null) {
            getBinding().homeRecyclerView.getItemAnimator().setAddDuration(0);
            getBinding().homeRecyclerView.getItemAnimator().setRemoveDuration(0);
            getBinding().homeRecyclerView.getItemAnimator().setMoveDuration(0);
            getBinding().homeRecyclerView.getItemAnimator().setChangeDuration(0);

            ((SimpleItemAnimator) getBinding().homeRecyclerView.getItemAnimator()).setSupportsChangeAnimations(
                    false);
        }

        getBinding().homeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        getBinding().homeRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false));

        getBinding().homeSwipeRefreshLayout.setColorSchemeResources(R.color.color_accent);
        getBinding().homeSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fetchCoinItems(
                true,
                0));

        // Set the view model variable
        getBinding().setViewModel((HomeViewModel) getViewModel());

        return getBinding().getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPresenter().fetchCoinItems(true, Constants.DRAWER_CLOSED_TIME_INTERVAL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Constants.INTENT_REQUEST_CODE_RETURNED_COIN_ITEM) {
            if (resultCode == Activity.RESULT_OK && intent != null && intent.getExtras() != null) {
                CoinItem returnedCoinItem = Parcels.unwrap(intent.getExtras().getParcelable(
                        Constants.INTENT_COIN_ITEM_DETAIL));
                if (returnedCoinItem != null) {
                    this.getPresenter().updateCoinItem(returnedCoinItem);
                    if (this.coinItemAdapter != null) {
                        this.coinItemAdapter.updateCoinItem(returnedCoinItem);
                    }
                }
            }
        }
    }

    @Override
    public Fragment getFragment() {
        return this;
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
            getBinding().homeSwipeRefreshLayout.setVisibility(View.GONE);
            toggleEmptyView(true);
        }
        if (!getBinding().homeSwipeRefreshLayout.isRefreshing()) {
            togglePopupLoader(true);
        }
    }

    @Override
    public void hideLoading() {
        loading = false;
        getBinding().homeSwipeRefreshLayout.setVisibility(View.VISIBLE);
        if (!getBinding().homeSwipeRefreshLayout.isRefreshing()) {
            togglePopupLoader(false);
        } else {
            getBinding().homeSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showNetworkErrorLayout(final GenericCallback genericCallback) {
        getBinding().noNetworkLayout.noNetworkButton.setOnClickListener(v -> genericCallback.execute());
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
            coinItemAdapter = new CoinItemAdapter(this, coinItems);
            getBinding().homeRecyclerView.setAdapter(coinItemAdapter);
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