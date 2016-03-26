package com.example.raksheet.cohortapplication;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Raksheet on 13-03-2016.
 */
public class HelloIntentService extends IntentService {

    LinkedList<Contacts> mPendingContacts = new LinkedList<>();
    PostData postData = null;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public HelloIntentService() {
        super("HelloIntentService");
    }


    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.

        System.out.println("service intent");
        readContacts();


    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //System.out.println("service started");
        Toast.makeText(HelloIntentService.this,"service started",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(HelloIntentService.this,"service ended",Toast.LENGTH_SHORT).show();
    }

    private class PostData extends AsyncTask<Contacts, Integer, Double> {

        @Override
        protected Double doInBackground(Contacts... params) {
            // TODO Auto-generated method stub
            //postData(params[0], params[1], params[2]);
            postData(params[0].getName(),params[0].getEmail_id(),params[0].getPhone_number());
            //postData(params[0],params[1],params[2]);
            return null;
        }

        protected void onPostExecute(Double result){
            super.onPostExecute(result);
            //pb.setVisibility(View.GONE);
            //Toast.makeText(getApplicationContext(), "command sent", Toast.LENGTH_LONG).show();
            System.out.println("posted");
            if(!mPendingContacts.isEmpty()){
                postData = new PostData();
                postData.execute(mPendingContacts.poll());
            }
        }
        protected void onProgressUpdate(Integer... progress){
            //pb.setProgress(progress[0]);
        }

        public void postData(String name,String email,String phone_number) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://cohort.esy.es/insert-db.php");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", name));
                nameValuePairs.add(new BasicNameValuePair("email_id", email));
                nameValuePairs.add(new BasicNameValuePair("phone_number", phone_number));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                System.out.println(response);

                //HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getStatusCode() == 200)
                {
                    HttpEntity entity = response.getEntity();
                    System.out.println("entity: "+EntityUtils.toString(entity));
                }


            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void readContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        String username,email_id ,phone = "No number saved";
        //PostData postData = new PostData();

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                email_id = "No email id saved";
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                username = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //System.out.println("name : " + username + ", ID : " + id);

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //System.out.println("phone" + phone);
                    }
                    pCur.close();


                    // get email and type

                    Cursor emailCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (emailCur.moveToNext()) {
                        // This would allow you get several email addresses
                        // if the email addresses were stored in an array
                        email_id = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        String emailType = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                        //System.out.println("Email " + email_id + " Email Type : " + emailType);
                    }
                    emailCur.close();
                    //System.out.println(username + " " + email_id + "  " + phone);
                    //System.out.println(new PostData().getStatus());
                    //new PostData().execute(username,email_id,phone);
                    //postData.execute(username,email_id,phone);

                    //mPendingContacts.add(username);
//                    if(!mPendingContacts.contains(username)){
//                        mPendingContacts.offer(username);
//                        if(postData == null || postData.getStatus() == AsyncTask.Status.FINISHED){
//                            postData = new PostData();
//                            postData.execute(username,"","");
//                        }
//                    }else{
//                        Toast.makeText(this,"service finished",Toast.LENGTH_SHORT).show();
//                    }
                    Contacts contact = new Contacts();
                    contact.setName(username);
                    contact.setEmail_id(email_id);
                    contact.setPhone_number(phone);

                    if(!mPendingContacts.contains(contact)){
                        mPendingContacts.offer(contact);
                        if(postData == null || postData.getStatus() == AsyncTask.Status.FINISHED){
                            postData = new PostData();
                            postData.execute(contact);
                        }
                    }else{
                        Toast.makeText(this,"service finished",Toast.LENGTH_SHORT).show();
                    }


                }


            }
        }
        cur.close();
    }
}
