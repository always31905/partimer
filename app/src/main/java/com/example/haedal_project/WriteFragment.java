package com.example.haedal_project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;
import java.util.stream.Collectors;

public class WriteFragment extends Fragment {
    private Spinner spGu;
    private EditText etTitle, etContent, etStartTime, etEndTime, etPay, etKeyword;
    private TextView tvDate;
    private Button btnPost;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_write, container, false);

        // 바인딩
        etTitle     = v.findViewById(R.id.etTitle);
        etContent   = v.findViewById(R.id.etContent);
        spGu        = v.findViewById(R.id.spGuWrite);
        tvDate      = v.findViewById(R.id.tvDate);
        etStartTime = v.findViewById(R.id.etStartTime);
        etEndTime   = v.findViewById(R.id.etEndTime);
        etPay       = v.findViewById(R.id.etPay);
        etKeyword   = v.findViewById(R.id.etKeyword);
        btnPost     = v.findViewById(R.id.btnPost);


        tvDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getContext(),
                    (view1, y, m, d) -> {
                        // 선택된 날짜를 "YYYY-MM-DD" 형식으로 표시
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                        tvDate.setText(selectedDate);
                    }, year, month, day);

            dialog.show();
        });


        btnPost.setOnClickListener(x -> {
            String title   = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            String gu      = spGu.getSelectedItem().toString();
            String date    = tvDate.getText().toString();
            String start   = etStartTime.getText().toString().trim();
            String end     = etEndTime.getText().toString().trim();
            String payTxt  = etPay.getText().toString().trim();
            String kwTxt   = etKeyword.getText().toString().trim();

            // 검증
            if (title.isEmpty() || content.isEmpty()
                    || date.isEmpty() || start.isEmpty() || end.isEmpty()
                    || payTxt.isEmpty()) {
                Toast.makeText(getContext(),
                        "제목·내용·날짜·시작시간·종료시간·시급은 필수입니다",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int pay;
            try { pay = Integer.parseInt(payTxt); }
            catch (NumberFormatException e) {
                Toast.makeText(getContext(),
                        "시급은 숫자로 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String,Object> post = new HashMap<>();
            post.put("writerUid",
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            post.put("title",    title);
            post.put("content",  content);
            post.put("location", gu);
            post.put("date",     date);       // "YYYY-MM-DD"
            post.put("startTime", start);     // "HH:MM"
            post.put("endTime",   end);       // "HH:MM"
            post.put("pay",       pay);
            List<String> keywordList = Arrays.stream(kwTxt.split("\\s*,\\s*"))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            post.put("keywords", keywordList);
            post.put("status",   "open");
            post.put("createdAt", FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("job_posts")
                    .add(post)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(getContext(),
                                "게시글 등록 완료", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof HomeActivity) {
                            HomeActivity act = (HomeActivity) getActivity();
                            act.loadPosts(null, null, null, null);
                            act.changeFragment(new HomeFragment());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "등록 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        return v;
    }
}
