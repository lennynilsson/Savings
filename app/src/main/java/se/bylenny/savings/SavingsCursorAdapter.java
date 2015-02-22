package se.bylenny.savings;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Locale;

import se.bylenny.savings.models.internal.SavingsGoal;
import se.bylenny.savings.models.internal.User;

/**
 * A cursor adapter for the savings database table
 */
public class SavingsCursorAdapter extends CursorAdapter {

    private static final String TAG = "SavingsCursorAdapter";
    private DecimalFormat enUS;
    private LayoutInflater inflater;
    private Typeface light;
    private Typeface regular;

    public SavingsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        initialize();
    }

    public SavingsCursorAdapter(Context context, Cursor c, int flags, RawRowMapper<SavingsGoal> mapper) {
        super(context, c, flags);
        initialize();
    }

    private void initialize() {
        light = Typeface.createFromAsset(mContext.getAssets(), "fonts/BentonSans-Light.otf");
        regular = Typeface.createFromAsset(mContext.getAssets(), "fonts/BentonSans-Regular.otf");
        enUS = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.US);
        enUS.applyLocalizedPattern("$###,###.#");
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.savings_goal_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Tag tag = (Tag) view.getTag();
        if (null == view.getTag()) {
            tag = new Tag();
            tag.imageView = (FixedImageView) view.findViewById(R.id.image);
            tag.titleView = (TextView) view.findViewById(R.id.title);
            tag.titleView.setTypeface(regular);
            tag.savedView = (TextView) view.findViewById(R.id.saved);
            tag.savedView.setTypeface(light);
            tag.progressView = (ProgressBar) view.findViewById(R.id.progress);
            tag.connectionsView = (LinearLayout) view.findViewById(R.id.connections);
            view.setTag(tag);
        }

        String[] columns = new String[cursor.getColumnCount()];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = cursor.getString(i);
        }

        SavingsGoal savingsGoal = null;
        try {
            Dao<SavingsGoal, Integer> dao = SavingsService
                    .getHelper(context.getApplicationContext()).getSavingsGoalDao();
            RawRowMapper<SavingsGoal> mapper = dao.getRawRowMapper();
            savingsGoal = mapper.mapRow(cursor.getColumnNames(), columns);
            dao.refresh(savingsGoal);
        } catch (SQLException e) {
            return;
        }
        // Load image
        Glide.with(context)
                .load(savingsGoal.getGoalImageUri())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.gray)
                .error(R.color.red)
                .into(tag.imageView);

        float current = null == savingsGoal.getCurrentBalance() ? 0.0f : savingsGoal.getCurrentBalance();
        float target = null == savingsGoal.getTargetAmount() ? 0.0f : savingsGoal.getTargetAmount();
        String text = String.format("%s saved of %s", enUS.format(current), enUS.format(target));
        int percent = Math.round(100 * (target != 0.0f ? current / target : 1.0f));

        tag.title = savingsGoal.getName();
        tag.image = savingsGoal.getGoalImageUri();
        tag.titleView.setText(savingsGoal.getName());
        tag.savedView.setText(text);
        tag.progressView.setProgress(percent);
        tag.connectionsView.removeAllViewsInLayout();

        ImageView portrait = null;
        CircularTransformation transformer = new CircularTransformation(context);
        Resources res = context.getResources();
        float imageDp = res.getDimension(R.dimen.image);
        int imagePixels = (int) (imageDp
                * (context.getResources().getDisplayMetrics().densityDpi / 160f));

        for (User user : savingsGoal.getConnectedUsers()) {
            String image = user.getAvatarUri();
            portrait = new ImageView(context);
            portrait.setMaxWidth(imagePixels);
            portrait.setMaxHeight(imagePixels);
            portrait.setMinimumWidth(imagePixels);
            portrait.setMinimumHeight(imagePixels);
            portrait.setPadding(10, 0, 10, 0);
            tag.connectionsView.addView(portrait);
            if (null != image) {
                // Load avatar
                Glide.with(context)
                        .load(image)
                        .fitCenter()
                        .override(imagePixels, imagePixels)
                        .placeholder(R.drawable.placeholder_cicle)
                        .error(R.drawable.error_circle)
                        .bitmapTransform(transformer)
                        .into(portrait);
            }
        }
    }

    public class Tag {
        public String image;
        public String title;
        public FixedImageView imageView;
        public TextView titleView;
        public TextView savedView;
        public ProgressBar progressView;
        public LinearLayout connectionsView;
    }
}