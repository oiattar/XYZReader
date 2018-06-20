package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import java.util.Date;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.DateUtil;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import static com.example.xyzreader.util.DateUtil.parseStringDate;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mPhotoView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mToolbar = getActivity().findViewById(R.id.toolbar);
        mPhotoView = getActivity().findViewById(R.id.photo);
        FloatingActionButton mFab = mRootView.findViewById(R.id.share_fab);
        mCollapsingToolbarLayout = getActivity().findViewById(R.id.collapsing_toolbar);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();

        return mRootView;
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        final TextView titleView = mRootView.findViewById(R.id.article_title);
        TextView authorView = mRootView.findViewById(R.id.article_author);
        TextView dateView = mRootView.findViewById(R.id.article_date);
        TextView bodyView = mRootView.findViewById(R.id.article_text);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            Date publishedDate = parseStringDate(mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE));

            String articleDate = DateUtil.getSinceDate(publishedDate);
            String articleTitle = mCursor.getString(ArticleLoader.Query.TITLE);
            String articleAuthor = mCursor.getString(ArticleLoader.Query.AUTHOR);

            mToolbar.setTitle(articleTitle);
            titleView.setText(articleTitle);
            authorView.setText(articleAuthor);
            dateView.setText(articleDate);

            String articleText = mCursor.getString(ArticleLoader.Query.BODY);

            bodyView.setText(removeSingleLineBreaks(articleText));
            bodyView.setMovementMethod(new LinkMovementMethod());

            String imageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Glide.with(this)
                    .load(imageUrl)
                    .listener(GlidePalette.with(imageUrl)
                            .intoCallBack(new BitmapPalette.CallBack() {
                                @Override
                                public void onPaletteLoaded(@Nullable Palette palette) {
                                    if (palette != null) {
                                        Log.d(TAG, palette.toString());
                                        int mutedColor = palette.getMutedColor(ContextCompat.getColor(getActivityCast(), (R.color.colorPrimary)));
                                        int darkMutedColor = palette.getDarkMutedColor(ContextCompat.getColor(getActivityCast(), (R.color.colorPrimaryDark)));

                                        mCollapsingToolbarLayout.setContentScrimColor(mutedColor);
                                        mCollapsingToolbarLayout.setStatusBarScrimColor(darkMutedColor);
                                    }
                                }
                            })
                    )
                    .into(mPhotoView);
        } else {
            mRootView.setVisibility(View.GONE);
            mToolbar.setTitle("N/A");
            titleView.setText("N/A");
            authorView.setText("N/A");
            dateView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    private String removeSingleLineBreaks(String articleText) {
        String tempText = "newLineBreak";

        articleText = articleText.replaceAll("\r\n\r\n", tempText);
        articleText = articleText.replaceAll("\r\n", " ");
        articleText = articleText.replaceAll(tempText, "\r\n\r\n");

        articleText = articleText.replaceAll(" {2,}", " ");

        return articleText;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }
}
