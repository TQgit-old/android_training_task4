package com.example.android.trainingtask4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.android.trainingtask4.ChatMessage.MEDIA_PHOTO;
import static com.example.android.trainingtask4.ChatMessage.MEDIA_VIDEO;

public class ConversationActivity extends AppCompatActivity {

    ArrayList<ChatMessage> messages;
    ChatMessageAdapter adapter;
    EditText typedMessage;
    ImageView cam;
    ImageButton sendButton;
    ChatMessage mediaMessage;
    Toast toastOnScreen;
    MediaPlayer mp;
    MediaPlayer.OnCompletionListener completionListener;
    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    SharedPreferences preferences;
    SharedPreferences.Editor prefsEditor;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        getWindow().setBackgroundDrawableResource(R.drawable.default_wallpaper);
        setSupportActionBar((Toolbar) findViewById(R.id.chat_toolbar));

        preferences = getPreferences(MODE_PRIVATE);
        prefsEditor = preferences.edit();
        if (preferences.contains("messages")) {
            Gson gson = new Gson();
            String json = preferences.getString("messages", "");
            messages = gson.fromJson(json, new TypeToken<ArrayList<ChatMessage>>() {
            }.getType());
        } else {
            messages = new ArrayList<ChatMessage>();
        }

        cam = findViewById(R.id.cam);
        registerForContextMenu(cam);
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cam.showContextMenu();
            }
        });
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = typedMessage.getText().toString();
                if (message.isEmpty()) {
                    toastOnScreen.cancel();
                    toastOnScreen = Toast.makeText(ConversationActivity.this, R.string.empty_message_toast, Toast.LENGTH_SHORT);
                    toastOnScreen.show();
                    return;
                }
                Calendar calendar = Calendar.getInstance();

                sendMessage(new ChatMessage(message, calendar, true));
            }
        });
        typedMessage = findViewById(R.id.typed_message);
        toastOnScreen = new Toast(this);
        mp = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
            }
        };

        typedMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (typedMessage.getText().length() == 0) {
                    cam.setVisibility(View.VISIBLE);
                } else {
                    cam.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        typedMessage.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    sendButton.performClick();
                    return true;
                }
                return false;
            }
        });

        ListView listView = findViewById(R.id.chat_list);
        adapter = new ChatMessageAdapter(this, messages);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Gson gson = new Gson();
        String json = gson.toJson(messages);
        prefsEditor.putString("messages", json);
        prefsEditor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.convo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delivered_button) {
            for (ChatMessage c : messages) {
                if (c.getStatus() != ChatMessage.STATUS_READ) {
                    c.setStatus(ChatMessage.STATUS_SENT);
                }
            }
            adapter.notifyDataSetChanged();
            return true;
        }

        if (id == R.id.read_button) {
            for (ChatMessage c : messages) {
                c.setStatus(ChatMessage.STATUS_READ);
            }
            adapter.notifyDataSetChanged();
            return true;
        }

        if (id == R.id.reply_button) {
            String message = typedMessage.getText().toString();
            Calendar calendar = Calendar.getInstance();
            if (!message.isEmpty()) {
                sendMessage(new ChatMessage(message, calendar, false));
                return true;
            }

            if (!messages.isEmpty()) {
                ChatMessage chatMessage = messages.get(messages.size() - 1);
                int mediaType = chatMessage.getMediaType();
                ChatMessage reply;

                if (mediaType == ChatMessage.MEDIA_NONE) {
                    reply = new ChatMessage(chatMessage.getText(), calendar, false);
                } else {
                    reply = new ChatMessage(calendar, false, mediaType);
                    reply.setCurrentThumbnailPath(chatMessage.getCurrentThumbnailPath());
                    reply.setCurrentMediaPath(chatMessage.getCurrentMediaPath());
                }

                sendMessage(reply);
            }
            return true;
        }

        if (id == R.id.delete_button) {
            prefsEditor.clear().commit();
            messages.clear();
            adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    public void goBack(View view) {
        startActivity(new Intent(ConversationActivity.this, MainActivity.class));
    }

    public void sendMessage(ChatMessage message) {
        boolean isSent = message.isSent();
        if (completionListener != null) {
            completionListener.onCompletion(mp);
        }
        if (isSent) {
            mp = MediaPlayer.create(this, R.raw.send_message);
        } else {
            mp = MediaPlayer.create(this, R.raw.incoming);
        }

        messages.add(message);
        adapter.notifyDataSetChanged();
        typedMessage.setText("");

        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mp.start();
        mp.setOnCompletionListener(completionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                audioManager.abandonAudioFocus(audioFocusChangeListener);
                mp.release();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cam_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.take_photo:
                dispatchTakePictureIntent();
                return true;
            case R.id.take_video:
                dispatchTakeVideoIntent();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            mediaMessage = new ChatMessage(Calendar.getInstance(), true, MEDIA_PHOTO);
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = mediaMessage.createImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                toastOnScreen = Toast.makeText(this, R.string.pic_error, Toast.LENGTH_SHORT);
                toastOnScreen.show();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            mediaMessage = new ChatMessage(Calendar.getInstance(), true, MEDIA_VIDEO);
            // Create the File where the video should go
            File videoFile = null;
            try {
                videoFile = mediaMessage.createVideoFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                toastOnScreen = Toast.makeText(this, R.string.pic_error, Toast.LENGTH_SHORT);
                toastOnScreen.show();
                return;
            }
            // Continue only if the File was successfully created
            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            mediaMessage.setPic(this);
            sendMessage(mediaMessage);
            mediaMessage = null;
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            mediaMessage.setVideo(this);
            sendMessage(mediaMessage);
            mediaMessage = null;
        }
    }
}
