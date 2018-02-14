package com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.databinding.ListItemCustomerVisitsBinding;

import java.util.List;

public class CustomerVisitsAdapter  extends RecyclerView.Adapter<CustomerVisitsAdapter.CustomerVisitsViewHolder>{

    private List<CustomerVisit> mItemList;

    CustomerVisitsAdapter(List<CustomerVisit> itemList){
        this.mItemList  = itemList;
    }

    void setItemList(List<CustomerVisit> itemList){
        this.mItemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public CustomerVisitsAdapter.CustomerVisitsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context         = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        int layoutId  = R.layout.list_item_customer_visits;
        ListItemCustomerVisitsBinding binding = DataBindingUtil.inflate(inflater,layoutId,parent,false);

        return new CustomerVisitsAdapter.CustomerVisitsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(CustomerVisitsAdapter.CustomerVisitsViewHolder holder, int position) {
        CustomerVisit item = mItemList.get(position);

        // Set Visit Commentary
        holder.mBinding.itemVisitCommentary.setText(item.visitCommentary);

        // Set Visit Date
        holder.mBinding.itemVisitDate.setText(item.visitDateString);

    }


    @Override
    public int getItemCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    public class CustomerVisitsViewHolder extends RecyclerView.ViewHolder{

        final ListItemCustomerVisitsBinding mBinding;


        private CustomerVisitsViewHolder(ListItemCustomerVisitsBinding binding){
            super(binding.getRoot());
            mBinding = binding;
        }

    }
}

