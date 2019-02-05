package lestera.me.mypproject.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lestera.me.mypproject.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public abstract class ItemListFragment<T extends Parcelable> extends Fragment {
    private static final String LIST_KEY = "elements";

    private OnListFragmentInteractionListener mListener;

    protected ArrayList<T> list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            List<T> populatingList = getPopulatingList();
            list.addAll(populatingList != null ? populatingList : new ArrayList<>());
        } else {
            list.addAll(savedInstanceState.getParcelableArrayList(LIST_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(LIST_KEY, list);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof ListView) {
            Context context = view.getContext();
            ListView listView = (ListView) view;
            listView.setAdapter(new ListAdapter(context, getListViewItem(), list));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnListFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class ListAdapter extends ArrayAdapter<T> {
        private int layout;

        private ListAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);

            layout = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return ItemListFragment.this.getView(position, convertView, parent, layout, this::getItem);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener<T> {
        // TODO: Update argument type and name
        void onListFragmentInteraction(T item, int resourceId);
    }

    public abstract View getView(int position, View convertView, ViewGroup parent, int layout, Function<Integer, T> getItem);
    public abstract List<T> getPopulatingList();
    public abstract int getListViewItem();

    protected final void notifyInteraction(T item, int resourceId) {
        mListener.onListFragmentInteraction(item, resourceId);
    }
}
