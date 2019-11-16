package com.thesis.scannerprototype;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    TextView tvStatus, tvFirstname, tvDateSub, tvDateValid;
    RequestQueue queue;
    String value;
    CircleImageView ivProfile;
    String photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus=findViewById(R.id.tvStatus);
        tvFirstname = findViewById(R.id.tvFirstname);
        tvDateSub = findViewById(R.id.tvDateSub);
        tvDateValid = findViewById(R.id.tvDateValid);
        ivProfile = findViewById(R.id.ivProfile);



        queue = Volley.newRequestQueue(this);


        onLoad();

    }

    private void onLoad() {

        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");

            } else {
                Log.e("Scan", "Scanned");

                value = result.getContents();
                scanData();
                //Toast.makeText(this, ""+value , Toast.LENGTH_SHORT).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        tvStatus.setText(null);
        tvFirstname.setText(null);
        tvDateValid.setText(null);
        tvDateSub.setText(null);
        onLoad();
    }

    private void scanData() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading ...");
        pDialog.setCancelable(false);
        pDialog.show();

       String URL ="http://bus-ticketing.herokuapp.com/scanner_prototype.php?qr_code="+value;
       // String URL ="http://192.168.43.32/scanner_prototype.php?qr_code="+value;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response67", "aw"+response);
                        try {

                            String status = response.getString("status");
                            JSONArray result = response.getJSONArray("qrresult");
                            Log.d("Response67", status);

                            for(int x=0;x<result.length();x++) {
                                JSONObject collegeData = result.getJSONObject(x);
                                String firstname = collegeData.getString("firstname");
                                String lastname = collegeData.getString("lastname");
                                String date_subscribed = collegeData.getString("date_subscribed");
                                String validity = collegeData.getString("validity");
                                photo = collegeData.getString("photo");
                                Log.d("Response", photo);

                                Picasso.get().load(photo).into(ivProfile);
                                tvFirstname.setText("Name: "+firstname+" "+lastname);
                                tvDateSub.setText("Date Subscribed: "+date_subscribed);
                                tvDateValid.setText("Valid Until: "+validity);


                            }

                            if(status.equals("Active")){
                                pDialog.dismissWithAnimation();
                                tvStatus.setText("ACTIVE");
                                tvStatus.setTextColor(Color.GREEN);

                            }
                            if(status.equals("Expired")){
                                pDialog.dismissWithAnimation();
                                tvStatus.setText("EXPIRED");
                                tvStatus.setTextColor(Color.RED);

                            }
                            if(status.equals("NotExist")){
                                pDialog.dismissWithAnimation();
                                tvStatus.setText("QR CODE IS INVALID");
                                tvStatus.setTextColor(Color.RED);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismissWithAnimation();
                        Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return super.getHeaders();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return super.getBody();
            }
        };

        // add it to the RequestQueue
        queue.add(getRequest);


    }
}
