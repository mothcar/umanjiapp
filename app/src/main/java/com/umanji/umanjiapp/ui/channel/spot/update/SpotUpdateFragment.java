package com.umanji.umanjiapp.ui.channel.spot.update;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.model.SuccessData;
import com.umanji.umanjiapp.ui.channel.BaseChannelUpdateFragment;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;


public class SpotUpdateFragment extends BaseChannelUpdateFragment {
    private static final String TAG = "SpotUpdateFragment";

    private Spinner mFloorSpinner;

    public static SpotUpdateFragment newInstance(Bundle bundle) {
        SpotUpdateFragment fragment = new SpotUpdateFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mFloorSpinner = (Spinner) view.findViewById(R.id.floorSpinner);

        String[] floorList = mActivity.getResources().getStringArray(R.array.floorList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                R.layout.widget_spinner_item, floorList);

        mFloorSpinner.setAdapter(adapter);

        return view;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_spot_update, container, false);
    }


    @Override
    protected void request() {
        try {
            JSONObject params = new JSONObject();
            setChannelParams(params);
            setSpotDesc(params);
            mApi.call(api_channels_id_update, params);

        }catch(JSONException e) {
            Log.e("BaseChannelUpdate", "error " + e.toString());
        }
    }

    protected void setSpotDesc(JSONObject params) throws JSONException {
        String floor = mFloorSpinner.getSelectedItem().toString();

        JSONObject descParams = new JSONObject();
        descParams.put("floor", Integer.parseInt(floor.substring(0, floor.indexOf("F") - 1)));
        params.put("desc", descParams);
    }

    @Override
    public void updateView() {
        super.updateView();

        setName(mActivity, mChannel);
        setAddress(mActivity, mChannel);
        setPhoto(mActivity, mChannel);
    }

    @Override
    public void onEvent(SuccessData event) {
        super.onEvent(event);

        switch (event.type) {
            case api_channels_id_update:
                mActivity.finish();
                EventBus.getDefault().post(new SuccessData(EVENT_UPDATEVIEW, null));
                break;
        }
    }


}
