package com.umanji.umanjiapp.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.Regex;
import com.leocardz.link.preview.library.SearchUrls;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;
import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.helper.FileHelper;
import com.umanji.umanjiapp.helper.UiHelper;
import com.umanji.umanjiapp.model.SuccessData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;


public class BaseChannelCreateFragment extends BaseFragment {
    private static final String TAG = "BaseChannelCreate";

    /****************************************************
     *  View
     ****************************************************/

    protected AutoCompleteTextView mName;
    protected Button mPhotoBtn;
    protected Button mGallaryBtn;

    protected Button mCreateBtn;
    protected ImageView mPhoto;

    protected TextView mMetaTitle;
    protected TextView mMetaDesc;

    protected LinearLayout mLinearLayout;

    /****************************************************
     *  Etc.
     ****************************************************/
    TextCrawler textCrawler;

    protected String mPhotoUri;
    protected File mResizedFile;

    protected String mCreateApiName;

    // 카메라 찍은 후 저장될 파일 경로
    private String mFilePath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = getView(inflater, container);
        mName = (AutoCompleteTextView) view.findViewById(R.id.name);
        mName.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                ArrayList<String> urls = SearchUrls.matches(mName.getText().toString());

                if(arg1 == 66 && urls.size() > 0) {

                    textCrawler
                            .makePreview(new LinkPreviewCallback() {
                                @Override
                                public void onPre() {
                                    Log.d(TAG, "onPre");
                                    mProgress.show();
                                }

                                @Override
                                public void onPos(SourceContent sourceContent, boolean isNull) {
                                    Log.d(TAG, "onPos");

                                    if(sourceContent.getImages().size() > 0) {
                                        Glide.with(mActivity).load(sourceContent.getImages().get(0)).into(mPhoto);
                                        mPhotoUri = sourceContent.getImages().get(0);
                                    }

                                    if(TextUtils.isEmpty(sourceContent.getTitle())) {
                                        mMetaTitle.setVisibility(View.GONE);
                                    }else {
                                        mMetaTitle.setVisibility(View.VISIBLE);
                                        mMetaTitle.setText(sourceContent.getTitle());
                                    }

                                    if(TextUtils.isEmpty(sourceContent.getDescription())) {
                                        mMetaDesc.setVisibility(View.GONE);
                                    }else {
                                        mMetaDesc.setVisibility(View.VISIBLE);
                                        mMetaDesc.setText(sourceContent.getDescription());
                                    }



                                    mProgress.hide();
                                }
                            }, urls.get(0));
                }
                return false;
            }
        });

        mPhoto = (ImageView) view.findViewById(R.id.photo);

        mCreateBtn = (Button) view.findViewById(R.id.createBtn);
        mCreateBtn.setOnClickListener(this);

        mPhotoBtn = (Button) view.findViewById(R.id.photoBtn);
        mPhotoBtn.setOnClickListener(this);

        mGallaryBtn = (Button) view.findViewById(R.id.gallaryBtn);
        mGallaryBtn.setOnClickListener(this);

        mMetaTitle = (TextView) view.findViewById(R.id.metaTitle);
        mMetaDesc = (TextView) view.findViewById(R.id.metaDesc);

        textCrawler = new TextCrawler();
        super.onCreateView(view);
        return view;
    }

    public View getView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.activity_channel_create, container, false);
        return view;
    }

    @Override
    public void loadData() {

    }

    @Override
    public void updateView() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createBtn:
                create();
                break;
            case R.id.photoBtn:
                mFilePath = UiHelper.callCamera(this);
                break;
            case R.id.gallaryBtn:
                UiHelper.callGallery(this);
                break;
        }
    }

    @Override
    public void onEvent(SuccessData event) {
        if(mCreateApiName != null && mCreateApiName.equals(event.type)) {
            mActivity.finish();
            return;
        }

        switch (event.type) {
            case api_photo:

                try{
                    mResizedFile.delete();
                    mResizedFile = null;
                    mPhotoUri = null;

                    JSONObject data = event.response.getJSONObject("data");
                    mPhotoUri = REST_S3_URL + data.optString("photo");
                }catch(JSONException e) {
                    Log.e("BaseChannelCreate", "error " + e.toString());
                }
                break;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("BaseChannelCreate", "onActivityResult");

        File file = null;
        switch (requestCode) {
            case UiHelper.CODE_GALLERY_ACTIVITY:

                file = FileHelper.getFileFromUri(mContext, intent.getData());
                mResizedFile = UiHelper.imageUploadAndDisplay(mActivity, mApiHelper, file, mResizedFile, mPhoto, false);
                break;
            case UiHelper.CODE_CAMERA_ACTIVITY:
                file = new File(mFilePath);
                mResizedFile = UiHelper.imageUploadAndDisplay(mActivity, mApiHelper, file, mResizedFile, mPhoto, false);
                break;
        }

    }


    /****************************************************
     *  Methods
     ****************************************************/

    protected void create() {
        final String fName = mName.getText().toString();

        try {

            JSONObject params = mChannel.getAddressJSONObject();
            params.put("parent", mChannel.getId());
            params.put("type", mType);
            params.put("name", fName);


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


}
