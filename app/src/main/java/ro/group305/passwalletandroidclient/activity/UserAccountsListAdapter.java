package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.eu.passwallet.model.UserAccount;
import ro.group305.passwalletandroidclient.model.UserAccountXmlUriDAO;

class UserAccountsListAdapter extends BaseAdapter implements Filterable {

    private final LayoutInflater inflater;

    private final Context context;

    private final int resource;

    private int dropDownResource;

    private List<Map<String, String>> accounts;

    private boolean objectsFromResources;

    private int fieldId = 0;

    private boolean notifyOnChange = true;

    private ArrayList<Map<String, String>> originalValues;
    private ArrayFilter filter;
    private String[] attributesToDisplay;
    private UserAccountXmlUriDAO userAccountDAO;
    private final Comparator nickNameComparator = new Comparator<UserAccount>() {
        @Override
        public int compare(UserAccount o1, UserAccount o2) {
            return o1.getNickName().compareToIgnoreCase(o2.getNickName());
        }
    };

    public UserAccountsListAdapter(@NonNull Context context, @LayoutRes int resource,
                                   @IdRes int textViewResourceId, @NonNull UserAccountXmlUriDAO userAccountDAO,
                                   @NonNull String[] attributeToDisplay) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.resource = dropDownResource = resource;
        this.userAccountDAO = userAccountDAO;
        accounts = getAdapterData(userAccountDAO.getSortedUserAccounts(nickNameComparator));
        fieldId = textViewResourceId;
        this.attributesToDisplay = attributeToDisplay;
    }

    public void updateUserAccountsList() {
        accounts = getAdapterData(userAccountDAO.getSortedUserAccounts(nickNameComparator));
        originalValues = null;
        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        notifyOnChange = true;
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public @Nullable
    Map<String, ?> getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView,
                 @NonNull ViewGroup parent) {
        return createViewFromResource(inflater, position, convertView, parent, resource);
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
            if (fieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(fieldId);

                if (text == null) {
                    throw new RuntimeException("Failed to find view with ID "
                            + context.getResources().getResourceName(fieldId)
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
        for (String attribute : attributesToDisplay) {
            itemText.append(item.get(attribute)).append(" ");
        }

        text.setText(itemText);

        if (this.context instanceof Activity) {
            ((Activity) UserAccountsListAdapter.this.context).registerForContextMenu(text);
            text.setOnClickListener(new android.view.View.OnClickListener()
            {
                public void onClick(View v)
                {
                    ((Activity) UserAccountsListAdapter.this.context).openContextMenu(v);
                    v.showContextMenu();

                }
            });
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createViewFromResource(inflater, position, convertView, parent, dropDownResource);
    }

    @Override
    public @NonNull
    Filter getFilter() {
        if (filter == null) {
            filter = new ArrayFilter();
        }
        return filter;
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        // First check if app developer explicitly set them.
        final CharSequence[] explicitOptions = super.getAutofillOptions();
        if (explicitOptions != null) {
            return explicitOptions;
        }

        // Otherwise, only return options that came from static resources.
        if (!objectsFromResources || accounts == null || accounts.isEmpty()) {
            return null;
        }
        final int size = accounts.size();
        final CharSequence[] options = new CharSequence[size];
        accounts.toArray(options);
        return options;
    }

    private List<Map<String, String>> getAdapterData(Collection<UserAccount> accounts) {
        List<Map<String, String>> data = new ArrayList<>();
        for (UserAccount userAccount : accounts) {
            data.add(transformUserAccountToMap(userAccount));
        }
        return data;
    }

    private Map<String, String> transformUserAccountToMap(UserAccount userAccount) {
        Map<String, String> data = new HashMap<>();
        data.put("id", userAccount.getId().toString().toLowerCase());
        data.put("description", userAccount.getDescription() == null ? "" : userAccount.getDescription().toLowerCase());
        data.put("name", userAccount.getName() == null ? "" : userAccount.getName().toLowerCase());
        data.put("nickName", userAccount.getNickName() == null ? "" : userAccount.getNickName().toLowerCase());
        data.put("pass", userAccount.getPassword() == null ? "" : userAccount.getPassword().toLowerCase());
        data.put("siteURL", userAccount.getSiteURL() == null ? "" : userAccount.getSiteURL().toLowerCase());
        return data;
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            if (originalValues == null) {
                originalValues = new ArrayList<Map<String, String>>(accounts);
            }

            if (prefix == null || prefix.length() == 0) {
                final ArrayList<Map<String, ?>> list;
                list = new ArrayList<>(originalValues);
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();
                List<UserAccount> filteredUsersAccounts = new ArrayList<>(userAccountDAO.findUsersAccountsByName(prefixString));
                Collections.sort(filteredUsersAccounts, nickNameComparator);
                final List<Map<String, String>> newValues = getAdapterData(filteredUsersAccounts);
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            accounts = (List<Map<String, String>>) results.values;
            if (accounts == null) {
                accounts = Collections.emptyList();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}