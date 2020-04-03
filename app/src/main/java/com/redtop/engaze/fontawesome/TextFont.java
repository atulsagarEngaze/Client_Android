package com.redtop.engaze.fontawesome;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.collection.LruCache;


public class TextFont extends androidx.appcompat.widget.AppCompatTextView {

	private final static String NAME = "FONTAWESOME";
	private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

	public TextFont(Context context) {
		super(context);
		init();

	}

	public TextFont(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void init() {

		Typeface typeface = sTypefaceCache.get(NAME);

		if (typeface == null) {

			typeface = Typeface.createFromAsset(getContext().getAssets(), "fontawesome-webfont.ttf");
			sTypefaceCache.put(NAME, typeface);

		}

		setTypeface(typeface);

	}

}


