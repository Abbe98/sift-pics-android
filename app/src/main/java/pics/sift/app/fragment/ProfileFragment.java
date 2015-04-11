package pics.sift.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import pics.sift.app.R;
import pics.sift.app.data.Hyperlink;
import pics.sift.app.data.Profile;
import pics.sift.app.data.util.Status;
import pics.sift.app.fragment.util.WebFragment;
import pics.sift.app.util.Objects;
import pics.sift.app.util.Registration;
import pics.sift.app.util.WebAction;

public class ProfileFragment extends WebFragment {
    private static final String TAG = "ProfileFragment";
    private static final String KEY_PROFILE = "profile";

    private Profile m_profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Profile profile = null;
        CheckBox checkbox;

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            profile = savedInstanceState.getParcelable(KEY_PROFILE);
        } else {
            getLayout().setVisibility(View.GONE);
        }

        setProfile(profile);

        checkbox = getNotificationsCheckBox();

        if(checkPlayServices(false)) {
            Registration registration = getSettings().getRegistration();

            checkbox.setChecked((registration != null && registration.isValid()) ? true : false);
            checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean state) {
                    if(state) {
                        registerDevice(true);
                    } else {
                        unregisterDevice();
                    }
                }
            });
        } else {
            checkbox.setChecked(false);
            checkbox.setEnabled(false);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

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
        if(!Objects.match(m_profile, profile)) {
            Context context = getActivity();
            View layout = getLayout();

            m_profile = profile;

            if(m_profile != null) {
                Hyperlink link = m_profile.getLink();
                String summary = context.getResources().getQuantityString(R.plurals.profile_stats, m_profile.getTaggedCount(), m_profile.getTaggedCount(), m_profile.getPicturesCount());

                if(m_profile.getRank() != 0) {
                    summary = summary + " " + context.getResources().getString(R.string.album_stats_rank, m_profile.getRank());
                }

                layout.setVisibility(View.VISIBLE);
                getTitleView().setText(Html.fromHtml(summary));

                if(m_profile.getMessage() != null) {
                    getSubtitleView().setText(m_profile.getMessage());
                }

                if(link != null) {
                    getLinkView().setText(link.toHtml());
                } else {
                    getLinkView().setVisibility(View.GONE);
                }
            } else {
                layout.setVisibility(View.GONE);
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

    private View getLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    private TextView getTitleView() {
        return (TextView)getView().findViewById(R.id.text_title);
    }

    private TextView getSubtitleView() {
        return (TextView)getView().findViewById(R.id.text_subtitle);
    }

    private TextView getLinkView() {
        return (TextView)getView().findViewById(R.id.text_link);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progress_bar);
    }

    private CheckBox getNotificationsCheckBox() {
        return (CheckBox)getView().findViewById(R.id.checkbox_notifications);
    }
}
