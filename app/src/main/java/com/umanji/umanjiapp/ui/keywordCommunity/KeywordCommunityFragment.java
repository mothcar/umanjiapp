package com.umanji.umanjiapp.ui.keywordCommunity;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.analytics.ApplicationController;
import com.umanji.umanjiapp.gcm.GcmRegistrationIntentService;
import com.umanji.umanjiapp.helper.AuthHelper;
import com.umanji.umanjiapp.helper.FileHelper;
import com.umanji.umanjiapp.helper.Helper;
import com.umanji.umanjiapp.model.AuthData;
import com.umanji.umanjiapp.model.ChannelData;
import com.umanji.umanjiapp.model.ErrorData;
import com.umanji.umanjiapp.model.SubLinkData;
import com.umanji.umanjiapp.model.SuccessData;
import com.umanji.umanjiapp.ui.BaseFragment;
import com.umanji.umanjiapp.ui.channel.BaseTabAdapter;
import com.umanji.umanjiapp.ui.channel._fragment.about.AboutFragment;
import com.umanji.umanjiapp.ui.channel._fragment.communities.CommunityListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.members.MemberListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.posts.PostListAdapter;
import com.umanji.umanjiapp.ui.channel._fragment.posts.PostListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.spots.SpotListFragment;
import com.umanji.umanjiapp.ui.channel.complex.ComplexActivity;
import com.umanji.umanjiapp.ui.channel.profile.ProfileActivity;
import com.umanji.umanjiapp.ui.channel.spot.SpotActivity;
import com.umanji.umanjiapp.ui.main.search.SearchActivity;
import com.umanji.umanjiapp.ui.modal.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class KeywordCommunityFragment extends BaseFragment {
    private static final String TAG = "KeywordCommunityFrag";

    /****************************************************
     * View
     ****************************************************/
    private View mNoticePanel;

    private GoogleMap mMap;

    //private PostListAdapter mAdapter;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    //private LinearLayout mHeaderPanel;
    private RoundedImageView mAvatarImageBtn;
    private Button mNotyCountBtn;

    private RoundedImageView mZoomBtn;

//    private TextView mZoomLevelText;
//    private TextView mInfoTextPanel;


    private ImageView mGuideImageView01;

    private ImageView mInfoButton;



    /****************************************************
     * Map
     ****************************************************/
    LatLng mCurrentMyPosition;


    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
    };

    private static final int INITIAL_REQUEST = 10;
    private static final int PERMS_REQUEST = INITIAL_REQUEST + 2;


    /****************************************************
     * Etc
     ****************************************************/
    private ChannelData mUser;
    private JSONArray mMarkers;
    private JSONArray mAds;
    private ChannelData mCurrentChannel;
    private ChannelData mSelectedChannel;
    private ChannelData mClickedChannel;
    private ChannelData mAdChannel;

    private ArrayList<ChannelData> mPosts;


    private boolean isBlock = false;
    private boolean isLoading = false;
    private int mPreFocusedItem = 0;

    private String mSlidingState = SLIDING_COLLAPSED;

    private LatLng mLatLngByPoint = new LatLng(37.642443934398, 126.977429352700);
    private ChannelData mChannelByPoint;
    private Marker mMarkerByPoint;
    private Marker mFocusedMarker;
    private LatLng mPointByPost;

    private String mChannelIdForPush;
    private ImageView mAdsImage;
    private LinearLayout mlayout;

    TextView searchBtn;
    TextView mKeywordTitle;

    /****************************************************
     *  PageViewer
     ****************************************************/

    protected ChannelData   mChannel;

    protected BaseTabAdapter mAdapter;

    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;

    protected String mTabType = "";

    protected int mCurrentTapPosition = 0;

    public static KeywordCommunityFragment newInstance(Bundle bundle) {
        KeywordCommunityFragment fragment = new KeywordCommunityFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mChannelIdForPush = getArguments().getString("id");
            String jsonString = getArguments().getString("channel");
            if(jsonString != null) {
                mChannel = new ChannelData(jsonString);
            }

            mTabType = getArguments().getString("tabType");
        }


        Tracker t = ((ApplicationController) mActivity.getApplication()).getTracker();
        t.setScreenName("MainActivity");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onStart() {
        super.onStart();

        GoogleAnalytics.getInstance(mActivity).reportActivityStart(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(mActivity).reportActivityStop(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        //initMainListView(view);

        if (AuthHelper.isLogin(mActivity)) {
            loginByToken();
        } else {
            updateView();
        }

        initWidgets(view);

        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mActivity, Manifest.permission.INTERNET) ==
                            PackageManager.PERMISSION_GRANTED) {
                initMap();
            } else {
                requestPermissions(PERMS, PERMS_REQUEST);
            }
        } else {
            initMap();
        }

        if (!TextUtils.isEmpty(mChannelIdForPush)) {
            startActivityForPush();
        }

        initTabAdapter(view);

        return view;
    }

    protected void initTabAdapter(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewPaper);
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        mAdapter = new BaseTabAdapter(getActivity().getSupportFragmentManager());

        addFragmentToTabAdapter(mAdapter);

        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        setTabSelect();

        onTabSelected(mTabLayout);
    }

    protected void setTabSelect() {
        TabLayout.Tab tab;
        switch (mTabType) {
            case TAB_POSTS:
                tab = mTabLayout.getTabAt(0);
                break;
            case TAB_MEMBERS:
                tab = mTabLayout.getTabAt(1);
                break;
            case TAB_COMMUNITIES:
                tab = mTabLayout.getTabAt(2);
                break;
            case TAB_ABOUT:
                tab = mTabLayout.getTabAt(3);
                break;
            default:
                tab = mTabLayout.getTabAt(0);
                break;
        }

        tab.select();
    }

    protected void onTabSelected(TabLayout tabLayout) {
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                mCurrentTapPosition = tab.getPosition();

                /*switch (mCurrentTapPosition) {
                    case 0:
                        mFab.setImageResource(R.drawable.ic_discuss);
                        if (AuthHelper.isLogin(mActivity)) {
                            mFab.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 1: case 2: case 3: case 4:
                        mFab.setVisibility(View.GONE);
                        break;

                }*/

            }
        });
    }

    protected void startActivityForPush() {
        try {
            JSONObject params = new JSONObject();
            params.put("id", mChannelIdForPush);

            mApi.call(api_channels_get, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    ChannelData channelData = new ChannelData(object);

                    switch (channelData.getType()) {
                        case TYPE_POST:
                            Helper.startActivity(mActivity, channelData);
                            break;
                        case TYPE_MEMBER:
                        case TYPE_LINK:
                        case TYPE_SURVEY:
                            Helper.startActivity(mActivity, channelData.getParent());
                            break;
                        default:
                            Helper.startActivity(mActivity, channelData);
                            break;
                    }

                    mChannelIdForPush = "";
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_keyword_community_main, container, false);
    }

    @Override
    public void initWidgets(View view) {
        mNoticePanel = view.findViewById(R.id.noticePanel);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) view.findViewById(R.id.slidingUpPanelLayout);
        mSlidingUpPanelLayout.setPanelHeight(Helper.dpToPixel(mActivity, 200));
        mSlidingUpPanelLayout.setAnchorPoint(0.7f);
        mSlidingUpPanelLayout.setMinFlingVelocity(DEFAULT_MIN_FLING_VELOCITY);


//        mHeaderPanel = (LinearLayout) view.findViewById(R.id.headerPanel);
//        mHeaderPanel.setOnClickListener(this);


        mZoomBtn = (RoundedImageView) view.findViewById(R.id.mZoomBtn);
        mZoomBtn.setTag(ZOOM_IN);
        mZoomBtn.setOnClickListener(this);

        mAvatarImageBtn = (RoundedImageView) view.findViewById(R.id.mAvatarImageBtn);
        mAvatarImageBtn.setOnClickListener(this);

        mNotyCountBtn = (Button) view.findViewById(R.id.mNotyCount);
        mNotyCountBtn.setOnClickListener(this);
        mNotyCountBtn.setText("0");

        mAlert = new AlertDialog.Builder(mActivity);

        searchBtn = (TextView) view.findViewById(R.id.search);
        searchBtn.setOnClickListener(this);

//        mZoomLevelText = (TextView) view.findViewById(R.id.mZoomLevelText);
//
//        mInfoTextPanel = (TextView) view.findViewById(R.id.mInfoTextPanel);
//        mInfoTextPanel.setSelected(true);

        mAdsImage = (ImageView) view.findViewById(R.id.ads_image);
        mAdsImage.setOnClickListener(this);

        //mlayout = (LinearLayout) view.findViewById(R.id.mainListContainer);

        mInfoButton = (ImageView) view.findViewById(R.id.infoButton);
        mInfoButton.setOnClickListener(this);

        mKeywordTitle = (TextView) view.findViewById(R.id.keyword_community_Title);
        mKeywordTitle.setText(mChannel.getName());

    }


    @Override
    public void loadData() {
        if (AuthHelper.isLogin(mActivity)) {
            loadNewNoties();
        }

        loadMainMarkers();
        //loadMainPosts();
        loadMainAds();

    }

    @Override
    public void updateView() {
        if (AuthHelper.isLogin(mActivity)) {
            mAvatarImageBtn.setVisibility(View.VISIBLE);
            String userPhoto = mUser.getPhoto();
            if (!TextUtils.isEmpty(userPhoto)) {
                Glide.with(mActivity)
                        .load(userPhoto)
                        .placeholder(R.drawable.empty)
                        .animate(R.anim.abc_fade_in)
                        .override(40, 40)
                        .into(mAvatarImageBtn);
            } else {
                Glide.with(mActivity)
                        .load(R.drawable.avatar_default_0)
                        .placeholder(R.drawable.empty)
                        .override(40, 40)
                        .into(mAvatarImageBtn);
            }
        } else {
            Glide.with(mActivity)
                    .load(R.drawable.login)
                    .animate(R.anim.abc_fade_in)
                    .override(40, 40)
                    .into(mAvatarImageBtn);
        }
    }

    @Override
    public void onEvent(SuccessData event) {
        super.onEvent(event);

        switch (event.type) {
            case api_token_check:
            case api_signin:
            case api_signup:
                AuthData auth = new AuthData(event.response);
                if (auth != null && auth.getToken() != null) login(auth);
                else logout();
                break;

            case api_logout:
                logout();
                break;
            case api_channels_createCommunity:
            case api_channels_createComplex:
            case api_channels_createSpot:
            case api_channels_id_update:
            case api_channels_id_delete:
                mCurrentChannel = null;
                mSelectedChannel = null;
                mClickedChannel = null;
                loadData();
                break;
            case api_profile_id_update:
                mUser = new ChannelData(event.response);
                updateView();
                break;
            case api_noites_read:
                mNotyCountBtn.setVisibility(View.GONE);
                mNotyCountBtn.setText("0");
                break;

            case api_channels_id_vote:
            case api_channels_id_like:
                Helper.showNoticePanel(mActivity, mNoticePanel, POINT_DEFAULT + " 포인트 증가");
                break;

            case api_channels_id_unLike:
                Helper.showNoticePanel(mActivity, mNoticePanel, POINT_DEFAULT + " 포인트 감소");
                break;

            case EVENT_LOOK_AROUND:
                ChannelData channelData = new ChannelData(event.response);
                LatLng latLng = new LatLng(channelData.getLatitude(), channelData.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
                break;
        }
    }

    public void onEvent(ErrorData event) {

        switch (event.type) {
            case TYPE_ERROR_AUTH:
                Helper.startSigninActivity(mActivity, mCurrentMyPosition);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.headerPanel:
                if (TextUtils.equals(mSlidingState, SLIDING_COLLAPSED)) {
                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    mSlidingState = SLIDING_ANCHORED;

                } else if (TextUtils.equals(mSlidingState, SLIDING_ANCHORED)) {
                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    mSlidingState = SLIDING_COLLAPSED;
                }

                break;
            case R.id.mZoomBtn:
                if (mZoomBtn.getTag().equals(ZOOM_IN)) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 1000, null);
                } else {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
                }
                break;

            case R.id.mNotyCount:
            case R.id.mAvatarImageBtn:
                if (AuthHelper.isLogin(mActivity) && mUser != null) {
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("channel", mUser.getJsonObject().toString());
                    bundle.putInt("newNoticeCount", getNewNoticeCount());
                    if (getNewNoticeCount() > 0) {
                        bundle.putString("tabType", TAB_NOTIES);
                    } else {
                        bundle.putString("tabType", TAB_SPOTS);
                    }

                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                } else {
                    Helper.startSigninActivity(mActivity, mCurrentMyPosition);
                }
                break;

            case R.id.ads_image:
                Intent webIntent = new Intent(mActivity, WebViewActivity.class);
                mActivity.startActivity(webIntent);
                break;

            case R.id.infoButton:
                Intent webInt = new Intent(mActivity, WebViewActivity.class);
                webInt.putExtra("url", "http://blog.naver.com/mothcar/220715638989");
                mActivity.startActivity(webInt);
                break;


            case R.id.search:
                Intent searchIntent = new Intent(mActivity, SearchActivity.class);
                startActivity(searchIntent);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMS_REQUEST:
                if (canAccessLocation()) {
                    initMap();
                }
                break;
        }
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }else if(PackageManager.PERMISSION_GRANTED == mActivity.checkSelfPermission(perm)){
            return true;
        }

        return false;
    }

    private void initMap() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity.getBaseContext());

        if (status != ConnectionResult.SUCCESS) {

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, mActivity, requestCode);
            dialog.show();

        } else {
            mMap = ((MapFragment) mActivity.getFragmentManager().findFragmentById(R.id.mMapFragment))
                    .getMap();

            int paddingInDp = 50;

            final float scale = getResources().getDisplayMetrics().density;
            int paddingInPx = (int) (paddingInDp * scale + 0.5f);

            mMap.setPadding(0, paddingInPx, 0, 0);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            initMyLocation();
        }

        initMapEvents();
    }

    protected void initMyLocation() {
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(mActivity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        double latitude = 37.642443934398;
        double longitude = 126.977429352700;

        try {

            String isFirst = FileHelper.getString(mActivity, "isFirst");
            Location location = null;
            if (TextUtils.isEmpty(isFirst)) {
                FileHelper.setString(mActivity, "isFirst", "checked");
            } else {
                location = locationManager.getLastKnownLocation(provider);
            }

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            mCurrentMyPosition = new LatLng(latitude, longitude);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mCurrentMyPosition)
                    .zoom(7)
                    .bearing(90)
                    .tilt(40)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } catch (SecurityException e) {
            Log.e("SecurityException", "SecurityException 에러발생:" + e.toString());
        }

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(7), 2000, null);
    }

    /****************************************************
     * init Map Events
     ****************************************************/

    protected void initMapEvents() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                if (mFocusedMarker != null) {
                    mFocusedMarker.remove();
                }
                if (mChannelByPoint != null) {
                    mChannelByPoint = null;
                }

                final int zoom = (int) mMap.getCameraPosition().zoom;

                if (isComplexCreatable(zoom) && mUser.getPoint() < POINT_CREATE_COMPLEX) {
                    int gapPoint = POINT_CREATE_COMPLEX - mUser.getPoint();
                    Toast.makeText(mActivity, "복합단지 생성을 위한 포인트가 부족합니다(" + POINT_CREATE_COMPLEX + "이상부터 가능)" + ". 줌레벨 18에서 스팟을 먼저 생성해 보세요. ^^", Toast.LENGTH_LONG).show();
                    return;
                }

                mLatLngByPoint = point;


                if (zoom >= 15 && zoom <= 21) {

                    try {
                        JSONObject params = new JSONObject();
                        params.put("latitude", mLatLngByPoint.latitude);
                        params.put("longitude", mLatLngByPoint.longitude);

                        mApi.call(api_channels_getByPoint, params, new AjaxCallback<JSONObject>() {
                            @Override
                            public void callback(String url, JSONObject object, AjaxStatus status) {
                                mChannelByPoint = new ChannelData(object);

                                if (TextUtils.isEmpty(mChannelByPoint.getId())) {
                                    if (AuthHelper.isLogin(mActivity)) {
                                        isBlock = true;

                                        mMarkerByPoint = Helper.addNewMarkerToMap(mMap, mChannelByPoint);
                                        LatLng tmpPoint = Helper.getAdjustedPoint(mMap, mLatLngByPoint);

                                        mMap.animateCamera(CameraUpdateFactory.newLatLng(tmpPoint), 100, null);

                                        if (isComplexCreatable(zoom)) {
                                            showCreateComplexDialog();
                                        } else if (isSpotCreatable(zoom)) {
                                            showCreateSpotDialog();
                                        }

                                    } else {
                                        Helper.startSigninActivity(mActivity, mCurrentMyPosition);
                                    }

                                } else {
                                    if (isComplexCreatable(zoom)) {
                                        startSpotActivity(mChannelByPoint, TYPE_COMPLEX);
                                    } else if (isSpotCreatable(zoom)) {
                                        startSpotActivity(mChannelByPoint, TYPE_SPOT);
                                    }

                                }
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "error " + e.toString());
                    }
                }

            }
        });


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                isBlock = true;
                LatLng latLng = marker.getPosition();

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, null);

                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                }

                try {
                    String idx = marker.getSnippet();

                    if (TextUtils.equals(idx, String.valueOf(MARKER_INDEX_CLICKED))) {
                        mClickedChannel = mSelectedChannel;
                    } else {
                        mClickedChannel = new ChannelData(mMarkers.getJSONObject(Integer.valueOf(idx)));
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error " + e.toString());
                }

                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String index = marker.getSnippet();

                ChannelData channelData;
                try {
                    if (TextUtils.equals(index, String.valueOf(MARKER_INDEX_BY_POST))) {
                        channelData = mCurrentChannel.getParent();
                    } else if (TextUtils.equals(index, String.valueOf(MARKER_INDEX_CLICKED))) {
                        channelData = mSelectedChannel;
                    } else {
                        channelData = new ChannelData(mMarkers.getJSONObject(Integer.valueOf(index)));
                    }

                    Helper.startActivity(mActivity, channelData);

                } catch (JSONException e) {
                    Log.e(TAG, "error " + e.toString());
                }
            }
        });


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View view = mActivity.getLayoutInflater().inflate(R.layout.widget_info_window, null);
                TextView name = (TextView) view.findViewById(R.id.wiSpotName);
                name.setText(marker.getTitle());

                return view;

            }
        });


        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d(TAG, "onMapLoaded: ");
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (isBlock) {
                    isBlock = false;
                } else {

//                    mZoomLevelText.setText("Zoom: " + (int) position.zoom);

                    int zoom = (int) position.zoom;
                    // isPoliticTouchable

                    if (isComplexCreatable(zoom)) {
                        mZoomBtn.setImageResource(R.drawable.zoom_in);
                        mZoomBtn.setTag(ZOOM_IN);
                    } else if (isSpotCreatable(zoom)) {

                        mZoomBtn.setImageResource(R.drawable.zoom_out);
                        mZoomBtn.setTag(ZOOM_OUT);
                    } else if (isPoliticTouchable(zoom)) {
                        mZoomBtn.setImageResource(R.drawable.zoom_out);
                        mZoomBtn.setTag(ZOOM_OUT);
                    } else {
                        mZoomBtn.setImageResource(R.drawable.zoom_in);
                        mZoomBtn.setTag(ZOOM_IN);
                    }

                    loadData();

                }
            }
        });

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mCurrentMyPosition = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });

    }


   /* private void loadMainPosts() {
        mAdapter.resetDocs();
        mAdapter.setCurrentPage(0);

        loadMoreMainPosts();
    }*/

    //*******                광고로직 테스트

    private void loadMainAds() {

        try {
            JSONObject params = Helper.getZoomMinMaxLatLngParams(mMap);
            params.put("zoom", (int) mMap.getCameraPosition().zoom);  // level -> zoom

            // origin api :: api_main_findAds
            mApi.call(api_main_findAds2, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json == null) {
                        Random rd = new Random();
                        int randomNum = rd.nextInt(3);
                        if (randomNum == 0) {
                            mAdsImage.setImageResource(R.drawable.ads_umanji_guide);
                        } else if (randomNum == 2) {
                            mAdsImage.setImageResource(R.drawable.ad_sample01);
                        } else if (randomNum == 1) {
                            mAdsImage.setImageResource(R.drawable.ad_sample);
                        }

                    } else {
                        addAdsToMap(json);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error " + e.toString());
        }
        mProgress.hide();
    }


    private void addAdsToMap(JSONObject jsonObject) {
        try {
            mAds = jsonObject.getJSONArray("data");

            if (mAds.length() != 0) {

                Random rd = new Random();

                int randomNum = rd.nextInt(mAds.length());

                mAdChannel = new ChannelData(mAds.getJSONObject(randomNum));
                String photo = mAdChannel.getPhoto();
                if (!TextUtils.isEmpty(photo)) {
                    Glide.with(mActivity)
                            .load(photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(mAdsImage);

                    mAdsImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                JSONObject params = new JSONObject();
                                params.put("id", mAdChannel.getParent().getId());
                                mApi.call(api_channels_get, params, new AjaxCallback<JSONObject>() {
                                    @Override
                                    public void callback(String url, JSONObject object, AjaxStatus status) {
                                        ChannelData channel = new ChannelData(object);
                                        Helper.startActivity(mActivity, channel, channel.getType());
                                    }
                                });
                            } catch (JSONException e) {
                                Log.e(TAG, "error " + e.toString());
                            }
                        }
                    });
                }
            } else {
                mAdsImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent webIntent = new Intent(mActivity, WebViewActivity.class);
                        mActivity.startActivity(webIntent);
                    }
                });
                Random rd = new Random();
                int randomNum = rd.nextInt(3);
                if (randomNum == 0) {
                    mAdsImage.setImageResource(R.drawable.ads_umanji_guide);
                } else if (randomNum == 2) {
                    mAdsImage.setImageResource(R.drawable.ad_sample01);
                } else if (randomNum == 1) {
                    mAdsImage.setImageResource(R.drawable.ad_sample);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }

    }

    private void loadMoreMainPosts() {
        isLoading = true;
        mProgress.show();

        try {
            JSONObject params = Helper.getZoomMinMaxLatLngParams(mMap);
            //params.put("page", mAdapter.getCurrentPage());
            params.put("limit", 5);
            //params.put("sort", "point DESC");

            mApi.call(api_main_findPosts, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    if (status.getCode() == 500) {
                        EventBus.getDefault().post(new ErrorData(TYPE_ERROR_AUTH, TYPE_ERROR_AUTH));
                    } else {
                        try {
                            JSONArray jsonArray = object.getJSONArray("data");

                            if (jsonArray.length() != 0) {
                                //mlayout.setBackgroundResource(R.color.feed_bg);

                                for (int idx = 0; idx < jsonArray.length(); idx++) {
                                    JSONObject jsonDoc = jsonArray.getJSONObject(idx);
                                    ChannelData doc = new ChannelData(jsonDoc);

                                    if (doc != null && doc.getOwner() != null && !TextUtils.isEmpty(doc.getOwner().getId())) {
                                        //mAdapter.addBottom(doc);
                                    }
                                }

                            } else {
                                //mlayout.setBackgroundResource(R.drawable.empty_main_post);
                            }

                            isLoading = false;
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e(TAG, "Error " + e.toString());
                        }
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error " + e.toString());
        }

        //mAdapter.setCurrentPage(mAdapter.getCurrentPage() + 1);
        mProgress.hide();
    }

    private void loginByToken() {
        try {
            JSONObject params = new JSONObject();
            params.put("access_token", AuthHelper.getToken(mActivity));
            mApi.call(api_token_check, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    AuthData auth = new AuthData(object);
                    if (auth != null && auth.getToken() != null) login(auth);
                    else logout();
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }

    }

    private void login(AuthData auth) {
        if (checkPlayServices()) {
            Intent intent = new Intent(mActivity, GcmRegistrationIntentService.class);
            mActivity.startService(intent);
        }

        mUser = auth.getUser();
        AuthHelper.login(mActivity, auth);

        updateView();
    }

    private void logout() {
        mUser = null;
        AuthHelper.logout(mActivity);

        updateView();
    }

    private static boolean isPoliticTouchable(int zoom) {
        if (zoom >= 6 && zoom <= 7) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isComplexCreatable(int zoom) {
        if (zoom >= 15 && zoom <= 17) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSpotCreatable(int zoom) {
        if (zoom >= 18) {
            return true;
        } else {
            return false;
        }
    }


    private void loadMainMarkers() {
        try {
            JSONObject params = new JSONObject();
            //ArrayList<SubLinkData> subLinks = mChannel.getSubLinks(TYPE_KEYWORD);

            //if (subLinks == null || subLinks.size() < 1) return;
            //params.put("name", subLinks.get(0).getName());
            params.put("name", mChannel.getName());
            mApi.call(api_main_findDistributions, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    addChannelsToMap(json);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error " + e.toString());
        }

    }


    private void addChannelsToMap(JSONObject jsonObject) {
        try {
            mMap.clear();

            mMarkers = jsonObject.getJSONArray("data");

            int idx = 0;

            if (mCurrentChannel != null) {
                if (Helper.isInVisibleResion(mMap, new LatLng(mCurrentChannel.getLatitude(), mCurrentChannel.getLongitude()))) {
                    mFocusedMarker = Helper.addMarkerToMap(mMap, mCurrentChannel, MARKER_INDEX_BY_POST);
                } else {
                    mCurrentChannel = null;
                }
            }

            if (mClickedChannel != null) {
                if (Helper.isInVisibleResion(mMap, new LatLng(mClickedChannel.getLatitude(), mClickedChannel.getLongitude()))) {
                    mFocusedMarker = Helper.addMarkerToMap(mMap, mClickedChannel, MARKER_INDEX_CLICKED);
                    mSelectedChannel = mClickedChannel;
                } else {
                    mSelectedChannel = null;
                    mClickedChannel = null;
                }
            }

            if (mMarkers != null) {
                for (; idx < mMarkers.length(); idx++) {
                    ChannelData channelData = new ChannelData(mMarkers.getJSONObject(idx));

                    if (mCurrentChannel != null && !TextUtils.equals(mCurrentChannel.getId(), channelData.getId())) {
                        Helper.addMarkerToMap(mMap, channelData, idx);
                    } else if (mSelectedChannel != null && !TextUtils.equals(mSelectedChannel.getId(), channelData.getId())) {
                        Helper.addMarkerToMap(mMap, channelData, idx);
                    } else {
                        Helper.addMarkerToMap(mMap, channelData, idx);
                    }
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "error " + e.toString());
        }

    }

    private void showCreateComplexDialog() {
        mAlert.setPositiveButton(R.string.complex_create_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Helper.startCreateActivity(mActivity, mChannelByPoint, TYPE_COMPLEX);
                dialog.cancel();
            }
        });

        mAlert.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMarkerByPoint.remove();
                dialog.cancel();
            }
        });

        mAlert.setTitle(R.string.complex_create_confirm);
        mAlert.setMessage(Helper.getFullAddress(mChannelByPoint));
        mAlert.show();
    }

    private void showCreateSpotDialog() {
        mAlert.setPositiveButton(R.string.spot_create_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONObject params = mChannelByPoint.getAddressJSONObject();
                    params.put("type", TYPE_SPOT);
                    mApi.call(api_channels_createSpot, params, new AjaxCallback<JSONObject>() {
                        @Override
                        public void callback(String url, JSONObject object, AjaxStatus status) {
                            mChannelByPoint = new ChannelData(object);
                            if (mMarkerByPoint != null) mMarkerByPoint.remove();
                            startSpotActivity(mChannelByPoint, TYPE_SPOT);

                            EventBus.getDefault().post(new SuccessData(api_channels_createSpot, object));
                        }
                    });
                    dialog.cancel();
                } catch (JSONException e) {
                    Log.e(TAG, "Error " + e.toString());
                }
            }
        });

        mAlert.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMarkerByPoint.remove();
                dialog.cancel();
            }
        });

        mAlert.setTitle(R.string.spot_create_confirm);
        mAlert.setMessage(Helper.getFullAddress(mChannelByPoint));
        mAlert.show();
    }

    private void startSpotActivity(ChannelData channel, String type) {
        Intent intent = null;

        switch (type) {
            case TYPE_SPOT:
                intent = new Intent(getActivity(), SpotActivity.class);
                break;
            case TYPE_COMPLEX:
                intent = new Intent(getActivity(), ComplexActivity.class);
                break;
            default:
                intent = new Intent(getActivity(), SpotActivity.class);
                break;
        }

        Bundle bundle = new Bundle();
        bundle.putString("channel", channel.getJsonObject().toString());
        intent.putExtra("bundle", bundle);
        intent.putExtra("enterAnim", R.anim.zoom_out);
        intent.putExtra("exitAnim", R.anim.zoom_in);

        startActivity(intent);
    }

    /*protected void addOnScrollListener(RecyclerView rView) {
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(rView.getContext());
        rView.setLayoutManager(mLayoutManager);

        rView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    ArrayList<ChannelData> channels = mAdapter.getDocs();

                    int currentFocusedIndex = visibleItemCount + pastVisiblesItems;
                    if (currentFocusedIndex == mPreFocusedItem) return;
                    mPreFocusedItem = currentFocusedIndex;
                    if (channels.size() <= mPreFocusedItem) return;

                    ChannelData channelData = channels.get(pastVisiblesItems);

                    if (mCurrentChannel == null || (!TextUtils.equals(channelData.getId(), mCurrentChannel.getId()))) {
                        isBlock = true;

                        mCurrentChannel = channels.get(pastVisiblesItems);
                        mPointByPost = new LatLng(mCurrentChannel.getLatitude(), mCurrentChannel.getLongitude());

                        if (mFocusedMarker != null) {
                            mFocusedMarker.remove();
                        }

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(mPointByPost), 500, null);
                        mFocusedMarker = Helper.addMarkerToMap(mMap, mCurrentChannel, MARKER_INDEX_BY_POST);
                    }

                    if (!isLoading) {
                        if (mPreFocusedItem == (totalItemCount - 2)) {
                            loadMoreMainPosts();
                        }
                    }
                }
            }
        });

    }*/

    protected void addFragmentToTabAdapter(BaseTabAdapter adapter) {
        Bundle bundle = new Bundle();

            bundle.putString("channel", mChannel.getJsonObject().toString());
            adapter.addFragment(PostListFragment.newInstance(bundle), "정보광장");
            adapter.addFragment(MemberListFragment.newInstance(bundle), "멤버");
            adapter.addFragment(CommunityListFragment.newInstance(bundle), "커뮤니티");
            adapter.addFragment(AboutFragment.newInstance(bundle), "기타정보");

    }


    /*protected RecyclerView initMainListView(View view) {
        RecyclerView rView = (RecyclerView) view.findViewById(R.id.recyclerView);
        rView.setItemViewCacheSize(iItemViewCacheSize);
        mAdapter = new PostListAdapter(mActivity, this);
        rView.setAdapter(mAdapter);

        addOnScrollListener(rView);
        return rView;
    }*/

    private void loadNewNoties() {
        try {
            JSONObject params = new JSONObject();
            params.put("read", false);
            mApi.call(api_noites_new_count, params, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    super.callback(url, object, status);
                    int notyCount = object.optInt("data");
                    if (notyCount > 0) {
                        mNotyCountBtn.setVisibility(View.VISIBLE);
                        mNotyCountBtn.setText(String.valueOf(notyCount));
                    } else {
                        mNotyCountBtn.setVisibility(View.GONE);
                        mNotyCountBtn.setText("0");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error " + e.toString());
        }
    }

    private int getNewNoticeCount() {
        return Integer.parseInt(mNotyCountBtn.getText().toString());
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
}
