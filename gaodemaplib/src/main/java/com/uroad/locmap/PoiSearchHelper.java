package com.uroad.locmap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.uroad.locmap.model.LatLngMDL;
import com.uroad.locmap.model.PoiCitySearchOptionMDL;
import com.uroad.locmap.model.PoiInfoMDL;
import com.uroad.locmap.model.PoiResultMDL;
import com.uroad.locmap.util.ToastUtil;

public class PoiSearchHelper {

	private PoiSearchHelper(Context ct) {
		super();
		this.ct = ct;
	}

	private String keyWord = "";// 要输入的poi搜索关键字
	private PoiResult poiResult; // poi返回的结果
	private int currentPage = 0;// 当前页面，从0开始计数
	private int pageSize = 10;// 设置每页最多返回多少条poiitem,默认10条
	private PoiSearch.Query query;// Poi查询条件类
	private PoiSearch poiSearch;// POI搜索
	private Context ct;
	private PoiSearch.SearchBound bound;// 此类定义了查询圆形和查询矩形，查询返回的POI的位置在此圆形或矩形内

	private static PoiSearchHelper poiSearchHelper;
	PoiSearchResultListener listener;

	public static PoiSearchHelper getInstance(Context ct) {
		if (poiSearchHelper == null) {
			poiSearchHelper = new PoiSearchHelper(ct);
		}
		return poiSearchHelper;
	}

	/**
	 * 搜索POI
	 */
	public void searchPoi(PoiCitySearchOptionMDL poiCitySearchOptionMDL) {
		if (poiCitySearchOptionMDL != null) {
			if (TextUtils.isEmpty(poiCitySearchOptionMDL.getKeyword())) {
				ToastUtil.show(ct, "请输入搜索关键字");
				return;
			} else {
				doSearchQuery(poiCitySearchOptionMDL);
			}
		}

	}

	/**
	 * 搜索下一页POI
	 */
	public void nextPagePOI() {
		if (query != null && poiSearch != null && poiResult != null) {
			if (poiResult.getPageCount() - 1 > currentPage) {
				currentPage++;
				query.setPageNum(currentPage);// 设置查后一页
				poiSearch.searchPOIAsyn();
			} else {
				ToastUtil.show(ct, R.string.no_result);
			}
		}
	}

	/**
	 * 设置搜索第几页
	 */
	public void setCurrentPage(int currentPage){
		this.currentPage = currentPage;
		if(query != null){
			query.setPageNum(this.currentPage);
		}
	}

	/**
	 * 开始进行poi搜索
	 */
	protected void doSearchQuery(PoiCitySearchOptionMDL poiCitySearchOptionMDL) {
		keyWord = poiCitySearchOptionMDL.getKeyword();
		pageSize = poiCitySearchOptionMDL.getPageCapacity() > 0 ? poiCitySearchOptionMDL
				.getPageCapacity() : 10;
		currentPage = poiCitySearchOptionMDL.getPageNum();
		if (TextUtils.isEmpty(poiCitySearchOptionMDL.getCity())) {
			query = new PoiSearch.Query(keyWord, "", "");
		} else {
			query = new PoiSearch.Query(keyWord, "", poiCitySearchOptionMDL
					.getCity().trim());// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
		}

		query.setPageSize(pageSize);// 设置每页最多返回多少条poiitem
		query.setPageNum(currentPage);// 设置查第一页

		poiSearch = new PoiSearch(ct, query);
		poiSearch.setOnPoiSearchListener(onPoiSearchListener);
		poiSearch.searchPOIAsyn();
	}

	/**
	 * 开始进行poi搜索
	 */
	public void doSearchQueryInBound(List<LatLngMDL> list, String keyword) {
		if (list != null && list.size() > 0) {
			List<LatLonPoint> latLonPoints = new ArrayList<LatLonPoint>();
			for (LatLngMDL latLngMDL : list) {
				LatLonPoint latLonPoint = new LatLonPoint(
						latLngMDL.getLatitude(), latLngMDL.getLongitude());
				latLonPoints.add(latLonPoint);
			}
			double a1 = latLonPoints.get(0).getLatitude();
			double a2 = latLonPoints.get(1).getLatitude();
			double b1 = latLonPoints.get(0).getLongitude();
			double b2 = latLonPoints.get(1).getLongitude();
			double c1 = a1 > a2 ? a1 : a2, c2 = b1 > b2 ? b1 : b2;
			LatLonPoint right = new LatLonPoint(c1, c2);
			double d1 = a1 < a2 ? a1 : a2, d2 = b1 < b2 ? b1 : b2;
			LatLonPoint left = new LatLonPoint(d1, d2);
			bound = new PoiSearch.SearchBound(left, right);

			query = new PoiSearch.Query(keyword, "", "");
			poiSearch = new PoiSearch(ct, query);
			poiSearch.setBound(bound);
			poiSearch.setOnPoiSearchListener(onPoiSearchListener);
			poiSearch.searchPOIAsyn();
		}
	}

	/**
	 * 开始进行poi搜索
	 */
	public void doSearchQueryInBound(double latitude, double longitude, String keyword,
									 int distance, int size, int index) {
		currentPage = index;
		pageSize = size > 0 ? size : 10;
		query = new PoiSearch.Query(keyword, "", "");
		query.setPageSize(pageSize);// 设置每页最多返回多少条poiitem
		query.setPageNum(currentPage);// 设置查第一页
		poiSearch = new PoiSearch(ct, query);
		poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude,longitude), distance));
		poiSearch.setOnPoiSearchListener(onPoiSearchListener);
		poiSearch.searchPOIAsyn();
	}

	OnPoiSearchListener onPoiSearchListener = new OnPoiSearchListener() {

		/**
		 * POI信息查询回调方法
		 */
		@Override
		public void onPoiSearched(PoiResult result, int rCode) {
			// TODO Auto-generated method stub
			if (rCode == 1000 && listener != null) {
				PoiResultMDL poiResultMDL = new PoiResultMDL();
				if (result != null && result.getQuery() != null) {// 搜索poi的结果

					if (result.getQuery().equals(query)) {// 是否是同一条
						poiResult = result;
						// 取得搜索到的poiitems有多少页
						List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
						List<SuggestionCity> suggestionCities = poiResult
								.getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

						if (poiItems != null && poiItems.size() > 0) {
							poiResultMDL.setHasData(true);
							poiResultMDL.setCurrentPageCapacity(pageSize);
							poiResultMDL.setCurrentPageNum(currentPage);
							poiResultMDL.setTotalPageNum(result.getPageCount());
							List<PoiItem> lists = result.getPois();
							if (lists.size() > 0) {
								List<PoiInfoMDL> mdls = new ArrayList<PoiInfoMDL>();
								for (PoiItem poiInfo : lists) {
									PoiInfoMDL mdl = new PoiInfoMDL();
									mdl.setAddress(poiInfo.getSnippet());
									mdl.setCity(poiInfo.getCityName());
									mdl.setLatitude(poiInfo.getLatLonPoint()
											.getLatitude());
									mdl.setLongitude(poiInfo.getLatLonPoint()
											.getLongitude());
									mdl.setName(poiInfo.getTitle());
									mdl.setPhoneNum(poiInfo.getTel());
									mdl.setPostCode(poiInfo.getPostcode());
									mdls.add(mdl);
								}
								poiResultMDL.setAllPoi(mdls);
							}
						} else if (suggestionCities != null
								&& suggestionCities.size() > 0) {
							poiResultMDL.setHasData(false);
							// showSuggestCity(suggestionCities);
						} else {
							listener.onGetPoiFail(ct
									.getString(R.string.no_result));
						}
					}
				} else {
					listener.onGetPoiFail(ct.getString(R.string.no_result));
					poiResultMDL.setHasData(true);
				}
				listener.onGetPoiResult(poiResultMDL);
			} else if (rCode == 27) {
				listener.onGetPoiFail(ct.getString(R.string.error_network));
			} else if (rCode == 32) {
				listener.onGetPoiFail(ct.getString(R.string.error_key));
			} else {
				listener.onGetPoiFail(ct.getString(R.string.error_other)
						+ rCode);
			}

		}

		/**
		 * POI详情查询回调方法
		 */
//		@Override
//		public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
//			// TODO Auto-generated method stub
//
//		}

		@Override
		public void onPoiItemSearched(PoiItem arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
	};

	/**
	 * poi没有搜索到数据，返回一些推荐城市的信息
	 */
	private void showSuggestCity(List<SuggestionCity> cities) {
		String infomation = "推荐城市\n";
		for (int i = 0; i < cities.size(); i++) {
			infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
					+ cities.get(i).getCityCode() + "城市编码:"
					+ cities.get(i).getAdCode() + "\n";
		}
	}

	public interface PoiSearchResultListener {
		void onGetPoiResult(PoiResultMDL poiResult);

		void onGetPoiFail(String message);
	}

	public void setPoiSearchResultListener(PoiSearchResultListener listener) {
		this.listener = listener;
	}
}
