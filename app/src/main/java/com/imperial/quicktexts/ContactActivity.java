package com.imperial.quicktexts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.telephony.SmsManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ContactActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_SMS = 2;
    private static String[] PERMISSIONS_SMS = {Manifest.permission.SEND_SMS};
    public boolean send = false;
    HashMap<String, String> textContactSet = new HashMap<>();

    FloatingActionButton fab, fab2, fab3;

    org.json.simple.JSONObject textsObj;
    JSONParser parser;
    Object obj;
    JSONArray textsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateTexts();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    public void requestSMSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(ContactActivity.this, PERMISSIONS_SMS,
                            REQUEST_SMS);

        } else {
            send = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_SMS) {
            if (verifyPermissions(grantResults)) {
                send = true;
                fab2.performClick();
            } else {
                ContactActivity.this.finish();
                System.exit(0);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Class intendedActivity = ContactActivity.class;

        if (id == R.id.nav_qt) {
            intendedActivity = TextActivity.class;
        } else if (id == R.id.nav_qct) {
            intendedActivity = ContactActivity.class;
        }

        startNextActivity(ContactActivity.this, intendedActivity);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startNextActivity(Context currentActivity, Class intendedActivity) {
        Intent intent = new Intent(currentActivity, intendedActivity);
        currentActivity.startActivity(intent);
    }


    public void fab(View view) {
        QuickTextsDialogFragment dialogFragment = new QuickTextsDialogFragment();
        dialogFragment.setPassedValue("AddQC");
        dialogFragment.setStuff(getApplicationContext(), getContentResolver());
        dialogFragment.setListener(new QuickTextsDialogFragment.Listener() {
            @Override
            public void addJSON(String jsonValue) {

            }

            @Override
            public void delJSON(String text) {

            }

            @Override
            public void addQCJSON(String textValue, String contactName, String contactPhone) {
                addUpdateJSON(textValue, contactName, contactPhone);
            }

            @Override
            public void delQCJSON(String textValue, String contactName, String contactPhone) {

            }
        });
        FragmentManager fm = getSupportFragmentManager();
        dialogFragment.show(fm, "DIALOG");
    }

    public void fab2(View view) {
        requestSMSPermission();
        if (send = true) {
            sendSMS(textContactSet);
        } else {
            Toast.makeText(this, "SMS Permission not granted!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void fab3(View view) {
        QuickTextsDialogFragment dialogFragment = new QuickTextsDialogFragment();
        dialogFragment.setPassedValue("DeleteQC");
        dialogFragment.setStuff(getApplicationContext(), getContentResolver());
        dialogFragment.setListener(new QuickTextsDialogFragment.Listener() {
            @Override
            public void addJSON(String jsonValue) {

            }

            @Override
            public void delJSON(String text) {

            }

            @Override
            public void addQCJSON(String textValue, String contactName, String contactPhone) {

            }

            @Override
            public void delQCJSON(String textValue, String contactName, String contactPhone) {
                delUpdateJSON(textValue, contactName, contactPhone);
            }
        });
        FragmentManager fm = getSupportFragmentManager();
        dialogFragment.show(fm, "DIALOG");
    }


    private void updateTexts() {
        String sampleJSON = "{\n" +
                "        \"qct\": [\n" +
                "            \n" +
                "        ]\n" +
                "    }";

        String json = readFromFile("quickcontexts.json", sampleJSON);
        textsObj = initiateJSON(json, textsObj);
        updateJSONList(textsObj, "qct", R.layout.quickcontact_list_item, R.id.quickContactList);
    }

    public void addUpdateJSON(String textValue, String contactName, String contactPhone) {
        String sampleJSON = "{\"qct\":[]}";
        String file = "quickcontexts.json";

        String json = readFromFile(file, sampleJSON);
        JSONObject jsonObject = null;
        List<String> arrayList = new ArrayList<>();
        arrayList.add(textValue);
        arrayList.add(contactName);
        arrayList.add(contactPhone);
        jsonObject = addJSON(file, json, arrayList, "qct", jsonObject);
        updateJSONList(jsonObject, "qct", R.layout.quickcontact_list_item, R.id.quickContactList);
    }

    public void delUpdateJSON(String textValue, String contactName, String contactPhone) {
        String sampleJSON = "{\"qct\":[]}";
        String file = "quickcontexts.json";

        String json = readFromFile(file, sampleJSON);
        JSONObject jsonObject = null;
        List<String> arrayList = new ArrayList<>();
        arrayList.add(textValue);
        arrayList.add(contactName);
        arrayList.add(contactPhone);
        jsonObject = deleteJSON(file, json, arrayList, "qct", jsonObject);
        updateJSONList(jsonObject, "qct", R.layout.quickcontact_list_item, R.id.quickContactList);
    }


    private String readFromFile(String file, String data) {

        String ret = "";
        boolean test = true;

        while (test) {
            try {
                FileInputStream fileIn = openFileInput(file);
                InputStreamReader InputRead = new InputStreamReader(fileIn);

                char[] inputBuffer = new char[100];
                String s = "";
                int charRead;

                while ((charRead = InputRead.read(inputBuffer)) > 0) {
                    String readString = String.copyValueOf(inputBuffer, 0, charRead);
                    s += readString;
                }
                InputRead.close();
                ret = s;
                test = false;

            } catch (Exception e) {
                writeToFile(file, data);
                test = true;
                e.printStackTrace();
            }
        }

        return ret;
    }

    private void writeToFile(String file, String data) {
        try {
            FileOutputStream fileout = openFileOutput(file, MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(data);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSMS(HashMap<String, String> set) {
        int i;
        ArrayList<String> texts = new ArrayList<>();
        ArrayList<String> contacts = new ArrayList<>();
        for (String text : set.keySet()) {
            texts.add(text);
        }
        for (String contact : set.values()) {
            contacts.add(contact);
        }
        for (i = 0; i < contacts.size(); i++) {
            String currentNo = contacts.get(i);
            String currentText = texts.get(i);
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(currentNo, null, currentText, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private org.json.simple.JSONObject addJSON(String file,
                                               String json,
                                               List<String> jsonValue,
                                               String arrayName,
                                               org.json.simple.JSONObject jsonObject) {
        JSONParser jsonParser = new JSONParser();
        JSONArray textArray;
        try {
            Object object;
            object = jsonParser.parse(json);
            jsonObject = (org.json.simple.JSONObject) object;
            textArray = (JSONArray) jsonObject.get(arrayName);
            JSONArray jsonValueArr = new JSONArray();
            jsonValueArr.add(jsonValue.get(0));
            jsonValueArr.add(jsonValue.get(1));
            jsonValueArr.add(jsonValue.get(2));
            textArray.add(jsonValueArr);
            writeToFile(file, jsonObject.toJSONString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject deleteJSON(String file,
                                  String json,
                                  List<String> jsonValue,
                                  String arrayName,
                                  JSONObject jsonObject) {
        JSONParser jsonParser = new JSONParser();
        JSONArray textArray;
        try {
            Object object;
            object = jsonParser.parse(json);
            jsonObject = (JSONObject) object;
            textArray = (JSONArray) jsonObject.get(arrayName);
            for (int i = 0; i < textArray.size(); i++) {
                JSONArray currentArr = (JSONArray) textArray.get(i);
                String aa = (String) currentArr.get(0),
                        ab = (String) currentArr.get(1),
                        ac = (String) currentArr.get(2),
                        ad = jsonValue.get(0),
                        ae = jsonValue.get(1),
                        af = jsonValue.get(2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (Objects.equals(aa, ad) &&
                            Objects.equals(ab, ae) &&
                            Objects.equals(ac, af)) {
                        textArray.remove(i);
                    }
                } else {
                    if (aa == ad &&
                            ab == ae &&
                            ac == af) {
                        textArray.remove(i);
                    }
                }
            }
            writeToFile(file, jsonObject.toJSONString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private org.json.simple.JSONObject initiateJSON(String json, org.json.simple.JSONObject jsonObject) {
        parser = new JSONParser();
        try {
            obj = parser.parse(json);
            jsonObject = (org.json.simple.JSONObject) obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void updateJSONList(org.json.simple.JSONObject jsonObject, String jsonArrayRetriever,
                                int template, int listViewInt) {
        try {
            textsArray = (JSONArray) jsonObject.get(jsonArrayRetriever);
            QuickContact[] quickContactArray = new QuickContact[textsArray.size()];
            for (int i = 0; i < textsArray.size(); i++) {
                JSONArray iArray = (JSONArray) textsArray.get(i);
                QuickContact quickContact = new QuickContact((String) iArray.get(0),
                        (String) iArray.get(2));
                quickContactArray[i] = quickContact;
            }
            QuickContactAdapter adapter = new QuickContactAdapter(this, template, quickContactArray);
            ListView listView = (ListView) findViewById(listViewInt);
            listView.setAdapter(adapter);
            listView.setLongClickable(true);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    LinearLayout linearLayout =
                            (LinearLayout) view.findViewById(R.id.quickcontact_list_item);
                    int childCount = ((LinearLayout) view).getChildCount();
                    String[] bArray = new String[childCount];

                    for (int j = 0; j < childCount; j++) {
                        TextView children = (TextView) ((LinearLayout) view).getChildAt(j);
                        String b = children.getText().toString();
                        bArray[j] = b;
                    }

                    if (textContactSet.get(bArray[0]) == bArray[1]) {
                        linearLayout.setBackgroundColor(Color.TRANSPARENT);
                        textContactSet.remove(bArray[0]);
                    } else {
                        linearLayout.setBackgroundColor(Color.parseColor("#DDDDDD"));
                        textContactSet.put(bArray[0], bArray[1]);
                    }

                    fab = (FloatingActionButton) findViewById(R.id.fab);
                    fab2 = (FloatingActionButton) findViewById(R.id.fab2);

                    if (textContactSet.size() != 0) {
                        fab2.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.GONE);
                    } else {
                        fab.setVisibility(View.VISIBLE);
                        fab2.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
