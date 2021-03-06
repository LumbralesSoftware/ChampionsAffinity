package com.thedeveloperworldisyours.championsaffinity.rating;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumbralessoftware.voterussia2018.Utils;
import com.lumbralessoftware.voterussia2018.Vote;
import com.lumbralessoftware.voterussia2018.rating.RatingContract;

import java.text.DecimalFormat;

import static com.thedeveloperworldisyours.championsaffinity.Constantss.FIREBASE_ID;
import static com.thedeveloperworldisyours.championsaffinity.Constantss.FIREBASE_VOTE;
import static com.thedeveloperworldisyours.championsaffinity.Constantss.PLAYERS;
import static com.thedeveloperworldisyours.championsaffinity.Constantss.VOTE;
import static com.thedeveloperworldisyours.championsaffinity.SharedPreferences.isVote;
import static com.thedeveloperworldisyours.championsaffinity.SharedPreferences.setVote;

/**
 * Created by javiergonzalezcabezas on 9/6/18.
 */

public class RatingPresenter implements RatingContract.Presenter {
    RatingContract.View view;
    int idPlayer;
    AppCompatActivity activity;

    DatabaseReference databaseReference;
    DatabaseReference vote;
    Vote voteData;

    public RatingPresenter(RatingContract.View view, int idPlayer, AppCompatActivity activity) {
        this.view = view;
        this.idPlayer = idPlayer;
        this.activity = activity;
        view.setPresenter(this);
    }

    @Override
    public void sumit() {

    }

    @Override
    public void getVotes() {

        if (Utils.INSTANCE.isOnline(activity)) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            vote = databaseReference.child(VOTE);
            vote.orderByChild(FIREBASE_ID).equalTo(idPlayer).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot children : dataSnapshot.getChildren()) {
                        voteData = children.getValue(Vote.class);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    view.showError();
                }
            });
        } else {
            view.noInternet();
        }
    }

    @Override
    public void addValueVote(int vote) {
        if (isVote(activity, String.valueOf(idPlayer))) {
            view.votedMessage();
            view.dismissDialog();
        } else {
            switch (vote) {
                case 1:
                    voteData.setOne(voteData.getOne() + 1);
                    voteData.setSum(voteData.getSum() + 1);
                    break;
                case 2:
                    voteData.setTwo(voteData.getTwo() + 1);
                    voteData.setSum(voteData.getSum() + 2);
                    break;
                case 3:
                    voteData.setThree(voteData.getThree() + 1);
                    voteData.setSum(voteData.getSum() + 3);
                    break;
                case 4:
                    voteData.setFour(voteData.getFour() + 1);
                    voteData.setSum(voteData.getSum() + 4);
                    break;
                case 5:
                    voteData.setFive(voteData.getFive() + 1);
                    voteData.setSum(voteData.getSum() + 5);
                    break;
            }
            voteData.setTotal(voteData.getTotal() + 1);
            updateVote();
        }
    }

    @Override
    public void updateVote() {

        DatabaseReference voteReference = vote.child(String.valueOf(idPlayer));
        voteReference.setValue(voteData);
        updatePlayer(calculateVote());
    }

    @Override
    public float calculateVote() {
        return (float) voteData.getSum() / (float) voteData.getTotal();
    }

    @Override
    public void updatePlayer(float vote) {

        DecimalFormat df = new DecimalFormat("#.#");

        DatabaseReference player = databaseReference.child(PLAYERS);
        DatabaseReference playerReference = player.child(String.valueOf(idPlayer));
        DatabaseReference voteReference = playerReference.child(FIREBASE_VOTE);
        voteReference.setValue(Double.valueOf(df.format(vote)));

        setVote(activity, String.valueOf(idPlayer), true);
        view.successfulVote();
        view.dismissDialog();
    }
}
