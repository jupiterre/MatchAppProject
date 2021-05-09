package com.example.matchappproject;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executor;

public class EasyGameFragment extends Fragment  {
    ImageButton[][] buttonGrid;
    String[][] cardFaces;
    int row = 6;
    ArrayList<String> cardSpaces = new ArrayList<>();
    int col = 3;
    boolean cardAlreadyFlipped = false;
    String cardAlreadyFlippedID = "";

    ImageButton imgBtn1;
    ImageButton imgBtn2;
    long timeWhenStopped;
    String imgID1;
    String imgID2;
    int cardFlippedOverCounter = 0;
    boolean start = false;
    Chronometer chronometer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String user;
    Map<String, Object> userAndScoreInDB;

    SharedPreferences sharedPref;
    View view;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        view = inflater.inflate(R.layout.fragment_game_easy, container, false);

        Menu menu = ((MainActivity)getActivity()).getMenu();
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.match_title);
        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setVisible(false);

        buttonGrid = new ImageButton[row][col];
        timeWhenStopped = 0;

        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                String buttonID = "button" + r + c;
                int id = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());


                buttonGrid[r][c] = view.findViewById(id);
                buttonGrid[r][c].setTag("R.mipmap.ic_card");

                String roww = Integer.toString(r);
                String coll = Integer.toString(c);

                cardSpaces.add(roww + coll);
            }
        }
        Chronometer chronometer = view.findViewById(R.id.chronometer_timer);

        super.onCreateView(inflater, container, savedInstanceState);


        if (savedInstanceState != null) {
            // Restore last state for checked position.
            for (int r = 0; r < row; r++) {
                for (int c = 0; c < col; c++) {
                    String buttonID = "button" + r + c;
                    int id = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());

                    Log.d("ONN CREATE", "id is for buttonID");
                    buttonGrid[r][c] = view.findViewById(id);
                    ImageButton im =  buttonGrid[r][c];
                    String imgResource = savedInstanceState.getString("btn" + r + c, "");
                    Log.d("ONN CREATE", "img resource for " + "btn" + r + c + "is " + imgResource);


                    int imgID = getResources().getIdentifier(imgResource , "mipmap", getActivity().getPackageName());

                    im.setImageResource(imgID);




                }
            }
            start = savedInstanceState.getBoolean("b");
            if (start == true) {
                ((MainActivity)getActivity()).setGameState(1);
                long t = savedInstanceState.getLong("time");
                double min = savedInstanceState.getDouble("min");
                double sec = savedInstanceState.getDouble("sec");
                chronometer.setText(min + ":" + sec);
                timeWhenStopped = t;
                Log.d("TIME", "time when stopped is" + t);
                Log.d("CHRON", "unpause base is " + SystemClock.elapsedRealtime());
                chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                chronometer.start();

            }


        }




        cardFaces = new String[row][col];


        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        user = sharedPref.getString("Username", "Player");

        DocumentReference docRef = db.collection("High Scores Easy").document(user + " Score");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("DB", "setting user and score");
                        userAndScoreInDB = document.getData();
                        Log.d("DB", "DocumentSnapshot data: " + document.getData());
                        if (userAndScoreInDB.isEmpty()) {
                            Log.d("GETTER", "userAndScoreInDb is null");
                        } else {
                            Log.d("GETTER", "userAndScoreInDb is" + userAndScoreInDB);
                        }
                    } else {
                        Log.d("DB", "no such document");
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("AUTH", "get failed with " + task.getException());
                }
            }
        });




        //since there are 18 total cards
        int ind = 0;
        for (int c = 0; c < 18; c++) {
            int lengthLeft = 18 - c;
            System.out.println("lengthleft is " + lengthLeft);
            Random rand = new Random();
            int randSpace = rand.nextInt(lengthLeft);
            Log.d("RANDOM", "rand space in arraylist is" + randSpace);
            int randCard = rand.nextInt(13) + 1;

            String s = cardSpaces.get(randSpace);
            Log.d("RANDOM", "cardspace is" + s);
            char c0 = s.charAt(0);
            char c1 = s.charAt(1);
            int randRow = Integer.parseInt(String.valueOf(c0));
            int randCol = Integer.parseInt(String.valueOf(c1));
            Log.d("RANDOM", "rand row is" + randRow);
            Log.d("RANDOM", "rand col is" + randCol);
            Log.d("POP", "populating cardFaces at " + randRow + " " + randCol);
            cardFaces[randRow][randCol] = "ic_card" + randCard;

            //space is taken up
            int index = cardSpaces.indexOf(s);
            cardSpaces.remove(index);

            //need this repetitive code so there are 2 of each card to match
            c++;
            lengthLeft = 18 - c;
            System.out.println("lengthleft is " + lengthLeft);
            randSpace = rand.nextInt(lengthLeft);
            s = cardSpaces.get(randSpace);
            c0 = s.charAt(0);
            c1 = s.charAt(1);
            randRow = Integer.parseInt(String.valueOf(c0));
            randCol = Integer.parseInt(String.valueOf(c1));
            Log.d("POP", "populating cardFaces at " + randRow + " " + randCol);
            cardFaces[randRow][randCol] = "ic_card" + randCard;

            index = cardSpaces.indexOf(s);
            cardSpaces.remove(index);


            ind++;
        }




        //populates buttonGrid with buttons
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {

                int finalR = r;
                int finalC = c;
                ImageButton im =  buttonGrid[r][c];
                if (Objects.isNull(im)) {
                    Log.d("NULL", "imgBtn not populated right");
                }
                buttonGrid[r][c].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int imageID1 = 0;
                        int imageID2;

                        if (!cardAlreadyFlipped) {
                            if (start == false) {
                                start = true;
                                if (timeWhenStopped == 0) {
                                    Log.d("CHRON", "start base is " + SystemClock.elapsedRealtime());
                                    chronometer.setBase(SystemClock.elapsedRealtime());
                                    chronometer.start();
                                }

                                ((MainActivity)getActivity()).setGameState(1);
                                Log.i("game_state", "Currently in a game");
                            }
                            cardAlreadyFlippedID = cardFaces[finalR][finalC];
                            //set button image to be face of card
                            imgID1 = cardFaces[finalR][finalC];
                            Log.d("ID", "image id1 is " + imgID1);
                            imageID1 = getResources().getIdentifier(imgID1 , "mipmap", getActivity().getPackageName());
                            imgBtn1 = buttonGrid[finalR][finalC];
                            imgBtn1.setImageResource(imageID1);
                            imgBtn1.setTag(String.valueOf(imageID1));
                            cardAlreadyFlipped = true;

                        } else {
                            imgID2 = cardFaces[finalR][finalC];
                            Log.d("ID", "image id2 is " + imgID2);
                            imageID2 = getResources().getIdentifier(imgID2 , "mipmap", getActivity().getPackageName());
                            imgBtn2 = buttonGrid[finalR][finalC];
                            imgBtn2.setImageResource(imageID2);
                            imgBtn2.setTag(String.valueOf(imageID2));

                            if (!imgID1.equals(imgID2)) {
                                Log.d("CARD", imgID1 + " =/= " + imgID2);
                                Log.d("CARD", "CARDS DONT MATCH");
                                Handler handler = new Handler();

                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        imgBtn1.setImageResource(R.mipmap.ic_card);
                                        imgBtn1.setTag("R.mipmap.ic_card");
                                        imgBtn2.setImageResource(R.mipmap.ic_card);
                                        imgBtn2.setTag("R.mipmap.ic_card");

                                    }
                                }, 500);


                            } else {
                                cardFlippedOverCounter += 2;
                            }
                            Log.d("COUNT", "cards flipped over is " + cardFlippedOverCounter);

                            //all cards flipped over
                            if (cardFlippedOverCounter >= 18) {
                                chronometer.stop();
                                ((MainActivity)getActivity()).setGameState(0);
                                Log.i("game_state", "Not in a game");

                                double timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
                                //Log.d("TIME", "mili time taken is " + timeElapsed);
                                timeElapsed /= 1000.0;
                                //Log.d("TIME", "sec time taken is  " + timeElapsed);
                                int minutes = (int) (timeElapsed / 60);
                                //Log.d("TIME", "mine time taken is  " + minutes);


                                double seconds = timeElapsed % 60;
                                // it doesnt work right when i do the below code all in one line
                                seconds *= 1000;
                                seconds = Math.round(seconds);
                                seconds /= 1000;
                                Log.d("TIME", "time taken is  " + minutes + ":" + seconds);



                                String time = minutes + ":" + seconds;

                                HashMap<String, String> userAndScore = new HashMap<>();
                                userAndScore.put(user, time);




                                double timeCurrent = (minutes * 60) + seconds;

                                //initialized it so i can use it in the if-else below
                                //ensures that if timeinDBDoub doesnt exist in database, then current time will still be less
                                double timeinDBDoub = timeCurrent + 1.0;

                                if (!Objects.isNull(userAndScoreInDB)) {
                                    String timeinDBStr = (String) userAndScoreInDB.get(user);
                                    String[] s = timeinDBStr.split(":");
                                    double min = Double.parseDouble(s[0]);
                                    double sec = Double.parseDouble(s[1]);
                                    timeinDBDoub = (min * 60) + sec;
                                }



                                if (Objects.isNull(userAndScoreInDB) || timeCurrent < timeinDBDoub) {
                                    if (Objects.isNull(userAndScoreInDB)) {
                                        Log.d("IF-ELSE", "userAndScoreInDb is null");
                                    }
                                    if (timeCurrent < timeinDBDoub) {
                                        Log.d("IF-ELSE", "timeCurrent < timeinDBDoub = " + timeCurrent + " < " + timeinDBDoub);
                                    }

                                    //each user should only have 1 high score
                                    // so their document is named after them
                                    db.collection("High Scores Easy").document(user + " Score")
                                            .set(userAndScore)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("DB", "DocumentSnapshot successfully written!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("DB", "Error writing document", e);
                                                }
                                            });
                                }



                            }


                            cardAlreadyFlipped = false;

                        }
                    }
                });
            }
        }


        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d("ONN", "onviewCreated is called");
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = ((MainActivity)getActivity()).findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.match_title);

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu = ((MainActivity)getActivity()).getMenu();
        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Chronometer ch = view.findViewById(R.id.chronometer_timer);

        long timeElapsed = ch.getBase() - SystemClock.elapsedRealtime() ;
        outState.putLong("time", timeElapsed);
        timeElapsed /= 1000.0;
        int minutes = (int) (timeElapsed / 60);;

        double seconds = timeElapsed % 60;
        // it doesnt work right when i do the below code all in one line
        seconds *= 1000;
        seconds = Math.round(seconds);
        seconds /= 1000;

        outState.putDouble("min", minutes);
        outState.putDouble("sec", seconds);


        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                String buttonID = "button" + r + c;
                int id = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());


                ImageButton im = view.findViewById(id);

                //imgID1 = cardFaces[r][c];
                //Log.d("ID", "image id1 is " + imgID1);
                String imgID = (String) im.getTag();

                Drawable cardBack = getResources().getDrawable(R.mipmap.ic_card);
                if (imgID.equals("R.mipmap.ic_card")) {
                    Log.d("PUTTING", "putting ic_card in " + "btn" + r + c);
                    outState.putString("btn" + r + c, "ic_card");
                } else {
                    String s = cardFaces[r][c];
                    Log.d("PUTTING", "putting"+ s +" in " + "btn" + r + c);
                    outState.putString("btn" + r + c, s);
                }
                outState.putBoolean("b", start);
                outState.putInt("gamestate", ((MainActivity)getActivity()).getGameState());


            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("ON", "onActivityCreated called");
        super.onActivityCreated(savedInstanceState);

    }

}

