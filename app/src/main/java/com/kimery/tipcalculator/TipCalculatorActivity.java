package com.kimery.tipcalculator;

import java.text.NumberFormat;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class TipCalculatorActivity extends Activity 
implements OnEditorActionListener, OnClickListener {

    // define variables for the widgets
    private EditText billAmountEditText;
    private TextView percentTextView;
    private Button percentUpButton;
    private Button percentDownButton;
    private TextView tipTextView;
    private TextView totalTextView;
    private TextView dateTextView;
    MyDBHandler dbHandler;
    private static final String TAG = "TipCalculatorActivity";

    // define instance variables that should be saved
    private float billAmount;
    private String billAmountString = "";
    private float tipPercent = .15f;

    // set up preferences
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        // get references to the widgets
        billAmountEditText = (EditText) findViewById(R.id.billAmountEditText);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
        percentUpButton = (Button) findViewById(R.id.percentUpButton);
        percentDownButton = (Button) findViewById(R.id.percentDownButton);
        tipTextView = (TextView) findViewById(R.id.tipTextView);
        totalTextView = (TextView) findViewById(R.id.totalTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);

        // set the listeners
        billAmountEditText.setOnEditorActionListener(this);
        percentUpButton.setOnClickListener(this);
        percentDownButton.setOnClickListener(this);
        // get default SharedPreferences object
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        dbHandler = new MyDBHandler(this, null, null, 1);
        String data = dbHandler.getArray(dbHandler.getTips(1));
        Log.d(TAG, "\nDatabase:" + data);
        String date = dbHandler.getDate();
        Log.d(TAG, "\nDate" + date);
        dateTextView.setText(date);
    }

    @Override
    public void onPause() {
        // save the instance variables       
        Editor editor = prefs.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // get the instance variables
        billAmountString = prefs.getString("billAmountString", "");
        //set average tip percent
        tipPercent = dbHandler.getAverage();

        // set the bill amount on its widget
        billAmountEditText.setText(billAmountString);

        // calculate and display
        calculateAndDisplay();
    }

    public void calculateAndDisplay() {

        // get the bill amount
        billAmountString = billAmountEditText.getText().toString();

        if (billAmountString.equals("")) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }

        // calculate tip and total 
        float tipAmount = billAmount * tipPercent;
        float totalAmount = billAmount + tipAmount;

        // display the other results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));

        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            calculateAndDisplay();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.percentDownButton:
                tipPercent = tipPercent - .01f;
                calculateAndDisplay();
                break;
            case R.id.percentUpButton:
                tipPercent = tipPercent + .01f;
                calculateAndDisplay();
                break;
        }
    }

    public void saveClick(View view) {
        //call method to save current screen tip  to database
        dbHandler.saveTip(billAmount, tipPercent);
        String data = dbHandler.getArray(dbHandler.getTips(2));
        Log.d(TAG, "\nDatabase:" + data);
        //Show that tip data saved to database
        Context context = getApplicationContext();
        CharSequence text = "Tip saved!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        //reset screen
        billAmountEditText.setText("");
        tipPercent = dbHandler.getAverage();
        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));
        tipTextView.setText("$0.00");
        totalTextView.setText("$0.00");
        //Update date saved
        dateTextView.setText(dbHandler.getDate());

    }

}