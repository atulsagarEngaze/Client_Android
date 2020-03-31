package com.redtop.engaze.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redtop.engaze.R;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DraftEventsFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_draft_events, container, false);
		return v;
	}
}
