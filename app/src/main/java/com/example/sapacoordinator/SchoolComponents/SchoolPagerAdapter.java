package com.example.sapacoordinator.SchoolComponents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SchoolPagerAdapter extends FragmentStateAdapter {

    public SchoolPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override

    public Fragment createFragment(int position) {
        if (position == 0) {
            return SchoolList.newInstance("Approved");
        } else if (position == 1) {
            return SchoolList.newInstance("Pending");
        }else {
            return new AddSchoolFragment();
        }
    }


    @Override
    public int getItemCount() {
        return 3; // Two tabs
    }
}

