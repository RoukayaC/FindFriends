package roukaya.chelly.findfriends.ui.friends;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import roukaya.chelly.findfriends.DatabaseHelper;
import roukaya.chelly.findfriends.R;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private final Context context;
    private Cursor cursor;
    private OnFriendActionListener listener;

    // Interface for handling friend actions
    public interface OnFriendActionListener {
        void onViewLocation(double latitude, double longitude);
        void onDeleteFriend(long id);
        void onUpdateFriend(long id);
    }

    public FriendsAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public void setOnFriendActionListener(OnFriendActionListener listener) {
        this.listener = listener;
    }

    /**
     * Update the cursor and notify the adapter about specific data changes
     */
    public void swapCursor(Cursor newCursor) {
        if (this.cursor != null) {
            this.cursor.close();
        }

        if (newCursor == null) {
            // If new cursor is null, notify removal of all items
            int oldCount = getItemCount();
            this.cursor = null;
            notifyItemRangeRemoved(0, oldCount);
            return;
        }

        int oldCount = getItemCount();
        this.cursor = newCursor;
        int newCount = getItemCount();

        // Handle different scenarios with specific change events
        if (oldCount == 0) {
            // If previously empty, notify insertion of all new items
            if (newCount > 0) {
                notifyItemRangeInserted(0, newCount);
            }
        } else if (newCount == 0) {
            // If now empty, notify removal of all items
            notifyItemRangeRemoved(0, oldCount);
        } else {
            // Data changed, use more specific notifications where possible
            int changedCount = Math.min(oldCount, newCount);
            notifyItemRangeChanged(0, changedCount);

            if (newCount > oldCount) {
                // Items added at the end
                notifyItemRangeInserted(oldCount, newCount - oldCount);
            } else if (oldCount > newCount) {
                // Items removed from the end
                notifyItemRangeRemoved(newCount, oldCount - newCount);
            }
        }
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) {
            return;
        }

        // Get data from cursor
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_LONGITUDE));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_REQUEST_TIME));

        // Format date
        String date = formatDate(timestamp);

        // Set data to views
        holder.phoneTextView.setText(phone);
        holder.coordsTextView.setText(String.format(Locale.getDefault(),
                "Lat: %.6f, Long: %.6f", latitude, longitude));
        holder.dateTextView.setText(date);

        // Save ID for reference in click listeners
        holder.itemView.setTag(id);

        // Set up button click listeners
        setupButtonListeners(holder, id, latitude, longitude);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void setupButtonListeners(FriendViewHolder holder, long id, double latitude, double longitude) {
        holder.viewButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewLocation(latitude, longitude);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteFriend(id);
            }
        });

        holder.updateButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateFriend(id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    /**
     * ViewHolder class for friend items
     */
    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        final TextView phoneTextView;
        final TextView coordsTextView;
        final TextView dateTextView;
        final Button viewButton;
        final Button deleteButton;
        final Button updateButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneTextView = itemView.findViewById(R.id.text_phone);
            coordsTextView = itemView.findViewById(R.id.text_coordinates);
            dateTextView = itemView.findViewById(R.id.text_date);
            viewButton = itemView.findViewById(R.id.button_view);
            deleteButton = itemView.findViewById(R.id.button_delete);
            updateButton = itemView.findViewById(R.id.button_update);
        }
    }
}