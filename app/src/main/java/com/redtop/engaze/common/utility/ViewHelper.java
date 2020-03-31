package com.redtop.engaze.common.utility;

import android.content.Context;
import android.os.Build;
import android.widget.ImageView;

public class ViewHelper {

    public static void setRippleDrawable(ImageView view, Context context, int rippleResourceDrawableId){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.setBackground(context.getResources().getDrawable(rippleResourceDrawableId));
        } else {
            view.setBackground(context.getDrawable(rippleResourceDrawableId));
        }
    }
}
