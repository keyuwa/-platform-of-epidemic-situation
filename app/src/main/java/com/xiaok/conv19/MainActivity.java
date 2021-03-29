package com.xiaok.conv19;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NativeAppFragment nativeAppFragment;
    private WebAppFragment webAppFragment;
    private SettingFragment settingFragment;
    private Fragment[] fragments;
    private int lastFragment;//用于记录上个选择的Fragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //权限检查和申请
        initFragment();
    }

    //初始化fragment和fragment数组
    private void initFragment()
    {

        nativeAppFragment = new NativeAppFragment();
        webAppFragment = new WebAppFragment();
        settingFragment = new SettingFragment();
        fragments = new Fragment[]{nativeAppFragment,webAppFragment,settingFragment};
        lastFragment = 0;
        getSupportFragmentManager().beginTransaction().replace(R.id.lin_lay_fragment,nativeAppFragment).show(nativeAppFragment).commit();
        bottomNavigationView = findViewById(R.id.bnv);

        bottomNavigationView.setOnNavigationItemSelectedListener(changeFragment);
    }
    //判断选择的菜单
    private BottomNavigationView.OnNavigationItemSelectedListener changeFragment= new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId())
            {
                case R.id.home_page:
                {
                    if(lastFragment!=0)
                    {
                        switchFragment(lastFragment,0);
                        lastFragment=0;
                    }
                    return true;
                }
                case R.id.setting:
                {
                    if(lastFragment!=1)
                    {
                        switchFragment(lastFragment,1);
                        lastFragment=1;

                    }

                    return true;
                }
                case R.id.mycenter:
                {
                    if(lastFragment!=2)
                    {
                        switchFragment(lastFragment,2);
                        lastFragment=2;

                    }

                    return true;
                }


            }


            return false;
        }
    };


    //切换Fragment
    private void switchFragment(int lastfragment,int index)
    {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[lastfragment]);//隐藏上个Fragment
        if(fragments[index].isAdded()==false)
        {
            transaction.add(R.id.lin_lay_fragment,fragments[index]);


        }
        transaction.show(fragments[index]).commitAllowingStateLoss();


    }

}
