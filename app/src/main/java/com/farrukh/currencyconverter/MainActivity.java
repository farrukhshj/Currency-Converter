package com.farrukh.currencyconverter;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public boolean isSpinnerTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner) findViewById(R.id.currency_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(isSpinnerTouched) {
            EditText inputAmt = (EditText) findViewById(R.id.inputAmtText);  //reading input data enter by user in USD
            String inputData = inputAmt.getText().toString();
            String currency = (String) parent.getItemAtPosition(pos);  //reading the output currency user chose

            //setting selected currency to match the keyword in JSON output
            if (currency.equalsIgnoreCase("POUND")) {
                currency = "GBP";
            } else if (currency.equalsIgnoreCase("INDIAN RUPEE")) {
                currency = "INR";
            } else if (currency.equalsIgnoreCase("EURO")) {
                currency = "EUR";
            }

            try {

                Boolean flag=new RetrieveFeedTask().isInternetAvailable(this);
                Log.e("Error:",flag.toString());
                if(flag==true)
                {
                    if(inputAmt.getText().toString().equals(""))
                        inputAmt.setText("1");
                    String result = new RetrieveFeedTask().execute(inputData, currency).get();
                    EditText output = (EditText) findViewById(R.id.outputAmtText);
                    output.setText(result);
                }
                else
                {
                    AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
                    alertBox.setTitle("Error");
                    alertBox.setMessage("Device is not connected to the internet!");
                    alertBox.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) { }
                    });
                    alertBox.create();
                    alertBox.show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else
        {
            isSpinnerTouched=true;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
    }



}
class RetrieveFeedTask extends AsyncTask<String, String, String> {


    private Exception exception;
    private Context applicationContext;

    protected void onPreExecute() { }

    protected String doInBackground(String... args) {
        //Calling the web Api to get rates!
        try {
            String output=null;
            URL url = new URL("http://api.fixer.io/latest?base=USD");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();

                if (stringBuilder.toString() != null)
                {
                    try
                    {
                        JSONObject jobj=new JSONObject(stringBuilder.toString());
                        JSONObject rates= (JSONObject) jobj.get("rates");
                        output=rates.getString(args[1]);

                        Double x=Double.parseDouble(output);
                        Double convertedValue=x*(Integer.parseInt(args[0]));

                        output=Double.toString(convertedValue);
                    }
                    catch (Exception e)
                    {
                        Log.e("Exception Caught:",e.toString());
                    }

                }
                else
                {
                      Log.e("Error:","The web API did not send any data");
                }
                return output;
            }
            finally
            {
                urlConnection.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }


    }
    public boolean isInternetAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected void onPostExecute(String response) { }

}