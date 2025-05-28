package com.example.haedal_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private JobPostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rv = v.findViewById(R.id.rvPosts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new JobPostAdapter();
        rv.setAdapter(adapter);

        // 클릭 리스너 설정
        adapter.setOnItemClickListener(post -> {
            // DetailFragment로 이동하며 post ID 전달
            DetailFragment detail = new DetailFragment();
            Bundle args = new Bundle();
            args.putString("postId", post.getId());
            detail.setArguments(args);

            // HomeActivity의 changeFragment 메서드 사용
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).changeFragment(detail);
            }
        });

        // 기본 전체 로드
        loadPosts(null, null, null, null);
        return v;
    }

    /**
     * @param gu      근무 구(區) 필터 (null 이면 전체)
     * @param date    근무 날짜 필터 (YYYY-MM-DD, null 이면 전체)
     * @param minPay  최소 시급 필터 (null 이면 전체)
     * @param keywords 키워드 포함 검색 (null 이면 전체 정렬)
     */
    public void loadPosts(String gu, String date, Integer minPay, List<String> keywords) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("job_posts")
                .orderBy("createdAt", Query.Direction.DESCENDING);  // 최신순ㅇ로 정렬

        if (gu != null)
            query = query.whereEqualTo("location", gu);
        if (date != null)
            query = query.whereEqualTo("date", date);
        if (minPay != null)
            query = query.whereGreaterThanOrEqualTo("pay", minPay);

        query.get().addOnSuccessListener(snapshot -> {
            List<JobPost> result = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                JobPost post = doc.toObject(JobPost.class);
                if (post != null) {
                    // 명시적으로 document ID 설정
                    post.setId(doc.getId());

                    boolean match = true;

                    if (keywords != null && !keywords.isEmpty()) {
                        match = false;
                        for (String input : keywords) {
                            if (post.getKeywords() != null &&
                                    post.getKeywords().contains(input)) {
                                match = true;
                                break;
                            }
                        }
                    }

                    if (match) result.add(post);
                }
            }
            updateRecyclerView(result);
        }).addOnFailureListener(e -> {
            // 에러 처리 추가
            if (getActivity() != null) {
                Toast.makeText(getActivity(),
                    "게시글을 불러오는데 실패했습니다: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView(List<JobPost> posts) {
        adapter.setItems(posts);
    }
}
