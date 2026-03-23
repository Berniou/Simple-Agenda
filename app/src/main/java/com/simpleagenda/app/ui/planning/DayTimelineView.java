package com.simpleagenda.app.ui.planning;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.simpleagenda.app.R;
import com.simpleagenda.app.data.AgendaRepository;
import com.simpleagenda.app.data.ScheduledTaskWithTask;
import com.simpleagenda.app.data.Task;
import com.simpleagenda.app.ui.common.TaskPalette;
import com.simpleagenda.app.util.TimeUtils;

import java.util.List;

/**
 * Grille horaire (6h–22h) avec blocs déplaçables verticalement (tout le bloc est draggable).
 */
public class DayTimelineView extends FrameLayout {

    public interface BlockMoveListener {
        void onBlockMoved(long scheduledId, int newStartMinutesFromMidnight);
    }

    /** Glisser-déposer d’une tâche sélectionnée depuis la liste vers la grille. */
    public interface ExternalDropListener {
        void onTaskDroppedFromPalette(long taskId, int rawStartMinutesFromMidnight);
    }

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float pxPerMinute;
    private int labelGutterPx;
    private BlockMoveListener moveListener;
    private ExternalDropListener externalDropListener;

    private final int dayStartMin = AgendaRepository.DAY_START_MINUTES;
    private final int dayEndMin = AgendaRepository.DAY_END_MINUTES;

    private final int touchSlop;

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

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription() != null
                            && event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DROP:
                    return handlePaletteDrop(event.getClipData(), event.getY());
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    return true;
            }
        });
    }

    public void setMoveListener(@Nullable BlockMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public void setExternalDropListener(@Nullable ExternalDropListener externalDropListener) {
        this.externalDropListener = externalDropListener;
    }

    private boolean handlePaletteDrop(@Nullable ClipData clip, float yInTimeline) {
        if (externalDropListener == null || clip == null || clip.getItemCount() == 0) {
            return false;
        }
        CharSequence text = clip.getItemAt(0).getText();
        if (text == null) {
            return false;
        }
        long taskId;
        try {
            taskId = Long.parseLong(text.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        int totalMin = dayEndMin - dayStartMin;
        float totalH = totalMin * pxPerMinute;
        yInTimeline = Math.max(0, Math.min(yInTimeline, totalH - 1));
        int rawStart = dayStartMin + Math.round(yInTimeline / pxPerMinute);
        externalDropListener.onTaskDroppedFromPalette(taskId, rawStart);
        return true;
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

            card.setClickable(true);
            card.setOnTouchListener(new BlockDragHelper(scheduledId));

            card.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return event.getClipDescription() != null
                                && event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case DragEvent.ACTION_DROP:
                        FrameLayout.LayoutParams lpCard = (FrameLayout.LayoutParams) v.getLayoutParams();
                        return handlePaletteDrop(event.getClipData(), lpCard.topMargin + event.getY());
                    default:
                        return false;
                }
            });

            addView(card, lp);
        }
    }

    /**
     * Remonte depuis le bloc jusqu’au {@link NestedScrollView} pour que le scroll parent
     * ne capture pas le geste pendant le déplacement.
     */
    private static void requestAncestorsDisallowInterceptFrom(View child, boolean disallow) {
        ViewParent p = child.getParent();
        while (p != null) {
            p.requestDisallowInterceptTouchEvent(disallow);
            if (p instanceof NestedScrollView) {
                break;
            }
            p = p.getParent();
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
        private boolean dragging;

        BlockDragHelper(long scheduledId) {
            this.scheduledId = scheduledId;
        }

        @Override
        @SuppressWarnings("ClickableViewAccessibility")
        public boolean onTouch(View v, MotionEvent event) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
            int totalMinSpan = dayEndMin - dayStartMin;
            int maxTop = Math.round(totalMinSpan * pxPerMinute) - lp.height;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRawY = event.getRawY();
                    startTopMargin = lp.topMargin;
                    dragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float dy = event.getRawY() - downRawY;
                    if (!dragging) {
                        if (Math.abs(dy) <= touchSlop) {
                            return true;
                        }
                        dragging = true;
                        requestAncestorsDisallowInterceptFrom(v, true);
                        v.bringToFront();
                        v.setElevation(16f);
                    }
                    int next = Math.round(startTopMargin + dy);
                    if (next < 0) {
                        next = 0;
                    }
                    if (next > maxTop) {
                        next = maxTop;
                    }
                    lp.topMargin = next;
                    v.setLayoutParams(lp);
                    return true;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    requestAncestorsDisallowInterceptFrom(v, false);
                    v.setElevation(4f);
                    if (dragging && moveListener != null) {
                        int newStart = dayStartMin + Math.round(lp.topMargin / pxPerMinute);
                        moveListener.onBlockMoved(scheduledId, newStart);
                    }
                    return true;

                default:
                    return false;
            }
        }
    }
}
