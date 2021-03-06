package pics.sift.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tonicartos.superslim.LayoutManager;

import java.util.List;

import pics.sift.app.R;
import pics.sift.app.adapter.FavoritesAdapter;
import pics.sift.app.data.Favorite;
import pics.sift.app.data.Profile;
import pics.sift.app.data.util.Status;
import pics.sift.app.fragment.util.WebFragment;
import pics.sift.app.util.Objects;
import pics.sift.app.util.WebAction;

public class FavoritesFragment extends WebFragment {
    private static final String TAG = "FavoritesFragment";
    private static final String KEY_LAYOUT = "layout";
    private static final String KEY_PROFILE = "profile";

    private Profile m_profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        getRecyclerView(view).setLayoutManager(new LayoutManager(inflater.getContext()));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Parcelable layout = null;
        Profile profile = null;

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            profile = savedInstanceState.getParcelable(KEY_PROFILE);
            layout = savedInstanceState.getParcelable(KEY_LAYOUT);
        }

        setProfile(profile, layout);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_LAYOUT, getRecyclerView().getLayoutManager().onSaveInstanceState());
        savedInstanceState.putParcelable(KEY_PROFILE, m_profile);
    }

    @Override
    public void onStart() {
        super.onStart();
        onRefresh(false);
    }

    public Profile getProfile() {
        return m_profile;
    }

    public void setProfile(Profile profile) {
        setProfile(profile, null);
    }

    public void setProfile(Profile profile, Parcelable state) {
        if(!Objects.match(m_profile, profile)) {
            RecyclerView recyclerView = getRecyclerView();
            Context context = getActivity();
            List<Favorite> favorites = (profile != null) ? profile.getFavorites() : null;

            m_profile = profile;

            if(state == null) {
                state = recyclerView.getLayoutManager().onSaveInstanceState();
            }

            if(favorites != null && favorites.size() > 0) {
                recyclerView.setAdapter(new FavoritesAdapter(context, favorites, m_profile.getMeta()));
                getEmptyView().setText(R.string.none);
            } else {
                recyclerView.setAdapter(null);
                getEmptyView().setText(R.string.favorites_label_no_data);
            }

            if(state != null) {
                recyclerView.getLayoutManager().onRestoreInstanceState(state);
            }
        }
    }

    protected void onRefresh(final boolean animated) {
        Context context = getActivity();

        if(m_profile == null) {
            getProgressBar().setVisibility(View.VISIBLE);
        }

        getConnection().enqueue(context, Profile.createAction(context, (m_profile != null) ? m_profile : getSettings().getProfile()), new WebAction.ResultHandler<Profile>() {
            @Override
            public void onActionResult(Status status, Profile profile) {
                if(m_profile == null) {
                    getProgressBar().setVisibility(View.GONE);
                }

                if(profile != null) {
                    if(!Objects.match(m_profile, profile)) {
                        getSettings().setProfile(profile);
                    }

                    setProfile(profile);
                } else if(m_profile == null || animated) {
                    // TODO: Show error alert
                }
            }
        });
    }

    private TextView getEmptyView() {
        return (TextView)getView().findViewById(R.id.empty);
    }

    private RecyclerView getRecyclerView() {
        return getRecyclerView(getView());
    }

    private RecyclerView getRecyclerView(View view) {
        return (RecyclerView)view.findViewById(R.id.recycler_view);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progress_bar);
    }
}
