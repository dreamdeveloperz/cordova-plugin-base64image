/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plugin.base64;

import android.os.Environment;
import android.util.Log;
import android.content.Context;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * This class echoes a string called from JavaScript.
 */
public class Base64ImagePlugin extends CordovaPlugin {

    public static final String TAG = "Base64Image";

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action The action to execute.
     * @param args JSONArry of arguments for the plugin.
     * @param callbackId The callback id used when calling back into JavaScript.
     * @return A PluginResult object with a status and message.
     */
    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        boolean result = false;
        Log.v(TAG, "execute: action=" + action);
//        Context context = getContext();
        if(action.equals("saveImage")){
             try {
            Log.v(TAG, data.getString(0));
            Log.v(TAG, data.getJSONObject(1).toString());
            String b64String = data.getString(0);
            if (b64String.startsWith("data:image")) {
                if (b64String.contains("data:image/jpeg")) {
                    b64String = b64String.substring(23);
                } else if (b64String.contains("data:image/png") || b64String.contains("data:image/gif")) {
                    b64String = b64String.substring(22);
                }

            } else {
                b64String = data.getString(0);
            }
            JSONObject params = data.getJSONObject(1);

            //Optional parameter
            String filename = (params.has("filename") ? params.getString("filename") : "Weave_" + System.currentTimeMillis()) + ".png";

            String storagetype = params.has("externalStorage") ? Environment.getExternalStorageDirectory() + "" : getApplicationContext().getFilesDir().getAbsolutePath();

            String folder = params.has("folder") ? storagetype + "/" + params.getString("folder") : storagetype + "/Pictures/Weave";
            
            Boolean overwrite = params.has("overwrite") ? params.getBoolean("overwrite") : false;

            result = this.saveImage(b64String, filename, folder, overwrite, callbackContext);

        } catch (JSONException e) {
             Log.v(TAG, "Exception from json in save image action");
            Log.v(TAG, e.getMessage());
            callbackContext.error("Exception :" + e.getMessage());
            result = false;
        }

        }else if(action.equals("convertImageToBase64FromUrl")){
            try{
                JSONArray imageUrls=data.getJSONArray(0);
                result=this.convertImageToBase64FromUrl(imageUrls,callbackContext);
            }catch (JSONException e) {
            Log.v(TAG, "Exception from json in convertImageToBase64FromUrl action");
             Log.v(TAG, e.getMessage());
            callbackContext.error("Exception :" + e.getMessage());
            result = false;
        }

        }
        else{

            callbackContext.error("Invalid action : " + action);
            result = false;
        }

       
        return result;
    }

    private boolean saveImage(String b64String, String fileName, String dirName, Boolean overwrite, CallbackContext callbackContext) {
        boolean result = false;
        try {

            //Directory and File
            File dir = new File(dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dirName, fileName);

            //Avoid overwriting a file
            if (!overwrite && file.exists()) {
                Log.v(TAG, "File already exists");
//                return new PluginResult(PluginResult.Status.OK, "File already exists!");
                callbackContext.error("File already exists!");
                return false;
            }

            //Decode Base64 back to Binary format
            byte[] decodedBytes = Base64.decodeBase64(b64String.getBytes());

            //Save Binary file to phone
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            fOut.write(decodedBytes);
            fOut.close();
            Log.v(TAG, "Saved successfully");
            callbackContext.success(file.getPath());
//            return new PluginResult(PluginResult.Status.OK, "Saved successfully!");
            result = true;

        } catch (FileNotFoundException e) {
            Log.v(TAG, "File not Found");
//            return new PluginResult(PluginResult.Status.ERROR, "File not Found!");
            callbackContext.error("File not Found!");
            result = false;
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
//            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            callbackContext.error("Exception :" + e.getMessage());
            result = false;
        }
        return result;
    }
     private boolean convertImageToBase64FromUrl(JSONArray imageUrls, CallbackContext callbackContext) {
        boolean result = false;
        JSONArray base64StringArray=new JSONArray();
        //String[] base64StringArray=new String[imageUrls.length()];
        try {
Log.v(TAG, "array count ---"+imageUrls.length());
            for(int i=0;i<imageUrls.length();i++){
                Log.v(TAG, "start 1");
                Log.v(TAG,imageUrls.toString());
               // JSONObject imageObject = imageUrls.getJSONObject(i);
                Log.v(TAG, "start 2");
                String fileUrl=imageUrls.getString(i).substring(7);
 Log.v(TAG, "file url ---"+fileUrl);
                File file=new File(fileUrl);
                  
                byte[] bFile = new byte[(int) file.length()];

                FileInputStream inputstream=new FileInputStream(file);
            
                inputstream.read(bFile);
                inputstream.close();
                //Log.v(TAG,Arrays.toString(bFile));
                String base64String=new String(Base64.encodeBase64(bFile));
                
                //String base64String=Base64.encodeBase64String(bFile);
                /*String filename=file.getName();
                int extensionIndex = filename.lastIndexOf(".");
                String extension = filename.substring(extensionIndex + 1);
                if("jpeg".equals(extension)){
                    base64String="data:image/jpeg;base64,"+base64String;
                }else if("png".equals(extension)){
                    base64String="data:image/png;base64,"+base64String;
                }else if("gif".equals(extension)){
                    base64String="data:image/gif;base64,"+base64String;
                }
               /* switch (extension) {
                case "jpeg":base64String="data:image/jpeg;base64,"+base64String;
                break;
                case "png":base64String="data:image/png;base64,"+base64String;
                break;
                case "gif":base64String="data:image/gif;base64,"+base64String;
                break;
                }*/
                base64String="data:image/png;base64,"+base64String;
                Log.v(TAG,base64String);
                JSONObject object=new JSONObject();
                object.put("url", base64String);
                base64StringArray.put(object);

            }
          callbackContext.success(base64StringArray);

        } catch (FileNotFoundException e) {
            Log.v(TAG, "File not Found");
//            return new PluginResult(PluginResult.Status.ERROR, "File not Found!");
            callbackContext.error("File not Found!");
            result = false;
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
//            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            callbackContext.error("Exception :" + e.getMessage());
            result = false;
        }catch (JSONException e) {
             Log.v(TAG, "my json exception");
            Log.v(TAG, e.getMessage());
            callbackContext.error("Exception :" + e.getMessage());
            result = false;
        }
        return result;
    }


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

    }
}
