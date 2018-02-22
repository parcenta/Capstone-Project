package com.peterarkt.customerconnect.ui.customersList;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.databinding.ListItemCustomerBinding;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

/**
 * Created by Andrés on 2/2/18.
 */

public class CustomerListAdapter extends RecyclerView.Adapter<CustomerListAdapter.CustomerListViewHolder>{

    private Context mContext;
    private List<CustomerItem> mItemList;
    private CustomerOnClickHandler mHandler;


    public CustomerListAdapter(Context context, List<CustomerItem> itemList, CustomerOnClickHandler handler){
        this.mContext   = context;
        this.mItemList  = itemList;
        this.mHandler   = handler;
    }

    public void setItemList(List<CustomerItem> itemList){
        this.mItemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public CustomerListAdapter.CustomerListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context         = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        int layoutId  = R.layout.list_item_customer;
        ListItemCustomerBinding binding = DataBindingUtil.inflate(inflater,layoutId,parent,false);

        return new CustomerListAdapter.CustomerListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(CustomerListAdapter.CustomerListViewHolder holder, int position) {
        CustomerItem item = mItemList.get(position);

        // Set Customer Name
        holder.mBinding.itemCustomerName.setText(item.customerName);

        // Set CustomerAddress
        holder.mBinding.itemCustomerAddress.setText(item.customerAddressStreet);

        // Loading image in CircleImageView
        if(item.customerPhotoUrl.trim().length() > 0){
            Picasso.with(mContext)
                    .load("file://"+item.customerPhotoUrl)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.ic_material_person_gray)
                    .into(holder.mBinding.itemCustomerImage);
        }else {
            Picasso.with(mContext)
                    .load(R.drawable.ic_material_person_gray)
                    .fit()
                    .into(holder.mBinding.itemCustomerImage);
        }
    }


    @Override
    public int getItemCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    public interface CustomerOnClickHandler {
        void onCustomerClick(int customerId);
    }

    public class CustomerListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final ListItemCustomerBinding mBinding;


        private CustomerListViewHolder(ListItemCustomerBinding binding){
            super(binding.getRoot());
            mBinding = binding;
            mBinding.getRoot().setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            CustomerItem item = mItemList.get(adapterPosition);
            if(mHandler!=null && item!=null)
                mHandler.onCustomerClick(item.customerId);
            else
                Toast.makeText(mContext,mContext.getString(R.string.an_error_has_ocurred),Toast.LENGTH_SHORT).show();
        }
    }
}
