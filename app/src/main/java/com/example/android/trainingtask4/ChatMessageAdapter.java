package com.example.android.trainingtask4;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static com.example.android.trainingtask4.ChatMessage.MEDIA_NONE;
import static com.example.android.trainingtask4.ChatMessage.MEDIA_PHOTO;
import static com.example.android.trainingtask4.ChatMessage.MEDIA_VIDEO;

/**
 * Created by TQ on 20-Mar-18.
 */

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private int eightDp;
    private int sixteenDp;

    public ChatMessageAdapter(@NonNull Context context, @NonNull List<ChatMessage> objects) {
        super(context, 0, objects);
        eightDp = dpToPx(8);
        sixteenDp = dpToPx(16);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ChatMessage currentMessage = getItem(position);

        View chatView = convertView;
        if (chatView == null) {
            chatView = LayoutInflater.from(getContext()).inflate(
                    R.layout.chat_list_item, parent, false);
        }

        LinearLayout listLayout = chatView.findViewById(R.id.list_item_layout);

        TextView chatText = chatView.findViewById(R.id.chat_text);
        RelativeLayout thumbnailLayout = chatView.findViewById(R.id.thumbnail_layout);
        ImageView thumbnail = chatView.findViewById(R.id.thumbnail);
        ImageView playButton = chatView.findViewById(R.id.play_button);
        TextView timeStamp = chatView.findViewById(R.id.chat_timestamp);
        ImageView messageStatus = chatView.findViewById(R.id.message_status);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (currentMessage.isSent()) {
            listLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal);
            params.addRule(ALIGN_PARENT_RIGHT);
            listLayout.setLayoutParams(params);
            listLayout.setPadding(eightDp, eightDp, sixteenDp, eightDp);
            messageStatus.setVisibility(View.VISIBLE);
        } else {
            listLayout.setBackgroundResource(R.drawable.balloon_incoming_normal);
            params.addRule(ALIGN_PARENT_LEFT);
            listLayout.setLayoutParams(params);
            listLayout.setPadding(sixteenDp, eightDp, eightDp, eightDp);
            messageStatus.setVisibility(View.GONE);
        }

        final int mediaType = currentMessage.getMediaType();

        if (mediaType == MEDIA_NONE) {
            thumbnailLayout.setVisibility(View.GONE);
            playButton.setVisibility(View.GONE);
            chatText.setVisibility(View.VISIBLE);
            chatText.setText(currentMessage.getText());
        } else {
            chatText.setVisibility(View.GONE);
            playButton.setVisibility(View.GONE);
            thumbnailLayout.setVisibility(View.VISIBLE);
            thumbnail.setImageBitmap(BitmapFactory.decodeFile(currentMessage.getCurrentThumbnailPath()));
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mediaType == MEDIA_PHOTO) {
                        new PhotoFullPopupWindow(getContext(), R.layout.popup_photo_full, view, currentMessage.getCurrentMediaPath(), null);
                    } else {
                        new VideoFullPopupWindow(getContext(), R.layout.popup_video_full, view, currentMessage.getCurrentMediaPath());
                    }
                }
            });
        }
        if (mediaType == MEDIA_VIDEO) {
            playButton.setVisibility(View.VISIBLE);
        }

        Calendar calendar = currentMessage.getCalendar();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a");
        timeStamp.setText(DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(), 0) + getContext().getString(R.string.comma) + " " + dateFormatter.format(calendar.getTime()));
        messageStatus.setImageResource(currentMessage.getStatus());

        return chatView;
    }

    private int dpToPx(int dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int px = (int) (dp * scale + 0.5f);
        return px;
    }
}
