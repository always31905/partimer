package com.example.haedal_project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.*;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {
    private Spinner spGu;
    private EditText etDate, etMinPay, etKeyword;
    private Button btnFilter;
    private RecyclerView rvResults;
    private JobPostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        // 바인딩
        spGu      = v.findViewById(R.id.spGu);
        etDate    = v.findViewById(R.id.etDate);
        etMinPay  = v.findViewById(R.id.etMinPay);
        etKeyword = v.findViewById(R.id.etKeyword);
        btnFilter = v.findViewById(R.id.btnFilter);
        rvResults = v.findViewById(R.id.rvSearchResults);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new JobPostAdapter();
        rvResults.setAdapter(adapter);

        // 글 클릭 시 DetailFragment로 이동
        adapter.setOnItemClickListener(post -> {
            DetailFragment detail = new DetailFragment();
            Bundle args = new Bundle();
            args.putString("postId", post.getId());
            detail.setArguments(args);

            // HomeActivity의 changeFragment 메서드 사용
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).changeFragment(detail);
            }
        });

        // 날짜 선택
        etDate.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        String mm = String.format("%02d", month + 1);
                        String dd = String.format("%02d", dayOfMonth);
                        etDate.setText(year + "-" + mm + "-" + dd);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 검색 버튼 클릭 처리
        btnFilter.setOnClickListener(x -> {
            String gu = spGu.getSelectedItem().toString();
            if ("전체".equals(gu)) gu = null;

            String date = etDate.getText().toString().trim();
            if (date.isEmpty()) date = null;

            Integer minPay = null;
            String minStr = etMinPay.getText().toString().trim();
            if (!minStr.isEmpty()) {
                try {
                    minPay = Integer.parseInt(minStr);
                } catch (NumberFormatException ignored) {}
            }

            String kwStr = etKeyword.getText().toString().trim();
            List<String> keywordList = null;
            if (!kwStr.isEmpty()) {
                keywordList = Arrays.stream(kwStr.split("\\s*,\\s*"))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            searchPosts(gu, date, minPay, keywordList);
        });

        return v;
    }

    // 검색 조건으로 Firestore에서 글 목록 불러오기
    private void searchPosts(String gu, String date, Integer minPay, List<String> keywords) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("job_posts");

        // 1) 구 필터 & 정렬 (전체 선택 시 gu == null이므로 스킵)
        if (gu != null) {
            query = query
                    .whereEqualTo("location", gu)
                    .orderBy("location");
        }

        // 2) 날짜 필터 (정렬에는 영향 없음)
        if (date != null) {
            query = query.whereEqualTo("date", date);
        }

        // 3) 시급 범위 필터 & 정렬
        if (minPay != null) {
            query = query
                    .whereGreaterThanOrEqualTo("pay", minPay)
                    .orderBy("pay");           // 범위 필터 필드 먼저
        }

        // 4) 기본 정렬: 최신 순
        query = query.orderBy("createdAt", Query.Direction.DESCENDING);

        // 5) 쿼리 실행
        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<JobPost> result = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        JobPost post = doc.toObject(JobPost.class);
                        if (post != null) {
                            post.setId(doc.getId());

                            // 키워드 매칭
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
                    adapter.setItems(result);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}