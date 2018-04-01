package com.lupinemoon.favicoin.presentation.ui.features.landing.home;

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

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.FragmentHomeBinding;
import com.lupinemoon.favicoin.presentation.services.rxbus.RxBus;
import com.lupinemoon.favicoin.presentation.services.rxbus.events.UpdatedCoinsEvent;
import com.lupinemoon.favicoin.presentation.ui.base.BaseVMPFragment;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.ui.features.landing.home.adapters.CoinItemAdapter;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.widgets.OnBackPressedListener;
import com.lupinemoon.favicoin.presentation.widgets.WrapContentLinearLayoutManager;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class HomeFragment extends BaseVMPFragment<HomeContract.ViewModel, HomeContract.Presenter, FragmentHomeBinding> implements HomeContract.View {

    public static final String TAG = HomeFragment.class.getSimpleName();

    private CoinItemAdapter coinItemAdapter;

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
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Subscribe to RxBus events
        createRxBusSubscriptions();

        // Disable animations to avoid view flicker when we modify list items
        if (getBinding().homeRecyclerView.getItemAnimator() != null) {
            getBinding().homeRecyclerView.getItemAnimator().setAddDuration(0);
            getBinding().homeRecyclerView.getItemAnimator().setRemoveDuration(0);
            getBinding().homeRecyclerView.getItemAnimator().setMoveDuration(0);
            getBinding().homeRecyclerView.getItemAnimator().setChangeDuration(0);

            ((SimpleItemAnimator) getBinding().homeRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        getBinding().homeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    getPresenter().loadMore(totalItemCount);
                }
            }
        });

        getBinding().homeRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));

        getBinding().homeSwipeRefreshLayout.setColorSchemeResources(R.color.color_accent);
        getBinding().homeSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPresenter().fetchCoinItems(true, 0);
            }
        });

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
    public void showLoading() {
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
        getBinding().homeSwipeRefreshLayout.setVisibility(View.VISIBLE);
        if (!getBinding().homeSwipeRefreshLayout.isRefreshing()) {
            togglePopupLoader(false);
        } else {
            getBinding().homeSwipeRefreshLayout.setRefreshing(false);
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
        }
    }

    @Override
    public void updateCoinItem(CoinItem coinItem) {
        if (coinItemAdapter != null) {
            coinItemAdapter.updateCoinItem(coinItem);
        }
    }

    private void createRxBusSubscriptions() {
        rxBusEvents.add(
                RxBus.getDefault().observeEvents(UpdatedCoinsEvent.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<UpdatedCoinsEvent>() {
                                    @Override
                                    public void accept(UpdatedCoinsEvent updatedCoinsEvent) {
                                        Timber.d("Updated Coins Event");
                                        if (getPresenter() != null &&
                                                updatedCoinsEvent != null &&
                                                updatedCoinsEvent.getCoins() != null &&
                                                updatedCoinsEvent.getCoins().getCoinItems() != null) {
                                            getPresenter().updateCoinItems(updatedCoinsEvent.getCoins());
                                        }
                                    }
                                })
                       );
    }
}