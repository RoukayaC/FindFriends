package roukaya.chelly.findfriends.ui.friends;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Closeable;

import roukaya.chelly.findfriends.DatabaseHelper;
import roukaya.chelly.findfriends.R;

public class FriendsListFragment extends Fragment implements FriendsAdapter.OnFriendActionListener {

    private FriendsAdapter adapter;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Set up RecyclerView
        setupRecyclerView(view);

        // Set up search functionality
        setupSearchView(view);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Get all friends from database
        Cursor cursor = dbHelper.getAllFriends();

        // Create and set adapter
        adapter = new FriendsAdapter(requireContext(), cursor);
        adapter.setOnFriendActionListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView(View view) {
        SearchView searchView = view.findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterFriends(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterFriends(newText);
                    return true;
                }
            });

            // Clear button click listener
            searchView.setOnCloseListener(() -> {
                refreshFriendsList();
                return false;
            });
        }
    }

    /**
     * Filter friends list based on search query
     */
    private void filterFriends(String query) {
        if (query.isEmpty()) {
            refreshFriendsList();
        } else {
            Cursor cursor = dbHelper.searchFriends(query);
            adapter.swapCursor(cursor);
        }
    }

    /**
     * Refresh the friends list with all friends
     */
    private void refreshFriendsList() {
        adapter.swapCursor(dbHelper.getAllFriends());
    }

    @Override
    public void onViewLocation(double latitude, double longitude) {
        // Open location in maps app
        try {
            // Create a geo URI with the location data
            Uri geoUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

            // Launch the map intent directly without checking for handlers
            // (Android guarantees that common intents like ACTION_VIEW with geo URIs
            // will be handled by the system)
            startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not open map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteFriend(long id) {
        try {
            boolean deleted = dbHelper.deleteFriend(id);
            if (deleted) {
                // Refresh the list
                refreshFriendsList();
                Toast.makeText(requireContext(), "Friend deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateFriend(long id) {
        // This would typically show a dialog to update friend details
        // For simplicity, just show a toast message
        Toast.makeText(requireContext(), "Update functionality to be implemented", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.swapCursor(null); // Close the cursor to prevent memory leaks
        }

        // Close the database helper
        if (dbHelper instanceof Closeable) {
            try {
                ((Closeable) dbHelper).close();
            } catch (Exception e) {
                // Log but don't crash on close error
               // e.printStackTrace();
            }
        }
    }
}