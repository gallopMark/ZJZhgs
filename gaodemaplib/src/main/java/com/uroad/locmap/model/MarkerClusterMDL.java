package com.uroad.locmap.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.uroad.locmap.R;

import java.util.ArrayList;

public class MarkerClusterMDL {
	private Activity activity;
	private MarkerOptions options;
	private ArrayList<Marker> includeMarkers;
	private ArrayList<MarkerOptions> includeMarkerOptions;
	private LatLngBounds bounds;// 创建区域

	/**
	 * 
	 * @param activity
	 * @param firstMarkers
	 * @param projection
	 * @param gridSize
	 *            区域大小参数
	 */
	public MarkerClusterMDL(Activity activity, Marker firstMarkers,
			Projection projection, int gridSize) {
		// TODO Auto-generated constructor stub
		// this.options = firstMarkers;
		options = new MarkerOptions().position(firstMarkers.getPosition())
				.title(firstMarkers.getTitle())
				.icon(firstMarkers.getIcons().get(0));
		this.activity = activity;
		Point point = projection.toScreenLocation(firstMarkers.getPosition());
		Point southwestPoint = new Point(point.x - gridSize, point.y + gridSize);
		Point northeastPoint = new Point(point.x + gridSize, point.y - gridSize);
		bounds = new LatLngBounds(
				projection.fromScreenLocation(southwestPoint),
				projection.fromScreenLocation(northeastPoint));
		includeMarkers = new ArrayList<Marker>();
		includeMarkerOptions = new ArrayList<MarkerOptions>();
		includeMarkers.add(firstMarkers);
		includeMarkerOptions.add(new MarkerOptions()
				.position(firstMarkers.getPosition())
				.title(firstMarkers.getTitle())
				.icon(firstMarkers.getIcons().get(0)));
	}

	/**
	 * 添加marker
	 */
	public void addMarker(Marker marker) {
		includeMarkers.add(marker);// 添加到列表中
	}

	/**
	 * 设置聚合点的中心位置以及图标
	 */
	public void setpositionAndIcon() {
		int size = includeMarkers.size();

		if (size == 1) {
			return;
		}
		double lat = 0.0;
		double lng = 0.0;

		String snippet = "";
		for (Marker op : includeMarkers) {
			lat += op.getPosition().latitude;
			lng += op.getPosition().longitude;
			snippet += op.getTitle() + "\n";
		}
		options.position(new LatLng(lat / size, lng / size));// 设置中心位置为聚集点的平均位置
		options.title("聚合点");
		options.snippet(snippet);

		int iconType = size / 10;

		switch (iconType) {
		case 0:
			options.icon(BitmapDescriptorFactory
					.fromBitmap(getViewBitmap(getView(size,
							R.drawable.marker_cluster_10))));
			break;
		case 1:
			options.icon(BitmapDescriptorFactory
					.fromBitmap(getViewBitmap(getView(size,
							R.drawable.marker_cluster_20))));
			break;
		case 2:
			options.icon(BitmapDescriptorFactory
					.fromBitmap(getViewBitmap(getView(size,
							R.drawable.marker_cluster_30))));
			break;
		case 3:
			options.icon(BitmapDescriptorFactory
					.fromBitmap(getViewBitmap(getView(size,
							R.drawable.marker_cluster_30))));
			break;
		case 4:
			options.icon(BitmapDescriptorFactory
					.fromBitmap(getViewBitmap(getView(size,
							R.drawable.marker_cluster_50))));
			break;
		default:
			options.icon(BitmapDescriptorFactory
					.fromBitmap(getViewBitmap(getView(size,
							R.drawable.marker_cluster_100))));
			break;
		}
	}

	public LatLngBounds getBounds() {
		return bounds;
	}

	public MarkerOptions getOptions() {
		return options;
	}

	public void setOptions(MarkerOptions options) {
		this.options = options;
	}

	public View getView(int carNum, int resourceId) {
		View view = activity.getLayoutInflater().inflate(
				R.layout.my_car_cluster_view, null);
		TextView carNumTextView = (TextView) view.findViewById(R.id.my_car_num);
		RelativeLayout myCarLayout = (RelativeLayout) view
				.findViewById(R.id.my_car_bg);
		myCarLayout.setBackgroundResource(resourceId);
		carNumTextView.setText(String.valueOf(carNum));
		return view;
	}

	/**
	 * 把一个view转化成bitmap对象
	 */
	public static Bitmap getViewBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}
}
