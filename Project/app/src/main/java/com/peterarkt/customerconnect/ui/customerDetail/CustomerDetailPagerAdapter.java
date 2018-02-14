package com.peterarkt.customerconnect.ui.customerDetail;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.peterarkt.customerconnect.ui.customerDetail.customerDetailHeader.CustomerDetailHeaderFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo.CustomerDetailInfoFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits.CustomerVisitsFragment;

/**
 * Created by Andrés on 2/8/18.
 */

public class CustomerDetailPagerAdapter extends FragmentStatePagerAdapter{

    private int tabsCount;
    private int mCustomerId;

    public CustomerDetailPagerAdapter(FragmentManager fragmentManager, int tabsCount, int customerId) {
        super(fragmentManager);
        this.tabsCount   = tabsCount;
        this.mCustomerId = customerId;
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
            case 0: // Fragment # 0 - This will show FirstFragment
                return "Details";
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return "Visits";
            default:
                return null;
        }
    }

}
