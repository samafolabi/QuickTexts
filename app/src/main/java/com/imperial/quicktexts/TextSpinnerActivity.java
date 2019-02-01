package com.imperial.quicktexts;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

class TextSpinnerActivity {

    private Context context;

    private JSONObject textsObj;

    TextSpinnerActivity(final Context context, Spinner spinner) {
        this.context = context;

        String sampleJSON = "{\n" +
                "  \"texts\": [\n" +
                "    \"Hello world!\",\n" +
                "    \"Hi yall\"\n" +
                "  ]\n" +
                "}";

        String json = readFromFile("quicktexts.json", sampleJSON);
        textsObj = initiateJSON(json, textsObj);
        updateJSONList(textsObj, "texts", android.R.layout.simple_spinner_item, spinner);
    }

    private String readFromFile(String file, String data) {

        String ret = "";
        boolean test = true;

        while (test) {
            try {
                FileInputStream fileIn = context.openFileInput(file);
                InputStreamReader InputRead= new InputStreamReader(fileIn);

                char[] inputBuffer= new char[100];
                String s="";
                int charRead;

                while ((charRead=InputRead.read(inputBuffer))>0) {
                    String readstring=String.copyValueOf(inputBuffer,0,charRead);
                    s +=readstring;
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
            FileOutputStream fileout = context.openFileOutput(file, Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(data);
            outputWriter.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private org.json.simple.JSONObject initiateJSON(String json, org.json.simple.JSONObject jsonObject) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(json);
            jsonObject = (org.json.simple.JSONObject) obj;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void updateJSONList(org.json.simple.JSONObject jsonObject, String jsonArrayRetriever,
                                int template, Spinner spinner) {
        try {
            JSONArray textsArray = (JSONArray) jsonObject.get(jsonArrayRetriever);
            ArrayAdapter adapter = new ArrayAdapter<>(context, template, textsArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
