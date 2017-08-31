package com.zolipe.communitycensus.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.model.FamilyHead;

import java.util.ArrayList;

public class HorizantalListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<FamilyHead> familyHeads;
    private String TAG  = "ListAdapter";

    public HorizantalListAdapter(Context context, ArrayList<FamilyHead> familyHeads) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.familyHeads = familyHeads;
    }

    @Override
    public int getCount() {
        return familyHeads.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.custom_horizantal_list, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }
        holder.pos = position;
        holder.familyHeadNameTV.setTag(position); // This line is important.
        holder.relationTV.setTag(position); // This line is important.

        holder.familyHeadNameTV.setText("Name : " + familyHeads.get(holder.pos).getFirst_name()+" "+familyHeads.get(holder.pos).getLast_name());
        holder.relationTV.setText("Relation : " + familyHeads.get(holder.pos).getRelationship());
        holder.age.setText("Age : " + familyHeads.get(holder.pos).getAge());
        holder.gender.setText("Gender : " + familyHeads.get(holder.pos).getGender());

        Glide.with(context).load(familyHeads.get(holder.pos).getImage_url())
//                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_family_head)
                .into(holder.familyHeadIMG);

        return convertView;
    }


    private static class ViewHolder {
        private TextView familyHeadNameTV, relationTV, age, gender;
        private int pos;
        private ImageView familyHeadIMG;

        public ViewHolder(View v) {
            familyHeadNameTV = (TextView) v.findViewById(R.id.familyHeadNameTV);
            relationTV = (TextView) v.findViewById(R.id.relationTV);
            age = (TextView) v.findViewById(R.id.tv_age_hl);
            gender = (TextView) v.findViewById(R.id.tv_gender_hl);
            familyHeadIMG = (ImageView) v.findViewById(R.id.familyHeadIMG);
        }
    }

}
