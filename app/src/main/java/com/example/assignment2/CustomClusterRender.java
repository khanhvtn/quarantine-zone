package com.example.assignment2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomClusterRender extends DefaultClusterRenderer {
    private final Context mContext;

    public CustomClusterRender(Context context,
                               GoogleMap map,
                               ClusterManager clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterItem item,
                                               @NonNull MarkerOptions markerOptions) {
        int height = 70;
        int width = 70;
        BitmapDrawable bitmapdraw =
                (BitmapDrawable) mContext.getDrawable(R.drawable.icon_marker);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(smallMarker));
    }
}
