package com.umanji.umanjiapp.ui.channel.community.create;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.model.SuccessData;
import com.umanji.umanjiapp.ui.channel.BaseChannelCreateFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CommunityCreateFragment extends BaseChannelCreateFragment {
    private static final String TAG = "CommunityCreateFragment";

    public static CommunityCreateFragment newInstance(Bundle bundle) {
        CommunityCreateFragment fragment = new CommunityCreateFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void initWidgets(View view) {
        super.initWidgets(view);

        mSubmitBtn.setText("커뮤니티 생성");
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_channel_create, container, false);
    }

    @Override
    protected void request() {
        try {
            JSONObject params = mChannel.getAddressJSONObject();
            params.put("parent", mChannel.getId());
            params.put("name", mName.getText().toString());
            params.put("type", TYPE_COMMUNITY);


            if(mPhotoUri != null) {
                ArrayList<String> photos = new ArrayList<>();
                photos.add(mPhotoUri);
                params.put("photos", new JSONArray(photos));
                mPhotoUri = null;
            }

            mApi.call(api_channels_create, params);

        }catch(JSONException e) {
            Log.e("BaseChannelCreate", "error " + e.toString());
        }
    }

    @Override
    public void onEvent(SuccessData event) {
        super.onEvent(event);

        switch (event.type) {
            case api_channels_create:
                mActivity.finish();
                break;
        }
    }


}