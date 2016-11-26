package com.example.da.lab9;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Da on 2016/11/27.
 */
public class RecyleViewAdapter extends RecyclerView.Adapter<RecyleViewAdapter.ViewHolder>{

    private List<Map<String,String>> weather_list;
    private LayoutInflater mInflater;

    public interface OnItemClickLitener {
        void onItemClick(View view, int position, Map<String,String> item);
    }
    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public RecyleViewAdapter(Context context, List<Map<String,String>> items) {
        super();
        weather_list = items;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = mInflater.inflate(R.layout.weather_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        holder.Date = (TextView)view.findViewById(R.id.date);
        holder.Weather_description =(TextView)view.findViewById(R.id.weather_description);
        holder.Temperature = (TextView)view.findViewById(R.id.temprature);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i)
    {
        viewHolder.Date.setText(weather_list.get(i).get("date").toString());
        viewHolder.Weather_description.setText(weather_list.get(i).get("weather").toString());
        viewHolder.Temperature.setText(weather_list.get(i).get("temperature").toString());
        if (mOnItemClickLitener != null)
        {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // TODO Auto-generated method stub
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, i,weather_list.get(i));
                }
            });
        }
    }
    @Override
    public int getItemCount()
    {
        return weather_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView)
        {
            super(itemView);
        }
        TextView Date;
        TextView Weather_description;
        TextView Temperature;
    }
}
