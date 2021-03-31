package com.example.pawsitiveproject;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.gotev.uploadservice.data.UploadInfo;
import net.gotev.uploadservice.network.ServerResponse;
import net.gotev.uploadservice.observer.request.RequestObserver;
import net.gotev.uploadservice.observer.request.RequestObserverDelegate;
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadFragment extends Fragment {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    private final String API_KEY = "2fc6b7da-314a-4327-9888-0fd52d813f7f";
    private final String TAG = "api-req";

    private ImageView selectedImage;
    private Button btn_camera, btn_gallery, btn_upload;
    private ProgressBar upload_progress_bar;
    private TextView txt_upload_status;
    private String currentPath;
    private String chosenFilePath;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedImage = view.findViewById(R.id.upload_image);
        btn_gallery = view.findViewById(R.id.btn_gallery);
        btn_camera = view.findViewById(R.id.btn_take_picture);
        btn_upload = view.findViewById(R.id.btn_upload);
        upload_progress_bar = view.findViewById(R.id.upload_progress_bar);
        txt_upload_status = view.findViewById(R.id.upload_status);

        upload_progress_bar.setVisibility(View.INVISIBLE);

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testCameraPermission();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postNewPicture();
            }
        });
    }

    private void testCameraPermission() {
        if(ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.getActivity(),new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startCamera();
            }else {
                Toast.makeText(this.getContext(), "Camera permission is necessary to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPath = image.getAbsolutePath();
        return image;
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File pictureFile = null;
        try {
            pictureFile = createPictureFile();
        } catch (IOException ioe) {
            Log.d("bsr", "FILE IO ERROR: " + ioe);
        }

        if (pictureFile != null) {
            Uri pictureURI = FileProvider.getUriForFile(this.getContext(), "com.example.pawsitiveproject.fileprovider", pictureFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureURI);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File file = new File(currentPath);
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.getContext().sendBroadcast(mediaScanIntent);
                selectedImage.setImageURI(contentUri);
                btn_upload.setEnabled(true);
                txt_upload_status.setText("Picture selected. Press upload now");
                txt_upload_status.setError(null);
                upload_progress_bar.setProgress(0);
                upload_progress_bar.setVisibility(View.VISIBLE);
                MyFileUtils fileUtils = new MyFileUtils(this.getContext());
                chosenFilePath = fileUtils.getPath(contentUri);
            }

        }

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File galleryImageFile = new File(contentUri.getPath());
                selectedImage.setImageURI(contentUri);
                btn_upload.setEnabled(true);
                txt_upload_status.setText("Picture selected. Press upload now");
                txt_upload_status.setError(null);
                upload_progress_bar.setProgress(0);
                upload_progress_bar.setVisibility(View.VISIBLE);
                MyFileUtils fileUtils = new MyFileUtils(this.getContext());
                chosenFilePath = fileUtils.getPath(contentUri);
            }

        }
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = this.getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private void postNewPicture() {
        String url = "https://api.thedogapi.com/v1/images/upload?sub_id=" + currentUser.getUid();
        //String url = "https://ptsv2.com/t/pawsitivetesting";
        Log.d("bsr", "url: " + url);
        String response = null;
        try {
            MultipartUploadRequest imageUploadRequest = new MultipartUploadRequest(this.getContext(), url)
                    .setMethod("POST")
                    .addFileToUpload(chosenFilePath, "file")
                    .addHeader("x-api-key", API_KEY)
                    .setMaxRetries(3);
            imageUploadRequest.subscribe(this.getContext(), this.getViewLifecycleOwner(), new RequestObserverDelegate() {
                    @Override
                    public void onProgress(Context context, UploadInfo uploadInfo) {
                        int percent = uploadInfo.getProgressPercent();
                        Log.d("bsr", "Upload progress: " + percent);
                        upload_progress_bar.setProgress(percent);
                        txt_upload_status.setText("Upload progress ... " + percent + "%");
                    }

                    @Override
                    public void onSuccess(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                        Log.d("bsr", "Success");
                        Log.d("bsr", serverResponse.getBodyString());
                    }

                    @Override
                    public void onError(Context context, UploadInfo uploadInfo, Throwable throwable) {
                        Log.d("bsr", "UPLOAD ERROR: " + throwable.getMessage());
                        txt_upload_status.setText(throwable.getMessage() + " ");
                        txt_upload_status.setError("Possible reasons may include:\nThe picture does not show a dog clearly\nConnection is bad");
                    }

                    @Override
                    public void onCompleted(Context context, UploadInfo uploadInfo) {
                        Log.d("bsr", "Complete");
                    }

                    @Override
                    public void onCompletedWhileNotObserving() {

                    }
            });

            String reqID = imageUploadRequest.startUpload();
            Log.d("bsr", "reqID == " + reqID);
        } catch (FileNotFoundException fnfe) {
            Log.d("bsr", "FILE ERROR: " + fnfe);
        }
    }

}