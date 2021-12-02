package com.example.assignment2.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;

import com.example.assignment2.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomClusterRender extends DefaultClusterRenderer<MarkerItem> {
    private final Context mContext;

    public CustomClusterRender(Context context,
                               GoogleMap map,
                               ClusterManager clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
        clusterManager.setRenderer(this);
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<MarkerItem> cluster) {
        return cluster.getSize() >= 2;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MarkerItem item,
                                               @NonNull MarkerOptions markerOptions) {
        int height = 70;
        int width = 70;
        BitmapDrawable bitmapdraw =
                (BitmapDrawable) mContext.getDrawable(R.drawable.icon_marker);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(smallMarker)).title(item.getTitle())
                .snippet(item.getSnippet());
    }

}
