package com.simpleagenda.app.ui.planning;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.ScheduledTaskWithTask;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.ui.common.TaskPalette;
import com.simpleagenda.app.util.TimeUtils;

import java.util.List;

/**
 * Grille horaire (6h–22h) avec blocs déplaçables verticalement.
 */
public class DayTimelineView extends FrameLayout {

    public interface BlockMoveListener {
        void onBlockMoved(long scheduledId, int newStartMinutesFromMidnight);
    }

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float pxPerMinute;
    private int labelGutterPx;
    private BlockMoveListener moveListener;

    private final int dayStartMin = AgendaRepository.DAY_START_MINUTES;
    private final int dayEndMin = AgendaRepository.DAY_END_MINUTES;

    public DayTimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        float density = getResources().getDisplayMetrics().density;
        float hourPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, getResources().getDisplayMetrics());
        pxPerMinute = hourPx / 60f;
        labelGutterPx = (int) (40f * density);

        gridPaint.setColor(ContextCompat.getColor(context, R.color.divider));
        gridPaint.setStrokeWidth(1f);

        labelPaint.setColor(ContextCompat.getColor(context, R.color.on_surface_muted));
        labelPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, getResources().getDisplayMetrics()));
    }

    public void setMoveListener(@Nullable BlockMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public void setBlocks(@NonNull List<ScheduledTaskWithTask> blocks) {
        removeAllViews();
        int totalMin = dayEndMin - dayStartMin;
        int totalHeight = Math.round(totalMin * pxPerMinute);
        setMinimumHeight(totalHeight);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (ScheduledTaskWithTask st : blocks) {
            Task task = st.getTask();
            if (task == null) {
                continue;
            }
            MaterialCardView card = (MaterialCardView) inflater.inflate(R.layout.item_timeline_block, this, false);
            TextView time = card.findViewById(R.id.block_time);
            TextView title = card.findViewById(R.id.block_title);

            int start = st.getScheduled().getStartMinutesFromMidnight();
            int end = start + task.durationMinutes();
            time.setText(getContext().getString(
                    R.string.time_range_format,
                    TimeUtils.formatTime(start),
                    TimeUtils.formatTime(end)
            ));
            title.setText(task.getTitle());
            int light = TaskPalette.lightBackground(getContext(), task.getColorIndex());
            card.setCardBackgroundColor(light);
            card.setStrokeWidth(0);

            int durationMin = task.durationMinutes();
            int top = Math.round((start - dayStartMin) * pxPerMinute);
            int height = Math.round(durationMin * pxPerMinute);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    height
            );
            lp.leftMargin = labelGutterPx + (int) (4 * getResources().getDisplayMetrics().density);
            lp.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
            lp.topMargin = top;

            long scheduledId = st.getScheduled().getId();

            View handle = card.findViewById(R.id.block_handle);
            handle.setOnTouchListener(new BlockDragHelper(scheduledId));

            addView(card, lp);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int totalMin = dayEndMin - dayStartMin;
        float totalHeight = totalMin * pxPerMinute;
        for (int h = 6; h <= 22; h++) {
            int minuteOfHour = h * 60;
            float y = (minuteOfHour - dayStartMin) * pxPerMinute;
            canvas.drawLine(labelGutterPx, y, getWidth(), y, gridPaint);
            canvas.drawText(h + "h", 8f, y + labelPaint.getTextSize(), labelPaint);
        }
        canvas.drawLine(labelGutterPx, 0, labelGutterPx, totalHeight, gridPaint);
    }

    private final class BlockDragHelper implements OnTouchListener {
        private final long scheduledId;

        private float downRawY;
        private int startTopMargin;

        BlockDragHelper(long scheduledId) {
            this.scheduledId = scheduledId;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            View card = v;
            while (card != null && !(card instanceof MaterialCardView)) {
                if (!(card.getParent() instanceof View)) {
                    break;
                }
                card = (View) card.getParent();
            }
            if (!(card instanceof MaterialCardView)) {
                return false;
            }
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) card.getLayoutParams();
            int totalMinSpan = dayEndMin - dayStartMin;
            int maxTop = Math.round(totalMinSpan * pxPerMinute) - lp.height;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRawY = event.getRawY();
                    startTopMargin = lp.topMargin;
                    card.setElevation(12f);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dy = event.getRawY() - downRawY;
                    int next = Math.round(startTopMargin + dy);
                    if (next < 0) {
                        next = 0;
                    }
                    if (next > maxTop) {
                        next = maxTop;
                    }
                    lp.topMargin = next;
                    card.setLayoutParams(lp);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    card.setElevation(4f);
                    int newStart = dayStartMin + Math.round(lp.topMargin / pxPerMinute);
                    if (moveListener != null) {
                        moveListener.onBlockMoved(scheduledId, newStart);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }
}
