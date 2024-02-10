package si.uni_lj.fe.seminar.mojprojekt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DropdownFetcher {

    private Context context;
    private SharedPreferences preferences;

    public DropdownFetcher(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("Your_Preference_Name", Context.MODE_PRIVATE);
    }

    private class DropdownOption {
        private int id;
        private String name;

        public DropdownOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public void fetchVrstaDropdown(Spinner spinner) {
        new AsyncTask<Void, Void, List<DropdownOption>>() {
            @Override
            protected List<DropdownOption> doInBackground(Void... voids) {
                List<DropdownOption> options = new ArrayList<>();
                try {
                    String jwtToken = preferences.getString("jwt_token", "");
                    String urlBase = context.getString(R.string.URL_base_storitve);
                    URL url = new URL("http://" + urlBase +"/mojProjekt/zaledje/APIji/dropdownvrsta");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    connection.setRequestMethod("GET");
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("ID_vrsta");
                        String name = jsonObject.getString("vrsta");
                        options.add(new DropdownOption(id, name));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return options;
            }

            @Override
            protected void onPostExecute(List<DropdownOption> options) {
                List<String> names = new ArrayList<>();
                for (DropdownOption option : options) {
                    names.add(option.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }
        }.execute();
    }

    public void fetchNamenDropdown(Spinner spinner) {
        new AsyncTask<Void, Void, List<DropdownOption>>() {
            @Override
            protected List<DropdownOption> doInBackground(Void... voids) {
                List<DropdownOption> options = new ArrayList<>();
                try {
                    String jwtToken = preferences.getString("jwt_token", "");
                    String urlBase = context.getString(R.string.URL_base_storitve);
                    URL url = new URL("http://"+urlBase+"/mojProjekt/zaledje/APIji/dropdownnamen");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int id = jsonObject.getInt("ID_namen");
                        String name = jsonObject.getString("kategorija");
                        options.add(new DropdownOption(id, name));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return options;
            }

            @Override
            protected void onPostExecute(List<DropdownOption> options) {
                List<String> names = new ArrayList<>();
                for (DropdownOption option : options) {
                    names.add(option.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }
        }.execute();
    }
}
