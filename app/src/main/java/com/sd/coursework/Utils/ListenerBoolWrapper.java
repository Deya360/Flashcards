package com.sd.coursework.Utils;

import java.util.ArrayList;
import java.util.List;

public class ListenerBoolWrapper {
    private boolean currentState = false;
    private List<ChangeListener> listeners = new ArrayList<>();

    public boolean getState() {
        return currentState;
    }

    public void setState(boolean newState) {
        this.currentState = newState;

        for(ChangeListener l: listeners) {
            if (l != null) l.onChange(this.currentState);
        }
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void setListener(ChangeListener listener) {
        if(!listeners.contains(listener))
            this.listeners.add(listener);
    }

    public interface ChangeListener {
        void onChange(boolean state);
    }
}
