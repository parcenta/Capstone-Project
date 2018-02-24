package com.peterarkt.customerconnect.ui.customerDetail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo.CustomerDetailInfoFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits.CustomerVisitsFragment;


public class CustomerDetailPagerAdapter extends FragmentStatePagerAdapter{

    private int tabsCount;
    private int mCustomerId;

    private String infoTabTitle = "";
    private String visitsTabTitle = "";


    public CustomerDetailPagerAdapter(Context context,FragmentManager fragmentManager, int tabsCount, int customerId) {
        super(fragmentManager);
        this.tabsCount   = tabsCount;
        this.mCustomerId = customerId;

        this.infoTabTitle   = context.getString(R.string.info_tab_title);
        this.visitsTabTitle = context.getString(R.string.visits_tab_title);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return tabsCount;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Tab "Detail"
                return CustomerDetailInfoFragment.newInstance(mCustomerId);
            case 1: // Tab "Visits"
                return CustomerVisitsFragment.newInstance(mCustomerId);
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return infoTabTitle;
            case 1:
                return visitsTabTitle;
            default:
                return null;
        }
    }

}
