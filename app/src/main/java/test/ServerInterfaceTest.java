package test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.MainActivity;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.google.gson.Gson;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by aleksey.boev on 2015-03-25.
 */
public class ServerInterfaceTest  extends ActivityInstrumentationTestCase2<MainActivity> {
    private String strOriginalUserID = "";
    private String strMockUserID = "";
    private String strMockUserPublicID = "47";
    private String strMockUserProfile = "{\"avatar\":\"http://dev.snufan.com/" +
            "uploads/katiefritz.jpg\",\"email\":\"test@test.com\",\"id\":\"\"," +
            "\"name\":\"Katie Fritz\",\"phone\":\"116\",\"pushid\":\"\"}";
    private String strImageID = "fc2751ed-626c-4d32-bdab-bef5333498cc";
    private String strSampleComment = "Intentional whatsoever including that";
    private long longCommentId = 0;
    private Boolean boolRequestFinished = false;
    private Boolean boolImageDownloaded = false;

    public final static String strProxyHost = "";
    public final static Integer intProxyPort = 3128;
    private int intDefaultRequestTimeout = 5000;
    Gson gson = new Gson();

    public final static Boolean boolUnitTest = true;
    public final static Boolean boolUseProxy = true;

    public ServerInterfaceTest(){
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(1000);
        strOriginalUserID = Photobook.getPreferences().strUserID;
        Photobook.getPreferences().strUserID = strMockUserID;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Photobook.getPreferences().strUserID = strOriginalUserID;
    }

    public void setRequestFinishedFlag(){
        boolRequestFinished = true;
    }

    public void setImageDownloadFlag(){
        boolImageDownloaded = true;
    }

    public void setCommentId(String strId){
        longCommentId = Long.parseLong(strId);
    }

    public void testGetUserProfileRequest() throws Exception {
        boolRequestFinished = false;
        ServerInterface.getUserProfileRequest(getActivity(),
            new String[] {strMockUserPublicID},
            new Response.Listener<HashMap<String, UserProfile>>() {
                @Override
                public void onResponse(HashMap<String, UserProfile> response) {
                    assertNotNull(response);
                    assertEquals(response.size(), 1);
                    assertEquals(response.containsKey(strMockUserPublicID), true);
                    UserProfile profile = gson.fromJson(strMockUserProfile,
                            UserProfile.class);
                    UserProfile responseProfile = response.get(strMockUserPublicID);
                    compare(profile.name, responseProfile.name);
                    compare(profile.avatar, responseProfile.avatar);
                    compare(profile.email, responseProfile.email);
                    setRequestFinishedFlag();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fail("Network error " + error.getMessage());
                }
            });
        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
    }

    public void testGetImageDetailsRequest() throws Exception {
        boolRequestFinished = false;
        ServerInterface.getImageDetailsRequestJson(getActivity(), null, null,
            new Response.Listener<HashMap<String, ImageJson>>() {
                @Override
                public void onResponse(HashMap<String, ImageJson> response) {
                    assertNotNull(response);
                    assertNotSame(0, response.size());
                    Iterator it = response.entrySet().iterator();
                    Map.Entry pair = (Map.Entry)it.next();
                    ImageJson image = (ImageJson) pair.getValue();
                    assertNotNull(image.title);
                    assertNotSame("", image.title);
                    new DownloadImageTask().execute(image);
                    setRequestFinishedFlag();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fail("Network error " + error.getMessage());
                }
            });
        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
        assertTrue(boolImageDownloaded);
    }

    public void testLikeRequest() throws Exception {
        boolRequestFinished = false;

        ServerInterface.likeRequest(getActivity(), strImageID, strMockUserID,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ServerInterface.getImageDetailsRequestJson(getActivity(), null, null,
                        new Response.Listener<HashMap<String, ImageJson>>() {
                            @Override
                            public void onResponse(HashMap<String, ImageJson> response) {
                                assertNotNull(response);
                                assertTrue(response.containsKey(strImageID));
                                Boolean boolContainsSelfID = false;
                                for (String id : response.get(strImageID).likes)
                                    if (id.equals(strMockUserPublicID)) {
                                        boolContainsSelfID = true;
                                        break;
                                    }
                                assertTrue(boolContainsSelfID);
                                setRequestFinishedFlag();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                fail("Network error " + error.getMessage());
                            }
                        });
                }}, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        fail("Network error " + error.getMessage());
                    }
                });
        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
        boolRequestFinished = false;

        ServerInterface.unLikeRequest(getActivity(), strImageID, strMockUserID,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ServerInterface.getImageDetailsRequestJson(getActivity(), null, null,
                        new Response.Listener<HashMap<String, ImageJson>>() {
                            @Override
                            public void onResponse(HashMap<String, ImageJson> response) {
                                assertNotNull(response);
                                assertTrue(response.containsKey(strImageID));
                                Boolean boolContainsSelfID = false;
                                for (String id : response.get(strImageID).likes)
                                    if (id.equals(strMockUserPublicID)) {
                                        boolContainsSelfID = true;
                                        break;
                                    }
                                assertFalse(boolContainsSelfID);
                                setRequestFinishedFlag();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                fail("Network error " + error.getMessage());
                            }
                        });
                }}, null);
        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
    }

    public void testCommentRequest() throws Exception {
        boolRequestFinished = false;
        // Post comment
        ServerInterface.postCommentRequestJson(getActivity(), strImageID, strMockUserID,
            strSampleComment, 0,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    setCommentId(response);
                    setRequestFinishedFlag();
                }},
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fail("Network error " + error.getMessage());
                }
            });
        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
        boolRequestFinished = false;

        // Get comments - should return new comment
        ServerInterface.getCommentsJson(strImageID, false,
            new Response.Listener<ArrayList<CommentEntryJson>>() {
                @Override
                public void onResponse(ArrayList<CommentEntryJson> response) {
                    CommentEntryJson comment = null;
                    for (int i = 0; i < response.size(); i++)
                        if (response.get(i).id == longCommentId) {
                            comment = response.get(i);
                            break;
                        }
                    assertNotNull(comment);
                    compare(strSampleComment, comment.text);
                    compare(strMockUserPublicID, comment.author_id );
                    setRequestFinishedFlag();
                }},
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fail("Network error " + error.getMessage());
                }
            });

        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
        boolRequestFinished = false;

        // Delete comment
        ServerInterface.deleteCommentRequest (getActivity(),
            String.valueOf(longCommentId),
            strMockUserID,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    setRequestFinishedFlag();
                }},
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fail("Network error " + error.getMessage());
                }
            }
        );

        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
        boolRequestFinished = false;

        // Get comments should not return comment
        ServerInterface.getCommentsJson(strImageID, false,
            new Response.Listener<ArrayList<CommentEntryJson>>() {
                @Override
                public void onResponse(ArrayList<CommentEntryJson> response) {
                    CommentEntryJson comment = null;
                    for (int i = 0; i < response.size(); i++)
                        if (response.get(i).id == longCommentId) {
                            comment = response.get(i);
                            break;
                        }
                    assertNull(comment);
                    setRequestFinishedFlag();
                }},
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    fail("Network error " + error.getMessage());
                }
            });
        Thread.sleep(intDefaultRequestTimeout);
        assertTrue(boolRequestFinished);
    }

    public void compare(String a, String b) {
        System.out.println("Comparing " + a + " and " + b);
        assertEquals(a, b);
    }

    class DownloadImageTask extends AsyncTask<ImageJson, Void, Boolean> {

        private Bitmap bitmap;
        private ImageJson image;

        protected Boolean doInBackground(ImageJson... images) {
            try {
                image = images[0];
                System.out.println("Loading image " + image.url_small);
                URL url = new URL(image.url_small);
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(strProxyHost, intProxyPort));
                HttpURLConnection connection;
                if (boolUseProxy)
                    connection = (HttpURLConnection) url.openConnection(proxy);
                else
                    connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean b) {
            assertNotNull(bitmap);
            assertNotSame(0, bitmap.getByteCount());
            double ratio = bitmap.getHeight() * 1.0 / bitmap.getWidth();
            assertEquals(ratio, image.ratio);
            setImageDownloadFlag();
        }
    }

}
