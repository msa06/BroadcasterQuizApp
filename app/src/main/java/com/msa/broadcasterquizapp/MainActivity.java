package com.msa.broadcasterquizapp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msa.broadcasterquizapp.Model.Question;
import com.msa.broadcasterquizapp.Model.Quiz;
import com.msa.broadcasterquizapp.Model.QuizStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView questionText;
    private Button optbtn1, optbtn2, optbtn3, optbtn4;
    private Button startbtn, showbtn, nextbtn, endbtn;
    private DatabaseReference mQuizStatusReference;
    private DatabaseReference mQuizReference;
    private DatabaseReference mQuestionsReference;
    private String liveStatus, showQuestion;
    private String currentQuesno = "1";
    private List<Question> questionList;
    private ValueEventListener mQuizStatusListner;
    private ValueEventListener mQuizListener;
    private ValueEventListener mQuestionListner;
    private VideoView v1;
    private Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/quizapp-36880.appspot.com/o/Phree%20-%20Make%20the%20world%20your%20paper.mp4?alt=media&token=e2943cc3-554a-4cd2-a23e-d38256aa28ed");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intialise Ui
        initUI();

        //setting On CLick Listner
        attachOnClickListner();

        //Listen to Quiz Status
        attachQuizStatusListener();

        //Listner to Quiz
        attachQuizListener();

        //Listner to Question
        attachQuestionListener();

    }

    private void initUI() {
        questionText = (TextView) findViewById(R.id.question_text);
        optbtn1 = (Button) findViewById(R.id.optionbtn1);
        optbtn2 = (Button) findViewById(R.id.optionbtn2);
        optbtn3 = (Button) findViewById(R.id.optionbtn3);
        optbtn4 = (Button) findViewById(R.id.optionbtn4);
        startbtn = (Button) findViewById(R.id.start_btn);
        showbtn = (Button) findViewById(R.id.showque_btn);
        nextbtn = (Button) findViewById(R.id.nextque_btn);
        endbtn = (Button) findViewById(R.id.end_btn);
        mQuizStatusReference = FirebaseDatabase.getInstance().getReference().child("QuizStatus");
        mQuestionsReference = FirebaseDatabase.getInstance().getReference().child("Question");
        mQuizReference = FirebaseDatabase.getInstance().getReference().child("Quizzes");
        questionList = new ArrayList<>();
        v1=findViewById(R.id.video);
    }

    private void updateQuestion(String currentQuestionNo, List<Question> questions) {
        int currentno = Integer.parseInt(currentQuestionNo);
        questionText.setText(questions.get(currentno).getQues());
        optbtn1.setText(questions.get(currentno).getOp1());
        optbtn2.setText(questions.get(currentno).getOp2());
        optbtn3.setText(questions.get(currentno).getOp3());
        optbtn4.setText(questions.get(currentno).getOp4());
    }

    private void attachOnClickListner() {
        //To Start the Quiz
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                liveStatus = "1";
                v1.setVideoURI(uri);
                v1.requestFocus();
                v1.start();

                showQuestion = "1";
               currentQuesno = "0";
                updateChange();
            }
        });

        //To showQuestion
        showbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showQuestion == "0") {
                    showQuestion = "1";
                    showbtn.setText("Hide Question");
                } else {
                    showQuestion = "0";
                    showbtn.setText("Show Question");
                }
                updateChange();
            }
        });

        //To Show Next Question
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curr = Integer.parseInt(currentQuesno);
                curr++;
                if (curr > 5) {
                    curr = 0;
                }
                currentQuesno = Integer.toString(curr);
                updateQuestion(currentQuesno, questionList);
                updateChange();
            }
        });

        //TO End the Quiz
        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                liveStatus = "0";
                updateChange();
            }
        });
    }

    private void updateChange() {
        QuizStatus status = new QuizStatus(liveStatus, showQuestion, currentQuesno);
        mQuizStatusReference.setValue(status);
    }

    private void attachQuestionListener() {
        if (mQuestionListner == null) {
            mQuestionListner = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot questionsnap : dataSnapshot.getChildren()) {
                        Question questions = questionsnap.getValue(Question.class);
                        questionList.add(questions);
                    }
                   // updateQuestion(currentQuesno, questionList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mQuestionsReference.addValueEventListener(mQuestionListner);
        }
    }

    private void attachQuizListener() {
        if (mQuizListener == null) {
            mQuizListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Quiz quiz = dataSnapshot.getValue(Quiz.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mQuizReference.addValueEventListener(mQuizListener);
        }
    }

    private void attachQuizStatusListener() {
        if (mQuizStatusListner == null) {
            mQuizStatusListner = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    QuizStatus status = dataSnapshot.getValue(QuizStatus.class);
                    liveStatus = status.getLive();
                    currentQuesno = status.getCurques();
                    showQuestion = status.getShowques();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mQuizStatusReference.addValueEventListener(mQuizStatusListner);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachAllListener();
    }

    private void detachAllListener() {
        if(mQuestionListner!=null)
            mQuestionListner = null;
        if(mQuizListener!=null)
            mQuizListener = null;
        if(mQuizStatusListner!=null)
            mQuizStatusListner = null;
    }
}
