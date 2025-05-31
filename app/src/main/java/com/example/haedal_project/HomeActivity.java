package com.example.haedal_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private FloatingActionButton fabWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fabWrite = findViewById(R.id.fab_write);

        // 초기 HomeFragment 표시
        changeFragment(new HomeFragment());

        // 하단 버튼 바
        ImageButton btnHome    = findViewById(R.id.btn_home);
        ImageButton btnSearch  = findViewById(R.id.btn_search);
        ImageButton btnChat    = findViewById(R.id.btn_chat);
        ImageButton btnAccount = findViewById(R.id.btn_account);

        btnHome.setOnClickListener(v ->
                changeFragment(new HomeFragment())
        );
        btnSearch.setOnClickListener(v ->
                changeFragment(new SearchFragment())
        );
        btnChat.setOnClickListener(v ->
                changeFragment(new ChatFragment())
        );
        btnAccount.setOnClickListener(v ->
                changeFragment(new AccountFragment())
        );

        // FloatingActionButton 클릭 리스너
        fabWrite.setOnClickListener(v ->
                changeFragment(new WriteFragment())
        );
    }

    public void changeFragment(Fragment fragment) {
        // 프래그먼트 전환
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)  // 뒤로가기 지원
                .commit();

        updateFabVisibility(fragment);
    }

    private void updateFabVisibility(Fragment fragment) {
        // 프래그먼트에 따라 FloatingActionButton과 하단 바 표시/숨김
        View bottomBar = findViewById(R.id.bottom_bar);

        if (fragment instanceof HomeFragment) {
            fabWrite.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
        } else if (fragment instanceof WriteFragment) {
            fabWrite.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else {
            fabWrite.setVisibility(View.GONE);
            bottomBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            // 백스택에서 현재 프래그먼트를 제거하기 전에 이전 프래그먼트 확인
            getSupportFragmentManager().popBackStackImmediate();
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            updateFabVisibility(currentFragment);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 네 가지 필터(구, 날짜, 최소시급, 키워드)를 받아
     * 현재 표시 중인 HomeFragment 에 loadPosts를 호출합니다.
     */
    public void loadPosts(String gu,
                          String date,
                          Integer minPay,
                          List<String> keywords) {
        Fragment f = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (f instanceof HomeFragment) {
            ((HomeFragment) f).loadPosts(gu, date, minPay, keywords);
        }
    }
}
