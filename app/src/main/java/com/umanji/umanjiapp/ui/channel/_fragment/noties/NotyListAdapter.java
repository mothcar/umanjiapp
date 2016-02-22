package com.umanji.umanjiapp.ui.channel._fragment.noties;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.umanji.umanjiapp.R;
import com.umanji.umanjiapp.helper.Helper;
import com.umanji.umanjiapp.model.ChannelData;
import com.umanji.umanjiapp.model.ErrorData;
import com.umanji.umanjiapp.model.NotyData;
import com.umanji.umanjiapp.ui.BaseActivity;
import com.umanji.umanjiapp.ui.channel._fragment.BaseChannelListAdapter;
import com.umanji.umanjiapp.ui.channel.community.CommunityActivity;
import com.umanji.umanjiapp.ui.channel.post.PostActivity;
import com.umanji.umanjiapp.ui.channel.spot.SpotActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class NotyListAdapter extends BaseChannelListAdapter {
    private static final String TAG = "BaseChannelListAdapter";

    /****************************************************
     *  ArrayList
     ****************************************************/
    protected ArrayList<NotyData> mNoties;

    public NotyListAdapter(BaseActivity activity, Fragment fragment, ChannelData channelData) {
        super(activity, fragment, channelData);
        mNoties = new ArrayList<NotyData>();
    }



    @Override
    public NotyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_noty, parent, false);

        return new NotyListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NotyListAdapter.ViewHolder holder, int position) {

        final NotyData notyData     = mNoties.get(position);
        final ChannelData channelData = notyData.getChannel();
        final ChannelData userData = notyData.getFrom();
        final ChannelData parentData = notyData.getParent();
        final boolean isRead = notyData.isRead();

        if(channelData == null || channelData.getType() == null) return;

        if(!isRead) {
            holder.mView.setBackgroundResource(R.drawable.feed_new);
        }

        switch (channelData.getType()) {
            case TYPE_POST:
                holder.desc.setText("글작성");
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(channelData.getName());
                setNotyClickEvent(holder, channelData);
                break;
            case TYPE_MEMBER:
                holder.desc.setText("참여");
                holder.name.setVisibility(View.GONE);
                setNotyClickEvent(holder, parentData);
                break;
            case TYPE_LIKE:
                holder.desc.setText("도움");
                holder.name.setVisibility(View.GONE);
                setNotyClickEvent(holder, parentData);
                break;
            case TYPE_SURVEY:
                holder.desc.setText("설문참여");
                holder.name.setVisibility(View.GONE);
                setNotyClickEvent(holder, parentData);
                break;
            default:
                break;
        }

        setUserName(holder, userData);
        setParentName(holder, parentData);
        setUserPhoto(holder, userData);
        setCreatedAt(holder, channelData);
    }

    protected void setNotyClickEvent(final ViewHolder holder, final ChannelData channelData) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.startActivity(mActivity, channelData);
            }
        });
    }

    /****************************************************
     *  methods
     ****************************************************/


    public void addBottom(NotyData doc) {
        mNoties.add(doc);
        mChannels.add(doc.getChannel());
    }

    public void add(NotyData doc) {
        mNoties.add(0, doc);
        mChannels.add(0, doc.getChannel());
    }
}
