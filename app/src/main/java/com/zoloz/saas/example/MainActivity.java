/**
 * MIT License
 *
 * Copyright (c) 2020 ZOLOZ-PTE-LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zoloz.saas.example;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ap.zoloz.hummer.api.IZLZCallback;
import com.ap.zoloz.hummer.api.ZLZConstants;
import com.ap.zoloz.hummer.api.ZLZFacade;
import com.ap.zoloz.hummer.api.ZLZRequest;
import com.ap.zoloz.hummer.api.ZLZResponse;
import com.zoloz.builder.BuildConfig;

import Objects.IdType;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String TAG = MainActivity.class.getSimpleName();


    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    String[] hosts = {
            "https://rn-dev-ekyc-api.nprod.platform-11.com/ekyc",
            "https://rn-sit-ekyc-api.nprod.platform-11.com/ekyc",
            "https://rn-uat-ekyc-api.nprod.platform-11.com/ekyc",
            "https://rn-staging-ekyc-api.nprod.platform-11.com/ekyc",
            "http://10.11.153.36:8080/ekyc"
    };

    String[] idNames = {
            "postal"
    };
    String[] idCodes = {
            "00630000016"
    };

    private Handler mHandler;

    String selectedHost = null;
    String selectedId = null;
    String userId = null;
    String status = "CONTACT_DETAILS";
    TextView textView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        textView = (TextView) findViewById(R.id.logs_textView);

        Spinner spinner = (Spinner) findViewById(R.id.host_spinner);
        Spinner idTypeSpinner = (Spinner) findViewById(R.id.id_type_spinner);

        spinner.setOnItemSelectedListener(this);
        idTypeSpinner.setOnItemSelectedListener(this);

        ArrayAdapter ad = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                hosts
        );
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);

        ArrayAdapter ad2 = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                idNames
        );
        ad2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        idTypeSpinner.setAdapter(ad2);

        userId =  UUID.randomUUID().toString();
    }

    private void runOnIoThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void doSomething(View view) {
        Toast.makeText(MainActivity.this, status,
                Toast.LENGTH_LONG).show();

        switch(status) {
            case "CONTACT_DETAILS":
                this.contactDetails();
                break;
            case "INITIALIZE_ZOLOZ":
                this.initializeZoloz(ZLZFacade.getMetaInfo(this));
                break;
            case "CHECK_RESULT":
                this.checkResult();
                break;
            case "TAG_INITIAL_ID":
                this.tagInitialId();
                break;
            case "SUBMIT_CIF":
                this.submitCif();
                break;
            default:
                break;
        }
    }


    public String contactDetails() {
        runOnIoThread(new Runnable() {
            @Override
            public void run() {
                IRequest request = new LocalRequest();
                String requestUrl = selectedHost + "/v6/customer/" + userId + "/contact-details";
                JSONObject jsonObject = (JSONObject) JSONObject.parse("{ \"device\": { \"id\": \"string\", \"model\": \"string\" }, \"email\": \"mariano.rlp01+003@gmail.com\", \"host\": { \"os\": \"string\", \"userName\": \"mariano.rlp01+003@gmail.com\", \"version\": \"string\" }, \"mobileNumber\": \"09770349543\", \"sessionStart\": \"2023-05-10T01:29:42.325Z\" }");

                final String result = request.contactDetailsRequest(requestUrl, jsonObject);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logSomething("contactDetails");
                        logSomething(result);
                        status = "INITIALIZE_ZOLOZ";
                    }
                });
            }
        });

        return "";
    }

    public String initializeZoloz(String metaInfo) {
        runOnIoThread(new Runnable() {
            @Override
            public void run() {
                IRequest request = new LocalRequest();
                String requestUrl = selectedHost + "/v1/zoloz/realId/transaction";
                JSONObject jsonObject1 = new JSONObject();
                JSONObject host = new JSONObject();
                host.put("os", "string");
                host.put("userName", "mariano.rlp01+003@gmail.com");
                host.put("version", "string");

                jsonObject1.put("docType", selectedId);
                jsonObject1.put("metaInfo", metaInfo);
                jsonObject1.put("host", host);

                final JSONObject result = request.initializeZoloz(requestUrl, jsonObject1, userId);
                System.out.println("111111111111111111" + result.toJSONString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logSomething("initializeZoloz");
                        logSomething(JSONObject.toJSONString(result));
                        logSomething(result.getString("transactionId"));
                        status = "INITIALIZE_ZOLOZ";

                        startZoloz(result);
                    }
                });
            }
        });




        this.logSomething("initializeZoloz");
        this.status = "CHECK_RESULT";
        return "";
    }

    public String checkResult() {
        this.logSomething("checkResult");
        this.status = "TAG_INITIAL_ID";
        return "";
    }

    public String tagInitialId() {
        this.logSomething("tagInitialId");
        this.status = "SUBMIT_CIF";
        return "";
    }

    public String submitCif() {
        this.logSomething("submitCif");
        return "";
    }

    public void logSomething(String string) {
        textView.setText(textView.getText() + "\n" + string);
    }



    public void startZoloz(JSONObject jsonObject) {
        runOnIoThread(new Runnable() {
            @Override
            public void run() {
//                String result = mockInitRequest(jsonObject);
//                if (TextUtils.isEmpty(result)) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "network exception, please try again later.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    return;
//                }
//                final InitResponse initResponse = JSON.parseObject(result, InitResponse.class);
                final ZLZFacade zlzFacade = ZLZFacade.getInstance();
                final ZLZRequest request = new ZLZRequest();
                System.out.println("222222222222222222222222" + jsonObject.getString("clientCfg"));
                request.zlzConfig = jsonObject.getString("clientCfg");
                request.bizConfig.put(ZLZConstants.CONTEXT, MainActivity.this);
                request.bizConfig.put(ZLZConstants.PUBLIC_KEY, jsonObject.getString("rsaPubKey"));
                request.bizConfig.put(ZLZConstants.LOCALE, "en");
                Log.d(TAG, "request success:");
                mHandler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "start zoloz");
                        zlzFacade.start(request, new IZLZCallback() {
                            @Override
                            public void onCompleted(ZLZResponse response) {
                                System.out.println("aaaaaaaaa");
//                                checkResult(initResponse.transactionId);
                            }

                            @Override
                            public void onInterrupted(ZLZResponse response) {
                                System.out.println("bbbbbbbbbbbbb");
//                                showResponse(initResponse.transactionId, JSON.toJSONString(response));
                                Toast.makeText(MainActivity.this, "interrupted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

//    private void checkResult(final String transactionId) {
//        runOnIoThread(new Runnable() {
//            @Override
//            public void run() {
//                IRequest request = new LocalRequest();
//                String requestUrl = EditTextUtils.getAndSave(MainActivity.this, R.id.init_host) + EditTextUtils.getAndSave(MainActivity.this, R.id.init_ref);
//                requestUrl = requestUrl.replaceAll("initialize", "checkresult");
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("transactionId", transactionId);
//                String requestData = jsonObject.toString();
//                final String result = request.request(requestUrl, requestData);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showResponse(transactionId, result);
//                    }
//                });
//            }
//        });
//    }

    private void showResponse(final String flowId, String response) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Check Result")
                .setMessage(response)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData myClip;
                        String text = flowId;
                        myClip = ClipData.newPlainText("text", text);
                        myClipboard.setPrimaryClip(myClip);
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.host_spinner) {
            this.selectedHost = hosts[i];
        } else if (adapterView.getId() == R.id.id_type_spinner) {
            this.selectedId = idCodes[i];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    protected String mockInitRequest(JSONObject data) {
//        IRequest request = new LocalRequest();
//        String requestUrl = "https://sg-sandbox-api.zoloz.com" + EditTextUtils.getAndSave(this, R.id.init_ref);
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("metaInfo", ZLZFacade.getMetaInfo(this));
//        jsonObject.put("serviceLevel", "REALID0001");
//        jsonObject.put("docType", selectedId);
//        jsonObject.put("v", BuildConfig.VERSION_NAME);
//        String requestData = jsonObject.toString();
//        String result = request.request(requestUrl, requestData);
//        return result;
//    }
}
