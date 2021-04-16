package com.example.stringtracker;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

// StringTracker main activity class WKD
public class MainActivity extends AppCompatActivity implements SessionSentiment.SaveSentListener {
    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();

    public static final String EXTRA_MESSAGE
            = "com.example.android.StringTracker_test.extra.MESSAGE";

    public static final int TEXT_REQUEST = 1;
    private TextView selectedInstrTV;
    private TextView mStringsSel;
    private Button mButton;
    //private boolean isFragmentDisplayed = false;
    static final String STATE_FRAGMENT = "state_of_fragment";
    Button buttonStartSes;
    Button buttonSelInst;
    private String selInstrText;
    private String selStrText;

    // main data objects
    AppState A1 = new AppState();
    StringSet S1 = new StringSet();
    Instrument I1 = new Instrument();
    SessionSent Sent1 = new SessionSent();

    private TextView selInstTV;
    private TextView selStrTV;
    private TextView timeDebugTV;
    Context context = MainActivity.this;  // activity context needed for DB calls

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize AppState.
        //String appstate = null;
        selInstTV = (TextView) findViewById(R.id.selInstrTV);
        selStrTV = (TextView) findViewById(R.id.selStringsTV);
        timeDebugTV = (TextView) findViewById(R.id.timeDebug);
        timeDebugTV.setBackgroundResource(R.color.background1);

        if (A1.init()) {      // if .init() returns true it is the first time the app has been run
            // TODO - direct user to Configuration to add StingSet and Instrument
            //genStrInst();  // DEBUG test stuff
            //appstate = A1.getAppState();

            A1.setInstState(I1.getInstState());  // update object state strings in AppState for 1st run
            A1.setStrState(S1.getStrState());
            updateSelDisplay("FirstRun: ");

        } else {
            loadAppState();
            updateSelDisplay(null);

            // restore Instrument and Strings fro DB
            I1.loadInstr(A1.getInstrID(), context);
            S1.loadStrings(I1.getStringsID(), context);
        }
        // DEBUG forced settings for preferences
        A1.setTestMode(true);
        A1.setEnableSent(true);
        A1.setMaxSessionTime(200);
        S1.setAvgLife(800);

        // Start/Stop Session Button
        buttonStartSes = findViewById(R.id.startButton);
        buttonStartSes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(A1.sessionStarted()) {  // Stop session
                    A1.stopSession();
                    I1.setSessionCnt(I1.getSessionCnt() + 1); // inc session cnt
                    I1.setLastSessionTime(A1.getLastSessionTime());
                    I1.setPlayTime(I1.getPlayTime() + I1.getLastSessionTime());
                    buttonStartSes.setText("Start");
                    buttonStartSes.setBackgroundColor( 0xff00A020);
                    // update DB items
                    I1.updateInstr(I1.getInstrID(), context);
                    S1.updateStrings(S1.getStringsID(), context);

                     if (A1.getEnableSent()) {
                         SessionSentiment SentDialog = new SessionSentiment();
                         FragmentManager fmanager = getSupportFragmentManager();
                         SentDialog.show(fmanager, "RateStrings");

                         // TODO call user sent dialog
                         ////////////////////////
                         //if (A1.getTestMode()) {
                         //    I1.logSessionSent(genRandSent());  // DEBUG store random sent to log file normally from sent dialog
                         // }
                    }
                    updateSelDisplay(null);
                    timeDebugTV.setBackgroundResource(R.color.background1);
                    String timeText = "SessionCnt = "+I1.getSessionCnt() + ", SessionT = " + (A1.getStopT() - A1.getStartT())
                            + "ms \n LastSessT = " + I1.getLastSessionTime() + ", TotalPlayT = " + I1.getPlayTime();
                    timeDebugTV.setText(timeText);
                    timeDebugTV.setVisibility(View.VISIBLE);

                } else {                // Start session
                    A1.startSession();
                    I1.setCurrSessionStart(A1.getCurrSessionStart());
                    buttonStartSes.setText("Stop");
                    buttonStartSes.setBackgroundColor(0xffb02020);


                    timeDebugTV.setText("Session Started");
                    timeDebugTV.setVisibility(View.VISIBLE);
                }
                saveAppState();
            }
        });

        // Select Instrument Button
        buttonSelInst = findViewById(R.id.selInstrButton);
        buttonSelInst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (A1.sessionStarted()) {  // Stop session if running
                    A1.stopSession();
                    I1.setSessionCnt(I1.getSessionCnt() + 1); // inc session cnt
                    I1.setLastSessionTime(A1.getLastSessionTime());
                    I1.setPlayTime(I1.getPlayTime() + I1.getLastSessionTime());
                    buttonStartSes.setText("Start");
                    buttonStartSes.setBackgroundColor(0xff00A020);

                    if (A1.getEnableSent()) {
                        // TODO call user sent dialog
                        ////////////////////////
                        if (A1.getTestMode()) {
                            I1.logSessionSent(genRandSent());  // DEBUG store random sent to log file normally from sent dialog
                        }
                    }
                }
                // update DB items and save app run state
                I1.updateInstr(I1.getInstrID(), context);
                S1.updateStrings(S1.getStringsID(), context);

                // lock button if first run of the program => no data in DB yet
                if(!A1.firstRun()) {
                    launchSelectInstrument(v);
                }
                saveAppState();
            }
        });

    }  // end OnCreate

    // helper to restore app and data object states
    public void loadAppState() {
        A1.loadRunState();  // load prev app state and restore data objects
        I1.setInstState(A1.getInstState());
        S1.setStrState(A1.getStrState());
    }

    // helper to store app and data object states
    public void saveAppState() {
        A1.setInstState(I1.getInstState());  // update object state strings in AppState
        A1.setStrState(S1.getStrState());
        A1.saveRunState();
    }

    // Methods to launch activities
    public void launchConfig(View view) {
        Log.d(LOG_TAG, "Config Button clicked!");
        A1.setInstState(I1.getInstState());  // update object state strings in AppState
        A1.setStrState(S1.getStrState());
        A1.saveRunState();

        Intent intent = new Intent(this, Configuration.class);
        String appState = A1.getAppState();
        String instState = I1.getInstState();
        String strState = S1.getStrState();
        intent.putExtra("appstate", appState);   // forward object states
        intent.putExtra("inststate", instState);
        intent.putExtra("strstate", strState);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    public void launchAnalytics(View view) {
        Log.d(LOG_TAG, "Analytics Button clicked!");
        A1.setInstState(I1.getInstState());  // update object state strings in AppState
        A1.setStrState(S1.getStrState());
        A1.saveRunState();

        Intent intent = new Intent(this, Analytics.class);
        String appState = A1.getAppState();
        String instState = I1.getInstState();
        String strState = S1.getStrState();
        intent.putExtra("appstate", appState);    // forward object states
        intent.putExtra("inststate", instState);
        intent.putExtra("strstate", strState);

        // button lockout for first rum
        if (!A1.firstRun()) {
            startActivityForResult(intent, TEXT_REQUEST);
        }

    }

    public void launchSelectInstrument(View view) {
        Log.d(LOG_TAG, "Sel Instrument Button clicked!");
        A1.setInstState(I1.getInstState());  // update object state strings in AppState
        A1.setStrState(S1.getStrState());
        A1.saveRunState();

        Intent intent = new Intent(this, SelectInstrument.class);
        String appState = A1.getAppState();
        String instState = I1.getInstState();
        String strState = S1.getStrState();
        intent.putExtra("appstate", appState);    // forward object states
        intent.putExtra("inststate", instState);
        intent.putExtra("strstate", strState);

        // button lockout for first rum
        if (!A1.firstRun()) {
            startActivityForResult(intent, TEXT_REQUEST);
        }

    }

    // Method collects returned appState strings (or other messages) and updates object
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String appState;
        String instState;
        String strState;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                appState = data.getStringExtra("appstate");
                instState = data.getStringExtra("inststate");
                strState = data.getStringExtra("strstate");
                //A1.loadRunState();  // DEBUG tests using file for message passing
                A1.setAppState(appState);  // Restore data object states on return
                I1.setInstState(instState);
                S1.setStrState(strState);
                System.out.println("*** RETURN to MAIN InstrID = "+I1.getInstrID());
                updateSelDisplay(null);
                saveAppState();
            }
            // DEBUG MESSAGES
            if (resultCode == RESULT_CANCELED) {
                selInstTV.setText("CANCELED!");
                selInstTV.setVisibility(View.VISIBLE);

            }
        }
    }

    // Method called by SessionSentiment dialog to report back ratings
    @Override
    public void didFinishSentDialog(float ratingProj, float ratingTone, float ratingInton) {
        Sent1.setProj(ratingProj);
        Sent1.setTone(ratingTone);
        Sent1.setInton(ratingInton);
        Sent1.setSessTime(I1.getLastSessionTime());
        Sent1.setTimeStamp(I1.getCurrSessionStart());
        I1.logSessionSent(Sent1);

        Toast.makeText(MainActivity.this, "Strings Ratings: "
                + String.format("Proj=%.2f Tone=%.2f Inton=%.2f", ratingProj, ratingTone, ratingInton), Toast.LENGTH_LONG).show();

    }

    // Helper to update instrument and strings selected
    public void updateSelDisplay(String prefix){
        if(prefix != null) {
            selInstrText = prefix + "Instrument ID:" + A1.getInstrID() + " - " + I1.getBrand() + ": " + I1.getModel();
        } else {
            selInstrText = "Instrument ID:" + A1.getInstrID() + " - " + I1.getBrand() + ": " + I1.getModel();
        }
        selInstTV.setText(selInstrText);
        selInstTV.setBackgroundResource(R.color.background1);
        selInstTV.setVisibility(View.VISIBLE);
        selStrText = "Strings ID:" + S1.getStringsID() + " - " + S1.getBrand() + ": " + S1.getModel();
        ///selStrText = "Strings: " + S1.getStringsID() + " - " + S1.getBrand() + ": " + S1.getModel() + "\n AvgProj: " + S1.getAvgProjStr();
        // select background color based on strings AvgLife
        if(S1.getAvgLife()>0) {
            int pctlife = (int) (((float) I1.getPlayTime() / (float) S1.getAvgLife()) * 100.0f);
            if (pctlife < A1.getLifeThresholds()[1]) {
                selStrTV.setBackgroundResource(R.color.lifecolor1);
            } else if (pctlife < A1.getLifeThresholds()[2]) {
                selStrTV.setBackgroundResource(R.color.lifecolor2);
            } else if (pctlife < A1.getLifeThresholds()[3]) {
                selStrTV.setBackgroundResource(R.color.lifecolor3);
            } else {
                selStrTV.setBackgroundResource(R.color.lifecolor4);
            }
        } else{
            selStrTV.setBackgroundResource(R.color.lifecolor0);   // color for 1st stringset
        }
        selStrTV.setText(selStrText);
        selStrTV.setVisibility(View.VISIBLE);
        timeDebugTV.setBackgroundResource(R.color.background1);
        String timeText = "SessionCnt = "+I1.getSessionCnt() + ", SessionT = " + (A1.getStopT() - A1.getStartT())
                + "ms \n LastSessT = " + I1.getLastSessionTime() + ", TotalPlayT = " + I1.getPlayTime();
        timeDebugTV.setText(timeText);
        timeDebugTV.setVisibility(View.VISIBLE);

    }

/////////////////////////////////////////////////////////////////////////

    // DEBUG test code randomly generates instrument and stringset values for A1 and S1
    public void genStrInst() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        String sBrand[] = {"GHS", "D'Addario", "Martin", "Elixir", "Ernie Bal"};
        String sModel[] = {"A-180", "G-42", "Bronze", "Stainless FW", "Slinky"};
        String sTension[] = {"XL", "Light", "Medium", "Heavy"};
        String sType[] = {"guitar", "banjo", "mandolin", "violin", "cello"};

        String iBrand[] = {"Gibson", "Collings", "Fender", "Taylor", "PRS"};
        String iModel[] = {"L5", "D28", "Artist", "F-5", "Yellowstone"};

        int rand_sBr = rand.nextInt(5);
        int rand_sMo = rand.nextInt(5);
        int rand_sTe = rand.nextInt(4);
        int rand_sTy = rand.nextInt(5);
        int rand_sID = rand.nextInt(4)+1;

        int rand_iBr = rand.nextInt(5);
        int rand_iMo = rand.nextInt(5);
        int rand_iID = rand.nextInt(4)+1;

        S1.setStringsID(rand_sID);
        S1.setBrand(sBrand[rand_sBr]);

        S1.setModel(sModel[rand_sMo]);
        S1.setTension(sTension[rand_sTe]);
        S1.setType(sType[rand_sTy]);
        S1.setAvgLife(800);  // set this value for DEBUG test purposes

        I1.setInstrID(rand_iID);
        I1.setBrand(iBrand[rand_iBr]);
        I1.setModel(iModel[rand_iMo]);
        I1.setType(sType[rand_sTy]);
        I1.setStringsID(rand_sID);  // match up strings with instrument

    }

    public SessionSent genRandSent() {
        SessionSent S = new SessionSent();
        Random rand = new Random();
        int rand_sent = rand.nextInt(10);
        S.setProj((float) rand_sent * 0.5f);
        rand_sent = rand.nextInt(10);
        S.setTone((float) rand_sent * 0.5f);
        rand_sent = rand.nextInt(10);
        S.setInton((float) rand_sent * 0.5f);
        S.setSessTime((int) A1.getLastSessionTime());
        S.setTimeStamp(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));

        return S;
    }

}