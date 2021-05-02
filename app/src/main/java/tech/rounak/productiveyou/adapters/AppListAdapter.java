package tech.rounak.productiveyou.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import tech.rounak.productiveyou.models.AppModel;
import tech.rounak.productiveyou.R;

/**
 * Created by Rounak
 * For more info visit https://rounak.tech
 **/


public class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<AppModel> data;
    private Context context;
    int frag = 0;

    public void setFrag(int frag) {
        this.frag = frag;
    }

    private class AppsViewHolder extends RecyclerView.ViewHolder{

        private TextView txtAppName;
        private ImageView appIcon;
        private TextView txtAppUsage;

        private AppsViewHolder(View view) {
            super (view);
            txtAppName = (TextView) view.findViewById(R.id.txt_app_label);
            txtAppUsage = (TextView) view.findViewById(R.id.txt_app_time);
            appIcon = (ImageView) view.findViewById(R.id.app_icon);
            }


    }

    public AppListAdapter(Context context, List<AppModel> data) {
        super();
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.card_app, parent, false);
        return new AppsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (data.size() > 0) {
            AppModel appInfo = data.get(position);
            AppsViewHolder holder = (AppsViewHolder) viewHolder;
            holder.txtAppName.setText(appInfo.getAppName());
            holder.appIcon.setImageDrawable(appInfo.getAppIcon());
            holder.txtAppUsage.setText(appInfo.getUsageTime());
        }
    }

    @Override
    public int getItemCount() {

        if (frag == 0) {
            return Math.min(data.size(), 5);
        }else{
            return data.size();
        }

//        return data.size();
    }
//
//    public interface OnSettingsChangedListener {
//        void onListChanged(String pkgName,
//                           boolean isBlocked);
//    }
//
//    public void setFilter(List<AppInfo> countryModels){
//        data = new ArrayList<>();
//        data.addAll(countryModels);
//        notifyDataSetChanged();
//    }

}