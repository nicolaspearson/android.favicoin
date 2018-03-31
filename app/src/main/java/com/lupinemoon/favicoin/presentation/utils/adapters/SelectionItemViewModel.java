package com.lupinemoon.favicoin.presentation.utils.adapters;

import android.databinding.Bindable;

import com.lupinemoon.favicoin.data.models.KeyValue;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericItemSelected;

public class SelectionItemViewModel extends BaseViewModel {

    private KeyValue item;

    private GenericItemSelected<KeyValue> itemSelectedCallback;

    SelectionItemViewModel(GenericItemSelected<KeyValue> itemSelectedCallback) {
        this.itemSelectedCallback = itemSelectedCallback;
    }

    void setItem(KeyValue item) {
        this.item = item;
        notifyChange();
    }

    @Override
    public BaseViewModel.State getInstanceState() {
        // Not required
        return null;
    }

    public void onItemClicked() {
        if (itemSelectedCallback != null) {
            itemSelectedCallback.onItemSelected(item);
        }
    }

    @Bindable
    public String getValue() {
        return item.getValue();
    }
}
