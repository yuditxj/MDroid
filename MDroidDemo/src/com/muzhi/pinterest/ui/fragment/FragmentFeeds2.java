package com.muzhi.pinterest.ui.fragment;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.muzhi.mdroid.tools.T;
import com.muzhi.mdroid.widget.fab.FloatingActionsMenu;
import com.muzhi.mdroid.widget.fab.FloatingActionsMenu.OnFloatingActionsMenuUpdateListener;
import com.muzhi.mdroid.widget.fab.FloatingActionsMenuHidable;
import com.muzhi.mdroid.widget.refresh.XMultiColumnListView;
import com.muzhi.pinterest.R;
import com.muzhi.pinterest.R.id;
import com.muzhi.pinterest.R.layout;
import com.muzhi.pinterest.adapter.StaggeredAdapter;
import com.muzhi.pinterest.model.DuitangInfo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask.Status;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;


public class FragmentFeeds2 extends Fragment {
	
	private View mView;
	private Context mContext;
	private static final String ARG_POSITION = "position";
	private int position;
	private Handler mHandler;
	
	private SwipeRefreshLayout swipeRefreshLayout;
	
	private StaggeredAdapter mAdapter = null;
	private int currentPage = 0;
	ContentTask task = new ContentTask(getActivity(), 2);
	
	
	private XMultiColumnListView mListView;
	private FloatingActionsMenuHidable fab;
	
	
	public static FragmentFeeds2 newInstance(int position) {
		FragmentFeeds2 f = new FragmentFeeds2();
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_swip_content, null);
		
		swipeRefreshLayout=(SwipeRefreshLayout)mView.findViewById(R.id.swipe_layout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		
		mListView = (XMultiColumnListView)mView.findViewById(R.id.list);
		
        mListView.setPullRefreshEnable(false);
        mListView.setPullLoadEnable(true);
        mListView.setAutoLoadEnable(true);        
        
        
        
        
      //设置刷新手势监听
  		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
  			@Override
  			public void onRefresh() {
  				new Handler().postDelayed(new Runnable() {
  					public void run() {
  						getList(1, 1);
  					}
  				}, 3000);
  			}
  		});
        mListView.setXListViewListener(new XMultiColumnListView.IXListViewListener() {
			
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				// AddItemToContainer(++currentPage, 1);
			}
			
			@Override
			public void onLoadMore() {
				// TODO Auto-generated method stub
				getList(++currentPage, 2);
			}
		});

        mAdapter = new StaggeredAdapter(mContext);
		
        
        
        
        
        fab=(FloatingActionsMenuHidable)mView.findViewById(R.id.multiple_actions);
        
		
		return mView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext=getActivity();
		
		mAdapter = new StaggeredAdapter(mContext);
	    mListView.setAdapter(mAdapter);
	        
        getList(currentPage, 2);
	}
	
	
	private void getList(int pageindex, int type) {
        if (task.getStatus() != Status.RUNNING) {
            String url = "http://www.duitang.com/album/62572137/masn/p/" + pageindex + "/24/";
        	
            Log.d("MainActivity", "current url:" + url);
            ContentTask task = new ContentTask(mContext, type);
            task.execute(url);

        }
    }

   
	
    private class ContentTask extends AsyncTask<String, Integer, List<DuitangInfo>> {

        private Context mContext;
        private int mType = 1;

        public ContentTask(Context context, int type) {
            super();
            mContext = context;
            mType = type;
        }

        @Override
        protected List<DuitangInfo> doInBackground(String... params) {
            try {
                return parseNewsJSON(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<DuitangInfo> result) {
            if (mType == 1) {

            	mAdapter.setList(result);
                mListView.stopRefresh();
                swipeRefreshLayout.setRefreshing(false);

            } else if (mType == 2) {
                mListView.stopLoadMore();
                mAdapter.addToLast(result);
                swipeRefreshLayout.setRefreshing(false);
            }

        }

        @Override
        protected void onPreExecute() {
        }

        public List<DuitangInfo> parseNewsJSON(String url) throws IOException {
            List<DuitangInfo> duitangs = new ArrayList<DuitangInfo>();
            String json = "";
            try {
                json = getStringFromUrl(url);

            } catch (IOException e) {
                Log.e("IOException is : ", e.toString());
                e.printStackTrace();
                return duitangs;
            }

            try {
                if (null != json) {
                    JSONObject newsObject = new JSONObject(json);
                    JSONObject jsonObject = newsObject.getJSONObject("data");
                    JSONArray blogsJson = jsonObject.getJSONArray("blogs");

                    for (int i = 0; i < blogsJson.length(); i++) {
                        JSONObject newsInfoLeftObject = blogsJson.getJSONObject(i);
                        DuitangInfo newsInfo1 = new DuitangInfo();
                        newsInfo1.setAlbid(newsInfoLeftObject.isNull("albid") ? "" : newsInfoLeftObject.getString("albid"));
                        newsInfo1.setIsrc(newsInfoLeftObject.isNull("isrc") ? "" : newsInfoLeftObject.getString("isrc"));
                        newsInfo1.setMsg(newsInfoLeftObject.isNull("msg") ? "" : newsInfoLeftObject.getString("msg"));
                        newsInfo1.setHeight(newsInfoLeftObject.getInt("iht"));
                        duitangs.add(newsInfo1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return duitangs;
        }
    }
	 

    
    
    
    
    
    public static String getStringFromUrl(String url) throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(url);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, "UTF-8");
	}
    
    
    
    
    
    
    
    
}