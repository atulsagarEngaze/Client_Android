package com.redtop.engaze.Interface;

import androidx.fragment.app.Fragment;

public interface FragmentToActivity<TObject> {
    void communicate(TObject comm, Fragment source);
}

