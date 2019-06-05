package com.sd.coursework.Utils;

import java.util.ArrayList;
import java.util.List;

public class ListenerIntWrapper {
    private int count = 0;
    private int maxSize = 0;

    private List<Integer> list = new ArrayList<>();
    private List<ChangeListener> listenersC = new ArrayList<>();
    private List<SelectListener> listenersS = new ArrayList<>();
    private List<DeleteListener> listenersD = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int newCount) {
        this.count = newCount;
        for(ChangeListener l: listenersC) {
            if (l != null) l.onChange(count);
        }
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        if(list!=null) {
            this.list = list;
            setCount(list.size());
        }
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void selectAll() {
        if (count != maxSize) {
            count = maxSize;

            for(SelectListener l: listenersS) {
                if (l != null) l.onSelectAll();
            }
        } else {
            selectNone();
        }
    }

    public void selectNone() {
        count = 0;

        for(SelectListener l: listenersS) {
            if (l != null) l.onSelectNone();
        }
    }

    public void delete() {
        for(DeleteListener l: listenersD) {
            if (l != null) l.onDelete(this.list);
        }
    }

    public void clearListeners() {
        listenersC.clear();
        listenersS.clear();
        listenersD.clear();
    }

    public void setListener(ChangeListener listener) {
        if(!listenersC.contains(listener)) this.listenersC.add(listener);
    }

    public void setListener(SelectListener listener) {
        if(!listenersS.contains(listener)) this.listenersS.add(listener);
    }

    public void setListener(DeleteListener listener) {
        if(!listenersD.contains(listener)) this.listenersD.add(listener);
    }

    public interface ChangeListener {
        void onChange(int selected_count);
    }
    public interface SelectListener {
        void onSelectAll();
        void onSelectNone();
    }
    public interface DeleteListener {
        void onDelete(List<Integer> itemsMap);
    }
}
