package repo.kyle.android.dictopedia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import us.feras.mdv.MarkdownView;

public class MainActivity extends AppCompatActivity {

    private Button mSubmitBt;
    private EditText mQueryET;
    private MarkdownView markdownView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    private MyDB dictDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, dictDB.selectRecords().toString(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        markdownView = (MarkdownView) findViewById(R.id.markdownView);
        dictDB = new MyDB(this);

        mSubmitBt = (Button) findViewById(R.id.submit_button);

        mQueryET = (EditText) findViewById(R.id.query_str);

        mSubmitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query_str = String.valueOf(mQueryET.getText());
                youdaoDict(query_str);
                dictDB.createRecords("1", query_str);
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void youdaoDict(String query) {
        String youdaoUrl = "http://fanyi.youdao.com/openapi.do?keyfrom=Dictopedia&key=1682019666&type=data&doctype=json&version=1.1&q=";

        String params[] = new String[2];
        params[0] = youdaoUrl + query;

        GetHttpResp task = new GetHttpResp();

        task.execute(params);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mClient, getIndexApiAction());
        mClient.disconnect();
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

// Converting InputStream to String

    public class GetHttpResp extends AsyncTask<String, Void, String> {
        String server_response;

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    server_response = readStream(urlConnection.getInputStream());

                    return server_response;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                String target_str;
                JSONObject jObject;
                jObject = new JSONObject(server_response);
                target_str = "**" + jObject.getString("translation") + "**\n";
                JSONObject trans_j;
                trans_j = jObject.getJSONObject("basic");
                if (trans_j == null) {
                    Toast.makeText(MainActivity.this, "Query failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                target_str += " _[" + trans_j.getString("us-phonetic") + "]_\n";

                target_str += "> - " + trans_j.getString("explains");
                target_str = target_str.replace("[\"", "");
                target_str = target_str.replace("\"]", "");
                target_str = target_str.replace("\",\"", ";  \n- ");
                markdownView.loadMarkdown(target_str);
            } catch (JSONException e) {
                e.printStackTrace();
                markdownView.loadMarkdown("");
                Toast.makeText(MainActivity.this, "Query failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class MyDB {

        public final static String EMP_TABLE = "MyEmployees"; // name of table
        public final static String EMP_ID = "_id"; // id value for employee
        public final static String EMP_NAME = "name";  // name of employee
        private MyDatabaseHelper dbHelper;
        private SQLiteDatabase database;

        /**
         * @param context
         */
        public MyDB(Context context) {
            dbHelper = new MyDatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }


        public long createRecords(String id, String name) {
            ContentValues values = new ContentValues();
            values.put(EMP_ID, id);
            values.put(EMP_NAME, name);
            return database.insert(EMP_TABLE, null, values);
        }

        public Cursor selectRecords() {
            String[] cols = new String[]{EMP_ID, EMP_NAME};
            Cursor mCursor = database.query(true, EMP_TABLE, cols, null
                    , null, null, null, null, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
            return mCursor; // iterate to get each value.
        }
    }
}

class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DBName";

    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table MyEmployees( _id integer primary key,name text not null);";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(MyDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS MyEmployees");
        onCreate(database);
    }
}
