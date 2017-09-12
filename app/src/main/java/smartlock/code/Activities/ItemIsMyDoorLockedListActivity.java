package smartlock.code.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import smartlock.code.R;
import smartlock.code.DoorsContents.DoorContent;

import java.util.List;

/**
 * An activity representing a list of Doors.
 * This activity has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemIsMyDoorLockedDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemIsMyDoorLockedListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemismydoorlocked_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.itemismydoorlocked_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        //todo- http request to check if door is locked.
        if (findViewById(R.id.itemismydoorlocked_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }
    //endregion

    //region setupRecyclerView
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        try {
            //There's a list because we made an infrastructure for having more than one door
            List<DoorContent.DoorItem> obj = DoorContent.ITEMS;
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(obj));
        }
        catch(Exception ex)
        {
            int test;
            test=1;
        }
    }
    //endregion

    //This class is made to adapt the door to the specific view.
    //region SimpleItemRecyclerViewAdapter Class
    //this class's point is to adapt the item to the recycler view.
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DoorContent.DoorItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DoorContent.DoorItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.itemismydoorlocked_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);
            DoorContent.DoorItem chosenItem  = holder.mItem;
            chosenItem.RefreshDoorStatus();

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ItemIsMyDoorLockedDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        ItemIsMyDoorLockedDetailFragment fragment = new ItemIsMyDoorLockedDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.itemismydoorlocked_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemIsMyDoorLockedDetailActivity.class);
                        intent.putExtra(ItemIsMyDoorLockedDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        ItemIsMyDoorLockedDetailActivity.SetDoorMessage(holder.mItem.details);
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DoorContent.DoorItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
    //endregion
}
