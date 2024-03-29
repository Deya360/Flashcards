package com.sd.coursework.Utils.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.jaredrummler.fastscrollrecyclerview.FastScrollRecyclerView;

public class EmptyFastScrollSupportedRecyclerView extends FastScrollRecyclerView {
    private View emptyView;
    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public EmptyFastScrollSupportedRecyclerView(Context context) {
        super(context);
    }

    public EmptyFastScrollSupportedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyFastScrollSupportedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        if (this.emptyView != null) {
            this.emptyView.setVisibility(GONE);
        }
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    public void setPlaceHolderView (View placeHolderView) {
        this.emptyView = placeHolderView;
        emptyView.setVisibility(VISIBLE);
    }
}
