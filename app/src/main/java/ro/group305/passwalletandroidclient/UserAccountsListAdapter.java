package ro.group305.passwalletandroidclient;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.group305.passwallet.model.UserAccount;

public class UserAccountsListAdapter extends BaseAdapter implements Filterable {

    private final LayoutInflater mInflater;

    private final Context mContext;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private final int mResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private int mDropDownResource;

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<Map<String, String>> mAccounts;

    /**
     * Indicates whether the contents of {@link #mAccounts} came from static resources.
     */
    private boolean mObjectsFromResources;

    /**
     * If the inflated resource is not a TextView, {@code mFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private int mFieldId = 0;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mAccounts} is modified.
     */
    private boolean mNotifyOnChange = true;

    // A copy of the original mAccounts array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mAccounts will then only contain the filtered values.
    private ArrayList<Map<String, ?>> mOriginalValues;
    private ArrayFilter mFilter;
    private String[] attributeToDisplay;

    public UserAccountsListAdapter(@NonNull Context context, @LayoutRes int resource,
                                   @IdRes int textViewResourceId, @NonNull List<UserAccount> userAccountList,
                                   @NonNull String[] attributeToDisplay) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = mDropDownResource = resource;
        mAccounts = getAdapterData(userAccountList);
        mFieldId = textViewResourceId;
        this.attributeToDisplay = attributeToDisplay;
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        if (mOriginalValues != null) {
            mOriginalValues.clear();
        } else {
            mAccounts.clear();
        }
        mObjectsFromResources = false;
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    @Override
    public int getCount() {
        return mAccounts.size();
    }

    @Override
    public @Nullable
    Map<String, ?> getItem(int position) {
        return mAccounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView,
                 @NonNull ViewGroup parent) {
        return createViewFromResource(mInflater, position, convertView, parent, mResource);
    }

    private @NonNull
    View createViewFromResource(@NonNull LayoutInflater inflater, int position,
                                @Nullable View convertView, @NonNull ViewGroup parent, int resource) {
        final View view;
        final TextView text;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(mFieldId);

                if (text == null) {
                    throw new RuntimeException("Failed to find view with ID "
                            + mContext.getResources().getResourceName(mFieldId)
                            + " in item layout");
                }
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        final Map<String, ?> item = getItem(position);
        StringBuilder itemText = new StringBuilder();
        for (String attribute : attributeToDisplay) {
            itemText.append(item.get(attribute)).append(" ");
        }
        text.setText(itemText);

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createViewFromResource(mInflater, position, convertView, parent, mDropDownResource);
    }

    @Override
    public @NonNull
    Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        // First check if app developer explicitly set them.
        final CharSequence[] explicitOptions = super.getAutofillOptions();
        if (explicitOptions != null) {
            return explicitOptions;
        }

        // Otherwise, only return options that came from static resources.
        if (!mObjectsFromResources || mAccounts == null || mAccounts.isEmpty()) {
            return null;
        }
        final int size = mAccounts.size();
        final CharSequence[] options = new CharSequence[size];
        mAccounts.toArray(options);
        return options;
    }

    private List<Map<String,String>> getAdapterData(List<UserAccount> accounts) {
        List<Map<String,String>> data = new ArrayList<>();
        for (UserAccount userAccount : accounts) {
            data.add(transformUserAccountToMap(userAccount));
        }
        return data;
    }

    private Map<String,String> transformUserAccountToMap(UserAccount userAccount) {
        Map<String, String> data = new HashMap<>();
        data.put("id", userAccount.getId().toString().toLowerCase());
        data.put("description", userAccount.getDescription() == null ? "" : userAccount.getDescription().toLowerCase());
        data.put("name", userAccount.getName() == null ? "" : userAccount.getName().toLowerCase());
        data.put("nickName", userAccount.getNickName() == null ? "" : userAccount.getNickName().toLowerCase());
        data.put("pass", userAccount.getPassword() == null ? "" : userAccount.getPassword().toLowerCase());
        data.put("siteURL", userAccount.getSiteURL() == null ? "" : userAccount.getSiteURL().toLowerCase());
        return data;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<Map<String, ?>>(mAccounts);
            }

            if (prefix == null || prefix.length() == 0) {
                final ArrayList<Map<String, ?>> list;
                list = new ArrayList<Map<String, ?>>(mOriginalValues);
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                final ArrayList<Map<String, ?>> values;
                values = new ArrayList<Map<String, ?>>(mOriginalValues);

                final int count = values.size();
                final ArrayList<Map<String, ?>> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final Map<String, ?> value = values.get(i);
                    final String valueText = valueToString(value);

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        private String valueToString(Map<String, ?> value) {
            StringBuilder valuesString = new StringBuilder();
            for(Map.Entry<String, ?> entry : value.entrySet()) {
                valuesString.append(entry.getValue().toString()).append(" ");
            }
            return valuesString.toString();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mAccounts = (List<Map<String, String>>) results.values;
            if (mAccounts == null) {
                mAccounts = Collections.emptyList();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}