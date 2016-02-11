package com.umanji.umanjiapp.ui.fragment.about.edit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.helper.CommonHelper;
import com.umanji.umanjiapp.helper.UiHelper;
import com.umanji.umanjiapp.ui.base.BaseChannelCreateFragment;
import com.umanji.umanjiapp.ui.page.channel.profile.ProfileActivity;
import com.umanji.umanjiapp.ui.page.map.MapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class AboutEditFragment extends BaseChannelCreateFragment {
    private static final String TAG = "AboutEditFragment";

    protected Spinner mFloorSpinner;

    protected TextView mAddress;
    protected Button mChangeAddressBtn;


    public static AboutEditFragment newInstance(Bundle bundle) {
        AboutEditFragment fragment = new AboutEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCreateApiName = api_channels_spots_update;
        //mType = TYPE_SPOT;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getView(inflater, container);

        mName = (AutoCompleteTextView) view.findViewById(R.id.name);
        mPhoto = (ImageView) view.findViewById(R.id.photo);

        mCreateBtn = (Button) view.findViewById(R.id.createBtn);
        mCreateBtn.setOnClickListener(this);

        mPhotoBtn = (Button) view.findViewById(R.id.photoBtn);
        mPhotoBtn.setOnClickListener(this);

        // gallaryBtn  mGallaryBtn
        mGallaryBtn = (Button) view.findViewById(R.id.gallaryBtn);
        mGallaryBtn.setOnClickListener(this);

        mAddress = (TextView) view.findViewById(R.id.address);
        mChangeAddressBtn = (Button) view.findViewById(R.id.changeAddressBtn);
        mChangeAddressBtn.setOnClickListener(this);

        mFloorSpinner = (Spinner) view.findViewById(R.id.floorSpinner);

        String[] floorList = mActivity.getResources().getStringArray(R.array.floorList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                R.layout.widget_spinner_item, floorList);

        mFloorSpinner.setAdapter(adapter);
        super.onCreateView(view);

        updateView();
        return view;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.activity_spot_update, container, false);
        return view;
    }


    @Override
    public void updateView() {
        super.updateView();
        mName.setText(mChannel.getName());
        mAddress.setText(CommonHelper.getFullAddress(mChannel));
    }

    @Override
    protected void create() {
        final String fName = mName.getText().toString();
        String floor = mFloorSpinner.getSelectedItem().toString();
        final int fFloor = Integer.parseInt(floor.substring(0, floor.indexOf("F") - 1));

        Bundle mbundle = getArguments();
        mType = mbundle.getString(mChannel.getType());

        try {

            JSONObject params = mChannel.getAddressJSONObject();
            params.put("id", mChannel.getId());
            params.put("type", mType);
            params.put("name", fName);

            JSONObject descParams = new JSONObject();
            descParams.put("floor", fFloor);

            params.put("desc", descParams);

            if(mPhotoUri != null) {
                ArrayList<String> photos = new ArrayList<>();
                photos.add(mPhotoUri);
                params.put("photos", new JSONArray(photos));
                mPhotoUri = null;
            }

            mApiHelper.call(mCreateApiName, params);

        }catch(JSONException e) {
            Log.e("BaseChannelCreate", "error " + e.toString());
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.changeAddressBtn:
                Intent intent = new Intent(mContext, MapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("channel", mChannel.getJsonObject().toString());
                intent.putExtra("bundle", bundle);
                startActivity(intent);
                break;
        }

    }
}
