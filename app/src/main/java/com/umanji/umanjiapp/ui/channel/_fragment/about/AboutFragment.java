package com.umanji.umanjiapp.ui.channel._fragment.about;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.helper.AuthHelper;
import com.umanji.umanjiapp.helper.Helper;
import com.umanji.umanjiapp.model.AuthData;
import com.umanji.umanjiapp.model.ChannelData;
import com.umanji.umanjiapp.model.SuccessData;
import com.umanji.umanjiapp.model.TestCDN;
import com.umanji.umanjiapp.ui.channel._fragment.BaseChannelListAdapter;
import com.umanji.umanjiapp.ui.channel._fragment.BaseChannelListFragment;
import com.umanji.umanjiapp.ui.channel.advertise.AdsCreateActivity;
import com.umanji.umanjiapp.ui.channel.community.update.CommunityUpdateActivity;
import com.umanji.umanjiapp.ui.channel.complex.update.ComplexUpdateActivity;
import com.umanji.umanjiapp.ui.channel.keyword.create.KeywordCreateActivity;
import com.umanji.umanjiapp.ui.channel.spot.create.SpotCreateActivity;
import com.umanji.umanjiapp.ui.channel.spot.update.SpotUpdateActivity;
import com.umanji.umanjiapp.ui.distribution.DistributionActivity;
import com.umanji.umanjiapp.ui.modal.map.update_address.MapUpdateAddressActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public class AboutFragment extends BaseChannelListFragment {
    private static final String TAG = "AboutFragment";

    protected TextView mEditChannelBtn;
    protected Button mDeleteBtn;
    protected TextView mAddHomeBtn;
    protected TextView advertiseBtn;
    protected TextView mAddress;

    protected LinearLayout mRepresentKeyword;

    protected TextView mNameType;
    protected TextView mName;
    protected TextView mKeywordName;
    protected TextView mUpdatedDate;

    protected Button mDescEditButton;

    protected LinearLayout mAppointmentBar;
    protected TextView mAppointment;

    protected String mUserId;
    protected String mOwnerId;

    private AlertDialog.Builder mAlert;
    boolean mType = false;

    public static AboutFragment newInstance(Bundle bundle) {
        AboutFragment fragment = new AboutFragment();
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
    public View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public BaseChannelListAdapter getListAdapter() {
        return new KeywordListAdapter(mActivity, this, mChannel);
    }

    @Override
    public void initWidgets(View view) {
        mAppointmentBar = (LinearLayout) view.findViewById(R.id.appointmentBar);
        final TestCDN mCdn = new TestCDN();

        mNameType = (TextView) view.findViewById(R.id.nameType);

        try {
            JSONObject param = new JSONObject();
            param.put("access_token", AuthHelper.getToken(mActivity));

            mApi.call(api_token_check, param, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    AuthData auth = new AuthData(object);

                    // info center의 about tab에서 현재 사용자와 info center의 owner이 같은지 비교
                    if (auth != null && auth.getToken() != null) {
                        mUserId = auth.getUser().getId();
                    }
                    String[] centerManagerArr = new String[0];
                    if(mChannel.getOwner() != null){
                        mOwnerId = mChannel.getOwner().getId();
//                        centerManagerArr = mChannel.getOwner().getRoles();
                    } else {
                        mOwnerId = mChannel.getOwnerId();
                    }
/*

                    String[] myRoleArr = auth.getUser().getRoles();
                    String myRole = null;
                    String centerManager = null;
                    if(myRoleArr.length > 0){
                        myRole = myRoleArr[0];
                    }
                    if(centerManagerArr.length >0){
                        centerManager = centerManagerArr[0];
                    }
                    boolean isUpper = false;
                    isUpper = mCdn.isUpper(myRole, centerManager);
*/

                    if (mChannel.getType().equals(TYPE_INFO_CENTER) || mChannel.getType().equals(TYPE_KEYWORD_COMMUNITY)){
                        if(mUserId.equals(mOwnerId)){                         // Info의 owner ID와 User의 ID가 같은면
                            mAppointmentBar.setVisibility(View.VISIBLE);
                        } /*else if (isUpper) {
                            mAppointmentBar.setVisibility(View.VISIBLE);

                        } */else {
                            mAppointmentBar.setVisibility(View.GONE);
                        }

                    } else {
                        mAppointmentBar.setVisibility(View.GONE);
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mDescEditButton = (Button) view.findViewById(R.id.descEditBtn);
        mDescEditButton.setOnClickListener(this);

/*

        JSONObject data = jsonObject;
        if(jsonObject.optJSONObject("data") != null) {
            data = jsonObject.optJSONObject("data");
        }
*/

        JSONObject descObject = new JSONObject();
        descObject = mChannel.getDesc();

        String title = "";
        if(descObject != null && descObject.optString("description") != null){
            title = descObject.optString("description");
        }
        TextView myTV = (TextView) view.findViewById(R.id.clickable_text_view);
        myTV.setText(title);

        mEditChannelBtn = (TextView) view.findViewById(R.id.editChannelBtn);
        mEditChannelBtn.setOnClickListener(this);

        mDeleteBtn = (Button) view.findViewById(R.id.deleteBtn);
        mDeleteBtn.setOnClickListener(this);

        mAddHomeBtn = (TextView) view.findViewById(R.id.addHomeBtn);
        mAddHomeBtn.setOnClickListener(this);

        advertiseBtn = (TextView) view.findViewById(R.id.advertiseBtn);
        advertiseBtn.setOnClickListener(this);

        mAddress = (TextView) view.findViewById(R.id.address);

        mAlert = new AlertDialog.Builder(mActivity);

        mName = (TextView) view.findViewById(R.id.name);
        mName.setText(mChannel.getName());

        mRepresentKeyword = (LinearLayout) view.findViewById(R.id.representKeyword);

        mUpdatedDate = (TextView) view.findViewById(R.id.updatedDate);
        mUpdatedDate.setText(subDate());

        mKeywordName = (TextView) view.findViewById(R.id.keywordName);
        if (mChannel.getKeywords() != null) {
            mRepresentKeyword.setVisibility(View.VISIBLE);
            String[] keywords = mChannel.getKeywords();
            mKeywordName.setText(keywords[0]);
        }

        mAddress.setText(Helper.getFullAddress(mChannel));

        mAppointment = (TextView) view.findViewById(R.id.appointment);
        mAppointment.setOnClickListener(this);
    }

    public String subDate() {
        String fullStr = mChannel.getUpdatedAt();
        String yearStr = fullStr.substring(0, 4);
        String monthStr = fullStr.substring(5, 7);
        String dayStr = fullStr.substring(8, 10);
        String submitDate = yearStr + "년 " + monthStr + "월 " + dayStr + "일";

        return submitDate;
    }


    @Override
    public void loadMoreData() {

    }

    @Override
    public void updateView() {
        //mAddress.setText(Helper.getFullAddress(mChannel));
        mAdapter.notifyDataSetChanged();

        setAddBtn(mActivity, mChannel);
        setDeleteBtn(mActivity, mChannel);
    }

    private void editDescription() {
        ViewSwitcher switcher = (ViewSwitcher) getView().findViewById(R.id.my_switcher);
        switcher.showNext(); //or switcher.showPrevious();
//        TextView myTV = (TextView) switcher.findViewById(R.id.clickable_text_view);
//        myTV.setText(mChannel.getDesc().toString());
    }

    private void request(){
        ViewSwitcher switcher = (ViewSwitcher) getView().findViewById(R.id.my_switcher);
        EditText descEditPanel = (EditText) switcher.findViewById(R.id.hidden_edit_view);
        String description = descEditPanel.getText().toString();

        try {
            JSONObject params = new JSONObject();
            mChannel.setAddressJSONObject(params);
            params.put("id", mChannel.getId());

            JSONObject descParams = new JSONObject();
            descParams.put("description", description);
            params.put("desc", descParams);

            mApi.call(api_channels_id_update, params);

        }catch(JSONException e) {
            Log.e("BaseChannelUpdate", "error " + e.toString());
        }
    }

    private void setAddBtn(Activity activity, ChannelData channelData) {

        if (TextUtils.equals(channelData.getType(), TYPE_INFO_CENTER)) {
            mEditChannelBtn.setVisibility(View.GONE);
        } else {
            mEditChannelBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setDeleteBtn(Activity activity, ChannelData channelData) {
        int loginUserLevel = Integer.parseInt(AuthHelper.getLevel(mActivity));
        switch (channelData.getType()) {
            case TYPE_SPOT:
                if (loginUserLevel < channelData.getLevel()) {
                    mDeleteBtn.setVisibility(View.VISIBLE);
                } else {
                    mDeleteBtn.setVisibility(View.GONE);
                }
                break;
            default:
                if (loginUserLevel < channelData.getLevel() ||
                        channelData.isOwner(AuthHelper.getUserId(mActivity))) {
                    mDeleteBtn.setVisibility(View.VISIBLE);
                } else {
                    mDeleteBtn.setVisibility(View.GONE);
                }
                break;
        }

    }

    @Override
    public void onEvent(SuccessData event) {
        super.onEvent(event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.editChannelBtn:
                startChannelUpdateActivity(mActivity, mChannel);
                break;

            case R.id.descEditBtn:
                if(mDescEditButton.getText().equals("수정")){
                    editDescription();
                    mDescEditButton.setText("제출");
                } else {
                    request();
                    mDescEditButton.setText("수정");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(this).attach(this).commit();
                }

                break;

            case R.id.deleteBtn:
                showDeleteChannelDialog();
                break;

            case R.id.addHomeBtn:
                Bundle bundle = new Bundle();
                bundle.putString("channel", mChannel.getJsonObject().toString());
                bundle.putString("mapType", MAP_UPDATE_ADDRESS);
                Intent intent = new Intent(mActivity, MapUpdateAddressActivity.class);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
                break;

            case R.id.advertiseBtn:
                Bundle adsBundle = new Bundle();
                adsBundle.putString("channel", mChannel.getJsonObject().toString());
                adsBundle.putString("whichAction", "whatever");
                Intent adsIntent = new Intent(mActivity, AdsCreateActivity.class);
                adsIntent.putExtra("bundle", adsBundle);
                startActivity(adsIntent);
                break;

            case R.id.appointment:
                Intent roleIntent = new Intent(mActivity, DistributionActivity.class);
                Bundle roleBundle = new Bundle();
                roleBundle.putString("channel", mChannel.getJsonObject().toString());
                roleIntent.putExtra("bundle", roleBundle);
                startActivity(roleIntent);
                break;
        }
    }

    private void startChannelUpdateActivity(Activity activity, ChannelData channelData) {
        Intent intent = null;

        switch (channelData.getType()) {
            case TYPE_SPOT_INNER:
            case TYPE_SPOT:
                intent = new Intent(activity, SpotUpdateActivity.class);
                break;
            case TYPE_COMMUNITY:
                intent = new Intent(activity, CommunityUpdateActivity.class);
                break;
            case TYPE_INFO_CENTER:
                intent = new Intent(activity, SpotUpdateActivity.class);
                break;
            case TYPE_USER:
                break;
            case TYPE_COMPLEX:
                intent = new Intent(activity, ComplexUpdateActivity.class);
                break;

            case TYPE_KEYWORD_COMMUNITY:
                intent = new Intent(activity, SpotUpdateActivity.class);
                break;

        }
        Bundle bundle = new Bundle();
        bundle.putString("channel", channelData.getJsonObject().toString());
        intent.putExtra("bundle", bundle);
        activity.startActivity(intent);
    }

    private void showDeleteChannelDialog() {
        mAlert.setPositiveButton(R.string.delete_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("id", mChannel.getId());
                    mApi.call(api_channels_id_delete, params, new AjaxCallback<JSONObject>() {
                        @Override
                        public void callback(String url, JSONObject object, AjaxStatus status) {
                            dialog.cancel();
                            mActivity.finish();
                            EventBus.getDefault().post(new SuccessData(api_channels_id_delete, object));
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "Error " + e.toString());
                }
            }
        });

        mAlert.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        mAlert.setTitle(R.string.delete_confirm);
        mAlert.show();
    }
}
