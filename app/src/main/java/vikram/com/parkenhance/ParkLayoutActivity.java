package vikram.com.parkenhance;

import android.graphics.Color;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParkLayoutActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // volley stuff
    private RequestQueue queue;
    public static final String TAG = "MyTag";

    // layout
    private GridLayout grid;
    private int floor = 0;
    private int[][][] map3d;
    private Spinner spinner;
    private String android_id;
    private TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_layout_main);
        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        grid = (GridLayout) findViewById(R.id.grid);
        spinner = (Spinner) findViewById(R.id.floor);
        name = (TextView) findViewById(R.id.plot_name);
    }

    @Override
    protected void onResume(){
        super.onResume();
        name.setText(Common.parkingLot.getName());
        getParkingData();
    }
    private void resume(){
        String[] spinnerArray = new String[map3d.length];
        for (int i = 0; i<spinnerArray.length; i++){
            spinnerArray[i] = "level " + i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        genGrid();
    }

    private void genGrid() {
        grid.removeAllViews();
        grid.invalidate();
        int[][] arr = map3d[floor];
        grid.setRowCount(arr.length);
        grid.setColumnCount(arr[0].length);
        for (int i = 0; i<arr.length; i++){
            for (int j = 0; j<arr[i].length; j++){
                TextView gridView = new TextView(this);
                gridView.setGravity(Gravity.CENTER);
                gridView.setTextSize(20);
                gridView.setMinHeight(100);
                gridView.setMinWidth(100);
                gridView.setPadding(10, 10, 10, 10);
                switch (arr[i][j]){
                    case 0: //free green
                        gridView.setBackgroundColor(Color.GREEN);
                        gridView.setText("E");
                        gridView.setClickable(true);
                        gridView.setOnClickListener(new ParkingSpaceClickListener(queue,
                                new int[] {floor, i, j}, this, android_id));
                        break;
                    case 1: //  reserved (android this is yellow)
                        gridView.setBackgroundColor(Color.YELLOW);
                        gridView.setText("R");
                        break;
                    case 2: // full
                        gridView.setBackgroundColor(Color.RED);
                        gridView.setText("F");
                        break;
                    case 10: // right
                        gridView.setText("→");
                        //gridView.setBackgroundResource(R.drawable.right);
                        break;
                    case 11: // left
                        gridView.setText("←");
                        //gridView.setBackgroundResource(R.drawable.left);
                        break;
                    case 12: // left + right
                        gridView.setText("↔");
                        break;
                    case 13: // up
                        gridView.setText("↑");
                        //gridView.setBackgroundResource(R.drawable.up);
                        break;
                    case 14: // down
                        gridView.setText("↓");
                        //gridView.setBackgroundResource(R.drawable.down);
                        break;
                    case 15:
                        gridView.setText("↕");
                        break;
                }

                //gridView.setText(""+arr[i][j]);
                grid.addView(gridView);
            }
        }

        grid.postInvalidate();
    }

    public void getParkingData() {
        queue = Volley.newRequestQueue(this);
        String url = Common.BASEURL + Common.PARKING_LOT_INFO + "?name=" + Common.parkingLot.getName();

        JsonObjectRequest lotRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject parkingLot) {
                        try {
                            JSONArray map = parkingLot.getJSONArray("map");
                            map3d = new int[map.length()][][];
                            for (int i=0;i<map.length();i++){
                                JSONArray floor = map.getJSONArray(i);
                                map3d[i] = new int[floor.length()][];
                                for (int j = 0; j<floor.length(); j++){
                                    JSONArray col = floor.getJSONArray(j);
                                    map3d[i][j] = new int[col.length()];
                                    for (int k = 0; k<col.length(); k++){
                                        map3d[i][j][k] = col.getInt(k);
                                    }
                                }
                            }
                            resume();
                            Toast.makeText(ParkLayoutActivity.this, "success", Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ParkLayoutActivity.this, "json error", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ParkLayoutActivity.this, "Volley Request Error" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(lotRequest);
    }
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        floor = position;
        genGrid();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        floor = 0;
    }

    @Override
    public void onPause(){
        super.onPause();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}
