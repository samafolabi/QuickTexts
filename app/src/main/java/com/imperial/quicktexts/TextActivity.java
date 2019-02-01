package com.imperial.quicktexts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class TextActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int CONTACT_LOADER_ID = 7;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_SMS = 2;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};
    private static String[] PERMISSIONS_SMS = {Manifest.permission.SEND_SMS};
    public boolean send = false;

    private SimpleCursorAdapter adapter;

    ArrayList<String> textSet = new ArrayList<>();
    ArrayList<String> contactSet = new ArrayList<>();

    FloatingActionButton fab, fab2, fab3;
    ListView listView;

    org.json.simple.JSONObject textsObj;
    JSONParser parser;
    Object obj;
    JSONArray textsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
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
        ;

        requestContactPermission();

    }


    public void requestContactPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(TextActivity.this, PERMISSIONS_CONTACT,
                            REQUEST_CONTACT);

        } else {
            setUpCursorAdapter();
            getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                    new Bundle(), contactsLoader);
            listViewSetup();
        }
    }

    public void requestSMSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(TextActivity.this, PERMISSIONS_SMS,
                            REQUEST_SMS);

        } else {
            send = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CONTACT) {
            if (verifyPermissions(grantResults)) {
                setUpCursorAdapter();
                getSupportLoaderManager().initLoader(CONTACT_LOADER_ID,
                        new Bundle(), contactsLoader);
                listViewSetup();
            } else {
                TextActivity.this.finish();
                System.exit(0);
            }
        } else if (requestCode == REQUEST_SMS) {
            if (verifyPermissions(grantResults)) {
                send = true;
                fab2.performClick();
            } else {
                TextActivity.this.finish();
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

        Class intendedActivity = TextActivity.class;

        if (id == R.id.nav_qt) {
            intendedActivity = TextActivity.class;
        } else if (id == R.id.nav_qct) {
            intendedActivity = ContactActivity.class;
        }

        startNextActivity(TextActivity.this, intendedActivity);

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
        dialogFragment.setPassedValue("Add");
        dialogFragment.setListener(new QuickTextsDialogFragment.Listener() {
            @Override
            public void addJSON(String jsonValue) {
                addUpdateJSON(jsonValue);
            }

            @Override
            public void delJSON(String text) {

            }

            @Override
            public void addQCJSON(String textValue, String contactName, String contactPhone) {

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
            sendSMS(textSet, contactSet);
        } else {
            Toast.makeText(this, "SMS Permission not granted!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void fab3(View view) {

        QuickTextsDialogFragment dialogFragment = new QuickTextsDialogFragment();
        dialogFragment.setPassedValue("Delete");
        dialogFragment.setStuff(getApplicationContext(), getContentResolver());
        dialogFragment.setListener(new QuickTextsDialogFragment.Listener() {
            @Override
            public void addJSON(String jsonValue) {

            }

            @Override
            public void delJSON(String text) {
                delUpdateJSON(text);
            }

            @Override
            public void addQCJSON(String textValue, String contactName, String contactPhone) {

            }

            @Override
            public void delQCJSON(String textValue, String contactName, String contactPhone) {

            }
        });
        FragmentManager fm = getSupportFragmentManager();
        dialogFragment.show(fm, "DIALOG");

    }


    private void updateTexts() {
        String sampleJSON = "{\n" +
                "  \"texts\": [\n" +
                "  ]\n" +
                "}";

        String json = readFromFile("quicktexts.json", sampleJSON);
        textsObj = initiateJSON(json, textsObj);
        updateJSONList(textsObj, "texts", R.layout.text_list_item, R.id.textlist);
    }

    public void addUpdateJSON(String jsonValue) {
        String sampleJSON = "{\n" +
                "  \"texts\": [\n" +
                "   ]\n" +
                "}";
        String file = "quicktexts.json";

        String json = readFromFile(file, sampleJSON);
        org.json.simple.JSONObject jsonObject = null;
        jsonObject = addJSON(file, json, jsonValue, "texts", jsonObject);
        updateJSONList(jsonObject, "texts", R.layout.text_list_item, R.id.textlist);
    }

    public void delUpdateJSON(String jsonValue) {
        String sampleJSON = "{\n" +
                "  \"texts\": [\n" +
                "   ]\n" +
                "}";
        String file = "quicktexts.json";

        String json = readFromFile(file, sampleJSON);
        org.json.simple.JSONObject jsonObject = null;
        jsonObject = deleteJSON(file, json, jsonValue, "texts", jsonObject);
        updateJSONList(jsonObject, "texts", R.layout.text_list_item, R.id.textlist);
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
                    String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                    s += readstring;
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

    public void sendSMS(ArrayList<String> texts, ArrayList<String> contacts) {
        int i, j;
        for (i = 0; i < contacts.size(); i++) {
            String currentNo = contacts.get(i);
            for (j = 0; j < texts.size(); j++) {
                String currentText = texts.get(j);
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(currentNo, null, currentText, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private LoaderManager.LoaderCallbacks<Cursor> contactsLoader =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    String[] projectionFields = new String[]{
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    };
                    return new CursorLoader(TextActivity.this,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projectionFields,
                            null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    adapter.swapCursor(data);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    adapter.swapCursor(null);
                }
            };

    private void setUpCursorAdapter() {
        String[] uiBindFrom = {
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        int[] uiBindTo = {
                R.id.display_name, R.id.phone_number
        };
        adapter = new SimpleCursorAdapter(this, R.layout.contacts_list_item,
                null, uiBindFrom, uiBindTo);
    }


    private void listViewSetup() {
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.list_item);

                int childCount = ((LinearLayout) view).getChildCount();

                String[] bArray = new String[childCount];

                for (int j = 0; j < childCount; j++) {
                    TextView children = (TextView) ((LinearLayout) view).getChildAt(j);
                    String b = children.getText().toString();
                    bArray[j] = b;
                }

                String contactPhone = bArray[1];
                if (contactSet.contains(contactPhone)) {
                    linearLayout.setBackgroundColor(Color.TRANSPARENT);
                    contactSet.remove(contactPhone);
                } else {
                    linearLayout.setBackgroundColor(Color.parseColor("#DDDDDD"));
                    contactSet.add(contactPhone);
                }

                if (contactSet.size() != 0 && textSet.size() != 0) {
                    fab2.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                    fab2.setVisibility(View.GONE);
                }
            }
        });
    }

    private void colorPressItem(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view.findViewById(R.id.text_list_item);
                String textValue = textView.getText().toString();
                if (textSet.contains(textValue)) {
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    textSet.remove(textValue);
                } else {
                    textView.setBackgroundColor(Color.parseColor("#DDDDDD"));
                    textSet.add(textValue);
                }

                fab = (FloatingActionButton) findViewById(R.id.fab);
                fab2 = (FloatingActionButton) findViewById(R.id.fab2);

                if (textSet.size() != 0 && contactSet.size() != 0) {
                    fab2.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                    fab2.setVisibility(View.GONE);
                }
            }
        });
    }


    private org.json.simple.JSONObject addJSON(String file,
                                               String json,
                                               String jsonValue,
                                               String arrayName,
                                               org.json.simple.JSONObject jsonObject) {
        JSONParser jsonParser = new JSONParser();
        JSONArray textArray;
        try {
            Object object;
            object = jsonParser.parse(json);
            jsonObject = (org.json.simple.JSONObject) object;
            textArray = (JSONArray) jsonObject.get(arrayName);
            textArray.add(jsonValue);
            writeToFile(file, jsonObject.toJSONString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private org.json.simple.JSONObject deleteJSON(String file,
                                                  String json,
                                                  String jsonValue,
                                                  String arrayName,
                                                  org.json.simple.JSONObject jsonObject) {
        JSONParser jsonParser = new JSONParser();
        JSONArray textArray;
        try {
            Object object;
            object = jsonParser.parse(json);
            jsonObject = (org.json.simple.JSONObject) object;
            textArray = (JSONArray) jsonObject.get(arrayName);
            textArray.remove(jsonValue);
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
            ArrayAdapter adapter = new ArrayAdapter(this, template, textsArray);
            ListView listView = (ListView) findViewById(listViewInt);
            listView.setAdapter(adapter);
            listView.setLongClickable(true);
            colorPressItem(listView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}