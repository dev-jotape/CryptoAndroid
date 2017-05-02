package com.example.pedro.rsaaes;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;




public class TelaInicial extends AppCompatActivity {


    EncryptAES aes = new EncryptAES();
    CryptoPubKey cryptoPubKey = new CryptoPubKey();

    private Button btnSendMsg,btnRequestKey,btnSendKey;
    private CheckBox cbSolicita,cbSend;
    private TextView txtValid;
    private EditText edtMsg;

    private String encrypted;
    private String cvPub;
    private String cvCifrada;
    private String ivCifrado;
    // variaveis para conexão com webservice
    private RequestQueue requestQueue;
    private StringRequest request;
    private JsonObjectRequest request2;
    private static final String urlSendMsg = "http://192.168.43.150/php/teste.php";
    private static final String urlPubKey = "http://192.168.43.150/php/sendPub.php";
    private static final String urlCvSim = "http://192.168.43.150/php/receberChave.php";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSendMsg = (Button) findViewById(R.id.btnSendMsg);
        btnRequestKey = (Button) findViewById(R.id.btnRequestKey);
        btnSendKey = (Button) findViewById(R.id.btnSendKey);
        cbSolicita = (CheckBox) findViewById(R.id.cbSolicita);
        cbSend = (CheckBox) findViewById(R.id.cbSend);
        txtValid = (TextView) findViewById(R.id.txtValid);
        edtMsg = (EditText) findViewById(R.id.edtMsg);

        requestQueue = Volley.newRequestQueue(TelaInicial.this);


        //Clique no botão Solicitar chave
        btnRequestKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request = new StringRequest(Request.Method.POST, urlPubKey, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            cvPub = jsonObject.getString("PubKey");
                            Log.d("teste", cvPub);

                            cbSolicita.setChecked(true);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Erro ao se conectar ao servidor\n".concat(error.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("op", "pub");
                        return hashMap;
                    }
                };
                requestQueue.add(request);


            }
        });

        //clique no botao envia chave simetrica
        btnSendKey.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                if (cbSolicita.isChecked()) {

                    try {
                        String key = aes.getSecretKey();
                        String iv = aes.getIv();
                        byte[] byteKey = cryptoPubKey.encrypt(key,cvPub);
                        Log.d("teste","1");

                        byte[] byteIv = cryptoPubKey.encrypt(iv,cvPub);

                        cvCifrada = aes.bytesToHex(byteKey);

                        ivCifrado = aes.bytesToHex(byteIv);



                        request = new StringRequest(Request.Method.POST, urlCvSim, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {

                                    JSONObject jsonObject = new JSONObject(response);
                                    Log.d("teste","deu certo");

                                    boolean cv = jsonObject.getBoolean("sucess");

                                    if (cv){
                                        Log.d("teste","deu certo");
                                        cbSend.setChecked(true);
                                        btnSendMsg.setEnabled(true);
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "nao recebeu chave e iv", Toast.LENGTH_LONG).show();
                                    }



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Erro ao se conectar ao servidor\n".concat(error.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> hashMap = new HashMap<String, String>();
                               // hashMap.put("op","0");
                                hashMap.put("cvCifrada", cvCifrada);
                               // hashMap.put("ivCifrado", ivCifrado);
                                return hashMap;
                            }
                        };
                        requestQueue.add(request);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }





                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TelaInicial.this);
                    builder.setMessage("É necessário solicitar a chave publica primeiro!")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();

                }

            }
        });


        //Clica no botao enviar mensagem
        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edtMsg.getText().toString();

                /* Encrypt */
                try {
                    encrypted = aes.bytesToHex(aes.encrypt(msg));
                    Log.d("teste", encrypted);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // enviar mensagem para webservice
                request = new StringRequest(Request.Method.POST, urlSendMsg, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            boolean insert = jsonObject.getBoolean("insert");

                            if (insert) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TelaInicial.this);
                                builder.setMessage("Mensagem Enviada com Sucesso!")
                                        .setPositiveButton("OK", null)
                                        .create()
                                        .show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Erro ao inserir mensagem\n", Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Erro ao se conectar ao servidor\n".concat(error.getMessage()), Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("msgEncrypted", encrypted);
                        return hashMap;
                    }
                };
                requestQueue.add(request);

            }
        });
    }






 }



