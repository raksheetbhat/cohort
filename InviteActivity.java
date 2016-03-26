/**
 * Created by Raksheet on 08-03-2016.
 */

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.internal.http.multipart.MultipartEntity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Raksheet on 02-03-2016.
 */
public class InviteActivity extends ListActivity {
    ListView inviteList;
    ArrayList<CharSequence> mail_ids;
    ProgressBar pb;

    String myJSON;

    private static final String TAG_RESULTS="result";
    private static final String TAG_NAME = "name";
    private static final String TAG_EMAIL_ID = "email_id";
    private static final String TAG_PHONE_NUMBER ="phone_number";

    JSONArray peoples = null;

    ArrayList<HashMap<String, String>> personList = new ArrayList<>();
    Button invite_extra;


    @Override
    public long getSelectedItemId() {
        // TODO Auto-generated method stub
        return super.getSelectedItemId();
    }

    @Override
    public int getSelectedItemPosition() {
        // TODO Auto-generated method stub
        return super.getSelectedItemPosition();
    }

    Cursor cursor1;
    Button submit_button;
    Button select_all,deselect_all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_layout);


        submit_button = (Button) findViewById(R.id.invite_submit);
        select_all = (Button) findViewById(R.id.select_all);
        deselect_all = (Button) findViewById(R.id.deselect_all);

        invite_extra = (Button) findViewById(R.id.invite_extra);

        select_all.setVisibility(View.VISIBLE);
        // create a cursor to query the Contacts on the device to start populating a listview
        cursor1 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        startManagingCursor(cursor1);





        int[] to = {android.R.id.text1, android.R.id.text2}; // sets the items from above string to listview

        

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.invite_list_item,getNameEmailDetails());

        

        setListAdapter(arrayAdapter);

        mail_ids = new ArrayList<CharSequence>();

        // adds listview so I can get data from it
        inviteList = getListView();
        inviteList.setItemsCanFocus(false);
        inviteList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        inviteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView ctv = (CheckedTextView) view;
                if (ctv.isChecked()) {
                    //Toast.makeText(InviteActivity.this, ((CheckedTextView) view).getText(), Toast.LENGTH_SHORT).show();
                    mail_ids.add(((CheckedTextView) view).getText());
                } else {
                    //Toast.makeText(InviteActivity.this, "now it is unchecked", Toast.LENGTH_SHORT).show();
                    mail_ids.remove(((CheckedTextView) view).getText());
                }

            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (CharSequence c : mail_ids) {
                    Log.i("invites", c.toString());
                    try {
                        MailOperation l = new MailOperation();
                        l.execute(c.toString());  //sends the email in background
                        Toast.makeText(InviteActivity.this, l.get(), Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e("SendMail", e.getMessage(), e);
                    }
                }

            }

        });

        select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mail_ids = new ArrayList<CharSequence>(getNameEmailDetails());
                for (int i = 0; i < inviteList.getCount(); i++) {
                    inviteList.setItemChecked(i, true);

                }
                Log.i("selected all", mail_ids.toString());
                select_all.setVisibility(View.GONE);
                deselect_all.setVisibility(View.VISIBLE);
            }
        });

        deselect_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mail_ids = new ArrayList<CharSequence>();
                for (int i = 0; i < inviteList.getCount(); i++) {
                    inviteList.setItemChecked(i, false);
                }
                Log.i("selected all", mail_ids.toString());
                deselect_all.setVisibility(View.GONE);
                select_all.setVisibility(View.VISIBLE);
            }
        });

        invite_extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getData();
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(InviteActivity.this);
                View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        InviteActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        //result.setText(userInput.getText());
                                        //relation = String.valueOf(userInput.getText());
                                        getData(String.valueOf(userInput.getText()));
                                        System.out.println(userInput.getText());
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        //pb.setVisibility(View.VISIBLE);
        //readContacts();
        //getNameEmailDetails();
        //new PostData().execute("name","email","1234567890");
        //uploadFile();
        //new UploadData().execute();

        Intent intent = new Intent(InviteActivity.this,HelloIntentService.class);
        startService(intent);
    }

    public class MailOperation extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            String recipient = params[0];
            String subject = "Download our app Cohort Plus";
            String body = "Cohort Plus helps to connect you to your alumni body. Professional Networking. Simplified." +
                    "It is an alumni driven networking platform";
            try{GMailSender sender = new GMailSender("stories.unblemished@gmail.com","1teehskartabh0");
                sender.sendMail(subject,
                        body,
                        "stories.unblemished@gmail.com",
                        recipient);
            }
            catch(Exception e)
            {
                Log.e("error",e.getMessage(),e);
                return "Email Not Sent";
            }
            return "Email Sent";
        }

        @Override
        protected void onPostExecute(String result)
        {
        }
        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
        }
    }

    public ArrayList<String> getNameEmailDetails() {
        ArrayList<String> emlRecs = new ArrayList<String>();
        HashSet<String> emlRecsHS = new HashSet<String>();

        Context context = InviteActivity.this;
        ContentResolver cr = context.getContentResolver();
        String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
        if (cur.moveToFirst()) {
            do {
                // names comes in hand sometimes
                String name = cur.getString(1);
                String emlAddr = cur.getString(3);

                //System.out.println(name+"  "+emlAddr);

                // keep unique only
                if (emlRecsHS.add(emlAddr.toLowerCase())) {
                    emlRecs.add(emlAddr);
                }
            } while (cur.moveToNext());
        }

        cur.close();
        return emlRecs;
    }

    protected void showList(){
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String email_id = c.getString(TAG_EMAIL_ID);
                String name = c.getString(TAG_NAME);
                String phone_number = c.getString(TAG_PHONE_NUMBER);

                HashMap<String,String> persons = new HashMap<String,String>();

                persons.put(TAG_NAME,name);
                persons.put(TAG_EMAIL_ID,email_id);
                persons.put(TAG_PHONE_NUMBER,phone_number);

                System.out.println(persons.get(TAG_NAME));
                personList.add(persons);
            }

            final ListAdapter adapter = new SimpleAdapter(
                    InviteActivity.this, personList, R.layout.list_item,
                    new String[]{TAG_NAME,TAG_EMAIL_ID,TAG_PHONE_NUMBER},
                    new int[]{R.id.name, R.id.email_id, R.id.phone_number}
            );

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(InviteActivity.this);
            //builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Invites:");



            builderSingle.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(
                    adapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strName = (String) adapter.getItem(which);
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                    InviteActivity.this);
                            builderInner.setMessage(strName);
                            builderInner.setTitle("Your Selected Item is");
                            builderInner.setPositiveButton(
                                    "Ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            builderInner.show();
                        }
                    });
            builderSingle.show();


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getData(final String relation){
        class GetDataJSON extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {
                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
                HttpPost httppost = new HttpPost("http://cohort.esy.es/get-related-data.php");

                // Depends on your web service
                httppost.setHeader("Content-type", "application/json");

                InputStream inputStream = null;
                String result = null;
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("relation", relation));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();

                } catch (Exception e) {
                    // Oops
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                //System.out.println(result);
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

}

