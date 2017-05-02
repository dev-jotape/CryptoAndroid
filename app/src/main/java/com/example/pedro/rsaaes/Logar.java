package com.example.pedro.rsaaes;


import android.app.DownloadManager;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;


public class Logar extends AppCompatActivity {

    private EditText edtUsername,edtPassword;
    private Button btnLogin;

    //public static final String URL="http://192.168.0.104/php/Logar.php"; //House
    private static final String URL = "http://192.168.43.150/php/Logar.php"; //3g

    private RequestQueue requestQueue;
    private StringRequest request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logar);

        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPassword= (EditText) findViewById(R.id.edtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);




        requestQueue = Volley.newRequestQueue(this);
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            //boolean success = jsonObject.getBoolean("success");


                            if(jsonObject.names().get(0).equals("success")){
                                startActivity(new Intent(getApplicationContext(),TelaInicial.class));

                            }
                            else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(Logar.this);
                                builder.setMessage("Invalid Username/Password")
                                        .setPositiveButton("Retry",null)
                                        .create()
                                        .show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Erro ao se conectar ao servidor\n". concat(error.getMessage()),Toast.LENGTH_LONG).show();
                    }
                }){

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> hashMap = new HashMap<String, String>();

                        hashMap.put("username",edtUsername.getText().toString());
                        hashMap.put("password",edtPassword.getText().toString());

                        return hashMap;
                    }
                };

                requestQueue.add(request);



            }
        });




    }
}
