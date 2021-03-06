package com.umanji.umanjiapp.ui.channel.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.helper.AuthHelper;
import com.umanji.umanjiapp.helper.FileHelper;
import com.umanji.umanjiapp.helper.Helper;
import com.umanji.umanjiapp.model.ChannelData;
import com.umanji.umanjiapp.model.SuccessData;
import com.umanji.umanjiapp.ui.channel.BaseChannelFragment;
import com.umanji.umanjiapp.ui.channel.BaseTabAdapter;
import com.umanji.umanjiapp.ui.channel._fragment.about.AboutProfileFragment;
import com.umanji.umanjiapp.ui.channel._fragment.communities.CommunityListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.noties.NotyListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.posts.PostListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.roles.RoleListFragment;
import com.umanji.umanjiapp.ui.channel._fragment.spots.SpotListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ProfileFragment extends BaseChannelFragment {
    private static final String TAG = "ProfileFragment";

    private File mResizedFile;
    private String mPhotoUri;

    int mNewNoticeCount = 0;

    public static ProfileFragment newInstance(Bundle bundle) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            mNewNoticeCount = getArguments().getInt("newNoticeCount");
        }
    }

    @Override
    public void initWidgets(View view) {
        super.initWidgets(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // ROLES: 0, POST: 1, SPOT: 2, COMMUTNITY: 3, NOTICE: 4, ABOUT: 5
        if(AuthHelper.isLoginUser(mActivity, mChannel.getId()) && mNewNoticeCount > 0) {
            TabLayout.Tab tab = mTabLayout.getTabAt(4);
            tab.setText("NOTIES (" + mNewNoticeCount + ")");
            tab.select();
        } else {
            TabLayout.Tab tab = mTabLayout.getTabAt(0);
            tab.select();
        }


        return view;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    protected void addFragmentToTabAdapter(BaseTabAdapter adapter) {
        Bundle bundle = new Bundle();
        bundle.putString("channel", mChannel.getJsonObject().toString());

        // new Roles Tab
        adapter.addFragment(RoleListFragment.newInstance(bundle), "역할");
        adapter.addFragment(PostListFragment.newInstance(bundle), "포스트");
        adapter.addFragment(SpotListFragment.newInstance(bundle), "장소");
        adapter.addFragment(CommunityListFragment.newInstance(bundle), "커뮤니티");

        if(AuthHelper.isLoginUser(mActivity, mChannel.getId())) {
            adapter.addFragment(NotyListFragment.newInstance(bundle), "알림");
        }
        adapter.addFragment(AboutProfileFragment.newInstance(bundle), "설정");
    }

    @Override
    protected void setTabSelect() {
        if(TextUtils.isEmpty(mTabType)) return;

        TabLayout.Tab tab;
        switch (mTabType) {
            case TAB_ROLES:
                tab = mTabLayout.getTabAt(0);
                break;
            case TAB_POSTS:
                tab = mTabLayout.getTabAt(1);
                break;
            case TAB_SPOTS:
                tab = mTabLayout.getTabAt(2);
                break;
            case TAB_COMMUNITIES:
                tab = mTabLayout.getTabAt(3);
                break;
            case TAB_NOTIES:
                tab = mTabLayout.getTabAt(4);
                break;
            case TAB_ABOUT:
                tab = mTabLayout.getTabAt(5);
                break;
            default:
                tab = mTabLayout.getTabAt(0);  // 1
                break;
        }

        tab.select();
    }

    @Override
    public void loadData() {
        super.loadData();
    }

    @Override
    public void updateView() {
        super.updateView();

//        mFab.setVisibility(View.GONE);

        setUserName(mActivity, mChannel, "프로필");
        setPhoto(mActivity, mChannel, R.drawable.multi_spot_background);
        setParentName(mActivity, mChannel.getParent());
        setUserPhoto(mActivity, mChannel);
        setPoint(mActivity, mChannel);
//        setLevel(mActivity, mChannel);
        setMemberCount(mActivity, mChannel);
//        setKeywords(mActivity, mChannel);
    }

    @Override
    protected void setUserName(Activity activity, final ChannelData channelData, String label) {
        if(!TextUtils.isEmpty(channelData.getUserName())) {
            mName.setText(Helper.getShortenString(channelData.getUserName()) + " " + label);
        } else {
            mName.setText(label);
        }

        if(AuthHelper.isLoginUser(mActivity, mChannel.getId())) {
            mName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.startUpdateActivity(mActivity, channelData);
                }
            });
        }
    }

    @Override
    protected void setPhoto(Activity activity, final ChannelData channelData, int defaultImage) {
        String photoUrl = channelData.getPhoto();
        if(photoUrl != null) {
            Glide.with(activity)
                    .load(photoUrl)
                    .into(mPhoto);

            mPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.startImageViewActivity(mActivity, channelData);
                }
            });
        }else {
            Glide.with(activity)
                    .load(defaultImage)
                    .into(mPhoto);

            if(AuthHelper.isLoginUser(mActivity, mChannel.getId())) {
                mPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.startUpdateActivity(mActivity, channelData);
                    }
                });
            }
        }
    }

    protected void setUserPhoto(Activity activity, final ChannelData userData) {
        if(userData != null) {
            String userPhoto = userData.getPhoto();
            if(userPhoto != null) {
                Glide.with(mActivity)
                        .load(userPhoto)
                        .placeholder(R.drawable.empty)
                        .animate(R.anim.abc_fade_in)
//                        .override(40, 40)
                        .into(mUserPhoto);
            }

        } else {
            Glide.with(mActivity)
                    .load(R.drawable.avatar_default_0)
                    .animate(R.anim.abc_fade_in)
                    .into(mUserPhoto);
        }
    }

    protected void onTabSelected(TabLayout tabLayout) {
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                mCurrentTapPosition = tab.getPosition();

                switch (mCurrentTapPosition) {
                    case 0:
                    case 1:
                        /*mFab.setImageResource(R.drawable.ic_discuss);
                        if (AuthHelper.isLogin(mActivity) && AuthHelper.isLoginUser(mActivity, mChannel.getId())) {
                            mFab.setVisibility(View.VISIBLE);
                        }
                        break;*/
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        /*mFab.setVisibility(View.GONE);
                        break;*/

                }

            }
        });
    }

    @Override
    public void onEvent(SuccessData event) {
        super.onEvent(event);

        switch (event.type) {
            case api_photo:
                mProgress.hide();

                try{
                    mResizedFile.delete();
                    mResizedFile = null;
                    mPhotoUri = null;

                        JSONObject data = event.response.getJSONObject("data");
                        mPhotoUri = REST_S3_URL + data.optString("photo");


                        JSONObject params = new JSONObject();
                        params.put("id", mChannel.getId());

                        ArrayList<String> photos = new ArrayList<>();
                        photos.add(mPhotoUri);
                        params.put("photos", new JSONArray(photos));

                        mApi.call(api_profile_id_update, params);
                        mPhotoUri = null;
                    } catch (JSONException e) {
                        Log.e("BaseChannelCreate", "error " + e.toString());
                    }

                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.userPhoto:
                if(AuthHelper.isLoginUser(mActivity, mChannel.getId())) {
                    Helper.callGallery(this);
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(intent == null) return;

        switch (requestCode) {
            case CODE_GALLERY_ACTIVITY:
                mProgress.show();
                File file = FileHelper.getFileFromUri(mActivity, intent.getData());
                mResizedFile = Helper.imageUploadAndDisplay(mActivity, mApi, file, mResizedFile, mUserPhoto, true);
                break;
        }
    }
}
